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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Floor extends LazyObject {

	// lazily generated fields
	private List<Room> rooms;
	private String mapUrl = null;

	// fields instantiated upon initialization
	private Building building;
	private String name;

	/** Hidden initialize function, use get */
	@Override
	protected void initialize(String ID) {
		super.initialize(ID);
		// ID is of form 'CAB E'
		String[] parts = ID.split(" ");
		this.building = Building.getBuilding(parts[0]);
		this.name = parts[1];
	}

	/** Get a floor by identifier */
	public static Floor getFloor(Building building, String floor) {
		return (Floor) get(constructID(building, floor), Floor.class);
	}

	protected static String constructID(Building building, String floor) {
		return String.format("%s %s", building.getId(), floor);
	}

	public static Floor parseFloor(Building building, String floor, JSONObject desc) throws JSONException {
		Floor f = getFloor(building, floor);
		if (!f.isLoaded()) {
			// parse map URL
			f.mapUrl = desc.optString("map", null);
		}
		return f;
	}

	/**
	 * Loads the floor with all rooms on this floor
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	@Override
	public void load() throws ServerException, ConnectionException, UnrecognizedResponseException {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s/%s", this.building.getName(), this.name));
		try {
			JSONObject f = (JSONObject) o;

			// TODO: parsing identifier tags and throw exception if they don't match input

			// parse map URL
			mapUrl = f.optString("map", null);

			// parse rooms
			JSONObject rms = f.getJSONObject("rooms");
			rooms = new LinkedList<Room>();
			for (Iterator<?> keys = rms.keys(); keys.hasNext();) {
				String key = (String) keys.next();
				Room r = Room.parseRoom(this, key, rms.getJSONObject(key));
				rooms.add(r);
			}

			setLoaded(true);
		} catch (Exception e) {
			String info = String.format(
					"Result part of the servers response wasn't of the expected form. Request was \"/r/%s/%s\".",
					building, name);
			throw new UnrecognizedResponseException(info);
		}
	}

	/**
	 * Gets a list of free rooms on this floor
	 * @return A list of free rooms
	 * @throws ServerException
	 * @throws ConnectionException
	 * @throws UnrecognizedResponseException
	 */
	public List<Room> getFreeRooms() throws ServerException, ConnectionException, UnrecognizedResponseException {
		return getBuilding().getFreeRooms(this, null, null);
	}

	/**
	 * Gets a list of free rooms on this floor in a given time constraint
	 * @param start start time constraint in quarter hours
	 * @param end end time constraint in quarter hours
	 * @return A list of free rooms
	 * @throws ServerException
	 * @throws ConnectionException
	 * @throws UnrecognizedResponseException
	 */
	public List<Room> getFreeRooms(float start, float end) throws ServerException, ConnectionException, UnrecognizedResponseException {
		return getBuilding().getFreeRooms(this, start, end);
	}


	/**
	 * Gets a list of all rooms on this floor Make sure the object is loaded with isLoaded() before calling
	 */
	public List<Room> getRooms() {
		assert (isLoaded());
		return rooms;
	}

	/** Gets the building associated with this floor */
	public Building getBuilding() {
		return this.building;
	}

	/** Gets the floor name (e.g. 'F') */
	public String getName() {
		return this.name;
	}

	/** Gets the URL of the map associated with this floor, null if not available */
	public String getMapUrl() {
		return mapUrl;
	}
}
