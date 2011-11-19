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

import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;

public class Building extends LazyObject {

	protected Building(String ID) {
		super(ID);
	}

	/** Get a list of all buildings */
	public static List<Building> getBuildings() {
		RequestHandler req = RequestHandler.getInstance();
		req.request("/r");
		return null; // TODO
	}

	/** Get a building by identifier */
	public static Building getBuilding(String name) {
		return (Building) get(name, Building.class);
	}

//	public List<Floor> getFloors() {
//		// TODO
//	}
}
