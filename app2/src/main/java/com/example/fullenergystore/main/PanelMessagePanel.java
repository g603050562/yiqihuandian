package com.example.fullenergystore.main;


import java.util.ArrayList;


import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.fragementPagerAdapter.MyFragmentPagerAdapter;
import com.example.fullenergystore.extend_plug.viewPagerScroller.ViewPagerScroller;

public class PanelMessagePanel extends Fragment implements OnClickListener{
	
	private View view;
	public static Handler panelMessageTurn;
	
	private ViewPager viewPager;
	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mLinearLayout,setup;
	private ImageView mImageView;
	private int mScreenWidth;
	private int item_width;
	private RelativeLayout nav_rel;
	private ArrayList<Fragment> fragments;
	private String[] str = new String[] { "系统消息", "充电记录", "提现记录","订单记录"};
	private int endPosition;
	private int beginPosition;
	private int currentFragmentIndex;
	private boolean isEnd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_message_panel,container,false);
		
		init();
		main();
		
		return view;
	}
	
	private void init(){

		setup = (LinearLayout) view.findViewById(R.id.setup);
		setup.setOnClickListener(this);
		
		mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.panelMineAppoHorizontal);
		viewPager = (ViewPager) view.findViewById(R.id.panelMineAppoViewpager);
		mLinearLayout = (LinearLayout) view.findViewById(R.id.panelMineAppoHorizontalLinearLayout);
		mImageView = (ImageView) view.findViewById(R.id.panelMineAppoHorizontalImg);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		item_width = (int) ((mScreenWidth / 4.0 + 0.5f));
		mImageView.getLayoutParams().width = item_width;
		initViewPager();// 页面初始化
		initNav();// 导航初始化
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(1);
	};
	
	private void main(){
		
	}
	
	private void initViewPager() {
		fragments = new ArrayList<Fragment>();
		fragments.add(new PanelMessageIndex());
		fragments.add(new PanelMessageCharge());
		fragments.add(new PanelMessageWithdrawList());
		fragments.add(new PanelMessageOrder());
	
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
			TextView view = new TextView(getActivity());
			view.setText(str[i]);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == nav_rel.getId()) {
			viewPager.setCurrentItem((Integer) v.getTag());
		}
		if(v.getId() == setup.getId()){
			Intent intent = new Intent(getActivity(),SetUp.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
		}
	}

}

