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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;

@SuppressLint("ValidFragment")
public class PanelShopIndexItem extends Fragment implements OnScrollListener {

	private View view;
	private PullToRefreshGridView gridView;
	private PanelShopIndexItemAdaptr adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	private int catid;
	private SharedPreferences preferences;
	private Handler panelShopIndexItemSuccessHandler, panelShopIndexItemSuccessREHandler,
			panelShopIndexItemErrorHandler, panelShopIndexItemUnknownHandler, turnToLogin;
	private HttpPanelShopIndexItem th, th_re;

	private int count_anli_page = 2;
	private int current_page = 1;

	private Fragment fragement;

	public PanelShopIndexItem() {
		// TODO Auto-generated constructor stub
	}

	public PanelShopIndexItem(int catid) {
		this.catid = catid;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_shop_index_1, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelShopIndexItemSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONArray array = th.getResult();

				adapter = new PanelShopIndexItemAdaptr(getActivity(), getdata(array), R.layout.panel_shop_index_item,
						new String[] { "count", "price" },
						new int[] { R.id.panelShopItemCount, R.id.panelShopItemPrice }, getActivity());
				gridView.setAdapter(adapter);
				// gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
				gridView.onRefreshComplete();
			}
		};

		panelShopIndexItemSuccessREHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				JSONArray temp_result = th_re.getResult();

				if (temp_result.toString().equals("[]")) {
					Toast.makeText(getActivity(), "已经加载到最底！", Toast.LENGTH_LONG).show();
				} else {
					JSONObject jsonObject = null;
					for (int i = 0; i < temp_result.length(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						try {
							jsonObject = temp_result.getJSONObject(i);
							map.put("count", jsonObject.get("goodsname").toString());
							map.put("price", jsonObject.get("price").toString() + "元");
							map.put("thumb", jsonObject.get("thumb").toString());
							map.put("id", jsonObject.get("id").toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}
						list.add(map);
					}
					adapter.notifyDataSetChanged();
					count_anli_page = count_anli_page + 1;
				}
			}
		};

		panelShopIndexItemErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}
		};
		panelShopIndexItemUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
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
		gridView = (PullToRefreshGridView) view.findViewById(R.id.panelShopGridView);
		gridView.setOnScrollListener(this);

		gridView.setOnRefreshListener(new OnRefreshListener<GridView>() {

			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				// TODO Auto-generated method stub
				list.clear();
				count_anli_page = 2;
				current_page = 1;
				adapter.notifyDataSetChanged();
				if (PubFunction.isNetworkAvailable(getActivity())) {
					init();
					handler();
					th = new HttpPanelShopIndexItem(catid, preferences, panelShopIndexItemSuccessHandler,
							panelShopIndexItemErrorHandler, panelShopIndexItemUnknownHandler,
							panelShopIndexItemSuccessREHandler, turnToLogin, 1, fragement, getActivity());
					th.start();
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			th = new HttpPanelShopIndexItem(catid, preferences, panelShopIndexItemSuccessHandler,
					panelShopIndexItemErrorHandler, panelShopIndexItemUnknownHandler,
					panelShopIndexItemSuccessREHandler, turnToLogin, 1, this, getActivity());
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	private List<Map<String, Object>> getdata(JSONArray array) {
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonObject = null;
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				jsonObject = (JSONObject) array.get(i);
				map.put("count", jsonObject.get("goodsname").toString());
				map.put("price", jsonObject.get("price").toString() + "元");
				map.put("thumb", jsonObject.get("thumb").toString());
				map.put("id", jsonObject.get("id").toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			list.add(map);
		}
		return list;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		list.clear();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					th_re = new HttpPanelShopIndexItem(catid, preferences, panelShopIndexItemSuccessHandler,
							panelShopIndexItemErrorHandler, panelShopIndexItemUnknownHandler,
							panelShopIndexItemSuccessREHandler, turnToLogin, count_anli_page, this, getActivity());
					th_re.start();
					current_page = count_anli_page;
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
		count_anli_page = 2;
		current_page = 1;
	}
}

class HttpPanelShopIndexItem extends Thread {

	private int catid;
	private SharedPreferences preferences;
	private JSONArray jsonArray;
	private Handler SuccessHandler, SuccessREHandler, ErrorHandler, UnknownHandler, turntologin;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelShopIndexItem(int catid, SharedPreferences preferences, Handler SuccessHandler,
			Handler ErrorHandler, Handler UnknownHandler, Handler SuccessREHandler, Handler turntologin, int page,
			Fragment fragment, Activity activity) {
		this.catid = catid;
		this.preferences = preferences;
		this.SuccessHandler = SuccessHandler;
		this.SuccessREHandler = SuccessREHandler;
		this.ErrorHandler = ErrorHandler;
		this.UnknownHandler = UnknownHandler;
		this.page = page;
		this.fragment = fragment;
		this.activity = activity;
		this.turntologin = turntologin;
	}

	@Override
	public void run() {
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api_business.php/shop/lists_goods";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("catid", catid + ""));
		list.add(new BasicNameValuePair("page", page + ""));
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
						if (messageStr.equals("请重新登录")) {
							turntologin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							ErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						if (page == 1) {
							this.jsonArray = jsonObject.getJSONArray("data");
							SuccessHandler.sendMessage(new Message());
						} else {
							this.jsonArray = jsonObject.getJSONArray("data");
							SuccessREHandler.sendMessage(new Message());
						}
					} else {
						UnknownHandler.sendMessage(new Message());
					}
				}

			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.err.println(e + "");
				UnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return jsonArray;
	}
}

class PanelShopIndexItemAdaptr extends SimpleAdapter {

	private int[] mTo;
	private String[] mFrom;
	private ViewHolder mHolder;
	private int panel;
	protected List<? extends Map<String, ?>> mData;
	private LayoutInflater mInflater;
	private Context context;
	private Activity activity;

	public PanelShopIndexItemAdaptr(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, Activity activity) {
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		mData = data;
		panel = resource;
		mFrom = from;
		mTo = to;
		if (context != null) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = context;
		}
		this.activity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mInflater.inflate(panel, null);
			mHolder = new ViewHolder();
			mHolder.mImageView = (ImageView) convertView.findViewById(R.id.panelShopIndexItemImg);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		String thumb = mData.get(position).get("thumb").toString();
		Picasso.with(context).load(PubFunction.www + thumb).config(Bitmap.Config.RGB_565).resize(800, 800).centerCrop()
				.into(mHolder.mImageView);

		final int tempPosition = position;

		convertView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(context, PanelShopGoodInfo.class);
				intent.putExtra("id", mData.get(tempPosition).get("id").toString());
				context.startActivity(intent);
				activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		});

		return super.getView(position, convertView, parent);
	}

	@Override
	public Object getItem(int position) {
		return super.getItem(position);
	}

	private class ViewHolder {
		ImageView mImageView;
		TextView mTextView;
	}
}
