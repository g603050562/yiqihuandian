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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

import de.hdodenhof.circleimageview.CircleImageView;

public class PanelMineIndex extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelMineIndexInformation, panelMineCertified, panelMineBooking, panelMineIndexRecord,
			panelMineIndexMessage, panelMineIndexSetUp, panelMineIndexSurplus, panelMineReward,
			panelMineIndexTransactionPassword, panelMineIndexInstructions, panelMineIndexTransactionReceiveAddress;
	public static Handler panelMineIndexSuccessHandler, panelMineIndexErrorHandler, panelMineIndexUnknownHandler,
			panelMineIndexMyBookingSuccessHandler, panelMineIndexMyBookingErrorHandler,
			panelMineIndexTransactionPasswordSuccessHandler, panelMineIndexTransactionPasswordErrorHandler, turnToLogin,panelMineIndexCountSuccessHandler,panelMineIndexCountErrorHandler;
	private TextView panelMineIndexNickname, panelMineIndexSurplusText,panelMineIndexSurplusInvitation;
	private SharedPreferences preferences;
	private HttpPanelMineIndex th;
	private HttpPanelIndexCount th1;
	private ProgressDialog progressDialog;
	private ImageView headImage;
	private String avaterUrl;
	private Fragment fragement;
	private PullToRefreshScrollView scrollView;
//	private int ScrollTo = 0;
	private TextView panelMineIndexCount;

	int type = 0;

	public PanelMineIndex() {
		type = 0;
	}
	@SuppressLint("ValidFragment")
	public PanelMineIndex(int type) {
		this.type = type;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_mine_index, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineIndexSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				scrollView.onRefreshComplete();
				JSONObject jsonObject = th.getResult();
				// System.out.println(jsonObject.toString());
				try {
					String nickname = jsonObject.getString("nickname");
					String car = jsonObject.getString("car");
					String buyMember = jsonObject.getString("buy_member");
					String tradePassword = jsonObject.getString("trade_password");
					String surplus = jsonObject.getString("surplus");
					String yqm = jsonObject.getString("yqm");
					if(yqm.equals("null")){
						yqm = "无";
					}
					panelMineIndexSurplusInvitation.setText(yqm);
					avaterUrl = jsonObject.getString("avater");
					Picasso.with(getActivity()).load(PubFunction.www + avaterUrl).into(headImage);
					panelMineIndexNickname.setText(nickname);
					panelMineIndexSurplusText.setText(surplus);

					int trade_password = jsonObject.getInt("trade_password");
					if (trade_password == 0) {
						LayoutInflater inflater = LayoutInflater.from(getActivity());
//						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//						final AlertDialog mAlertDialog = builder.create();
						
						final Dialog mAlertDialog = new Dialog(getActivity());  
						
						View view = inflater.inflate(R.layout.alertdialog_transaction_password, null);

						final EditText panelMineIndexAlertDialogPsw = (EditText) view
								.findViewById(R.id.panelMineIndexAlertDialogPsw);
						final EditText panelMineIndexAlertDialogPswRe = (EditText) view
								.findViewById(R.id.panelMineIndexAlertDialogPswRe);

						TextView submit = (TextView) view.findViewById(R.id.panelMineIndexAlertDialogSubmit);
						submit.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								String psw = "";
								String pswRe = "";
								psw = panelMineIndexAlertDialogPsw.getText().toString().trim();
								pswRe = panelMineIndexAlertDialogPswRe.getText().toString().trim();
								if (psw.equals("") || pswRe.equals("")) {
									Toast.makeText(getActivity(), "输入内容不能为空！", Toast.LENGTH_SHORT).show();
								} else if (!psw.equals(pswRe)) {
									Toast.makeText(getActivity(), "两次密码输入不相同！", Toast.LENGTH_SHORT).show();
									panelMineIndexAlertDialogPsw.setText("");
									panelMineIndexAlertDialogPswRe.setText("");
								} else {
									if (PubFunction.isNetworkAvailable(getActivity())) {
										HttpPanelMineIndexTransactionPasswordSubmit th = new HttpPanelMineIndexTransactionPasswordSubmit(
												psw, preferences, fragement, getActivity());
										th.start();
									} else {
										Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
									}

								}
							}
						});
						mAlertDialog.setCancelable(true);
						mAlertDialog.show();
						mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));  
						mAlertDialog.getWindow().setContentView(view);

						panelMineIndexTransactionPasswordSuccessHandler = new Handler() {
							public void handleMessage(Message msg) {
								Toast.makeText(getActivity(), "交易密码设置成功！", Toast.LENGTH_SHORT).show();
								mAlertDialog.dismiss();
							};
						};
						panelMineIndexTransactionPasswordErrorHandler = new Handler() {
							public void handleMessage(Message msg) {
								String message = msg.getData().getString("message");
								Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
								panelMineIndexAlertDialogPsw.setText("");
								panelMineIndexAlertDialogPswRe.setText("");
							};
						};

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				progressDialog.dismiss();
			}
		};
		panelMineIndexErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineIndexUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineIndexMyBookingSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
		panelMineIndexMyBookingErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
				Intent intent = new Intent(getActivity(), Login_.class);
				intent.putExtra("type", "1");
				getActivity().startActivity(intent);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
		
		panelMineIndexCountSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th1.getResult();
				if(jsonObject!=null){
					try {
						String nums = jsonObject.getString("nums");
						if(nums.equals("0")){
							panelMineIndexCount.setVisibility(View.GONE);
						}else{
							panelMineIndexCount.setVisibility(View.VISIBLE);
							panelMineIndexCount.setText(nums);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		panelMineIndexCountErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		};
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		progressDialog = Panel.progressDialog;

		panelMineIndexInformation = (LinearLayout) view.findViewById(R.id.panelMineIndexInformation);
		panelMineIndexInformation.setOnClickListener(this);
		panelMineCertified = (LinearLayout) view.findViewById(R.id.panelMineCertified);
		panelMineCertified.setOnClickListener(this);
		panelMineBooking = (LinearLayout) view.findViewById(R.id.panelMineBooking);
		panelMineBooking.setOnClickListener(this);
		panelMineIndexRecord = (LinearLayout) view.findViewById(R.id.panelMineIndexRecord);
		panelMineIndexRecord.setOnClickListener(this);
		panelMineIndexMessage = (LinearLayout) view.findViewById(R.id.panelMineIndexMessage);
		panelMineIndexMessage.setOnClickListener(this);
		panelMineIndexSetUp = (LinearLayout) view.findViewById(R.id.panelMineIndexSetUp);
		panelMineIndexSetUp.setOnClickListener(this);
		panelMineIndexSurplus = (LinearLayout) view.findViewById(R.id.panelMineIndexSurplus);
		panelMineIndexSurplus.setOnClickListener(this);
		panelMineReward = (LinearLayout) view.findViewById(R.id.panelMineReward);
		panelMineReward.setOnClickListener(this);
		panelMineIndexTransactionPassword = (LinearLayout) view.findViewById(R.id.panelMineIndexTransactionPassword);
		panelMineIndexTransactionPassword.setOnClickListener(this);
		panelMineIndexInstructions = (LinearLayout) view.findViewById(R.id.panelMineIndexInstructions);
		panelMineIndexInstructions.setOnClickListener(this);
		panelMineIndexNickname = (TextView) view.findViewById(R.id.panelMineIndexNickname);
		headImage = (ImageView) view.findViewById(R.id.indexImage);
		panelMineIndexTransactionReceiveAddress = (LinearLayout) view.findViewById(R.id.panelMineIndexTransactionReceiveAddress);
		panelMineIndexTransactionReceiveAddress.setOnClickListener(this);
		panelMineIndexSurplusText = (TextView) view.findViewById(R.id.panelMineIndexSurplusText);

		scrollView = (PullToRefreshScrollView) view.findViewById(R.id.panel_mine_index_scrollview);
		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				if (PubFunction.isNetworkAvailable(getActivity())) {
					th = new HttpPanelMineIndex(preferences, fragement, getActivity());
					th.start();
					
					th1 = new HttpPanelIndexCount(getActivity());
					th1.start();

					progressDialog.show();
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		});
//		scrollView.setScrollViewListener(this);
		panelMineIndexCount = (TextView) view.findViewById(R.id.panel_mine_index_count);
		panelMineIndexSurplusInvitation = (TextView) view.findViewById(R.id.panelMineIndexSurplusInvitation);

	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {

			th = new HttpPanelMineIndex(preferences, this, getActivity());
			th.start();
			
			th1 = new HttpPanelIndexCount(getActivity());
			th1.start();

//			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (panelMineIndexInformation.getId() == arg0.getId()) {
			PanelMineInformation information = new PanelMineInformation(avaterUrl);
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, information);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineCertified.getId() == arg0.getId()) {
			PanelMineCertified certified = new PanelMineCertified();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, certified);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineBooking.getId() == arg0.getId()) {
			PanelMineAppo panelMineAppo = new PanelMineAppo();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineAppo);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexRecord.getId() == arg0.getId()) {
			PanelMineRecord record = new PanelMineRecord();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, record);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexMessage.getId() == arg0.getId()) {
			PanelMineMessage message = new PanelMineMessage();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, message);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexSetUp.getId() == arg0.getId()) {
			PanelMineSetUp setup = new PanelMineSetUp();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, setup);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexSurplus.getId() == arg0.getId()) {
			Panel.mine2ShopHandler.sendMessage(new Message());
		} else if (panelMineReward.getId() == arg0.getId()) {
			PanelMineReward panelMineReward = new PanelMineReward();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineReward);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexTransactionPassword.getId() == arg0.getId()) {
			PanelMineTransactionPassword panelMineTransactionPassword = new PanelMineTransactionPassword();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineTransactionPassword);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexInstructions.getId() == arg0.getId()) {
			PanelMineInstructions panelMineInstructions = new PanelMineInstructions();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineInstructions);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelMineIndexTransactionReceiveAddress.getId() == arg0.getId()) {
			PanelMineReceiveAddress panelMineReceiveAddress = new PanelMineReceiveAddress();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineReceiveAddress);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
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
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
					LayoutInflater inflater = LayoutInflater.from(getActivity());
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					final AlertDialog mAlertDialog = builder.create();
					View view = inflater.inflate(R.layout.alertdialog, null);
					TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
					titleSmall.setText("");
					titleSmall.setVisibility(View.GONE);
					TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
					content.setText("");
					content.setVisibility(View.GONE);
					ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
					divid.setVisibility(View.GONE);

					TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
					title.setText("是否要退出换电网？");

					TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
					success.setText("确定");
					success.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							getActivity().finish();
						}
					});
					TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
					error.setText("取消");
					error.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mAlertDialog.dismiss();
						}
					});
					mAlertDialog.show();
					mAlertDialog.getWindow().setContentView(view);

					return true;
				}
				return false;
			}
		});

	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

class HttpPanelMineIndex extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineIndex(SharedPreferences preferences, Fragment fragment, Activity activity) {
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
		String path = PubFunction.www + "api.php/member/my_center";
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

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineIndex.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineIndex.panelMineIndexErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						PanelMineIndex.panelMineIndexSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
			}
		}

	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

class HttpPanelMineIndexTransactionPasswordSubmit extends Thread {

	private String password;
	private SharedPreferences preferences;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineIndexTransactionPasswordSubmit(String password, SharedPreferences preferences,
			Fragment fragment, Activity activity) {
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
				System.out.println(result.toString());
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
							PanelMineIndex.turnToLogin.sendMessage(message);
						} else {
							PanelMineIndex.panelMineIndexTransactionPasswordErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineIndex.panelMineIndexTransactionPasswordSuccessHandler.sendMessage(message);
					} else {
						PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}else{
				System.out.println("aaaaaa"+httpResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}
}

class HttpPanelIndexCount extends Thread {

	private JSONObject jsonObject;
	private Activity activity;

	public HttpPanelIndexCount(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/no_read_nums";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());

				System.out.println(result.toString());

				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				
				String code = jsonObject.getString("code");
				String messageStr = jsonObject.getString("message");
				this.jsonObject = jsonObject.getJSONObject("data");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				message.setData(bundle);

				if (activity != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineIndex.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineIndex.panelMineIndexCountErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineIndex.panelMineIndexCountSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (activity != null) {
				PanelMineIndex.panelMineIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
