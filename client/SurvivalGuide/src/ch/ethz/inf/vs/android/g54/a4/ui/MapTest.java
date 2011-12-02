package ch.ethz.inf.vs.android.g54.a4.ui;

import ch.ethz.inf.vs.android.g54.a4.R;
import ch.ethz.inf.vs.android.g54.a4.util.U;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MapTest extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_test);
		
		Bundle extras = getIntent().getExtras();
		String url = extras.getString("ch.ethz.inf.vs.android.g54.a4.ImageUrl");
		
		if (url == null) {
			U.showToast("bäääh");
			return;
		}
		
		WebView web = (WebView) findViewById(R.id.web_maptest);
		web.getSettings().setBuiltInZoomControls(true);
		web.setInitialScale(100);
		web.loadUrl(url);
	}

}
