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

/**
 * Class now directly representing pixel data, but later could be adapted to gps data
 */
public class Coordinate {
	private float x, y;

	protected static Coordinate parseCoordinate(JSONObject coord) throws JSONException {
		// TODO: find solution to send floating point gps locations
		int x = coord.getInt("x");
		int y = coord.getInt("y");
		return new Coordinate(x, y);
	}

	public Coordinate(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

}
