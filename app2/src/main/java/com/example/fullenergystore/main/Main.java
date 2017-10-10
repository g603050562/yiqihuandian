package com.example.fullenergystore.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.StatusBar.statusBar;
import com.example.fullenergystore.pub.CreateFile;
import com.example.fullenergystore.pub.JPush.ExampleUtil;
import com.example.fullenergystore.pub.PubFunction;

import cn.jpush.android.api.JPushInterface;

public class Main extends Activity {

    private Thread th;
    private Activity activity;
    private TextView mainVersionCode;
    private String userNameString, passWordString;
    private SharedPreferences Preferences;
    public static boolean isForeground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;
        init();
    }

    private void init() {
        //沉浸式,创建文件夹
        new statusBar(activity);
        new CreateFile(getApplicationContext());
        //jpush接受
        registerMessageReceiver();
        //版本号
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mainVersionCode = (TextView) this.findViewById(R.id.mainVersionCode);
        mainVersionCode.setText("Version : " + info.versionName);
        //默认登陆
        Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        userNameString = Preferences.getString("usrename", null);
        passWordString = Preferences.getString("password", null);
        th = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (userNameString != null && passWordString != null && PubFunction.isNetworkAvailable(Main.this)) {
                    String path = PubFunction.www + "api_business.php/login/login";
                    HttpPost httpPost = new HttpPost(path);
                    httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
                    httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("mobile", userNameString));
                    params.add(new BasicNameValuePair("password", passWordString));
                    try {
                        HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
                        httpPost.setEntity(entity);
                        DefaultHttpClient client = new DefaultHttpClient();
                        HttpResponse httpResponse = client.execute(httpPost);
                        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String result = EntityUtils.toString(httpResponse.getEntity());
                            JSONTokener jsonParser = new JSONTokener(result);
                            JSONObject person = (JSONObject) jsonParser.nextValue();

                            String PHPSESSID = null;
                            String apibus_businessid = null;
                            String apibus_username = null;
                            CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
                            List<Cookie> cookies = mCookieStore.getCookies();
                            SharedPreferences.Editor editor = Preferences.edit();
                            for (int i = 0; i < cookies.size(); i++) {
                                if ("PHPSESSID".equals(cookies.get(i).getName())) {
                                    PHPSESSID = cookies.get(i).getValue();
                                    editor.putString("PHPSESSID", PHPSESSID);
                                }
                                if ("apibus_businessid".equals(cookies.get(i).getName())) {
                                    apibus_businessid = cookies.get(i).getValue();
                                    editor.putString("apibus_businessid", apibus_businessid);
                                }
                                if ("apibus_username".equals(cookies.get(i).getName())) {
                                    apibus_username = cookies.get(i).getValue();
                                    editor.putString("apibus_username", apibus_username);
                                }
                            }
                            editor.commit();

                            if (person.get("code").toString().equals("200")) {
                                startActivity(new Intent(getApplicationContext(), Login.class));
                                overridePendingTransition(R.anim.in_right, R.anim.out_left);
                                activity.finish();
                            } else {
                                startActivity(new Intent(getApplicationContext(), Panel.class));
                                overridePendingTransition(R.anim.in_right, R.anim.out_left);
                                activity.finish();
                            }
                        }
                    } catch (Exception e) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        overridePendingTransition(R.anim.in_right, R.anim.out_left);
                        activity.finish();
                    }

                } else {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                    activity.finish();
                }
            }

        };
        th.start();
    }

    //jpush
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
            }
        }
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        isForeground = false;
        JPushInterface.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isForeground = true;
        JPushInterface.onResume(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}

