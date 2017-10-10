package com.example.fullenergy.main;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;

import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.FragementPagerAdapter.MyFragmentPagerAdapter;
import com.example.fullenergy.extend_plug.ViewPagerScroller.ViewPagerScroller;

import org.json.JSONException;
import org.json.JSONObject;

public class PanelMineAppo extends Fragment implements OnClickListener {

	private View view;
	private LinearLayout panelMineAppoReturn;
	private Fragment fragment;

	private ViewPager viewPager;
	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mLinearLayout;
	private ImageView mImageView;
	private int mScreenWidth;
	private int item_width;
	private RelativeLayout nav_rel;
	private ArrayList<Fragment> fragments;
	private String[] str = new String[] { "预约进行中", "预约完成", "预约超时" };
	private int endPosition;
	private int beginPosition;
	private int currentFragmentIndex;
	private boolean isEnd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.panel_mine_appo, container, false);
		fragment = this;

		init();
		handler();
		main();
		return view;
	}

	private void handler() {

	}

	private void init() {
		panelMineAppoReturn = (LinearLayout) view.findViewById(R.id.panelMineAppoReturn);
		panelMineAppoReturn.setOnClickListener(this);

		mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.panelMineAppoHorizontal);
		viewPager = (ViewPager) view.findViewById(R.id.panelMineAppoViewpager);
		mLinearLayout = (LinearLayout) view.findViewById(R.id.panelMineAppoHorizontalLinearLayout);
		mImageView = (ImageView) view.findViewById(R.id.panelMineAppoHorizontalImg);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		item_width = (int) ((mScreenWidth / 3.0 + 0.5f));
		mImageView.getLayoutParams().width = item_width;
		initViewPager();// 页面初始化
		initNav();// 导航初始化
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(1);
	}

	private void main() {

	}

	private void initViewPager() {
		fragments = new ArrayList<Fragment>();
		for (int i = 0; i < str.length; i++) {
			fragments.add(new PanelMineAppoItem(i));
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

	private void initNav() {
		for (int i = 0; i < str.length; i++) {
			nav_rel = new RelativeLayout(getActivity());
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.panel_shop_index_top_text, null);
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(str[i]);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,     ViewGroup.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);//居中显示。
			nav_rel.addView(view,lp);
			mLinearLayout.addView(nav_rel, (int) (mScreenWidth / 3 + 0.5f), 50);
			nav_rel.setOnClickListener(this);
			nav_rel.setTag(i);
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == panelMineAppoReturn.getId()) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineIndex index = new PanelMineIndex();
			fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, index);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
		if (arg0.getId() == nav_rel.getId()) {
			viewPager.setCurrentItem((Integer) arg0.getTag());
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
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
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
}
