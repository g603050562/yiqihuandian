package com.example.fullenergystore.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.NoneFragement;

public class PanelRentCar extends Fragment {

    private View view;
    private boolean pageHidden = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.panel_rentcar, container, false);

        return view;
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(!pageHidden){
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.panelRentcarPanel, new PanelRentCarIndex());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            finishActivity();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (hidden) {
            pageHidden = true;
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.panelRentcarPanel, new NoneFragement());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else{
            pageHidden = false;
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.panelRentcarPanel,new PanelRentCarIndex());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        finishActivity();
    }

    private void finishActivity() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
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
                    title.setText("是否要退出充电网？");

                    TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
                    success.setText("确定");
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            getActivity().finish();
                        }
                    });
                    TextView error = (TextView) view.findViewById(R.id.AlertdialogCancel);
                    error.setText("取消");
                    error.setOnClickListener(new View.OnClickListener() {
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
