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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.a.a.a;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineCertifiedInfoAdd extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineCertifiedInfoAddReturn;
	private EditText panelMineCertifiedInfoAddCarID,
			panelMineCertifiedInfoAddBettaryID;
//	private InputMethodManager manager = null;
	private SharedPreferences preferences;
	public static Handler panelMineCertifiedInfoAddSuccessHandler, panelMineCertifiedInfoAddErrorHandler,
			panelMineCertifiedInfoAddUnknownHandler,turnToLogin;
	private TextView panelMineCertifiedInfoAddSubmit;
	private HttpPanelMineCertifiedInfoAdd th;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_certified_info_add, container, false);

		init();
		handler();

		return view;
	}

	private void handler() {
		panelMineCertifiedInfoAddSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "添加认证成功!", Toast.LENGTH_SHORT).show();
				PanelMineCertified panelMineCertified = new PanelMineCertified();
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelMinePanel, panelMineCertified);
				fragmentTransaction.commit();
				PubFunction.hideInput(getActivity(), view);
				progressDialog.dismiss();
			}
		};
		panelMineCertifiedInfoAddErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineCertifiedInfoAddUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误!", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		
		turnToLogin = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = getActivity().getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(getActivity(), Login_.class);
				intent.putExtra("type", "1");
				getActivity().startActivity(intent);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
				progressDialog.dismiss();
			}
		};
		
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		progressDialog = Panel.progressDialog;

		panelMineCertifiedInfoAddReturn = (LinearLayout) view.findViewById(R.id.panelMineCertifiedInfoAddReturn);
		panelMineCertifiedInfoAddReturn.setOnClickListener(this);
		panelMineCertifiedInfoAddSubmit = (TextView) view.findViewById(R.id.panelMineCertifiedInfoAddSubmit);
		panelMineCertifiedInfoAddSubmit.setOnClickListener(this);
//		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		panelMineCertifiedInfoAddCarID = (EditText) view.findViewById(R.id.panelMineCertifiedInfoAddCarID);
		panelMineCertifiedInfoAddBettaryID = (EditText) view.findViewById(R.id.panelMineCertifiedInfoAddBettaryID);
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineCertifiedInfoAddReturn.getId()) {
			PanelMineCertified panelMineCertified = new PanelMineCertified();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineCertified);
			fragmentTransaction.commit();
			PubFunction.hideInput(getActivity(), view);
		} else if (panelMineCertifiedInfoAddSubmit.getId() == arg0.getId()) {
			if (PubFunction.isNetworkAvailable(getActivity())) {
				th = new HttpPanelMineCertifiedInfoAdd(preferences,
						panelMineCertifiedInfoAddCarID.getText().toString().trim(),
						panelMineCertifiedInfoAddBettaryID.getText().toString().trim(),
						this, getActivity());
				th.start();
				progressDialog.show();
			} else {
				Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					PanelMineCertified panelMineCertified = new PanelMineCertified();
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, panelMineCertified);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
}

class HttpPanelMineCertifiedInfoAdd extends Thread {

	private SharedPreferences preferences;
	private String carID, bettaryID;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineCertifiedInfoAdd(SharedPreferences preferences, String carID, String bettaryID,Fragment fragment, Activity activity) {
		this.bettaryID = bettaryID;
		this.carID = carID;
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/set_my_car";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("frame_number", carID));
		list.add(new BasicNameValuePair("battery_number", bettaryID));
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

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineCertifiedInfoAdd.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineCertifiedInfoAdd.panelMineCertifiedInfoAddErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineCertifiedInfoAdd.panelMineCertifiedInfoAddSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineCertifiedInfoAdd.panelMineCertifiedInfoAddUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineCertifiedInfoAdd.panelMineCertifiedInfoAddUnknownHandler.sendMessage(new Message());
			}
		}
	}

}
