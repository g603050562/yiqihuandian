package com.example.fullenergy.main;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;

public class PanelServiceNews extends Activity implements OnClickListener{

	private ImageView buttonReturn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_service_news);
		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);
		
		init();
	}
	
	private void init(){
		buttonReturn = (ImageView) this.findViewById(R.id.panelServiceNewsReturn);
		buttonReturn.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == buttonReturn.getId()){
			this.finish();
		}
	}
}
