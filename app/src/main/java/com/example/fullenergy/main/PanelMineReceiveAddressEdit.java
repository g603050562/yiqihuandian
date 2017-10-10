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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.a.a.d;
import com.alipay.a.a.l;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
@SuppressLint("ValidFragment")
public class PanelMineReceiveAddressEdit extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineReceiveAddressEditReturn;
	public static Handler panelMineReceiveAddressEditSuccessHandler, panelMineReceiveAddressEditErrorHandler,
			panelMineReceiveAddressEditUnknownHandler,turnToLogin;
	private EditText panelMineReceiveAddressEditAddress, panelMineReceiveAddressEditName,
			panelMineReceiveAddressEditMobile;
	private TextView panelMineReceiveAddressEditSubmit;
	private SharedPreferences Preferences;
	private HttpPanelMineReceiveAddressEdit th;
	private ProgressDialog dialog;

	private int type;

	public PanelMineReceiveAddressEdit(){
	}

	public PanelMineReceiveAddressEdit(int type){
		this.type = type;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_receive_address_edit, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineReceiveAddressEditSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "修改成功！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				pageReturn();
			}
		};
		panelMineReceiveAddressEditErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelMineReceiveAddressEditUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
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
		Preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		dialog = Panel.progressDialog;

		panelMineReceiveAddressEditReturn = (LinearLayout) view.findViewById(R.id.panelMineReceiveAddressEditReturn);
		panelMineReceiveAddressEditReturn.setOnClickListener(this);
		panelMineReceiveAddressEditAddress = (EditText) view.findViewById(R.id.panelMineReceiveAddressEditAddress);
		panelMineReceiveAddressEditName = (EditText) view.findViewById(R.id.panelMineReceiveAddressEditName);
		panelMineReceiveAddressEditMobile = (EditText) view.findViewById(R.id.panelMineReceiveAddressEditMobile);
		panelMineReceiveAddressEditSubmit = (TextView) view.findViewById(R.id.panelMineReceiveAddressEditSubmit);
		panelMineReceiveAddressEditSubmit.setOnClickListener(this);
	}

	private void main() {

	}

	private void pageReturn(){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(type == 0){
			PanelMineReceiveAddress panelMineReceiveAddress = new PanelMineReceiveAddress();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineReceiveAddress);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}else if(type == 1){
			getActivity().finish();
			getActivity().overridePendingTransition(R.anim.in_left,R.anim.out_right);
		}
	}


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == panelMineReceiveAddressEditReturn.getId()) {
			pageReturn();
		} else if (panelMineReceiveAddressEditSubmit.getId() == arg0.getId()) {
			String name = "";
			String mobile = "";
			String address = "";
			name = panelMineReceiveAddressEditName.getText().toString().trim();
			mobile = panelMineReceiveAddressEditMobile.getText().toString().trim();
			address = panelMineReceiveAddressEditAddress.getText().toString().trim();
			if (name.equals("") || mobile.equals("") || address.equals("")) {
				Toast.makeText(getActivity(), "填写内容不能为空!", Toast.LENGTH_SHORT).show();
			} else {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					th = new HttpPanelMineReceiveAddressEdit(Preferences, name, mobile, address, this, getActivity());
					th.start();
					dialog.show();
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
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
					pageReturn();
					return true;
				}
				return false;
			}
		});
	}
}

class HttpPanelMineReceiveAddressEdit extends Thread {

	private SharedPreferences preferences;
	private String name, mobile, address;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineReceiveAddressEdit(SharedPreferences preferences, String name, String mobile, String address,
			Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.address = address;
		this.mobile = mobile;
		this.name = name;
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
		String path = PubFunction.www + "api.php/member/update_address";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("name", this.name));
		list.add(new BasicNameValuePair("mobile", this.mobile));
		list.add(new BasicNameValuePair("address", this.address));
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
							PanelMineReceiveAddressEdit.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineReceiveAddressEdit.panelMineReceiveAddressEditErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineReceiveAddressEdit.panelMineReceiveAddressEditSuccessHandler
								.sendMessage(new Message());
					} else {
						PanelMineReceiveAddressEdit.panelMineReceiveAddressEditUnknownHandler
								.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.out.println(e.toString());
				PanelMineReceiveAddressEdit.panelMineReceiveAddressEditUnknownHandler.sendMessage(new Message());
			}
		}
	}
}
