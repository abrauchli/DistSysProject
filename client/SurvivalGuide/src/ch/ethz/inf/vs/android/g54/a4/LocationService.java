package ch.ethz.inf.vs.android.g54.a4;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.util.SnapshotCache;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class LocationService extends Service {

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
			Location locRes = Location.getFromReadings(visibleNetworks);
			ui.setLocation(locRes);
			SnapshotCache.storeSnapshot(readings, "foo", ui);
		}
	}

	WifiManager wifi;
	WifiScanReceiver scanReceiver;

	@Override
	/** Return the communication channel to the service. */
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/** Called when the service is started or when the delay is updated */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// onResume
		registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		/* Updates the delay. */
		if (intent.hasExtra("is_running") && intent.getExtras().getBoolean("is_running")) {
			return START_NOT_STICKY;
		}
		return START_NOT_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// onPause
		unregisterReceiver(scanReceiver);
		return true;
	}

	/** Called when the service is created for the first time. */
	@Override
	public void onCreate() {
		wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		wifi.startScan(); // TODO: execute every n secs.
	}

}
