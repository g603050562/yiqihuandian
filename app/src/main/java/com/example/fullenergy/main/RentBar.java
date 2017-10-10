package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.scanCode.CaptureActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2017/8/29.
 */

@EActivity(R.layout.rentbar)
public class RentBar extends Activity {

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView button;

    private Handler showDialogHandler;

    private Activity activity;

    private String bus_id = "";

    @AfterViews
    void afterviews(){
        init();
        handler();
    }

    private void handler() {
        showDialogHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressDialog.show();
            }
        };
    }

    private void init() {

        activity = this;
        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
    }

    @Click
    void page_return(){
        this.finish();
    }
    @Click
    void button(){
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, 0x0001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传

        if (requestCode == 0x0001 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                bus_id = content;
                httpGetOrderInfo(content);
            }
        }
    }

    @UiThread
    void renturnError(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnSuccess(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void turnToLogin() {
        SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usrename", null);
        editor.putString("password", null);
        editor.putString("jy_password", null);
        editor.putString("PHPSESSID", null);
        editor.putString("api_userid", null);
        editor.putString("api_username", null);
        editor.commit();
        Intent intent = new Intent(activity, Login_.class);
        intent.putExtra("type", "1");
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
        progressDialog.dismiss();
    }

    @UiThread
    void renturnHttpGetOrderInfo(String string , String data){

        Intent intent = new Intent(activity , RentBarOrder_.class);
        intent.putExtra("data",data.toString());
        intent.putExtra("bus_id",bus_id);
        startActivity(intent);
        progressDialog.dismiss();
    }


    @Background
    void httpGetOrderInfo(String bus_id) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www+"index.php/Business/info";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("business_id", bus_id));
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String messageString = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("0")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        renturnHttpGetOrderInfo(messageString, data);
                    } else {
                        returnSuccess(messageString);
                    }
                } else if (code.equals("200")) {
                    if (messageString.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageString);
                    }
                } else {
                    renturnError(messageString);
                }
            } else {
                renturnError("服务器错误：httpGetOrderInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpGetOrderInfo");
        }
    }

}
