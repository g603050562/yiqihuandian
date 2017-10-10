package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.service.NotificationService;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
@EFragment(R.layout.rentcar_type)
public class RentCarType extends Fragment {

    @ViewById
    LinearLayout page_return;
    @ViewById
    LinearLayout turn_to_index;
    @ViewById
    ListView rentcar_car_type_list;


    private String code = "";
    private Fragment fragment;
    private RentCarTypeAdapt simpleAdapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        fragment = this;
        progressDialog = new ProgressDialog(getActivity());
        code = getArguments().getString("address_id");
        if(!code.equals("")){
            getCarType();
            progressDialog.show();
        }else{
            Toast.makeText(getActivity(),"传参出错！！",Toast.LENGTH_LONG).show();
        }
    }

    private List<Map<String, Object>> getData(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length() ; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("title",jsonObject.getString("name"));
                map.put("content",jsonObject.getString("describe"));
                map.put("thumb",jsonObject.getString("logo"));
                map.put("id",jsonObject.getString("id"));
                list.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return list;
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
        list.clear();
    }

    @Background
    void getCarType(){
        String path = PubFunction.www + "api.php/Cab/business_cab/"+code;
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
                    }else if (code.equals("1005")) {
                        getTypLoading(messageString);
                    }else{
                        getTypUnknown();
                    }
                }
            }
        } catch (Exception e) {
            if (fragment != null) {
                getTypUnknown();
            }
        }
    }

    @UiThread
    void getTypSuccess(JSONArray jsonArray){

        simpleAdapter = new RentCarTypeAdapt(getActivity(), getData(jsonArray), R.layout.rentcar_type_item, new String[]{"title","content"}, new int[]{R.id.title,R.id.content},R.id.img);
        rentcar_car_type_list.setAdapter(simpleAdapter);
        rentcar_car_type_list.setDividerHeight(0);
        rentcar_car_type_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                sharedPreferences = getActivity().getSharedPreferences("rentCarOrder", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("car_id", list.get(position).get("id").toString());
                editor.putString("car_name", list.get(position).get("title").toString());
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
        progressDialog.dismiss();
    }

    @UiThread
    void getTypLoading(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
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
        list.clear();
    }
}

class RentCarTypeAdapt extends BaseAdapter{

    private int[] mTo;
    private String[] mFrom;
    protected List<? extends Map<String, ?>> mData;
    private int mResources;
    private LayoutInflater mInflater;
    private int img;
    private Context context;

    public RentCarTypeAdapt(Context context, List<? extends Map<String, ?>> data, int resources,String[] from,int[] to,int img) {
        mData = data;
        mResources = resources;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.img = img;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mResources, null);
            ImageView imageView = (ImageView) convertView.findViewById(img);
            Picasso.with(context).load(PubFunction.www + mData.get(position).get("thumb")).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).resize(200, 200).centerCrop().into(imageView);
            TextView titleText = (TextView) convertView.findViewById(R.id.title);
            titleText.setText(mData.get(position).get("title").toString());
            TextView contentText = (TextView) convertView.findViewById(R.id.content);
            contentText.setText(mData.get(position).get("content").toString());
        }else{

        }
        return convertView;
    }

}
