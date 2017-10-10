package com.xinyu.ElectricCabinet.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


//开机自启动广播接受
public class AutoStartBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {     // boot
            Intent intent2 = new Intent(context, NewControl.class);
//          intent2.setAction("android.intent.action.MAIN");
//          intent2.addCategory("android.intent.category.LAUNCHER");
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
    }

}