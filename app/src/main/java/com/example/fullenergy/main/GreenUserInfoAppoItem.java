package com.example.fullenergy.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
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

import com.alipay.a.a.c;
import com.google.gson.JsonArray;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

@EFragment(R.layout.green_userinfo_appo_item)
public class GreenUserInfoAppoItem extends Fragment implements OnScrollListener {
    
    
    @ViewById
    PullToRefreshListView listview;

    private Fragment fragement;
    private int catid;
    private SimpleAdapter adapter;
    private HttpAppoItem th;
    private HttpAppoItem th_re;
    private SharedPreferences preferences;
    private Handler panelMineAppoItemSuccessHandler, panelMineAppoItemErrorHandler, panelMineAppoItemUnknownHandler, panelMineAppoItemSuccessReHandler, turnToLogin;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private int count_anli_page = 2;
    private int current_page = 1;
    private int[] item = new int[]{R.layout.green_userinfo_appo_item_0, R.layout.green_userinfo_appo_item_1, R.layout.green_userinfo_appo_item_2};

    @AfterViews
    void afterview(){
        fragement = this;

        init();
        handler();
        main();
    }

    private void handler() {

        panelMineAppoItemSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                JSONArray jsonArray = th.getResult();
                int item_id = 0;
                if (catid == 0) {
                    item_id = item[0];
                } else if (catid == 1) {
                    item_id = item[1];
                } else if (catid == 2) {
                    item_id = item[2];
                }
                adapter = new SimpleAdapter(getActivity(), getdata(jsonArray), item_id,
                        new String[]{"name", "more", "bettary", "mobile", "date", "address"},
                        new int[]{R.id.panelMineAppoItemName, R.id.panelMineAppoItemMore,
                                R.id.panelMineAppoItemBettary, R.id.panelMineAppoItemMobile, R.id.panelMineAppoItemDate,
                                R.id.panelMineAppoItemAddress});
                listview.setAdapter(adapter);
                listview.onRefreshComplete();
            }
        };

        panelMineAppoItemSuccessReHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                JSONArray temp_result = null;
                temp_result = th_re.getResult();
                if (temp_result.toString().equals("[]")) {
                    Toast.makeText(getActivity(), "已经加载到最底！", Toast.LENGTH_LONG).show();
                } else {
                    JSONObject jsonObject = null;
                    for (int i = 0; i < temp_result.length(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        try {
                            jsonObject = temp_result.getJSONObject(i);
                            map.put("name", jsonObject.get("companyname"));
                            map.put("more", jsonObject.get("surplus_time") + " 分钟");
                            map.put("mobile", jsonObject.get("mobile"));
                            map.put("address", jsonObject.get("address"));
                            if (jsonObject.get("catname_battery").equals("A")) {
                                map.put("bettary", "60V20电池");
                            } else {
                                map.put("bettary", "40V20电池");
                            }

                            Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            String dateTime = df.format(date);
                            map.put("date", dateTime);
                            map.put("status", jsonObject.get("status"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        list.add(map);
                    }
                    adapter.notifyDataSetChanged();
                    count_anli_page = count_anli_page + 1;
                }
            }
        };

        panelMineAppoItemErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                listview.onRefreshComplete();
            }
        };

        panelMineAppoItemUnknownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
                listview.onRefreshComplete();
            }
        };

        turnToLogin = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
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
        };
    }

    private List<Map<String, Object>> getdata(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                map.put("name", jsonObject.get("companyname"));
                map.put("more", jsonObject.get("surplus_time") + " 分钟");
                map.put("mobile", jsonObject.get("mobile"));
                map.put("address", jsonObject.get("address"));
                if (jsonObject.get("catname_battery").equals("A")) {
                    map.put("bettary", "60V20电池");
                } else {
                    map.put("bettary", "40V20电池");
                }
                Long date = Long.parseLong(jsonObject.get("time").toString()) * 1000;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateTime = df.format(date);
                map.put("date", dateTime);
                map.put("status", jsonObject.get("status"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            list.add(map);
        }
        return list;
    }

    private void init() {

        catid = Integer.parseInt(getArguments().getString("id")) ;
        listview.setOnScrollListener(this);
        listview.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                list.clear();
                count_anli_page = 2;
                current_page = 1;
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                if (PubFunction.isNetworkAvailable(getActivity())) {
                    init();
                    handler();
                    th = new HttpAppoItem(catid, preferences, panelMineAppoItemSuccessHandler,
                            panelMineAppoItemErrorHandler, panelMineAppoItemUnknownHandler, panelMineAppoItemSuccessReHandler,
                            turnToLogin, 1, fragement, getActivity());
                    th.start();
                } else {
                    Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                    listview.onRefreshComplete();
                }
            }
        });
    }

    private void main() {
        if (PubFunction.isNetworkAvailable(getActivity())) {
            preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
            th = new HttpAppoItem(catid, preferences, panelMineAppoItemSuccessHandler,
                    panelMineAppoItemErrorHandler, panelMineAppoItemUnknownHandler, panelMineAppoItemSuccessReHandler,
                    turnToLogin, 1, this, getActivity());
            th.start();
        } else {
            Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_anli_page) {
                if (PubFunction.isNetworkAvailable(getActivity())) {
                    th_re = new HttpAppoItem(catid, preferences, panelMineAppoItemSuccessHandler,
                            panelMineAppoItemErrorHandler, panelMineAppoItemUnknownHandler, panelMineAppoItemSuccessReHandler,
                            turnToLogin, count_anli_page, fragement, getActivity());

                    th_re.start();
                    current_page = count_anli_page;
                } else {
                    Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (list != null) {
            list.clear();
        }
    }
}

class HttpAppoItem extends Thread {

    private int catid;
    private SharedPreferences preferences;
    private JSONArray jsonArray;
    private Handler SuccessHandler, SuccessREHandler, ErrorHandler, UnknownHandler;
    private int page;
    private Fragment fragment;
    private Activity activity;
    private Handler turnToLogin;
    private String[] str = new String[]{"api.php/member/my_booking_doing", "api.php/member/my_booking_success", "api.php/member/my_booking_fail"};

    public HttpAppoItem(int catid, SharedPreferences preferences, Handler SuccessHandler, Handler ErrorHandler,
                        Handler UnknownHandler, Handler SuccessREHandler, Handler turnToLogin, int page, Fragment fragment,
                        Activity activity) {
        this.catid = catid;
        this.preferences = preferences;
        this.SuccessHandler = SuccessHandler;
        this.SuccessREHandler = SuccessREHandler;
        this.ErrorHandler = ErrorHandler;
        this.UnknownHandler = UnknownHandler;
        this.page = page;
        this.fragment = fragment;
        this.activity = activity;
        this.turnToLogin = turnToLogin;
    }

    @Override
    public void run() {
        super.run();

        System.out.println(page + "");

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + str[catid];
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie",
                "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("page", page + ""));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject.toString());

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (fragment.getView() != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            turnToLogin.sendMessage(new Message());
                        } else {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("message", messageStr);
                            message.setData(bundle);
                            ErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        if (page == 1) {
                            this.jsonArray = jsonObject.getJSONArray("data");
                            SuccessHandler.sendMessage(new Message());
                        } else {
                            this.jsonArray = jsonObject.getJSONArray("data");
                            SuccessREHandler.sendMessage(new Message());
                        }
                    } else {
                        UnknownHandler.sendMessage(new Message());
                    }
                }

            }
        } catch (Exception e) {
            if (fragment.getView() != null) {
                System.err.println(e + "");
                UnknownHandler.sendMessage(new Message());
            }
        }
    }

    public JSONArray getResult() {
        return jsonArray;
    }
}