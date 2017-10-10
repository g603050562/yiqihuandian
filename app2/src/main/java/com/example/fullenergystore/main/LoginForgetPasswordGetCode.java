package com.example.fullenergystore.main;

import java.util.ArrayList;
import java.util.List;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.StatusBar.statusBar;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;

public class LoginForgetPasswordGetCode extends Activity implements OnClickListener {

    private TextView returnLogin;
    private InputMethodManager manager = null;
    private LinearLayout loginForgetPasswordGetCodeFocus;
    private Button loginForgetPasswordGetCodeSubmit, loginForgetPasswordGetCodeGetCode;

    private EditText loginForgetPasswordGetCodeInputCode, loginForgetPasswordGetCodePhone;
    public static Handler loginForgetPasswordGetCodeGetCodeSuccessHandler, loginForgetPasswordGetCodeGetCodeErrorHandler, loginForgetPasswordGetCodeVerifySuccessHandler, loginForgetPasswordGetCodeVerifyErrorHandler, unknownHandler;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_forget_password_get_code);
        init();
        handler();
    }

    private void init() {

        new statusBar(this);
        progressDialog = new ProgressDialog(this);

        returnLogin = (TextView) this.findViewById(R.id.returnLogin);
        returnLogin.setOnClickListener(this);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        loginForgetPasswordGetCodeFocus = (LinearLayout) this.findViewById(R.id.loginForgetPasswordGetCodeFocus);
        loginForgetPasswordGetCodePhone = (EditText) this.findViewById(R.id.loginForgetPasswordGetCodePhone);
        loginForgetPasswordGetCodeGetCode = (Button) this.findViewById(R.id.loginForgetPasswordGetCodeGetCode);
        loginForgetPasswordGetCodeGetCode.setOnClickListener(this);
        loginForgetPasswordGetCodeInputCode = (EditText) this.findViewById(R.id.loginForgetPasswordGetCodeInputCode);
        loginForgetPasswordGetCodeSubmit = (Button) this.findViewById(R.id.loginForgetPasswordGetCodeSubmit);
        loginForgetPasswordGetCodeSubmit.setOnClickListener(this);
    }

    private void handler() {
        loginForgetPasswordGetCodeGetCodeSuccessHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = msg.getData().getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };
        loginForgetPasswordGetCodeGetCodeErrorHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = msg.getData().getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };
        loginForgetPasswordGetCodeVerifyErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = msg.getData().getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };
        loginForgetPasswordGetCodeVerifySuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = msg.getData().getString("message");
                String PHPSESSID = msg.getData().getString("PHPSESSID");
                String mobileCookies = msg.getData().getString("mobileCookies");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginForgetPasswordGetCode.this, LoginForgetPassword.class);
                intent.putExtra("phone", loginForgetPasswordGetCodePhone.getText().toString().trim());
                intent.putExtra("PHPSESSID", PHPSESSID);
                intent.putExtra("mobileCookies", mobileCookies);
                LoginForgetPasswordGetCode.this.startActivity(intent);
                LoginForgetPasswordGetCode.this.finish();
                LoginForgetPasswordGetCode.this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
                progressDialog.dismiss();
            }
        };
        unknownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = msg.getData().getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };
    }


    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        if (returnLogin.getId() == arg0.getId()) {
            startActivity(new Intent(this, Login.class));
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        } else if (loginForgetPasswordGetCodeGetCode.getId() == arg0.getId()) {
            String phone = loginForgetPasswordGetCodePhone.getText().toString();
            if (phone.equals("")) {
                Toast.makeText(getApplicationContext(), "请填写您的手机号", Toast.LENGTH_SHORT).show();
                loginForgetPasswordGetCodeFocus.requestFocus();
            } else {
                if (PubFunction.isNetworkAvailable(this)) {
                    LoginForgetPasswordGetCodeGetCode th = new LoginForgetPasswordGetCodeGetCode(phone, this);
                    th.start();
                    progressDialog.show();
                } else {
                    Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (loginForgetPasswordGetCodeSubmit.getId() == arg0.getId()) {
            String code = loginForgetPasswordGetCodeInputCode.getText().toString();
            if (code.equals("")) {
                Toast.makeText(getApplicationContext(), "请填写您的验证码", Toast.LENGTH_SHORT).show();
                loginForgetPasswordGetCodeFocus.requestFocus();
            } else {
                if (PubFunction.isNetworkAvailable(this)) {
                    LoginForgaetPasswordGetCodeVerify th = new LoginForgaetPasswordGetCodeVerify(loginForgetPasswordGetCodePhone.getText().toString().trim(), loginForgetPasswordGetCodeInputCode.getText().toString().trim(), this);
                    th.start();
                    progressDialog.show();
                } else {
                    Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                loginForgetPasswordGetCodeFocus.requestFocus();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            startActivity(new Intent(this, Login.class));
            this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
        return super.onKeyDown(keyCode, event);
    }
}

class LoginForgetPasswordGetCodeGetCode extends Thread {

    String mobile = null;
    private Activity activity;

    public LoginForgetPasswordGetCodeGetCode(String mobile, Activity activity) {
        this.mobile = mobile;
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();
        String path = PubFunction.www + "api.php/login/send_sms";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("mobile", this.mobile));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonToken = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonToken.nextValue();

                System.out.println(jsonObject.toString());

                String code = jsonObject.getString("code");
                String messageString = jsonObject.getString("message");
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageString);
                message.setData(bundle);

                if (activity != null) {
                    if (code.equals("200")) {
                        LoginForgetPasswordGetCode.loginForgetPasswordGetCodeGetCodeErrorHandler.sendMessage(message);
                    } else if (code.equals("100")) {
                        LoginForgetPasswordGetCode.loginForgetPasswordGetCodeGetCodeSuccessHandler.sendMessage(message);
                    } else {
                        LoginForgetPasswordGetCode.unknownHandler.sendMessage(new Message());
                    }
                }

            }
        } catch (Exception e) {
            if (activity != null) {
                System.out.println(e.toString());
                LoginForgetPasswordGetCode.unknownHandler.sendMessage(new Message());
            }
        }
    }
}

class LoginForgaetPasswordGetCodeVerify extends Thread {

    private String mobile = null;
    private String mobileVerify = null;
    private Activity activity;

    public LoginForgaetPasswordGetCodeVerify(String mobile, String mobileVerify, Activity activity) {
        this.mobile = mobile;
        this.mobileVerify = mobileVerify;
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();
        String path = PubFunction.www + "api_business.php/login/mobile_yzm_users";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("mobile", mobile));
        list.add(new BasicNameValuePair("mobile_verify", mobileVerify));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                System.out.println(result.toString());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                String messageString = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (activity != null) {
                    if (code.equals("200")) {
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("message", messageString);
                        message.setData(bundle);
                        LoginForgetPasswordGetCode.loginForgetPasswordGetCodeVerifyErrorHandler.sendMessage(message);
                    } else if (code.equals("100")) {
                        String PHPSESSID = null;
                        String mobileCookies = null;
                        CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
                        List<Cookie> cookies = mCookieStore.getCookies();

                        for (int i = 0; i < cookies.size(); i++) {
                            if ("PHPSESSID".equals(cookies.get(i).getName())) {
                                PHPSESSID = cookies.get(i).getValue();
                            }
                            if ("cd_mobile".equals(cookies.get(i).getName())) {
                                mobileCookies = cookies.get(i).getValue();
                            }
                        }

                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("message", messageString);
                        bundle.putString("PHPSESSID", PHPSESSID);
                        bundle.putString("mobileCookies", mobileCookies);
                        message.setData(bundle);
                        LoginForgetPasswordGetCode.loginForgetPasswordGetCodeVerifySuccessHandler.sendMessage(message);
                    } else {
                        LoginForgetPasswordGetCode.unknownHandler.sendMessage(new Message());
                    }
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                System.out.println(e.toString());
                LoginForgetPasswordGetCode.unknownHandler.sendMessage(new Message());
            }
        }
    }
}