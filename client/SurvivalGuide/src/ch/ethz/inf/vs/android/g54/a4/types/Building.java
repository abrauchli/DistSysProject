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

/**
 * Lazily loaded class representing buildings.
 */
public class Building extends LazyObject {
	/** Caching of list of all buildings. */
	private static List<Building> allBuildings = null;

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

	/**
	 * Get a list of all buildings
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	public static List<Building> getBuildings() throws ServerException, ConnectionException,
			UnrecognizedResponseException {
		if (allBuildings == null) {
			RequestHandler req = RequestHandler.getInstance();
			Object o = req.request("/r");
			if (o instanceof JSONObject) {
				try {
					JSONObject buildingsList = (JSONObject) o;
					allBuildings = new LinkedList<Building>();
					for (Iterator<?> keys = buildingsList.keys(); keys.hasNext();) {
						String key = (String) keys.next();
						Building b = Building.parseBuilding(buildingsList.getJSONObject(key));
						allBuildings.add(b);
					}
				} catch (JSONException e) {
					allBuildings = null;
					String info = String
							.format("Result part of the servers response wasn't of the expected form. Request was \"/r\".");
					throw new UnrecognizedResponseException(info);
				}
			} else {
				String info = String
						.format("Result part of the servers response doesn't have the expected type. Request was \"/r\".");
				throw new UnrecognizedResponseException(info);
			}

		}
		return allBuildings;
	}

	private static Building parseBuilding(JSONObject desc) throws JSONException {
		String bName = desc.getString("name");
		Building b = getBuilding(bName);
		if (!b.isLoaded()) {
			JSONObject addr = desc.getJSONObject("address");
			Address a = Address.parseAddress(addr);
			b.address = a;
		}
		return b;
	}

	/** Get a building by identifier */
	public static Building getBuilding(String name) {
		return (Building) get(name, Building.class);
	}

	/**
	 * Loads the building.
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	@Override
	protected void load() throws ServerException, ConnectionException, UnrecognizedResponseException {
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
				// TODO Don't throw away things, that were there before loading.
				address = null;
				floors = null;
				setLoaded(false);
				String info = String
						.format("Result part of the servers response wasn't of the expected form. Request was \"/r/%s\".",
								name);
				throw new UnrecognizedResponseException(info, e);
			}
		} else {
			String info = String.format(
					"Result part of the servers response doesn't have the expected type. Request was \"/r/%s\".", name);
			throw new UnrecognizedResponseException(info);
		}
	}

	/**
	 * Get the list of floors, located in this building.
	 * Make sure the object is loaded by checking isLoaded() before calling this
	 */
	public List<Floor> getFloors() {
		assert (isLoaded());
		return this.floors;
	}

	/**
	 * Get the address of this building.
	 * The address may (but is not guaranteed) be set even if the object is not loaded 
	 */
	public Address getAddress() {
		return this.address;
	}

	/** Get the name of this building. */
	public String getName() {
		return this.name;
	}
}
