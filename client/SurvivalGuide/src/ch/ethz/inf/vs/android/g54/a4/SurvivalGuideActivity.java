/*
 * This file is part of SurvivalGuide
 * Copyleft 2011 The SurvivalGuide Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.inf.vs.android.g54.a4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.types.AccessPoint;
import ch.ethz.inf.vs.android.g54.a4.types.Address.Campus;
import ch.ethz.inf.vs.android.g54.a4.types.Building;
import ch.ethz.inf.vs.android.g54.a4.types.Coordinate;
import ch.ethz.inf.vs.android.g54.a4.types.Floor;
import ch.ethz.inf.vs.android.g54.a4.types.LazyObject.MessageStatus;
import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.Room;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.ui.LocationMarker;
import ch.ethz.inf.vs.android.g54.a4.ui.MapTest;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView.OnSizeChangedListener;
import ch.ethz.inf.vs.android.g54.a4.ui.WifiReadingArrayAdapter;
import ch.ethz.inf.vs.android.g54.a4.util.MapCache;
import ch.ethz.inf.vs.android.g54.a4.util.U;

public class SurvivalGuideActivity extends Activity implements OnClickListener, CompoundButton.OnCheckedChangeListener,
		RadioGroup.OnCheckedChangeListener, OnItemSelectedListener {
	private static final String TAG = "SurvivalGuideActivity";
	private static final int ROOMS_DIALOG = 1;

	private enum Mode {
		OVERVIEW,
		FREEROOMS,
		LOCATION
	}

	private Mode mode;
	private Campus currentCampus;
	private Building currentBuilding;
	private Floor currentFloor;
	private Location currentLocation;
	private boolean locationScanning;

	Handler handler;

	WifiManager wifi;
	WifiScanReceiver scanReceiver;

	ArrayAdapter<WifiReading> readingAdapter;
	TextView txt_room, txt_ap;
	ListView lst_networks;
	TouchImageView tiv_map;

	List<LocationMarker> markers;

	// List<WifiConfiguration> configuredNetworks;
	List<WifiReading> visibleNetworks;

	// TODO find a better way to save spinner selection
	String selectedBuilding, selectedFloor, selectedRoom;

	/**
	 * When changing the mode, always use this method, as it updates the layout as well
	 */
	private void setMode(Mode mode) {
		this.mode = mode;
		LinearLayout lin_building = (LinearLayout) findViewById(R.id.lin_building);
		switch (this.mode) {
		case OVERVIEW:
			lin_building.setVisibility(View.GONE);
			// TODO: switch to correct image
			break;
		case LOCATION:
			lin_building.setVisibility(View.VISIBLE);
			// TODO: switch to correct image
			break;
		case FREEROOMS:
			lin_building.setVisibility(View.VISIBLE);
			// TODO: switch to correct image
			break;
		}
	}

	/**
	 * When toggling the location scanning, always use this method, as it changes the image of the button as well
	 */
	private void toggleLocationScanning() {
		locationScanning = !locationScanning;
		ImageButton tgl_scan = (ImageButton) findViewById(R.id.tgl_scan);
		if (locationScanning) {
			tgl_scan.setImageResource(R.drawable.target_on);
			// TODO: start thread/service scanning locations
		} else {
			tgl_scan.setImageResource(R.drawable.target);
			// TODO: stop thread/service scanning locations
		}
	}

	/**
	 * When changing campus, always use this method, since TODO: explanation
	 */
	private void setCampus(Campus campus) {
		this.currentCampus = campus;
		RadioGroup grp_campus = (RadioGroup) findViewById(R.id.grp_campus);
		switch (grp_campus.getCheckedRadioButtonId()) {
		case R.id.rbt_eth_center:
			if (currentCampus.equals(Campus.HOENGG)) {
				RadioButton rbt_eth_center = (RadioButton) findViewById(R.id.rbt_eth_center);
				rbt_eth_center.setChecked(true);
			}
			break;
		case R.id.rbt_eth_hoengg:
			if (currentCampus.equals(Campus.ZENTRUM)) {
				RadioButton rbt_eth_hoengg = (RadioButton) findViewById(R.id.rbt_eth_hoengg);
				rbt_eth_hoengg.setChecked(true);
			}
		}
	}

	/**
	 * Initialize the campus and set the radio buttons accordingly
	 */
	private void initCampus(Campus campus) {
		this.currentCampus = campus;
		switch (campus) {
		case ZENTRUM:
			RadioButton rbt_eth_center = (RadioButton) findViewById(R.id.rbt_eth_center);
			rbt_eth_center.setChecked(true);
			break;
		case HOENGG:
			RadioButton rbt_eth_hoengg = (RadioButton) findViewById(R.id.rbt_eth_hoengg);
			rbt_eth_hoengg.setChecked(true);
		}
	}
	
	private void setLocation(Location location) {
		this.currentLocation = location;
		// TODO: finish this method
	}
	
	private void setBuilding(Building building) {
		this.currentBuilding = building;
		// TODO: finish this method
	}
	
	private void setFloor(Floor floor) {
		this.currentFloor = floor;
		// TODO: finish this method
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		U.initContext(this);

		if (getLastNonConfigurationInstance() != null)
			visibleNetworks = (List<WifiReading>) getLastNonConfigurationInstance();

		handler = new Handler();
		
		// TODO: probably needs to be made consistent, e.g. when turning phone...
		locationScanning = false;
		initCampus(Campus.ZENTRUM);
		setMode(Mode.OVERVIEW);

		try {
			Button btn_scan = (Button) findViewById(R.id.btn_scan);
			Button btn_location = (Button) findViewById(R.id.btn_location);
			txt_room = (TextView) findViewById(R.id.txt_room);
			txt_ap = (TextView) findViewById(R.id.txt_ap);
			ToggleButton tgl_map = (ToggleButton) findViewById(R.id.tgl_map);
			lst_networks = (ListView) findViewById(R.id.lst_networks);
			tiv_map = (TouchImageView) findViewById(R.id.tiv_map);
			ImageButton tgl_scan = (ImageButton) findViewById(R.id.tgl_scan);

			btn_scan.setOnClickListener(this);
			btn_location.setOnClickListener(this);
			tgl_map.setOnCheckedChangeListener(this);
			tgl_scan.setOnClickListener(this);

			wifi = (WifiManager) getSystemService(WIFI_SERVICE);
			// configuredNetworks = wifi.getConfiguredNetworks();

			markers = new ArrayList<LocationMarker>();
			// Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.hg_e);

			OnSizeChangedListener hideOnce = new OnSizeChangedListener() {
				public void onSizeChanged(int viewWidth, int viewHeight) {
					tiv_map.setVisibility(View.GONE);
					tiv_map.setOnSizeChangedListener(null);
				}
			};

			tiv_map.setOnSizeChangedListener(hideOnce);
			// tiv_map.setImage(bm);
			tiv_map.setMarkers(markers);
			// tiv_map.updateMarkers();
			// tiv_map.centerZoomPoint(200, 200);

			if (visibleNetworks == null)
				visibleNetworks = new ArrayList<WifiReading>();
			readingAdapter = new WifiReadingArrayAdapter(this, R.layout.scan_result_list_item, visibleNetworks);
			lst_networks.setAdapter(readingAdapter);
			lst_networks.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						AccessPoint ap = visibleNetworks.get(position).ap;
						if (ap == null) {
							U.showToast("no further AP info available");
						} else {
							U.showToast("temporarily disabled"); // TODO
						}

					} catch (Exception e) {
						U.showException(TAG, e);
					}
				}
			});
			scanReceiver = new WifiScanReceiver(this);
		} catch (Exception e) {
			U.showException(TAG, e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(scanReceiver);
	}

	/**
	 * From extending Activity
	 * 
	 * Creates the rooms dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ROOMS_DIALOG:
			View room_dialog = getLayoutInflater().inflate(R.layout.room_dialog, null);

			// initialize radio buttons
			RadioGroup grp_campus = (RadioGroup) room_dialog.findViewById(R.id.grp_campus);
			grp_campus.setOnCheckedChangeListener(this);

			// initialize spinners
			List<String> buildings = new ArrayList<String>();
			List<String> floors = new ArrayList<String>();
			List<String> rooms = new ArrayList<String>();

			// initialize building spinner
			Spinner spn_building = (Spinner) room_dialog.findViewById(R.id.spn_building);
			ArrayAdapter<String> buildingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					buildings);
			buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_building.setAdapter(buildingAdapter);
			spn_building.setOnItemSelectedListener(this);
			spn_building.setClickable(false);

			// initialize floor spinner
			Spinner spn_floor = (Spinner) room_dialog.findViewById(R.id.spn_floor);
			ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					floors);
			floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_floor.setAdapter(floorAdapter);
			spn_floor.setOnItemSelectedListener(this);
			spn_floor.setClickable(false);

			// initialize room spinner
			Spinner spn_room = (Spinner) room_dialog.findViewById(R.id.spn_room);
			ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					rooms);
			roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_room.setAdapter(roomAdapter);
			spn_room.setOnItemSelectedListener(this);
			spn_room.setClickable(false);

			// TODO: what does this thing?
			return new AlertDialog.Builder(SurvivalGuideActivity.this)
					.setTitle(R.string.room_dialog_title)
					.setView(room_dialog)
					.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							U.showToast(String.format("yay, we're going to %s %s %s", selectedBuilding, selectedFloor,
									selectedRoom));
						}
					})
					.create();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mni_rooms:
			showDialog(ROOMS_DIALOG);
			break;
		case R.id.mni_dummy_data:
			loadDummyData(false);
			break;
		case R.id.mni_map:
			Intent foo = new Intent(this, MapTest.class);
			foo.putExtra(getPackageName() + ".ImageUrl", "http://deserver.moeeeep.com:32123/static/cache/CAB_G_11.gif");
			startActivity(foo);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (visibleNetworks != null)
			return visibleNetworks;
		return super.onRetainNonConfigurationInstance();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_scan:
			wifi.startScan();
			break;
		case R.id.btn_location:
			Location locRes;
			try {
				locRes = Location.getFromReadings(visibleNetworks);
				Room r = locRes.getRoom();
				Map<String, AccessPoint> aps = locRes.getAps();
				markers.clear();
				if (r != null) {
					tiv_map.recycleBitmaps();
					tiv_map.setImage(MapCache.getMap(r.getFloor(), this));
					String roomID = r.toString();
					txt_room.setText(roomID);
					final Building b = r.getFloor().getBuilding();
					b.loadAsync(new Handler() {
						public void handleMessage(Message msg) {
							if (msg.what == MessageStatus.SUCCESS.ordinal()) {
								// post success
								txt_ap.setText(b.getAddress().getCampus().name);
							} else if (msg.what == MessageStatus.FAILURE.ordinal()) {
								// post failure
								// txt_ap.setText(msg.getData().getString("message"));
							} else {
								// post failure
								// txt_ap.setText("something weird happened");
							}
						}
					});
				} else {
					U.showToast("no position found");
				}
				if (aps != null) {
					float blueHue = 240;
					float greenHue = 120;
					float orangeHue = 30;
					for (WifiReading reading : visibleNetworks) {
						reading.ap = aps.get(reading.mac);
						if (reading.ap != null) {
							Coordinate coords = reading.ap.getCoordinate();
							Point pos = new Point((int) coords.getX(), (int) coords.getY());
							int s = reading.signal;
							float saturation;
							if (s < -60)
								saturation = (s + 100) * 0.02f;
							else
								saturation = (s + 60) * 0.005f + 0.8f;
							// FIXME
							markers.add(new LocationMarker(pos, 100, Color
									.HSVToColor(new float[] { blueHue, saturation, 1 }), reading.mac));
							markers.add(new LocationMarker(pos, 120, Color
									.HSVToColor(new float[] { orangeHue, saturation, 1 }), reading.mac));
							markers.add(new LocationMarker(pos, 80, Color
									.HSVToColor(new float[] { greenHue, saturation, 1 }), reading.mac));
						}
					}
					readingAdapter.notifyDataSetChanged();

				} else {
					U.showToast("no info about aps");
				}
				if (r != null) {
					Coordinate center = r.getRoomCenter();
					if (center != null) {
						markers.add(new LocationMarker(new Point((int) center.getX(), (int) center.getY()), 20,
								Color.RED, "Your approximate location"));
						tiv_map.centerZoomPoint((int) center.getX(), (int) center.getY());
					}
				}
				tiv_map.updateMarkers();
			} catch (ServerException e) {
				U.showException(TAG, e);
			} catch (ConnectionException e) {
				U.showException(TAG, e);
			} catch (UnrecognizedResponseException e) {
				U.showException(TAG, e);
			}
			break;
		case R.id.tgl_scan:
			toggleLocationScanning();
			break;
		}
	}

	private void loadDummyData(boolean allowNonexistent) {
		int mincount = 3, maxcount = 8;
		String[] macs = {
				//
				"00:0f:61:1a:0c:5", // air-cab-e10-4-a
				"00:03:52:1c:14:b", // air-cab-e11-a
				"00:03:52:1c:14:d", // air-cab-e12-a
				"00:03:52:1c:34:5", // air-cab-e16-a
				"00:0f:61:1a:20:8", // air-cab-e22-2-a
				"00:0f:61:5d:dd:5", // air-cab-e27-1-a
				"00:0f:61:1a:18:4", // air-cab-e32-a
				"00:03:52:29:ae:4", // air-cab-e45-a
				"00:03:52:1c:11:b", // air-cab-f42-1-a
				"00:03:52:1c:32:9", // air-cab-f63-1-a
				"00:03:52:1b:f6:5", // air-cab-g11-a
				"00:03:52:1b:f4:f", // air-cab-g11-b
				"00:03:52:1c:31:c", // air-cab-g20-1-a
				"00:03:52:5c:34:f", // air-hg-g5-b
				"00:03:52:d8:2d:a", // air-hg-f3-a
				"ff:ff:ff:ff:ff:a", // NON-EXISTENT
				"ff:ff:ff:ff:ff:b", // NON-EXISTENT
				"ff:ff:ff:ff:ff:c", // NON-EXISTENT
				"ff:ff:ff:ff:ff:d", // NON-EXISTENT
				"ff:ff:ff:ff:ff:e", // NON-EXISTENT
				"ff:ff:ff:ff:ff:f", // NON-EXISTENT
		};
		String[] mactypes = { "eth", "public", "MOBILE-EAPSIM", "eduroam" };
		Random rand = new Random();
		int count = rand.nextInt(maxcount - mincount) + mincount;
		visibleNetworks.clear();
		for (int i = 0; i < count; i++) {
			int macidx = rand.nextInt(allowNonexistent ? macs.length : macs.length - 6);
			int mactype = rand.nextInt(4);
			int signal = rand.nextInt(70) - 90; // -21 to -90
			visibleNetworks.add(new WifiReading(macs[macidx] + mactype, mactypes[mactype], signal));
		}
		showReadings(visibleNetworks);
	}

	private void showReadings(final List<WifiReading> readings) {
		if (readings != visibleNetworks) {
			visibleNetworks.clear();
			visibleNetworks.addAll(readings);
		}
		Collections.sort(visibleNetworks, WifiReading.bySignal);
		readingAdapter.notifyDataSetChanged();
	}

	private class WifiScanReceiver extends BroadcastReceiver {
		SurvivalGuideActivity scanner;

		public WifiScanReceiver(SurvivalGuideActivity scanner) {
			super();
			this.scanner = scanner;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			List<ScanResult> results = scanner.wifi.getScanResults();
			List<WifiReading> readings = new ArrayList<WifiReading>();
			for (ScanResult result : results) {
				readings.add(new WifiReading(result));
			}
			scanner.showReadings(readings);
		}
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.tgl_map:
			lst_networks.setVisibility(isChecked ? View.GONE : View.VISIBLE);
			tiv_map.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			break;
		}
	}

	/**
	 * From implementing OnCheckedChangeListener
	 * 
	 * Listens to changes of radio buttons
	 */
	public void onCheckedChanged(RadioGroup group, int checkedID) {
		View v = (View) group.getParent().getParent();
		switch (checkedID) {
		case R.id.rbt_eth_center:
			try {
				List<Building> buildings = Building.getBuildings(Campus.ZENTRUM);
				updateBuildingsList(v, buildings);
			} catch (Exception e) {
				U.postException(handler, TAG, e);
			}
			break;
		case R.id.rbt_eth_hoengg:
			try {
				List<Building> buildings = Building.getBuildings(Campus.HOENGG);
				updateBuildingsList(v, buildings);
			} catch (Exception e) {
				U.postException(handler, TAG, e);
			}
			break;
		case R.id.rbt_eth_all:
			try {
				List<Building> buildings = Building.getAllBuildings();
				updateBuildingsList(v, buildings);
			} catch (Exception e) {
				U.postException(handler, TAG, e);
			}
			break;
		}
	}

	/**
	 * From implementing OnItemSelectedListener
	 * 
	 * Manages clicks on buildings/floors/rooms in the rooms dialog
	 */
	@SuppressWarnings("unchecked")
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		View v = (View) parent.getParent().getParent();
		ArrayAdapter<String> sa;
		Building b;
		Floor f;
		switch (parent.getId()) {
		case R.id.spn_building:
			Spinner spn_building = (Spinner) v.findViewById(R.id.spn_building);
			sa = (ArrayAdapter<String>) spn_building.getAdapter();
			selectedBuilding = sa.getItem(position);
			selectedFloor = "";
			selectedRoom = "";

			b = Building.getBuilding(selectedBuilding);
			try {
				b.load(); // only loads if needed
				updateFloorsList(v, b.getFloors());
			} catch (Exception e) {
				U.postException(handler, TAG, e);
			}
			break;
		case R.id.spn_floor:
			Spinner spn_floor = (Spinner) v.findViewById(R.id.spn_floor);
			sa = (ArrayAdapter<String>) spn_floor.getAdapter();
			selectedFloor = sa.getItem(position);
			selectedRoom = "";

			b = Building.getBuilding(selectedBuilding); // building is already loaded
			f = Floor.getFloor(b, selectedFloor);
			try {
				f.load(); // only loads if needed
				List<Room> rooms = f.getRooms();
				updateRoomsList(v, rooms);
			} catch (Exception e) {
				U.postException(handler, TAG, e);
			}
			break;
		case R.id.spn_room:
			Spinner spn_room = (Spinner) v.findViewById(R.id.spn_room);
			sa = (ArrayAdapter<String>) spn_room.getAdapter();
			selectedRoom = sa.getItem(position);
			break;
		}
	}

	/**
	 * From implementing OnItemSelectedListener
	 * 
	 * Manages clicks on buildings/floors/rooms in the rooms dialog
	 */
	public void onNothingSelected(AdapterView<?> parent) {
		switch (parent.getId()) {
		case R.id.spn_building:
			selectedBuilding = "";
			break;
		case R.id.spn_floor:
			selectedFloor = "";
			break;
		case R.id.spn_room:
			selectedRoom = "";
			break;
		}
	}

	/**
	 * Updates the list of the building spinner in the rooms dialog
	 * 
	 * @param v
	 *            View where to find the spinners
	 * @param buildings
	 *            List of buildings to put into the dropdown list of the spinner
	 */
	@SuppressWarnings("unchecked")
	private void updateBuildingsList(View v, List<Building> buildings) {
		Spinner spn_building = (Spinner) v.findViewById(R.id.spn_building);
		Spinner spn_floor = (Spinner) v.findViewById(R.id.spn_floor);
		Spinner spn_room = (Spinner) v.findViewById(R.id.spn_room);

		// update building spinner
		ArrayAdapter<String> sa = (ArrayAdapter<String>) spn_building.getAdapter();
		sa.clear();
		for (Building b : buildings) {
			sa.add(b.getName());
		}
		sa.sort(new StringComparator());
		sa.notifyDataSetChanged();
		spn_building.setClickable(true);

		// update floor spinner
		sa = (ArrayAdapter<String>) spn_floor.getAdapter();
		sa.clear();
		sa.notifyDataSetChanged();
		spn_room.setClickable(false);

		// update room spinner
		sa = (ArrayAdapter<String>) spn_room.getAdapter();
		sa.clear();
		sa.notifyDataSetChanged();
		spn_room.setClickable(false);
	}

	/**
	 * Updates the list of the floor spinner in the rooms dialog
	 * 
	 * @param v
	 *            View where to find the spinners
	 * @param floors
	 *            List of floors to put into the dropdown list of the spinner
	 */
	@SuppressWarnings("unchecked")
	private void updateFloorsList(View v, List<Floor> floors) {
		Spinner spn_building = (Spinner) v.findViewById(R.id.spn_building);
		Spinner spn_floor = (Spinner) v.findViewById(R.id.spn_floor);
		Spinner spn_room = (Spinner) v.findViewById(R.id.spn_room);

		ArrayAdapter<String> sa;

		// update building spinner
		spn_building.setClickable(true);

		// update floor spinner
		sa = (ArrayAdapter<String>) spn_floor.getAdapter();
		sa.clear();
		for (Floor f : floors) {
			sa.add(f.getName());
		}
		sa.sort(new StringComparator());
		sa.notifyDataSetChanged();
		spn_floor.setClickable(true);

		// update room spinner
		sa = (ArrayAdapter<String>) spn_room.getAdapter();
		sa.clear();
		sa.notifyDataSetChanged();
		spn_room.setClickable(false);
	}

	/**
	 * Updates the list of the room spinner in the rooms dialog
	 * 
	 * @param v
	 *            View where to find the spinners
	 * @param rooms
	 *            List of rooms to put into the dropdown list of the spinner
	 */
	@SuppressWarnings("unchecked")
	private void updateRoomsList(View v, List<Room> rooms) {
		Spinner spn_building = (Spinner) v.findViewById(R.id.spn_building);
		Spinner spn_floor = (Spinner) v.findViewById(R.id.spn_floor);
		Spinner spn_room = (Spinner) v.findViewById(R.id.spn_room);

		ArrayAdapter<String> sa;

		// update building spinner
		spn_building.setClickable(true);

		// update floor spinner
		spn_floor.setClickable(true);

		// update room spinner
		sa = (ArrayAdapter<String>) spn_room.getAdapter();
		sa.clear();
		for (Room r : rooms) {
			sa.add(r.getName());
		}
		sa.sort(new StringComparator());
		sa.notifyDataSetChanged();
		spn_room.setClickable(true);
	}

	private class StringComparator implements Comparator<String> {
		public int compare(String lhs, String rhs) {
			return lhs.compareTo(rhs);
		}
	}

}
