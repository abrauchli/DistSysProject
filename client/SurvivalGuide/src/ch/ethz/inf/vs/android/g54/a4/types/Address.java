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

public class Address {

	private String street;
	private String city;
	private Campus campus;

	public Address(JSONObject addr) throws JSONException {
		this.street = addr.getString("address"); // TODO: change back to street, once server is fixed
		this.city = addr.getString("city");
		String c = addr.getString("location"); // TODO: maybe change protocol to reflect different name
		if (c.equals(Campus.HOENGG.name)) {
			this.campus = Campus.HOENGG;
		} else if (c.equals(Campus.ZENTRUM.name)) {
			this.campus = Campus.ZENTRUM;
		} else {
			this.campus = Campus.OTHER;
		}
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public Campus getCampus() {
		return campus;
	}
	
	public enum Campus {
		HOENGG("Höngg"),
		ZENTRUM("Zentrum"),
		OTHER("Other");
		
	    public final String name;
	    Campus(String name) {
	        this.name = name;
	    }
	}
}
