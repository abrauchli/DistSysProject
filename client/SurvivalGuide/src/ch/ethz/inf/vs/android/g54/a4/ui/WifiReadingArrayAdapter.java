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

import ch.ethz.inf.vs.android.g54.a4.R;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WifiReadingArrayAdapter extends ArrayAdapter<WifiReading> {
	
	private final Context context;
	private final int textViewResourceId;
	private final List<WifiReading> readings;

	public WifiReadingArrayAdapter(Context context, int textViewResourceId, List<WifiReading> readings) {
		super(context, textViewResourceId, readings);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
		this.readings = readings;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater li = LayoutInflater.from(context);
			v = li.inflate(textViewResourceId, null);
		}
		WifiReading reading = readings.get(position);
		((TextView) v.findViewById(R.id.txt_bssid)).setText(reading.mac);
		((TextView) v.findViewById(R.id.txt_level)).setText(Integer.toString(reading.signal));
		((TextView) v.findViewById(R.id.txt_ssid)).setText(reading.ssid);
		return v;
	}

}
