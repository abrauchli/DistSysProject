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

import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Floor extends LazyObject {

	// lazily generated fields
	private List<Room> rooms;
	private String mapUrl;
	private boolean mapAvailable;
	// TODO: map

	// fields instantiated upon initialization
	private String building;
	private String name;

	/** Hidden initialize function, use get */
	@Override
	protected void initialize(String ID) {
		super.initialize(ID);
		// ID should always be something like 'CAB E'
		String[] parts = ID.split(" ");
		building = parts[0];
		name = parts[1];
	}

	/** Get a floor by identifier */
	public static Floor getFloor(String building, String floor) {
		return (Floor) get(constructID(building, floor), Floor.class);
	}

	protected static String constructID(String building, String floor) {
		return String.format("%s %s", building, floor);
	}

	protected static Floor parseFloor(String building, String floor, JSONObject desc) throws JSONException {
		Floor f = getFloor(building, floor);
		if (!f.isLoaded()) {
			f.mapAvailable = desc.getBoolean("mapAvailable");
			if (f.mapAvailable) {
				f.mapUrl = desc.getString("map");
			}
		}
		return f;
	}

	@Override
	protected void load() {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s/%s", building, name));
		if (o instanceof JSONObject) {
			try {
				JSONObject f = (JSONObject) o;

				// TODO: parse building and initialize it if necessary
				// TODO: parse map

				// parse rooms
				JSONObject rms = f.getJSONObject("rooms");
				rooms = new LinkedList<Room>();
				for (Iterator<?> keys = rms.keys(); keys.hasNext();) {
					String key = (String) keys.next();
					Room r = Room.parseRoom(building, name, key, rms.getJSONObject(key));
					rooms.add(r);
				}
				setLoaded(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				rooms = null;
				setLoaded(false);
			}
		} else {
			// TODO: error handling?
		}
	}

	public List<Room> getRooms() {
		if (!isLoaded()) {
			load();
		}
		return rooms;
	}

	public Building getBuilding() {
		return Building.getBuilding(building);
	}

	public String getName() {
		return name;
	}

}
