package com.example.fullenergystore.main;

import java.util.ArrayList;
import java.util.List;

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
import android.view.View.OnFocusChangeListener;
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

public class LoginForgetPassword extends Activity implements OnClickListener{

	private TextView returnLogin;
	private InputMethodManager manager=null;
	private LinearLayout loginForgetPasswordFocus;
	private Button loginForgetPasswordSubmit;
	
	private EditText loginForgetPasswordNewPassword,loginForgetPasswordNewPasswordSure;
	public static Handler loginForgetPasswordSuccessHandler,loginForgetPasswordErrorHandler,unknownHandler;
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_forget_password);
		init();
		handler();
	}
	private void init(){
		
		new statusBar(this);
		progressDialog = new ProgressDialog(this);

		returnLogin = (TextView) this.findViewById(R.id.returnLogin);
		returnLogin.setOnClickListener(this);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		loginForgetPasswordFocus = (LinearLayout) this.findViewById(R.id.loginForgetPasswordFocus);
		
		loginForgetPasswordNewPassword = (EditText) this.findViewById(R.id.loginForgetPasswordNewPassword);
		loginForgetPasswordNewPasswordSure = (EditText) this.findViewById(R.id.loginForgetPasswordNewPasswordSure);
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
		loginForgetPasswordSubmit = (Button) this.findViewById(R.id.loginForgetPasswordSubmit);
		loginForgetPasswordSubmit.setOnClickListener(this);
	}

	private void handler(){
		loginForgetPasswordSuccessHandler = new Handler(){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
				startActivity(new Intent(LoginForgetPassword.this, Login.class));
				LoginForgetPassword.this.finish();
				overridePendingTransition(R.anim.in_left, R.anim.out_right);
				progressDialog.dismiss();
			}
		};
		loginForgetPasswordErrorHandler = new Handler(){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		unknownHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(returnLogin.getId() == arg0.getId()){
			startActivity(new Intent(this,Login.class));
			overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}else if(loginForgetPasswordSubmit.getId() == arg0.getId()){
			Intent intent = getIntent();
			String mobile = intent.getStringExtra("phone");
			String password = loginForgetPasswordNewPassword.getText().toString().trim();
			String passwordre = loginForgetPasswordNewPasswordSure.getText().toString().trim();
			String PHPSESSID = intent.getStringExtra("PHPSESSID");
			String mobileCookies = intent.getStringExtra("mobileCookies");
			
			if(mobile.equals("")||password.equals("")||passwordre.equals("")){
				Toast.makeText(getApplicationContext(), "输入信息不能为空" +
						"",Toast.LENGTH_SHORT).show();
			}else{
				if(PubFunction.isNetworkAvailable(this)){
					LoginForgetPasswordSubmit th = new LoginForgetPasswordSubmit(mobile,password,PHPSESSID,mobileCookies,this);
					th.start();
					progressDialog.show();
				}else{
					Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		}
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
			startActivity(new Intent(this, Login.class));
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}
		return super.onKeyDown(keyCode, event);
	}
}

class LoginForgetPasswordSubmit extends Thread{
	
	String mobile = null;
	String password = null;
	String PHPSESSID = null;
	String mobileCookies = null;
	private Activity activity;
	
	public LoginForgetPasswordSubmit(String mobile,String password,String PHPSESSID,String mobileCookies,Activity activity){
		this.mobile = mobile;
		this.password = password;
		this.PHPSESSID = PHPSESSID;
		this.mobileCookies = mobileCookies;
		this.activity = activity;
	}
	
	@Override
	public void run() {
		super.run();
		String path = PubFunction.www + "api_business.php/login/forget_password";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("mobile", mobile));
		list.add(new BasicNameValuePair("password", password));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			
			httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "cd_mobile=" + mobileCookies);
			
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageString = jsonObject.getString("message");
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);
				
				if(activity != null){
					if(code.equals("200")){
						if(LoginForgetPassword.loginForgetPasswordErrorHandler!=null){
							LoginForgetPassword.loginForgetPasswordErrorHandler.sendMessage(message);
						}
					}else if(code.equals("100")){
						if(LoginForgetPassword.loginForgetPasswordSuccessHandler!=null){
							LoginForgetPassword.loginForgetPasswordSuccessHandler.sendMessage(message);
						}
					}else{
						LoginForgetPassword.unknownHandler.sendMessage(message);
					}
				}
			
			}
		} catch (Exception e) {
			if(activity != null){
				LoginForgetPassword.unknownHandler.sendMessage(new Message());
			}
		}
	}


}
