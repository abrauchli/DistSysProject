package ch.ethz.inf.vs.android.g54.a4;

import java.util.List;

import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;

import android.content.Context;
import android.net.wifi.ScanResult;
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
