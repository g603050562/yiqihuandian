package com.example.fullenergy.main;

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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelServiceLease extends Activity implements OnClickListener {

	private ImageView buttonReturn, panelServiceLeaseButton;
	private SharedPreferences preferences;
	public static Handler panelServiceLeaseSuccessHandler, panelServiceLeaseErrorHandler,
			panelServiceLeaseUnknownHandler, panelServiceLeaseSubmitSuccessHandler, panelServiceLeaseSubmitErrorHandler,turnToLogin;
	private HttpPanelServiceLease th;
	public static Handler panelServiceLeaseHandler;
	private TextView panelServiceLeaseAddress, panelServiceLeaseContent;
	private ProgressDialog dialog;
	
	private Activity view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_service_lease);
		view = this;

		init();
		handler();
		main();

	}

	private void handler() {
		panelServiceLeaseSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					panelServiceLeaseContent.setText("        " + jsonObject.getString("content"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		panelServiceLeaseErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		};
		panelServiceLeaseSubmitSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		};
		panelServiceLeaseSubmitErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		};
		panelServiceLeaseUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplication(), "发生未知错误！", Toast.LENGTH_SHORT).show();
			}
		};

		panelServiceLeaseHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				panelServiceLeaseAddress.setText("        " + msg.getData().getString("address"));
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
		preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		dialog = Panel.progressDialog;

		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);

		dialog.show();

		buttonReturn = (ImageView) this.findViewById(R.id.panelServiceLeaseReturn);
		buttonReturn.setOnClickListener(this);
		panelServiceLeaseButton = (ImageView) this.findViewById(R.id.panelServiceLeaseButton);
		panelServiceLeaseButton.setOnClickListener(this);
		panelServiceLeaseAddress = (TextView) this.findViewById(R.id.panelServiceLeaseAddress);
		panelServiceLeaseContent = (TextView) this.findViewById(R.id.panelServiceLeaseContent);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(this)) {

			th = new HttpPanelServiceLease(preferences, this);
			th.start();
			PanelIndexIndex.getGeoCoderHandler.sendMessage(new Message());
		} else {
			Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == buttonReturn.getId()) {
			this.finish();
			overridePendingTransition(R.anim.in_left, R.anim.out_right);
		} else if (panelServiceLeaseButton.getId() == arg0.getId()) {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "1324234234"));
			startActivity(intent);

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

class HttpPanelServiceLease extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Activity activity;

	public HttpPanelServiceLease(SharedPreferences preferences, Activity activity) {
		this.preferences = preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/service/rescue_explain";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
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
							PanelServiceLease.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelServiceLease.panelServiceLeaseErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelServiceLease.panelServiceLeaseSuccessHandler.sendMessage(new Message());
					} else {
						PanelServiceLease.panelServiceLeaseUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (activity != null) {
				PanelServiceLease.panelServiceLeaseUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
