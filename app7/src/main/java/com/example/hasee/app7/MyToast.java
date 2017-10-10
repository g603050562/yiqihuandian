package com.example.hasee.app7;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hasee on 2017/6/8.
 */
public class MyToast {

    private AlertDialog.Builder builder;
    private AlertDialog mAlertDialog;

    private Handler handler;

    public void showTheToast(Context context ,String string){
//        Toast toast = Toast.makeText(context,string,Toast.LENGTH_LONG);
//        View view = LayoutInflater.from(context).inflate(R.layout.toast_panel, null);
//        TextView textView = (TextView) view.findViewById(R.id.text_1);
//        textView.setText(string);
//        toast.setView(view);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mAlertDialog.dismiss();
            }
        };


        LayoutInflater inflater = LayoutInflater.from(context);
        builder = new AlertDialog.Builder(context);
        mAlertDialog = builder.create();
        View view = inflater.inflate(R.layout.toast_panel, null);
        TextView titleSmall = (TextView) view.findViewById(R.id.text_1);
        titleSmall.setText(string);
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
               handler.sendMessage(new Message());
            }
        }, 4000);// 设定指定的时间time,此处为2000毫秒
    }


}
