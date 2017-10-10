package com.example.fullenergy.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_index)
public class RentCarIndex extends Fragment {

    @ViewById
    LinearLayout page_return,rentcar_index_local,rentcar_index_time,rentcar_index_type;
    @ViewById
    TextView rentcar_index_button,rentcar_index_local_text,rentcar_index_time_text,rentcar_index_type_text;

    private String string = null;
    private SharedPreferences sharedPreferences;

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        sharedPreferences = getActivity().getSharedPreferences("rentCarOrder", Activity.MODE_PRIVATE);
        String rent_time_name = null;
        rent_time_name = sharedPreferences.getString("rent_time_name", null);
        if(rent_time_name !=  null){
            rentcar_index_time_text.setText(rent_time_name.toString());
        }
        String address_name = null;
        address_name = sharedPreferences.getString("address_name",null);
        if(address_name !=  null){
            rentcar_index_local_text.setText(address_name.toString());
        }
        String car_name = null;
        car_name = sharedPreferences.getString("car_name",null);
        if(car_name !=  null){
            rentcar_index_type_text.setText(car_name.toString());
        }
    }

    @Click
    void rentcar_index_button(){
        if(rentcar_index_time_text.getText().equals("")||rentcar_index_local_text.getText().equals("")||rentcar_index_type_text.getText().equals("")){
            Toast.makeText(getActivity(),"请先完善订单信息！",Toast.LENGTH_LONG).show();
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
            fragmentTransaction.replace(R.id.rentcar_panel, new RentCarCost_());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Click
    void page_return(){
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.in_left, R.anim.out_right);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        System.gc();
    }

    @Click
    void rentcar_index_local(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarProvince_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Click
    void rentcar_index_time(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final Dialog dialog = new Dialog(getActivity(), R.style.Translucent_NoTitle);
        View view = inflater.inflate(R.layout.centcar_index_datepicker, null);
        LinearLayout dataPickerPanel = (LinearLayout) view.findViewById(R.id.date_picker_panel);
        final DatePicker datePicker = new DatePicker(getActivity());
        TextView suerText = (TextView) view.findViewById(R.id.date_picker_sure);
        suerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth() + 1;
                    int day = datePicker.getDayOfMonth();

                    Calendar now = Calendar.getInstance();
                    int now_year = now.get(Calendar.YEAR);
                    int now_month = now.get(Calendar.MONTH) + 1;
                    int now_day = now.get(Calendar.DAY_OF_MONTH);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = simpleDateFormat .parse(year+"-"+month+"-"+day);
                    Date now_date = simpleDateFormat .parse(now_year+"-"+now_month+"-"+now_day);
                    long Stemp = date.getTime();
                    long now_Stemp = now_date.getTime();
                    if (Stemp >= now_Stemp) {
                        String str = year + " 年 " + month + " 月 " + day+ " 日";
                        rentcar_index_time_text.setText(str);
                        dialog.dismiss();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("rent_time", year + "-" + month + "-" + day);
                        editor.putString("rent_time_name", str);
                        editor.commit();
                    } else {
                        Toast.makeText(getActivity(), "请选择正确的租车日期！", Toast.LENGTH_LONG).show();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        dataPickerPanel.addView(datePicker);
        dialog.setContentView(view);
        dialog.show();
    }

    @Click
    void rentcar_index_type(){
        String address_name = null;
        address_name = sharedPreferences.getString("address_name",null);
        String address_id = null;
        address_id = sharedPreferences.getString("address_id",null);
        if(address_name != null){
            RentCarType rentCarArea = new RentCarType_();
            Bundle bundle = new Bundle();
            bundle.putString("address_id",address_id);
            rentCarArea.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
            fragmentTransaction.replace(R.id.rentcar_panel,rentCarArea);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else{
            Toast.makeText(getActivity(),"请先选择您的租车地址！",Toast.LENGTH_LONG).show();
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
                    getActivity().finish();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.commit();
                    System.gc();
                    return true;
                }
                return false;
            }
        });
    }
}
