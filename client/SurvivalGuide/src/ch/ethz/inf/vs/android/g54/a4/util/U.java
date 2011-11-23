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

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class U {
	private static Context context;

	public static void initContext(Context context) {
		U.context = context;
	}

	/*
	 * Methods for displaying errors
	 */

	private static String formatException(Exception e) {
		return e.getClass().getSimpleName() + ": " + e.getMessage();
	}

	private static void logAndShowException(String tag, Exception e, Handler postHandler) {
		String exDesc = formatException(e);
		Log.e(tag, exDesc); // TODO superfluous?
		Log.e(tag, Log.getStackTraceString(e));
		if (postHandler == null)
			showToast(exDesc, Toast.LENGTH_LONG);
		else
			postToast(postHandler, exDesc, Toast.LENGTH_LONG);
	}

	public static void showException(String tag, Exception e) {
		logAndShowException(tag, e, null);
	}

	public static void postException(Handler handler, String tag, Exception e) {
		logAndShowException(tag, e, handler);
	}

	/*
	 * Logic for showing general information
	 */

	public static void showToast(String text) {
		showToast(text, Toast.LENGTH_SHORT);
	}

	public static void showToast(String text, int duration) {
		if (context == null)
			throw new RuntimeException(String.format("%s.context is not set. Call initContext first.",
					U.class.getName()));
		Toast.makeText(context, text, duration).show();
	}

	public static void postToast(Handler handler, String text) {
		postToast(handler, text, Toast.LENGTH_SHORT);
	}

	public static void postToast(Handler handler, final String text, final int duration) {
		handler.post(new Runnable() {
			public void run() {
				showToast(text, duration);
			}
		});
	}

}
