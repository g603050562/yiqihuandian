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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PanelMineReward extends Fragment implements OnClickListener, OnScrollListener {

	private View view;
	private LinearLayout panelMinerewardReturn;
	private PullToRefreshListView listView;
	private SimpleAdapter adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	public static Handler PanelMineRewardSuccessHandler, PanelMineRewardErrorHandler, PanelMineRewardUnknownHandler,
			PanelMineRewardSuccessREHandler, turnToLogin , bottomHandler;
	private HttpPanelMineReward th;
	private SharedPreferences preferences;

	private int count_anli_page = 2;
	private int current_page = 1;

	private ProgressDialog progressDialog;
	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_reward, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		PanelMineRewardSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONArray jsonArray = th.getResultArray();
				JSONObject jsonObject = th.getResultObject();
				if (jsonArray != null) {
					adapter = new SimpleAdapter(getActivity(), getdata(jsonArray), R.layout.panel_mine_reward_item,
							new String[] { "title", "content" },
							new int[] { R.id.panelMineRewardTitle, R.id.panelMineRewardContent });
				}
				if (jsonObject != null) {
					adapter = new SimpleAdapter(getActivity(), getdata(jsonObject), R.layout.panel_mine_reward_item,
							new String[] { "title", "content" },
							new int[] { R.id.panelMineRewardTitle, R.id.panelMineRewardContent });
				}
				listView.setAdapter(adapter);
				listView.onRefreshComplete();
				progressDialog.dismiss();
			}
		};
		PanelMineRewardErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
		PanelMineRewardUnknownHandler = new Handler() {
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

		panelMinerewardReturn = (LinearLayout) view.findViewById(R.id.panelMineRewardReturn);
		panelMinerewardReturn.setOnClickListener(this);
		listView = (PullToRefreshListView) view.findViewById(R.id.panelMineRewardListView);
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
						th = new HttpPanelMineReward(preferences, 1, fragement, getActivity());
						th.start();
					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		// listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}

	private List<Map<String, Object>> getdata(JSONObject jsonObject) {
		for (int i = 0; i < 1; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				map.put("title", jsonObject.get("reward_explain"));
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
				String dateTime = df.format(date);
				map.put("content", dateTime);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			list.add(map);
		}
		return list;
	}

	private List<Map<String, Object>> getdata(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title", jsonObject.get("reward_explain"));
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
				String dateTime = df.format(date);
				map.put("content", dateTime);
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

			th = new HttpPanelMineReward(preferences, 1, this, getActivity());
			th.start();

			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMinerewardReturn.getId()) {
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
					final HttpPanelMineReward th = new HttpPanelMineReward(preferences, count_anli_page, this,
							getActivity());
					th.start();
					progressDialog.show();
					current_page = count_anli_page;
					PanelMineRewardSuccessREHandler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							JSONArray temp_result = null;
							temp_result = th.getResultArray();
							if (temp_result.toString().equals("[]")) {
								bottomHandler.sendMessage(new Message());
							} else {
								JSONObject jsonObject = null;
								for (int i = 0; i < temp_result.length(); i++) {
									Map<String, Object> map = new HashMap<String, Object>();
									try {
										jsonObject = temp_result.getJSONObject(i);
										map.put("title", jsonObject.get("reward_explain"));
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
										String dateTime = df.format(date);
										map.put("content", dateTime);
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
		list.clear();
		fragement = null;
		view = null;
	}
}

class HttpPanelMineReward extends Thread {

	private SharedPreferences preferences;
	private JSONArray jsonArray = null;
	private JSONObject jsonObject = null;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineReward(SharedPreferences preferences, int page, Fragment fragment, Activity activity) {
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
		String path = PubFunction.www + "api.php/member/my_reward";
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("page", page + ""));
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
							PanelMineReward.turnToLogin.sendMessage(new Message());
						}else if(messageStr.equals("没有记录")){
							PanelMineReward.bottomHandler.sendMessage(new Message());
						}else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineReward.PanelMineRewardErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						
						String jsonType = PubFunction.getJSONType(jsonObject.getString("data"));
						if (jsonType.equals("JSONArray")) {
							this.jsonArray = jsonObject.getJSONArray("data");
						} else if (jsonType.equals("JSONObject")) {
							this.jsonObject = jsonObject.getJSONObject("data");
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", "json格式返回错误");
							message.setData(bundle);
							PanelMineReward.PanelMineRewardUnknownHandler.sendMessage(new Message());
						}
						if (page == 1) {
							PanelMineReward.PanelMineRewardSuccessHandler.sendMessage(new Message());
						} else {
							PanelMineReward.PanelMineRewardSuccessREHandler.sendMessage(new Message());
						}
					} else {
						PanelMineReward.PanelMineRewardUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineReward.PanelMineRewardUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResultArray() {
		return this.jsonArray;
	}

	public JSONObject getResultObject() {
		return this.jsonObject;
	}
}
