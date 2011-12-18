// Create a copy of the current bitmap
// which will be drawn on.
mbm = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.RGB_565);
Canvas canvas = new Canvas(mbm);
super.setImageBitmap(mbm);

// Draw the original bitmap.
canvas.drawBitmap(bm, 0, 0, null);

// Draw the markers.
for (int i = 0; i < markers.size(); i++) {
	LocationMarker marker = markers.get(i);
	drawMarker(canvas, marker.getPosition().x, marker.getPosition().y, marker.getRadius(), marker.getColor());
}
