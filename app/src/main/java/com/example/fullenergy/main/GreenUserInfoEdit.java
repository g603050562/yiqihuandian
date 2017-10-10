package com.example.fullenergy.main;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@EActivity(R.layout.green_userinfo_edit)
public class GreenUserInfoEdit extends Activity {

    private Activity activity;

    @ViewById
    LinearLayout return_page, set_head_panel;
    @ViewById
    CircleImageView user_head;
    @ViewById
    EditText nickname, name, address;
    @ViewById
    TextView submit;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;


    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        httpGetUserInfo();
        progressDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @AfterViews
    void afterViews() {
        init();
    }

    private void init() {
        activity = this;
        progressDialog = new ProgressDialog(activity);

        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);

        String head_img_url = getIntent().getStringExtra("url");
        Picasso.with(activity).load(PubFunction.www + head_img_url).into(user_head);
    }

    @Click
    void return_page() {
        this.finish();
    }

    @Click
    void set_head_panel() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Click
    void submit() {
        HttpSubmitInfo(nickname.getText().toString(),name.getText().toString(),address.getText().toString());
        progressDialog.show();
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
    void returnHttpUpLoadImgSuccess(String str,Bitmap bitmap){
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
        user_head.setImageBitmap(bitmap);
    }


    @Background
    void httpUpLoadImg(Bitmap bitmap) {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/set_avater_and";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("avater", bitmaptoString(bitmap)));
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

                if (code.equals("100")) {
                    returnHttpUpLoadImgSuccess(messageString,bitmap);
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
                renturnError("服务器错误：httpPanelMineIndexTransactionPasswordSubmit");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpPanelMineIndexTransactionPasswordSubmit");
        }

    }


    public String bitmaptoString(Bitmap bitmap) {
        Bitmap smallBitmap = PubFunction.small(bitmap);
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        smallBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
        byte[] bytes = bStream.toByteArray();
        String string = Base64.encodeToString(bytes, Base64.DEFAULT);
        bytes = new byte[]{};
        return string;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            httpUpLoadImg(bitmap);
        }

    }

    @UiThread
    void renturnHttpGetUserInfo(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            String nickname_str = jsonObject.getString("nickname");
            nickname.setText(nickname_str);

            String name_str = jsonObject.getString("realname");
            name.setText(name_str);

            String address_str = jsonObject.getString("address");
            address.setText(address_str);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }


    @Background
    void httpGetUserInfo() {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/set_my_name";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
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

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        renturnHttpGetUserInfo(messageString, data);
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
                renturnError("服务器错误：httpGetUserInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpGetUserInfo");
        }
    }

    @UiThread
    void returnHttpSubmitInfoSuccess(String str){
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
        this.finish();
    }


    @Background
    void HttpSubmitInfo(String nickName , String name , String address) {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/set_my_name";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("nickname", nickName));
        list.add(new BasicNameValuePair("name", name));
        list.add(new BasicNameValuePair("address", address));
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject.toString());

                String messageString = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageString);
                message.setData(bundle);

                if (code.equals("100")) {
                        returnHttpSubmitInfoSuccess(messageString);
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
                renturnError("服务器错误：HttpSubmitInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpSubmitInfo");
        }
    }
}
