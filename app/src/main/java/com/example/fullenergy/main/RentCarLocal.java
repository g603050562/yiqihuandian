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
import android.view.ViewGroup;
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
import org.androidannotations.annotations.res.ColorRes;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_local)
public class RentCarLocal extends Fragment {

    @ViewById
    LinearLayout page_return;
    @ViewById
    LinearLayout rentcar_local_left_panel;
    @ViewById
    ListView rentcar_local_right_list;

    private String code = "";

    private List<Map<String, Object>> rightList = new ArrayList<Map<String, Object>>();
    private List<View> leftViewList = new ArrayList<View>();
    private SimpleAdapter RightSimpleAdapter;

    private Fragment fragment;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void init() {
        fragment = this;
        progressDialog = new ProgressDialog(getActivity());
        code = getArguments().getString("code2");
        if (!code.equals("")) {
            getArea();
            progressDialog.show();
        } else {
            Toast.makeText(getActivity(), "传参出错！！", Toast.LENGTH_LONG).show();
        }
    }

    private void rightList(JSONArray jsonArray) {
        RightSimpleAdapter = new SimpleAdapter(getActivity(), getData(jsonArray), R.layout.rentcar_local_right_item, new String[]{"title", "content"}, new int[]{R.id.title, R.id.content});
        rentcar_local_right_list.setAdapter(RightSimpleAdapter);
        rentcar_local_right_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                sharedPreferences = getActivity().getSharedPreferences("rentCarOrder", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("address_id", rightList.get(position).get("code").toString());
                editor.putString("address_name", rightList.get(position).get("title").toString());
                editor.commit();

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
                RentCarIndex rentCarIndex = new RentCarIndex_();
                fragmentTransaction.replace(R.id.rentcar_panel, rentCarIndex);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
    }

    private List<Map<String, Object>> getData(JSONArray jsonArray) {
        if(jsonArray!=null){
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    map.put("title", jsonObject.getString("companyname"));
                    map.put("content", jsonObject.getString("address"));
                    map.put("code", jsonObject.getString("id"));
                    rightList.add(map);
                }
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

        RentCarArea rentCarArea = new RentCarArea_();
        Bundle bundle = new Bundle();
        bundle.putString("code", getArguments().getString("code1"));
        rentCarArea.setArguments(bundle);

        fragmentTransaction.replace(R.id.rentcar_panel, rentCarArea);
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
                    fragmentTransaction.replace(R.id.rentcar_panel, new RentCarProvince_());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
        init();
    }

    @Background
    void getArea() {
        String path = PubFunction.www + "api.php/City/area/" + code;
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
                JSONTokener jsonToken = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonToken.nextValue();
                System.out.println(jsonObject.toString());
                String code = jsonObject.getString("code");
                String messageString = jsonObject.getString("message");

                if (fragment != null) {
                    if (code.equals("0")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        getCitySuccess(jsonArray);
                    } else if (code.equals("200")) {
                        if (messageString.equals("秘钥不正确,请重新登录")) {
                            turnaToLogin();
                        } else {
                            getTypError(messageString);
                        }
                    } else {
                        getTypUnknown();
                    }
                }
            }else{
                getTypError(httpResponse.getStatusLine().getStatusCode()+"");
            }
        } catch (Exception e) {
            if (fragment != null) {
                getTypUnknown();
            }
        }
    }

    @Background
    void getAddress(String addressCode) {
        String path = PubFunction.www + "api.php/City/address/" + addressCode;
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
                        getAddressSuccess(jsonArray);
                    } else if (code.equals("200")) {
                        if (messageString.equals("秘钥不正确,请重新登录")) {
                            turnaToLogin();
                        } else {
                            getTypError(messageString);
                        }
                    } else if (code.equals("1005")) {
                        getTypLoading(messageString);
                    } else {
                        getTypUnknown();
                    }
                }
            }else{
                getTypError(httpResponse.getStatusLine().getStatusCode()+"");
            }
        } catch (Exception e) {
            if (fragment != null) {
                getTypUnknown();
            }
        }
    }

    @UiThread
    void getCitySuccess(JSONArray ja) {

        try {
            JSONArray jsonArray = null;
            if (ja.length() > 0) {
                jsonArray = ja;
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                for (int i = 0; i < jsonArray.length(); i++) {
                    View view = inflater.inflate(R.layout.rentcar_local_left_item, null);
                    final TextView content = (TextView) view.findViewById(R.id.content);
                    final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.is_select);

                    JSONObject jsonObject_item = jsonArray.getJSONObject(i);
                    String item_name = jsonObject_item.getString("name");
                    content.setText(item_name);
                    view.setTag(jsonObject_item.getString("code"));
                    if (i == 0) {
                        content.setBackgroundColor(0x00ff50c26d);
                        linearLayout.setVisibility(View.VISIBLE);
                    } else {
                        content.setBackgroundColor(0x00ff40b25d);
                    }
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (View view_item : leftViewList) {
                                LinearLayout linearLayout = (LinearLayout) view_item.findViewById(R.id.is_select);
                                linearLayout.setVisibility(View.GONE);
                                TextView content = (TextView) view_item.findViewById(R.id.content);
                                    content.setBackgroundColor(0x00ff40b25d);
                            }
                            linearLayout.setVisibility(View.VISIBLE);
                            content.setBackgroundColor(0x00ff50c26d);
                            getAddress(v.getTag().toString());
                            progressDialog.show();
                        }
                    });

                    if (jsonObject_item.has("address_list")) {
                        JSONArray jsonArray_right = jsonObject_item.getJSONArray("address_list");
                        rightList(jsonArray_right);
                    }

                    leftViewList.add(view);
                    rentcar_local_left_panel.addView(view);
                }

            } else {
                Toast.makeText(getActivity(), "此地区正在开发中！敬请期待！", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    @UiThread
    void getAddressSuccess(JSONArray ja) {
        rightList.clear();
        rightList(ja);
        progressDialog.dismiss();
    }

    @UiThread
    void getTypError(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @UiThread
    void getTypLoading(String str) {
        Toast.makeText(getActivity(), "此区域目前还没有商家！", Toast.LENGTH_SHORT).show();
        rightList.clear();
        rightList(null);
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

    @Override
    public void onPause() {
        super.onPause();
        rightList.clear();
        rentcar_local_left_panel.removeAllViews();
        leftViewList.clear();
    }
}

