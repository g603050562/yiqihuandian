package com.example.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.select)
public class Select extends Activity{

    @ViewById
    TextView button_1;
    @ViewById
    TextView button_2;
    @ViewById
    TextView button_3;

    private SharedPreferences Preferences;
    private String userName = null;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        userName = Preferences.getString("usrename", null);
        if(userName == null || !userName.equals("18611992352")) {
            button_2.setBackgroundResource(R.drawable.button_corners_gay_radius_0);
        }
        activity = this;
    }

    @Click
    void button_1(){
        startActivity(new Intent(getApplicationContext(), Main_.class));
    }

    @Click
    void button_2(){
        if(userName == null || !userName.equals("18611992352")){
            Toast.makeText(getApplicationContext(),"您的账户尚未拥有此权限！",Toast.LENGTH_LONG).show();
        }else{
            startActivity(new Intent(getApplicationContext(), Version_.class));
        }

    }
    @Click
    void button_3(){
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        title.setText("是否要退出此用户？");

        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
        success.setText("确定");
        success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SharedPreferences.Editor editor = Preferences.edit();
                editor.putString("usrename", null);
                editor.putString("password", null);
                editor.putString("jy_password", null);
                editor.putString("PHPSESSID", null);
                editor.putString("api_userid", null);
                editor.putString("api_username", null);
                editor.commit();
                startActivity(new Intent(activity, Login_.class));
                activity.finish();
            }
        });
        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
        error.setText("取消");
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
    }
}
