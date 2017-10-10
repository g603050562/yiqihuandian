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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.version)
public class Version extends Activity{

    @ViewById
    TextView android_now_version;
    @ViewById
    TextView ios_now_version;

    @ViewById
    EditText android_input_version;
    @ViewById
    EditText ios_input_version;

    @ViewById
    TextView android_submit;
    @ViewById
    TextView ios_submit;

    private ProgressDialog progressDialog;
    private SharedPreferences Preferences;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        main();
    }

    private void main() {
        progressDialog.show();
        getNowVersion();
    }

    private void init() {
        activity = this;
        Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);

    }

    @Click
    void android_submit(){
        String submit_version = "";
        submit_version = android_input_version.getText().toString().trim();
        if(submit_version.equals("")){
            Toast.makeText(getApplicationContext(),"提交的信息不能为空！",Toast.LENGTH_LONG).show();
        }else {
            and_version_Submit(submit_version);
            progressDialog.show();
        }
    }

    @Click
    void ios_submit(){
        String submit_version = "";
        submit_version = ios_input_version.getText().toString().trim();
        if(submit_version.equals("")){
            Toast.makeText(getApplicationContext(),"提交的信息不能为空！",Toast.LENGTH_LONG).show();
        }else {
            ios_version_Submit(submit_version);
            progressDialog.show();
        }
    }

    @Background
    void getNowVersion(){

        //接口地址
        String path = PubFunction.www + "adphone.php/version/index";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            //服务端
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            //返回成功
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //数据解析
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonParser = new JSONTokener(result);
                JSONObject person = (JSONObject) jsonParser.nextValue();

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        JSONObject jsonObject = person.getJSONObject("data");
                        getSuccess(jsonObject);
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    // 登陆时如果上传给服务器的信息正确 ，就是登陆成功 ，返回给这个handler，写入数据库，然后进行转跳页面
    @UiThread
    void getSuccess(JSONObject msg){
        try {
            String and_version = msg.getString("and_version");
            String and_vname = msg.getString("and_vname");
            String ios_version = msg.getString("ios_version");
            String ios_vname = msg.getString("ios_vname");
            android_now_version.setText(and_vname);
            ios_now_version.setText(ios_vname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    // 登陆时返回了错误信息，就在这里
    @UiThread
    void loginError(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    // 返回的时候 不知道 出现什么错误的时候都会在这里 但一般的情况下 都是json解析出错了
    @UiThread
    void unknown(String e){
        Toast.makeText(getApplicationContext(),e, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    @UiThread
    void errorcode(int str){
        Toast.makeText(getApplicationContext(), "服务器错误！"+ str, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }


    @Background
    void and_version_Submit(String version){

        //接口地址
        String path = PubFunction.www + "adphone.php/version/and_version";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String b =version.replace(".","");
        params.add(new BasicNameValuePair("and_version", b));

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

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        andUpVersionSuccess(version);
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @Background
    void and_name_Submit(String version){

        //接口地址
        String path = PubFunction.www + "adphone.php/version/and_vname";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("and_vname", version));

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

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        andUpNameSuccess();
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @UiThread
    void andUpVersionSuccess(String version){
        and_name_Submit(version);
    }

    @UiThread
    void andUpNameSuccess(){
        Toast.makeText(getApplicationContext(),"上传成功!",Toast.LENGTH_LONG).show();
        android_input_version.setText("");
        getNowVersion();
    }


    @Background
    void ios_version_Submit(String version){

        //接口地址
        String path = PubFunction.www + "adphone.php/version/ios_version";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String b =version.replace(".","");
        params.add(new BasicNameValuePair("ios_version", b));

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

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        iosUpVersionSuccess(version);
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @Background
    void ios_name_Submit(String version){

        //接口地址
        String path = PubFunction.www + "adphone.php/version/ios_vname";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ios_vname", version));

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

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        iosUpNameSuccess();
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @UiThread
    void iosUpVersionSuccess(String version){
        ios_name_Submit(version);
    }

    @UiThread
    void iosUpNameSuccess(){
        Toast.makeText(getApplicationContext(),"上传成功!",Toast.LENGTH_LONG).show();
        ios_input_version.setText("");
        getNowVersion();
    }

}
