package com.example.fullenergy.pub;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;


/**
 * Created by hasee on 2017/6/8.
 */
public class MyToast {

    public static void showTheToast(Context context ,String string){
        Toast toast = Toast.makeText(context,string,Toast.LENGTH_LONG);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}
