package com.example.fullenergystore.extend_plug.StatusBar;

import android.app.Activity;
import android.view.WindowManager;

public class statusBar {

	private Activity activity;
	
	public statusBar(Activity activity) {
		this.activity = activity;
		
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
	}
	
	
}
