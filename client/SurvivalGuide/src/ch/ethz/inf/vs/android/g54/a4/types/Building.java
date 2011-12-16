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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;
import ch.ethz.inf.vs.android.g54.a4.types.LazyObject.MessageStatus;

/**
 * Lazily loaded class representing buildings.
 */
public class Building extends LazyObject {
	/** Caching of list of all buildings. */
	private static List<Building> allBuildings = null;
	private static List<List<Building>> buildingGroups = null;

	// Lazily generated fields
	private Address address = null;
	private List<Floor> floors;

	// Fields instantiated upon initialization
	private String name;

	/** Hidden initialize function, use get */
	@Override
	protected void initialize(String ID) {
		super.initialize(ID);
		// ID is of form 'CAB'
		name = ID;
	}

	/**
	 * Get a list of all buildings
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	public static List<Building> getAllBuildings() throws ServerException, ConnectionException,
			UnrecognizedResponseException {
		if (allBuildings == null) {
			RequestHandler req = RequestHandler.getInstance();
			Object o = req.request("/r");
			try {
				JSONObject buildingsList = (JSONObject) o;
				allBuildings = new LinkedList<Building>();
				for (Iterator<?> keys = buildingsList.keys(); keys.hasNext();) {
					String key = (String) keys.next();
					Building b = Building.parseBuilding(buildingsList.getJSONObject(key));
					allBuildings.add(b);
				}
			} catch (Exception e) {
				allBuildings = null;
				String info = String
						.format("Result part of the servers response wasn't of the expected form. Request was \"/r\".");
				throw new UnrecognizedResponseException(info);
			}
		}
		return allBuildings;
	}

	/**
	 * Get a list of buildings, filtered by campus
	 * 
	 * @param campus
	 *            Specify the campus where the returned buildings need to be. If null, all the buildings will be
	 *            returned.
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ConnectionException
	 * @throws ServerException
	 */
	public static List<Building> getBuildings(Address.Campus campus) throws ServerException, ConnectionException,
			UnrecognizedResponseException {
		// TODO: rewrite as soon as filtered lists directly available from server
		if (buildingGroups == null) {
			// initialization of the list (individual lists are set to null)
			buildingGroups = new ArrayList<List<Building>>(Address.Campus.values().length);
			for (int i = 0; i < Address.Campus.values().length; i++) {
				buildingGroups.add(null);
			}
		}
		if (campus != null) {
			List<Building> filteredBuildings = buildingGroups.get(campus.ordinal);
			if (filteredBuildings == null) {
				if (allBuildings == null) {
					// get the list of all buildings
					getAllBuildings();
				}
				// filter the list by campus
				filteredBuildings = new LinkedList<Building>();
				for (Building building : allBuildings) {
					if (building.address.getCampus().equals(campus)) {
						filteredBuildings.add(building);
					}
				}
				// cache the list for later use
				buildingGroups.set(campus.ordinal, filteredBuildings);
			}
			return filteredBuildings;
		} else {
			return getAllBuildings();
		}
	}

	public static Building parseBuilding(JSONObject desc) throws JSONException {
		String bName = desc.getString("name");
		Building b = getBuilding(bName);
		if (!b.isLoaded()) {
			JSONObject addr = desc.getJSONObject("address");
			b.address = new Address(addr);
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
	public void load() throws ServerException, ConnectionException, UnrecognizedResponseException {
		RequestHandler req = RequestHandler.getInstance();
		Object o = req.request(String.format("/r/%s", name));
		if (o instanceof JSONObject) {
			try {
				JSONObject b = (JSONObject) o;

				// TODO: parsing identifier tags and throw exception if they don't match input

				// parse address
				JSONObject addr = b.getJSONObject("address");
				address = new Address(addr);

				// parse floors
				JSONObject flrs = b.getJSONObject("floors");
				floors = new LinkedList<Floor>();
				for (Iterator<?> keys = flrs.keys(); keys.hasNext();) {
					String key = (String) keys.next();
					Floor f = Floor.parseFloor(this, key, flrs.getJSONObject(key));
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
	 * Synchronously get free rooms
	 */
	private List<Room> getFreeRoom(Floor f, Float start, Float end) throws ServerException, ConnectionException, UnrecognizedResponseException {
		RequestHandler req = RequestHandler.getInstance();
		JSONObject data = new JSONObject();
		try {
			data.put("request", "freeroom");
			data.put("building", getName());
			if (f != null)
				data.put("floor", f.getName());
			if (start != null)
				data.put("starttime", start);
			if (end != null)
				data.put("endtime", start);
		} catch (JSONException e1) {}
		Object o = req.post("/json", data.toString());
		try {
			JSONArray a = (JSONArray) o;

			LinkedList<Room> rooms = new LinkedList<Room>();
			for(int i = 0; i < a.length(); ++i) {
				JSONObject ro = a.getJSONObject(i);
				rooms.add(Room.parseRoom(ro));
			}
			return rooms;

		} catch (Exception e) {
			String info = String.format("Invalid freeroom request");
			throw new UnrecognizedResponseException(info, e);
		}
	}

	/**
	 * Asynchronously get free rooms based on floor and time constraints
	 * A message will be dispatched to the handler informing of the status
	 * In case of failure, the exception string is passed through the message key of the bundle
	 * @param f Floor constraint
	 * @param start time constraint in quarter hours
	 * @param end time constraint in quarter hours
	 * @param handler Handler that will get the success/failure message with this object
	 */
	public void getFreeRoomsAsync(final Floor f, final Float start, final Float end, final Handler handler) {
		new Thread(new Runnable() {
			public void run() {
				Message m = handler.obtainMessage();
				try {
					m.obj = getFreeRoom(f, start, end);
					m.what = MessageStatus.SUCCESS.ordinal();
				} catch (Exception e) {
					m.what = MessageStatus.FAILURE.ordinal();
					Bundle b = new Bundle();
					b.putString("message", e.getMessage());
					m.setData(b);
				} finally {
					handler.sendMessage(m);
				}
			}
		}).run();
	}

	/**
	 * Gets a list of free rooms in this building
	 * A message will be dispatched to the handler informing of the status
	 * In case of failure, the exception string is passed through the message key of the bundle
	 * @param handler Handler that will get the success/failure message with this object
	 */
	public void getFreeRooms(Handler h) {
		getFreeRoomsAsync(null, null, null, h);
	}

	/**
	 * Gets a list of free rooms in this building in a given time constraint
	 * A message will be dispatched to the handler informing of the status
	 * In case of failure, the exception string is passed through the message key of the bundle
	 * @param start start time constraint in quarter hours
	 * @param end end time constraint in quarter hours
	 * @param handler Handler that will get the success/failure message with this object
	 */
	public void getFreeRooms(float start, float end, Handler h) {
		getFreeRoomsAsync(null, start, end, h);
	}

	/**
	 * Get the list of floors, located in this building.
	 * 
	 * Make sure the object is loaded by checking isLoaded() before calling this
	 */
	public List<Floor> getFloors() {
		assert (isLoaded());
		return this.floors;
	}

	/**
	 * Get the address of this building.
	 * 
	 * The address may (but is not guaranteed) be set even if the object is not loaded
	 */
	public Address getAddress() {
		return this.address;
	}

	/** Get the name of this building. */
	public String getName() {
		return this.name;
	}

	@SuppressWarnings("serial")
	public static Map<String, Point> buildingLocationsCenter = new HashMap<String, Point>() {{
		// some buildings to get started
		put("HG", new Point(476, 1015));
		put("CAB", new Point(627, 694));
		put("CHN", new Point(586, 586));
		put("IFW", new Point(184, 449));
		put("RZ", new Point(229, 432));
		put("NO", new Point(449, 713));
		put("ML", new Point(480, 833));
		put("CLA", new Point(410, 805));
		put("ETZ", new Point(994, 813));
		put("ETF", new Point(960, 845));
		put("ETA", new Point(1027, 774));
		put("CNB", new Point(690, 725));
		put("LFW", new Point(631, 837));
	}};

	@SuppressWarnings("serial")
	public static Map<String, Point> buildingLocationsHoengg = new HashMap<String, Point>() {{
		put("HPH", new Point(892, 878));
		put("HIL", new Point(418, 770));
		put("HIT", new Point(470, 390));
		put("HCI", new Point(679, 1005));
		put("HIF", new Point(270, 645));
		put("HIR", new Point(427, 573));
		put("HIQ", new Point(486, 539));
		put("HIP", new Point(536, 537));
		put("HPI", new Point(658, 797));
		put("HPT", new Point(651, 653));
		put("HPF", new Point(702, 456));
		put("HPM", new Point(860, 402));
		put("HPK", new Point(977, 441));
		put("HPZ", new Point(962, 673));
		put("HPR", new Point(933, 769));
		put("HPV", new Point(1042, 870));
		put("HPP", new Point(1116, 728));
		put("HPS", new Point(1268, 894));
		put("HDB", new Point(472, 240));
		put("HKK", new Point(642, 228));
		put("HPW", new Point(1067, 554));
		put("HXA", new Point(489, 1218));
		put("HXB", new Point(526, 1366));
		put("HXC", new Point(339, 1297));
		put("HXD", new Point(371, 1188));
		put("HXE", new Point(491, 1127));
		put("HIG", new Point(233, 891));
		put("HPG", new Point(817, 652));
		put("HEZ", new Point(901, 83));
	}};
}
