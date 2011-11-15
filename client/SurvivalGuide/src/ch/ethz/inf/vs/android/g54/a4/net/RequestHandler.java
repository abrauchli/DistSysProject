package ch.ethz.inf.vs.android.g54.a4.net;

import java.io.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.json.*;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RequestHandler {
	private static final String PROTO_PREFIX = "http://";
	private static final String HOST = "deserver.moeeeep.com";
	private static final int PORT = 23032;

	private static final String ROOMS = "/r/";
	private static final String MACS = "/m/";
	private static final String LOCATION = "/getNearestLocation/";

	private static final String TAG = "SG_NetLib";

	private Context context;

	public RequestHandler(Context context) {
		this.context = context;
	}

	private JSONObject getRequest(String resourceLoc) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(PROTO_PREFIX + HOST + ":" + PORT + resourceLoc);
		get.addHeader("Accept", "application/json");
		JSONObject jsonResponse = null;
		HttpResponse response;
		try {
			response = client.execute(get);
			jsonResponse = parseResponse(response);
		} catch (ClientProtocolException e) {
			Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}

	private JSONObject postRequest(String resourceLoc, JSONObject jsonRequest) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(PROTO_PREFIX + HOST + ":" + PORT + resourceLoc);
		post.addHeader("Content-Type", "application/json");
		JSONObject jsonResponse = null;
		try {
			StringEntity se = new StringEntity(jsonRequest.toString());
			post.setEntity(se);
			HttpResponse response = client.execute(post);
			jsonResponse = parseResponse(response);
		} catch (ClientProtocolException e) {
			Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}

	private JSONObject parseResponse(HttpResponse response) {
		JSONObject jsonResponse = null;
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
				String inputline, text = "";
				while ((inputline = reader.readLine()) != null) {
					text += inputline + '\n';
				}
				try {
					jsonResponse = new JSONObject(text);
				} catch (JSONException e) {
					Toast.makeText(context, "JSONException", Toast.LENGTH_LONG).show();
					Log.e(TAG, e.toString());
				}
			}
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}

	public JSONObject getAllBuildings() {
		return getRequest(ROOMS);
	}

	public JSONObject getBuilding(String building) {
		String resourceLocation = String.format("%s%s", ROOMS, building);
		return getRequest(resourceLocation);
	}

	public JSONObject getFloor(String building, String floor) {
		String resourceLocation = String.format("%s%s/%s", ROOMS, building, floor);
		return getRequest(resourceLocation);
	}

	public JSONObject getRoom(String building, String floor, String roomNumber) {
		String resourceLocation = String.format("%s%s/%s/%s", ROOMS, building, floor, roomNumber);
		return getRequest(resourceLocation);
	}

	public JSONObject getLocation(JSONObject wifiReadings) {
		return postRequest(LOCATION, wifiReadings);
	}
}
