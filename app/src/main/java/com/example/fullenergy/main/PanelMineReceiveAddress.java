package com.example.fullenergy.main;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.a.a.d;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineReceiveAddress extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineReceiveAddressReturn;
	public static Handler panelMineReceiveAddressSuccessHandler, panelMineReceiveAddressErrorHandler,
			panelMineReceiveAddressUnknownHandler, panelMineReceiveAddressEditHandler,turnToLogin;
	private TextView panelMineReceiveAddressAddress, panelMineReceiveAddressName, panelMineReceiveAddressPhone,
			panelMineReceiveAddressEdit;
	private SharedPreferences Preferences;
	private HttpPanelMineReceiveAddress th;
	private ProgressDialog dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_receive_address, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineReceiveAddressSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					String name = jsonObject.getString("name");
					String mobile = jsonObject.getString("mobile");
					String address = jsonObject.getString("address");
					panelMineReceiveAddressName.setText(name.toString());
					panelMineReceiveAddressAddress.setText(address.toString());
					panelMineReceiveAddressPhone.setText(mobile.toString());
					dialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		panelMineReceiveAddressErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelMineReceiveAddressUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};

		panelMineReceiveAddressEditHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "请先设置收货地址！", Toast.LENGTH_SHORT).show();
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
		dialog = Panel.progressDialog;

		panelMineReceiveAddressReturn = (LinearLayout) view.findViewById(R.id.panelMineReceiveAddressReturn);
		panelMineReceiveAddressReturn.setOnClickListener(this);
		panelMineReceiveAddressAddress = (TextView) view.findViewById(R.id.panelMineReceiveAddressAddress);
		panelMineReceiveAddressName = (TextView) view.findViewById(R.id.panelMineReceiveAddressName);
		panelMineReceiveAddressPhone = (TextView) view.findViewById(R.id.panelMineReceiveAddressPhone);
		panelMineReceiveAddressEdit = (TextView) view.findViewById(R.id.panelMineReceiveAddressEdit);
		panelMineReceiveAddressEdit.setOnClickListener(this);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			dialog.show();
			Preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelMineReceiveAddress(Preferences, this, getActivity());
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (arg0.getId() == panelMineReceiveAddressReturn.getId()) {
			PanelMineIndex index = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, index);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineReceiveAddressEdit.getId() == arg0.getId()) {
			PanelMineReceiveAddressEdit panelMineReceiveAddressEdit = new PanelMineReceiveAddressEdit(0);
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineReceiveAddressEdit);
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
					PanelMineIndex index = new PanelMineIndex();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, index);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
}

class HttpPanelMineReceiveAddress extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineReceiveAddress(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/my_address";
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

				if (code.equals("200")) {
					if (messageStr.equals("秘钥不正确,请重新登录")) {
						PanelMineReceiveAddress.turnToLogin.sendMessage(new Message());
					} else {
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("message", messageStr);
						message.setData(bundle);
						PanelMineReceiveAddress.panelMineReceiveAddressErrorHandler.sendMessage(message);
					}
				} else if (code.equals("100")) {
					if (fragment.getView() != null) {
						if (jsonObject.getString("data").equals("[]")) {
							PanelMineReceiveAddress.panelMineReceiveAddressEditHandler.sendMessage(new Message());
						} else {
							this.jsonObject = jsonObject.getJSONObject("data");
							PanelMineReceiveAddress.panelMineReceiveAddressSuccessHandler.sendMessage(new Message());
						}
					}
				} else {
					PanelMineReceiveAddress.panelMineReceiveAddressUnknownHandler.sendMessage(new Message());
				}

			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.out.println(e.toString());
				PanelMineReceiveAddress.panelMineReceiveAddressUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
