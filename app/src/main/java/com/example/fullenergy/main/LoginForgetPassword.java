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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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


/**
 * 类说明： 忘记密码页面
 * 
 * LoginForgetPassword - 主activity
 * 
 * LoginForgetPasswordSubmit - 忘记密码信息的提交
 */

@EActivity(R.layout.login_forget_password)
public class LoginForgetPassword extends Activity {


	@ViewById
	TextView returnLogin;
	@ViewById
	LinearLayout loginForgetPasswordFocus;
	@ViewById
	Button loginForgetPasswordSubmit;
	@ViewById
	EditText loginForgetPasswordNewPassword, loginForgetPasswordNewPasswordSure;

	private Activity activity;
	private ProgressDialog progressDialog;
	private InputMethodManager manager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
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
	void loginForgetPasswordSubmit(){
		Intent intent = getIntent();
		String mobile = intent.getStringExtra("phone");
		String password = loginForgetPasswordNewPassword.getText().toString().trim();
		String passwordsure = loginForgetPasswordNewPasswordSure.getText().toString().trim();
		String PHPSESSID = intent.getStringExtra("PHPSESSID");
		String mobileCookies = intent.getStringExtra("mobileCookies");

		if(!password.equals(passwordsure)){
			Toast.makeText(getApplicationContext(), "输入信息不一致，请重新输入！", Toast.LENGTH_SHORT).show();
			loginForgetPasswordNewPassword.setText("");
			loginForgetPasswordNewPasswordSure.setText("");
		}else if (mobile.equals("") || password.equals("")||passwordsure.equals("")){
			Toast.makeText(getApplicationContext(), "输入信息不能为空！", Toast.LENGTH_SHORT).show();
		} else {
			if (PubFunction.isNetworkAvailable(this)) {
				LoginForgetPasswordSubmit(mobile,password,PHPSESSID,mobileCookies);
				progressDialog.show();
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@UiThread
	void unknownHandler(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	@UiThread
	void loginForgetPasswordSuccessHandler(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		startActivity(new Intent(LoginForgetPassword.this, Login_.class));
		activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
		LoginForgetPassword.this.finish();
		progressDialog.dismiss();
	}

	@UiThread
	void loginForgetPasswordErrorHandler(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
	}

	private void init() {
		new StatusBar(this);
		progressDialog = new ProgressDialog(this);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		loginForgetPasswordNewPasswordSure.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (!arg1) {
					String st1 = loginForgetPasswordNewPassword.getText().toString().trim();
					String st2 = loginForgetPasswordNewPasswordSure.getText().toString().trim();
					if (!st1.equals(st2)) {
						loginForgetPasswordNewPassword.setText("");
						loginForgetPasswordNewPasswordSure.setText("");
						Toast.makeText(getApplicationContext(), "两次密码输入不正确，请重新输入！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}


	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				loginForgetPasswordFocus.requestFocus();
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
	void LoginForgetPasswordSubmit(String mobile , String password , String PHPSESSID , String mobileCookies){
		String path = PubFunction.www + "api.php/login/forget_password";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile", mobile));
		list.add(new BasicNameValuePair("password", password));
		httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "cd_mobile=" + mobileCookies);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageString = jsonObject.getString("message");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);

				if (activity != null) {
					if (code.equals("200")) {
						loginForgetPasswordErrorHandler(messageString);
					} else if (code.equals("100")) {
						loginForgetPasswordSuccessHandler(messageString);
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

}

