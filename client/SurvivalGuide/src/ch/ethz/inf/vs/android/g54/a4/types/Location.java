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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Location {
	private boolean valid = false;
	private Building building;

	private Location(Building b) {
		this.building = b;
	}

	/**
	 * Get the location from a list of wifi readings
	 * 
	 * @param readings
	 *            A list of wifi readings
	 * @return a location instance
	 */
	public static Location getFromReadings(List<WifiReading> readings) {
		RequestHandler rh = RequestHandler.getInstance();
		Object o = rh.post("/json", readingsToJSON(readings).toString());
		if (o instanceof JSONObject) {
			try {
				JSONObject res = (JSONObject) o;
				res.getJSONObject("location");
				// TODO: parse location
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// TODO: error handling?
		}
		return new Location(Building.getBuilding("TODO: removeMe"));
	}

	/** Whether there is an actual location associated with this request */
	public boolean isValid() {
		return this.valid;
	}

	/** Get the building this location is associated with. Null if none */
	public Building getBuilding() {
		return this.building;
	}

	private static JSONObject readingsToJSON(List<WifiReading> readings) {
		JSONObject aps = new JSONObject();
		for (WifiReading reading : readings) {
			try {
				aps.put(reading.mac, reading.signal);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		JSONObject req = new JSONObject();
		try {
			req.put("request", "location");
			req.put("aps", aps);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aps;
	}
}
