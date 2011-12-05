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
package ch.ethz.inf.vs.android.g54.a4.ui;

import java.util.ArrayList;

import ch.ethz.inf.vs.android.g54.a4.R;
import ch.ethz.inf.vs.android.g54.a4.ui.ScrollMapView.ScrollMapViewListener;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MapView extends FrameLayout implements ScrollMapViewListener {

	public ScrollMapView scollMapView;
	public FrameLayout contentView;
	public ArrayList<Pin> listPins;
	public int displayWidth;
	public int displayHeight;
	
	public MapView(Context context) {
		super(context);
		
		Display display = ((WindowManager) 
				context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		
		// TODO Auto-generated constructor stub
		listPins = new ArrayList<Pin>();
		
		scollMapView = new ScrollMapView(context);
		scollMapView.setMap(BitmapFactory.decodeResource(getResources(), R.drawable.hg_e));
		addView(scollMapView);
		scollMapView.setListener(this);
		
		contentView = new FrameLayout(context);
		addView(contentView);
		scollMapView.setContentView(contentView);		
	}

	public void onPinAdded(Pin button) {
		// TODO Auto-generated method stub
		listPins.add(button);
	}
}