package ch.ethz.inf.vs.android.g54.a4.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;
import ch.ethz.inf.vs.android.g54.a4.types.Floor;

/**
 * Gets maps from the network if needed and manages caching them on the SD card
 */
public class MapCache {
	private static final String TAG = "SG MapCache";

	private static final String FILE_EXTENSION = "png";

	public static Bitmap getMap(Floor floor, Context c) {
		Bitmap map = getMapFromSDCard(floor, c);

		if (map == null) {
			map = getMapFromNetwork(floor);
			if (map != null) {
				storeMapOnSDCard(map, floor, c);
			}
		}

		return map;
	}

	private static Bitmap getMapFromSDCard(final Floor floor, Context c) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can at least read the external storage
			File root = c.getExternalCacheDir();
			File[] matchedFiles = root.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					if (filename.equals(constructFileName(floor))) {
						return true;
					} else {
						return false;
					}
				}
			});
			if (matchedFiles.length > 0) {
				try {
					FileInputStream in = new FileInputStream(matchedFiles[0]);
					Bitmap bitmap = BitmapFactory.decodeStream(in);
					Log.i(TAG, "Successfully read image from SDCard");
					return bitmap;
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Could not read from file.", e);
					return null;
				}
			} else {
				// the map is not yet cached
				return null;
			}
		} else {
			// we cannot read the external storage
			return null;
		}
	}

	private static void storeMapOnSDCard(Bitmap bitmap, Floor floor, Context c) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the external storage
			try {
				File root = c.getExternalCacheDir();
				File imageFile = new File(root, constructFileName(floor));
				FileOutputStream out;
				out = new FileOutputStream(imageFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				Log.i(TAG, "Successfully stored image to SDCard");
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Could save the image on the SDCard.", e);
			}
		}
	}

	private static Bitmap getMapFromNetwork(Floor floor) {
		String url = floor.getMapUrl();
		if (url == null) {
			try {
				floor.load();
				url = floor.getMapUrl();
			} catch (Exception e) {
				Log.e(TAG, "Could not load floor details.", e);
			}
			if (url == null) {
				return null;
			}
		}
		try {
			return RequestHandler.getBitmap(url);
		} catch (Exception e) {
			Log.e(TAG, "Could not load floor details.", e);
			return null;
		}
	}

	private static String constructFileName(Floor floor) {
		String buildingName = floor.getBuilding().getName();
		String floorName = floor.getName();
		return String.format("%s_%s.%s", buildingName, floorName, FILE_EXTENSION);
	}
}
