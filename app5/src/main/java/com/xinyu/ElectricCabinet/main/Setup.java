package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.Unit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;

/**
 * Created by hasee on 2016/12/28.
 */
public class Setup extends Activity implements View.OnClickListener{

    private int iDeviceCount = 0;
    private static Boolean isExit = false;
    PL2303MultiLib mSerialMulti;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";

    private EditText editText,cabinet_password,cabinet_number;
    private TextView textView;
    private LinearLayout page_return;

    private SharedPreferences sharedPreferences;
    private String catinetNumberString = "";

    private RelativeLayout item_1,item_2,item_3,item_4,item_5,item_6,item_7,item_8,item_9,item_10,item_11,item_12,item_13,item_14,item_15;
    private TextView item_1_id,item_2_id,item_3_id,item_4_id,item_5_id,item_6_id,item_7_id,item_8_id,item_9_id,item_10_id,item_11_id,item_12_id,item_13_id,item_14_id,item_15_id;
    private TextView[] idTextViews;
    private RelativeLayout[] textViews ;

    private LinearLayout finish;
    private TextView open_all;
    private ImageView wifi_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
        setContentView(R.layout.setup);
        init();
        main();
    }

    private void main() {
        String[] barID = Unit.barId;
        if(barID != null){
            for(int i = 0 ; i < barID.length ; i++){
                if(barID[i] != null){
                    int j = Unit.barSta[i];
                    String str = "";
                    if(j == 0){
                        str = "空仓";
                    }else if(j == 1){
                        str = "满电";
                    }else if(j == 2){
                        str = "充电";
                    }else if(j == 3){
                        str = "异常";
                    }
                    if(barID[i].equals("000000000000")||barID[i].equals("0000000000000000")){
                        idTextViews[i].setText(str+"   没有电池在此箱柜里！");
                    }else{

                        idTextViews[i].setText(str+"   ID : " + barID[i]);
                    }
                }
            }
        }
    }

    private void init() {

        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
        catinetNumberString = sharedPreferences.getString("cabinetNumber", "");

        mSerialMulti = new PL2303MultiLib((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);
        if (catinetNumberString.equals("")) {
            iDeviceCount = mSerialMulti.PL2303Enumerate();
        }

        editText = (EditText) this.findViewById(R.id.cabinet_name);
        editText.setText(catinetNumberString.toString());

        textView = (TextView) this.findViewById(R.id.submit);
        textView.setOnClickListener(this);
        cabinet_number = (EditText) this.findViewById(R.id.cabinet_number);
        cabinet_password = (EditText) this.findViewById(R.id.cabinet_password);
        page_return = (LinearLayout) this.findViewById(R.id.page_return);
        page_return.setOnClickListener(this);

        finish = (LinearLayout) this.findViewById(R.id.finish);
        finish.setOnClickListener(this);

        item_1 = (RelativeLayout) this.findViewById(R.id.item_1);
        item_2 = (RelativeLayout) this.findViewById(R.id.item_2);
        item_3 = (RelativeLayout) this.findViewById(R.id.item_3);
        item_4 = (RelativeLayout) this.findViewById(R.id.item_4);
        item_5 = (RelativeLayout) this.findViewById(R.id.item_5);
        item_6 = (RelativeLayout) this.findViewById(R.id.item_6);
        item_7 = (RelativeLayout) this.findViewById(R.id.item_7);
        item_8 = (RelativeLayout) this.findViewById(R.id.item_8);
        item_9 = (RelativeLayout) this.findViewById(R.id.item_9);
        item_10 = (RelativeLayout) this.findViewById(R.id.item_10);
        item_11 = (RelativeLayout) this.findViewById(R.id.item_11);
        item_12 = (RelativeLayout) this.findViewById(R.id.item_12);
        item_13 = (RelativeLayout) this.findViewById(R.id.item_13);
        item_14 = (RelativeLayout) this.findViewById(R.id.item_14);
        item_15 = (RelativeLayout) this.findViewById(R.id.item_15);

        item_1.setOnClickListener(this);
        item_2.setOnClickListener(this);
        item_3.setOnClickListener(this);
        item_4.setOnClickListener(this);
        item_5.setOnClickListener(this);
        item_6.setOnClickListener(this);
        item_7.setOnClickListener(this);
        item_8.setOnClickListener(this);
        item_9.setOnClickListener(this);
        item_10.setOnClickListener(this);
        item_11.setOnClickListener(this);
        item_12.setOnClickListener(this);
        item_13.setOnClickListener(this);
        item_14.setOnClickListener(this);
        item_15.setOnClickListener(this);

        textViews = new RelativeLayout[]{item_1,item_2,item_3,item_4,item_5,item_6,item_7,item_8,item_9,item_10,item_11,item_12,item_13,item_14,item_15};

        item_1_id = (TextView) this.findViewById(R.id.item_1_id);
        item_2_id = (TextView) this.findViewById(R.id.item_2_id);
        item_3_id = (TextView) this.findViewById(R.id.item_3_id);
        item_4_id = (TextView) this.findViewById(R.id.item_4_id);
        item_5_id = (TextView) this.findViewById(R.id.item_5_id);
        item_6_id = (TextView) this.findViewById(R.id.item_6_id);
        item_7_id = (TextView) this.findViewById(R.id.item_7_id);
        item_8_id = (TextView) this.findViewById(R.id.item_8_id);
        item_9_id = (TextView) this.findViewById(R.id.item_9_id);
        item_10_id = (TextView) this.findViewById(R.id.item_10_id);
        item_11_id = (TextView) this.findViewById(R.id.item_11_id);
        item_12_id = (TextView) this.findViewById(R.id.item_12_id);
        item_13_id = (TextView) this.findViewById(R.id.item_13_id);
        item_14_id = (TextView) this.findViewById(R.id.item_14_id);
        item_15_id = (TextView) this.findViewById(R.id.item_15_id);

        idTextViews = new TextView[]{item_1_id,item_2_id,item_3_id,item_4_id,item_5_id,item_6_id,item_7_id,item_8_id,item_9_id,item_10_id,item_11_id,item_12_id,item_13_id,item_14_id,item_15_id};

        open_all = (TextView) this.findViewById(R.id.open_all);
        open_all.setOnClickListener(this);

        wifi_img = (ImageView) this.findViewById(R.id.wifi_image);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() ==  textView.getId()){
            String number = cabinet_number.getText().toString();
            String password = cabinet_password.getText().toString();

            if(number.equals("18611992352") && password.equals("zyp0111051001732")){
                catinetNumberString = editText.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cabinetNumber", catinetNumberString);
                editor.commit();
                startActivity(new Intent(this,Exmple.class));
                this.finish();
            }else{
                showTheToast("用户名密码错误！请与服务商联系！");
            }
        }else if(v.getId() == page_return.getId()){
            this.finish();
        }else if(open_all.getId() == v.getId()){
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("item",Integer.toString(0));
            message.setData(bundle);
            Exmple.adminOpenDoorHandler.sendMessage(message);
        }else if(finish.getId() == v.getId()){
            Timer tExit = null;
            if (isExit == false) {
                isExit = true; // 准备退出
                showTheToast("再按一次退出应用");
                tExit = new Timer();
                tExit.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isExit = false;// 取消退出
                    }
                }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
            } else {
                MyApplication.getInstance().exit();
            }
        }else{
            for(int i = 0 ; i < textViews.length ; i++){
                if(textViews[i].getId() == v.getId()){
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("item",Integer.toString(i+1));
                    message.setData(bundle);
                    Exmple.adminOpenDoorHandler.sendMessage(message);
                }
            }
        }
    }

    private void showTheToast(String string){
        Toast toast = Toast.makeText(this,string,Toast.LENGTH_LONG);
        View view = LayoutInflater.from(this).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }



}