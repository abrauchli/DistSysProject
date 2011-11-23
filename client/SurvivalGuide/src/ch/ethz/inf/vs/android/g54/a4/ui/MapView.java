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
package ch.ethz.inf.vs.android.g54.a4.ui;

import java.util.List;

import android.content.Context;
import android.view.View;

public class MapView extends View {
	
	/**
	 * A list of pins to be shown on the map.
	 * @see #updatePins
	 */
	public List<Pin> pins;

	public MapView(Context context) {
		super(context);
	}
	
	/**
	 * This should be called after manipulating pins, to reflect those changes in the View.
	 */
	public void updatePins() {
				
	}

}
