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

import ch.ethz.inf.vs.android.g54.a4.R;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class Pin extends Button implements OnClickListener {

	private int xCoord = 0;
	private int yCoord = 0;
	private String name = "";
	private FrameLayout.LayoutParams layout;
	
	public Pin(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setText("");
		setBackgroundResource(R.drawable.ic_launcher);
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
		layout = (android.widget.FrameLayout.LayoutParams) getLayoutParams();
		
		setOnClickListener(this);
	}
	
	public Pin(Context context, int xCoord, int yCoord, String name) {
		super(context);
		// TODO Auto-generated constructor stub
		setText("");
		setBackgroundResource(R.drawable.ic_launcher);
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
		layout = (android.widget.FrameLayout.LayoutParams) getLayoutParams();
		
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.name = name;
		
		setOnClickListener(this);
	}
	
	public void setMargins(int xCoord, int yCoord) {
		layout.setMargins(xCoord, yCoord, 0, 0);
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Context context = v.getContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, name + " " + xCoord + " " + yCoord, duration);
		toast.show();
	}

	public int getXCoord() {
		return xCoord;
	}

	public void setXCoord(int xCoord) {
		this.xCoord = xCoord;
	}
	
	public int getYCoord() {
		return yCoord;
	}

	public void setYCoord(int yCoord) {
		this.yCoord = yCoord;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}