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

import org.json.JSONException;
import org.json.JSONObject;

public class AccessPoint {
	private String bssid;
	private Coordinate coord;
	private String building = null;
	private String floor = null;
	private String room = null;

	public AccessPoint(String bssid, JSONObject jo) throws JSONException {
		this.bssid = bssid;
		this.coord = new Coordinate(jo.getJSONObject("coords"));
		JSONObject l = jo.getJSONObject("location");
		if (l.has("building"))
			this.building = l.getString("building");
		if (l.has("floor"))
			this.floor = l.getString("floor");
		if (l.has("room"))
			this.room = l.getString("room");
	}

	/** Get this APs bssid (mac address) */
	public String getBssid() {
		return this.bssid;
	}

	/** Gets this APs coordinates */
	public Coordinate getCoordinate() {
		return this.coord;
	}
}
