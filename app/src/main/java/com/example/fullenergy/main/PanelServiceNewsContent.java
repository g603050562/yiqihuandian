package com.example.fullenergy.main;

import java.text.SimpleDateFormat;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelServiceNewsContent extends Activity implements OnClickListener {

	private ImageView panelServiceNewsContentImg;
	private LinearLayout panelServiceNewsReturn;
	public static Handler panelServiceNewsContentSuccessHandler, panelServiceNewsContentErrorHandler,
			panelServiceNewsContentUnknownHandler,turnToLogin;
	private SharedPreferences preference;
	private HttpPanelServiceNewsContent th;
	private int id = 0;
	private TextView panelServiceNewsContentTitle, panelServiceNewsContentDate, panelServiceNewsContentContent;
	private ProgressDialog dialog;
	private Activity view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_service_news_content);
		view = this;

		init();
		handler();
		main();

	}

	private void handler() {
		panelServiceNewsContentSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					panelServiceNewsContentTitle.setText(jsonObject.getString("title").trim().toString());
					String url = PubFunction.www + (jsonObject.getString("thumb").trim().toString());
					Picasso.with(getApplicationContext()).load(url).into(panelServiceNewsContentImg);

					Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
					String dateTime = df.format(date);
					panelServiceNewsContentDate.setText(dateTime);

					panelServiceNewsContentContent.setText(jsonObject.getString("content").trim().toString());
					dialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		panelServiceNewsContentErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelServiceNewsContentUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
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
				view.finish();
				view.overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
	}

	private void init() {
		preference = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		Intent intent = getIntent();
		this.id = Integer.parseInt(intent.getStringExtra("id"));

		dialog = Panel.progressDialog;

		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);

		panelServiceNewsReturn = (LinearLayout) findViewById(R.id.panelServiceNewsReturn);
		panelServiceNewsReturn.setOnClickListener(this);
		panelServiceNewsContentTitle = (TextView) findViewById(R.id.panelServiceNewsContentTitle);
		panelServiceNewsContentImg = (ImageView) findViewById(R.id.panelServiceNewsContentImg);
		panelServiceNewsContentDate = (TextView) findViewById(R.id.panelServiceNewsContentDate);
		panelServiceNewsContentContent = (TextView) findViewById(R.id.panelServiceNewsContentContent);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(this)) {

			th = new HttpPanelServiceNewsContent(id, preference, this);
			th.start();
			dialog.show();
		} else {
			Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelServiceNewsReturn.getId()) {
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
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

}

class HttpPanelServiceNewsContent extends Thread {

	private SharedPreferences preferences;
	private int id;
	private JSONObject jsonObject;
	private Activity activity;

	public HttpPanelServiceNewsContent(int id, SharedPreferences preferences, Activity activity) {
		this.preferences = preferences;
		this.id = id;
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/service/show_news";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id + ""));
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
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
				this.jsonObject = jsonObject.getJSONObject("data");

				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelServiceNewsContent.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelServiceNewsContent.panelServiceNewsContentErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelServiceNewsContent.panelServiceNewsContentSuccessHandler.sendMessage(new Message());
					} else {
						PanelServiceNewsContent.panelServiceNewsContentUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (activity != null) {
				PanelServiceNewsContent.panelServiceNewsContentUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}