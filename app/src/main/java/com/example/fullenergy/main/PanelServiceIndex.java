package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.fullenergy.pub.MyToast;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;


@EFragment(R.layout.panel_service_index)
public class PanelServiceIndex extends Fragment {

    @ViewById
    ViewFlipper panel_service_index_title;
    @ViewById
    LinearLayout panel_service_index_rent_car;

    private SharedPreferences preferences;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init();
        main();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void init() {
        dialog = Panel.progressDialog;
        preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
    }

    private void main() {
        if (PubFunction.isNetworkAvailable(getActivity())) {
            dialog.show();
            HttpPanelServiceIndex(1);
        } else {
            Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
        }
    }

    @Click
    void panel_service_index_rent_car(){

        new MyToast().showTheToast(getActivity(),"此功能暂未开放，请耐心等待！");
//        Intent intent = new Intent(getActivity(),RentCar_.class);
//        startActivity(intent);
//        getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @UiThread
    void panelServiceIndexSuccess(JSONObject jsonObject) {
        try {
            dialog.dismiss();
            JSONArray top = jsonObject.getJSONArray("tuijian");

            JSONObject jsonObject_1 = top.getJSONObject(0);
            View view_flipper_1 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.index_recommend_flipper, null);
            ImageView imageView_1 = (ImageView) view_flipper_1.findViewById(R.id.index_recommend_flipper_img);
            Picasso.with(getActivity()).load(jsonObject_1.getString("data")).into(imageView_1);
            TextView textView_1 = (TextView) view_flipper_1.findViewById(R.id.index_recommend_flipper_text);
            textView_1.setText(jsonObject_1.getString("name"));


            JSONObject jsonObject_2 = top.getJSONObject(1);
            View view_flipper_2 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.index_recommend_flipper, null);
            ImageView imageView_2 = (ImageView) view_flipper_2.findViewById(R.id.index_recommend_flipper_img);
            Picasso.with(getActivity()).load(jsonObject_2.getString("data")).into(imageView_2);
            TextView textView_2 = (TextView) view_flipper_2.findViewById(R.id.index_recommend_flipper_text);
            textView_2.setText(jsonObject_2.getString("name"));
            final String top_2_id = jsonObject_2.getString("id");


            panel_service_index_title.addView(view_flipper_1);
            panel_service_index_title.addView(view_flipper_2);
            panel_service_index_title.setInAnimation(getActivity(), R.anim.in_right);
            panel_service_index_title.setOutAnimation(getActivity(), R.anim.out_left);
            panel_service_index_title.setFlipInterval(5000);
            panel_service_index_title.startFlipping();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @UiThread
    void panelServiceIndexError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @UiThread
    void turnToLogin() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usrename", null);
        editor.putString("password", null);
        editor.putString("jy_password", null);
        editor.putString("PHPSESSID", null);
        editor.putString("api_userid", null);
        editor.putString("api_username", null);
        editor.commit();
        Intent intent = new Intent(getActivity(), Login.class);
        intent.putExtra("type", "1");
        getActivity().startActivity(intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Background
    void HttpPanelServiceIndex(int page) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/service/lists_news";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("page", page + ""));
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
                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
                JSONObject returnJSONObject = jsonObject.getJSONObject("data");

                if(code.equals("100")){
                    panelServiceIndexSuccess(returnJSONObject);
                }else{
                    if(messageStr.equals("秘钥不正确,请重新登录")){
                        turnToLogin();
                    }else{
                        panelServiceIndexError(messageStr);
                    }
                }
            }
        } catch (Exception e) {
            if (this.getView() != null) {
                panelServiceIndexError("json解析失败！");
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
        }
    }
}