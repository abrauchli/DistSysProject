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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class TouchImageView extends ImageView {

    private static final String TAG = "Touch";
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Bitmap bm, mbm;
    Paint paint = new Paint();
    List<Pin2> pins;
    int displayWidth;
    int displayHeight;

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


    public TouchImageView(Context context) {
        super(context);
        super.setClickable(true);
        this.context = context;
        
    	Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
        
        paint.setAntiAlias(true);
        
        matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent rawEvent) {
                WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);

            	dumpEvent(event);

                // Handle touch events here...
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    Log.d(TAG, "mode=DRAG");
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    Log.d(TAG, "oldDist=" + oldDist);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        Log.d(TAG, "mode=ZOOM");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    int xDiff = (int) Math.abs(event.getX() - start.x);
                    int yDiff = (int) Math.abs(event.getY() - start.y);
                    if (xDiff < 8 && yDiff < 8){
                        performClick(event.getX(), event.getY());
                    }
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    Log.d(TAG, "mode=NONE");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        // ...
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        Log.d(TAG, "newDist=" + newDist);
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
        super.setImageBitmap(bm);
        this.bm = bm;

        //Fit to screen.
        float scale;
        if ((displayHeight / bm.getHeight()) >= (displayWidth / bm.getWidth())){
            scale =  (float)displayWidth / (float)bm.getWidth();
        } else {
            scale = (float)displayHeight / (float)bm.getHeight();
        }

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(scale, scale, mid.x, mid.y);
        setImageMatrix(matrix);

        // Center the image
        float redundantYSpace = (float)displayHeight - (scale * (float)bm.getHeight()) ;
        float redundantXSpace = (float)displayWidth - (scale * (float)bm.getWidth());

        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }
    
    public void setPins(List<Pin2> pins) {
    	this.pins = pins;
    }
    
    public void centerImage() {
    	matrix = new Matrix();
    	savedMatrix = new Matrix();
    	
    	matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
    	
    	//Fit to screen.
        float scale;
        if ((displayHeight / bm.getHeight()) >= (displayWidth / bm.getWidth())){
            scale =  (float)displayWidth / (float)bm.getWidth();
        } else {
            scale = (float)displayHeight / (float)bm.getHeight();
        }

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(scale, scale, 0, 0);
        setImageMatrix(matrix);

        // Center the image
        float redundantYSpace = (float)displayHeight - (scale * (float)bm.getHeight()) ;
        float redundantXSpace = (float)displayWidth - (scale * (float)bm.getWidth());

        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }
    
    public void centerZoomImage() {
    	matrix = new Matrix();
    	savedMatrix = new Matrix();
    	
    	matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
    	
    	//Fit to screen.
        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(1, 1, 0, 0);
        setImageMatrix(matrix);

        // Center the image
        float redundantYSpace = (float)displayHeight - ((float)bm.getHeight()) ;
        float redundantXSpace = (float)displayWidth - ((float)bm.getWidth());

        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }
    
    public void centerPoint(int x, int y) {
    	matrix = new Matrix();
    	savedMatrix = new Matrix();
    	
    	matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
    	
    	//Fit to screen.
        float scale;
        if ((displayHeight / bm.getHeight()) >= (displayWidth / bm.getWidth())){
            scale =  (float)displayWidth / (float)bm.getWidth();
        } else {
            scale = (float)displayHeight / (float)bm.getHeight();
        }

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(scale, scale, 0, 0);
        setImageMatrix(matrix);

        // Center the image
        float redundantYSpace = (float)displayHeight - (scale * ((float)2*(float)y)) ;
        float redundantXSpace = (float)displayWidth - (scale * ((float)2*(float)x));

        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }
    
    public void centerZoomPoint(int x, int y) {
    	matrix = new Matrix();
    	savedMatrix = new Matrix();
    	
    	matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
    	
    	//Fit to screen.
        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(1, 1, 0, 0);
        setImageMatrix(matrix);

        // Center the image
        float redundantYSpace = (float)displayHeight - (float)2*(float)y;
        float redundantXSpace = (float)displayWidth - (float)2*(float)x;

        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        Toast toast = Toast.makeText(getContext(), matrix.toString(), Toast.LENGTH_SHORT);
		toast.show();
        setImageMatrix(matrix);
    }
    
    public void updatePins() {
    	mbm = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
    	Canvas canvas = new Canvas(mbm);
    	super.setImageBitmap(mbm);
    	canvas.drawBitmap(bm, 0, 0, paint);
    	for (int i = 0; i < pins.size(); i++) {
    		Pin2 pin = pins.get(i);
    		drawPin(canvas, pin.getPosition().x, pin.getPosition().y, pin.getRadius(), pin.getColour());
    	}
    }
    
    public void drawPin(Canvas canvas, int x, int y, int radius, int colour) {
		paint.setColor(colour);
		
		//Draw location of access point
		paint.setStyle(Style.FILL);
		canvas.drawCircle(x, y, 5, paint);
		
		//Draw signal strength
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(x, y, radius, paint);
	}

    public boolean performClick(float x, float y) {
		Toast toast = Toast.makeText(getContext(), x + " " + y, Toast.LENGTH_SHORT);
		toast.show();
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
        Log.d(TAG, sb.toString());
    }

    public void recycleBitmaps() {
    	bm.recycle();
    	mbm.recycle();
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
}