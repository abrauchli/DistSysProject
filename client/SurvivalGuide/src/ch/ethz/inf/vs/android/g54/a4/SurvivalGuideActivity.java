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
package ch.ethz.inf.vs.android.g54.a4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ServerException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.types.AccessPoint;
import ch.ethz.inf.vs.android.g54.a4.types.Address;
import ch.ethz.inf.vs.android.g54.a4.types.Address.Campus;
import ch.ethz.inf.vs.android.g54.a4.types.Building;
import ch.ethz.inf.vs.android.g54.a4.types.Coordinate;
import ch.ethz.inf.vs.android.g54.a4.types.Floor;
import ch.ethz.inf.vs.android.g54.a4.types.Location;
import ch.ethz.inf.vs.android.g54.a4.types.Room;
import ch.ethz.inf.vs.android.g54.a4.types.WifiReading;
import ch.ethz.inf.vs.android.g54.a4.ui.LocationMarker;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView;
import ch.ethz.inf.vs.android.g54.a4.ui.TouchImageView.OnSizeChangedListener;
import ch.ethz.inf.vs.android.g54.a4.ui.WifiReadingArrayAdapter;
import ch.ethz.inf.vs.android.g54.a4.util.MapCache;
import ch.ethz.inf.vs.android.g54.a4.util.SnapshotCache;
import ch.ethz.inf.vs.android.g54.a4.util.U;

public class SurvivalGuideActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemSelectedListener {

	private static final int BUILDING_MARKER_RADIUS = 100;
	private static final int LOCATION_MARKER_RADIUS = 20;
	private static final String TAG = "SurvivalGuideActivity";

	private enum Mode {
		OVERVIEW,
		FREEROOMS,
		LOCATION
	}

	private String snapshotName = "snapshot";

	private Mode mode;
	private Campus currentCampus;
	private Building currentBuilding;
	private Floor currentFloor;
	private Location currentLocation;
	private LocationThread locationThread;
	private boolean locationScanning;

	Handler handler;

	ArrayAdapter<WifiReading> wifiAdapter;
	TouchImageView tiv_map;

	List<LocationMarker> markers;
	List<WifiReading> visibleNetworks;

	// TODO find a better way to save spinner selection
	String selectedBuilding, selectedFloor, selectedRoom;

	/**
	 * When changing the mode, always use this method, as it updates the layout as well
	 */
	private void setMode(Mode mode) {
		this.mode = mode;
		LinearLayout lin_building = (LinearLayout) findViewById(R.id.lin_building);
		switch (this.mode) {
		case OVERVIEW:
			lin_building.setVisibility(View.GONE);
			break;
		case LOCATION:
			// fall through
		case FREEROOMS:
			lin_building.setVisibility(View.VISIBLE);
			// resetFloorButtons();
			break;
		}
		updateMap();
	}
	
	private Point approximateLocation(List<AccessPoint> aps, Floor limitToFloor) {
		int x = 0, y = 0, weight = 0;
		for (AccessPoint ap : aps) {
			if (ap.getQualifiedFloor().equals(limitToFloor.toString())) {
					Point p = ap.getCoordinate().toPoint();
					x += p.x;
					y += p.y;
					weight += 1;
			}
		}
		if (weight == 0)
			return null;
		else
			return new Point(x/weight, y/weight);
	}

	/**
	 * When toggling the location scanning, always use this method, as it changes the image of the button as well
	 */
	private void toggleLocationScanning() {
		locationScanning = !locationScanning;
		ImageButton tgl_scan = (ImageButton) findViewById(R.id.tgl_scan);
		if (locationScanning) {
			tgl_scan.setImageResource(R.drawable.target_on);
			locationThread.start();
		} else {
			tgl_scan.setImageResource(R.drawable.target);
			locationThread.interrupt();
		}
	}

	/**
	 * When changing campus, always use this method, since TODO: explanation
	 */
	private void setCampus(Campus campus) {
		this.currentCampus = campus;
		RadioGroup grp_campus = (RadioGroup) findViewById(R.id.grp_campus);
		switch (grp_campus.getCheckedRadioButtonId()) {
		case R.id.rbt_eth_center:
			if (currentCampus.equals(Campus.HOENGG)) {
				RadioButton rbt_eth_center = (RadioButton) findViewById(R.id.rbt_eth_center);
				rbt_eth_center.setChecked(true);
			}
			break;
		case R.id.rbt_eth_hoengg:
			if (currentCampus.equals(Campus.ZENTRUM)) {
				RadioButton rbt_eth_hoengg = (RadioButton) findViewById(R.id.rbt_eth_hoengg);
				rbt_eth_hoengg.setChecked(true);
			}
		}
		updateMap();
	}

	/**
	 * Initialize the campus and set the radio buttons accordingly
	 */
	private void initCampus(Campus campus) {
		this.currentCampus = campus;
		switch (campus) {
		case ZENTRUM:
			RadioButton rbt_eth_center = (RadioButton) findViewById(R.id.rbt_eth_center);
			rbt_eth_center.setChecked(true);
			break;
		case HOENGG:
			RadioButton rbt_eth_hoengg = (RadioButton) findViewById(R.id.rbt_eth_hoengg);
			rbt_eth_hoengg.setChecked(true);
		}
	}

	private void updateMap() {
		Bitmap bm;
		switch (mode) {
		case OVERVIEW:
			tiv_map.recycleBitmaps();
			bm = BitmapFactory.decodeResource(getResources(), currentCampus == Campus.ZENTRUM
					? R.drawable.zentrum
					: R.drawable.hoengg);
			tiv_map.setImage(bm);
			tiv_map.centerImage();
			Map<String, Point> buildingsLocations = currentCampus == Campus.ZENTRUM
					? Building.buildingLocationsCenter
					: Building.buildingLocationsHoengg;
			markers.clear();
			for (Map.Entry<String, Point> bLoc : buildingsLocations.entrySet()) {
				markers.add(new LocationMarker(bLoc.getValue(), BUILDING_MARKER_RADIUS, Color.TRANSPARENT, bLoc
						.getKey(),
						buildingClickListener));
			}
			tiv_map.updateMarkers();
			tiv_map.centerZoomPoint(buildingsLocations.get(currentCampus == Campus.ZENTRUM ? "HG" : "HPH"));
			break;
		case LOCATION:
			tiv_map.recycleBitmaps();
			tiv_map.setImage(MapCache.getMap(currentFloor, this));
			updateAPMarkers();
			break;
		case FREEROOMS:
			// TODO
			break;
		}
	}

	void setLocation(Location location) {
		this.currentLocation = location;
		this.currentFloor = currentLocation.getRoom().getFloor();
		this.currentBuilding = currentFloor.getBuilding();
		setMode(Mode.LOCATION);

		Map<String, AccessPoint> aps = location.getAps();
		for (WifiReading reading : visibleNetworks) {
			reading.ap = aps.get(reading.mac);
		}
		wifiAdapter.notifyDataSetChanged();
	}

	/**
	 * Updates the markers for the access points as well as the marker for the location
	 */
	private void updateAPMarkers() {
		markers.clear();
		List<AccessPoint> aps = new ArrayList<AccessPoint>();

		float blueHue = 240;
		float greenHue = 120;
		float orangeHue = 30;
		for (WifiReading reading : visibleNetworks) {
			if (reading.ap != null) {
				aps.add(reading.ap);
				Coordinate coords = reading.ap.getCoordinate();
				int s = reading.signal;
				float saturation;
				if (s < -60)
					saturation = (s + 100) * 0.02f;
				else
					saturation = (s + 60) * 0.005f + 0.8f;
				// FIXME
				int blueish = Color.HSVToColor(new float[] { blueHue, saturation, 1 });
				int orangeish = Color.HSVToColor(new float[] { orangeHue, saturation, 1 });
				int greenish = Color.HSVToColor(new float[] { greenHue, saturation, 1 });
				markers.add(new LocationMarker(coords.toPoint(), 100, blueish, reading.mac));
				// markers.add(new LocationMarker(coords.toPoint(), 120, orangeish, reading.mac));
				// markers.add(new LocationMarker(coords.toPoint(), 80, greenish, reading.mac));
			}
		}

		if (currentLocation != null) {
			Room r = currentLocation.getRoom();
			if (r != null) {
				Coordinate center = r.getRoomCenter();
				if (center != null) {
					markers.add(new LocationMarker(center.toPoint(), LOCATION_MARKER_RADIUS, Color.RED, "Your approximate location"));
					tiv_map.centerZoomPoint(center.toPoint());
				} else {
					tiv_map.centerImage();
				}
			} 
			
