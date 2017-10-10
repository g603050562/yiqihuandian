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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PanelMineMessage extends Fragment implements OnClickListener, OnScrollListener {

	private View view;
	private LinearLayout panelMineMessageReturn;
	private PullToRefreshListView listView;
	private PanelMineMessageAdapter adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private HttpPanelMineMessage th;
	private SharedPreferences preferences;
	public static Handler panelMineMessageSuccessHandler, panelMineMessageErrorHandler, panelMineMessageUnknownHandler,
			panelMineMessageSuccessREHandler,turnToLogin;

	private int count_anli_page = 2;
	private int current_page = 1;

	private ProgressDialog progressDialog;
	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_message, container, false);
		fragement = this;

		handler();
		init();
		main();

		return view;
	}

	private void handler() {
		panelMineMessageSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONArray jsonArray = th.getResult();
				adapter = new PanelMineMessageAdapter(getActivity(), getdata(jsonArray),new int[]{R.layout.panel_mine_message_item},
						new String[] { "title", "content", "date","style"}, new int[] { R.id.panelMineMessageTitle,
								R.id.panelMineMessageContent, R.id.panelMineMessageDate ,R.id.panelMineMessageStyle});
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						String id = (String) list.get(arg2-1).get("id");
						FragmentManager fragmentManager = getFragmentManager();
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						PanelMineMessageDetails panelMineMessageDetails = new PanelMineMessageDetails(id);
						fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
						fragmentTransaction.replace(R.id.panelMinePanel, panelMineMessageDetails);
						fragmentTransaction.addToBackStack(null);
						fragmentTransaction.commit();
					}
				});
				listView.onRefreshComplete();
				progressDialog.dismiss();
			}
		};
		panelMineMessageErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
		panelMineMessageUnknownHandler = new Handler() {
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
	}

	private void init() {
		panelMineMessageReturn = (LinearLayout) view.findViewById(R.id.panelMineMessageReturn);
		panelMineMessageReturn.setOnClickListener(this);
		listView = (PullToRefreshListView) view.findViewById(R.id.panelMineMessageListView);
		listView.setOnScrollListener(this);

		progressDialog = Panel.progressDialog;

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
						th = new HttpPanelMineMessage(preferences, 1, fragement, getActivity());
						th.start();
					} else {
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		// listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}

	private List<Map<String, Object>> getdata(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				map.put("title", jsonObject.get("title").toString());
				map.put("content", jsonObject.get("content").toString());
				String read = jsonObject.get("read").toString();
				if(read.equals("1")){
					map.put("style", "已读");
				}else{
					map.put("style", "未读");
				}
				Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String dateTime = df.format(date);
				map.put("date", dateTime);
				map.put("id", jsonObject.get("id").toString());
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
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelMineMessage(preferences, 1, this, getActivity());
			th.start();
			progressDialog.show();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineMessageReturn.getId()) {
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
					final HttpPanelMineMessage th = new HttpPanelMineMessage(preferences, count_anli_page, this,
							getActivity());
					th.start();
					progressDialog.show();
					current_page = count_anli_page;
					panelMineMessageSuccessREHandler = new Handler() {
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
										map.put("title", jsonObject.get("title").toString());
										map.put("content", jsonObject.get("content").toString());
										String read = jsonObject.get("read").toString();
										if(read.equals("1")){
											map.put("style", "已读");
										}else{
											map.put("style", "未读");
										}
										Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
										String dateTime = df.format(date);
										map.put("date", dateTime);
										map.put("id", jsonObject.get("id").toString());
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
		adapter = null;
		fragement = null;
		view = null;
		System.gc();
	}
}

class HttpPanelMineMessage extends Thread {

	private SharedPreferences preferences;
	private JSONArray jsonArray;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineMessage(SharedPreferences preferences, int page, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.page = page;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);

		String path = PubFunction.www + "api.php/member/my_message_list";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
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
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineMessage.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineMessage.panelMineMessageErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonArray = jsonObject.getJSONArray("data");
						if (page == 1) {
							PanelMineMessage.panelMineMessageSuccessHandler.sendMessage(new Message());
						} else {
							PanelMineMessage.panelMineMessageSuccessREHandler.sendMessage(new Message());
						}
					} else {
						PanelMineMessage.panelMineMessageUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelMineMessage.panelMineMessageUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return this.jsonArray;
	}
}

class PanelMineMessageAdapter extends BaseAdapter implements Filterable {
	private int[] mTo;
	private String[] mFrom;
	private ViewBinder mViewBinder;
	private ViewHolder mHolder;

	protected List<? extends Map<String, ?>> mData;

	private int[] mResources;
	private int[] mDropDownResources;
	private LayoutInflater mInflater;

	private SimpleFilter mFilter;
	private ArrayList<Map<String, ?>> mUnfilteredData;

	public PanelMineMessageAdapter(Context context, List<? extends Map<String, ?>> data, int[] resources, String[] from,
			int[] to) {
		mData = data;
		mResources = mDropDownResources = resources;
		mFrom = from;
		mTo = to;
		if (context != null) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}

	@Override
	public int getViewTypeCount() {
		return mResources.length;
	}

	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		return mData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			convertView = mInflater.inflate(mResources[getItemViewType(position)], null);
			mHolder = new ViewHolder();
			mHolder.mTextView = (TextView) convertView.findViewById(R.id.panelMineMessageStyle);
			convertView.setTag(mHolder);
		}else{
			mHolder = (ViewHolder) convertView.getTag();
		}
		if(mData.get(position).get("style").equals("已读")){
			mHolder.mTextView.setTextColor(0xffff9c2c);
		}else{
			mHolder.mTextView.setTextColor(0xffff0000);
		}
		
		
		return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(resource, parent, false);
		} else {
			v = convertView;
		}
		bindView(position, v);
		return v;
	}

	public void setDropDownViewResource(int[] resources) {
		this.mDropDownResources = resources;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
	}

	private void bindView(int position, View view) {
		final Map<String, ?> dataSet = mData.get(position);
		if (dataSet == null) {
			return;
		}

		final ViewBinder binder = mViewBinder;
		final String[] from = mFrom;
		final int[] to = mTo;
		final int count = to.length;

		for (int i = 0; i < count; i++) {
			final View v = view.findViewById(to[i]);
			if (v != null) {
				final Object data = dataSet.get(from[i]);
				String text = data == null ? "" : data.toString();
				if (text == null) {
					text = "";
				}

				boolean bound = false;
				if (binder != null) {
					bound = binder.setViewValue(v, data, text);
				}

				if (!bound) {
					if (v instanceof Checkable) {
						if (data instanceof Boolean) {
							((Checkable) v).setChecked((Boolean) data);
						} else if (v instanceof TextView) {
							setViewText((TextView) v, text);
						} else {
							throw new IllegalStateException(
									v.getClass().getName() + " should be bound to a Boolean, not a "
											+ (data == null ? "<unknown type>" : data.getClass()));
						}
					} else if (v instanceof TextView) {
						setViewText((TextView) v, text);
					} else if (v instanceof ImageView) {
						if (data instanceof Integer) {
							setViewImage((ImageView) v, (Integer) data);
						} else {
							setViewImage((ImageView) v, text);
						}
					} else if (v instanceof Spinner) {
						if (data instanceof Integer) {
							((Spinner) v).setSelection((Integer) data);
						} else {
							continue;
						}
					} else {
						throw new IllegalStateException(v.getClass().getName() + " is not a "
								+ " view that can be bounds by this SimpleAdapter");
					}
				}
			}
		}
	}

	public ViewBinder getViewBinder() {
		return mViewBinder;
	}

	public void setViewBinder(ViewBinder viewBinder) {
		mViewBinder = viewBinder;
	}

	public void setViewImage(ImageView v, int value) {
		v.setImageResource(value);
	}

	public void setViewImage(ImageView v, String value) {
		try {
			v.setImageResource(Integer.parseInt(value));
		} catch (NumberFormatException nfe) {
			v.setImageURI(Uri.parse(value));
		}
	}

	public void setViewText(TextView v, String text) {
		v.setText(text);
	}

	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new SimpleFilter();
		}
		return mFilter;
	}

	public static interface ViewBinder {
		boolean setViewValue(View view, Object data, String textRepresentation);
	}

	private class SimpleFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mUnfilteredData == null) {
				mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<Map<String, ?>> list = mUnfilteredData;
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
				int count = unfilteredValues.size();

				ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

				for (int i = 0; i < count; i++) {
					Map<String, ?> h = unfilteredValues.get(i);
					if (h != null) {

						int len = mTo.length;

						for (int j = 0; j < len; j++) {
							String str = (String) h.get(mFrom[j]);

							String[] words = str.split(" ");
							int wordCount = words.length;

							for (int k = 0; k < wordCount; k++) {
								String word = words[k];

								if (word.toLowerCase().startsWith(prefixString)) {
									newValues.add(h);
									break;
								}
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			// noinspection unchecked
			mData = (List<Map<String, ?>>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	@Override
		 public int getItemViewType(int position) {
		return 0;
	}

	private class ViewHolder {
		ImageView mImageView;
		TextView mTextView;
	}
}

