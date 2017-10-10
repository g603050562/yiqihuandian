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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergystore.R;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PanelMessageWithdrawList extends Fragment implements OnClickListener, OnScrollListener {

	private View view;
	private PullToRefreshListView listView;
	private SimpleAdapter adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private SharedPreferences preferences;
	private HttpPanelIndexWithdrawList th;
	public static Handler panelIndexWithdrawListSuccessHandler, panelIndexWithdrawListErrorHandler,
			panelIndexWithdrawListUnknownHandler, panelIndexWithdrawListSuccessREHandler, turnToLogin;
	private ProgressDialog progressDialog;

	private int count_anli_page = 2;
	private int current_page = 1;

	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_index_withdraw_list, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelIndexWithdrawListSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONArray array = th.getResult();
				adapter = new SimpleAdapter(getActivity(), getdata(array), R.layout.panel_index_withdraw_list_item,
						new String[] { "phone", "money", "date", "value" },
						new int[] { R.id.panelIndexWithdrawListItemPhone, R.id.panelIndexWithdrawListItemMoney,
								R.id.panelIndexWithdrawListItemDate, R.id.panelIndexWithdrawListItemValue });
				listView.setAdapter(adapter);
				listView.onRefreshComplete();
				progressDialog.dismiss();
			}
		};
		panelIndexWithdrawListErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
//				Toast.makeText(getActivity(), msg.getData().getString("smessage")+"", 5000).show();
				progressDialog.dismiss();
			}
		};
		panelIndexWithdrawListUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "未知错误!", Toast.LENGTH_SHORT).show();
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

		listView = (PullToRefreshListView) view.findViewById(R.id.panelIndexWithdrawListView);
		listView.setOnScrollListener(this);
		// listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
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
						init();
						handler();
						th = new HttpPanelIndexWithdrawList(preferences, 1, getActivity(), fragement);
						th.start();
						progressDialog = Panel.progressDialog;
						progressDialog.show();
					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			}

		});
	}

	private List<Map<String, Object>> getdata(JSONArray array) {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject jsonObject = array.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("phone", jsonObject.getString("business_mobile"));
				map.put("money", jsonObject.getString("money"));
				Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String dateTime = df.format(date);
				map.put("date", date);
				map.put("value", jsonObject.getString("ispass_value"));
				list.add(map);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelIndexWithdrawList(preferences, 1, getActivity(), fragement);
			th.start();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					final HttpPanelIndexWithdrawList th = new HttpPanelIndexWithdrawList(preferences, count_anli_page,
							getActivity(), fragement);
					th.start();
					progressDialog.show();
					current_page = count_anli_page;
					panelIndexWithdrawListSuccessREHandler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							JSONArray temp_result = null;
							temp_result = th.getResult();
							if (temp_result.toString().equals("[]")) {
								Toast.makeText(getActivity(), "已经加载到最底！", Toast.LENGTH_LONG).show();
							} else {
								JSONObject jsonObject = null;
								for (int i = 0; i < temp_result.length(); i++) {
									Map<String, Object> map = new HashMap<String, Object>();
									try {
										jsonObject = temp_result.getJSONObject(i);
										map.put("phone", jsonObject.getString("business_mobile"));
										map.put("money", jsonObject.getString("money"));
										Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										String dateTime = df.format(date);
										map.put("date", date);
										map.put("value", jsonObject.getString("ispass_value"));
									} catch (JSONException e) {
										e.printStackTrace();
									}
									list.add(map);
								}
								adapter.notifyDataSetChanged();
								count_anli_page = count_anli_page + 1;
								progressDialog.dismiss();
							}
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
	public void onDestroy() {
		super.onDestroy();
		list.clear();
	}
}

class HttpPanelIndexWithdrawList extends Thread {

	private SharedPreferences preferences;
	private JSONArray array;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelIndexWithdrawList(SharedPreferences preferences, int page, Activity activity, Fragment fragment) {
		this.preferences = preferences;
		this.page = page;
		this.activity = activity;
		this.fragment = fragment;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);
		String path = PubFunction.www + "api_business.php/Home/give_me_lists";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("page", page + ""));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				
				System.out.println(jsonObject.toString());
				
				String messageString = jsonObject.getString("message");
				String code = jsonObject.getString("code");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);

				if (fragment.getView() != null) {

					if (code.equals("200")) {

						if (messageString.equals("请重新登录")) {
							PanelMessageWithdrawList.turnToLogin.sendMessage(new Message());
						} else {
							PanelMessageWithdrawList.panelIndexWithdrawListErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.array = jsonObject.getJSONArray("data");
						if (page == 1) {
							PanelMessageWithdrawList.panelIndexWithdrawListSuccessHandler.sendMessage(new Message());
						} else {
							PanelMessageWithdrawList.panelIndexWithdrawListSuccessREHandler.sendMessage(new Message());
						}
					} else {
						PanelMessageWithdrawList.panelIndexWithdrawListUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMessageWithdrawList.panelIndexWithdrawListUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return this.array;
	}
}