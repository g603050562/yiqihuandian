package com.example.fullenergy.main;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;


public class PanelIndexComplaint extends Activity implements OnClickListener{

	private Activity view;
	private LinearLayout panelIndexCompReturn;
	private EditText panelIndexComplaintText;
	private TextView panelIndexComplaintButton;
	private SharedPreferences Preferences;
	private ProgressDialog dialog;
	
	public static Handler panelIndexComplaintSuccessHandler,panelIndexComplaintErrorHandler,panelIndexComplaintUnknownHandler,turnToLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_index_complaint);
		view = this;
		
		init();
		handler();
	}
	
	
	private void handler() {
		panelIndexComplaintSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
				view.finish();
				dialog.dismiss();
			}
		};
		panelIndexComplaintErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelIndexComplaintUnknownHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(),"发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		
		turnToLogin = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = view.getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(view, Login_.class);
				intent.putExtra("type", "1");
				view.startActivity(intent);
				finish();
				overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
	}

	private void init(){
		Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		dialog = Panel.progressDialog;
		
		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);


		panelIndexCompReturn = (LinearLayout) view.findViewById(R.id.panelIndexCompReturn);
		panelIndexCompReturn.setOnClickListener(this);
		panelIndexComplaintText = (EditText) view.findViewById(R.id.panelIndexComplaintText);
		panelIndexComplaintButton = (TextView) view.findViewById(R.id.panelIndexComplaintButton);
		panelIndexComplaintButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		if(panelIndexCompReturn.getId() == arg0.getId()){
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}else if(panelIndexComplaintButton.getId() == arg0.getId()){
			String str = panelIndexComplaintText.getText().toString().trim();
			if(!str.equals("")){
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				if(PubFunction.isNetworkAvailable(this)){
					HttpPanelIndexComplaint th = new HttpPanelIndexComplaint(str,Preferences,this);
					th.start();
					dialog.show();
				}else{
					Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
				
			}else{
				Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
}

class HttpPanelIndexComplaint extends Thread{
	
	private String content;
	private SharedPreferences Preferences;
	private Activity activity;
	
	public HttpPanelIndexComplaint(String content,SharedPreferences Preferences,Activity activity) {
		this.content = content;
		this.Preferences = Preferences;
		this.activity = activity;
	}
	
	@Override
	public void run() {
		super.run();
		
		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String api_userid = Preferences.getString("api_userid", null);
		String api_username = Preferences.getString("api_username", null);
		String path = PubFunction.www+"api.php/member/tousu";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("content", content));
		
		try {
			httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageString = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);
				if(activity!=null){
					if(code.equals("200")){
						if (messageString.equals("秘钥不正确,请重新登录")) {
							PanelIndexComplaint.turnToLogin.sendMessage(new Message());
						} else {
							PanelIndexComplaint.panelIndexComplaintErrorHandler.sendMessage(message);
						}
					}else if(code.equals("100")){
						PanelIndexComplaint.panelIndexComplaintSuccessHandler.sendMessage(message);
					}else{
						PanelIndexComplaint.panelIndexComplaintUnknownHandler.sendMessage(new Message());
					}
				}
			}
		}catch (Exception e) {
			if(activity!=null){
				PanelIndexComplaint.panelIndexComplaintUnknownHandler.sendMessage(new Message());		
			}
		}
	}
}
