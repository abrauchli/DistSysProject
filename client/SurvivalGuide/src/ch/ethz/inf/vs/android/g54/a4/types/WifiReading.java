package ch.ethz.inf.vs.android.g54.a4.types;

import android.net.wifi.ScanResult;

public class WifiReading {

	final public String mac;
	final public String ssid;
	final public int signal;
	
	public WifiReading(String mac, String ssid, int signal) {
		this.mac = mac;
		this.ssid = ssid;
		this.signal = signal;
	}

	public WifiReading(ScanResult result) {
		this.mac = result.BSSID;
		this.ssid = result.SSID;
		this.signal = result.level;
	}
	
}
