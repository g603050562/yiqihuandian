package com.example.fullenergy.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.gaode.*;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

public class GreenNavigation extends com.example.fullenergy.pub.gaode.BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.green_navigation);
		mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
		mAMapNaviView.onCreate(savedInstanceState);
		mAMapNaviView.setAMapNaviViewListener(this);

	}

	@Override
	public void onInitNaviSuccess() {
		super.onInitNaviSuccess();
		/**
		 * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
		 *
		 * @congestion 躲避拥堵
		 * @avoidhightspeed 不走高速
		 * @cost 避免收费
		 * @hightspeed 高速优先
		 * @multipleroute 多路径
		 *
		 *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
		 *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
		 */
		int strategy = 0;
		try {
			//再次强调，最后一个参数为true时代表多路径，否则代表单路径
			strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAMapNavi.setCarNumber("京", "DFZ588");
		mAMapNavi.calculateDriveRoute(startList, endList, wayPointList, strategy);
	}

	@Override
	public void onCalculateRouteSuccess() {
		super.onCalculateRouteSuccess();
		mAMapNavi.startNavi(NaviType.GPS);
	}
}
