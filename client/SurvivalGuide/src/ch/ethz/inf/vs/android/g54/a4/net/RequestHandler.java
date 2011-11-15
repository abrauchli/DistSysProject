package ch.ethz.inf.vs.android.g54.a4.net;

import java.io.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.json.*;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RequestHandler {
	private static final String PROTO_PREFIX = "http://";
	private static final String HOST = "deserver.moeeeep.com";
	private static final int PORT = 23032;
	
	private static final String LOC_ROOMS = "/r/";
	private static final String LOC_MACS = "/m/";
	
	private static final String TAG = "SG_NetLib";
	
	private Context context;
	
	public RequestHandler(Context context){
		this.context = context;
	}
	
	private JSONObject getRequest(String resourceLoc) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(PROTO_PREFIX + HOST + ":" + PORT + resourceLoc);
		get.addHeader("Accept", "application/json");
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = client.execute(get);
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
		} catch (ClientProtocolException e) {
			Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
			Log.e(TAG, e.toString());
		}
		return jsonResponse;
	}
	
	public JSONObject getAllBuildings(){
		return getRequest(LOC_ROOMS);
	}
	
	public JSONObject getBuilding(String building){
		String resourceLocation = String.format("%s%s", LOC_ROOMS, building);
		return getRequest(resourceLocation);
	}
	
	public JSONObject getFloor(String building, String floor){
		String resourceLocation = String.format("%s%s/%s", LOC_ROOMS, building, floor);
		return getRequest(resourceLocation);
	}
	
	public JSONObject getRoom(String building, String floor, String roomNumber){
		String resourceLocation = String.format("%s%s/%s/%s", LOC_ROOMS, building, floor, roomNumber);
		return getRequest(resourceLocation);
	}
}
