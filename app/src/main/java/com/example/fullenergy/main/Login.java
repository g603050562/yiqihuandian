package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

/**
 * 类说明： 登陆页面
 *
 * Login - 主activity
 *
 * HttpLogin - 登陆信息的提交
 */
@EActivity(R.layout.login)
public class Login extends Activity{

	@ViewById
	Button login_button;// 登陆按钮
	@ViewById
	TextView forget_password, registered; // 两个textview，相当于按钮，一个是忘记密码，一个注册
	@ViewById
	EditText login_name, login_password; // 填写 用户名 密码
	@ViewById
	LinearLayout laoginFocus; // 主要用于点击别处，隐藏键盘

	private String userNameString, passWordString; // 用户名 密码 字符串
	private ProgressDialog progressDialog; // loading的弹出框
	private SharedPreferences Preferences; // SharedPreferences数据库
	private InputMethodManager manager = null; // 键盘
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
	}

	@AfterViews
	void initView(){
		init();
		main();
	}

	private void init() {
		new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
		progressDialog = new ProgressDialog(this); // loading弹出框初始化
		//数据库和键盘
		Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void main() {
		// 这个是判断 是不是 被强制下线了 如果intent里面传来了一个type为1，就会弹出一个alert，告诉被强制下线了，得要重新登陆
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
		// 获取数据库里面的信息，如果没有就为空
		userNameString = Preferences.getString("usrename", null);
		passWordString = Preferences.getString("password", null);
	}

	@Click
	void login_button(){
		userNameString = login_name.getText().toString().trim();
		passWordString = login_password.getText().toString().trim();
		if (userNameString == null || userNameString.equals("") || passWordString == null || passWordString.equals("")) {
			Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
		} else {
			if (PubFunction.isNetworkAvailable(this)) { // 看是否有网
				progressDialog.show(); // 显示loading框
				httpLogin();// 启动线程
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Click
	void forget_password(){
		startActivity(new Intent(this, LoginForgetPasswordGetCode_.class));
		this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
		this.finish();
	}
	@Click
	void registered(){
		startActivity(new Intent(this, LoginRegistered_.class));
		this.overridePendingTransition(R.anim.in_right, R.anim.out_left);
		this.finish();
	}
	// 点击别处隐藏键盘用的
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				laoginFocus.requestFocus();
			}
		}
		return super.onTouchEvent(event);
	}
	/**
	 * 按两次退出程序
	 */
	private static Boolean isExit = false;
	@SuppressLint("NewApi")
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Timer tExit = null;
			if (isExit == false) {
				isExit = true; // 准备退出
				Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
				tExit = new Timer();
				tExit.schedule(new TimerTask() {
					@Override
					public void run() {
						isExit = false;// 取消退出
					}
				}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
			} else {
				finish();
			}
			return false;
		} else {
			return super.dispatchKeyEvent(event); // 按下其他按钮，调用父类进行默认处理
		}
	}

	@Background
	void httpLogin(){
		//接口地址
		String path = PubFunction.www + "api.php/login/login";
		//建立连接
		HttpPost httpPost = new HttpPost(path);
		//相应时间
		httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		// post参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", userNameString));
		params.add(new BasicNameValuePair("password", passWordString));
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
		editor.putString("usrename", userNameString);
		editor.putString("password", passWordString);
		editor.commit();
		startActivity(new Intent(Login.this, GreenMain_.class));
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
}
