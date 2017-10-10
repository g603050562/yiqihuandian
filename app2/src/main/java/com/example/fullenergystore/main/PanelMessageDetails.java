package com.example.fullenergystore.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;

@SuppressLint("ValidFragment")
public class PanelMessageDetails extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMessageDetailReturn;
	private String id;
	private TextView panelMessageDetailTitle, panelMessageDetailDate, panelMessageDetailContent;
	public static Handler panelMessageDetailsSuccessHandler, panelMessageDetailsErrorHandler, turnToLogin,
			panelMessageDetailsUnknownHandler;
	private HttpPanelMessageDetails th;
	private SharedPreferences preferences;
	private ProgressDialog progressDialog;

	private Fragment fragement;

	public PanelMessageDetails() {
		// TODO Auto-generated constructor stub
	}

	public PanelMessageDetails(String id) {
		this.id = id;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_message_detail, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		// TODO Auto-generated method stub
		panelMessageDetailsSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					panelMessageDetailTitle.setText(jsonObject.getString("title"));
					Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					String dateTime = df.format(date);
					panelMessageDetailDate.setText(dateTime);
					panelMessageDetailContent.setText(jsonObject.getString("content"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				progressDialog.dismiss();
			}
		};
		panelMessageDetailsErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
		panelMessageDetailsUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
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
		panelMessageDetailReturn = (LinearLayout) view.findViewById(R.id.panelMessageDetailReturn);
		panelMessageDetailReturn.setOnClickListener(this);

		panelMessageDetailTitle = (TextView) view.findViewById(R.id.panelMessageDetailTitle);
		panelMessageDetailDate = (TextView) view.findViewById(R.id.panelMessageDetailDate);
		panelMessageDetailContent = (TextView) view.findViewById(R.id.panelMessageDetailContent);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {

			th = new HttpPanelMessageDetails(preferences, id, fragement, getActivity());
			th.start();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMessageDetailReturn.getId()) {
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

class HttpPanelMessageDetails extends Thread {

	private SharedPreferences preferences;
	private String id;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMessageDetails(SharedPreferences preferences, String id, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.id = id;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/Home/my_message_show";

		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);

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
						if (messageStr.equals("请重新登录")) {
							PanelMessageCharge.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMessageDetails.panelMessageDetailsErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelMessageDetails.panelMessageDetailsSuccessHandler.sendMessage(new Message());
					} else {
						PanelMessageDetails.panelMessageDetailsUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				PanelMessageDetails.panelMessageDetailsUnknownHandler.sendMessage(new Message());

			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
