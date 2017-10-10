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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineTransactionPassword extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineTransactionPasswordReturn;
	private TextView panelMineTransactionPasswordSubmit;
	private EditText panelMineTransactionPasswordPwd, panelMineTransactionPasswordPwdRe;
	private InputMethodManager manager = null;
	private SharedPreferences preferences;
	public static Handler panelMineTransactionPasswordSuccessHandler, panelMineTransactionPasswordErrorHandler,
			panelMineTransactionPasswordUnknownHandler,turnToLogin;
	private ProgressDialog dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_transaction_password, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineTransactionPasswordUnknownHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误!", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelMineTransactionPasswordSuccessHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("jy_password", msg.getData().getString("jy_password"));
				editor.commit();

				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				PanelMineIndex setup = new PanelMineIndex();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelMinePanel, setup);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				dialog.dismiss();
			}
		};
		panelMineTransactionPasswordErrorHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
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
			}
		};
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		dialog = Panel.progressDialog;

		panelMineTransactionPasswordReturn = (LinearLayout) view.findViewById(R.id.panelMineTransactionPasswordReturn);
		panelMineTransactionPasswordReturn.setOnClickListener(this);
		panelMineTransactionPasswordSubmit = (TextView) view.findViewById(R.id.panelMineTransactionPasswordSubmit);
		panelMineTransactionPasswordSubmit.setOnClickListener(this);
		panelMineTransactionPasswordPwd = (EditText) view.findViewById(R.id.panelMineTransactionPasswordPwd);
		panelMineTransactionPasswordPwdRe = (EditText) view.findViewById(R.id.panelMineTransactionPasswordPwdRe);
		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void main() {

	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineTransactionPasswordReturn.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineIndex setup = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, setup);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineTransactionPasswordSubmit.getId() == arg0.getId()) {
			if (panelMineTransactionPasswordPwd.getText().toString()
					.equals(panelMineTransactionPasswordPwdRe.getText().toString())) {
				if (panelMineTransactionPasswordPwd.getText().toString().equals("")) {
					Toast.makeText(getActivity(), "输入密码不能为空！", Toast.LENGTH_SHORT).show();
				} else {
					if (PubFunction.isNetworkAvailable(getActivity())) {
						HttpPanelMineTransactionPasswordSubmit th = new HttpPanelMineTransactionPasswordSubmit(
								panelMineTransactionPasswordPwd.getText().toString(), preferences, this, getActivity());
						th.start();
						dialog.show();
					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				Toast.makeText(getActivity(), "两次输入密码不一致!", Toast.LENGTH_SHORT).show();
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
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					PanelMineIndex setup = new PanelMineIndex();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, setup);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}

class HttpPanelMineTransactionPasswordSubmit extends Thread {

	private String password;
	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineTransactionPasswordSubmit(String password, SharedPreferences preferences, Fragment fragment,
			Activity activity) {
		this.password = password;
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
		String path = PubFunction.www + "api.php/member/set_jy_password";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("jy_password", password));
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
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				bundle.putString("jy_password", password);
				message.setData(bundle);
				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineTransactionPassword.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineTransactionPassword.panelMineTransactionPasswordErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineTransactionPassword.panelMineTransactionPasswordSuccessHandler.sendMessage(message);
					} else {
						PanelMineTransactionPassword.panelMineTransactionPasswordUnknownHandler
								.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineTransactionPassword.panelMineTransactionPasswordUnknownHandler.sendMessage(new Message());
			}
		}
	}
}