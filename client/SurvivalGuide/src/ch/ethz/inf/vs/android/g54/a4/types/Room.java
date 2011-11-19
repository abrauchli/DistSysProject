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

import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Room extends LazyObject {

	// lazily generated fields
	String description;
	// TODO: map url and map
	Coordinate roomCenter;

	// fields instantiated upon initialization
	String building;
	String floor;
	String name;

	protected Room(String ID) {
		super(ID);
		// ID should always be something like 'CAB G 11.1'
		String[] parts = ID.split(" ");
		building = parts[0];
		floor = parts[1];
		name = parts[2];
	}

	/** Get a room by identifier */
	public static Room getRoom(String building, String floor, String room) {
		return (Room) get(constructID(building, floor, room), Room.class);
	}

	public static String constructID(String building, String floor, String room) {
		return String.format("%s %s", Floor.constructID(building, floor), room);
	}

	@Override
	protected boolean isLoaded() {
		return (description != null) && (roomCenter != null);
	}

	@Override
	protected void load() {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s/%s/%s", building, floor, name));
		if (o instanceof JSONObject) {
			try {
				JSONObject r = (JSONObject) o;

				// TODO: decide if parsing identifier tags is a good idea

				// parse room
				description = r.getString("desc");
				// TODO: get map url and map
				JSONObject loc = r.getJSONObject("location");
				// TODO: find solution to send floating point gps locations
				int x = loc.getInt("x");
				int y = loc.getInt("y");
				roomCenter = new Coordinate(x, y);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				description = null;
				roomCenter = null;
			}
		} else {
			// TODO: error handling?
		}
	}

	public String getDescription() {
		if (!isLoaded()) {
			load();
		}
		return description;
	}

	public Coordinate getRoomCenter() {
		if (!isLoaded()) {
			load();
		}
		return roomCenter;
	}

	public Building getBuilding() {
		return Building.getBuilding(building);
	}

	public Floor getFloor() {
		return Floor.getFloor(building, floor);
	}

	public String getName() {
		return name;
	}

}
