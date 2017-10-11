package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by apple on 2017/8/21.
 */
@EFragment(R.layout.green_shop_item)
public class GreenShopItemFragment extends Fragment implements AbsListView.OnScrollListener {

    @ViewById
    ListView listview;

    private View view;
    private PanelShopIndexItemAdaptr simpleAdapter;
    private List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
    private String catID = "";
    private SharedPreferences preferences;

    private int count_anli_page = 2;
    private int current_page = 1;

    private int cat_type = 0;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.green_shop_item, container, false);
        return view;
    }

    @AfterViews
    void afterView() {
        init();
        main();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataList.clear();
    }

    private void main() {
        httpPanelShopIndexItem(1);
    }


    private void init() {
        progressDialog = new ProgressDialog(getActivity());
        preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        catID = getArguments().getString("catID");
        if (catID.equals("9")) {//9是换电卡
            cat_type = R.layout.green_shop_item_fragment_item;
        } else {
            cat_type = R.layout.green_shop_item_fragment_item_1;
        }


        listview.setDividerHeight(0);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View footView = inflater.inflate(R.layout.green_shop_footview, null);
        listview.addFooterView(footView);
        listview.setOnScrollListener(this);
    }

    private List<Map<String, String>> getdata(JSONArray jsonArray) throws JSONException {

        System.out.println(jsonArray);
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, String> map = new HashMap<String, String>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                map.put("id", id);
                String goodsname = jsonObject.getString("goodsname");
                map.put("text1", goodsname);
                map.put("text2", jsonObject.getString("intro"));
                String price = jsonObject.getString("price");
                map.put("text3", "¥ " + price);
                map.put("text4", "无限期");
                map.put("text5", "供换电柜电池更换使用");
                String thumb = jsonObject.getString("thumb");
                map.put("image",thumb);
                dataList.add(map);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return dataList;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
                if (PubFunction.isNetworkAvailable(getActivity())) {
                    httpPanelShopIndexItem(count_anli_page);
                    current_page = count_anli_page;
                } else {
                    Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    @UiThread
    void renturnError(String str) {
        MyToast.showTheToast(getActivity(), str);
    }

    @UiThread
    void returnSuccess(String str) {
        MyToast.showTheToast(getActivity(), str);
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
    void returnHttpPanelShopIndexItem(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            if (!jsonTokener.toString().equals("[]")) {
                JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                simpleAdapter = new PanelShopIndexItemAdaptr(getActivity(), getdata(jsonArray), cat_type, new String[]{"text1", "text2", "text3", "text4", "text5"}, new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5},getActivity());
                listview.setAdapter(simpleAdapter);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        progressDialog.dismiss();
    }

    @UiThread
    void returnHttpPanelShopIndexItemRE(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            if (jsonTokener.toString().length() >=4) {
                JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                getdata(jsonArray);
                simpleAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        count_anli_page = count_anli_page + 1;
        progressDialog.dismiss();
    }


    @Background
    void httpPanelShopIndexItem(int page) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/shop/lists_goods";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("catid", catID + ""));
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

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        if (page == 1) {
                            returnHttpPanelShopIndexItem(messageStr, data);
                        } else {
                            returnHttpPanelShopIndexItemRE(messageStr, data);
                        }
                    } else {
                        returnSuccess(messageStr);
                    }
                } else if (code.equals("200")) {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageStr);
                    }
                } else {
                    renturnError(messageStr);
                }
            } else {
                renturnError("服务器错误：HttpPanelShopIndex");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpPanelShopIndex");
        }
    }

    class PanelShopIndexItemAdaptr extends SimpleAdapter {

        private int[] mTo;
        private String[] mFrom;
        private ViewHolder mHolder;
        private int panel;
        protected List<? extends Map<String, ?>> mData;
        private LayoutInflater mInflater;
        private Context context;
        private Activity activity;

        public PanelShopIndexItemAdaptr(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, Activity activity) {
            super(context, data, resource, from, to);
            // TODO Auto-generated constructor stub
            mData = data;
            panel = resource;
            mFrom = from;
            mTo = to;
            if (context != null) {
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                this.context = context;
            }
            this.activity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(panel, null);
                mHolder = new ViewHolder();
                mHolder.mImageView = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(mHolder);

                String thumb = mData.get(position).get("image").toString();
                Picasso.with(context).load(PubFunction.www + thumb).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).resize(400, 400).centerCrop().into(mHolder.mImageView);
                final int tempPosition = position;
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PanelShopGoodInfo.class);
                        intent.putExtra("id", mData.get(tempPosition).get("id").toString());
                        context.startActivity(intent);
                    }
                });

            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            return super.getView(position, convertView, parent);
        }

        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        private class ViewHolder {
            ImageView mImageView;
            TextView mTextView;
        }
    }
}
