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

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Location {
	private boolean valid;
	private Room nearestRoom;
	private Coordinate fineGrainedLoc;

	private Location() {
	}

	/**
	 * Get the location from a list of wifi readings
	 * 
	 * @param readings
	 *            A list of wifi readings
	 * @return a location instance
	 * @throws UnrecognizedResponseException 
	 * @throws ConnectionException 
	 * @throws ServerException 
	 */
	public static Location getFromReadings(List<WifiReading> readings) throws ServerException, ConnectionException, UnrecognizedResponseException {
		RequestHandler rh = RequestHandler.getInstance();
		Object o = rh.post("/json", readingsToJSON(readings).toString());
		Location location = new Location();
		if (o instanceof JSONObject) {
			try {
				JSONObject res = (JSONObject) o;

				// parse location
				JSONObject loc = res.getJSONObject("location");

				// parse location type
				String type = loc.getString("type");
				if (type.equals("room")) {
					location.nearestRoom = Room.parseRoom(loc.getJSONObject("result"));
					location.valid = true;
				} else if (type.equals("floor")) {
					// TODO: parse floor
				} else {
					location.valid = false;
				}

				// parse coordinates
				if (loc.has("coords")) {
					JSONObject coords = loc.getJSONObject("coords");
					location.fineGrainedLoc = Coordinate.parseCoordinate(coords);
				}

				// TODO: parse aps
			} catch (JSONException e) {
				location.valid = false;
				String info = String.format(
						"Result part of the servers response wasn't of the expected form. Post was \"/json\", with \"request\"=\"location\".");
				throw new UnrecognizedResponseException(info);
			}
		} else {
			location.valid = false;
		}
		return location;
	}

	/** Whether there is an actual location associated with this request */
	public boolean isValid() {
		return this.valid;
	}

	/** Get the nearest room this location is associated with. Null if none */
	public Room getNearestRoom() {
		return this.nearestRoom;
	}

	public Coordinate getLocation() {
		return this.fineGrainedLoc;
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
		return req;
	}
}
