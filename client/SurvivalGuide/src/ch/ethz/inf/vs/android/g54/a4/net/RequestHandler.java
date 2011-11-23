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
package ch.ethz.inf.vs.android.g54.a4.net;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Singleton class in charge of http connections to the server
 * Also does basic JSON parsing and displays toast on errors
 */
public class RequestHandler {
	private static final String HOST = "http://deserver.moeeeep.com";
	private static final int PORT = 32123;

	private static final String TAG = "SG_NetLib";

	private Context context = null;
	private static RequestHandler instance = null;

	/** Singleton ctor */
	private RequestHandler() { }

	/** Get the RequestHandler singleton */
	public static RequestHandler getInstance() {
		if (RequestHandler.instance == null) {
			RequestHandler.instance = new RequestHandler();
		}
		return RequestHandler.instance;
	}

	/** Initialize the instance's context for toasty error messages */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Execute an HTTP get on a given resource on the configured server
	 * @param res The resource URL without host
	 * @return a JSONObject / JSONArray
	 */
	public Object request(String res) {
		HttpClient client = new DefaultHttpClient();
		String responseBody = null;
		try {
			HttpGet htget = new HttpGet(HOST + ":" + PORT + res);
			BasicHeader header = new BasicHeader("Accept", "application/json");
			htget.addHeader(header);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(htget, responseHandler);

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Request failed", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
			return null;

		} finally {
			client.getConnectionManager().shutdown();
		}
		return parseResponse(responseBody);
	}

	/**
	 * Execute an HTTP post on a given resource on the configured server
	 * @param res The resource URL without host
	 * @param data The data to post
	 * @return a JSONObject / JSONArray
	 */
	public Object post(String res, String data) {
		HttpClient client = new DefaultHttpClient();
		String responseBody = null;

		try {
			HttpPost post = new HttpPost(HOST + ":" + PORT + res);
			post.addHeader("Content-Type", "application/json");
			StringEntity se = new StringEntity(data);
			post.setEntity(se);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(post, responseHandler);

		} catch (Exception e) {
			Toast.makeText(context, "POST request failed", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return parseResponse(responseBody);
	}

	/** Do the actual JSON parsing and ensure a correct server response */
	private Object parseResponse(String response) {
		try {
			JSONObject jso = new JSONObject(response);
			if (!jso.getBoolean("ok")) {
				String msg = "Request failed";
				if (jso.has("message")) {
					msg += jso.getString("message");
				}
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
				Log.e(TAG, msg);
			}

			assert (jso.has("result"));
			return jso.get("result");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
