package com.example.hasee.app7;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by hasee on 2017/3/26.
 */
public class AppAdapter extends BaseAdapter {
    List<ResolveInfo> mApps;
    Context context;

    public AppAdapter(Context context, List<ResolveInfo> apps) {
        super();
        this.context = context;
        this.mApps = apps;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;
        if (convertView == null) {
            iv = new ImageView(context);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setLayoutParams(new GridView.LayoutParams(50,50));
        } else {
            iv = (ImageView) convertView;
        }
        ResolveInfo info = mApps.get(position);
        iv.setImageDrawable(info.activityInfo.loadIcon(context.getPackageManager()));
        return iv;
    }

}