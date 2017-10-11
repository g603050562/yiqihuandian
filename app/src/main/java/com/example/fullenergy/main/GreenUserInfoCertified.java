package com.example.fullenergy.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2017/8/24.
 */
@EActivity(R.layout.green_userinfo_certified)
public class GreenUserInfoCertified extends FragmentActivity {

    private Activity activity;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    private GreenUserInfoCertifiedAdapter adapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    @ViewById
    LinearLayout page_return;
    @ViewById
    SwipeMenuListView gridView;

    @AfterViews
    void afterView() {
        init();
    }

    private void init() {
        activity = this;
        progressDialog = new ProgressDialog(activity);

        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        if (PubFunction.isNetworkAvailable(activity)) {
            HttpCertified();
            progressDialog.show();
        } else {
            Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
        }

    }

    @Click
    void page_return() {
        this.finish();
    }


    @UiThread
    void returnError(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnSuccess(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void turnToLogin() {
        SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usrename", null);
        editor.putString("password", null);
        editor.putString("jy_password", null);
        editor.putString("PHPSESSID", null);
        editor.putString("api_userid", null);
        editor.putString("api_username", null);
        editor.commit();
        Intent intent = new Intent(activity, Login_.class);
        intent.putExtra("type", "1");
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
        progressDialog.dismiss();
    }

    @UiThread
    void returnHttpCertified(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
            list.clear();
            if (jsonArray.toString().length() > 4) {

                adapter = new GreenUserInfoCertifiedAdapter(activity, getdata(jsonArray),
                        new int[]{R.layout.green_userinfo_certified_item, R.layout.green_userinfo_certified_item_add},
                        new String[]{"number", "date", "battery", "batteryID"},
                        new int[]{R.id.panelMineCertifiedItemNmunber, R.id.panelMineCertifiedItemDate,
                                R.id.panelMineCertifiedItemBattery, R.id.panelMineCertifiedItemBatteryID});
                gridView.setAdapter(adapter);

                SwipeMenuCreator creator = new SwipeMenuCreator() {

                    @Override
                    public void create(SwipeMenu menu) {
                        // create "delete" item
                        SwipeMenuItem deleteItem = new SwipeMenuItem(activity);
                        // set item background
                        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                        // set item width
                        deleteItem.setWidth(PubFunction.dip2px(activity, 90));
                        // set a icon
                        deleteItem.setIcon(R.drawable.ic_delete);
                        // add to menu
                        menu.addMenuItem(deleteItem);
                    }
                };
                // set creator
                gridView.setMenuCreator(creator);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == list.size() - 1) {
                            if (list.size() >= 2) {
                                Toast.makeText(activity, "请先删除已存在的验证（左划删除）！", Toast.LENGTH_LONG).show();
                            } else {
                                activity.startActivity(new Intent(activity, GreenUserInfoCertifiedEdit_.class));
                            }
                        }
                    }
                });

                gridView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                        switch (index) {
                            case 0:
                                // open
                                String id = list.get(position).get("id").toString();
                                HttpDeleteCertified(id);
                                progressDialog.show();
                                break;
                        }
                    }
                });

                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    @UiThread
    void nullCarCertified(){
        progressDialog.dismiss();

        MyToast.showTheToast(activity, "没有认证车辆");
        adapter = new GreenUserInfoCertifiedAdapter(activity, getdata(),
                new int[] { R.layout.panel_mine_certified_item, R.layout.panel_mine_certified_item_add },
                new String[] { "number", "date", "battery", "batteryID" },
                new int[] { R.id.panelMineCertifiedItemNmunber, R.id.panelMineCertifiedItemDate,
                        R.id.panelMineCertifiedItemBattery, R.id.panelMineCertifiedItemBatteryID });
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == list.size() - 1) {
					   activity.startActivity(new Intent(activity, GreenUserInfoCertifiedEdit_.class));
                }
            }
        });
    }


    private List<Map<String, Object>> getdata() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "add");
        list.add(map);
        return list;
    }

    private List<Map<String, Object>> getdata(JSONArray array) {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                try {
                    JSONObject object = array.getJSONObject(i);
                    map.put("number", object.getString("frame_number"));
                    map.put("id", object.getString("id"));
                    map.put("userid", object.getString("userid"));
                    Long date = Long.parseLong(object.get("time").toString()) * 1000;
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateTime = df.format(date);
                    map.put("date", dateTime);
                    map.put("battery", object.getString("catname_battery"));
                    map.put("batteryID", object.getString("battery_number"));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                map.put("type", "normal");
                list.add(map);
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", "add");
            list.add(map);
        }
        return list;
    }

    @Background
    void HttpCertified() {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/member/my_car";
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

                String messageString = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        returnHttpCertified(messageString, data);
                    } else {
                        returnSuccess(messageString);
                    }
                } else if (code.equals("200")) {
                    if (messageString.equals("没有认证车辆")) {
                        nullCarCertified();
                    } else if(messageString.equals("")){

                    }else {
                        returnError(messageString);
                    }
                } else {
                    returnError(messageString);
                }
            } else {
                returnError("服务器错误：HttpCertified");
            }
        } catch (Exception e) {
            returnError("json解析错误：HttpCertified");
        }

    }

    @Background
    void HttpDeleteCertified(String id) {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/member/delete_my_car";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("id", id));

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
                    returnSuccess(messageStr);
                } else {
                    returnError(messageStr);
                }
            } else {
                returnError("服务器错误：HttpDeleteCertified");
            }
        } catch (Exception e) {
            returnError("json解析错误：HttpDeleteCertified");
        }
    }
}

class GreenUserInfoCertifiedAdapter extends BaseAdapter {
    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;
    private ViewHolder mHolder;

    protected List<? extends Map<String, ?>> mData;

    private int[] mResources;
    private int[] mDropDownResources;
    private LayoutInflater mInflater;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, ?>> mUnfilteredData;

    public GreenUserInfoCertifiedAdapter(Context context, List<? extends Map<String, ?>> data, int[] resources, String[] from, int[] to) {
        mData = data;
        mResources = mDropDownResources = resources;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount() {
        return mResources.length;
    }

    public int getCount() {
        return mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

        } else {

        }
        return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }
        bindView(position, v);

        return v;
    }

    public void setDropDownViewResource(int[] resources) {
        this.mDropDownResources = resources;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
    }

    private void bindView(int position, View view) {
        final Map<String, ?> dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        final ViewBinder binder = mViewBinder;
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(
                                    v.getClass().getName() + " should be bound to a Boolean, not a "
                                            + (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else if (v instanceof Spinner) {
                        if (data instanceof Integer) {
                            ((Spinner) v).setSelection((Integer) data);
                        } else {
                            continue;
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a "
                                + " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    public static interface ViewBinder {
        boolean setViewValue(View view, Object data, String textRepresentation);
    }

    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, ?>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {

                        int len = mTo.length;

                        for (int j = 0; j < len; j++) {
                            String str = (String) h.get(mFrom[j]);

                            String[] words = str.split(" ");
                            int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                String word = words[k];

                                if (word.toLowerCase().startsWith(prefixString)) {
                                    newValues.add(h);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // noinspection unchecked
            mData = (List<Map<String, ?>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).get("type").toString().equals("normal")) {
            return 0;
        } else if (mData.get(position).get("type").toString().equals("add")) {
            return 1;
        } else {
            return 0;
        }
    }

    private class ViewHolder {
        ImageView mImageView;
        TextView mTextView;
    }
}

