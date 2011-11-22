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
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ch.ethz.inf.vs.android.g54.a4.net.*;
import ch.ethz.inf.vs.android.g54.a4.types.*;

public class SurvivalGuideActivity extends Activity implements OnClickListener {
	private static final String TAG = "SurvivalGuideActivity";

	Handler handler;

	WifiManager wifi;
	WifiScanReceiver receiver;

	ArrayAdapter<WifiReading> adapter;
	TextView txt_room, txt_ap;
	ListView lst_networks;

	// List<WifiConfiguration> configuredNetworks;
	List<WifiReading> visibleNetworks;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (getLastNonConfigurationInstance() != null)
			visibleNetworks = (List<WifiReading>) getLastNonConfigurationInstance();

		handler = new Handler();
		RequestHandler.getInstance().setContext(this);

		try {
			Button btn_scan = (Button) findViewById(R.id.btn_scan);
			Button btn_dummy = (Button) findViewById(R.id.btn_dummy);
			Button btn_location = (Button) findViewById(R.id.btn_location);
			txt_room = (TextView) findViewById(R.id.txt_room);
			txt_ap = (TextView) findViewById(R.id.txt_ap);
			lst_networks = (ListView) findViewById(R.id.lst_networks);

			btn_scan.setOnClickListener(this);
			btn_dummy.setOnClickListener(this);
			btn_location.setOnClickListener(this);

			wifi = (WifiManager) getSystemService(WIFI_SERVICE);
			// configuredNetworks = wifi.getConfiguredNetworks();

			if (visibleNetworks == null)
				visibleNetworks = new ArrayList<WifiReading>();
			adapter = new WifiReadingArrayAdapter(this, R.layout.scan_result_list_item, visibleNetworks);
			lst_networks.setAdapter(adapter);
			receiver = new WifiScanReceiver(this);
		} catch (Exception e) {
			showException(TAG, e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
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
		case R.id.btn_dummy:
			loadDummyData();
			break;
		case R.id.btn_location:
			Location location = Location.getFromReadings(visibleNetworks);
			if (location == null) {
				showToast("getting location failed");

			} else if (!location.isValid()) {
				showToast("no position found");

			} else {
				String buildingID = location.getNearestRoom().getID();
				txt_room.setText(buildingID);
				txt_ap.setText("");
			}
			break;
		}
	}

	private void loadDummyData() {
		String[] macs = { "00:0f:61:1a:18:4", // air-cab-e32-a
				"00:03:52:1c:34:5", // air-cab-e16-a
				"00:03:52:29:ae:4", // air-cab-e45-a
				"00:03:52:1b:f6:5", // air-cab-g11-a
				"00:0f:61:1a:20:8", // air-cab-e22-2-a
				"55:44:33:22:11:0" }; // NON-EXISTANT
		String[] mactypes = {"eth", "public", "MOBILE-EAPSIM", "eduroam"};
		Random rand = new Random();
		int count = rand.nextInt(4)+1;
		visibleNetworks.clear();
		for (int i = 0; i < count; i++) {
			int macidx = rand.nextInt(macs.length);
			int mactype = rand.nextInt(4);
			int signal = rand.nextInt(70) - 90; // -21 to -90
			visibleNetworks.add(new WifiReading(macs[macidx] + mactype, mactypes[mactype], signal));
		}
		adapter.notifyDataSetChanged();
	}

	private void showReadings(final List<WifiReading> readings) {
		visibleNetworks.clear();
		visibleNetworks.addAll(readings);
		adapter.notifyDataSetChanged();
		/*
		 * // going via handler does not seem to be necessary, not sure handler.post(new Runnable() {
		 * 
		 * @Override public void run() { // visibleNetworks.clear(); visibleNetworks.addAll(results);
		 * adapter.notifyDataSetChanged(); } });
		 */
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

	/*
	 * Methods for displaying errors
	 */

	private String formatException(Exception e) {
		return e.getClass().getSimpleName() + ": " + e.getMessage();
	}

	private void showException(String tag, Exception e) {
		String exDesc = formatException(e);
		Log.e(tag, exDesc);
		Log.e(tag, Log.getStackTraceString(e));
		showToastLong(exDesc);
	}

	private void postException(String tag, Exception e) {
		String exDesc = formatException(e);
		Log.e(tag, exDesc);
		Log.e(tag, Log.getStackTraceString(e));
		postToastLong(exDesc);
	}

	/*
	 * Logic for showing general information
	 */

	private void postToast(final String text) {
		handler.post(new Runnable() {
			public void run() {
				showToast(text);
			}
		});
	}

	private void postToastLong(final String text) {
		handler.post(new Runnable() {
			public void run() {
				showToastLong(text);
			}
		});
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void showToastLong(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

}