package com.example.fullenergy.main;


import android.app.AlertDialog;
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
import android.widget.TextView;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.NoneFragement;

public class PanelShop extends Fragment{
	
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_shop,container,false);

		showPanel();
		
		return view;
		
	}

	@Override
	public void onResume() {
		super.onResume();
		showPanel();
	}



	private void showPanel(){
		PanelShopIndex panelShopIndex = new PanelShopIndex();
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.panelShopPanel, panelShopIndex);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		finishActivity();
	}


	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}

	private void finishActivity() {
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
					LayoutInflater inflater = LayoutInflater.from(getActivity());
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					final AlertDialog mAlertDialog = builder.create();
					View view = inflater.inflate(R.layout.alertdialog, null);
					TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
					titleSmall.setText("");
					titleSmall.setVisibility(View.GONE);
					TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
					content.setText("");
					content.setVisibility(View.GONE);
					ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
					divid.setVisibility(View.GONE);

					TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
					title.setText("是否要退出换电网？");

					TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
					success.setText("确定");
					success.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							getActivity().finish();
						}
					});
					TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
					error.setText("取消");
					error.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mAlertDialog.dismiss();
						}
					});
					mAlertDialog.show();
					mAlertDialog.getWindow().setContentView(view);

					return true;
				}
				return false;
			}
		});
	}
	
}
