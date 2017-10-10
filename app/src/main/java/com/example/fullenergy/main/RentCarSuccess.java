package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.scanCode.CaptureActivity;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
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
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_success)
public class RentCarSuccess extends Fragment {

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView rentcar_success_button;
    @ViewById
    LinearLayout turn_to_index;
    @ViewById
    TextView carType;
    @ViewById
    TextView carLocal;
    @ViewById
    TextView carDate;

    public static Handler getScanCodeStringHandler;
    private SharedPreferences sharedPreferences;
    private Fragment fragment;
    private ProgressDialog progressDialog;

    private String code = "";


    @Override
    public void onStart() {
        super.onStart();
        init();
        handler();
    }

    private void init() {
        fragment = this;
        progressDialog = new ProgressDialog(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("rentCarOrder", Activity.MODE_PRIVATE);
        carType.setText(sharedPreferences.getString("car_name",null).toString());
        carLocal.setText(sharedPreferences.getString("address_name",null).toString());
        carDate.setText(sharedPreferences.getString("rent_time_name",null).toString());
    }

    @Click
    void rentcar_success_button(){
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        getActivity().startActivityForResult(intent, 0x0000);
    }

    private void handler() {
        getScanCodeStringHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                code =  msg.getData().getString("code");
                if(!code.equals("")){
                    progressDialog.show();
                    scanCodeSubmit(code);
                }else{
                    Toast.makeText(getActivity(),"扫描二维码返回参数错误！",Toast.LENGTH_LONG).show();
                }
            }
        };
    }


    @Click
    void turn_to_index(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarIndex_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Click
    void page_return() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarCost_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
                    fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
                    fragmentTransaction.replace(R.id.rentcar_panel, new RentCarCost_());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
    }

    @Background
    void scanCodeSubmit(String carID) {
        String path = PubFunction.www + "api.php/cab/car/" + carID;

        SharedPreferences Preferences;
        Preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);

        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                System.out.println(result.toString());

                JSONTokener jsonToken = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonToken.nextValue();
                String code = jsonObject.getString("code");
                String messageString = jsonObject.getString("message");

                if (fragment != null) {
                    if (code.equals("0")) {
                        getSuccess();
                    } else if (code.equals("200")) {
                        if (messageString.equals("秘钥不正确,请重新登录")) {
                            turnaToLogin();
                        } else {
                            getTypError(messageString);
                        }
                    }else {
                        getTypUnknown(messageString);
                    }
                }
            }
        } catch (Exception e) {
            if (fragment != null) {
                getTypUnknown("发生未知错误！");
            }
        }
    }

    @UiThread
    void getSuccess() {
        progressDialog.dismiss();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarSuccess_2_());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @UiThread
    void getTypError(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void getTypUnknown(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void getTypUnknown() {
        Toast.makeText(getActivity(), "发生未知错误!", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void turnaToLogin() {
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

}
