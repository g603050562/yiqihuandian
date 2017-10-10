package com.example.fullenergystore.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.fullenergystore.R;

public class PanelIndexWithdrawListDetails extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelWithdrawListDetailReturn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_index_withdraw_list_detail,container,false);
		
		init();
		
		return view;
	}
	
	private void init(){
		panelWithdrawListDetailReturn = (LinearLayout) view.findViewById(R.id.panelWithdrawListDetailReturn);
		panelWithdrawListDetailReturn.setOnClickListener(this);
	}

	
	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == panelWithdrawListDetailReturn.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMessageWithdrawList panelIndexWithdrawList = new PanelMessageWithdrawList();
			fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
			fragmentTransaction.replace(R.id.panelIndexPanel, panelIndexWithdrawList);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
	
}
