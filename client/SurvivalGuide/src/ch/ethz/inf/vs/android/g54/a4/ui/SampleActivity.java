package ch.ethz.inf.vs.android.g54.a4.ui;

import ch.ethz.inf.vs.android.g54.a4.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class SampleActivity extends Activity {

	// Physical display width and height.
	private static int displayWidth = 0;
	private static int displayHeight = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// displayWidth and displayHeight will change depending on screen
		// orientation. To get these dynamically, we should hook onSizeChanged().
		// This simple example uses only landscape mode, so it's ok to get them
		// once on startup and use those values throughout.
		Display display = ((WindowManager)
			getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayWidth = display.getWidth(); 		
		displayHeight = display.getHeight(); 	

		// SampleView constructor must be constructed last as it needs the 
		// displayWidth and displayHeight we just got.
		setContentView(new SampleView(this));
	}
	
	private static class SampleView extends View {
		private static Bitmap bmLargeImage; //bitmap large enough to be scrolled
		private static Rect displayRect = null; //rect we display to
		private Rect scrollRect = null; //rect we scroll over our bitmap with
		private int scrollRectX = 0; //current left location of scroll rect
		private int scrollRectY = 0; //current top location of scroll rect
		private float scrollByX = 0; //x amount to scroll by
		private float scrollByY = 0; //y amount to scroll by
		private float startX = 0; //track x from one ACTION_MOVE to the next
		private float startY = 0; //track y from one ACTION_MOVE to the next

		public SampleView(Context context) {
			super(context);

			// Destination rect for our main canvas draw. It never changes.
			displayRect = new Rect(0, 0, displayWidth, displayHeight);
			// Scroll rect: this will be used to 'scroll around' over the 
			// bitmap in memory. Initialize as above.
			scrollRect = new Rect(0, 0, displayWidth, displayHeight);

			// Load a large bitmap into an offscreen area of memory.
			bmLargeImage = BitmapFactory.decodeResource(getResources(), R.drawable.hg_e);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// Remember our initial down event location.
					startX = event.getRawX();
					startY = event.getRawY();
					break;

				case MotionEvent.ACTION_MOVE:
					float x = event.getRawX();
					float y = event.getRawY();
					// Calculate move update. This will happen many times
					// during the course of a single movement gesture.
					scrollByX = x - startX; //move update x increment
					scrollByY = y - startY; //move update y increment
					startX = x; //reset initial values to latest
					startY = y;
					invalidate(); //force a redraw
					break;
			}
			return true; //done with this event so consume it
		}

		@Override
		protected void onDraw(Canvas canvas) {

			// Our move updates are calculated in ACTION_MOVE in the opposite direction
			// from how we want to move the scroll rect. Think of this as dragging to
			// the left being the same as sliding the scroll rect to the right.
			int newScrollRectX = scrollRectX - (int)scrollByX;
			int newScrollRectY = scrollRectY - (int)scrollByY;

			// Don't scroll off the left or right edges of the bitmap.
			if (newScrollRectX < 0)
				newScrollRectX = 0;
			else if (newScrollRectX > (bmLargeImage.getWidth() - displayWidth))
				newScrollRectX = (bmLargeImage.getWidth() - displayWidth);

			// Don't scroll off the top or bottom edges of the bitmap.
			if (newScrollRectY < 0)
				newScrollRectY = 0;
			else if (newScrollRectY > (bmLargeImage.getHeight() - displayHeight))
				newScrollRectY = (bmLargeImage.getHeight() - displayHeight);

			// We have our updated scroll rect coordinates, set them and draw.
			scrollRect.set(newScrollRectX, newScrollRectY, 
				newScrollRectX + displayWidth, newScrollRectY + displayHeight);
			Paint paint = new Paint();
			canvas.drawBitmap(bmLargeImage, scrollRect, displayRect, paint);

			// Reset current scroll coordinates to reflect the latest updates, 
			// so we can repeat this update process.
			scrollRectX = newScrollRectX;
			scrollRectY = newScrollRectY;
		}
	}
}