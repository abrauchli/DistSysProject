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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.ClientProtocolException;
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.util.U;

/**
 * Singleton class in charge of http connections to the server Also does basic JSON parsing and displays toast on errors
 */
public class RequestHandler {
	private static String TAG = "SG RequestHandler";

	private static final String HOST = "http://deserver.moeeeep.com";
	// private static final String HOST = "http://129.132.185.110";
	private static final int PORT = 32123;

	private static RequestHandler instance = null;

	/** Singleton constructor */
	private RequestHandler() {
	}

	/** Get the RequestHandler singleton */
	public static RequestHandler getInstance() {
		if (RequestHandler.instance == null) {
			RequestHandler.instance = new RequestHandler();
		}
		return RequestHandler.instance;
	}

	/**
	 * Execute an HTTP get on a given resource on the configured server
	 * 
	 * @param res
	 *            The resource URL without host
	 * @return a JSONObject / JSONArray
	 * @throws ServerException
	 * @throws ConnectionException
	 * @throws UnrecognizedResponseException
	 */
	public Object request(String res) throws ServerException, ConnectionException, UnrecognizedResponseException {
		Log.d(TAG, String.format("Sending request for resource %s.", res));

		HttpClient client = new DefaultHttpClient();
		String responseBody = null;
		try {
			HttpGet htget = new HttpGet(HOST + ":" + PORT + res);
			BasicHeader header = new BasicHeader("Accept", "application/json");
			htget.addHeader(header);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(htget, responseHandler);
		} catch (ClientProtocolException e) {
			throw new ServerException("Server returned an error.", e);
		} catch (IOException e) {
			throw new ConnectionException("Could not connect to server.", e);
		} finally {
			client.getConnectionManager().shutdown();
		}
		return parseResponse(responseBody);
	}

	/**
	 * Downloads image from given URL and converts it to a Bitmap
	 * 
	 * @throws ConnectionException
	 * @throws UnrecognizedResponseException
	 */
	public static Bitmap getBitmap(String imageURL) throws ConnectionException, UnrecognizedResponseException {
		Log.d(TAG, "download image from:" + imageURL);

		try {
			URL url = new URL(imageURL);

			// Open a connection to URL
			URLConnection ucon = url.openConnection();

			// Define InputStreams to read from the URLConnection.
			InputStream is = ucon.getInputStream();

			// Read stream into a bitmap
			return BitmapFactory.decodeStream(is);
		} catch (MalformedURLException e) {
			String msg = String.format("Could not resolve url %s.", imageURL);
			throw new ConnectionException(msg, e);
		} catch (IOException e) {
			String msg = String.format("Could not download image from %s.", imageURL);
			throw new UnrecognizedResponseException(msg, e);
		}
	}

	/**
	 * Execute an HTTP post on a given resource on the configured server
	 * 
	 * @param res
	 *            The resource URL without host
	 * @param data
	 *            The data to post
	 * @return a JSONObject / JSONArray
	 * @throws ServerException
	 * @throws ConnectionException
	 * @throws UnrecognizedResponseException
	 */
	public Object post(String res, String data) throws ServerException, ConnectionException,
			UnrecognizedResponseException {
		U.logInPieces(TAG, String.format("Sending request for resource %s with data %s.", res, data));

		HttpClient client = new DefaultHttpClient();
		String responseBody = null;

		try {
			HttpPost post = new HttpPost(HOST + ":" + PORT + res);
			post.addHeader("Content-Type", "application/json");
			StringEntity se = new StringEntity(data);
			post.setEntity(se);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(post, responseHandler);

		} catch (ClientProtocolException e) {
			throw new ServerException("Server returned an error.", e);
		} catch (IOException e) {
			throw new ConnectionException("Could not connect to server.", e);
		} finally {
			client.getConnectionManager().shutdown();
		}
		return parseResponse(responseBody);
	}

	/**
	 * Do the actual JSON parsing and ensure a correct server response
	 * 
	 * @throws UnrecognizedResponseException
	 * @throws ServerException
	 */
	private Object parseResponse(String response) throws UnrecognizedResponseException, ServerException {
		U.logInPieces(TAG, "Server responded: " + response);

		try {
			JSONObject jso = new JSONObject(response);
			if (!jso.getBoolean("ok")) {
				if (jso.has("msg")) {
					String msg = jso.getString("msg");
					throw new ServerException(msg);
				} else {
					throw new ServerException("Server returned ok=false, without giving any further information.");
				}
			}
			if (jso.has("result")) {
				return jso.get("result");
			} else {
				throw new UnrecognizedResponseException("Answer of the server doesn't have a result field.");
			}

		} catch (JSONException e) {
			throw new UnrecognizedResponseException("Answer of the server doesn't have the expected form.", e);
		}
	}
}
