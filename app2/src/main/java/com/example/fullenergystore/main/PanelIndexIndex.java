package com.example.fullenergystore.main;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class PanelIndexIndex extends Fragment implements OnClickListener {

	private View view;
	private ImageView panelIndexIndexButton;
	private TextView panelIndexIndexWithdraw, panelIndexIndexExtPrice, panelIndexIndexAbleNums,
			panelIndexIndexTotalPrice , is_start_state;
	private HttpPanelIndexIndex th;
	private SharedPreferences preferences;
	public static Handler panelIndexIndexSuccessHandler, panelIndexIndexErrorHandler, panelIndexIndexUnknownHandler,
			panelIndexIndexOpenSuccessHandler, panelIndexIndexOpenErrorHandler, panelIndexIndexCloseSuccessHandler,
			panelIndexIndexCloseErrorHandler, panelIndexIndexWithDrawSuccessHandler, turnToLogin,
			panelIndexIndexWithDrawErrorHandler;
	private ProgressDialog progressDialog;
	private int shopStatiu;
	private Fragment fragement;
	private LinearLayout setup;
	private ImageView qrcode_img;

	public Bitmap qrcode_imgqrcode_imgqrcode_img(String content, int width, int height) {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (encode.get(j, i)) {
						pixels[i * width + j] = 0xff000000;
					} else {
						pixels[i * width + j] = 0xffffffff;
					}
				}
			}
			return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_index_index, container, false);
		fragement = this;

		init();
		handler();
		main();
		return view;
	}

	private void handler() {
		panelIndexIndexSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					shopStatiu = jsonObject.getInt("close_door");
					if (shopStatiu == 0) {
						panelIndexIndexButton.setImageResource(R.drawable.switch_on);
						is_start_state.setText("点击停止营业");
					} else {
						panelIndexIndexButton.setImageResource(R.drawable.switch_off);
						is_start_state.setText("点击开始营业");
					}
					String extPrice = jsonObject.getString("ext_price");
					panelIndexIndexExtPrice.setText(extPrice + "");
					String ableNums = jsonObject.getString("able_nums");
					panelIndexIndexAbleNums.setText("可兑换 " + ableNums + " 次");
					String totalPrice = jsonObject.getString("total_price");
					panelIndexIndexTotalPrice.setText("总收入: " + totalPrice + " 元");
					progressDialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		panelIndexIndexErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelIndexIndexUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelIndexIndexOpenSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "店铺开始营业！", Toast.LENGTH_SHORT).show();
				is_start_state.setText("点击停止营业");
				panelIndexIndexButton.setClickable(true);
				panelIndexIndexButton.setImageResource(R.drawable.switch_on);
				progressDialog.dismiss();
			}
		};
		panelIndexIndexOpenErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				panelIndexIndexButton.setClickable(true);
				progressDialog.dismiss();
			}
		};
		panelIndexIndexCloseSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "店铺已经打烊！", Toast.LENGTH_SHORT).show();
				is_start_state.setText("点击开始营业");
				panelIndexIndexButton.setClickable(true);
				panelIndexIndexButton.setImageResource(R.drawable.switch_off);
				progressDialog.dismiss();
			}
		};
		panelIndexIndexCloseErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
				panelIndexIndexButton.setClickable(true);
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
				editor.putString("apibus_businessid", null);
				editor.putString("apibus_username", null);
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
		progressDialog = Panel.progressDialog;

		panelIndexIndexWithdraw = (TextView) view.findViewById(R.id.panelIndexIndexWithdraw);
		panelIndexIndexWithdraw.setOnClickListener(this);
		panelIndexIndexButton = (ImageView) view.findViewById(R.id.panelIndexIndexButton);
		panelIndexIndexButton.setOnClickListener(this);

		panelIndexIndexExtPrice = (TextView) view.findViewById(R.id.panelIndexIndexExtPrice);
		panelIndexIndexAbleNums = (TextView) view.findViewById(R.id.panelIndexIndexAbleNums);
		panelIndexIndexTotalPrice = (TextView) view.findViewById(R.id.panelIndexIndexTotalPrice);

		setup = (LinearLayout) view.findViewById(R.id.setup);
		setup.setOnClickListener(this);

		qrcode_img = (ImageView) view.findViewById(R.id.qrcode_img);
		is_start_state = (TextView) view.findViewById(R.id.is_start_state);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			String bid = preferences.getString("bid","");

			qrcode_img.setImageBitmap(qrcode_imgqrcode_imgqrcode_img(bid,400,400));

			th = new HttpPanelIndexIndex(preferences, fragement, getActivity());
			th.start();
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (arg0.getId() == panelIndexIndexWithdraw.getId()) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final Dialog mAlertDialog = builder.create();
			View view = inflater.inflate(R.layout.alertdialog_pay, null);
			TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
			success.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (PubFunction.isNetworkAvailable(getActivity())) {
						final HttpPanelIndexWithdraw th = new HttpPanelIndexWithdraw(preferences, fragement,
								getActivity());
						th.start();
						panelIndexIndexWithDrawSuccessHandler = new Handler() {
							public void handleMessage(Message msg) {
								JSONArray array = th.getResult();
								Toast.makeText(getActivity(), msg.getData().getString("message"), Toast.LENGTH_SHORT)
										.show();
								mAlertDialog.dismiss();
							};
						};
						panelIndexIndexWithDrawErrorHandler = new Handler() {
							public void handleMessage(Message msg) {
								Toast.makeText(getActivity(), "提现失败！", Toast.LENGTH_SHORT).show();
								mAlertDialog.dismiss();
							};
						};

					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			});
			TextView cancel = (TextView) view.findViewById(R.id.AlertdialogCancel);
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mAlertDialog.dismiss();
				}
			});
			TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
			title.setText("申请提现");
			TextView smallTitle = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
			smallTitle.setText("提现金额 ： " + panelIndexIndexExtPrice.getText().toString() + "元");
			TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
			content.setText("预计 2 至 3 天 到账");
			mAlertDialog.show();
			mAlertDialog.getWindow().setContentView(view);
		} else if (panelIndexIndexButton.getId() == arg0.getId()) {
			if (PubFunction.isNetworkAvailable(getActivity())) {
				panelIndexIndexButton.setClickable(false);
				HttpPanelIndexIndexJudge th = new HttpPanelIndexIndexJudge(preferences, getActivity(), fragement);
				th.start();
				progressDialog.show();
			} else {
				Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
			}
		}else if(arg0.getId() == setup.getId()){
			Intent intent = new Intent(getActivity(),SetUp.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
		}
	}
}

class HttpPanelIndexWithdraw extends Thread {

	private JSONArray jsonObject;
	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelIndexWithdraw(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/Home/give_me";
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

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				message.setData(bundle);

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("请重新登录")) {
							PanelIndexIndex.turnToLogin.sendMessage(new Message());
						} else {
							PanelIndexIndex.panelIndexIndexWithDrawErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONArray("data");
						PanelIndexIndex.panelIndexIndexWithDrawSuccessHandler.sendMessage(message);
					} else {
						PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.err.println(e.toString());
				PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return jsonObject;
	}
}

class HttpPanelIndexIndex extends Thread {

	private JSONObject jsonObject;
	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelIndexIndex(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
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
							PanelIndexIndex.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelIndexIndex.panelIndexIndexErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelIndexIndex.panelIndexIndexSuccessHandler.sendMessage(new Message());
					} else {
						PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
			if (fragment.getView() != null) {
				PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return jsonObject;
	}
}

class HttpPanelIndexIndexJudge extends Thread {

	private SharedPreferences preferences;
	private Activity activity;
	private Fragment fragment;

	public HttpPanelIndexIndexJudge(SharedPreferences preferences, Activity activity, Fragment fragment) {
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
				if (code.equals("100")) {
					JSONObject date = jsonObject.getJSONObject("data");
					int statiu = date.getInt("close_door");
					if (statiu == 0) {
						if (PubFunction.isNetworkAvailable(activity)) {
							HttpPanelIndexIndexClose th = new HttpPanelIndexIndexClose(preferences, fragment, activity);
							th.start();
						} else {
							Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
						}
					} else if (statiu == 1) {
						if (PubFunction.isNetworkAvailable(activity)) {
							HttpPanelIndexIndexOpen th = new HttpPanelIndexIndexOpen(preferences, fragment, activity);
							th.start();
						} else {
							Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
				}
			}
		} catch (Exception e) {
		}
	}

}

class HttpPanelIndexIndexOpen extends Thread {

	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelIndexIndexOpen(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.activity = activity;
		this.fragment = fragment;
	}

	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/Home/open_door";
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
							PanelIndexIndex.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelIndexIndex.panelIndexIndexOpenErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelIndexIndex.panelIndexIndexOpenSuccessHandler.sendMessage(new Message());
					} else {
						PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}


}

class HttpPanelIndexIndexClose extends Thread {

	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelIndexIndexClose(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/Home/close_door";
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
							PanelIndexIndex.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelIndexIndex.panelIndexIndexCloseErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelIndexIndex.panelIndexIndexCloseSuccessHandler.sendMessage(new Message());
					} else {
						PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}
}
