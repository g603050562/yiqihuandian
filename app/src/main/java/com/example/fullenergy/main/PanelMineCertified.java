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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineCertified extends Fragment implements OnClickListener {

	private View view, gridViewEndView;
	private LinearLayout panelMineCertifiedReturn;
	private SwipeMenuListView gridView;
	private PanelMineCertifiedAdapter adapter;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private SharedPreferences preferences;
	private HttpPanelMineCertified th;
	private HttpPanelMineDeleteCertified th1;
	private Fragment fragment;
	private ProgressDialog dialog;

	public static Handler panelMineCertifiedSuccessHandler, panelMineCertifiedErrorHandler,
			panelMineCertifiedUnknownHandler,turnToLogin,panelMineDeleteSuccessHandler,panelMineDeleteErrorHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.panel_mine_certified, container, false);
		fragment = this;

		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		// TODO Auto-generated method stub
		panelMineCertifiedSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				list.clear();
				JSONArray array = th.getResult();

				adapter = new PanelMineCertifiedAdapter(getActivity(), getdata(array),
						new int[] { R.layout.panel_mine_certified_item, R.layout.panel_mine_certified_item_add },
						new String[] { "number", "date", "battery", "batteryID" },
						new int[] { R.id.panelMineCertifiedItemNmunber, R.id.panelMineCertifiedItemDate,
								R.id.panelMineCertifiedItemBattery, R.id.panelMineCertifiedItemBatteryID });
				gridView.setAdapter(adapter);
				
				SwipeMenuCreator creator = new SwipeMenuCreator() {

					@Override
					public void create(SwipeMenu menu) {
						// create "delete" item
						SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
						// set item background
						deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
								0x3F, 0x25)));
						// set item width
						deleteItem.setWidth(PubFunction.dip2px(getActivity(), 90));
						// set a icon
						deleteItem.setIcon(R.drawable.ic_delete);
						// add to menu
						menu.addMenuItem(deleteItem);
					}
				};
				// set creator
				gridView.setMenuCreator(creator);
				
				gridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (position == list.size() - 1) {
							if(list.size()>=2){
								Toast.makeText(getActivity(),"请先删除已存在的验证（左划删除）！",Toast.LENGTH_LONG).show();
							}else{
								PanelMineCertifiedInfoAdd panelMineCertifiedInfoAdd = new PanelMineCertifiedInfoAdd();
								FragmentManager fragmentManager = getFragmentManager();
								FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
								fragmentTransaction.addToBackStack(null);
								fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
								fragmentTransaction.replace(R.id.panelMinePanel, panelMineCertifiedInfoAdd);
								fragmentTransaction.commit();
							}
						}
					}
				});
				
				gridView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
					@Override
					public void onMenuItemClick(int position, SwipeMenu menu, int index) {
						switch (index) {
						case 0:
							// open
							String id = list.get(position).get("id").toString();
							th1 = new HttpPanelMineDeleteCertified(preferences, fragment, getActivity(), id);	
							th1.start();
							break;
						}
					}
				});
				
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		};
		panelMineCertifiedErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				list.clear();
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				adapter = new PanelMineCertifiedAdapter(getActivity(), getdata(),
						new int[] { R.layout.panel_mine_certified_item, R.layout.panel_mine_certified_item_add },
						new String[] { "number", "date", "battery", "batteryID" },
						new int[] { R.id.panelMineCertifiedItemNmunber, R.id.panelMineCertifiedItemDate,
								R.id.panelMineCertifiedItemBattery, R.id.panelMineCertifiedItemBatteryID });
				gridView.setAdapter(adapter);
				
				gridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (position == list.size() - 1) {
//							Toast.makeText(getActivity(),list.size()+"aaa",Toast.LENGTH_LONG).show();
							PanelMineCertifiedInfoAdd panelMineCertifiedInfoAdd = new PanelMineCertifiedInfoAdd();
							FragmentManager fragmentManager = getFragmentManager();
							FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
							fragmentTransaction.addToBackStack(null);
							fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
							fragmentTransaction.replace(R.id.panelMinePanel, panelMineCertifiedInfoAdd);
							fragmentTransaction.commit();
						}
					}
				});
				dialog.dismiss();
			}
		};
		panelMineCertifiedUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
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
				dialog.dismiss();
			}
		};
		
		panelMineDeleteSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "删除成功！", Toast.LENGTH_SHORT).show();
				th = new HttpPanelMineCertified(preferences, fragment, getActivity());
				th.start();
			}
		};
		
		panelMineDeleteErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "删除失败！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
	}

	private void init() {
		dialog = Panel.progressDialog;

		gridView = (SwipeMenuListView) view.findViewById(R.id.panelMineCertifiedGridView);
		gridView.setDividerHeight(1);
		panelMineCertifiedReturn = (LinearLayout) view.findViewById(R.id.panelMineCertifiedReturn);
		panelMineCertifiedReturn.setOnClickListener(this);

	}

	private List<Map<String, Object>> getdata(JSONArray array) {
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				try {
					JSONObject object = array.getJSONObject(i);
					map.put("number", object.getString("frame_number"));
					map.put("id", object.getString("id"));
					map.put("userid", object.getString("userid"));
					Long date = Long.parseLong(object.get("time").toString()) * 1000;
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String dateTime = df.format(date);
					map.put("date", dateTime);
					map.put("battery", object.getString("catname_battery"));
					map.put("batteryID", object.getString("battery_number"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map.put("type", "normal");
				list.add(map);
			}

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("type", "add");
			list.add(map);
		}
		return list;
	}

	private List<Map<String, Object>> getdata() {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", "add");
		list.add(map);
		return list;
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			dialog.show();
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_APPEND);
			th = new HttpPanelMineCertified(preferences, this, getActivity());
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineCertifiedReturn.getId()) {
			PanelMineIndex panelMineIndex = new PanelMineIndex();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, panelMineIndex);
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
					PanelMineIndex panelMineIndex = new PanelMineIndex();
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, panelMineIndex);
					fragmentTransaction.commit();
					return true;
				}
				return false;
			}
		});
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		list.clear();
		view = null;
		fragment = null;
		System.gc();
	}

}

class HttpPanelMineCertified extends Thread {

	private SharedPreferences preferences;
	private JSONArray jsonArray;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelMineCertified(SharedPreferences preferences, Fragment fragment, Activity activity) {
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

		String path = PubFunction.www + "api.php/member/my_car";
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
							PanelMineCertified.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineCertified.panelMineCertifiedErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonArray = jsonObject.getJSONArray("data");
						PanelMineCertified.panelMineCertifiedSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineCertified.panelMineCertifiedUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.out.println(e.toString());
				PanelMineCertified.panelMineCertifiedUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return jsonArray;
	}
}

class HttpPanelMineDeleteCertified extends Thread {

	private SharedPreferences preferences;
	private JSONArray jsonArray;
	private Fragment fragment;
	private Activity activity;
	private String id;

	public HttpPanelMineDeleteCertified(SharedPreferences preferences, Fragment fragment, Activity activity,String id) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
		this.id = id;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);

		String path = PubFunction.www + "api.php/member/delete_my_car";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie",
				"PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", id));

		try {
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				
				System.out.println(jsonObject.toString());
				
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMineCertified.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMineCertified.panelMineDeleteErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						this.jsonArray = jsonObject.getJSONArray("data");
						PanelMineCertified.panelMineDeleteSuccessHandler.sendMessage(new Message());
					} else {
						PanelMineCertified.panelMineCertifiedUnknownHandler.sendMessage(new Message());
					}
				}
			}else{
				System.out.println(httpResponse.getStatusLine().getStatusCode()+"");
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				System.out.println(e.toString());
				PanelMineCertified.panelMineCertifiedUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONArray getResult() {
		return jsonArray;
	}
}

class PanelMineCertifiedAdapter extends BaseAdapter {
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

	public PanelMineCertifiedAdapter(Context context, List<? extends Map<String, ?>> data, int[] resources,
			String[] from, int[] to) {
		mData = data;
		mResources = mDropDownResources = resources;
		mFrom = from;
		mTo = to;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		if (convertView == null) {
			
		}else{
			
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
		if (mData.get(position).get("type").toString().equals("normal")) {
			return 0;
		} else if (mData.get(position).get("type").toString().equals("add")) {
			return 1;
		} else {
			return 0;
		}
	}

	private class ViewHolder {
		ImageView mImageView;
		TextView mTextView;
	}
}
