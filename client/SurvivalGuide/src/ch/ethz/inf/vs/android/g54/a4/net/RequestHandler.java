package ch.ethz.inf.vs.android.g54.a4.net;

import java.io.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.json.*;

import android.content.Context;
import android.test.IsolatedContext;
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

	private Object getRequest(String resourceLoc, Class expectedResult) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(PROTO_PREFIX + HOST + ":" + PORT + resourceLoc);
		get.addHeader("Accept", "application/json");
		Object jsonResponse = null;
		HttpResponse response;
		try {
			response = client.execute(get);
			jsonResponse = parseResponse(response, expectedResult);
		} catch (ClientProtocolException e) {
			Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}

	private Object postRequest(String resourceLoc, JSONArray jsonRequest, Class expectedResult) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(PROTO_PREFIX + HOST + ":" + PORT + resourceLoc);
		post.addHeader("Content-Type", "application/json");
		Object jsonResponse = null;
		try {
			StringEntity se = new StringEntity(jsonRequest.toString());
			post.setEntity(se);
			HttpResponse response = client.execute(post);
			jsonResponse = parseResponse(response, expectedResult);
		} catch (ClientProtocolException e) {
			Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}

	private Object parseResponse(HttpResponse response, Class expectedResult) {
		Object jsonResponse = null;
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
				String inputline, text = "";
				while ((inputline = reader.readLine()) != null) {
					text += inputline + '\n';
				}
				try {
					if (expectedResult.equals(JSONArray.class)) {
						jsonResponse = new JSONArray(text);
					} else if (expectedResult.equals(JSONObject.class)) {
						jsonResponse = new JSONObject(text);
					}
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

	public JSONArray getAllBuildings() {
		JSONArray ja = null;
		Object obj = getRequest(ROOMS, JSONArray.class);
		if (obj instanceof JSONArray) {
			ja = (JSONArray) obj;
		}
		return ja;
	}

	public JSONObject getBuilding(String building) {
		JSONObject jo = null;
		String resourceLocation = String.format("%s%s", ROOMS, building);
		Object obj = getRequest(resourceLocation, JSONObject.class);
		if (obj instanceof JSONObject) {
			jo = (JSONObject) obj;
		}
		return jo;
	}

	public JSONObject getFloor(String building, String floor) {
		JSONObject jo = null;
		String resourceLocation = String.format("%s%s/%s", ROOMS, building, floor);
		Object obj = getRequest(resourceLocation, JSONObject.class);
		if (obj instanceof JSONObject) {
			jo = (JSONObject) obj;
		}
		return jo;
	}

	public JSONObject getRoom(String building, String floor, String roomNumber) {
		JSONObject jo = null;
		String resourceLocation = String.format("%s%s/%s/%s", ROOMS, building, floor, roomNumber);
		Object obj = getRequest(resourceLocation, JSONObject.class);
		if (obj instanceof JSONObject) {
			jo = (JSONObject) obj;
		}
		return jo;
	}

	public JSONObject getLocation(JSONArray wifiReadings) {
		JSONObject jo = null;
		Object obj = postRequest(LOCATION, wifiReadings, JSONObject.class);
		if (obj instanceof JSONObject) {
			jo = (JSONObject) obj;
		}
		return jo;
	}
}
