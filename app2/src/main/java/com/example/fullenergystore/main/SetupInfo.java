package com.example.fullenergystore.main;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;

public class SetupInfo extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelInfoIndexEdit,panelInforIndexReturn;
	private SharedPreferences preferences;
	public static Handler panelInfoIndexSuccessHandler, panelInfoIndexErrorHandler, panelInfoIndexUnknownHandler,
			turnToLogin;
	private HttpPanelInfoIndex th;
	private TextView panelInfoIndexName, panelInfoIndexCompanyName, panelInfoIndexPhone, panelInfoIndexAdress;
	private ProgressDialog progressDialog;

	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_info_index, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelInfoIndexSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					String companyName = jsonObject.getString("companyname");
					panelInfoIndexCompanyName.setText(companyName);
					String Name = jsonObject.getString("nickname");
					panelInfoIndexName.setText(Name);
					String address = jsonObject.getString("address");
					panelInfoIndexAdress.setText(address);
					String phone = jsonObject.getString("mobile");
					panelInfoIndexPhone.setText(phone);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				progressDialog.dismiss();
			}
		};
		panelInfoIndexErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelInfoIndexUnknownHandler = new Handler() {
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
		panelInfoIndexEdit = (LinearLayout) view.findViewById(R.id.panelInfoIndexEdit);
		panelInfoIndexEdit.setOnClickListener(this);

		panelInfoIndexName = (TextView) view.findViewById(R.id.panelInfoIndexName);
		panelInfoIndexCompanyName = (TextView) view.findViewById(R.id.panelInfoIndexCompanyName);
		panelInfoIndexPhone = (TextView) view.findViewById(R.id.panelInfoIndexPhone);
		panelInfoIndexAdress = (TextView) view.findViewById(R.id.panelInfoIndexAdress);
		panelInforIndexReturn = (LinearLayout) view.findViewById(R.id.panelInforIndexReturn);
		panelInforIndexReturn.setOnClickListener(this);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {

			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelInfoIndex(preferences, fragement, getActivity());
			th.start();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (panelInfoIndexEdit.getId() == arg0.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupInfoEdit panelInfoIndexEdit = new SetupInfoEdit();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelInfoIndexEdit);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}else if(panelInforIndexReturn.getId() == arg0.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupIndex panelInfoIndexEdit = new SetupIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelInfoIndexEdit);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
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
					SetupIndex panelInfoIndexEdit = new SetupIndex();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelSetupPanel, panelInfoIndexEdit);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}

}

class HttpPanelInfoIndex extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelInfoIndex(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.activity = activity;
		this.fragment = fragment;
	}

	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/Home/index_bus";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);
		try {
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
							SetupInfo.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							SetupInfo.panelInfoIndexErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						SetupInfo.panelInfoIndexSuccessHandler.sendMessage(new Message());
					} else {
						SetupInfo.panelInfoIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				SetupInfo.panelInfoIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}