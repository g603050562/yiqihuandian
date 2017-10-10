package com.example.fullenergy.main;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PanelMineRecord extends Fragment implements OnClickListener, OnScrollListener {

	private View view;
	private LinearLayout panelMineRecordReturn;
	private PullToRefreshListView listView;
	private SimpleAdapter adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private SharedPreferences preferences;
	public static Handler panelMineRecordErrorHandler, panelMineRecordUnknownHandler, panelMineRecordSuccessHandler,
			panelMineRecordSuccessREHandler,turnToLogin,bottomHandler;
	private HttpPanelMineRecord th;

	private int count_anli_page = 2;
	private int current_page = 1;

	private ProgressDialog progressDialog;
	private Fragment fragemet;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_record, container, false);
		fragemet = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineRecordSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONArray jsonArray = th.getResult();
				adapter = new SimpleAdapter(getActivity(), getdata(jsonArray), R.layout.panel_mine_record_item,
						new String[] { "id", "date", "price" },
						new int[] { R.id.panelMineRecordID, R.id.panelMineRecordDate, R.id.panelMineRecordPrice });
				listView.setAdapter(adapter);
				listView.onRefreshComplete();
				progressDialog.dismiss();
			}
		};
		panelMineRecordErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
		panelMineRecordUnknownHandler = new Handler() {
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
			}
		};
		bottomHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "已经加载到最底！", Toast.LENGTH_LONG).show();
				progressDialog.dismiss();
			}
		};
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		progressDialog = Panel.progressDialog;

		panelMineRecordReturn = (LinearLayout) view.findViewById(R.id.panelMineRecordReturn);
		panelMineRecordReturn.setOnClickListener(this);
		listView = (PullToRefreshListView) view.findViewById(R.id.panelMineRecondListView);
		listView.setOnScrollListener(this);

		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				if(adapter == null){
					listView.onRefreshComplete();
				}else{
					list.clear();
					count_anli_page = 2;
					current_page = 1;
					adapter.notifyDataSetChanged();
					if (PubFunction.isNetworkAvailable(getActivity())) {
						th = new HttpPanelMineRecord(preferences, 1, fragemet, getActivity());
						th.start();
					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		// listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}

	private List<Map<String, Object>> getdata(JSONArray array) {
		for (int i = 0; i < array.length(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				JSONObject jsonObject = array.getJSONObject(i);
				map.put("id", jsonObject.get("trade_sn"));
				Long date = Long.parseLong(jsonObject.get("addtime").toString()) * 1000;
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String dateTime = df.format(date);
				map.put("date", dateTime);
				map.put("price", jsonObject.get("price") + "元");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			list.add(map);
		}
		return list;
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			th = new HttpPanelMineRecord(preferences, 1, this, getActivity());
			th.start();
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineRecordReturn.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineIndex index = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, index);
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

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					final HttpPanelMineRecord th = new HttpPanelMineRecord(preferences, count_anli_page, this,
							getActivity());
					th.start();
					progressDialog.show();
					current_page = count_anli_page;
					panelMineRecordSuccessREHandler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							JSONArray temp_result = null;
							temp_result = th.getResult();
							if (temp_result.toString().equals("[]")) {
								bottomHandler.sendMessage(new Message());
							} else {
								JSONObject jsonObject = null;
								for (int i = 0; i < temp_result.length(); i++) {
									Map<String, Object> map = new HashMap<String, Object>();
									try {
										jsonObject = temp_result.getJSONObject(i);
										map.put("id", jsonObject.get("trade_sn"));
										Long date = Long.parseLong(jsonObject.get("addtime").toString()) * 1000;
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										String dateTime = df.format(date);
										map.put("date", dateTime);
										map.put("price", jsonObject.get("price") + "元");
									} catch (JSONException e) {
										e.printStackTrace();
									}
									list.add(map);
								}
								adapter.notifyDataSetChanged();
								count_anli_page = count_anli_page + 1;
							}
							progressDialog.dismiss();
						}
					};
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}

			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		fragemet = null;
		list.clear();
		view = null;
	}
}

class HttpPanelMineRecord extends Thread {

	private SharedPreferences preferences;
	private JSONArray jsonArray;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineRecord(SharedPreferences preferences, int page, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.page = page;
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
		String path = PubFunction.www + "api.php/member/my_order";
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("page", this.page + ""));
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
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
							PanelMineRecord.turnToLogin.sendMessage(new Message());
						}else if(messageStr.equals("没有记录")){
							PanelMineRecord.bottomHandler.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineRecord.panelMineRecordErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonArray = jsonObject.getJSONArray("data");
						if (page == 1) {
							PanelMineRecord.panelMineRecordSuccessHandler.sendMessage(new Message());
						} else {
							PanelMineRecord.panelMineRecordSuccessREHandler.sendMessage(new Message());
						}
					} else {
						PanelMineRecord.panelMineRecordUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineRecord.panelMineRecordUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return this.jsonArray;
	}
}
