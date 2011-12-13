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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Location {
	private static String TAG = "SG Location";

	private Room room = null;
	private Floor floor = null;

	/** Information concerning the access points, associated with the originally sent mac addresses. */
	private final Map<String, AccessPoint> aps;

	/** Interpolated location, computed from the sent wifi readings. Is null if no interpolation could be done. */
	private final Coordinate location;

	/** Hidden constructor, use getFromReadings */
	protected Location(Coordinate location, Floor floor, Map<String, AccessPoint> aps) {
		this(location, aps);
		this.floor = floor;
	}

	/** Hidden constructor, use getFromReadings */
	protected Location(Coordinate location, Room room, Map<String, AccessPoint> aps) {
		this(location, aps);
		this.room = room;
	}

	/** Hidden constructor, use getFromReadings */
	protected Location(Coordinate location, Map<String, AccessPoint> aps) {
		this.location = location;
		this.aps = aps;
	}

	/**
	 * Gets the coordinates associated with this location
	 * @return a Coordinate object
	 */
	public Coordinate getLocation() {
		return this.location;
	}

	/**
	 * Gets the floor associated with this location
	 * @return The associated floor or null if none
	 */
	public Floor getFloor() {
		return this.floor;
	}

	/**
	 * Gets the room associated with this location
	 * @return The associated room or null if none
	 */
	public Room getRoom() {
		return this.room;
	}

	public Map<String, AccessPoint> getAps() {
		return aps;
	}

	/**
	 * Get the location from a list of wifi readings.
	 * 
	 * @param readings
	 *            A list of wifi readings
	 * @return an instance of either LocationResult, LocationResultWithRoom, LocationResultWithFloor
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	public static Location getFromReadings(List<WifiReading> readings) throws ServerException,
			ConnectionException, UnrecognizedResponseException {
		RequestHandler rh = RequestHandler.getInstance();
		Object o = rh.post("/json", readingsToJSON(readings).toString());
		if (o instanceof JSONObject) {
			try {
				JSONObject res = (JSONObject) o;

				// parse location
				JSONObject loc = res.getJSONObject("location");

				// parse APs
				Map<String, AccessPoint> aps = new HashMap<String, AccessPoint>();
				JSONObject japs = res.getJSONObject("aps"); // politically wrong, i know..
				@SuppressWarnings("unchecked")
				Iterator<String> k = japs.keys(); 			// ..but we're not in politics here
				while (k.hasNext()) {
					try {
						String bssid = k.next();
						aps.put(bssid, new AccessPoint(bssid, japs.getJSONObject(bssid)));
					} catch (JSONException e) {
						// Error in ap, skip it
					}
				}

				// parse coordinates
				Coordinate location = null;
				if (loc.has("coords")) {
					// FIXME: need to remove that statement...
					Log.d(TAG, loc.get("coords").toString());
					if (loc.get("coords") != null) {
						location = new Coordinate(loc.getJSONObject("coords"));
					}
				}

				// parse location type
				String type = loc.getString("type");
				if (type.equals("room")) {
					Room room = Room.parseRoom(loc.getJSONObject("result"));
					return new Location(location, room, aps);
				} else if (type.equals("floor")) {
					Floor floor = null; // TODO: Floor.parseFloor(loc.getJSONObject("result"));
					return new Location(location, floor, aps);
				} else {
					return new Location(location, aps);
				}

			} catch (JSONException e) {
				String info = String
						.format("Result part of the servers response wasn't of the expected form. Post was \"/json\", with \"request\"=\"location\".");
				throw new UnrecognizedResponseException(info, e);
			}
		} else {
			String info = String
					.format("Result part of the servers response doesn't have the expected type. Post was \"/json\", with \"request\"=\"location\".");
			throw new UnrecognizedResponseException(info);
		}
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
			throw new RuntimeException(e);
		}
		return req;
	}
}
