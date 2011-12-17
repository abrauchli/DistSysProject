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

import java.util.List;

import ch.ethz.inf.vs.android.g54.a4.ui.LocationMarker.OnMarkerClickListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

	private static final int CLICK_THRESHOLD_RADIUS = 100;
	private static final String TAG = "TouchImageView";
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Bitmap bm, mbm;
	Paint paint = new Paint();
	List<LocationMarker> markers;
	int viewWidth;
	int viewHeight;

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	Context context;

	OnSizeChangedListener listener;

	public TouchImageView(Context context) {
		this(context, null);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setClickable(true);
		this.context = context;

		paint.setAntiAlias(true);

		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);

		setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent rawEvent) {
				WrapMotionEvent event = wrap(rawEvent);

				dumpEvent(event);

				// Handle touch events here...
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					savedMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					Log.v(TAG, "mode=DRAG");
					mode = DRAG;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					Log.v(TAG, "oldDist=" + oldDist);
					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
						Log.v(TAG, "mode=ZOOM");
					}
					break;
				case MotionEvent.ACTION_UP:
					int xDiff = (int) Math.abs(event.getX() - start.x);
					int yDiff = (int) Math.abs(event.getY() - start.y);
					if (xDiff < 8 && yDiff < 8) {
						performClick(event.getX(), event.getY());
					}
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					Log.v(TAG, "mode=NONE");
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						// ...
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
					} else if (mode == ZOOM) {
						float newDist = spacing(event);
						Log.v(TAG, "newDist=" + newDist);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, mid.x, mid.y);
						}
					}
					break;
				}
				setImageMatrix(matrix);
				return true; // indicate event was handled
			}
		});
	}

	public void setImage(Bitmap bm) {
		Log.d(TAG, String.format("setImage with dimensions %dx%d", bm.getWidth(), bm.getHeight()));
		super.setImageBitmap(bm);
		this.bm = bm;

		// Fit to screen.
		float scale;
		if ((viewHeight / bm.getHeight()) >= (viewWidth / bm.getWidth())) {
			scale = (float) viewWidth / (float) bm.getWidth();
		} else {
			scale = (float) viewHeight / (float) bm.getHeight();
		}

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postScale(scale, scale, mid.x, mid.y);
		setImageMatrix(matrix);

		// Center the image
		float redundantYSpace = (float) viewHeight - (scale * (float) bm.getHeight());
		float redundantXSpace = (float) viewWidth - (scale * (float) bm.getWidth());

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	public void setMarkers(List<LocationMarker> markers) {
		this.markers = markers;
	}

	public void centerImage() {
		Log.d(TAG, "centerImage");
		matrix = new Matrix();
		savedMatrix = new Matrix();

		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);

		// Fit to screen.
		float scale;
		if ((viewHeight / bm.getHeight()) >= (viewWidth / bm.getWidth())) {
			scale = (float) viewWidth / (float) bm.getWidth();
		} else {
			scale = (float) viewHeight / (float) bm.getHeight();
		}

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postScale(scale, scale, 0, 0);
		setImageMatrix(matrix);

		// Center the image
		float redundantYSpace = (float) viewHeight - (scale * (float) bm.getHeight());
		float redundantXSpace = (float) viewWidth - (scale * (float) bm.getWidth());

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	public void centerZoomImage() {
		Log.d(TAG, "centerZoomImage");
		matrix = new Matrix();
		savedMatrix = new Matrix();

		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);

		// Fit to screen.
		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postScale(1, 1, 0, 0);
		setImageMatrix(matrix);

		// Center the image
		float redundantYSpace = (float) viewHeight - ((float) bm.getHeight());
		float redundantXSpace = (float) viewWidth - ((float) bm.getWidth());

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	public void centerPoint(Point p) {
		Log.d(TAG, String.format("centerPoint on %s", p.toString()));
		matrix = new Matrix();
		savedMatrix = new Matrix();

		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);

		// Fit to screen.
		float scale;
		if ((viewHeight / bm.getHeight()) >= (viewWidth / bm.getWidth())) {
			scale = (float) viewWidth / (float) bm.getWidth();
		} else {
			scale = (float) viewHeight / (float) bm.getHeight();
		}

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postScale(scale, scale, 0, 0);
		setImageMatrix(matrix);

		// Center the image
		float redundantYSpace = (float) viewHeight - (scale * ((float) 2 * (float) p.y));
		float redundantXSpace = (float) viewWidth - (scale * ((float) 2 * (float) p.x));

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	public void centerZoomPoint(Point p) {
		Log.d(TAG, String.format("centerZoomPoint on %s", p.toString()));
		matrix = new Matrix();
		savedMatrix = new Matrix();

		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);

		// Fit to screen.
		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postScale(1, 1, 0, 0);
		setImageMatrix(matrix);

		// Center the image
		float redundantYSpace = (float) viewHeight - (float) 2 * (float) p.y;
		float redundantXSpace = (float) viewWidth - (float) 2 * (float) p.x;

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	public void updateMarkers() {
		mbm = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.RGB_565);
		Canvas canvas = new Canvas(mbm);
		super.setImageBitmap(mbm);
		canvas.drawBitmap(bm, 0, 0, null);
		for (int i = 0; i < markers.size(); i++) {
			LocationMarker marker = markers.get(i);
			drawMarker(canvas, marker.getPosition().x, marker.getPosition().y, marker.getRadius(), marker.getColor());
		}
	}

	public void drawMarker(Canvas canvas, int x, int y, int radius, int color) {
		paint.setColor(color);

		// Draw location of access point
		paint.setStyle(Style.FILL);
		canvas.drawCircle(x, y, 5, paint);

		// Draw signal strength
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(x, y, radius, paint);
	}

	public boolean performClick(float x, float y) {
		Matrix inverse = new Matrix();
		getImageMatrix().invert(inverse);
		float[] touchPoint = new float[] { x, y };
		inverse.mapPoints(touchPoint);
		double distance = Integer.MAX_VALUE;
		LocationMarker closestMarker = null;
		for (int i = 0; i < markers.size(); i++) {
			LocationMarker m = markers.get(i);
			float xDist = (float) m.getPosition().x - touchPoint[0];
			float yDist = (float) m.getPosition().y - touchPoint[1];
			double distClickPoint = Math.sqrt(xDist * xDist + yDist * yDist);
			if (distance > distClickPoint && distClickPoint < CLICK_THRESHOLD_RADIUS) {
				distance = distClickPoint;
				closestMarker = m;
			}
		}

		if (closestMarker != null) {
			OnMarkerClickListener onClickListener = closestMarker.getOnClickListener();
			if (onClickListener != null) {
				onClickListener.onClick(closestMarker);
			}
		}
		return true;
	}

	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(WrapMotionEvent event) {
		// ...
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		Log.v(TAG, sb.toString());
	}

	public void recycleBitmaps() {
		// calling this before loading/setting an image should prevent OutOfMemoryExceptions
		if (bm != null) {
			bm.recycle();
			bm = null;
		}
		if (mbm != null) {
			mbm.recycle();
			mbm = null;
		}
		System.gc(); // try to run the garbage collector to free more memory
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, String.format("onSizeChanged to %dx%d", w, h));
		viewWidth = w;
		viewHeight = h;
		if (listener != null) {
			listener.onSizeChanged(viewWidth, viewHeight);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		this.listener = listener;
	}

	/** Determine the space between the first two fingers */
	private float spacing(WrapMotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, WrapMotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public interface OnSizeChangedListener {
		void onSizeChanged(int viewWidth, int viewHeight);
	}

	private class EclairMotionEvent extends WrapMotionEvent {

		protected EclairMotionEvent(MotionEvent event) {
			super(event);
		}

		public float getX(int pointerIndex) {
			return event.getX(pointerIndex);
		}

		public float getY(int pointerIndex) {
			return event.getY(pointerIndex);
		}

		public int getPointerCount() {
			return event.getPointerCount();
		}

		public int getPointerId(int pointerIndex) {
			return event.getPointerId(pointerIndex);
		}
	}

	private WrapMotionEvent wrap(MotionEvent event) {
		try {
			return new EclairMotionEvent(event);
		} catch (VerifyError e) {
			return new WrapMotionEvent(event);
		}
	}

	private class WrapMotionEvent {
		protected MotionEvent event;

		protected WrapMotionEvent(MotionEvent event) {
			this.event = event;
		}

		public int getAction() {
			return event.getAction();
		}

		public float getX() {
			return event.getX();
		}

		public float getX(int pointerIndex) {
			verifyPointerIndex(pointerIndex);
			return getX();
		}

		public float getY() {
			return event.getY();
		}

		public float getY(int pointerIndex) {
			verifyPointerIndex(pointerIndex);
			return getY();
		}

		public int getPointerCount() {
			return 1;
		}

		public int getPointerId(int pointerIndex) {
			verifyPointerIndex(pointerIndex);
			return 0;
		}

		private void verifyPointerIndex(int pointerIndex) {
			if (pointerIndex > 0) {
				throw new IllegalArgumentException(
						"Invalid pointer index for Donut/Cupcake");
			}
		}

	}
}