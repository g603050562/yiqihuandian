package com.example.fullenergy.main;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import com.example.fullenergy.pub.PubFunction;

public class PanelMineSetUp extends Fragment implements OnClickListener {

	private View view;
	private ImageView panelMineSetUpVersionPoint;
	private LinearLayout panelMineSetUpReturn;
	private LinearLayout panelMineSetUpPassword, panelMineSetUpFeedback;
	private TextView panelMineSetupLogout, panelMineSetUpVersionCode;
	private LinearLayout panelMineSetUpVersion, panelMineSetUpBluetooth;
	private HttpPanelMineSetUpVersion th;

	private SharedPreferences preferences;
	public static Handler panelMineSetUpVersionSuccessHandler, panelMineSetUpVersionErrorHandler,
			panelMineSetUpVersionUnknownHandler,turnToLogin,downloadSuccess;

	private HttpDownLoadApk httpDownLoadApk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_setup, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void init() {
		PackageManager pm = getActivity().getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

		panelMineSetUpReturn = (LinearLayout) view.findViewById(R.id.panelMineSetUpReturn);
		panelMineSetUpReturn.setOnClickListener(this);
		panelMineSetUpPassword = (LinearLayout) view.findViewById(R.id.panelMineSetUpPassword);
		panelMineSetUpPassword.setOnClickListener(this);
		panelMineSetUpFeedback = (LinearLayout) view.findViewById(R.id.panelMineSetUpFeedback);
		panelMineSetUpFeedback.setOnClickListener(this);
		panelMineSetupLogout = (TextView) view.findViewById(R.id.panelMineSetupLogout);
		panelMineSetupLogout.setOnClickListener(this);
		panelMineSetUpVersionCode = (TextView) view.findViewById(R.id.panelMineSetUpVersionCode);
		panelMineSetUpVersionCode.setText(pi.versionName.toString());
		panelMineSetUpVersion = (LinearLayout) view.findViewById(R.id.panelMineSetUpVersion);
		panelMineSetUpVersion.setOnClickListener(this);
		panelMineSetUpVersionPoint = (ImageView) view.findViewById(R.id.panelMineSetUpVersionPoint);
		panelMineSetUpBluetooth = (LinearLayout) view.findViewById(R.id.panelMineSetUpBluetooth);
		panelMineSetUpBluetooth.setOnClickListener(this);
	}

	private void handler() {
		panelMineSetUpVersionSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				try {
					JSONObject jsonObjectVersion = th.getResult();
					final int versionCode = Integer.parseInt(jsonObjectVersion.getString("version"));
					PackageManager manager = getActivity().getPackageManager();
					PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
					final int versionCodeLocal = info.versionCode;
					if (versionCode > versionCodeLocal) {
						panelMineSetUpVersionPoint.setVisibility(View.VISIBLE);
					}
					panelMineSetUpVersion.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
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

							if (versionCode > versionCodeLocal) {
								TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
								title.setText("已发现新版本，是否下载？");

								TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
								success.setText("下载");
								success.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										httpDownLoadApk = new HttpDownLoadApk("http://www.huandianwang.com/APP/huandian.apk",getActivity(),2);
										httpDownLoadApk.start();
										Toast.makeText(getActivity(),"正在后台进行下载，请稍后！",Toast.LENGTH_LONG).show();
										mAlertDialog.dismiss();
									}
								});

							} else {
								TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
								title.setText("未检测到新版本！");

								TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
								success.setText("确定");
								success.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										mAlertDialog.dismiss();
									}
								});
							}

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
						}
					});

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		panelMineSetUpVersionErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		};
		panelMineSetUpVersionUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
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

		downloadSuccess = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				File file = httpDownLoadApk.getFile();
				Toast.makeText(getActivity(), "下载成功！", Toast.LENGTH_SHORT).show();
				System.out.println(file.toString());
				openFile(file);
			}
		};
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			th = new HttpPanelMineSetUpVersion(preferences, this, getActivity());
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
		if (arg0.getId() == panelMineSetUpReturn.getId()) {
			PanelMineIndex index = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, index);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (arg0.getId() == panelMineSetUpPassword.getId()) {
			PanelMinePassword password = new PanelMinePassword();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, password);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (arg0.getId() == panelMineSetUpFeedback.getId()) {
			PanelMineFeedBack feedBack = new PanelMineFeedBack();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, feedBack);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (arg0.getId() == panelMineSetUpBluetooth.getId()) {
			PanelMineSetUpBluetooth panelMineSetUpBluetooth = new PanelMineSetUpBluetooth();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineSetUpBluetooth);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (arg0.getId() == panelMineSetupLogout.getId()) {

			LayoutInflater inflater = LayoutInflater.from(getActivity());

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final AlertDialog mAlertDialog = builder.create();
			View view = inflater.inflate(R.layout.alertdialog, null);

			TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
			title.setText("确认注销帐号？");

			TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
			titleSmall.setText("");
			titleSmall.setVisibility(View.GONE);

			TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
			content.setText("");
			content.setVisibility(View.GONE);

			ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
			divid.setVisibility(View.GONE);

			TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
			success.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("usrename", null);
					editor.putString("password", null);
					editor.putString("jy_password", null);
					editor.putString("PHPSESSID", null);
					editor.putString("api_userid", null);
					editor.putString("api_username", null);
					editor.commit();

					startActivity(new Intent(getActivity(), Login_.class));
					getActivity().finish();
				}
			});
			TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
			error.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAlertDialog.dismiss();
				}
			});
			mAlertDialog.show();
			mAlertDialog.getWindow().setContentView(view);
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

	//    打开APK程序代码
	private void openFile(File file) {
		// TODO Auto-generated method stub
		Log.e("OpenFile", file.getName());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivity(intent);
	}
}

class HttpPanelMineSetUpVersion extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineSetUpVersion(SharedPreferences preferences, Fragment fragment, Activity activity) {
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
		String path = PubFunction.www + "api.php/home/and_version";
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

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineSetUp.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineSetUp.panelMineSetUpVersionErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelMineSetUp.panelMineSetUpVersionSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineSetUp.panelMineSetUpVersionUnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineSetUp.panelMineSetUpVersionUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
