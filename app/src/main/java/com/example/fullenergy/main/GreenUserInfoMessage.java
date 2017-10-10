package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

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
@EActivity(R.layout.green_userinfo_message)
public class GreenUserInfoMessage extends Activity implements AbsListView.OnScrollListener {

    private Activity activity;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    private int count_anli_page = 2;
    private int current_page = 1;

    private GreenMessageAdapter adapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();


    @ViewById
    LinearLayout page_return;
    @ViewById
    PullToRefreshListView listview;

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

        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                if (adapter == null) {
                    listview.onRefreshComplete();
                } else {
                    list.clear();
                    count_anli_page = 2;
                    current_page = 1;
                    adapter.notifyDataSetChanged();
                    if (PubFunction.isNetworkAvailable(activity)) {
                        httpUpReward(1);
                        progressDialog.show();
                    } else {
                        Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, GreenUserInfoMessageInfo_.class);
                intent.putExtra("id", list.get(position-1).get("id") + "");
                activity.startActivity(intent);
            }
        });
        listview.setOnScrollListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        list.clear();

        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

        httpUpReward(1);
        progressDialog.show();
    }

    private List<Map<String, Object>> getdata(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                map.put("title", jsonObject.get("title").toString());
                map.put("content", jsonObject.get("content").toString());
                String read = jsonObject.get("read").toString();
                if (read.equals("1")) {
                    map.put("style", "已读");
                } else {
                    map.put("style", "未读");
                }
                Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String dateTime = df.format(date);
                map.put("date", dateTime);
                map.put("id", jsonObject.get("id").toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            list.add(map);
        }
        return list;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
                if (PubFunction.isNetworkAvailable(activity)) {
                    httpUpReward(count_anli_page);
                    progressDialog.show();
                    current_page = count_anli_page;
                } else {
                    Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Click
    void page_return() {
        this.finish();
    }

    @UiThread
    void renturnError(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnSuccess(String str) {
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
        this.finish();
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
    void returnHttpUpReward(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            String jsonType = PubFunction.getJSONType(data);
            if (jsonType.equals("JSONArray")) {
                JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                adapter = new GreenMessageAdapter(activity, getdata(jsonArray),new int[]{R.layout.panel_mine_message_item},
                        new String[] { "title", "content", "date","style"}, new int[] { R.id.panelMineMessageTitle,
                        R.id.panelMineMessageContent, R.id.panelMineMessageDate ,R.id.panelMineMessageStyle});
            } else if (jsonType.equals("JSONObject")) {
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        listview.setAdapter(adapter);
        listview.onRefreshComplete();
        progressDialog.dismiss();
    }

    @UiThread
    void scrollBottom() {
        Toast.makeText(activity, "已经加载到最底！", Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }


    @UiThread
    void returnHttpUpRewardRE(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
            if (jsonArray.toString().equals("[]")) {
                scrollBottom();
            } else {
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                        map.put("title", jsonObject.get("title").toString());
                        map.put("content", jsonObject.get("content").toString());
                        String read = jsonObject.get("read").toString();
                        if (read.equals("1")) {
                            map.put("style", "已读");
                        } else {
                            map.put("style", "未读");
                        }
                        Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String dateTime = df.format(date);
                        map.put("date", dateTime);
                        map.put("id", jsonObject.get("id").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    list.add(map);
                }
                adapter.notifyDataSetChanged();
                count_anli_page = count_anli_page + 1;
            }
            progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Background
    void httpUpReward(int page) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/my_message_list";
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("page", page + ""));
        HttpPost httpPost = new HttpPost(path);
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

                String messageString = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        if (page == 1) {
                            returnHttpUpReward(messageString, data);
                        } else {
                            returnHttpUpRewardRE(messageString, data);
                        }
                    } else {
                        returnSuccess(messageString);
                    }
                } else if (code.equals("200")) {
                    if (messageString.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageString);
                    }
                } else {
                    renturnError(messageString);
                }
            } else {
                renturnError("服务器错误：httpUpReward");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpUpReward");
        }
    }

}

class GreenMessageAdapter extends BaseAdapter implements Filterable {
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

    public GreenMessageAdapter(Context context, List<? extends Map<String, ?>> data, int[] resources, String[] from,
                                   int[] to) {
        mData = data;
        mResources = mDropDownResources = resources;
        mFrom = from;
        mTo = to;
        if (context != null) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
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

        if(convertView == null){
            convertView = mInflater.inflate(mResources[getItemViewType(position)], null);
            mHolder = new ViewHolder();
            mHolder.mTextView = (TextView) convertView.findViewById(R.id.panelMineMessageStyle);
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }
        if(mData.get(position).get("style").equals("已读")){
            mHolder.mTextView.setTextColor(0xff40b25d);
        }else{
            mHolder.mTextView.setTextColor(0xffff9c2c);
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
        return 0;
    }

    private class ViewHolder {
        ImageView mImageView;
        TextView mTextView;
    }
}