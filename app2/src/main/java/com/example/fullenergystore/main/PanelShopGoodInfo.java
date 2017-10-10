package com.example.fullenergystore.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergystore.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Picasso;
import com.tandong.bottomview.view.BottomView;

import com.example.fullenergystore.extend_plug.StatusBar.statusBar;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.a;
import cn.jpush.android.util.ac;

public class PanelShopGoodInfo extends Activity implements OnClickListener {

	private LinearLayout panelMineGoodInfoReturn;
	private TextView panelShopGoodInfoTitle, panelShopGoodInfoBuy, count, add, reduce;
	private WebView panelShopGoodInfoContent;
	private InputMethodManager manager = null;
	public static Handler panelShopGoodInfoSuccessHandler, panelShopGoodInfoErrorHandler,
			panelShopGoodInfoUnknownHandler, turnToLogin,panelShopSubmitGoodSuccessHandler,panelShopSubmitGoodErrorHandler;
	private HttpPanelShopGoodInfo th;
	private SharedPreferences preferences;

	private String id;
	private String name;
	private HttpPanelShopSubmitGood th1;
	private ProgressDialog dialog;

	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_shop_good_info);
		activity = this;

		init();
		handler();
		main();

	}

	private void handler() {
		// TODO Auto-generated method stub
		panelShopGoodInfoSuccessHandler = new Handler() {
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				System.out.println(jsonObject.toString());
				try {
					name = jsonObject.getString("goodsname");
					panelShopGoodInfoTitle.setText(name);
					// panelShopGoodInfoContent.setText(content);

					WebSettings webSettings = panelShopGoodInfoContent.getSettings();
					webSettings.setJavaScriptEnabled(true);

					// User settings

					webSettings.setJavaScriptEnabled(true);
					webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
					webSettings.setUseWideViewPort(true);// 关键点

					webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

					webSettings.setDisplayZoomControls(false);
					webSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
					webSettings.setAllowFileAccess(true); // 允许访问文件
					webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
					webSettings.setSupportZoom(true); // 支持缩放

					webSettings.setLoadWithOverviewMode(true);

					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int mDensity = metrics.densityDpi;
					Log.d("maomao", "densityDpi = " + mDensity);
					if (mDensity == 240) {
						webSettings.setDefaultZoom(ZoomDensity.FAR);
					} else if (mDensity == 160) {
						webSettings.setDefaultZoom(ZoomDensity.MEDIUM);
					} else if (mDensity == 120) {
						webSettings.setDefaultZoom(ZoomDensity.CLOSE);
					} else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
						webSettings.setDefaultZoom(ZoomDensity.FAR);
					} else if (mDensity == DisplayMetrics.DENSITY_TV) {
						webSettings.setDefaultZoom(ZoomDensity.FAR);
					} else {
						webSettings.setDefaultZoom(ZoomDensity.MEDIUM);
					}

					/**
					 * 用WebView显示图片，可使用这个参数 设置网页布局类型：
					 * 1、LayoutAlgorithm.NARROW_COLUMNS ： 适应内容大小
					 * 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
					 */
					String url = PubFunction.www + "api.php/page/show_goods_page?id=" + id;

					webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
					panelShopGoodInfoContent.loadUrl(url);
					id = jsonObject.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		};
		panelShopGoodInfoErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelShopGoodInfoUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};

		turnToLogin = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(activity, Login.class);
				intent.putExtra("type", "1");
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
		
		panelShopSubmitGoodSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelShopSubmitGoodErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
	}

	private void init() {
		dialog = Panel.progressDialog;
		preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		Intent intent = getIntent();
		this.id = intent.getStringExtra("id");

		new statusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);

		panelMineGoodInfoReturn = (LinearLayout) findViewById(R.id.panelMineGoodInfoReturn);
		panelMineGoodInfoReturn.setOnClickListener(this);
		panelShopGoodInfoBuy = (TextView) findViewById(R.id.panelShopGoodInfoBuy);
		panelShopGoodInfoBuy.setOnClickListener(this);
		panelShopGoodInfoTitle = (TextView) findViewById(R.id.panelShopGoodInfoTitle);
		panelShopGoodInfoContent = (WebView) findViewById(R.id.panelShopGoodInfoContent);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		count = (TextView) findViewById(R.id.count);
		add = (TextView) findViewById(R.id.add);
		add.setOnClickListener(this);
		reduce = (TextView) findViewById(R.id.reduce);
		reduce.setOnClickListener(this);
		reduce.setClickable(false);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(this)) {
			th = new HttpPanelShopGoodInfo(id, preferences, this);
			th.start();
			dialog.show();
		} else {
			Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (panelMineGoodInfoReturn.getId() == arg0.getId()) {
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		} else if (panelShopGoodInfoBuy.getId() == arg0.getId()) {
			String str = count.getText().toString().trim();
			if(str.equals("0")){
				Toast.makeText(getApplicationContext(),"订单数不能为零！",Toast.LENGTH_LONG).show();
			}else{
				if (PubFunction.isNetworkAvailable(this)) {
					th1 = new HttpPanelShopSubmitGood(id, preferences, this,count.getText().toString().trim());
					th1.start();
					dialog.show();
				} else {
					Toast.makeText(this, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		} else if (add.getId() == arg0.getId()) {
			reduce.setClickable(true);
			String str = count.getText().toString().trim();
			int i = Integer.parseInt(str);
			i++;
			count.setText(i + "");
		} else if (reduce.getId() == arg0.getId()) {
			String str = count.getText().toString().trim();
			int i = Integer.parseInt(str);
			i--;
			if (i == 0) {
				reduce.setClickable(false);
			}
			count.setText(i + "");
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null && manager != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
		return super.onTouchEvent(event);
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

class HttpPanelShopGoodInfo extends Thread {

	private String id;
	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Activity activity;

	public HttpPanelShopGoodInfo(String id, SharedPreferences preferences, Activity activity) {
		this.id = id;
		this.preferences = preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/shop/show_goods";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id + ""));
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
//				System.out.println(result.toString());
				JSONTokener jsonTokener = new JSONTokener(result);
//				System.out.println(jsonTokener.toString());
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageStr = jsonObject.getString("message");

				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("请重新登录")) {
							PanelShopGoodInfo.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							PanelShopGoodInfo.panelShopGoodInfoErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelShopGoodInfo.panelShopGoodInfoSuccessHandler.sendMessage(new Message());
					} else {
						PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (activity != null) {
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

class HttpPanelShopSubmitGood extends Thread {

	private String id;
	private SharedPreferences preferences;
	private Activity activity;
	private String count;

	public HttpPanelShopSubmitGood(String id, SharedPreferences preferences, Activity activity,String count) {
		this.id = id;
		this.preferences = preferences;
		this.activity = activity;
		this.count = count;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/shop/go_order";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("goods_id", id + ""));
		list.add(new BasicNameValuePair("nums", count + ""));
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
//				System.out.println(result.toString());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageStr = jsonObject.getString("message");

				System.out.println(jsonObject.toString());

				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("请重新登录")) {
							PanelShopGoodInfo.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelShopGoodInfo.panelShopSubmitGoodErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("message", messageStr);
						message.setData(bundle);
						PanelShopGoodInfo.panelShopSubmitGoodSuccessHandler.sendMessage(message);
					} else {
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("message", messageStr);
						message.setData(bundle);
						PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(message);
					}
				}

			}else{
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
				System.out.println(httpResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (activity != null) {
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}
}
