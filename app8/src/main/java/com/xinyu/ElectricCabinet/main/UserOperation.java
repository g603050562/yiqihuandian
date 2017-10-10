package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.ProgressDialog;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hasee on 2017/3/6.
 */
public class UserOperation extends Fragment implements View.OnClickListener{

    private View view;
    private TextView number_1,number_2,number_3,number_4,number_5,number_6,number_7,number_8,number_9,number_0,number_clear,submit;
    private EditText username,password;
    private String username_string = "",password_string = "";
    private LinearLayout setup,number_back,username_cover,password_cover;

    private int setup_count = 0;
    private SharedPreferences sharedPreferences;
    private String catinetNumberString = "";

    private HttpLogin httpLogin;
    private HttpAllowLogin httpAllowLogin;

    private Handler returnErrorHandler,returnSuccessInfoHandler,allowLoginSuccessHandler;
    private ProgressDialog progressDialog;

    private int submit_state = 1;
    public static Handler getFocusHandler;

    private ImageView Qcode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_operation, container, false);
        init();
        main();
        handler();
        return view;
    }

    private void init() {
        progressDialog = new ProgressDialog(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
        catinetNumberString = sharedPreferences.getString("cabinetNumber", "");

        number_1 = (TextView) view.findViewById(R.id.number_1);
        number_2 = (TextView) view.findViewById(R.id.number_2);
        number_3 = (TextView) view.findViewById(R.id.number_3);
        number_4 = (TextView) view.findViewById(R.id.number_4);
        number_5 = (TextView) view.findViewById(R.id.number_5);
        number_6 = (TextView) view.findViewById(R.id.number_6);
        number_7 = (TextView) view.findViewById(R.id.number_7);
        number_8 = (TextView) view.findViewById(R.id.number_8);
        number_9 = (TextView) view.findViewById(R.id.number_9);
        number_0 = (TextView) view.findViewById(R.id.number_0);
        number_back = (LinearLayout) view.findViewById(R.id.number_back);
        number_clear = (TextView) view.findViewById(R.id.number_clear);
        number_1.setOnClickListener(this);
        number_2.setOnClickListener(this);
        number_3.setOnClickListener(this);
        number_4.setOnClickListener(this);
        number_5.setOnClickListener(this);
        number_6.setOnClickListener(this);
        number_7.setOnClickListener(this);
        number_8.setOnClickListener(this);
        number_9.setOnClickListener(this);
        number_0.setOnClickListener(this);
        number_back.setOnClickListener(this);
        number_clear.setOnClickListener(this);

        username_cover = (LinearLayout) view.findViewById(R.id.username_cover);
        username_cover.setOnClickListener(this);
        password_cover = (LinearLayout) view.findViewById(R.id.password_cover);
        password_cover.setOnClickListener(this);

        username = (EditText) view.findViewById(R.id.username);
        disableShowSoftInput(username);
        username.setFocusable(true);
        username.setFocusableInTouchMode(true);
        username.requestFocus();
        username.requestFocusFromTouch();


        password = (EditText) view.findViewById(R.id.password);
        disableShowSoftInput(password);
        password.setFocusable(true);
        password.setFocusableInTouchMode(true);

        submit = (TextView) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        setup = (LinearLayout) view.findViewById(R.id.setup);
        setup.setOnClickListener(this);

        Qcode = (ImageView) view.findViewById(R.id.qcode);
        Bitmap qrBitmap = generateBitmap(catinetNumberString,200, 200);
        Qcode.setImageBitmap(qrBitmap);
    }

    public void disableShowSoftInput(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }
        }
        editText.setSelection(editText.getText().length());
    }

    private void main() {
    }

    private void handler() {
        returnErrorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                submit_state = 1;
                progressDialog.dismiss();
                String str = msg.getData().getString("message");
                showTheToast(str);
            }
        };
        returnSuccessInfoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                submit_state = 1;
                progressDialog.dismiss();
                String str = msg.getData().getString("message");
                Control.openNullDoorHandler.sendMessage(new Message());
            }
        };
        allowLoginSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setup_count = 0;
                getActivity().startActivity(new Intent(getActivity(), Setup.class));
                System.out.println("管理员登陆后台");
            }
        };

        getFocusHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(username!=null){
                    username.requestFocus();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == number_1.getId()){
            setStringToEditText(1);
        }else if(v.getId() == number_2.getId()){
            setStringToEditText(2);
        }else if(v.getId() == number_3.getId()){
            setStringToEditText(3);
        }else if(v.getId() == number_4.getId()){
            setStringToEditText(4);
        }else if(v.getId() == number_5.getId()){
            setStringToEditText(5);
        }else if(v.getId() == number_6.getId()){
            setStringToEditText(6);
        }else if(v.getId() == number_7.getId()){
            setStringToEditText(7);
        }else if(v.getId() == number_8.getId()){
            setStringToEditText(8);
        }else if(v.getId() == number_9.getId()){
            setStringToEditText(9);
        }else if(v.getId() == number_0.getId()){
            setStringToEditText(0);
        }else if(v.getId() == number_back.getId()){
            setStringToEditText(-1);
        }else if(v.getId() == number_clear.getId()){
            setStringToEditText(-2);
        }else if(v.getId() == username_cover.getId()){
            username.requestFocus();
        }else if(v.getId() == password_cover.getId()){
            password.requestFocus();
        }else if(v.getId() == setup.getId()){
            if(setup_count < 5){
                setup_count = setup_count + 1;
            }else{
                httpAllowLogin = new HttpAllowLogin(getActivity(),catinetNumberString,returnErrorHandler,allowLoginSuccessHandler);
                httpAllowLogin.start();
                setup_count = 0;
            }
        }else if(v.getId() == submit.getId() && submit_state == 1){
            submit_state = 0;
            httpLogin  = new HttpLogin(getActivity(),username_string,password_string,catinetNumberString,returnErrorHandler,returnSuccessInfoHandler);
            httpLogin.start();
            progressDialog.show();
            username.setText("");
            password.setText("");
            username_string = "";
            password_string = "";
            username.requestFocus();
        }
    }

    private void setStringToEditText(int number){
        if(number >= 0) {
            if (username.hasFocus()) {
                if(username_string.length() < 11){
                    username_string = username_string + Integer.toString(number);
                    username.setText(username_string);
                    username.setSelection(username.getText().length());
                    if(username_string.length() > 10){
                        password.requestFocus();
                    }
                }else{
                    password.requestFocus();
                }
            } else if (password.hasFocus()) {
                password_string = password_string + Integer.toString(number);
                int pasCount = password_string.length();
                String outString = "";
                for(int i = 0 ; i < pasCount ; i++){
                    outString = outString + "*";
                }
                password.setText(outString);
                password.setSelection(password.getText().length());
            }
        }else if(number == -1){ //退回
            if (username.hasFocus()) {
                if(username_string.length() > 0){
                    username_string = username_string.substring(0,username_string.length()-1);
                    username.setText(username_string);
                    username.setSelection(username.getText().length());
                }
            } else if (password.hasFocus()) {
                if(password_string.length() > 0) {
                    password_string = password_string.substring(0, password_string.length() - 1);
                    int pasCount = password_string.length();
                    String outString = "";
                    for (int i = 0; i < pasCount; i++) {
                        outString = outString + "*";
                    }
                    password.setText(outString);
                    password.setSelection(password.getText().length());
                }
            }
        }else if(number == -2){ //清空
            if (username.hasFocus()) {
                username_string = "";
                username.setText(username_string);
                username.setSelection(username.getText().length());
            } else if (password.hasFocus()) {
                password_string = "";
                password.setText(password_string);
                password.setSelection(password.getText().length());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void showTheToast(String string){
        Toast toast = Toast.makeText(getActivity(),string,Toast.LENGTH_LONG);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xff0a898f;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class HttpLogin extends Thread {

    private Activity activity;

    private String username,password,catinetNumberString;
    private Handler errorHandler,successHanler;

    private SharedPreferences sharedPreferences;

    public HttpLogin(Activity activity,String username,String password,String catinetNumberString,Handler errorHandler,Handler successHanler) {
        this.activity = activity;
        this.username = username;
        this.password = password;
        this.catinetNumberString = catinetNumberString;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
        sharedPreferences = activity.getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        String path = "http://www.huandianwang.com/index.php/login/login";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("mobile", username + ""));
        list.add(new BasicNameValuePair("password", password + ""));
        list.add(new BasicNameValuePair("cabinetid", catinetNumberString + ""));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                String PHPSESSID = null;
                String api_userid = null;
                String api_username = null;
                //写入cookies
                CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
                List<Cookie> cookies = mCookieStore.getCookies();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i = 0; i < cookies.size(); i++) {
                    if ("PHPSESSID".equals(cookies.get(i).getName())) {
                        PHPSESSID = cookies.get(i).getValue();
                        editor.putString("PHPSESSID", PHPSESSID);
                    }
                    if ("web_userid".equals(cookies.get(i).getName())) {
                        api_userid = cookies.get(i).getValue();
                        editor.putString("web_userid", api_userid);
                    }
                    if ("web_username".equals(cookies.get(i).getName())) {
                        api_username = cookies.get(i).getValue();
                        editor.putString("web_username", api_username);
                    }
                }
                editor.commit();

                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        successHanler.sendMessage(message);
                    }
                } else{
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "返回错误信息！");
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                }
            }else{
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "服务器错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                System.out.println(e.toString());
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "网络解析失败！！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        }
    }
}

class HttpAllowLogin extends Thread {

    private Activity activity;

    private String cabinet;
    private Handler errorHandler,successHanler;

    private SharedPreferences sharedPreferences;

    public HttpAllowLogin(Activity activity,String cabinet,Handler errorHandler,Handler successHanler) {
        this.activity = activity;
        this.cabinet = cabinet;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
        sharedPreferences = activity.getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        String path = "http://www.huandianwang.com/index.php/yz/allow_login";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("cabinet", cabinet + ""));
        String str = Unit.getMd5(cabinet + "!@23*#&(@912oOo388*@#(fslKK");
        list.add(new BasicNameValuePair("token",str));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject.toString());

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                String PHPSESSID = null;
                String api_userid = null;
                String api_username = null;
                //写入cookies
                CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
                List<Cookie> cookies = mCookieStore.getCookies();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i = 0; i < cookies.size(); i++) {
                    if ("PHPSESSID".equals(cookies.get(i).getName())) {
                        PHPSESSID = cookies.get(i).getValue();
                        editor.putString("PHPSESSID", PHPSESSID);
                    }
                    if ("web_userid".equals(cookies.get(i).getName())) {
                        api_userid = cookies.get(i).getValue();
                        editor.putString("web_userid", api_userid);
                    }
                    if ("web_username".equals(cookies.get(i).getName())) {
                        api_username = cookies.get(i).getValue();
                        editor.putString("web_username", api_username);
                    }
                }
                editor.commit();

                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        successHanler.sendMessage(message);
                    }
                } else{
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "返回错误信息！");
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                }
            }else{
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "服务器错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                System.out.println(e.toString());
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "网络解析失败！！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        }
    }
}
