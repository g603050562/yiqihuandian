package com.example.fullenergystore.main;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.fragementPagerAdapter.MyFragmentPagerAdapter;
import com.example.fullenergystore.extend_plug.viewPagerScroller.ViewPagerScroller;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class PanelShopIndex extends Fragment implements OnClickListener {

	private View view;
	private ViewPager viewPager;
	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mLinearLayout,setup;
	private ImageView mImageView;
	private int mScreenWidth;
	private int item_width;
	private RelativeLayout nav_rel;
	private SharedPreferences preferences;

	private ArrayList<Fragment> fragments;

	private int endPosition;
	private int beginPosition;
	private int currentFragmentIndex;
	private boolean isEnd;
	private HttpPanelShopIndex th;
	private ProgressDialog dialog;

	public static Handler setCurrentItemHandler, panelShopIndexSuccessHandler, panelShopIndexErrorHandler,
			panelShopIndexUnknownHandler, turnToLogin;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_shop_index, container, false);

		init();
		handler();
		main();

		return view;
	}

	private void init(){
		setup = (LinearLayout) view.findViewById(R.id.setup);
		setup.setOnClickListener(this);
	}

	private void dataInit(JSONArray catname) {
		mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.panelShopIndexHorizontal);
		viewPager = (ViewPager) view.findViewById(R.id.panelShopIndexViewpager);
		mLinearLayout = (LinearLayout) view.findViewById(R.id.panelShopIndexHorizontalLinearLayout);
		mImageView = (ImageView) view.findViewById(R.id.panelShopIndexHorizontalImg);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		item_width = (int) ((mScreenWidth / 4.0 + 0.5f));
		mImageView.getLayoutParams().width = item_width;
		initViewPager(catname);// 页面初始化
		initNav(catname);// 导航初始化
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(1);
	}

	private void handler() {
		setCurrentItemHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int page = msg.getData().getInt("page");
				viewPager.setCurrentItem(page);
			}
		};
		panelShopIndexUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		};
		panelShopIndexSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					JSONArray catname = jsonObject.getJSONArray("catname");
					dataInit(catname);
					dialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		panelShopIndexErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
				Intent intent = new Intent(getActivity(), Login.class);
				intent.putExtra("type", "1");
				getActivity().startActivity(intent);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
			}
		};
	}

	private void initViewPager(JSONArray catname) {
		fragments = new ArrayList<Fragment>();
		for (int i = 0; i < catname.length(); i++) {
			try {
				JSONObject jsonObject = catname.getJSONObject(i);
				String catId = jsonObject.getString("id");
				fragments.add(new PanelShopIndexItem(Integer.parseInt(catId)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FragmentStatePagerAdapter adapter = new MyFragmentPagerAdapter(getChildFragmentManager(), fragments,
				getActivity());
		viewPager.setAdapter(adapter);
		ViewPagerScroller scroller = new ViewPagerScroller(getActivity());
		scroller.setScrollDuration(1000);
		scroller.initViewPagerScroll(viewPager);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setCurrentItem(0);
	}

	private void initNav(JSONArray catname) {
		for (int i = 0; i < catname.length(); i++) {
			nav_rel = new RelativeLayout(getActivity());
			TextView view = new TextView(getActivity());
			try {
				JSONObject jsonObject = catname.getJSONObject(i);
				view.setText(jsonObject.getString("catname_goods"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			view.setTextSize(13);
			view.setTextColor(0xff666666);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			nav_rel.addView(view, params);
			mLinearLayout.addView(nav_rel, (int) (mScreenWidth / 4 + 0.5f), 50);
			nav_rel.setOnClickListener(this);
			nav_rel.setTag(i);
		}
	}

	private void main() {
		if (PubFunction.isNetworkAvailable(getActivity())) {
			dialog = Panel.progressDialog;
			preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
			th = new HttpPanelShopIndex(preferences, this, getActivity());
			th.start();
		} else {
			Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(final int position) {
			Animation animation = new TranslateAnimation(endPosition, position * item_width, 0, 0);

			beginPosition = position * item_width;

			currentFragmentIndex = position;
			if (animation != null) {
				animation.setFillAfter(true);
				animation.setDuration(0);
				mImageView.startAnimation(animation);
				mHorizontalScrollView.smoothScrollTo((currentFragmentIndex - 1) * item_width, 0);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (!isEnd) {
				if (currentFragmentIndex == position) {
					endPosition = item_width * currentFragmentIndex + (int) (item_width * positionOffset);
				}
				if (currentFragmentIndex == position + 1) {
					endPosition = item_width * currentFragmentIndex - (int) (item_width * (1 - positionOffset));
				}

				Animation mAnimation = new TranslateAnimation(beginPosition, endPosition, 0, 0);
				mAnimation.setFillAfter(true);
				mAnimation.setDuration(0);
				mImageView.startAnimation(mAnimation);
				mHorizontalScrollView.invalidate();
				beginPosition = endPosition;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				isEnd = false;
			} else if (state == ViewPager.SCROLL_STATE_SETTLING) {
				isEnd = true;
				beginPosition = currentFragmentIndex * item_width;
				if (viewPager.getCurrentItem() == currentFragmentIndex) {
					// 未跳入下一个页面
					mImageView.clearAnimation();
					Animation animation = null;
					// 恢复位置
					animation = new TranslateAnimation(endPosition, currentFragmentIndex * item_width, 0, 0);
					animation.setFillAfter(true);
					animation.setDuration(1);
					mImageView.startAnimation(animation);
					mHorizontalScrollView.invalidate();
					endPosition = currentFragmentIndex * item_width;
				}
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == nav_rel.getId()) {
			viewPager.setCurrentItem((Integer) arg0.getTag());
		}else if(arg0.getId() == setup.getId()){
			Intent intent = new Intent(getActivity(),SetUp.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
		}
	}
}

class HttpPanelShopIndex extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Fragment fragment;
	private Activity activity;

	public HttpPanelShopIndex(SharedPreferences preferences, Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.fragment = fragment;
		this.activity = activity;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String apibus_businessid = preferences.getString("apibus_businessid", null);
		String apibus_username = preferences.getString("apibus_username", null);

		String path = PubFunction.www + "api_business.php/shop/category_goods";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";"
				+ "apibus_username=" + apibus_username);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

				// System.out.println(jsonObject.toString());

				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				this.jsonObject = jsonObject.getJSONObject("data");

				if (fragment.getView() != null) {
					if (code.equals("200")) {
						if (messageStr.equals("请重新登录")) {
							PanelShopIndex.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelShopIndex.panelShopIndexErrorHandler.sendMessage(message);
						}
					} else if (code.equals("100")) {
						PanelShopIndex.panelShopIndexSuccessHandler.sendMessage(new Message());
					} else {
						PanelShopIndex.panelShopIndexUnknownHandler.sendMessage(new Message());
					}
				}
			} else {
				System.out.println(httpResponse.getStatusLine().getStatusCode() + "");
			}
		} catch (Exception e) {
			if (fragment.getView() != null) {
				PanelShopIndex.panelShopIndexUnknownHandler.sendMessage(new Message());
			}
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}
