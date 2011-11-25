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

public class LocationResult {

	/** Information concerning the access points, associated with the originally sent mac addresses. */
	public final List<AccessPoint> aps;

	/** Interpolated location, computed from the sent wifi readings. Is null if no interpolation could be done. */
	public final Coordinate location;

	/** Hidden constructor, use getFromReadings */
	protected LocationResult(Coordinate location, List<AccessPoint> aps) {
		this.location = location;
		this.aps = aps;
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
	public static LocationResult getFromReadings(List<WifiReading> readings) throws ServerException,
			ConnectionException, UnrecognizedResponseException {
		RequestHandler rh = RequestHandler.getInstance();
		Object o = rh.post("/json", readingsToJSON(readings).toString());
		if (o instanceof JSONObject) {
			try {
				JSONObject res = (JSONObject) o;

				// parse location
				JSONObject loc = res.getJSONObject("location");

				// TODO: parse aps
				List<AccessPoint> aps = null;

				// parse coordinates
				Coordinate location = null;
				if (loc.has("coords")) {
					JSONObject coords = loc.getJSONObject("coords");
					location = Coordinate.parseCoordinate(coords);
				}

				// parse location type
				String type = loc.getString("type");
				if (type.equals("room")) {
					Room room = Room.parseRoom(loc.getJSONObject("result"));
					return new LocationResultWithRoom(location, room, aps);
				} else if (type.equals("floor")) {
					return new LocationResult(location, aps);
				} else {
					return new LocationResult(location, aps);
				}

			} catch (JSONException e) {
				String info = String
						.format("Result part of the servers response wasn't of the expected form. Post was \"/json\", with \"request\"=\"location\".");
				throw new UnrecognizedResponseException(info);
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
			e.printStackTrace();
		}
		return req;
	}
}
