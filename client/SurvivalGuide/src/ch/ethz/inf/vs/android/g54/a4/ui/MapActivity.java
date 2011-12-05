package ch.ethz.inf.vs.android.g54.a4.ui;

import ch.ethz.inf.vs.android.g54.a4.R;
import ch.ethz.inf.vs.android.g54.a4.exceptions.ConnectionException;
import ch.ethz.inf.vs.android.g54.a4.exceptions.UnrecognizedResponseException;
import ch.ethz.inf.vs.android.g54.a4.net.RequestHandler;
import ch.ethz.inf.vs.android.g54.a4.util.U;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class MapActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapView map = new MapView(this);
		setContentView(map);
		
		RequestHandler reqhandler = RequestHandler.getInstance();
		
		Bundle extras = getIntent().getExtras();
		String url = extras.getString("ch.ethz.inf.vs.android.g54.a4.ImageUrl");
		int x = extras.getInt("ch.ethz.inf.vs.android.g54.a4.PosX");
		int y = extras.getInt("ch.ethz.inf.vs.android.g54.a4.PosY");
		
		if (url == null) {
			U.showToast("no URL provided");
			return;
		}
		
		try {
			Bitmap bitmap = reqhandler.getBitmap(url);
			map.scollMapView.setMap(bitmap);
			map.listPins.add(new Pin(this, x, y, "test"));	
		} catch (Exception e) {
			U.showException("SurvivalGuide-MapActivity", e);
		}
	}

}
