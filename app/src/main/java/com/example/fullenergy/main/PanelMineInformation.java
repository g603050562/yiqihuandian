package com.example.fullenergy.main;


import java.io.ByteArrayOutputStream;
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

import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
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
import cn.jpush.android.a;
import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineInformation extends Fragment implements OnClickListener {

	private View view;
	private ImageView panelMineInformationHeadImg;
	private LinearLayout pageReturn;
	private LinearLayout panelMineInformationPanel, panelMineInformationHeadImgPanel, panelMineInformationFocus;
	private InputMethodManager manager = null;
	public static Handler panelMineIndexInformationHeadimgHandler, panelMineInformationUploadImgSuccessHandler,
			panelMineInformationUploadImgErrorHandler, panelMineInformationUnknown,
			panelMineInformationUploadInfoSuccessHandler, panelMineInformationUploadInfoErrorHandler,
			panelMineInformationGetSuccessHandler, panelMineInformationGetErrorHandler,turnToLogin;
	private SharedPreferences preferences;
	private String avaterUrl;
	private TextView panelMineInformationSubmit;
	private EditText panelMineInformationNickName, panelMineInformationName, panelMIneInformationAddress;
	private HttpPanelMineInformationGet th;
	private ProgressDialog progressDialog;
	private Fragment fragement;

	public PanelMineInformation() {
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("ValidFragment")
	public PanelMineInformation(String avaterUrl) {
		this.avaterUrl = avaterUrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_mine_information, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineIndexInformationHeadimgHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (PubFunction.isNetworkAvailable(getActivity())) {
					HttpPanelMineInformationUpLoadImg th = new HttpPanelMineInformationUpLoadImg(preferences, fragement, getActivity());
					th.start();
					Toast.makeText(getActivity(),"图片上传成功，请稍后！",Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		};
		panelMineInformationUploadImgSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				panelMineInformationHeadImg.setImageBitmap(PubFunction.bitmap);
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineInformationUploadImgErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineInformationUnknown = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineInformationUploadInfoSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				PanelMineIndex panelMinePanel = new PanelMineIndex();
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.replace(R.id.panelMinePanel, panelMinePanel);
				fragmentTransaction.commit();
				progressDialog.dismiss();
			}
		};
		panelMineInformationUploadInfoErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};

		panelMineInformationGetSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					panelMineInformationNickName.setText(jsonObject.getString("nickname"));
					panelMineInformationName.setText(jsonObject.getString("realname"));
					panelMIneInformationAddress.setText(jsonObject.getString("address"));
					progressDialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		panelMineInformationGetErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
			}
		};
	}

	private void init() {
		pageReturn = (LinearLayout) view.findViewById(R.id.panelMineInformationReturn);
		pageReturn.setOnClickListener(this);

		progressDialog = Panel.progressDialog;

		panelMineInformationPanel = (LinearLayout) view.findViewById(R.id.panelMineInformationPanel);
		panelMineInformationPanel.setOnClickListener(this);
		panelMineInformationHeadImgPanel = (LinearLayout) view.findViewById(R.id.panelMineInformationHeadImgPanel);
		panelMineInformationHeadImgPanel.setOnClickListener(this);
		panelMineInformationFocus = (LinearLayout) view.findViewById(R.id.panelMineInformationFocus);
		panelMineInformationHeadImg = (ImageView) view.findViewById(R.id.panelMineInformationHeadImg);
		Picasso.with(getActivity()).load(PubFunction.www + avaterUrl).into(panelMineInformationHeadImg);
		panelMineInformationSubmit = (TextView) view.findViewById(R.id.panelMineInformationSubmit);
		panelMineInformationSubmit.setOnClickListener(this);
		panelMineInformationNickName = (EditText) view.findViewById(R.id.panelMineInformationNickName);
		panelMineInformationName = (EditText) view.findViewById(R.id.panelMineInformationName);
		panelMIneInformationAddress = (EditText) view.findViewById(R.id.panelMIneInformationAddress);

		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void main() {

		if (PubFunction.isNetworkAvailable(getActivity())) {

			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelMineInformationGet(preferences, this, getActivity());
			th.start();

			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (pageReturn.getId() == arg0.getId()) {
			PanelMineIndex panelMinePanel = new PanelMineIndex();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMinePanel);
			fragmentTransaction.commit();
		} else if (panelMineInformationPanel.getId() == arg0.getId()) {
			if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				panelMineInformationFocus.requestFocus();
			}
		} else if (panelMineInformationHeadImgPanel.getId() == arg0.getId()) {
			PanelMine.panelMineIndexInformationHeadimgHandler.sendMessage(new Message());
			panelMineInformationFocus.requestFocus();
		} else if (panelMineInformationSubmit.getId() == arg0.getId()) {
			String nickName = "";
			String name = "";
			String address = "";
			nickName = panelMineInformationNickName.getText().toString().trim();
			name = panelMineInformationName.getText().toString().trim();
			address = panelMIneInformationAddress.getText().toString().trim();
			if (nickName.equals("") || name.equals("") || address.equals("")) {
				Toast.makeText(getActivity(), "提交信息不能为空！", Toast.LENGTH_SHORT).show();
				nickName = name = address = "";
			} else {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					HttpPanelMineInformationUploadInfo th = new HttpPanelMineInformationUploadInfo(preferences,
							nickName, name, address, this, getActivity());
					th.start();
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
					PanelMineIndex panelMinePanel = new PanelMineIndex();
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.replace(R.id.panelMinePanel, panelMinePanel);
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
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		fragement = null;
//		view = null;
//		System.gc();
	}
}

class HttpPanelMineInformationUpLoadImg extends Thread {

	private SharedPreferences Preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineInformationUpLoadImg(SharedPreferences Preferences, Fragment fragment, Activity activity) {
		this.Preferences = Preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String api_userid = Preferences.getString("api_userid", null);
		String api_username = Preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/set_avater_and";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("avater", bitmaptoString(PubFunction.bitmap)));
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				// System.out.println(result.toString());
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
						if (messageString.equals("秘钥不正确,请重新登录")) {
							PanelMineInformation.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineInformation.panelMineInformationUploadImgErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineInformation.panelMineInformationUploadImgSuccessHandler.sendMessage(message);
					} else {
						PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
					}
				}
			} else {
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
			}
		}
	}

	public String bitmaptoString(Bitmap bitmap) {
		Bitmap smallBitmap = PubFunction.small(bitmap);
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		smallBitmap.compress(CompressFormat.JPEG, 50, bStream);
		byte[] bytes = bStream.toByteArray();
		String string = Base64.encodeToString(bytes, Base64.DEFAULT);
		bytes = new byte[]{};
		return string;
	}
}

class HttpPanelMineInformationUploadInfo extends Thread {

	private SharedPreferences preferences;
	private String nickName = "";
	private String name = "";
	private String address = "";
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineInformationUploadInfo(SharedPreferences preferences, String nickName, String name,String address, Fragment fragment, Activity activity) {
		this.address = address;
		this.nickName = nickName;
		this.name = name;
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
		String path = PubFunction.www + "api.php/member/set_my_name";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("nickname", nickName));
		list.add(new BasicNameValuePair("name", name));
		list.add(new BasicNameValuePair("address", address));
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
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
				message.setData(bundle);

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineInformation.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineInformation.panelMineInformationUploadInfoErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineInformation.panelMineInformationUploadInfoSuccessHandler.sendMessage(message);
					} else {
						PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
			}
		}
	}
}

class HttpPanelMineInformationGet extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineInformationGet(SharedPreferences preferences, Fragment fragment, Activity activity) {
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
		String path = PubFunction.www + "api.php/member/set_my_name";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
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
				message.setData(bundle);

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineInformation.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineInformation.panelMineInformationGetErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelMineInformation.panelMineInformationGetSuccessHandler.sendMessage(message);
					} else {
						PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineInformation.panelMineInformationUnknown.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return jsonObject;
	}
}
