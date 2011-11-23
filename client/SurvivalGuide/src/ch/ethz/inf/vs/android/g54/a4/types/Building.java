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

/**
 * Lazily loaded class representing buildings.
 */
public class Building extends LazyObject {

	// Lazily generated fields
	private Address address;
	private List<Floor> floors;

	// Fields instantiated upon initialization
	private String name;

	/** Hidden initialize function, use get */
	@Override
	protected void initialize(String ID) {
		super.initialize(ID);
		// ID should always be something like 'CAB'
		name = ID;
	}

	/** Get a list of all buildings */
	public static List<Building> getBuildings() {
		RequestHandler req = RequestHandler.getInstance();
		req.request("/r");
		return null;// TODO: list of all buildings
	}

	/** Get a building by identifier */
	public static Building getBuilding(String name) {
		return (Building) get(name, Building.class);
	}

	@Override
	protected void load() {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s", name));
		if (o instanceof JSONObject) {
			try {
				JSONObject b = (JSONObject) o;

				// TODO: decide if this would make sense or not
				// parse name of the building (though already set by constructor)
				// name = b.getString("name");

				// parse address
				JSONObject addr = b.getJSONObject("address");
				address = Address.parseAddress(addr);

				// parse floors
				JSONObject flrs = b.getJSONObject("floors");
				floors = new LinkedList<Floor>();
				for (Iterator<?> keys = flrs.keys(); keys.hasNext();) {
					String key = (String) keys.next();
					Floor f = Floor.parseFloor(name, key, flrs.getJSONObject(key));
					floors.add(f);
				}
				setLoaded(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				address = null;
				floors = null;
				setLoaded(false);
			}
		} else {
			// TODO: error handling?
		}
	}

	/** Get the list of floors, located in this building. */
	public List<Floor> getFloors() {
		if (!isLoaded()) {
			load();
		}
		return floors;
	}

	/** Get the address of this building. */
	public Address getAddress() {
		if (!isLoaded()) {
			load();
		}
		return address;
	}

	/** Get the name of this building. */
	public String getName() {
		return name;
	}
}
