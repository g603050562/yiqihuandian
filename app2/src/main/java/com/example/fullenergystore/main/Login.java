package com.example.fullenergystore.main;


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
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.List;

public class Login extends Activity implements OnClickListener {

	private Button login_button;
	private TextView forget_password, registered,registred_info;
	private EditText userNameEditText, passwordEditText;
	private String userNameString, passWordString;
	private LinearLayout laoginFocus;
	public static Handler loginSuccessHandler, loginErrorHandler, unknownHandler;
	private ProgressDialog progressDialog;
	private SharedPreferences Preferences;
	private HttpLogin hp;
	private InputMethodManager manager = null;
	
	public Handler isRemoteLoginHandler; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		init();
		handler();
		main();
	}

	private void init() {

		new statusBar(this);
		progressDialog = new ProgressDialog(this);

		login_button = (Button) this.findViewById(R.id.login_button);
		login_button.setOnClickListener(this);
		forget_password = (TextView) this.findViewById(R.id.forget_password);
		forget_password.setOnClickListener(this);
		userNameEditText = (EditText) this.findViewById(R.id.login_name);
		passwordEditText = (EditText) this.findViewById(R.id.login_password);
		laoginFocus = (LinearLayout) this.findViewById(R.id.laoginFocus);
		Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		registred_info = (TextView) this.findViewById(R.id.registred_info);
		registred_info.setOnClickListener(this);
	}

	private void handler() {
		loginSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				Toast.makeText(getApplicationContext(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = Preferences.edit();
				editor.putString("usrename", userNameString);
				editor.putString("password", passWordString);
				editor.commit();
				startActivity(new Intent(Login.this, Panel.class));
				Login.this.finish();
				overridePendingTransition(R.anim.in_right, R.anim.out_left);
				progressDialog.dismiss();
			}
		};
		loginErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		unknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "未知错误!", Toast.LENGTH_SHORT).show();
			}
		};
		
		isRemoteLoginHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "您的帐号已在其他终端登录，请您核实帐号信息!", Toast.LENGTH_LONG).show();
			}
		};
	}
	
	private void main(){
		Intent intent = getIntent();
		String str = null;
		str = intent.getStringExtra("type");
		if (str != null) {
			if (str.equals("1")) {

				LayoutInflater inflater = LayoutInflater.from(this);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				final AlertDialog mAlertDialog = builder.create();
				View view = inflater.inflate(R.layout.alertdialog_login, null);

				TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
				success.setText("确定");
				success.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mAlertDialog.dismiss();
					}
				});

				mAlertDialog.show();
				mAlertDialog.getWindow().setContentView(view);
			}
		}
		
		userNameString = Preferences.getString("usrename", null);
		passWordString = Preferences.getString("password", null);
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == login_button.getId()) {

			userNameString = userNameEditText.getText().toString().trim();
			passWordString = passwordEditText.getText().toString().trim();

			if (userNameString == null || userNameString.equals("") || passWordString == null
					|| passWordString.equals("")) {
				Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
			} else {

				if (PubFunction.isNetworkAvailable(this)) {
					progressDialog.show();
					hp = new HttpLogin(userNameString, passWordString, Preferences,this);
					hp.start();
				} else {
					Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
			// startActivity(new Intent(Login.this,Panel.class));
		} else if (arg0.getId() == forget_password.getId()) {
			startActivity(new Intent(this, LoginForgetPasswordGetCode.class));
			overridePendingTransition(R.anim.in_right, R.anim.out_left);
		} else if (arg0.getId() == registred_info.getId()) {
			startActivity(new Intent(this, LoginInfo.class));
			this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				laoginFocus.requestFocus();
			}
		}
		return super.onTouchEvent(event);
	}
}

class HttpLogin extends Thread {

	private String nameString;
	private String passwordString;
	private SharedPreferences Preferences;
	private Activity activity;

	public HttpLogin(String nameString, String passwordString, SharedPreferences Preferences, Activity activity) {
		this.nameString = nameString;
		this.passwordString = passwordString;
		this.Preferences = Preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String path = PubFunction.www + "api_business.php/login/login";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", nameString));
		params.add(new BasicNameValuePair("password", passwordString));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(entity);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				System.out.println(result+"aa");
				JSONTokener jsonParser = new JSONTokener(result);
				JSONObject person = (JSONObject) jsonParser.nextValue();
				String messageString = person.get("message").toString();
				String code = person.getString("code").toString();

				String PHPSESSID = null;
				String apibus_businessid = null;
				String apibus_username = null;
				CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				
				SharedPreferences.Editor editor = Preferences.edit();
				for (int i = 0; i < cookies.size(); i++) {
					if ("PHPSESSID".equals(cookies.get(i).getName())) {
						PHPSESSID = cookies.get(i).getValue();
						editor.putString("PHPSESSID", PHPSESSID);
					}
					if ("apibus_businessid".equals(cookies.get(i).getName())) {
						apibus_businessid = cookies.get(i).getValue();
						editor.putString("apibus_businessid", apibus_businessid);
					}
					if ("apibus_username".equals(cookies.get(i).getName())) {
						apibus_username = cookies.get(i).getValue();
						editor.putString("apibus_username", apibus_username);
					}
				}
				editor.commit();

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);

				if (activity != null) {
					if (code.equals("200")) {
						Login.loginErrorHandler.sendMessage(message);
					} else if (code.equals("100")) {

						if(person.has("data")){
							JSONObject dataJson = person.getJSONObject("data");
							String bid = dataJson.getString("bid");
							SharedPreferences.Editor editor_1 = Preferences.edit();
							editor_1.putString("bid", bid);
							editor_1.commit();
						}

						Login.loginSuccessHandler.sendMessage(message);
					} else {
						Login.unknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			// System.out.println(e.toString());
			if (activity != null) {
				Login.unknownHandler.sendMessage(new Message());
			}
		}
	}
}
