package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

import org.androidannotations.annotations.AfterViews;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_province)
public class RentCarProvince extends Fragment {

    @ViewById
    LinearLayout page_return;
    @ViewById
    ListView rentcar_local_right_list;

    private SimpleAdapter RightSimpleAdapter;
    private List<Map<String, Object>> rightList = new ArrayList<Map<String, Object>>();
    private Fragment fragment;
    private ProgressDialog progressDialog;

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    void init(){
        this.fragment = this;
        progressDialog = new ProgressDialog(getActivity());
        getCarProvince();
        progressDialog.show();
    }


    private List<Map<String, Object>> getData(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("title", jsonObject.getString("name"));
                map.put("code", jsonObject.getString("code"));
                rightList.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return rightList;
    }

    @Click
    void page_return() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(R.id.rentcar_panel, new RentCarIndex_());
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
                    fragmentTransaction.replace(R.id.rentcar_panel, new RentCarIndex_());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
        rightList.clear();
    }

    @Background
    void getCarProvince(){
        String path = PubFunction.www + "api.php/City/province";
        SharedPreferences Preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
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
                JSONTokener jsonToken = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonToken.nextValue();
                String code = jsonObject.getString("code");
                String messageString = jsonObject.getString("message");
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                if (fragment != null) {
                    if (code.equals("0")) {
                        getTypSuccess(jsonArray);
                    } else if (code.equals("200")) {
                        if (messageString.equals("秘钥不正确,请重新登录")) {
                            turnaToLogin();
                        }else{
                            getTypError(messageString);
                        }
                    } else{
                        getTypUnknown();
                    }
                }
            }else{
                getTypError(Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            if (fragment != null) {
                getTypUnknown();
            }
        }
    }

    @UiThread
    void getTypSuccess(final JSONArray jsonArray){

        RightSimpleAdapter = new SimpleAdapter(getActivity(), getData(jsonArray), R.layout.rentcar_province_item, new String[]{"title"}, new int[]{R.id.title});
        rentcar_local_right_list.setAdapter(RightSimpleAdapter);
        rentcar_local_right_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                RentCarArea rentCarArea = new RentCarArea_();

                Bundle bundle = new Bundle();
                bundle.putString("code", rightList.get(position).get("code").toString());
                rentCarArea.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left);
                fragmentTransaction.replace(R.id.rentcar_panel, rentCarArea);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        progressDialog.dismiss();
    }

    @UiThread
    void getTypError(String str){
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void getTypUnknown(){
        Toast.makeText(getActivity(), "发生未知错误!", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void turnaToLogin(){
        SharedPreferences preferences = getActivity().getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
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

    @Override
    public void onPause() {
        super.onPause();
        rightList.clear();
    }
}
