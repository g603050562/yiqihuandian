package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
@EActivity(R.layout.green_userinfo_reward)
public class GreenUserInfoReward extends Activity implements AbsListView.OnScrollListener {

    private Activity activity;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    private int count_anli_page = 2;
    private int current_page = 1;

    private SimpleAdapter adapter;
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
        listview.setOnScrollListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

        httpUpReward(1);
        progressDialog.show();
    }

    private List<Map<String, Object>> getdata(JSONObject jsonObject) {
        for (int i = 0; i < 1; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                map.put("title", jsonObject.get("reward_explain"));
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                String dateTime = df.format(date);
                map.put("content", dateTime);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> getdata(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("title", jsonObject.get("reward_explain"));
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                String dateTime = df.format(date);
                map.put("content", dateTime);
                list.add(map);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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
                adapter = new SimpleAdapter(activity, getdata(jsonArray), R.layout.panel_mine_reward_item,
                        new String[]{"title", "content"},
                        new int[]{R.id.panelMineRewardTitle, R.id.panelMineRewardContent});
            } else if (jsonType.equals("JSONObject")) {
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                adapter = new SimpleAdapter(activity, getdata(jsonObject), R.layout.panel_mine_reward_item,
                        new String[]{"title", "content"},
                        new int[]{R.id.panelMineRewardTitle, R.id.panelMineRewardContent});
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        listview.setAdapter(adapter);
        listview.onRefreshComplete();
        progressDialog.dismiss();
    }

    @UiThread
    void scrollBottom(){
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
                        map.put("title", jsonObject.get("reward_explain"));
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                        String dateTime = df.format(date);
                        map.put("content", dateTime);
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
        String path = PubFunction.www + "api.php/member/my_reward";
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
