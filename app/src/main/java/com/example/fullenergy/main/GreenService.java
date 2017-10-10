package com.example.fullenergy.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Picasso;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2017/8/28.
 */

@EActivity(R.layout.green_service_2)
public class GreenService extends Activity {

    private Activity activity;

    @ViewById
    LinearLayout page_return, rent_car, rent_bar, return_cash_1, return_cash_2;

    @ViewById
    ImageView fuwui_img_1, fuwui_img_2;

    @ViewById
    TextView fuwu_text_1, fuwu_text_2;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    String bar_price = "";
    String car_price = "";


    @Override
    protected void onResume() {
        super.onResume();
        if (PubFunction.isNetworkAvailable(this)) {
            progressDialog.show();
            HttpPanelServiceIndex();
        } else {
            Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        activity = this;
        init();
    }

    private void init() {

        progressDialog = new ProgressDialog(this);
        preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void fuwui_img_1() {
        startActivity(new Intent(this, RentBar_.class));
    }

    @Click
    void fuwui_img_2() {
        startActivity(new Intent(this, RentCar_.class));
    }

    @Click
    void return_cash_1() {
        HttpMsg("1");
    }

    @Click
    void return_cash_2() {
        HttpMsg("2");
    }


    @UiThread
    void panelServiceIndexSuccess(String data) {
        try {
            progressDialog.dismiss();
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();

            JSONObject jsonObject_1 = jsonArray.getJSONObject(0);
            String type_1 = jsonObject_1.getString("type");
            if (type_1.equals("1")) {
                String img_1 = jsonObject_1.getString("img");
                Picasso.with(this).load(img_1).into(fuwui_img_1);
                String cash = jsonObject_1.getString("cash");
                bar_price = cash;
                fuwu_text_1.setText("电池押金" + cash + "元，退押金");
            }
            JSONObject jsonObject_2 = jsonArray.getJSONObject(1);
            String type_2 = jsonObject_2.getString("type");
            if (type_2.equals("2")) {
                String img_2 = jsonObject_2.getString("img");
                Picasso.with(this).load(img_2).into(fuwui_img_2);
                String cash_2 = jsonObject_2.getString("cash");
                car_price = cash_2;
                fuwu_text_2.setText("车押金" + cash_2 + "元，退押金");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @UiThread
    void panelReturnCashSuccess(String data, String str) {
        MyToast.showTheToast(activity,str);
        progressDialog.dismiss();
    }


    @UiThread
    void panelServiceIndexError(String message) {
        MyToast.showTheToast(activity,message);
        progressDialog.dismiss();
    }

    @UiThread
    void turnToLogin() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usrename", null);
        editor.putString("password", null);
        editor.putString("jy_password", null);
        editor.putString("PHPSESSID", null);
        editor.putString("api_userid", null);
        editor.putString("api_username", null);
        editor.commit();
        Intent intent = new Intent(this, Login.class);
        intent.putExtra("type", "1");
        this.startActivity(intent);
        this.finish();
        this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Background
    void HttpPanelServiceIndex() {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/Service/service";

        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("0")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        panelServiceIndexSuccess(data);
                    }
                } else {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        panelServiceIndexError(messageStr);
                    }
                }
            } else {
                panelServiceIndexError("服务器错误");
            }
        } catch (Exception e) {
            panelServiceIndexError("json解析失败！");
        }
    }

    @Background
    void HttpReturnCash(String type) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/Service/refund_cash";

        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("type", type));
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

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("0")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        panelReturnCashSuccess(data, messageStr);
                    }
                } else {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        panelServiceIndexError(messageStr);
                    }
                }
            } else {
                panelServiceIndexError("服务器错误");
            }
        } catch (Exception e) {
            panelServiceIndexError("json解析失败！");
        }
    }

    @UiThread
    void panelReturnMessage(String message) {
        LayoutInflater inflater = LayoutInflater.from(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog mAlertDialog = builder.create();
        View view = inflater.inflate(R.layout.green_alertdialog, null);

        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
        title.setText("退还车押金");

        TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogContent);
        titleSmall.setText(message);

        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
        success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                HttpReturnCash("2");
                mAlertDialog.dismiss();
            }
        });
        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
    }


    @Background
    void HttpMsg(String type) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/Service/msg";

        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("type", type));
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

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("0")) {
                    panelReturnMessage(messageStr);
                } else {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        panelServiceIndexError(messageStr);
                    }
                }
            } else {
                panelServiceIndexError("服务器错误");
            }
        } catch (Exception e) {
            panelServiceIndexError("json解析失败！");
        }
    }

}
