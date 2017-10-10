package com.example.fullenergy.main;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;


import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.CreateFile;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.InstrumentedActivity;
@EActivity(R.layout.main)
public class Main extends InstrumentedActivity {

	@ViewById
	TextView mainVersionCode;

	private Thread th;
	private Activity activity;
	private String userNameString, passWordString;
	private SharedPreferences Preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@AfterViews
	void initView(){
		init();
		doInBack();
	}

	private void init() {
		activity = this;

		//沉浸式 + 创建文件夹
		new CreateFile(getApplicationContext());
		new StatusBar(this);

		//极光推送 服务
		Intent intent = new Intent(Main.this, NotificationService.class);
		intent.setAction("Main");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(intent);

		//版本号
		PackageManager manager = getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mainVersionCode.setText("Version : " + info.versionName);

		//获得上次登陆信息
		Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		userNameString = Preferences.getString("usrename", null);
		passWordString = Preferences.getString("password", null);
	}


	//验证登录信息
	@Background
	void doInBack(){
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (userNameString != null && passWordString != null) {
			String path = PubFunction.www + "api.php/login/login";
			HttpPost httpPost = new HttpPost(path);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("mobile", userNameString));
			params.add(new BasicNameValuePair("password", passWordString));
			try {
				HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
				httpPost.setEntity(entity);
				DefaultHttpClient client = new DefaultHttpClient();

				client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
				client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000    );

				HttpResponse httpResponse = client.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String result = EntityUtils.toString(httpResponse.getEntity());
					JSONTokener jsonParser = new JSONTokener(result);
					JSONObject person = (JSONObject) jsonParser.nextValue();

					String PHPSESSID = null;
					String api_userid = null;
					String api_username = null;
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

					if (person.get("code").toString().equals("200")) {
						turnToLogin();
					} else {
						turnToPanel();
					}
				}else{
					turnToPanel();
				}
			} catch (Exception e) {
				turnToLogin();
			}
		} else {
			turnToLogin();
		}
	}

	@UiThread
	void turnToLogin(){
		startActivity(new Intent(getApplicationContext(), Login_.class));
		activity.finish();
	}

	@UiThread
	void turnToPanel(){
		startActivity(new Intent(getApplicationContext(),GreenMain_.class));
		activity.finish();
	}

}
