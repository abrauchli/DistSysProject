// Calculate the map location on which was
// clicked.
Matrix inverse = new Matrix();
getImageMatrix().invert(inverse);
float[] touchPoint = new float[] { x, y };
inverse.mapPoints(touchPoint);
