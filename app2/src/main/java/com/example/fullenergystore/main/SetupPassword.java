package com.example.fullenergystore.main;

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.PubFunction;

public class SetupPassword extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelPasswordReturn,focus;
	private EditText panelSetupPasswordOld, panelSetupPasswordNewRe, panelSetupPasswordNew;
	private TextView panelSetupPasswordSubmit;
	private SharedPreferences preferences;
	public static Handler panelSetupPasswordSuccessHandler,panelSetupPasswordErrorHandler,panelSetupPasswordUnknownHandler, turnToLogin;
	private HttpPanelSetupPassword th;

	private Fragment fragement;
	private FrameLayout fragement_panel;
	private InputMethodManager manager = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_setup_password, container, false);
		fragement = this;

		init();
		handler();

		return view;
	}

	private void handler() {
		panelSetupPasswordSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("apibus_businessid", null);
				editor.putString("apibus_username", null);
				editor.commit();
				startActivity(new Intent(getActivity(), Login.class));
				getActivity().finish();
			}
		};
		panelSetupPasswordErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
			}
		};
		panelSetupPasswordUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
			}
		};
		turnToLogin = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(getActivity(), Login.class);
				intent.putExtra("type", "1");
				getActivity().startActivity(intent);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

		panelPasswordReturn = (LinearLayout) view.findViewById(R.id.panelPasswordReturn);
		panelPasswordReturn.setOnClickListener(this);
		panelSetupPasswordOld = (EditText) view.findViewById(R.id.panelSetupPasswordOld);
		panelSetupPasswordOld.setOnKeyListener(onKeyListener);
		panelSetupPasswordNewRe = (EditText) view.findViewById(R.id.panelSetupPasswordNewRe);
		panelSetupPasswordNewRe.setOnKeyListener(onKeyListener);
		panelSetupPasswordNew = (EditText) view.findViewById(R.id.panelSetupPasswordNew);
		panelSetupPasswordNew.setOnKeyListener(onKeyListener);
		panelSetupPasswordSubmit = (TextView) view.findViewById(R.id.panelSetupPasswordSubmit);
		panelSetupPasswordSubmit.setOnClickListener(this);

		fragement_panel = (FrameLayout) view.findViewById(R.id.fragement_panel);
		fragement_panel.setOnClickListener(this);
		focus = (LinearLayout) view.findViewById(R.id.focus);
		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void onClick(View arg0) {
		if (panelPasswordReturn.getId() == arg0.getId()) {

			manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			focus.requestFocus();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupIndex panelSetupIndex = new SetupIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelSetupIndex);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelSetupPasswordSubmit.getId() == arg0.getId()) {
			String old = "";
			String NewRe = "";
			String New = "";
			old = panelSetupPasswordOld.getText().toString().trim();
			NewRe = panelSetupPasswordNewRe.getText().toString().trim();
			New = panelSetupPasswordNew.getText().toString().trim();
			if (old.equals("") || NewRe.equals("") || New.equals("")) {
				Toast.makeText(getActivity(), "输入信息不能为空！", Toast.LENGTH_SHORT).show();
				panelSetupPasswordOld.setText("");
				panelSetupPasswordNewRe.setText("");
				panelSetupPasswordNew.setText("");
			} else {
				if (!NewRe.equals(New)) {
					Toast.makeText(getActivity(), "新密码输入不相同，请重新输入！", Toast.LENGTH_SHORT).show();
					panelSetupPasswordOld.setText("");
					panelSetupPasswordNewRe.setText("");
					panelSetupPasswordNew.setText("");
				} else {
					th = new HttpPanelSetupPassword(preferences, old, New, getActivity(), fragement);
					th.start();
				}
			}
		}else{
			manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			focus.requestFocus();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(onKeyListener);
	}

	private View.OnKeyListener onKeyListener = new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				SetupIndex panelSetupIndex = new SetupIndex();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelSetupPanel, panelSetupIndex);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				return true;
			}
			return false;
		}
	};
}

class HttpPanelSetupPassword extends Thread {

	private SharedPreferences Preferences;
	private String old;
	private String New;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelSetupPassword(SharedPreferences Preferences, String old, String New, Activity activity,
			Fragment fragment) {
		this.Preferences = Preferences;
		this.old = old;
		this.New = New;
		this.activity = activity;
		this.fragment = fragment;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String apibus_businessid = Preferences.getString("apibus_businessid", null);
		String apibus_username = Preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/Home/update_password";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("old_password", old));
		list.add(new BasicNameValuePair("new_password", New));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageString = jsonObject.getString("message");
				String code = jsonObject.getString("code");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageString.equals("请重新登录")) {
							SetupPassword.turnToLogin.sendMessage(new Message());
						} else {
							SetupPassword.panelSetupPasswordErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						SetupPassword.panelSetupPasswordSuccessHandler.sendMessage(message);
					} else {
						SetupPassword.panelSetupPasswordUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				SetupPassword.panelSetupPasswordUnknownHandler.sendMessage(new Message());
			}
		}
	}
}