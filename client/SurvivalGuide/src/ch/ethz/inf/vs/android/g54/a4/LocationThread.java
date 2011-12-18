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
import java.util.Timer;
import java.util.TimerTask;

import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.util.SnapshotCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class LocationThread extends Thread {

	Context context;
	Timer periodicScanTimer = new Timer();
	WifiManager wifi;
	WifiScanReceiver scanReceiver;

	private class WifiScanReceiver extends BroadcastReceiver {
		SurvivalGuideActivity ui;

		public WifiScanReceiver(SurvivalGuideActivity ui) {
			super();
			this.ui = ui;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(scanReceiver);
			List<ScanResult> results = wifi.getScanResults();
			List<WifiReading> readings = new ArrayList<WifiReading>();
			for (ScanResult result : results) {
				readings.add(new WifiReading(result));
			}
			ui.showReadings(readings);
			try {
				Location locRes = Location.getFromReadings(readings);
				ui.postUpdateLocation(locRes);
			} catch (Exception e) {
				// TODO: Toast to indicate server error
				e.printStackTrace();
			}
			if (SurvivalGuideActivity.COLLECT_TEST_DATA) {
				SnapshotCache.storeSnapshot(readings, ui.snapshotName, ui);
			}
		}
	}

	/** Called when the service is created for the first time. */
	public LocationThread(SurvivalGuideActivity ui) {
		context = ui.getApplicationContext();
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		scanReceiver = new WifiScanReceiver(ui);
	}

	@Override
	public void run() {
		startPeriodicScan();
	}

	private void periodicScan() {
		if (isInterrupted()) {
			stopPeriodicScan();
		}
		if (!SurvivalGuideActivity.USE_TEST_DATA) {
			context.registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			wifi.startScan();
		} else {
			try {
				List<WifiReading> readings = SnapshotCache.getNextSnapshot(context);
				if (readings != null) {
					scanReceiver.ui.showReadings(readings);
					Location locRes = Location.getFromReadings(readings);
					scanReceiver.ui.postUpdateLocation(locRes);					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startPeriodicScan() {
		periodicScanTimer.schedule(
				new TimerTask() {
					@Override
					public void run() {
						periodicScan();
					}
				},
				0, // start right now
				1 * 60 * 1000 // every minute
				);
	}

	private void stopPeriodicScan() {
		periodicScanTimer.cancel();
	}

}
