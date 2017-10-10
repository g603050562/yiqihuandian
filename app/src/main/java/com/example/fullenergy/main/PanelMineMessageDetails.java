package com.example.fullenergy.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineMessageDetails extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineMessageDetailReturn;
	private String id = "";
	private SharedPreferences preferences;
	public static Handler panelMineMessageDetailsSuccessHandler, panelMineMessageDetailsErrorHandler,
			panelMineMessageDetailsUnknownHandler,turnToLogin;
	private HttpPanelMineMessageDetails th;

	private ProgressDialog progressDialog;

	public PanelMineMessageDetails() {
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("ValidFragment")
	public PanelMineMessageDetails(String id) {
		this.id = id;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_message_detail, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		// TODO Auto-generated method stub
		panelMineMessageDetailsSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				TextView panelMineMessageDetailTitle = (TextView) view.findViewById(R.id.panelMineMessageDetailTitle);
				TextView panelMineMessageDetailContent = (TextView) view
						.findViewById(R.id.panelMineMessageDetailContent);
				TextView panelMineMessageDetailDate = (TextView) view.findViewById(R.id.panelMineMessageDetailDate);
				try {
					panelMineMessageDetailTitle.setText(jsonObject.getString("title"));
					panelMineMessageDetailContent.setText(jsonObject.getString("content"));
					Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String dateTime = df.format(date);
					panelMineMessageDetailDate.setText(dateTime);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Panel.httpChangeCount.sendMessage(new Message());
				progressDialog.dismiss();
			}
		};
		panelMineMessageDetailsErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineMessageDetailsUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
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

		progressDialog = Panel.progressDialog;

		panelMineMessageDetailReturn = (LinearLayout) view.findViewById(R.id.panelMineMessageDetailReturn);
		panelMineMessageDetailReturn.setOnClickListener(this);
	}

	private void main() {
		
		if (PubFunction.isNetworkAvailable(getActivity())) {
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelMineMessageDetails(preferences, id, this, getActivity());
			th.start();
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineMessageDetailReturn.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineMessage panelMineMessage = new PanelMineMessage();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineMessage);
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
					PanelMineMessage panelMineMessage = new PanelMineMessage();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, panelMineMessage);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}

}

class HttpPanelMineMessageDetails extends Thread {

	private SharedPreferences preferences;
	private String id;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineMessageDetails(SharedPreferences preferences, String id, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.id = id;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);

		String path = PubFunction.www + "api.php/member/my_message_show";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id));
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
							PanelMineMessageDetails.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineMessageDetails.panelMineMessageDetailsErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelMineMessageDetails.panelMineMessageDetailsSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineMessageDetails.panelMineMessageDetailsUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			// System.out.println(e.toString());
			if (fragment.getView() != null) {
				PanelMineMessageDetails.panelMineMessageDetailsUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}