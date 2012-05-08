package fr.naonod.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.naonod.R;

public class SplashscreenActivity extends Activity implements Runnable{
	
	private static final int SPLASHSCREEN_DURATION = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splashscreen);
				
		new Handler().postDelayed(this, SPLASHSCREEN_DURATION);
	}

	@Override
	public void run() {
		startActivity(POIMapActivity.getIntent(this));
		finish();		
	}
}