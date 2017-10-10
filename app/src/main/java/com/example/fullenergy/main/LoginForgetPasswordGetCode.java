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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;


@EActivity(R.layout.login_forget_password_get_code)
public class LoginForgetPasswordGetCode extends Activity {

	private Activity activity;
	@ViewById
	TextView returnLogin;
	@ViewById
	LinearLayout loginForgetPasswordGetCodeFocus;
	@ViewById
	Button loginForgetPasswordGetCodeSubmit, loginForgetPasswordGetCodeGetCode;
	@ViewById
	EditText loginForgetPasswordGetCodeInputCode, loginForgetPasswordGetCodePhone;

	private ProgressDialog progressDialog;
	private InputMethodManager manager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.activity = this;

	}

	@AfterViews
	void afterViews(){
		init();
	}

	@Click
	void returnLogin(){
		startActivity(new Intent(this, Login_.class));
		this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		this.finish();
	}

	@Click
	void loginForgetPasswordGetCodeGetCode(){
		String phone = loginForgetPasswordGetCodePhone.getText().toString();
		if (phone.equals("")) {
			Toast.makeText(getApplicationContext(), "请填写您的手机号", Toast.LENGTH_SHORT).show();
			loginForgetPasswordGetCodeFocus.requestFocus();
		} else {
			if (PubFunction.isNetworkAvailable(this)) {
				LoginForgetPasswordGetCodeGetCode(phone);
				progressDialog.show();
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}

		}
	}

	@Click
	void loginForgetPasswordGetCodeSubmit(){
		String code = loginForgetPasswordGetCodeInputCode.getText().toString();
		String phone = loginForgetPasswordGetCodePhone.getText().toString();
		if (phone.equals("")) {
			Toast.makeText(getApplicationContext(), "请填写您的手机号", Toast.LENGTH_SHORT).show();
			loginForgetPasswordGetCodeFocus.requestFocus();
		}else if (code.equals("")) {
			Toast.makeText(getApplicationContext(), "请填写您的验证码", Toast.LENGTH_SHORT).show();
			loginForgetPasswordGetCodeFocus.requestFocus();
		} else {
			if (PubFunction.isNetworkAvailable(this)) {
				LoginForgaetPasswordGetCodeVerify(loginForgetPasswordGetCodePhone.getText().toString().trim(),loginForgetPasswordGetCodeInputCode.getText().toString().trim());
				progressDialog.show();
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}

		}
	}

	@UiThread
	void unknownHandler(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	@UiThread
	void loginForgetPasswordGetCodeGetCodeSuccessHandler(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}
	@UiThread
	void loginForgetPasswordGetCodeGetCodeErrorHandler(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}
	@UiThread
	void loginForgetPasswordGetCodeVerifyErrorHandler(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}
	@UiThread
	void loginForgetPasswordGetCodeVerifySuccessHandler(String message,String PHPSESSID,String mobileCookies){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(activity, LoginForgetPassword_.class);
		intent.putExtra("phone", loginForgetPasswordGetCodePhone.getText().toString().trim());
		intent.putExtra("PHPSESSID", PHPSESSID);
		intent.putExtra("mobileCookies", mobileCookies);
		LoginForgetPasswordGetCode.this.startActivity(intent);
		activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
		LoginForgetPasswordGetCode.this.finish();
		progressDialog.dismiss();
	}

	private void init() {
		new StatusBar(this);
		progressDialog = new ProgressDialog(this);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
			startActivity(new Intent(this, Login_.class));
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Background
	void LoginForgetPasswordGetCodeGetCode(String mobile){
		String path = PubFunction.www + "api.php/login/send_sms";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile",mobile));
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

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);

				if (activity != null) {
					if (code.equals("200")) {
						loginForgetPasswordGetCodeGetCodeErrorHandler(messageString);
					} else if (code.equals("100")) {
						loginForgetPasswordGetCodeGetCodeSuccessHandler(messageString);
					} else {
						unknownHandler(messageString);
					}
				}
			}
		} catch (Exception e) {
			if (activity != null) {
				unknownHandler(e.toString());
			}
		}
	}

	@Background
	void LoginForgaetPasswordGetCodeVerify(String mobile , String mobileVerify){
		String path = PubFunction.www + "api.php/login/mobile_yzm_users";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile", mobile));
		list.add(new BasicNameValuePair("mobile_verify", mobileVerify));
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
				String messageString = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				if (code.equals("200")) {
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("message", messageString);
					message.setData(bundle);
					if (activity != null) {
						loginForgetPasswordGetCodeVerifyErrorHandler(messageString);
					}
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
					if (activity != null) {
						loginForgetPasswordGetCodeVerifySuccessHandler(messageString,PHPSESSID,mobileCookies);
					}
				} else {
					if (activity != null) {
						unknownHandler(messageString);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			unknownHandler(e.toString());
		}
	}

}
