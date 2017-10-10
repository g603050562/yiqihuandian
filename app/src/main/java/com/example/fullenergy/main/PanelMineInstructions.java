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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.example.fullenergy.R;

public class PanelMineInstructions extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelMineInstructionsReturn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_mine_instructions,container,false);
		
		init();
		
		
		return view;
	}
	
	private void init(){
		panelMineInstructionsReturn = (LinearLayout) view.findViewById(R.id.panelMineInstructionsReturn);
		panelMineInstructionsReturn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == panelMineInstructionsReturn.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineIndex index = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, index);
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
					PanelMineIndex index = new PanelMineIndex();
					fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, index);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
}
