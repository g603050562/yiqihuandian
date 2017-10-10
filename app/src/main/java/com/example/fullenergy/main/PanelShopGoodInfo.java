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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergy.pub.ProgressDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tandong.bottomview.view.BottomView;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.PubFunction;

public class PanelShopGoodInfo extends Activity implements OnClickListener {

	private LinearLayout panelMineGoodInfoReturn;
	private TextView  panelShopGoodInfoTitle, panelShopGoodInfoPrice;
	private LinearLayout panelShopGoodInfoBuy;
	private WebView panelShopGoodInfoContent;
	private InputMethodManager manager = null;
	public static Handler panelShopGoodInfoSuccessHandler, panelShopGoodInfoErrorHandler,
			panelShopGoodInfoUnknownHandler, panelShopGoodInfoGetAddressErrorHandler,
			panelShopGoodInfoGetAddressSuccessHandler, PanelShopGoodInfoUploadAddressSuccessHandler,
			PanelShopGoodInfoUploadAddressErrorHandler,turnToLogin;
	private HttpPanelShopGoodInfo th;
	private SharedPreferences preferences;

	private int IS_BOTTOM_VIEW = 0;
	private String id;
	private String name, content, price;
	private String not_address;
	private Activity activity;


	private ProgressDialog progressDialog;

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
//				System.out.println(jsonObject.toString());
				try {
					name = jsonObject.getString("goodsname");
					content = jsonObject.getString("content");
					price = jsonObject.getString("price");
					panelShopGoodInfoTitle.setText(name);
					panelShopGoodInfoPrice.setText("¥" + price);
					// panelShopGoodInfoContent.setText(content);
					panelShopGoodInfoContent.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
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
					panelShopGoodInfoContent.setWebViewClient(new WebViewClient(){
						public void onPageFinished(WebView view, String url) {
							super.onPageFinished(view, url);
							//隐藏loading框
							if(progressDialog!=null){
								progressDialog.dismiss();
							}
						};
					});

					/**
					 * 用WebView显示图片，可使用这个参数 设置网页布局类型：
					 * 1、LayoutAlgorithm.NARROW_COLUMNS ： 适应内容大小
					 * 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
					 */
					String url = PubFunction.www + "api.php/page/show_goods_page?id=" + id;

					webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
					panelShopGoodInfoContent.loadUrl(url);

					not_address = jsonObject.getString("not_address");
					id = jsonObject.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		panelShopGoodInfoErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		};
		panelShopGoodInfoUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
			}
		};
		
		turnToLogin = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = activity.getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(activity, Login_.class);
				intent.putExtra("type", "1");
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};

		panelShopGoodInfoGetAddressErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final BottomView bottomView = new BottomView(PanelShopGoodInfo.this, R.style.BottomViewTheme_Defalut,
						R.layout.panel_shop_good_info_user_info);
				bottomView.setAnimation(R.style.popwin_anim_style);
				bottomView.showBottomView(true);
				final View bottomPanelView = bottomView.getView();

				LinearLayout panelShopGoodInfoUserInfoPanel = (LinearLayout) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoPanel);
				final LinearLayout panelShopGoodInfoUserInfoFocus = (LinearLayout) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoFocus);
				panelShopGoodInfoUserInfoPanel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						manager.hideSoftInputFromWindow(bottomPanelView.getApplicationWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
						panelShopGoodInfoUserInfoFocus.requestFocus();
					}
				});

				final EditText editTextName = (EditText) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoSubmitName);
				final EditText editTextPhone = (EditText) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoSubmitPhone);
				final EditText editTextAddress = (EditText) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoSubmitAddress);

				TextView panelShopGoodInfoUserInfoSubmit = (TextView) bottomPanelView
						.findViewById(R.id.panelShopGoodInfoUserInfoSubmit);
				panelShopGoodInfoUserInfoSubmit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (PubFunction.isNetworkAvailable(PanelShopGoodInfo.this)) {
							HttpPanelShopGoodInfoUploadAddress th = new HttpPanelShopGoodInfoUploadAddress(preferences,
									editTextName.getText().toString().trim(), editTextPhone.getText().toString().trim(),
									editTextAddress.getText().toString().trim(), activity);
							th.start();
						} else {
							Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
						}

					}
				});

				PanelShopGoodInfoUploadAddressSuccessHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);
						Toast.makeText(getApplicationContext(), "地址设置成功！", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(PanelShopGoodInfo.this, PanelShopPayZhiFuBao.class);
						intent.putExtra("name", name);
						intent.putExtra("price", price);
						intent.putExtra("content", content);
						intent.putExtra("not_address", not_address);
						intent.putExtra("id", id);
						startActivity(intent);
						bottomView.dismissBottomView();
					}
				};
				PanelShopGoodInfoUploadAddressErrorHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);
						String message = msg.getData().getString("message");
						Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				};
			}
		};

		panelShopGoodInfoGetAddressSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Intent intent = new Intent(PanelShopGoodInfo.this, PanelShopPayZhiFuBao.class);
				intent.putExtra("name", name);
				intent.putExtra("price", price);
				intent.putExtra("content", content);
				intent.putExtra("not_address", not_address);
				intent.putExtra("id", id);
				startActivity(intent);
				overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
	}

	private void init() {
		progressDialog = new ProgressDialog(activity);
		
		preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		Intent intent = getIntent();
		this.id = intent.getStringExtra("id");

		new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0x00000000);

		panelMineGoodInfoReturn = (LinearLayout) findViewById(R.id.page_return);
		panelMineGoodInfoReturn.setOnClickListener(this);
		panelShopGoodInfoBuy = (LinearLayout) findViewById(R.id.panelShopGoodInfoBuy);
		panelShopGoodInfoBuy.setOnClickListener(this);
		panelShopGoodInfoTitle = (TextView) findViewById(R.id.panelShopGoodInfoTitle);
		panelShopGoodInfoContent = (WebView) findViewById(R.id.panelShopGoodInfoContent);
		panelShopGoodInfoPrice = (TextView) findViewById(R.id.panelShopGoodInfoPrice);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

	}

	private void main() {
		if (PubFunction.isNetworkAvailable(this)) {
			th = new HttpPanelShopGoodInfo(id, preferences, this);
			th.start();
			progressDialog.show();
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
			if (PubFunction.isNetworkAvailable(this)) {
				HttpPanelShopGoodInfoGetAddress th = new HttpPanelShopGoodInfoGetAddress(preferences, this);
				th.start();
			} else {
				Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
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
			System.gc();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(progressDialog!=null){
		}
		if(panelShopGoodInfoContent!=null){
			panelShopGoodInfoContent = null;
		}
		
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
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/shop/show_goods";
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
				String code = jsonObject.getString("code");
				String messageStr = jsonObject.getString("message");

				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
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
			if (activity != null) {
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

class HttpPanelShopGoodInfoGetAddress extends Thread {

	SharedPreferences preferences;
	private Activity activity;

	public HttpPanelShopGoodInfoGetAddress(SharedPreferences preferences, Activity activity) {
		this.preferences = preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/my_address";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				message.setData(bundle);
				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelShopGoodInfo.turnToLogin.sendMessage(new Message());
						} else {
							PanelShopGoodInfo.panelShopGoodInfoGetAddressErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelShopGoodInfo.panelShopGoodInfoGetAddressSuccessHandler.sendMessage(message);
					} else {
						PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (activity != null) {
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}
}

class HttpPanelShopGoodInfoUploadAddress extends Thread {

	private SharedPreferences preferences;
	private String name = "";
	private String mobile = "";
	private String address = "";
	private Activity activity;

	public HttpPanelShopGoodInfoUploadAddress(SharedPreferences preferences, String name, String mobile, String address,
			Activity activity) {
		this.address = address;
		this.name = name;
		this.mobile = mobile;
		this.preferences = preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/set_address";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("name", name));
		list.add(new BasicNameValuePair("mobile", mobile));
		list.add(new BasicNameValuePair("address", address));
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");

				if (activity != null) {
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("message", messageStr);
					message.setData(bundle);
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelShopGoodInfo.turnToLogin.sendMessage(new Message());
						} else {
							PanelShopGoodInfo.PanelShopGoodInfoUploadAddressErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelShopGoodInfo.PanelShopGoodInfoUploadAddressSuccessHandler.sendMessage(message);
					} else {
						PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (activity != null) {
				PanelShopGoodInfo.panelShopGoodInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}
}