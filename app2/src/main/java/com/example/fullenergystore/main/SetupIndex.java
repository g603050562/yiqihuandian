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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.PubFunction;

import java.io.File;

public class SetupIndex extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelSetUpPassword, panelSetUpIndexVersion,panelSetUpInfo,pageReturn;
	private TextView panelSetupIndexLogout, panelSetUpIndexVersionCode;
	private SharedPreferences preferences;
	private ImageView panelSetUpIndexVersionPoint;
	public static Handler setUpIndexVersionSuccessHandler, setUpIndexVersionErrorHandler,setUpIndexVersionUnknownHandler, turnToLogin,downloadSuccess;
	private HttpSetUpIndexVersion th;
	private Fragment fragement;
	private HttpDownLoadApk httpDownLoadApk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_setup_index, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		setUpIndexVersionSuccessHandler = new Handler() {
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
						panelSetUpIndexVersionPoint.setVisibility(View.VISIBLE);
					}
					panelSetUpIndexVersion.setOnClickListener(new OnClickListener() {
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

								TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
								success.setText("下载");
								success.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										httpDownLoadApk = new HttpDownLoadApk("http://www.huandianwang.com/APP/huandianBus.apk",getActivity(),2);
										httpDownLoadApk.start();
										Toast.makeText(getActivity(),"正在后台进行下载，请稍后！",Toast.LENGTH_LONG).show();
										mAlertDialog.dismiss();
									}
								});

							} else {
								TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
								title.setText("未检测到新版本");

								TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
								success.setText("确定");
								success.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										mAlertDialog.dismiss();
									}
								});
							}

							TextView error = (TextView) view.findViewById(R.id.AlertdialogCancel);
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

		setUpIndexVersionErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		};
		setUpIndexVersionUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
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

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		PackageManager manager = getActivity().getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		panelSetUpPassword = (LinearLayout) view.findViewById(R.id.panelSetUpPassword);
		panelSetUpPassword.setOnClickListener(this);
		panelSetupIndexLogout = (TextView) view.findViewById(R.id.panelSetupIndexLogout);
		panelSetupIndexLogout.setOnClickListener(this);

		panelSetUpIndexVersionCode = (TextView) view.findViewById(R.id.panelSetUpIndexVersionCode);
		panelSetUpIndexVersionCode.setText(info.versionName);

		panelSetUpIndexVersion = (LinearLayout) view.findViewById(R.id.panelSetUpIndexVersion);
		panelSetUpIndexVersionPoint = (ImageView) view.findViewById(R.id.panelSetUpIndexVersionPoint);
		panelSetUpInfo = (LinearLayout) view.findViewById(R.id.panelSetUpInfo);
		panelSetUpInfo.setOnClickListener(this);

		pageReturn = (LinearLayout) view.findViewById(R.id.pageReturn);
		pageReturn.setOnClickListener(this);
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			th = new HttpSetUpIndexVersion(preferences, getActivity(), fragement);
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onClick(View arg0) {
		if (panelSetUpPassword.getId() == arg0.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupPassword panelSetupPassword = new SetupPassword();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelSetupPassword);
			fragmentTransaction.commit();
		} else if (panelSetUpInfo.getId() == arg0.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			SetupInfo panelSetupPassword = new SetupInfo();
			fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
			fragmentTransaction.replace(R.id.panelSetupPanel, panelSetupPassword);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else if (panelSetupIndexLogout.getId() == arg0.getId()) {
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

			TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
			success.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("usrename", null);
					editor.putString("password", null);
					editor.putString("jy_password", null);
					editor.putString("PHPSESSID", null);
					editor.putString("apibus_businessid", null);
					editor.putString("apibus_username", null);
					editor.commit();

					startActivity(new Intent(getActivity(), Login.class));
					getActivity().finish();
				}
			});
			TextView error = (TextView) view.findViewById(R.id.AlertdialogCancel);
			error.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAlertDialog.dismiss();
				}
			});
			mAlertDialog.show();
			mAlertDialog.getWindow().setContentView(view);
		}else if(pageReturn.getId() == arg0.getId()){
			getActivity().finish();
			getActivity().overridePendingTransition(R.anim.in_left,R.anim.out_right);
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
					getActivity().finish();
					getActivity().overridePendingTransition(R.anim.in_left,R.anim.out_right);
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

class HttpSetUpIndexVersion extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Activity activity;
	private Fragment fragment;

	public HttpSetUpIndexVersion(SharedPreferences preferences, Activity activity, Fragment fragment) {
		this.preferences = preferences;
		this.activity = activity;
		this.fragment = fragment;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/home/version";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";" + "apibus_username=" + apibus_username);

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
							SetupIndex.turnToLogin.sendMessage(new Message());
						} else {
							SetupIndex.setUpIndexVersionErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonObject = jsonObject.getJSONObject("data");
						SetupIndex.setUpIndexVersionSuccessHandler.sendMessage(new Message());
					} else {
						SetupIndex.setUpIndexVersionUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (fragment.getView() != null) {
				SetupIndex.setUpIndexVersionUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
