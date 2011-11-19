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
