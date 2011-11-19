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

import java.lang.reflect.Constructor;
import java.util.HashMap;

public abstract class LazyObject {
	/** Instance caching */
	private static HashMap<String, LazyObject> instances = new HashMap<String, LazyObject>();

	/** Hidden constructor, use get */
	protected LazyObject(String ID) {
		this.ID = ID;
	}

	/** Get instance by name */
	public static LazyObject get(String name, Class<? extends LazyObject> type) {
		LazyObject o = instances.get(name);
		if (o != null)
			return o;

		try {
			Constructor<? extends LazyObject> c = type.getConstructor(new Class[] { String.class });
			o = c.newInstance(new Object[] { name });
			instances.put(name, o);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

	protected String ID;

	public String getID() {
		return ID;
	}

	protected abstract boolean isLoaded();

	protected abstract void load();
}
