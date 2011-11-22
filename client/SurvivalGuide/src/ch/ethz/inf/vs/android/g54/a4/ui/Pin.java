package ch.ethz.inf.vs.android.g54.a4.ui;

import android.graphics.Point;

public class Pin {
	
	private Point location;
	private int radius;
	private Colour colour;
	private String label;
	
	public Pin(Point location, int radius, Colour colour, String label) {
		this.location = location;
		this.radius = radius;
		this.location = location;
		this.label = label;
	}
	
	public Point getLocation() {
		return location;
	}

	public int getRadius() {
		return radius;
	}	

	public Colour getColour() {
		return colour;
	}

	public String getLabel() {
		return label;
	}	
	
	public void setLocation(Point location) {
		this.location = location;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	public void setColour(Colour colour) {
		this.colour = colour;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	
		
}
