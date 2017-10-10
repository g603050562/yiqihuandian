package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
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
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.gaode.TTSController;

public class PanelIndexNavigation extends Activity implements OnClickListener,AMapNaviListener, AMapNaviViewListener {

	private LinearLayout panelIndexNavigationReturn;
	public AMapNaviView naviView;
	public AMapNavi aMapNavi;
	
	NaviLatLng endLatlng;
	NaviLatLng startLatlng;
	List<NaviLatLng> startList;
	List<NaviLatLng> endList;
	List<NaviLatLng> wayPointList;
	
	public PanelIndexNavigation() {
		endLatlng = new NaviLatLng(PubFunction.marker[0], PubFunction.marker[1]);
		startLatlng = new NaviLatLng(PubFunction.local[0], PubFunction.local[1]);
		startList = new ArrayList<NaviLatLng>();
		endList = new ArrayList<NaviLatLng>();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_index_navigation);

		naviView = (AMapNaviView) findViewById(R.id.navi_view);
		naviView.onCreate(savedInstanceState);
		naviView.setAMapNaviViewListener(this);

		init();
	}

	private void init() {


		
	    new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.title_black);


		// 为了尽最大可能避免内存泄露问题，建议这么写
		aMapNavi = AMapNavi.getInstance(getApplicationContext());
		aMapNavi.setAMapNaviListener(this);
		aMapNavi.setEmulatorNaviSpeed(150);
		
		panelIndexNavigationReturn = (LinearLayout) this.findViewById(R.id.panelIndexNavigationReturn);
		panelIndexNavigationReturn.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		if (panelIndexNavigationReturn.getId() == arg0.getId()) {
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (naviView != null) {
			naviView.onResume();
		}
		if (startList != null) {
			startList.add(startLatlng);
		}
		if (endList != null) {
			endList.add(endLatlng);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.finish();
			this.overridePendingTransition(R.anim.in_left, R.anim.out_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (naviView != null) {
			naviView.onPause();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (naviView != null) {
			naviView.onDestroy();
		}
		if (aMapNavi != null) {
			aMapNavi.destroy();
		}
	}

	@Override
	public void onLockMap(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviViewLoaded() {

	}

	@Override
	public boolean onNaviBackClick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onNaviCancel() {
		// TODO Auto-generated method stub
		finish();
	}

	@Override
	public void onNaviMapMode(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviSetting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviTurnClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNextRoadClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanViewButtonClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

	}

	@Override
	public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

	}

	@Override
	public void onPlayRing(int i) {

	}

	@Override
	public void hideCross() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hideLaneInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCalculateMultipleRoutesSuccess(int[] ints) {

	}

	@Override
	public void notifyParallelRoad(int i) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

	}

	@Override
	public void onArriveDestination() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrivedWayPoint(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCalculateRouteFailure(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCalculateRouteSuccess() {
		// TODO Auto-generated method stub
		aMapNavi.startNavi(AMapNavi.GPSNaviMode);

	}

	@Override
	public void onEndEmulatorNavi() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetNavigationText(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGpsOpenStatus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitNaviFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitNaviSuccess() {
		// TODO Auto-generated method stub
		aMapNavi.calculateDriveRoute(startList, endList, wayPointList, 5);
	}

	@Override
	public void onLocationChange(AMapNaviLocation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviInfoUpdated(AMapNaviInfo arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

	}

	@Override
	public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReCalculateRouteForYaw() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartNavi(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrafficStatusUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void showCross(AMapNaviCross arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
		// TODO Auto-generated method stub

	}

}
