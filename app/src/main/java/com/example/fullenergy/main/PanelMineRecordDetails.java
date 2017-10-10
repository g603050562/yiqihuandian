package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.fullenergy.R;

public class PanelMineRecordDetails extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelMineRecordDetailReturn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_mine_record_detail,container,false);
		
		init();
		
		return view;
	}
	
	private void init(){
		panelMineRecordDetailReturn = (LinearLayout) view.findViewById(R.id.panelMineRecordDetailReturn);
		panelMineRecordDetailReturn.setOnClickListener(this);
	}

	
	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == panelMineRecordDetailReturn.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineRecord panelMineRecord = new PanelMineRecord();
			fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineRecord);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					PanelMineRecord panelMineRecord = new PanelMineRecord();
					fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, panelMineRecord);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
	
}
