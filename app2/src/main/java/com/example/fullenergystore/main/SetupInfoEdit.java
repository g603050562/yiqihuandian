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
import org.json.JSONArray;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
import android.view.View.OnClickListener;

public class SetupInfoEdit extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelInforIndexEditReturn,focus;
	private TextView panelInfoIndexEditSubmit;
	private EditText panelInfoIndexEditCompany, panelInfoIndexEditAddress;
	public static Handler panelInfoIndexEditSuccessHandler, panelInfoIndexEditErrorHandler, turnToLogin, panelInfoIndexEditUnknownHandler;
	private ProgressDialog progressDialog;
	private HttpPanelInfoIndexEditSubmit th;
	private SharedPreferences preferences;

	private Fragment fragement;
	private InputMethodManager manager = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_info_index_edit, container, false);
		fragement = this;

		init();
		handler();

		return view;
	}

	private void handler() {
		panelInfoIndexEditSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				SetupInfo panelInfoIndex = new SetupInfo();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelInfoPanel, panelInfoIndex);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
		};
		panelInfoIndexEditErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelInfoIndexEditUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "未知错误!", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
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

		panelInforIndexEditReturn = (LinearLayout) view.findViewById(R.id.panelInforIndexEditReturn);
		panelInforIndexEditReturn.setOnClickListener(this);

		panelInfoIndexEditCompany = (EditText) view.findViewById(R.id.panelInfoIndexEditCompany);
		panelInfoIndexEditCompany.setOnKeyListener(onKeyListener);
		panelInfoIndexEditAddress = (EditText) view.findViewById(R.id.panelInfoIndexEditAddress);
		panelInfoIndexEditAddress.setOnKeyListener(onKeyListener);

		panelInfoIndexEditSubmit = (TextView) view.findViewById(R.id.panelInfoIndexEditSubmit);
		panelInfoIndexEditSubmit.setOnClickListener(this);

		focus = (LinearLayout) view.findViewById(R.id.focus);
		focus.setOnKeyListener(onKeyListener);
		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void onClick(View arg0) {
		if (panelInforIndexEditReturn.getId() == arg0.getId()) {

			manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			focus.requestFocus();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupInfo panelInfoIndex = new SetupInfo();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelInfoIndex);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelInfoIndexEditSubmit.getId() == arg0.getId()) {
			String company = "";
			String address = "";
			company = panelInfoIndexEditCompany.getText().toString().trim();
			address = panelInfoIndexEditAddress.getText().toString().trim();
			if (company.equals("") || address.equals("")) {
				Toast.makeText(getActivity(), "输入信息不能为空！", Toast.LENGTH_SHORT).show();
			} else {
				th = new HttpPanelInfoIndexEditSubmit(preferences, company, address, getActivity(), fragement);
				th.start();
				progressDialog = new ProgressDialog(getActivity());
				progressDialog.show();
			}
		} else{
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
				SetupInfo panelInfoIndex = new SetupInfo();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelSetupPanel, panelInfoIndex);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				return true;
			}
			return false;
		}
	};
}

class HttpPanelInfoIndexEditSubmit extends Thread {

	private SharedPreferences preferences;
	private String company;
	private String address;

	private JSONArray array;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelInfoIndexEditSubmit(SharedPreferences preferences, String company, String address,
			Activity activity, Fragment fragment) {
		this.company = company;
		this.preferences = preferences;
		this.address = address;
		this.activity = activity;
		this.fragment = fragment;
	}

	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/Home/set_my_name";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("companyname", company));
		list.add(new BasicNameValuePair("address", address));
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

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("请重新登录")) {
							SetupInfoEdit.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							SetupInfoEdit.panelInfoIndexEditErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.array = jsonObject.getJSONArray("data");
						SetupInfoEdit.panelInfoIndexEditSuccessHandler.sendMessage(new Message());
					} else {
						SetupInfoEdit.panelInfoIndexEditUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				SetupInfoEdit.panelInfoIndexEditUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return array;
	}
}
