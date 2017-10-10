package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.pub.ProgressDialog;
import com.example.app.pub.PubFunction;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
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

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.login)
public class Login extends Activity {

    @ViewById
    EditText username;
    @ViewById
    EditText password;
    @ViewById
    TextView submit;

    private String usernameStr = "";
    private String passwordStr = "";

    private ProgressDialog progressDialog;
    private SharedPreferences Preferences;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        init();
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String cookies = "";
        cookies = Preferences.getString("api_userid", "");
        if(!cookies.equals("")){
            startActivity(new Intent(Login.this, Select_.class));
            activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
            Login.this.finish();
            if(progressDialog!=null){
                progressDialog.dismiss();// 隐藏loading框
            }
            System.gc();
        }
    }

    @Click
    void submit(){
        usernameStr = username.getText().toString().trim();
        passwordStr = password.getText().toString().trim();
        if(usernameStr.equals("")||passwordStr.equals("")){
            Toast.makeText(getApplicationContext(),"输入信息不能为空！",Toast.LENGTH_LONG).show();
            username.setText("");
            password.setText("");
        }else{
            httpLogin();
            progressDialog.show();
        }
    }

    @Background
    void httpLogin(){
        //接口地址
        String path = PubFunction.www + "adphone.php/login/login";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        // post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("mobile", usernameStr));
        params.add(new BasicNameValuePair("password", passwordStr));
        try {
            //写入post参数
            HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
            httpPost.setEntity(entity);
            //服务端
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            //返回成功
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //数据解析
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonParser = new JSONTokener(result);
                JSONObject person = (JSONObject) jsonParser.nextValue();
                String messageString = person.get("message").toString();
                String code = person.getString("code").toString();
                String PHPSESSID = null;
                String api_userid = null;
                String api_username = null;
                //写入cookies
                CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
                List<Cookie> cookies = mCookieStore.getCookies();
                SharedPreferences.Editor editor = Preferences.edit();
                for (int i = 0; i < cookies.size(); i++) {
                    if ("PHPSESSID".equals(cookies.get(i).getName())) {
                        PHPSESSID = cookies.get(i).getValue();
                        editor.putString("PHPSESSID", PHPSESSID);
                    }
                    if ("api_userid".equals(cookies.get(i).getName())) {
                        api_userid = cookies.get(i).getValue();
                        editor.putString("api_userid", api_userid);
                    }
                    if ("api_username".equals(cookies.get(i).getName())) {
                        api_username = cookies.get(i).getValue();
                        editor.putString("api_username", api_username);
                    }
                }
                editor.commit();
                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageString);
                    } else if (code.equals("100")) {
                        loginSuccess(messageString);
                    } else {
                        unknown();
                    }
                }
            }else{
                errorcode();
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown();
            }
        }
    }

    // 登陆时返回了错误信息，就在这里
    @UiThread
    void loginError(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    // 登陆时如果上传给服务器的信息正确 ，就是登陆成功 ，返回给这个handler，写入数据库，然后进行转跳页面
    @UiThread
    void loginSuccess(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("usrename", usernameStr);
        editor.putString("password", passwordStr);
        editor.commit();
        startActivity(new Intent(Login.this, Select_.class));
        activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
        Login.this.finish();
        progressDialog.dismiss();// 隐藏loading框
        System.gc();
    }
    // 返回的时候 不知道 出现什么错误的时候都会在这里 但一般的情况下 都是json解析出错了
    @UiThread
    void unknown(){
        Toast.makeText(getApplicationContext(), "登陆超时！请检查您的网络设置!", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    @UiThread
    void errorcode(){
        Toast.makeText(getApplicationContext(), "服务器错误！", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
}
