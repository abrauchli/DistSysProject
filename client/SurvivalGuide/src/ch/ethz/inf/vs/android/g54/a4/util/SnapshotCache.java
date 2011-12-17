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
package ch.ethz.inf.vs.android.g54.a4.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;

public class SnapshotCache {
	private static final String TAG = "SG SnapshotCache";

	private static final String FILE_EXTENSION = "json";

	public static void storeSnapshot(List<WifiReading> readings, String fileName, Context c) {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the external storage
			try {
				JSONArray json = readingsToJson(readings);
				File root = c.getExternalCacheDir();
				File jsonFile = new File(root, constructFileName(fileName));
				FileOutputStream out;
				out = new FileOutputStream(jsonFile);
				out.write(json.toString().getBytes());
				out.close();
				U.showToast(constructFileName(fileName));
				Log.d(TAG, "Successfully stored snapshot to SDCard");
			} catch (Exception e) {
				U.showToast("Could not save the snapshot on the SDCard.");
				Log.e(TAG, "Could not save the snapshot on the SDCard.", e);
			}
		} else {
			U.showToast("Cannot write to external storage.");
		}
	}

	public static List<WifiReading> getRandomSnapshot(Context c) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can at least read the external storage
			File root = c.getExternalCacheDir();
			File[] snapshotFiles = root.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					if (filename.endsWith(FILE_EXTENSION)) {
						return true;
					} else {
						return false;
					}
				}
			});
			if (snapshotFiles.length > 0) {
				Random rand = new Random();
				int index = rand.nextInt(snapshotFiles.length);
				try {
					// read file into a string
					FileInputStream fstream = new FileInputStream(snapshotFiles[index]);
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String fileContents = "";
					String strLine;
					while ((strLine = br.readLine()) != null) {
						fileContents += strLine;
					}
					
					// make a json array out of the string
					JSONArray json = new JSONArray(fileContents);
					
					// parse the json array
					return jsonToReadings(json);
				} catch (Exception e) {
					Log.e(TAG, "Could not read file.");
					return null;
				}
			} else {
				// there are no cached snapshots
				return null;
			}
		} else {
			// we cannot read the external storage
			return null;
		}
	}

	private static JSONArray readingsToJson(List<WifiReading> readings) throws JSONException {
		JSONArray rs = new JSONArray();
		for (WifiReading reading : readings) {
			JSONObject ap = new JSONObject();
			ap.put("mac", reading.mac);
			ap.put("ssid", reading.ssid);
			ap.put("signal", reading.signal);
			rs.put(ap);
		}
		return rs;
	}

	private static List<WifiReading> jsonToReadings(JSONArray json) {
		List<WifiReading> readings = new ArrayList<WifiReading>(json.length());
		for (int i = 0; i < json.length(); i++) {
			try {
				JSONObject ap = json.getJSONObject(i);
				String mac = ap.getString("mac");
				String ssid = ap.getString("ssid");
				int signal = ap.getInt("signal");
				readings.add(new WifiReading(mac, ssid, signal));
			} catch (JSONException e) {
				return null;
			}
		}
		return readings;
	}

	private static String constructFileName(String name) {
		Date d = new Date();
		return String.format("%d-%d-%d_%d-%d-%d_%s.%s", d.getYear() + 1900, d.getMonth(), d.getDay(), d.getHours(),
				d.getMinutes(), d.getSeconds(), name, FILE_EXTENSION);
	}
}
