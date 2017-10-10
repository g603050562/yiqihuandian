package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

@EActivity(R.layout.login_registered)
public class LoginRegistered extends Activity{

	@ViewById
	TextView returnLogin,registred_info;
	@ViewById
	Button loginRegisteredGetCode, loginRegisteredSubmit;
	@ViewById
	EditText loginRegisteredPhone, loginRegisteredCode, loginRegisteredPassword, loginRegisteredPasswordSure,loginRegisteredInvitation;
	@ViewById
	LinearLayout loginRegisteredFocus;

	private InputMethodManager manager = null;
	private ProgressDialog progressDialog;
	private int recLen = 60;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
	}

	@AfterViews
	void initView(){
		init();
	}

	private void init() {
		new StatusBar(this);
		progressDialog = new ProgressDialog(this);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		loginRegisteredPasswordSure.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (!arg1) {
					String st1 = loginRegisteredPassword.getText().toString().trim();
					String st2 = loginRegisteredPasswordSure.getText().toString().trim();
					if (!st1.equals(st2)) {
						loginRegisteredPassword.setText("");
						loginRegisteredPasswordSure.setText("");
						Toast.makeText(getApplicationContext(), "两次密码输入不正确，请重新输入！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	@Click
	void returnLogin(){
		startActivity(new Intent(this, Login_.class));
		this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		this.finish();
	}
	@Click
	void registred_info(){
		startActivity(new Intent(this, LoginRegisteredInfo_.class));
		this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
	}

	@Click
	void loginRegisteredGetCode(){
		String phone = loginRegisteredPhone.getText().toString();
		if (phone.equals("")) {
			Toast.makeText(getApplicationContext(), "请填写您的手机号", Toast.LENGTH_SHORT).show();
			loginRegisteredPhone.requestFocus();
		} else {
			if (PubFunction.isNetworkAvailable(this)) {
				progressDialog.show();
				loginRegisteredGetCode(phone);
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Click
	void loginRegisteredSubmit(){
		if (PubFunction.isNetworkAvailable(this)) {
			loginRegisteredSubmit(loginRegisteredPhone.getText().toString().trim(), loginRegisteredCode.getText().toString().trim(), loginRegisteredPassword.getText().toString().trim(), loginRegisteredInvitation.getText().toString());
			progressDialog.show();
		} else {
			Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	// 点击别处隐藏键盘用的
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				loginRegisteredFocus.requestFocus();
			}
		}
		return super.onTouchEvent(event);
	}
	//点击返回键返回
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
			startActivity(new Intent(this, Login_.class));
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	//点击获取验证码
	@Background
	void  loginRegisteredGetCode(String phone){
		String path = PubFunction.www + "api.php/login/send_sms";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile",phone));
		httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonToken = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonToken.nextValue();
				String code = jsonObject.getString("code");
				String messageString = jsonObject.getString("message");
				if (activity != null) {
					if (code.equals("200")) {
						registeredGetCodeError(messageString);
					} else if (code.equals("100")) {
						registeredGetCodeSuccess(messageString);
					} else {
						unknown();
					}
				}
			}
		} catch (Exception e) {
			if (activity != null) {
				unknown();
			}
		}
	}

	//上传注册的个人信息
	@Background
	void loginRegisteredSubmit(String mobile,String vcode,String password,String invitation){
		String path = PubFunction.www + "api.php/login/register";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile", mobile));
		list.add(new BasicNameValuePair("mobile_verify", vcode));
		list.add(new BasicNameValuePair("password", password));
		list.add(new BasicNameValuePair("yqm", invitation));
		httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageString = jsonObject.getString("message");
				if (activity != null) {
					if (code.equals("200")) {
						registeredSubmitError(messageString);
					} else if (code.equals("100")) {
						registeredSubmitSuccess(messageString);
					} else {
						unknown();
					}
				}
			}
		} catch (Exception e) {
			if (activity != null) {
				unknown();
			}
		}
	}
	//倒计时
	@Background
	void countDown(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		recLen--;
		setS();
	}
	@UiThread
	void setS(){
		if(recLen == 0){
			loginRegisteredGetCode.setText("获取验证码");
			loginRegisteredGetCode.setClickable(true);
			loginRegisteredGetCode.setBackgroundResource(R.drawable.button_corners_orange_radius_5);
			recLen = 60;
		}else{
			loginRegisteredGetCode.setText("请稍后(" + recLen + "S)");
			countDown();
		}
	}
    @UiThread
	void registeredGetCodeSuccess(String msg){
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
		loginRegisteredGetCode.setClickable(false);
		loginRegisteredGetCode.setBackgroundResource(R.drawable.button_corners_gray_radius_5);
		countDown();
		loginRegisteredGetCode.setText("请稍后(" + recLen + "S)");
	}
	@UiThread
	void registeredGetCodeError(String msg){
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}
	@UiThread
	void registeredSubmitSuccess(String msg){
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		startActivity(new Intent(LoginRegistered.this, Login_.class));
		LoginRegistered.this.finish();
		progressDialog.dismiss();
	}
	@UiThread
	void registeredSubmitError(String msg){
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}
	@UiThread
	void unknown(){
		Toast.makeText(getApplicationContext(), "未知错误!", Toast.LENGTH_SHORT).show();
	}
}
