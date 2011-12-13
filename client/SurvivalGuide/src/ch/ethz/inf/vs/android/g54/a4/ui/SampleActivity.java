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
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.KeyEvent;

public class SampleActivity extends Activity {
	
	TouchImageView im;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		im = new TouchImageView(this);
		
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.hg_e);
		
		ArrayList<Pin2> pins = new ArrayList<Pin2>();
		Pin2 pin = new Pin2(new Point(0,0), 25, Color.RED, "Pin (0,0)");
		pins.add(pin);
		pin = new Pin2(new Point(0,bm.getHeight()), 25, Color.RED, "Pin (0," + bm.getHeight() + ")");
		pins.add(pin);
		pin = new Pin2(new Point(bm.getWidth(),0), 25, Color.RED, "Pin (" + bm.getWidth() + ",0)");
		pins.add(pin);
		pin = new Pin2(new Point(bm.getWidth(),bm.getHeight()), 25, Color.RED, "Pin (" + bm.getWidth() + "," + bm.getHeight() + ")");
		pins.add(pin);
		pin = new Pin2(new Point(bm.getWidth()/2,bm.getHeight()/2), 25, Color.RED, "Pin (" + (int)(bm.getWidth()/2) + "," + (int)(bm.getHeight()/2) + ")");
		pins.add(pin);
		pin = new Pin2(new Point(175,175), 25, Color.RED, "Pin (175,175)");
		pins.add(pin);
		pin = new Pin2(new Point(175,375), 25, Color.RED, "Pin (175,375)");
		pins.add(pin);
		pin = new Pin2(new Point(200,375), 25, Color.RED, "Pin (200,375)");
		pins.add(pin);
		pin = new Pin2(new Point(175,400), 25, Color.RED, "Pin (175,400)");
		pins.add(pin);
		pin = new Pin2(new Point(200,400), 25, Color.RED, "Pin (200,400)");
		pins.add(pin);
		
		im.setImage(bm);
		im.setPins(pins);
		im.updatePins();
		//im.centerImage();
		//im.centerZoomImage();
		//im.centerPoint(bm.getWidth(), bm.getHeight());
		im.centerZoomPoint(175, 175);
		setContentView(im);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		im.recycleBitmaps();
		return super.onKeyDown(keyCode, event);
	}
}