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
			List<ScanResult> results = wifi.getScanResults();
			List<WifiReading> readings = new ArrayList<WifiReading>();
			for (ScanResult result : results) {
				readings.add(new WifiReading(result));
			}
			ui.showReadings(readings);
			try {
				Location locRes = Location.getFromReadings(readings);
				ui.updateLocation(locRes);
			} catch (Exception e) {
				// TODO: Toast to indicate server error
				e.printStackTrace();
			}
			if (SurvivalGuideActivity.COLLECT_SNAPSHOTS) {
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
		wifi.startScan();
	}

	private void startPeriodicScan() {
		context.registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
		context.unregisterReceiver(scanReceiver);
		periodicScanTimer.cancel();
	}

}
