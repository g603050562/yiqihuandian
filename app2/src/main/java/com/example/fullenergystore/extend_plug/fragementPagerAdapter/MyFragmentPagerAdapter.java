package com.example.fullenergystore.extend_plug.fragementPagerAdapter;

import java.util.ArrayList;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import android.widget.Toast;

public class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {
	private ArrayList<Fragment> fragments;
	private FragmentManager fm;
	private Activity activity;

	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
		notifyDataSetChanged();
	}

	public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments , Activity activity) {
		super(fm);
		this.fm = fm;
		this.fragments = fragments;
		this.activity = activity;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getItemPosition(Object object) {
		return this.POSITION_NONE;
	}

	public void setFragments(ArrayList<Fragment> fragments) {
		this.fragments = fragments;
		if (this.fragments != null) {
			FragmentTransaction ft = fm.beginTransaction();
			for (Fragment f : this.fragments) {
				Toast.makeText(activity, f+"", Toast.LENGTH_LONG).show();
				ft.remove(f);
			}
			ft.commit();
			ft = null;
			fm.executePendingTransactions();
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Object obj = super.instantiateItem(container, position);
		return obj;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		 super.destroyItem(container, position, object);
	}
	
	
}