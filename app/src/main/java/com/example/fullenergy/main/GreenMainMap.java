package com.example.fullenergy.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.example.fullenergy.R;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.gaode.My3dMapView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap;

/**
 * ／
 * Created by hasee on 2017/6/9.
 */
@EFragment(R.layout.green_main_map)
public class GreenMainMap extends Fragment implements LocationSource, AMapLocationListener, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerDragListener, AMap.OnMapLoadedListener, View.OnClickListener, AMap.InfoWindowAdapter,
        AMap.OnCameraChangeListener, AMap.OnMapClickListener, AMap.OnMapLongClickListener {

    @ViewById
    MapView map;

    private View view;
    private AMap aMap;
    private Bundle savedInstanceState;
    private ProgressDialog progressDialog;

    private BitmapDescriptor bitmapDescriptor_1;
    private BitmapDescriptor bitmapDescriptor_2;

    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;
    private MyLocationStyle myLocationStyle;
    private OnLocationChangedListener mListener;
    private AMapLocation amapLocation;

    public static Handler setLocalHandler;
    private SharedPreferences preferences;

    private boolean is_local = false;
    private boolean has_local = false;

    private List<JSONObject> markerList = new ArrayList<JSONObject>();

    ArrayList<Marker> old_clustersMarker = new ArrayList<Marker>();
    /**
     * 所有的marker
     */
    private List<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
    /**
     * 视野内的marker
     */
    private ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();

    private int thread_1_count = 0;


    private Thread thread_1  = new Thread(){
        @Override
        public void run() {
            super.run();

            while (thread_1_count == 0){
                try {
                    sleep(1000);
                    if(is_local == true && has_local == false){
                        setLocalHandler.sendMessage(new Message());
                        has_local = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.green_main_map, container, false);
        this.savedInstanceState = savedInstanceState;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @AfterViews
    void afterViews() {
        init(savedInstanceState);
        handler();

    }


    private void handler() {
        setLocalHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!is_local) {
                    show_no_local();
                } else {
                    if(aMap.getMyLocation() != null){
                        LatLng location = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(location, 15, 0, 0)), 1000, null);
                    }
                }
            }
        };
    }

    /**
     * 初始化AMap对象
     */
    private void init(Bundle savedInstanceState) {

        preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        progressDialog = new ProgressDialog(getActivity());

        Resources res_1 = getResources();
        Bitmap bmp_1 = BitmapFactory.decodeResource(res_1, R.drawable.d1);

        Resources res_2 = getResources();
        Bitmap bmp_2 = BitmapFactory.decodeResource(res_2, R.drawable.d2);

        int width = bmp_1.getWidth();
        int height = bmp_1.getHeight();
        // 设置想要的大小
        int newWidth = PubFunction.dip2px(getActivity(), 21);
        int newHeight = PubFunction.dip2px(getActivity(), 29);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm_1 = Bitmap.createBitmap(bmp_1, 0, 0, width, height, matrix, true);
        Bitmap newbm_2 = Bitmap.createBitmap(bmp_2, 0, 0, width, height, matrix, true);

        BitmapDescriptor var1 = fromBitmap(newbm_1);
        bitmapDescriptor_1 = var1;

        BitmapDescriptor var2 = fromBitmap(newbm_2);
        bitmapDescriptor_2 = var2;

        map.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = map.getMap();
            setUpMap();
        }


    }

    private void setUpMap() {
        // 自定义系统定位小蓝点
        setLocation();
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.getUiSettings().setTiltGesturesEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
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
        // 往地图上添加marker
        addMarkersToMap();
    }

    private void addMarkersToMap() {

        if (PubFunction.isNetworkAvailable(getActivity())) {
            HttpGetMarker();
            progressDialog.show();
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

    private void setLocation() {

        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.location_marker);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int newWidth = PubFunction.dip2px(getActivity(), 29);
        int newHeight = PubFunction.dip2px(getActivity(), 29);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        BitmapDescriptor var = fromBitmap(newbm);

        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(var);
        myLocationStyle.strokeColor(0x00000000);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(0.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        // aMap.setMyLocationType();
    }

    private void show_no_local() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog mAlertDialog = builder.create();
        View view = inflater.inflate(R.layout.green_alertdialog, null);
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
        success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                mAlertDialog.dismiss();
            }
        });
        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
        error.setText("取消");
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        if(!thread_1.isAlive()){
            thread_1.start();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        if (markerList != null) {
            markerList.clear();
        }
        if (markerOptionsList != null) {
            markerOptionsList.clear();
        }
        if (markerOptionsListInView != null) {
            markerOptionsListInView.clear();
        }

        has_local = false;
        thread_1_count = 1;
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
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
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
    /**
     * 对marker标注点点击响应事件
     */

    public boolean onMarkerClick(Marker marker) {

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
                            HttpGetMarkerInfo(id);
                            progressDialog.show();
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
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        GreenMain_.dismissHandler.sendMessage(new Message());
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        GreenMain_.dismissHandler.sendMessage(new Message());
    }

    @UiThread
    void renturnError(String str) {
        MyToast.showTheToast(getActivity(), str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnSuccess(String str) {
        MyToast.showTheToast(getActivity(), str);
        progressDialog.dismiss();
    }

    @UiThread
    void turnToLogin() {
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
        progressDialog.dismiss();
    }

    @UiThread
    void renturnHttpGetMarker(String str , String data){
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
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
                    } else {
                        Toast.makeText(getActivity(), flag.toString(), Toast.LENGTH_LONG).show();
                    }
                    markerOptions.title(jsonObject.getString("id"));
                    markerOptionsList.add(markerOptions);
                    markerList.add(jsonObject);
                    old_clustersMarker.add(aMap.addMarker(markerOptions));
//					aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    progressDialog.dismiss();

                }
            }

        }catch (Exception e){
            System.out.println(e.toString());
        }
    }




    @Background
    void HttpGetMarker() {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/home/lists_cabinet";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        renturnHttpGetMarker(messageStr, data);
                    } else {
                        returnSuccess(messageStr);
                    }
                } else if (code.equals("200")) {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageStr);
                    }
                }else {
                    renturnError(messageStr);
                }
            } else {
                renturnError("服务器错误：HttpGetMarker");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpGetMarker");
        }

    }

    @UiThread
    void renturnHttpGetMarkerInfo(String str , String data){

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data",data);
        message.setData(bundle);
        GreenMain_.showHandler.sendMessage(message);

        progressDialog.dismiss();
    }

    @Background
    void HttpGetMarkerInfo(String id) {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/home/show_cabinet";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("id", id + ""));
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        renturnHttpGetMarkerInfo(messageStr, data);
                    } else {
                        returnSuccess(messageStr);
                    }
                } else if (code.equals("200")) {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageStr);
                    }
                }else {
                    renturnError(messageStr);
                }
            } else {
                renturnError("服务器错误：HttpGetMarkerInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpGetMarkerInfo");
        }

    }

}

