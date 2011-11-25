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

import java.util.HashMap;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;

public abstract class LazyObject {
	/** Instance caching */
	private static HashMap<String, LazyObject> instances = new HashMap<String, LazyObject>();

	/** Hidden constructor, use get */
	protected LazyObject() {
		loaded = false;
	}

	/** Hidden initialize function, use get */
	protected void initialize(String ID) {
		this.ID = ID;
	}

	/** Get instance by name */
	public static LazyObject get(String id, Class<? extends LazyObject> type) {
		LazyObject o = instances.get(id);
		if (o != null)
			return o;

		try {
			o = type.newInstance();
			o.initialize(id);
			instances.put(id, o);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

	/** Unique ID of this LazyObject. */
	protected String ID;

	@Override
	public String toString() {
		return ID;
	}

	/** Indicates, whether this LazyObject is fully loaded */
	private boolean loaded;

	/** Indicates, whether this LazyObject is fully loaded */
	protected final boolean isLoaded() {
		return loaded;
	}

	/** Overwrites the loaded flag */
	protected final void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	/** Load the LazyObject */
	protected abstract void load() throws ServerException, ConnectionException, UnrecognizedResponseException;
}
