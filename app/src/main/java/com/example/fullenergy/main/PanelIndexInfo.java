package com.example.fullenergy.main;


import java.math.BigDecimal;
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

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;


public class PanelIndexInfo extends Activity implements OnClickListener{

	private int id;
	private Activity view;
	private LinearLayout panelIndexInfoReturn;
	private LinearLayout panelIndexInfoNavi;
	private SharedPreferences Preferences;
	private TextView panelIndexInfoCompanyName,panelIndexInfoMobile,panelIndexInfoAddress,panelIndexInfoDes;
	public static Handler panelIndexInfoSuccessHandler,panelIndexInfoErrorHandler,panelIndexInfoUnknownHandler,turnToLogin;
	private HttpPanelIndexInfo th = null;
	private ProgressDialog dialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panel_index_info);
		view = this;
		
		init();
		handler();
		main();
		
	}
	
	private void handler() {
		panelIndexInfoUnknownHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "出现未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelIndexInfoSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					String companyname = jsonObject.getString("companyname");
					String mobile = jsonObject.getString("mobile");
					String address = jsonObject.getString("address");
					if(jsonObject.has("description")){
						String description = jsonObject.getString("description");
						panelIndexInfoDes.setText(description);
					}else{
						panelIndexInfoDes.setText("暂无描述！");
					}
					panelIndexInfoCompanyName.setText(companyname.toString().trim());
					panelIndexInfoMobile.setText(mobile.toString().trim());
					panelIndexInfoAddress.setText(address.toString().trim());
					TextView panelIndexInfoLong = (TextView) view.findViewById(R.id.panelIndexInfoLong);
					LatLng start = new LatLng(PubFunction.local[0],PubFunction.local[1]);
					LatLng end = new LatLng(PubFunction.marker[0],PubFunction.marker[1]);
					float distance = AMapUtils.calculateLineDistance(start, end);
					distance = distance / 1000 ;
					BigDecimal b = new BigDecimal(distance); 
					double f1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
					panelIndexInfoLong.setText(f1+"");
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		};
		panelIndexInfoErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
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

	private void init(){
		Preferences = getApplicationContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		Intent intent = getIntent();
		this.id = Integer.parseInt(intent.getStringExtra("id"));
		
		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);
		dialog = Panel.progressDialog;

		
		panelIndexInfoReturn = (LinearLayout) view.findViewById(R.id.panelIndexInfoReturn);
		panelIndexInfoReturn.setOnClickListener(this);
		panelIndexInfoCompanyName = (TextView) view.findViewById(R.id.panelIndexInfoCompanyName);
		panelIndexInfoMobile = (TextView) view.findViewById(R.id.panelIndexInfoMobile);
		panelIndexInfoAddress = (TextView) view.findViewById(R.id.panelIndexInfoAddress);
		panelIndexInfoNavi = (LinearLayout) view.findViewById(R.id.panelIndexInfoNavi);
		panelIndexInfoDes = (TextView) view.findViewById(R.id.panelIndexInfoDes);
		panelIndexInfoNavi.setOnClickListener(this);
	}
	
	private void main(){
		if(PubFunction.isNetworkAvailable(this)){
			th = new HttpPanelIndexInfo(id,Preferences,this);
			th.start();
			dialog.show();
		}else{
			Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if(panelIndexInfoReturn.getId() == arg0.getId()){
			this.finish();
			overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}else if(panelIndexInfoNavi.getId() == arg0.getId()){
			Intent intent = new Intent(getApplicationContext(),PanelIndexNavigation.class);
			startActivity(intent);
			overridePendingTransition(R.anim.in_right, R.anim.out_left);
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
		dialog.dismiss();
	}
	
}

class HttpPanelIndexInfo extends Thread{
	
	private JSONObject jsonObject;
	private int id;
	private SharedPreferences Preferences;
	private Activity activity;
	
	public HttpPanelIndexInfo(int id,SharedPreferences Preferences,Activity activity) {
		this.id = id;
		this.Preferences = Preferences;
		this.activity = activity;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String api_userid = Preferences.getString("api_userid", null);
		String api_username = Preferences.getString("api_username", null);
		String path = PubFunction.www+"api.php/home/show_cabinet";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id+""));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String code = jsonObject.getString("code");
				String messageStr = jsonObject.getString("message");
				
				
				if(activity!=null){
					if(code.equals("200")){
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelIndexInfo.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message",messageStr);
							message.setData(bundle);
							PanelIndexInfo.panelIndexInfoErrorHandler.sendMessage(message);
						}
						
					}else if(code.equals("100")){
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelIndexInfo.panelIndexInfoSuccessHandler.sendMessage(new Message());
					}else{
						PanelIndexInfo.panelIndexInfoUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if(activity!=null){
				PanelIndexInfo.panelIndexInfoUnknownHandler.sendMessage(new Message());
			}
		}
	}
	
	public JSONObject getResult(){
		return jsonObject;
	}
}
