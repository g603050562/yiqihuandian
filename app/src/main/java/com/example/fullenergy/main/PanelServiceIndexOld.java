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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.ViewGroup;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelServiceIndexOld extends Fragment implements OnClickListener, OnScrollListener {

	private View view;
	private PullToRefreshListView listview;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private HttpPanelServiceIndexOld th;
	private ProgressDialog dialog;
	private SharedPreferences preferences;
	public static Handler panelServiceIndexSuccessHandler, panelServiceIndexSuccessREHandler,
			panelServiceIndexErrorHandler, panelServiceIndexUnknownHandler, turnToLogin, buttomHandler;;
	private IndexRecommendAdapterOld adapter;
	private int count_anli_page = 2;
	private int current_page = 1;
	private Fragment fragement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_service_index_old, container, false);
		fragement = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelServiceIndexSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					JSONArray top = jsonObject.getJSONArray("tuijian");
					JSONArray lists = jsonObject.getJSONArray("lists");
					dialog.dismiss();
					adapter = new IndexRecommendAdapterOld(getActivity(), getdata(lists),
							new int[] { R.layout.panel_service_index_top, R.layout.panel_service_index_item },
							new String[] { "title", "date", "content" },
							new int[] { R.id.title, R.id.date, R.id.content }, getActivity(), R.id.img, top);
					listview.setAdapter(adapter);
					listview.onRefreshComplete();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private List<? extends Map<String, ?>> getdata(JSONArray array) {
				// TODO Auto-generated method stub
				for (int i = 0; i < array.length(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					try {
						JSONObject jsonObject = array.getJSONObject(i);
						map.put("title", jsonObject.getString("title"));
						map.put("content", jsonObject.getString("content"));
						map.put("thumb", jsonObject.getString("thumb"));
						map.put("id", jsonObject.getString("id"));
						Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						String dateTime = df.format(date);
						map.put("date", dateTime);
						list.add(map);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				return list;
			}
		};
		panelServiceIndexErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelServiceIndexUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
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

		buttomHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "已经加载到底！", Toast.LENGTH_SHORT).show();
			}
		};
	}

	private void init() {
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		dialog = new ProgressDialog(getActivity());
		listview = (PullToRefreshListView) view.findViewById(R.id.panelServiceIndexOldList);
		listview.setOnScrollListener(this);
		listview.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				list.clear();
				count_anli_page = 2;
				current_page = 1;
				adapter.notifyDataSetChanged();
				if (PubFunction.isNetworkAvailable(getActivity())) {
					th = new HttpPanelServiceIndexOld(preferences, 1, fragement, getActivity());
					th.start();
				} else {
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			dialog.show();
			th = new HttpPanelServiceIndexOld(preferences, 1, this, getActivity());
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
				if (PubFunction.isNetworkAvailable(getActivity())) {
					final HttpPanelServiceIndexOld th = new HttpPanelServiceIndexOld(preferences, count_anli_page, this,
							getActivity());
					th.start();
					dialog.show();
					current_page = count_anli_page;
					panelServiceIndexSuccessREHandler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							try {
								JSONObject jsonObject1 = th.getResult();
								JSONArray temp_result = jsonObject1.getJSONArray("lists");
								if (temp_result.toString().equals("[]")) {
									buttomHandler.sendMessage(new Message());
								} else {
									for (int i = 0; i < temp_result.length(); i++) {
										Map<String, Object> map = new HashMap<String, Object>();
										JSONObject jsonObject = temp_result.getJSONObject(i);
										map.put("title", jsonObject.getString("title"));
										map.put("content", jsonObject.getString("content"));
										map.put("thumb", jsonObject.getString("thumb"));
										map.put("id", jsonObject.getString("id"));
										Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										String dateTime = df.format(date);
										map.put("date", dateTime);
										list.add(map);
									}
									adapter.notifyDataSetChanged();
									count_anli_page = count_anli_page + 1;
								}
								dialog.dismiss();
							} catch (JSONException e) {
								e.printStackTrace();
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
	
	private void onstop() {
		// TODO Auto-generated method stub
		if(fragement!=null){
			fragement = null;
		}
		if(list!=null){
			list.clear();
			list = null;
		}
		System.gc();
	}
}

class HttpPanelServiceIndexOld extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private int page;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelServiceIndexOld(SharedPreferences preferences, int page, Fragment fragment, Activity activity) {
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
		String path = PubFunction.www + "api.php/service/lists_news";
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
				this.jsonObject = jsonObject.getJSONObject("data");

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelServiceIndexOld.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelServiceIndexOld.panelServiceIndexErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						if (page == 1) {
							PanelServiceIndexOld.panelServiceIndexSuccessHandler.sendMessage(new Message());
						} else {
							PanelServiceIndexOld.panelServiceIndexSuccessREHandler.sendMessage(new Message());
						}
					} else {
						PanelServiceIndexOld.panelServiceIndexUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelServiceIndexOld.panelServiceIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return jsonObject;
	}
}

class IndexRecommendAdapterOld extends BaseAdapter implements Filterable {
	private int[] mTo;
	private String[] mFrom;
	private ViewBinder mViewBinder;
	private ViewHolder mHolder;

	protected List<? extends Map<String, ?>> mData;

	private int[] mResources;
	private int[] mDropDownResources;
	private LayoutInflater mInflater;
	private Activity activity;
	private int imagePanel;

	private SimpleFilter mFilter;
	private ArrayList<Map<String, ?>> mUnfilteredData;
	private JSONArray top;

	public IndexRecommendAdapterOld(Context context, List<? extends Map<String, ?>> data, int[] resources,
			String[] from, int[] to, Activity mactivity, int imagePanel, JSONArray top) {
		mData = data;
		mResources = mDropDownResources = resources;
		mFrom = from;
		mTo = to;
		if (context != null) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		this.activity = mactivity;
		this.imagePanel = imagePanel;
		this.top = top;
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

		if (position == 0) {
			if (convertView == null) {
				try {
					convertView = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.panel_service_index_top, null);
					
					System.out.println(top.toString());
					
					ViewFlipper flipper = (ViewFlipper) convertView.findViewById(R.id.panel_service_index_title);

					JSONObject jsonObject_1 = top.getJSONObject(0);
					View view_flipper_1 = LayoutInflater.from(activity.getApplicationContext())
							.inflate(R.layout.index_recommend_flipper, null);
					ImageView imageView_1 = (ImageView) view_flipper_1.findViewById(R.id.index_recommend_flipper_img);
					Picasso.with(activity).load(jsonObject_1.getString("data")).into(imageView_1);
					TextView textView_1 = (TextView) view_flipper_1.findViewById(R.id.index_recommend_flipper_text);
					textView_1.setText(jsonObject_1.getString("name"));
					final String top_1_id = jsonObject_1.getString("id");
					view_flipper_1.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
//							Intent intent = new Intent(activity, PanelServiceNewsContent.class);
//							intent.putExtra("id", top_1_id);
//							activity.startActivity(intent);
//							activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
						}
					});

					JSONObject jsonObject_2 = top.getJSONObject(1);
					View view_flipper_2 = LayoutInflater.from(activity.getApplicationContext())
							.inflate(R.layout.index_recommend_flipper, null);
					ImageView imageView_2 = (ImageView) view_flipper_2.findViewById(R.id.index_recommend_flipper_img);
					Picasso.with(activity).load(jsonObject_2.getString("data")).into(imageView_2);
					TextView textView_2 = (TextView) view_flipper_2.findViewById(R.id.index_recommend_flipper_text);
					textView_2.setText(jsonObject_2.getString("name"));
					final String top_2_id = jsonObject_2.getString("id");
					view_flipper_2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
//							Intent intent = new Intent(activity, PanelServiceNewsContent.class);
//							intent.putExtra("id", top_2_id);
//							activity.startActivity(intent);
//							activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
						}
					});
					flipper.addView(view_flipper_1);
					flipper.addView(view_flipper_2);
					flipper.setInAnimation(activity, R.anim.in_right);
					flipper.setOutAnimation(activity, R.anim.out_left);
					flipper.setFlipInterval(5000);
					flipper.startFlipping();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (convertView == null) {
				convertView = mInflater.inflate(mResources[getItemViewType(position)], null);
				mHolder = new ViewHolder();
				mHolder.mImageView = (ImageView) convertView.findViewById(this.imagePanel);
				convertView.setTag(mHolder);
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}

			String url = PubFunction.www + mData.get(position).get("thumb").toString();
			Picasso.with(activity).load(url).into(mHolder.mImageView);

			final String tem_id = mData.get(position).get("id").toString();
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(activity, PanelServiceNewsContent.class);
					intent.putExtra("id", tem_id);
					activity.startActivity(intent);
					activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
				}
			});

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
		if (position == 0) {
			return 0;
		} else {
			return 1;
		}
	}

	private class ViewHolder {
		ImageView mImageView;
		TextView mTextView;
	}
}
