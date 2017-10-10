package com.example.fullenergy.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_deposit)
public class RentCarDeposit extends Fragment {

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView rentcar_deposit_button;
    @ViewById
    LinearLayout turn_to_index;
    @ViewById
    CheckBox check_2,check_1;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Click
    void rentcar_deposit_button(){
        if(check_1.isChecked()){
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("rentCarOrder", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deposit", "3500");
            editor.commit();

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
            fragmentTransaction.replace(R.id.rentcar_panel, new RentCarCost_());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if(check_2.isChecked()){
            Toast.makeText(getActivity(),"目前不支持此业务！",Toast.LENGTH_LONG).show();
        }
    }

    @Click
    void check_1(){
        check_2.setChecked(false);
        check_1.setChecked(true);
    }

    @Click
    void check_2(){
        check_1.setChecked(false);
        check_2.setChecked(true);
    }

    @Click
    void turn_to_index(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarIndex_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Click
    void page_return() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarIndex_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
                    fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
                    fragmentTransaction.replace(R.id.rentcar_panel, new RentCarProvince_());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
    }


}
