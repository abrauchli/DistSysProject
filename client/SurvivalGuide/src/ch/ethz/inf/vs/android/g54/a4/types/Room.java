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

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Room extends LazyObject {

	// lazily generated fields
	private String description;
	private String mapUrl;
	Coordinate roomCenter;

	// fields instantiated upon initialization
	private Floor floor;
	private String name;

	/** Hidden initialize function, use get */
	@Override
	protected void initialize(String ID) {
		super.initialize(ID);
		// ID should always be something like 'CAB G 11.1'
		String[] parts = ID.split(" ");
		Building building = Building.getBuilding(parts[0]);
		floor = Floor.getFloor(building, parts[1]);
		name = parts[2];
	}

	/** Get a room by identifier */
	public static Room getRoom(Floor floor, String room) {
		return (Room) get(constructID(floor, room), Room.class);
	}

	private static String constructID(Floor floor, String room) {
		return String.format("%s %s", floor.getId(), room);
	}

	protected static Room parseRoom(Floor floor, String room, JSONObject desc) throws JSONException {
		Room r = getRoom(floor, room);
		if (!r.isLoaded()) {
			r.description = desc.getString("desc");
		}
		return r;
	}

	protected static Room parseRoom(JSONObject result) throws JSONException {
		Building building = Building.getBuilding(result.getString("building"));
		Floor floor = Floor.getFloor(building, result.getString("floor"));
		Room room = getRoom(floor, result.getString("room"));

		if (!room.isLoaded()) {
			// parse map URL
			room.mapUrl = result.optString("map", null);

			// parse room
			room.description = result.getString("desc");
			if (result.has("coords")) {
				room.roomCenter = new Coordinate(result.getJSONObject("coords"));
			} else {
				room.roomCenter = null;
			}
		}
		return room;
	}

	@Override
	public void load() throws ServerException, ConnectionException, UnrecognizedResponseException {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s/%s/%s", floor.getBuilding().getName(), floor.getName(), this.name));
		try {
			JSONObject r = (JSONObject) o;

			// TODO: parsing identifier tags and throw exception if they don't match input

			// parse map URL
			mapUrl = r.optString("map", null);

			// parse room
			description = r.getString("desc");
			if (r.has("coords")) {
				roomCenter = new Coordinate(r.getJSONObject("coords"));
			} else {
				roomCenter = null;
			}

			setLoaded(true);
		} catch (Exception e) {
			// TODO Don't throw away things, that were there before loading.
			description = null;
			roomCenter = null;
			setLoaded(false);
			String info = String.format(
					"Result part of the servers response wasn't of the expected form. Request was \"/r/%s/%s/%s\".",
					floor.getBuilding(), floor, name);
			throw new UnrecognizedResponseException(info);
		}
	}

	/**
	 * Gets the room description Make sure the object is preloaded with load() before calling
	 */
	public String getDescription() {
		assert (isLoaded());
		return this.description;
	}

	/**
	 * Gets the room center Make sure the object is preloaded with load() before calling
	 */
	public Coordinate getRoomCenter() {
		assert (isLoaded());
		return this.roomCenter;
	}

	/** Gets the floor associated with this room */
	public Floor getFloor() {
		return this.floor;
	}

	/** Gets the room name/number (e.g. 21.5) */
	public String getName() {
		return this.name;
	}

	/** Gets the URL of the map associated with this room, null if not available */
	public String getMapUrl() {
		return mapUrl;
	}

}
