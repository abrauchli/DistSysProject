package ch.ethz.inf.vs.android.g54.a4.ui;

import ch.ethz.inf.vs.android.g54.a4.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MapTest extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_test);
		
		WebView web = (WebView) findViewById(R.id.web_maptest);
		web.getSettings().setBuiltInZoomControls(true);
		web.loadUrl("http://deserver.moeeeep.com:32123/static/cache/CAB_E_11.gif");
	}

}
