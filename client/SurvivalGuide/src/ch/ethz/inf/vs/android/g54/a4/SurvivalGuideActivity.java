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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.types.Building;
import ch.ethz.inf.vs.android.g54.a4.types.LazyObject;
import ch.ethz.inf.vs.android.g54.a4.types.LazyObject.MessageStatus;
import ch.ethz.inf.vs.android.g54.a4.types.AccessPoint;
import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.Room;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.ui.MapTest;
import ch.ethz.inf.vs.android.g54.a4.ui.WifiReadingArrayAdapter;
import ch.ethz.inf.vs.android.g54.a4.util.U;

public class SurvivalGuideActivity extends Activity implements OnClickListener {
	private static final String TAG = "SurvivalGuideActivity";
	private static final int ROOMS_DIALOG = 1;

	// temp dummy data
	private static final String[] buildings = { "CAB", "HG", "IFW" };
	private static final String[] floors = { "C", "D", "E" };
	private static final String[] rooms = { "1", "2", "3.1", "3.2", "4", "5", "6.1", "6.2", "6.3", "6.5", "7", "8" };

	Handler handler;

	WifiManager wifi;
	WifiScanReceiver scanReceiver;

	ArrayAdapter<WifiReading> readingAdapter;
	TextView txt_room, txt_ap;
	ListView lst_networks;

	// List<WifiConfiguration> configuredNetworks;
	List<WifiReading> visibleNetworks;

	// TODO find a better way to save spinner selection
	String selectedBuilding, selectedFloor, selectedRoom;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		U.initContext(this);

		if (getLastNonConfigurationInstance() != null)
			visibleNetworks = (List<WifiReading>) getLastNonConfigurationInstance();

		handler = new Handler();

		try {
			Button btn_scan = (Button) findViewById(R.id.btn_scan);
			Button btn_location = (Button) findViewById(R.id.btn_location);
			txt_room = (TextView) findViewById(R.id.txt_room);
			txt_ap = (TextView) findViewById(R.id.txt_ap);
			lst_networks = (ListView) findViewById(R.id.lst_networks);

			btn_scan.setOnClickListener(this);
			btn_location.setOnClickListener(this);

			wifi = (WifiManager) getSystemService(WIFI_SERVICE);
			// configuredNetworks = wifi.getConfiguredNetworks();

			if (visibleNetworks == null)
				visibleNetworks = new ArrayList<WifiReading>();
			readingAdapter = new WifiReadingArrayAdapter(this, R.layout.scan_result_list_item, visibleNetworks);
			lst_networks.setAdapter(readingAdapter);
			lst_networks.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						Room r = (Room) LazyObject.get(visibleNetworks.get(position).ap.getQualifiedRoom(), Room.class);
						r.load(); // TODO remove once map url is there automatically
						startActivity(new Intent(SurvivalGuideActivity.this, MapTest.class)
								.putExtra(getPackageName() + ".ImageUrl", r.getMapUrl()));
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

	@Override
	protected Dialog onCreateDialog(int id) {
		AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (parent.getId()) {
				case R.id.spn_building:
					selectedBuilding = buildings[position];
					break;
				case R.id.spn_floor:
					selectedFloor = floors[position];
					break;
				case R.id.spn_room:
					selectedRoom = rooms[position];
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
		};

		switch (id) {
		case ROOMS_DIALOG:
			View room_dialog = getLayoutInflater().inflate(R.layout.room_dialog, null);

			Spinner spn_building = (Spinner) room_dialog.findViewById(R.id.spn_building);
			ArrayAdapter<String> buildingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					buildings);
			buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_building.setAdapter(buildingAdapter);
			spn_building.setOnItemSelectedListener(selectedListener);

			Spinner spn_floor = (Spinner) room_dialog.findViewById(R.id.spn_floor);
			ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					floors);
			floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_floor.setAdapter(floorAdapter);
			spn_floor.setOnItemSelectedListener(selectedListener);

			Spinner spn_room = (Spinner) room_dialog.findViewById(R.id.spn_room);
			ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					rooms);
			roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn_room.setAdapter(roomAdapter);
			spn_room.setOnItemSelectedListener(selectedListener);

			return new AlertDialog.Builder(SurvivalGuideActivity.this).setTitle(R.string.room_dialog_title)
					.setView(room_dialog).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							U.showToast(String.format("yay, we're going to %s %s %s", selectedBuilding, selectedFloor,
									selectedRoom));
						}
					}).create();
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
				if (r != null) {
					String roomID = r.toString();
					txt_room.setText(roomID);
					final Building b = r.getFloor().getBuilding();
					b.loadAsync(new Handler() {
						public void handleMessage(Message msg) {
							if (msg.what == MessageStatus.SUCCESS.ordinal()) {
								// post success
								txt_ap.setText(b.getAddress().getCampus());
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
					for (WifiReading reading : visibleNetworks) {
						reading.ap = aps.get(reading.mac);
					}
					readingAdapter.notifyDataSetChanged();
				} else {
					U.showToast("no info about aps");
				}
			} catch (ServerException e) {
				U.showException(TAG, e);
			} catch (ConnectionException e) {
				U.showException(TAG, e);
			} catch (UnrecognizedResponseException e) {
				U.showException(TAG, e);
			}
			break;
		}
	}

	private void loadDummyData(boolean allowNonexistant) {
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
				"ff:ff:ff:ff:ff:a", // NON-EXISTANT
				"ff:ff:ff:ff:ff:b", // NON-EXISTANT
				"ff:ff:ff:ff:ff:c", // NON-EXISTANT
				"ff:ff:ff:ff:ff:d", // NON-EXISTANT
				"ff:ff:ff:ff:ff:e", // NON-EXISTANT
				"ff:ff:ff:ff:ff:f", // NON-EXISTANT
		};
		String[] mactypes = { "eth", "public", "MOBILE-EAPSIM", "eduroam" };
		Random rand = new Random();
		int count = rand.nextInt(maxcount - mincount) + mincount;
		visibleNetworks.clear();
		for (int i = 0; i < count; i++) {
			int macidx = rand.nextInt(allowNonexistant ? macs.length : macs.length - 6);
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

}
