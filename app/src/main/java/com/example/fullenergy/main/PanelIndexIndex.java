package com.example.fullenergy.main;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.a.a.a;
import com.alipay.a.a.c;
import com.alipay.a.a.f;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.gaode.My3dMapView;
import com.example.fullenergy.pub.gaode.MyGaodeImageView;
import com.google.gson.JsonArray;
import com.tandong.bottomview.view.BottomView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.proto.am;

import static com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap;

public class PanelIndexIndex extends Fragment implements LocationSource, AMapLocationListener, OnMarkerClickListener,
        OnInfoWindowClickListener, OnMarkerDragListener, OnMapLoadedListener, OnClickListener, InfoWindowAdapter,
        OnCameraChangeListener, OnMapClickListener, OnMapLongClickListener, OnGeocodeSearchListener {

    private View view;

    private My3dMapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;
    private MyLocationStyle myLocationStyle;

    public static Handler panelIndexIndexGetMarkerSuccessHandler, panelIndexIndexGetMarkerErrorHandler,
            panelIndexIndexUnknownHandler, getGeoCoderHandler, turnToLogin;

    private LinearLayout selectLayout, locationLayout, helpLayout;
    boolean selectStatiu = false;
    private LinearLayout selectButton, selectList;
    private TextView selectButtonText;
    private ImageView selectButtonImage;

    private List<JSONObject> markerList = new ArrayList<JSONObject>();
    private AMapLocation amapLocation;

    private HttpPanelIndexIndexGetMarker th;
    private GeocodeSearch geocoderSearch;
    private int further = 10000000;
    private ProgressDialog dialog;

    private boolean is_local = false;

    ArrayList<Marker> old_clustersMarker = new ArrayList<Marker>();
    /**
     * 所有的marker
     */
    private List<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
    /**
     * 视野内的marker
     */
    private ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();
    private BitmapDescriptor bitmapDescriptor_1;
    private BitmapDescriptor bitmapDescriptor_2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.panel_index_index, container, false);

        init(savedInstanceState);
        handler();

        return view;
    }

    private void handler() {

        panelIndexIndexUnknownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        };

        getGeoCoderHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (amapLocation != null) {
                    LatLonPoint latLonPoint = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
                    RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                    geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
                }
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
    }

    ;

    private void init(Bundle savedInstanceState) {

        dialog = Panel.progressDialog;

        Resources res_1 = getResources();
        Bitmap bmp_1 = BitmapFactory.decodeResource(res_1, R.drawable.map_01);

        Resources res_2 = getResources();
        Bitmap bmp_2 = BitmapFactory.decodeResource(res_2, R.drawable.map_04);

        int width = bmp_1.getWidth();
        int height = bmp_1.getHeight();
        // 设置想要的大小
        int newWidth = PubFunction.dip2px(getActivity(),30);
        int newHeight = PubFunction.dip2px(getActivity(),32);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm_1 = Bitmap.createBitmap(bmp_1, 0, 0, width, height, matrix,true);
        Bitmap newbm_2 = Bitmap.createBitmap(bmp_2, 0, 0, width, height, matrix,true);

        BitmapDescriptor var1 = fromBitmap(newbm_1);
        bitmapDescriptor_1 = var1;


        BitmapDescriptor var2 = fromBitmap(newbm_2);
        bitmapDescriptor_2 = var2;

        try{
            mapView = (My3dMapView) view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);// 此方法必须重写

            if (aMap == null) {
                aMap = mapView.getMap();
                setUpMap();
            }

        }catch (Exception e){
            System.out.println(e.toString());
        }


        selectLayout = (LinearLayout) view.findViewById(R.id.panel_index_index_select);
        selectLayout();
        locationLayout = (LinearLayout) view.findViewById(R.id.panel_index_index_location);
        locationLayout();
        helpLayout = (LinearLayout) view.findViewById(R.id.panel_index_index_help);
        helpLayout();
    }

    ;

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        is_local = false;
        deactivate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // mapView.onDestroy();
        if (markerList != null) {
            markerList.clear();
        }
        if (markerOptionsList != null) {
            markerOptionsList.clear();
        }
        if (markerOptionsListInView != null) {
            markerOptionsListInView.clear();
        }
    }

    private void locationLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View locationView = inflater.inflate(R.layout.panel_index_index_location, null);
        locationLayout.addView(locationView);
        locationView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (!is_local) {
                    show_no_local();
                } else {
                    LatLng location = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
                    aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(location, 15, 0, 0)), 1000, null);
                }
            }
        });
    }

    private void helpLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View selectView = inflater.inflate(R.layout.panel_index_index_help, null);
        helpLayout.addView(selectView);
        TextView help = (TextView) selectView.findViewById(R.id.help);
        help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), PanelServiceHelp.class);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
                getActivity().finish();
                System.gc();
            }
        });
    }

    private void selectLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View selectView = inflater.inflate(R.layout.panel_index_index_select, null);
        selectLayout.addView(selectView);
        selectButton = (LinearLayout) selectView.findViewById(R.id.selectButton);
        selectButtonText = (TextView) selectView.findViewById(R.id.selectButtonText);
        selectList = (LinearLayout) selectView.findViewById(R.id.selectList);
        selectButtonImage = (ImageView) selectView.findViewById(R.id.selectButtonImage);
        final TextView panelIndexIndexSelect1 = (TextView) selectView.findViewById(R.id.panelIndexIndexSelect1);
        final TextView panelIndexIndexSelect2 = (TextView) selectView.findViewById(R.id.panelIndexIndexSelect2);
        final TextView panelIndexIndexSelect3 = (TextView) selectView.findViewById(R.id.panelIndexIndexSelect3);
        final TextView panelIndexIndexSelect4 = (TextView) selectView.findViewById(R.id.panelIndexIndexSelect4);
        panelIndexIndexSelect1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!is_local) {
                    show_no_local();
                } else {
                    selectButtonText.setText("5公里以内充电点");
                    selectButtonText.setTextColor(0xff666666);
                    selectButtonText.setTextSize(11);
                    panelIndexIndexSelect1.setBackgroundColor(0xeeff9c2c);
                    panelIndexIndexSelect1.setTextColor(0xffffffff);
                    panelIndexIndexSelect2.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect2.setTextColor(0xff666666);
                    panelIndexIndexSelect3.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect3.setTextColor(0xff666666);
                    panelIndexIndexSelect4.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect4.setTextColor(0xff666666);
                    further = 5;
                }
                closeSelectLayout(1);
            }
        });
        panelIndexIndexSelect2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_local) {
                    show_no_local();
                } else {
                    selectButtonText.setText("10公里以内充电点");
                    selectButtonText.setTextColor(0xff666666);
                    selectButtonText.setTextSize(11);
                    panelIndexIndexSelect1.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect1.setTextColor(0xff666666);
                    panelIndexIndexSelect2.setBackgroundColor(0xeeff9c2c);
                    panelIndexIndexSelect2.setTextColor(0xffffffff);
                    panelIndexIndexSelect3.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect3.setTextColor(0xff666666);
                    panelIndexIndexSelect4.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect4.setTextColor(0xff666666);
                    further = 10;
                }
                closeSelectLayout(1);
            }
        });
        panelIndexIndexSelect3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_local) {
                    show_no_local();
                } else {
                    selectButtonText.setText("50公里以内充电点");
                    selectButtonText.setTextColor(0xff666666);
                    selectButtonText.setTextSize(11);
                    panelIndexIndexSelect1.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect1.setTextColor(0xff666666);
                    panelIndexIndexSelect2.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect2.setTextColor(0xff666666);
                    panelIndexIndexSelect3.setBackgroundColor(0xeeff9c2c);
                    panelIndexIndexSelect3.setTextColor(0xffffffff);
                    panelIndexIndexSelect4.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect4.setTextColor(0xff666666);
                    further = 50;
                }
                closeSelectLayout(1);
            }
        });

        panelIndexIndexSelect4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_local) {
                    show_no_local();
                } else {
                    selectButtonText.setText("全部充电点");
                    selectButtonText.setTextColor(0xff666666);
                    selectButtonText.setTextSize(11);
                    panelIndexIndexSelect1.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect1.setTextColor(0xff666666);
                    panelIndexIndexSelect2.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect2.setTextColor(0xff666666);
                    panelIndexIndexSelect3.setBackgroundColor(0xeeffffff);
                    panelIndexIndexSelect3.setTextColor(0xff666666);
                    panelIndexIndexSelect4.setBackgroundColor(0xeeff9c2c);
                    panelIndexIndexSelect4.setTextColor(0xffffffff);
                    further = 10000000;
                }
                closeSelectLayout(1);
            }
        });
        selectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!selectStatiu) {
                    AnimationSet set = new AnimationSet(true);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 1.0f);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                            PubFunction.dip2px(getActivity(), 75), PubFunction.dip2px(getActivity(), 75),
                            PubFunction.dip2px(getActivity(), 75), PubFunction.dip2px(getActivity(), 75));
                    set.addAnimation(alphaAnimation);
                    set.addAnimation(scaleAnimation);
                    set.setDuration(100);
                    selectList.startAnimation(set);
                    selectList.setVisibility(View.VISIBLE);
                    selectStatiu = true;
                    RotateAnimation rotateAnimation = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(100);
                    rotateAnimation.setFillAfter(true);
                    selectButtonImage.startAnimation(rotateAnimation);
                } else {
                    closeSelectLayout(0);
                }
            }
        });
    }

    private void closeSelectLayout(int type) { // 1为按钮点击关闭面板，0为其他点击关闭面板
        final int temp_type = type;
        if (selectStatiu) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(300);
            alphaAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    selectList.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    selectList.setVisibility(View.GONE);
                    if (temp_type == 1) {
                        addMarkersToMap(further);
                    }
                }
            });
            selectList.startAnimation(alphaAnimation);
            selectStatiu = false;
            RotateAnimation rotateAnimation = new RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(100);
            rotateAnimation.setFillAfter(true);
            selectButtonImage.startAnimation(rotateAnimation);
        }

    }

    private void setUpMap() {
        // 自定义系统定位小蓝点
        setLocation();
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.getUiSettings().setTiltGesturesEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        // 设置收缩按钮
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        // 设置marker
        aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
        geocoderSearch = new GeocodeSearch(getActivity());
        geocoderSearch.setOnGeocodeSearchListener(this);
        // 往地图上添加marker
        addMarkersToMap();
    }

    private void setLocation() {
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(0x00000000);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(0.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        // aMap.setMyLocationType();
    }

    private void addMarkersToMap() {

        if (PubFunction.isNetworkAvailable(getActivity())) {
            th = new HttpPanelIndexIndexGetMarker(getActivity(), this);
            th.start();
            dialog.show();

            panelIndexIndexGetMarkerSuccessHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    JSONObject result = th.getResult();
                    try {
                        JSONArray array = (JSONArray) result.get("data");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String wei = jsonObject.getString("wei");
                            String jing = jsonObject.getString("jing");
                            String flag = jsonObject.getString("flag");
                            if (wei.equals("") || jing.equals("")) {

                            } else {
                                double Dwei = Double.parseDouble(wei);
                                double Djing = Double.parseDouble(jing);
                                LatLng marker = new LatLng(Dwei, Djing);
                                MarkerOptions markerOptions = null;
                                if (flag.equals("1")) {
                                    markerOptions = new MarkerOptions().icon(bitmapDescriptor_1).anchor(0.5f, 0.5f).position(marker).draggable(true);
                                } else if (flag.equals("0")) {
                                    markerOptions = new MarkerOptions().icon(bitmapDescriptor_2).anchor(0.5f, 0.5f).position(marker).draggable(true);
                                }else{
                                    Toast.makeText(getActivity(),flag.toString(),Toast.LENGTH_LONG).show();
                                }
                                markerOptions.title(jsonObject.getString("id"));
                                markerOptionsList.add(markerOptions);
                                markerList.add(jsonObject);
                                old_clustersMarker.add(aMap.addMarker(markerOptions));
//									aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                                dialog.dismiss();

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            panelIndexIndexGetMarkerErrorHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    super.handleMessage(msg);
                    String message = msg.getData().getString("message");
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            };
        } else {
            Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
        }

    }

    private void addMarkersToMap(int k) {
        for (Marker marker : old_clustersMarker) {
            marker.remove();
        }
        for (int i = 0; i < markerOptionsList.size(); i++) {
            MarkerOptions markerOptions = markerOptionsList.get(i);
            LatLng start = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
            LatLng end = new LatLng(markerOptions.getPosition().latitude, markerOptions.getPosition().longitude);
            float distance = AMapUtils.calculateLineDistance(start, end);
            distance = distance / 1000;
            BigDecimal b = new BigDecimal(distance);
            double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (f1 >= k) {
            } else {
                // 重新添加 marker
                old_clustersMarker.add(aMap.addMarker(markerOptions));
            }
        }
    }

    //	坐标聚合相关
    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
//		resetMarks();
    }
//
//	/**
//	 * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
//	 */
//	private void resetMarks() {
//		// 开始刷新
//		Projection projection = aMap.getProjection();
//		Point p = null;
//		markerOptionsListInView.clear();
//		// 获取在当前视野内的marker;提高效率
//		for (MarkerOptions mp : markerOptionsList) {
//			p = projection.toScreenLocation(mp.getPosition());
//			if (p.x < 0 || p.y < 0 || p.x > getResources().getDisplayMetrics().widthPixels
//					|| p.y > getResources().getDisplayMetrics().heightPixels) {
//				// 不添加到计算的列表中
//			} else {
//				// 在当前可观区域内
//				markerOptionsListInView.add(mp);
//			}
//		}
//		// 自定义的聚合类MyMarkerCluster
//		ArrayList<MyGaodeImageView> clustersMarker = new ArrayList<MyGaodeImageView>();
//		for (MarkerOptions mp : markerOptionsListInView) {
//			if (clustersMarker.size() == 0) {
//				// 添加一个新的自定义marker
//				clustersMarker.add(new MyGaodeImageView(getActivity(), mp, projection, 80, mp.getTitle()));// 80=相距多少才聚合
//			} else {
//				boolean isIn = false;
//				for (MyGaodeImageView cluster : clustersMarker) {
//					// 判断当前的marker是否在前面marker的聚合范围内 并且每个marker只会聚合一次。
//					if (cluster.getBounds().contains(mp.getPosition())) {
//						cluster.addMarker(mp);
//						isIn = true;
//						break;
//					}
//				}
//				// 如果没在任何范围内，自己单独形成一个自定义marker。在和后面的marker进行比较
//				if (!isIn) {
//					clustersMarker.add(new MyGaodeImageView(getActivity(), mp, projection, 80, mp.getTitle()));// 80=相距多少才聚合
//				}
//			}
//		}
//		// 设置聚合点的位置和icon
//		for (MyGaodeImageView mmc : clustersMarker) {
//			mmc.setpositionAndIcon();
//		}
//
//		for(Marker marker : old_clustersMarker){
//			marker.remove();
//			// aMap.clear();
//		}
//		// 重新添加 marker
//		for (MyGaodeImageView cluster : clustersMarker) {
//			old_clustersMarker.add(aMap.addMarker(cluster.getOptions()));
//		}
//	}

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        this.amapLocation = amapLocation;
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                is_local = true;
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mlocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            // 设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        View infoContent = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        return infoContent;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
        return infoWindow;
    }

    @Override
    public void onClick(View arg0) {

    }

    @Override
    public void onMapLoaded() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDrag(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragEnd(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
    }

    @Override
    /**
     * 对marker标注点点击响应事件
     */

    public boolean onMarkerClick(Marker marker) {
        closeSelectLayout(0);

        for (int i = 0; i < markerList.size(); i++) {
            String id = null;
            try {
                id = markerList.get(i).getString("id");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (id.equals(marker.getTitle())) {
                try {
                    PubFunction.local = new double[]{amapLocation.getLatitude(), amapLocation.getLongitude()};
                    PubFunction.marker = new double[]{markerList.get(i).getDouble("wei"), markerList.get(i).getDouble("jing")};

                    if (PubFunction.isNetworkAvailable(getActivity())) {
                        JSONObject jsonObject = markerList.get(i);
                        String flag = jsonObject.getString("flag");
                        if (flag.equals("1")) {
                            PanelIndexIndexBottomView panelIndexIndexBottomView = new PanelIndexIndexBottomView(getActivity(), id, this, turnToLogin);
                            panelIndexIndexBottomView.showView();
                        }else{
                            Toast.makeText(getActivity(),"该电柜已停止营业！",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                break;
            }
        }

        return false;
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
        // TODO Auto-generated method stub
        closeSelectLayout(0);

    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
        closeSelectLayout(0);
    }

    @Override
    public void onMapLongClick(LatLng arg0) {
        // TODO Auto-generated method stub
        closeSelectLayout(0);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {

                if (PanelServiceLease.panelServiceLeaseHandler != null) {
                    String addressName = result.getRegeocodeAddress().getFormatAddress() + "附近";
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("address", addressName);
                    message.setData(bundle);
                    PanelServiceLease.panelServiceLeaseHandler.sendMessage(message);
                }
                if (PanelServiceHelp.panelServiceHelpHandler != null) {
                    String addressName = result.getRegeocodeAddress().getFormatAddress() + "附近";
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("address", addressName);
                    message.setData(bundle);
                    PanelServiceHelp.panelServiceHelpHandler.sendMessage(message);
                }
            } else {
            }
        }
    }

    private void show_no_local() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog mAlertDialog = builder.create();
        View view = inflater.inflate(R.layout.alertdialog, null);
        TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
        titleSmall.setText("");
        titleSmall.setVisibility(View.GONE);
        ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
        divid.setVisibility(View.GONE);

        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
        title.setText("未能获取到您的定位信息！");

        TextView title1 = (TextView) view.findViewById(R.id.alertdialogContent);
        title1.setText("    请确定您是否已经打开定位。或者您是否已经允许本程序获得定位权限，并且请您保持您手机的信号通畅！");

        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
        success.setText("确定");
        success.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                mAlertDialog.dismiss();
            }
        });
        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
        error.setText("取消");
        error.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
    }
}

class HttpPanelIndexIndexBooking extends Thread {

    private Activity activity;
    private String id, battery_catid;
    private Fragment fragment;
    private String cabinetid;

    public HttpPanelIndexIndexBooking(Activity activity, String id, String cabinetid, String battery_catid,
                                      Fragment fragment) {
        this.activity = activity;
        this.id = id;
        this.battery_catid = battery_catid;
        this.fragment = fragment;
        this.cabinetid = cabinetid;
    }

    public void run() {
        super.run();
        SharedPreferences Preferences;
        Preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/home/booking";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("id", id));
        list.add(new BasicNameValuePair("battery_catid", battery_catid));
        list.add(new BasicNameValuePair("cabinet_number", cabinetid));
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
                String code = jsonObject.getString("code");
                String messageStr = jsonObject.getString("message");

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                message.setData(bundle);

                if (fragment.getView() != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            PanelIndexIndexBottomView.turnToLogin.sendMessage(message);
                        } else {
                            PanelIndexIndexBottomView.PanelIndexIndexBottomViewBookingErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        PanelIndexIndexBottomView.PanelIndexIndexBottomViewBookingSuccessHandler.sendMessage(message);
                    } else {
                        PanelIndexIndexBottomView.PanelIndexIndexBottomViewUnknownHandler.sendMessage(new Message());
                    }
                }
            }
        } catch (Exception e) {
            if (fragment.getView() != null) {
                PanelIndexIndexBottomView.PanelIndexIndexBottomViewUnknownHandler.sendMessage(new Message());
            }
        }
    }
}

class HttpPanelIndexIndexGetMarker extends Thread {

    private Activity activity;
    private JSONObject jsonObject;
    private Fragment fragment;

    public HttpPanelIndexIndexGetMarker(Activity activity, Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
    }

    public void run() {
        super.run();
        SharedPreferences Preferences;
        Preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/home/lists_cabinet";
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
                String code = jsonObject.getString("code");
                String messageStr = jsonObject.getString("message");
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                message.setData(bundle);

                if (fragment.getView() != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            PanelIndexIndex.turnToLogin.sendMessage(new Message());
                        } else {
                            PanelIndexIndex.panelIndexIndexGetMarkerErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        this.jsonObject = jsonObject;
                        PanelIndexIndex.panelIndexIndexGetMarkerSuccessHandler.sendMessage(message);
                    } else {
                        PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
                    }
                }
            }
        } catch (Exception e) {
            if (fragment.getView() != null) {
                PanelIndexIndex.panelIndexIndexUnknownHandler.sendMessage(new Message());
            }
        }
    }

    public JSONObject getResult() {
        return jsonObject;
    }
}

class HttpPanelIndexIndexGetMarkerInfo extends Thread {

    private Activity activity;
    private JSONObject jsonObject;
    private String id;
    private Fragment fragment;

    public HttpPanelIndexIndexGetMarkerInfo(Activity activity, String id, Fragment fragment) {
        this.activity = activity;
        this.id = id;
        this.fragment = fragment;
    }

    public void run() {
        super.run();
        SharedPreferences Preferences;
        Preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/home/show_cabinet";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("id", id + ""));
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

                String code = jsonObject.getString("code");
                String messageStr = jsonObject.getString("message");

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                message.setData(bundle);
                if (fragment.getView() != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            PanelIndexIndexBottomView.turnToLogin.sendMessage(new Message());
                        } else {
                            PanelIndexIndexBottomView.PanelIndexIndexBottomViewErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        this.jsonObject = jsonObject.getJSONObject("data");
                        PanelIndexIndexBottomView.PanelIndexIndexBottomViewSuccessHandler.sendMessage(message);
                    } else {
                        PanelIndexIndexBottomView.PanelIndexIndexBottomViewUnknownHandler.sendMessage(new Message());
                    }
                }
            }
        } catch (Exception e) {
            if (fragment.getView() != null) {
                PanelIndexIndexBottomView.PanelIndexIndexBottomViewUnknownHandler.sendMessage(new Message());
            }
        }
    }

    public JSONObject getResult() {
        return jsonObject;
    }
}

class PanelIndexIndexBottomView {

    private Activity activity;
    private BottomView bottomView;
    private View bottomPanelView;
    private TextView panelButtomViewName, panelButtomViewPhone, panelButtomViewAddress, panelButtomViewLong,
            panelBottomViewSurplus,panelButtomViewCode;
    private TextView panelBottomViewGoodAText, panelBottomViewGoodBText, panelBottomViewGoodAButton,
            panelBottomViewGoodBButton;
    public static Handler PanelIndexIndexBottomViewSuccessHandler, PanelIndexIndexBottomViewErrorHandler,
            PanelIndexIndexBottomViewUnknownHandler, PanelIndexIndexBottomViewBookingSuccessHandler,
            PanelIndexIndexBottomViewBookingErrorHandler, turnToLogin;
    private String id;
    private HttpPanelIndexIndexGetMarkerInfo th;
    private JSONObject jsonObject;
    private ProgressDialog dialog;
    private Fragment fragment;
    private String bussinessid = null;
    private String cabinetid = null;


    public PanelIndexIndexBottomView(Activity activity, String id, Fragment fragment, Handler turnToLogin) {

        handler();

        this.activity = activity;
        this.id = id;
        this.fragment = fragment;
        this.turnToLogin = turnToLogin;

    }

    public void showView() {
        th = new HttpPanelIndexIndexGetMarkerInfo(activity, id, fragment);
        th.start();
        dialog = new ProgressDialog(activity);
        dialog.show();
    }


    private void handler() {
        PanelIndexIndexBottomViewSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                jsonObject = th.getResult();
                init();
                dialog.dismiss();
            }
        };
        PanelIndexIndexBottomViewErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(activity, msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        };
        PanelIndexIndexBottomViewUnknownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(activity, "发生未知错误！", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        };
        PanelIndexIndexBottomViewBookingSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(activity, msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
            }
        };
        PanelIndexIndexBottomViewBookingErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(activity, msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void init() {

        try {
            bussinessid = jsonObject.getString("businessid");
            cabinetid = jsonObject.getString("cabinet_number");
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        bottomView = new BottomView(activity, R.style.BottomViewTheme_Defalut, R.layout.panel_bottom_view);
        bottomView.setAnimation(R.style.popwin_anim_style);
        bottomPanelView = bottomView.getView();
        panelBottomViewGoodAButton = (TextView) bottomPanelView.findViewById(R.id.panelBottomViewGoodAButton);
        panelBottomViewGoodAButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (PubFunction.isNetworkAvailable(activity)) {
                    HttpPanelIndexIndexBooking th = new HttpPanelIndexIndexBooking(activity, bussinessid, cabinetid,
                            "1", fragment);
                    th.start();
                } else {
                    Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }
                panelBottomViewGoodAButton.setClickable(false);
                panelBottomViewGoodAButton.setBackgroundResource(R.drawable.bottom_view_buttom_off);
                panelBottomViewGoodBButton.setClickable(false);
                panelBottomViewGoodBButton.setBackgroundResource(R.drawable.bottom_view_buttom_off);
            }
        });
        panelBottomViewGoodAText = (TextView) bottomPanelView.findViewById(R.id.panelBottomViewGoodAText);
        panelBottomViewGoodBText = (TextView) bottomPanelView.findViewById(R.id.panelBottomViewGoodBText);
        panelBottomViewGoodBButton = (TextView) bottomPanelView.findViewById(R.id.panelBottomViewGoodBButton);
        panelBottomViewGoodBButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (PubFunction.isNetworkAvailable(activity)) {
                    HttpPanelIndexIndexBooking th = new HttpPanelIndexIndexBooking(activity, bussinessid, cabinetid,
                            "2", fragment);
                    th.start();
                } else {
                    Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }

                panelBottomViewGoodAButton.setClickable(false);
                panelBottomViewGoodAButton.setBackgroundResource(R.drawable.bottom_view_buttom_off);
                panelBottomViewGoodBButton.setClickable(false);
                panelBottomViewGoodBButton.setBackgroundResource(R.drawable.bottom_view_buttom_off);
            }
        });
        panelButtomViewName = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewName);
        panelButtomViewCode = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewCode);
        panelButtomViewPhone = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewPhone);
        panelButtomViewAddress = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewAddress);
        panelBottomViewSurplus = (TextView) bottomPanelView.findViewById(R.id.panelBottomViewSurplus);
        try {
            panelButtomViewAddress.setText(jsonObject.getString("address").toString().trim());
            panelButtomViewName.setText(jsonObject.getString("companyname").toString().trim());
            panelButtomViewCode.setText(jsonObject.getString("cabinet_number").toString().trim());
            panelButtomViewPhone.setText(jsonObject.getString("mobile").toString().trim());
            panelBottomViewGoodAText.setText(jsonObject.getString("A_surplus").toString().trim());
            panelBottomViewGoodBText.setText(jsonObject.getString("B_surplus").toString().trim());
            panelBottomViewSurplus.setText("剩余充电" + jsonObject.getString("buy_member").toString().trim() + "次");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        panelButtomViewLong = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewLong);
        LatLng start = new LatLng(PubFunction.local[0], PubFunction.local[1]);
        LatLng end = new LatLng(PubFunction.marker[0], PubFunction.marker[1]);
        float distance = AMapUtils.calculateLineDistance(start, end);
        distance = distance / 1000;
        BigDecimal b = new BigDecimal(distance);
        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        panelButtomViewLong.setText(f1 + "");

        LinearLayout panelBottomViewReturn = (LinearLayout) bottomPanelView.findViewById(R.id.panelBottomViewReturn);
        panelBottomViewReturn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                bottomViewDis();
            }
        });
        LinearLayout panelButtomViewNavigation = (LinearLayout) bottomPanelView.findViewById(R.id.panelButtomViewNavigation);
        panelButtomViewNavigation.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(activity, PanelIndexNavigation.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        });
        TextView panelButtomViewInfo = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewInfo);
        panelButtomViewInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String id = null;
                try {
                    id = jsonObject.getString("id").toString().trim();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Intent intent = new Intent(activity, PanelIndexInfo.class);
                intent.putExtra("id", id + "");
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        });
        TextView panelButtomViewComp = (TextView) bottomPanelView.findViewById(R.id.panelButtomViewComp);
        panelButtomViewComp.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(activity, PanelIndexComplaint.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        });
        TextView panel_buttom_view_pay = (TextView) bottomPanelView.findViewById(R.id.panel_buttom_view_pay);
        panel_buttom_view_pay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Panel.mine2ShopHandler.sendMessage(new Message());
                bottomViewDis();
            }
        });
        bottomView.showBottomView(true);
    }

    public void bottomViewDis() {
        bottomView.dismissBottomView();
    }

}
