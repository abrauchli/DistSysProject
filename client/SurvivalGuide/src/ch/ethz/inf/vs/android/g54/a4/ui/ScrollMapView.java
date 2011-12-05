package ch.ethz.inf.vs.android.g54.a4.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ScrollMapView extends View {
	private static Bitmap bmLargeImage = null; // bitmap large enough to scroll
	private static Rect displayRect = null; // rect we display to
	private Rect scrollRect = null; // rect we scroll over our bitmap with
	private int displayWidth = 0;
	private int displayHeight = 0;
	private int scrollRectX = 0; // current left location of scroll rect
	private int scrollRectY = 0; // current top location of scroll rect
	private float scrollByX = 0; // x amount to scroll by
	private float scrollByY = 0; // y amount to scroll by
	private float startX = 0; // track x from one ACTION_MOVE to the next
	private float startY = 0; // track y from one ACTION_MOVE to the next
	private float downX = 0; // x cached at ACTION_DOWN
	private float downY = 0; // y cached at ACTION_DOWN
	Vibrator vibrator;
	
	private boolean isLongPress = false; // only want one long press per gesture
	private boolean hasNotMoved = true; // long-press determination

	// Get static method data via static access to ViewConfiguration.
	private static final long tapTime = ViewConfiguration.getTapTimeout();
	private static final long longPressTime = 
				ViewConfiguration.getLongPressTimeout();
	private static int scaledTouchSlopSquared = 0;

	private static final int MSG_LONG_PRESS = 1;

	private FrameLayout contentView;
	
	private ScrollMapViewListener listener;
	
	public ScrollMapView(Context context) {
		super(context);
		Display display = ((WindowManager) 
				context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		vibrator = ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE));
		
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		
		displayRect = new Rect(0, 0, displayWidth, displayHeight);
		scrollRect = new Rect(0, 0, displayWidth, displayHeight);
		
		// Get non-static method data from ViewConfiguration.
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		scaledTouchSlopSquared = viewConfiguration.getScaledTouchSlop()
				* viewConfiguration.getScaledTouchSlop();
	}
	
	public void setMap(Bitmap bitmap) {
		bmLargeImage = bitmap;
	}
	
	public void setContentView(FrameLayout contentView) {
		this.contentView = contentView;
	}
	
	public void setListener(ScrollMapViewListener listener) {
		this.listener = listener;
	}
	
	private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("what")) {
				// Schedule a long press. This will be canceled if:
				//  * gesture finishes (ACTION_UP)
				//  * gesture is canceled (ACTION_CANCEL)
				//  * we move outside of our 'slop' range
				case MSG_LONG_PRESS:
					handleLongPress(msg.getData());
					break;

			default:
				throw new RuntimeException("handleMessage: unknown message "
											+ msg);
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// Remember our initial down event location.
				startX = downX = event.getRawX();
				startY = downY = event.getRawY();
				// We will set this once a long-press actually fires.
				isLongPress = false;
				// Assume this is a long-press, it will be canceled either
				// by moving or by canceling the gesture.
				messageHandler.removeMessages(0, null);
				Bundle data = new Bundle();
				data.putInt("what", MSG_LONG_PRESS);
				data.putFloat("x", startX);
				data.putFloat("y", startY);
				Message msg = new Message();
				msg.setData(data);
				messageHandler.sendMessageAtTime(msg, event.getDownTime() + tapTime + longPressTime);
				break;

			case MotionEvent.ACTION_MOVE:
				// A long-press has fired and is in progress. Don't bother with
				// any other processing. A side-effect of this is that we won't
				// be able to do any other movement until we either cancel 
				// (ACTION_CANCEL) or finish (ACTION_UP) the current movement.
				if (isLongPress)
					break;
							
				final float x = event.getRawX();
				final float y = event.getRawY();
				scrollByX = x - startX; // calculate move increments
				scrollByY = y - startY;

				if (hasNotMoved) {
					// Have we moved out of the threshold radius of initial 
					// user touch?
					final int deltaXFromDown = (int)(x - downX);
					final int deltaYFromDown = (int)(y - downY);
					int distance = (deltaXFromDown * deltaXFromDown)
									+ (deltaYFromDown * deltaYFromDown);
					if (distance > scaledTouchSlopSquared) {
						// We've moved so handle scroll update and cancel 
						// long-press.
						hasNotMoved = false;
						messageHandler.removeMessages(0, null);
						startX = x; // reset previous values to latest
						startY = y;
						invalidate();
					}
				} else {
					// This is a move gesture so update our move incrementers
					// for next pass through ACTION_MOVE, and force a redraw
					// with the updated scroll values. We don't need to call 
					// .removeMessages() since the only way here is via the if 
					// block above which calls it for us.
					startX = x; // reset previous values to latest
					startY = y;
					invalidate();
				} 
				break;
				
			case MotionEvent.ACTION_UP:
				isLongPress = false;
				hasNotMoved = true;
				listener.onCoordsChanged(scrollRectX, scrollRectY);
				messageHandler.removeMessages(0, null);
				break;

			case MotionEvent.ACTION_CANCEL:
				isLongPress = false;
				hasNotMoved = true;
				messageHandler.removeMessages(0, null);
				break;
		}
		return true; // done with this event so consume it
	}

	void handleLongPress(Bundle data) {
		// Indicate that a long-press has fired.
		isLongPress = true;
		
		// Execute your long press code here.+
		Context context = getContext();
		int duration = Toast.LENGTH_SHORT;

		int xPos = (int) data.getFloat("x") + scrollRectX;
		int yPos = (int) data.getFloat("y") + scrollRectY;
		
		Toast toast = Toast.makeText(context, xPos + " " + yPos , duration);
		toast.show();
		Pin pin = new Pin(context, xPos, yPos, "new pin");
		pin.setMargins((int) data.getFloat("x"), (int) data.getFloat("y"));
		listener.onPinAdded(pin);
		contentView.addView(pin);
		vibrator.vibrate(50);
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

		scrollRect.set(newScrollRectX, newScrollRectY, newScrollRectX
				+ displayWidth, newScrollRectY + displayHeight);
		Paint paint = new Paint();
		canvas.drawBitmap(bmLargeImage, scrollRect, displayRect, paint);

		scrollRectX = newScrollRectX; // reset previous to latest
		scrollRectY = newScrollRectY;
	}
	
	public interface ScrollMapViewListener {
		
		public void onPinAdded(Pin button);
		
		public void onCoordsChanged(int xCoord, int yCoord);
	}
}