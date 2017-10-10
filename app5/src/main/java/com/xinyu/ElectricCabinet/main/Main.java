package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.Unit;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;

/**
 * Created by hasee on 2016/12/28.
 */
public class Main extends Activity implements View.OnClickListener{

    private int iDeviceCount = 0;
    PL2303MultiLib mSerialMulti;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";
    private Button button,button_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyApplication.getInstance().addActivity(this);
        setContentView(R.layout.main);

        if(iDeviceCount == 0){
            mSerialMulti = new PL2303MultiLib((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);
            iDeviceCount = mSerialMulti.PL2303Enumerate();
        }

        button_1 = (Button) this.findViewById(R.id.button_1);
        button_1.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if(button_1.getId() == v.getId()){
            startActivity(new Intent(this,Exmple.class));
        }
    }


}
