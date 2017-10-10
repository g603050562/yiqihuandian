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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.PubFunction;

public class SetupFeedBack extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineFeedbackReturn;
	private SharedPreferences preferences;
	private HttpPanelMineFeedBack th;
	private TextView panelMineFeedBackSubmit;
	private EditText panelMineFeedBackContent;
	public static Handler panelMineFeedBackSuccessHandler, panelMineFeedBackErrorHandler,
			panelMineFeedBackUnknownHandler, turnToLogin;
	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_setup_feedback, container, false);
		fragement = this;

		init();
		handler();

		return view;
	}

	private void handler() {
		panelMineFeedBackSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				SetupIndex setup = new SetupIndex();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.replace(R.id.panelSetupPanel, setup);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
		};
		panelMineFeedBackErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			}
		};
		panelMineFeedBackUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "出现未知错误!", Toast.LENGTH_SHORT).show();
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

		panelMineFeedbackReturn = (LinearLayout) view.findViewById(R.id.panelSetupFeedBackReturn);
		panelMineFeedbackReturn.setOnClickListener(this);
		panelMineFeedBackSubmit = (TextView) view.findViewById(R.id.panelSetupFeedBackSubmit);
		panelMineFeedBackSubmit.setOnClickListener(this);
		panelMineFeedBackContent = (EditText) view.findViewById(R.id.panelSetupFeedBackContnet);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (panelMineFeedbackReturn.getId() == arg0.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupIndex setup = new SetupIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelSetupPanel, setup);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineFeedBackSubmit.getId() == arg0.getId()) {
			String content = "";
			content = panelMineFeedBackContent.getText().toString().trim();
			if (content.equals("")) {
				Toast.makeText(getActivity(), "内容不能为空!", Toast.LENGTH_SHORT).show();
			} else {
				th = new HttpPanelMineFeedBack(content, preferences, getActivity(), fragement);
				th.start();
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
					SetupIndex setup = new SetupIndex();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelSetupPanel, setup);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
}

class HttpPanelMineFeedBack extends Thread {

	private String content;
	private SharedPreferences Preferences;
	private Activity activity;
	private Fragment fragment;

	public HttpPanelMineFeedBack(String content, SharedPreferences Preferences, Activity activity, Fragment fragment) {
		this.content = content;
		this.Preferences = Preferences;
		this.activity = activity;
		this.fragment = fragment;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String api_userid = Preferences.getString("api_userid", null);
		String api_username = Preferences.getString("api_username", null);
		String path = PubFunction.www + "api_business.php/member/tousu";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("content", content));

		try {
			httpPost.setHeader("Cookie",
					"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
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
							SetupFeedBack.turnToLogin.sendMessage(new Message());
						} else {
							SetupFeedBack.panelMineFeedBackErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						SetupFeedBack.panelMineFeedBackSuccessHandler.sendMessage(message);
					} else {
						SetupFeedBack.panelMineFeedBackUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				SetupFeedBack.panelMineFeedBackUnknownHandler.sendMessage(new Message());

			}
		}
	}
}
