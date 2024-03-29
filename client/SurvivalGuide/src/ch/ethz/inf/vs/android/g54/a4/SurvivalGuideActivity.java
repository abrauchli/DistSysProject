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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.ethz.inf.vs.android.g54.a4.types.AccessPoint;
import ch.ethz.inf.vs.android.g54.a4.types.Address;
import ch.ethz.inf.vs.android.g54.a4.types.Address.Campus;
import ch.ethz.inf.vs.android.g54.a4.types.Building;
import ch.ethz.inf.vs.android.g54.a4.types.Coordinate;
import ch.ethz.inf.vs.android.g54.a4.types.Floor;
import ch.ethz.inf.vs.android.g54.a4.types.LazyObject;
import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.Room;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.ui.LocationMarker;
import ch.ethz.inf.vs.android.g54.a4.ui.LocationMarker.OnMarkerClickListener;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView.OnSizeChangedListener;
import ch.ethz.inf.vs.android.g54.a4.ui.WifiReadingArrayAdapter;
import ch.ethz.inf.vs.android.g54.a4.util.MapCache;
import ch.ethz.inf.vs.android.g54.a4.util.SnapshotCache;
import ch.ethz.inf.vs.android.g54.a4.util.U;

public class SurvivalGuideActivity extends Activity {

	// collect wifi snapshots to enable testing when not at ETH
	protected static final boolean COLLECT_TEST_DATA = false;
	// simulate getting ETH wifi data with collected snapshots when not at ETH
	protected static final boolean USE_TEST_DATA = false;

	private static final int BUILDING_MARKER_RADIUS = 100;
	private static final int LOCATION_MARKER_RADIUS = 20;
	private static final String TAG = "SurvivalGuideActivity";

	private final MainUiListener mainUiListener = new MainUiListener();
	private final RoomDialogListener roomDialogListener = new RoomDialogListener();

	protected String snapshotName = "snapshot";

	private UIMode uiMode;
	private ScanningMode scanningMode;
	private Campus currentCampus;
	private Building currentBuilding;
	private Floor currentFloor;
	private Location currentLocation;
	private LocationThread locationThread;
	Handler handler;

	ArrayAdapter<WifiReading> wifiAdapter;

	Button btn_prev_floor;
	Button btn_next_floor;
	TouchImageView tiv_map;
	List<LocationMarker> markers;

	List<WifiReading> visibleNetworks;
	String selectedBuilding, selectedFloor, selectedRoom;

	/**
	 * Every marker that uses this handler needs to have a building identifier as name, otherwise the effects of this
	 * method are not defined
	 */
	private OnMarkerClickListener buildingClickListener =
			new OnMarkerClickListener() {
				public void onClick(LocationMarker marker) {

					final String buildingID = marker.getName();
					Building b = Building.getBuilding(buildingID);

					Handler h = new Handler() {
						public void handleMessage(Message msg) {
							if (msg.what == LazyObject.MessageStatus.SUCCESS.ordinal()) {
								Building b = (Building) msg.obj;
								List<Floor> floors = b.getFloors();
								if (!floors.isEmpty()) {
									Floor eFloor = null;
									for (Floor floor : floors) {
										if (floor.getName().equals("E")) {
											eFloor = floor;
										}
									}
									if (eFloor == null) {
										eFloor = floors.get(0);
									}
									currentBuilding = b;
									currentFloor = eFloor;
									setUIMode(UIMode.DETAILED);
								} else {
									U.showToast("There are no floors in this building.");
								}
							} else {
								Log.e(TAG, "Could not load building " + buildingID);
							}
						}
					};
					b.loadAsync(h);
				}
			};

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
		scanningMode = ScanningMode.OFF;
		locationThread = new LocationThread(this);
		initCampus(Campus.ZENTRUM);

		try {
			tiv_map = (TouchImageView) findViewById(R.id.tiv_map);
			ImageButton tgl_scan = (ImageButton) findViewById(R.id.tgl_scan);
			btn_prev_floor = (Button) findViewById(R.id.btn_prev_floor);
			btn_next_floor = (Button) findViewById(R.id.btn_next_floor);
			RadioGroup grp_campus = (RadioGroup) findViewById(R.id.grp_campus);

			tgl_scan.setOnClickListener(mainUiListener);
			btn_prev_floor.setOnClickListener(mainUiListener);
			btn_next_floor.setOnClickListener(mainUiListener);
			grp_campus.setOnCheckedChangeListener(mainUiListener);
			for (int i = 0; i < grp_campus.getChildCount(); i++) {
				RadioButton rbt = (RadioButton) grp_campus.getChildAt(i);
				rbt.setOnClickListener(mainUiListener);
			}

			markers = new ArrayList<LocationMarker>();

			// the TouchImageView has to have a defined size before updating the map
			OnSizeChangedListener updateMapOnce = new OnSizeChangedListener() {
				public void onSizeChanged(int viewWidth, int viewHeight) {
					updateMap();
					tiv_map.setOnSizeChangedListener(null);
				}
			};

			tiv_map.setOnSizeChangedListener(updateMapOnce);
			tiv_map.setMarkers(markers);

			if (visibleNetworks == null)
				visibleNetworks = new ArrayList<WifiReading>();
			wifiAdapter = new WifiReadingArrayAdapter(this, R.layout.scan_result_list_item, visibleNetworks);

			uiMode = UIMode.OVERVIEW;
			LinearLayout lin_building = (LinearLayout) findViewById(R.id.lin_building);
			lin_building.setVisibility(View.GONE);
		} catch (Exception e) {
			U.showException(TAG, e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem fr = menu.findItem(R.id.mni_free_room);
		fr.setEnabled(this.currentBuilding != null); // also applies when current floor is set
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mni_rooms:
			showDialog(R.layout.room_dialog);
			break;
		case R.id.mni_dummy_data:
			loadTestData();
			break;
		case R.id.mni_aps:
			showDialog(R.layout.aps_dialog);
			break;
		case R.id.mni_snapshot_name:
			showDialog(R.layout.snapshot_dialog);
			break;
		case R.id.mni_free_room:
			Handler h = new Handler() {
				@Override
				public void handleMessage(Message msg) {
				    super.handleMessage(msg);
				    if (msg.what == LazyObject.MessageStatus.FAILURE.ordinal()
				    		|| msg.obj == null) {
						U.showToast("Could not load free room info");
						return;
				    }
				    @SuppressWarnings("unchecked")
                    List<Room> freerooms = (List<Room>) msg.obj;
				    if (freerooms.size() == 0) {
				    	U.showToast("No free rooms found");
				    } else {
				    	U.showToast("Found "
				    			+ freerooms.size()
				    			+ " free rooms. Try "
				    			+ freerooms.get(0).getId(), Toast.LENGTH_LONG);
				    }
				}
			};
			if (this.currentFloor != null) {
				this.currentFloor.getFreeRooms(h);
			} else if (this.currentBuilding != null) {
				this.currentBuilding.getFreeRooms(h);
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
		locationThread.interrupt();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (scanningMode != ScanningMode.OFF) {
			locationThread = new LocationThread(this);
			locationThread.start();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (visibleNetworks != null)
			return visibleNetworks;
		return super.onRetainNonConfigurationInstance();
	}
	
	private Point approximateLocation(List<AccessPoint> aps, Floor limitToFloor) {
		int x = 0, y = 0, weight = 0;
		for (AccessPoint ap : aps) {
			if (ap.getQualifiedFloor().equals(limitToFloor.toString())) {
					Point p = ap.getCoordinate().toPoint();
					x += p.x;
					y += p.y;
					weight += 1;
			}
		}
		if (weight == 0)
			return null;
		else
			return new Point(x/weight, y/weight);
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

	private void loadTestData() {
		List<WifiReading> readings = SnapshotCache.getRandomSnapshot(this);
		if (readings != null) {
			showReadings(readings);

			// Old location button functionality
			Location locRes;
			try {
				locRes = Location.getFromReadings(readings);
				updateLocation(locRes);
			} catch (Exception e) {
				U.showException(TAG, e);
			}
		} else {
			U.showToast("Could not load test data.");
		}
	}

	/**
	 * When changing campus, always use this method, since it updates the radio buttons and the map
	 */
	private void setCampus(Campus campus) {
		this.currentCampus = campus;
		RadioGroup grp_campus = (RadioGroup) findViewById(R.id.grp_campus);
		switch (grp_campus.getCheckedRadioButtonId()) {
		case R.id.rbt_eth_center:
			// if the current campus is hoengg, but the checked radio button is center
			// (happens if we set the campus not with the buttons, e.g. when location changes)
			// we now need to check center, or else the current campus and the checked
			// button would be inconsistent
			if (currentCampus.equals(Campus.HOENGG)) {
				RadioButton rbt_eth_center = (RadioButton) findViewById(R.id.rbt_eth_center);
				rbt_eth_center.setChecked(true);
			}
			break;
		case R.id.rbt_eth_hoengg:
			// if the current campus is center, but the checked radio button is hoengg
			// (happens if we set the campus not with the buttons, e.g. when location changes)
			// we now need to check center, or else the current campus and the checked
			// button would be inconsistent
			if (currentCampus.equals(Campus.ZENTRUM)) {
				RadioButton rbt_eth_hoengg = (RadioButton) findViewById(R.id.rbt_eth_hoengg);
				rbt_eth_hoengg.setChecked(true);
			}
		}
	}

	private void setFloor(String floorName) {
		currentFloor = Floor.getFloor(currentBuilding, floorName);
		updateFloorButtons();
		updateMap();
	}

	/**
	 * When changing the uiMode, always use this method, as it updates the layout as well
	 */
	private void setUIMode(UIMode uiMode) {
		this.uiMode = uiMode;
		LinearLayout lin_building = (LinearLayout) findViewById(R.id.lin_building);
		switch (this.uiMode) {
		case OVERVIEW:
			lin_building.setVisibility(View.GONE);
			break;
		case DETAILED:
			lin_building.setVisibility(View.VISIBLE);
			TextView txt_building = (TextView) findViewById(R.id.txt_building);
			txt_building.setText(currentBuilding.getName());
			updateFloorButtons();
			break;
		}
		updateMap();
	}

	/**
	 * When switching the location scanning mode, always use this method, as it changes the image of the button and
	 * starts/stops the scanning service
	 */
	private void cycleLocationScanning() {
		ImageButton tgl_scan = (ImageButton) findViewById(R.id.tgl_scan);
		switch (scanningMode) {
		case OFF:
			scanningMode = ScanningMode.BACKGROUND;
			tgl_scan.setImageResource(R.drawable.target_on);
			locationThread = new LocationThread(this);
			locationThread.start();
			break;
		case BACKGROUND:
			scanningMode = ScanningMode.AUTOMATIC;
			tgl_scan.setImageResource(R.drawable.target_lock);
			break;
		case AUTOMATIC:
			scanningMode = ScanningMode.OFF;
			tgl_scan.setImageResource(R.drawable.target_off);
			locationThread.interrupt();
			break;
		}
	}

	/**
	 * Updates the markers for the access points as well as the marker for the location
	 */
	private void updateAPMarkers() {
		markers.clear();
		List<AccessPoint> aps = new ArrayList<AccessPoint>();

		float blueHue = 240;
		float orangeHue = 30;
		for (WifiReading reading : visibleNetworks) {
			if (reading.ap != null) {
				aps.add(reading.ap);
				Coordinate coords = reading.ap.getCoordinate();
				int s = reading.signal;
				float saturation;
				// since values better than -60 dBm are rare, let them use only a small part of the scale
				if (s < -60)
					saturation = (s + 100) * 0.03f;
				else
					saturation = (s + 60) * 0.005f + 0.3f;
				// FIXME
				int blueish = Color.HSVToColor(new float[] { blueHue, saturation, 1 });
				int orangeish = Color.HSVToColor(new float[] { orangeHue, saturation / 2, 1 });
				if (reading.ap.getQualifiedFloor().equals(currentFloor.toString())) {
					markers.add(new LocationMarker(coords.toPoint(), 100, blueish, reading.mac));
				} else {
					markers.add(new LocationMarker(coords.toPoint(), 120, orangeish, reading.mac));
				}
			}
		}

		if (currentLocation != null && currentLocation.getRoom().getFloor().equals(currentFloor)) {
			Point approxLoc = approximateLocation(aps, currentLocation.getRoom().getFloor());
			if (approxLoc != null) {
				markers.add(new LocationMarker(approxLoc, LOCATION_MARKER_RADIUS, Color.RED,
						"Your approximate location"));
				tiv_map.centerZoomPoint(approxLoc);
			} else {
				tiv_map.centerBitmap();
			}
		} else {
			tiv_map.centerBitmap();
		}

		tiv_map.updateMarkers();
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
	 * Renames and enables/disables floor buttons according to current building and floor
	 */
	private void updateFloorButtons() {
		List<Floor> floors = currentBuilding.getFloors();

		Collections.sort(floors, Floor.byName);

		int currentFloorIndex = floors.indexOf(currentFloor);

		// update button of current floor
		Button btn_curr_floor = (Button) findViewById(R.id.btn_curr_floor);
		btn_curr_floor.setText(floors.get(currentFloorIndex).getName());

		// update button of previous floor
		Button btn_prev_floor = (Button) findViewById(R.id.btn_prev_floor);
		if (currentFloorIndex > 0) {
			btn_prev_floor.setText(floors.get(currentFloorIndex - 1).getName());
			btn_prev_floor.setEnabled(true);
		} else {
			btn_prev_floor.setText("");
			btn_prev_floor.setEnabled(false);
		}

		// update button of next floor
		Button btn_next_floor = (Button) findViewById(R.id.btn_next_floor);
		if (currentFloorIndex < floors.size() - 1) {
			btn_next_floor.setText(floors.get(currentFloorIndex + 1).getName());
			btn_next_floor.setEnabled(true);
		} else {
			btn_next_floor.setText("");
			btn_next_floor.setEnabled(false);
		}
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

	private void updateMap() {
		Bitmap bm;
		switch (uiMode) {
		case OVERVIEW:
			tiv_map.recycleBitmaps();
			bm = BitmapFactory.decodeResource(getResources(), currentCampus == Campus.ZENTRUM
					? R.drawable.zentrum
					: R.drawable.hoengg);
			tiv_map.setImage(bm);
			tiv_map.centerBitmap();
			Map<String, Point> buildingsLocations = currentCampus == Campus.ZENTRUM
					? Building.buildingLocationsCenter
					: Building.buildingLocationsHoengg;
			markers.clear();
			for (Map.Entry<String, Point> bLoc : buildingsLocations.entrySet()) {
				markers.add(new LocationMarker(bLoc.getValue(), BUILDING_MARKER_RADIUS, Color.TRANSPARENT,
						bLoc.getKey(), buildingClickListener));
			}
			tiv_map.updateMarkers();
			tiv_map.centerZoomPoint(buildingsLocations.get(currentCampus == Campus.ZENTRUM ? "HG" : "HPH"));
			break;
		case DETAILED:
			tiv_map.recycleBitmaps();
			bm = MapCache.getMap(currentFloor, this);
			if (bm != null) {
				tiv_map.setImage(bm);
				tiv_map.centerBitmap();
			} else {
				U.showToast("No map available for this floor!");
				tiv_map.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.no_map));
				tiv_map.centerZoomBitmap();
			}
			updateAPMarkers();
			break;
		}
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

	/**
	 * Creates the rooms dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case R.layout.room_dialog:
			View room_dialog = getLayoutInflater().inflate(R.layout.room_dialog, null);

			// initialize radio buttons
			RadioGroup grp_rm_campus = (RadioGroup) room_dialog.findViewById(R.id.grp_rm_campus);
			grp_rm_campus.setOnCheckedChangeListener(roomDialogListener);

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
			spn_building.setOnItemSelectedListener(roomDialogListener);
			spn_building.setClickable(false);

			// initialize floor spinner
			Spinner spn_floor = (Spinner) room_dialog.findViewById(R.id.spn_floor);
			ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					floors);
			floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_floor.setAdapter(floorAdapter);
			spn_floor.setOnItemSelectedListener(roomDialogListener);
			spn_floor.setClickable(false);

			// initialize room spinner
			Spinner spn_room = (Spinner) room_dialog.findViewById(R.id.spn_room);
			ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					rooms);
			roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_room.setAdapter(roomAdapter);
			spn_room.setOnItemSelectedListener(roomDialogListener);
			spn_room.setClickable(false);

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
		case R.layout.aps_dialog:
			Dialog dialog = new Dialog(this);

			dialog.setContentView(R.layout.aps_dialog);
			dialog.setTitle(R.string.aps_dialog_title);

			ListView lst_aps = (ListView) dialog.findViewById(R.id.lst_aps);
			lst_aps.setAdapter(wifiAdapter);

			// TODO: subscribe to location changes, such that this may change in the dialog
			TextView txt_current_location = (TextView) dialog.findViewById(R.id.txt_current_location);
			if ((currentLocation != null) && (currentLocation.getRoom() != null)) {
				Room room = currentLocation.getRoom();
				String roomDescription = room.toString();
				Address address = room.getFloor().getBuilding().getAddress();
				if ((address != null) && (address.getCampus() != Campus.OTHER)) {
					roomDescription = String.format("%s (%s)", roomDescription, address.getCampus().name);
				} else {
				}
				txt_current_location.setText(roomDescription);
			} else {
				txt_current_location.setText(R.string.unknown_location);
			}
			return dialog;
		case R.layout.snapshot_dialog:
			View snapshot_dialog = getLayoutInflater().inflate(R.layout.snapshot_dialog, null);
			final EditText edt_snapshot = (EditText) snapshot_dialog.findViewById(R.id.edt_snapshot);
			edt_snapshot.setText(snapshotName);
			return new AlertDialog.Builder(SurvivalGuideActivity.this)
					.setTitle(R.string.snapshot_name)
					.setView(snapshot_dialog)
					.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							snapshotName = edt_snapshot.getText().toString();
						}
					})
					.create();

		}
		return null;
	}

	void showReadings(final List<WifiReading> readings) {
		visibleNetworks.clear();
		visibleNetworks.addAll(readings);
		Collections.sort(visibleNetworks, WifiReading.bySignal);
		wifiAdapter.notifyDataSetChanged();
	}

	/**
	 * updates the location according to the wifi data
	 * cannot be called from another tread, use postUpdateLocation instead
	 */
	private void updateLocation(Location location) {
		this.currentLocation = location;
		if (scanningMode == ScanningMode.AUTOMATIC) {
			if (currentLocation.getRoom() != null) {
				this.currentFloor = currentLocation.getRoom().getFloor();
				this.currentBuilding = currentFloor.getBuilding();
				Handler h = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == LazyObject.MessageStatus.SUCCESS.ordinal()) {
							Building b = (Building) msg.obj;
							setCampus(b.getAddress().getCampus());
							updateMap();
						} else {
							Log.e(TAG, "Could not load building " + currentBuilding.toString());
						}
					}
				};
				currentBuilding.loadAsync(h);
			}
		}

		Map<String, AccessPoint> aps = location.getAps();
		for (WifiReading reading : visibleNetworks) {
			reading.ap = aps.get(reading.mac);
		}
		wifiAdapter.notifyDataSetChanged();
	}

	/**
	 * updates the location according to the wifi data
	 * can be called from another tread
	 */
	protected void postUpdateLocation(final Location location) {
		handler.post(new Runnable() {
			public void run() {
				updateLocation(location);
			}
		});
	}

	private class MainUiListener implements OnClickListener, OnCheckedChangeListener {
		public void onCheckedChanged(RadioGroup group, int checkedID) {
			switch (checkedID) {
			case R.id.rbt_eth_center:
				// from main.xml
				setCampus(Campus.ZENTRUM);
				break;
			case R.id.rbt_eth_hoengg:
				// from main.xml
				setCampus(Campus.HOENGG);
				break;
			}
		}

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tgl_scan:
				cycleLocationScanning();
				break;
			case R.id.rbt_eth_center:
			case R.id.rbt_eth_hoengg:
				setUIMode(UIMode.OVERVIEW);
				break;
			case R.id.btn_prev_floor:
				setFloor(btn_prev_floor.getText().toString());
				break;
			case R.id.btn_next_floor:
				setFloor(btn_next_floor.getText().toString());
				break;
			}
		}
	}

	private enum UIMode {
		OVERVIEW,
		DETAILED
	}

	private enum ScanningMode {
		OFF,
		BACKGROUND,
		AUTOMATIC
	}

	private class RoomDialogListener implements OnCheckedChangeListener, OnItemSelectedListener {
		public void onCheckedChanged(RadioGroup group, int checkedID) {
			View roomDialogView = (View) group.getParent().getParent();
			switch (checkedID) {
			case R.id.rbt_rm_eth_center:
				// from room_dialog.xml
				try {
					List<Building> buildings = Building.getBuildings(Campus.ZENTRUM);
					updateBuildingsList(roomDialogView, buildings);
				} catch (Exception e) {
					U.postException(handler, TAG, e);
				}
				break;
			case R.id.rbt_rm_eth_hoengg:
				// from room_dialog.xml
				try {
					List<Building> buildings = Building.getBuildings(Campus.HOENGG);
					updateBuildingsList(roomDialogView, buildings);
				} catch (Exception e) {
					U.postException(handler, TAG, e);
				}
				break;
			case R.id.rbt_rm_eth_all:
				// from room_dialog.xml
				try {
					List<Building> buildings = Building.getAllBuildings();
					updateBuildingsList(roomDialogView, buildings);
				} catch (Exception e) {
					U.postException(handler, TAG, e);
				}
				break;
			}
		}

		@SuppressWarnings("unchecked")
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			final View v = (View) parent.getParent().getParent();
			ArrayAdapter<String> sa;
			Building b;
			Handler h;
			switch (parent.getId()) {
			case R.id.spn_building:
				Spinner spn_building = (Spinner) v.findViewById(R.id.spn_building);
				sa = (ArrayAdapter<String>) spn_building.getAdapter();
				selectedBuilding = sa.getItem(position);
				selectedFloor = "";
				selectedRoom = "";

				b = Building.getBuilding(selectedBuilding);
				h = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == LazyObject.MessageStatus.SUCCESS.ordinal()) {
							Building b = (Building) msg.obj;
							updateFloorsList(v, b.getFloors());
						} else {
							Log.e(TAG, "Could not load building " + currentBuilding.toString());
						}
					}
				};
				b.loadAsync(h); // only loads if needed
				break;
			case R.id.spn_floor:
				Spinner spn_floor = (Spinner) v.findViewById(R.id.spn_floor);
				sa = (ArrayAdapter<String>) spn_floor.getAdapter();
				selectedFloor = sa.getItem(position);
				selectedRoom = "";

				b = Building.getBuilding(selectedBuilding); // building is already loaded
				final Floor f = Floor.getFloor(b, selectedFloor);

				h = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == LazyObject.MessageStatus.SUCCESS.ordinal()) {
							Floor f = (Floor) msg.obj;
							List<Room> rooms = f.getRooms();
							updateRoomsList(v, rooms);
						} else {
							Log.e(TAG, "Could not load floor " + f.toString());
						}
					}
				};
				f.loadAsync(h); // only loads if needed
				break;
			case R.id.spn_room:
				Spinner spn_room = (Spinner) v.findViewById(R.id.spn_room);
				sa = (ArrayAdapter<String>) spn_room.getAdapter();
				selectedRoom = sa.getItem(position);
				break;
			}
		}

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
	}

	private class StringComparator implements Comparator<String> {
		public int compare(String lhs, String rhs) {
			return lhs.compareTo(rhs);
		}
	}

}
