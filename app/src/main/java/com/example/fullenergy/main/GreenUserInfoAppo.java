package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.FragementPagerAdapter.MyFragmentPagerAdapter;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.extend_plug.ViewPagerScroller.ViewPagerScroller;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import static com.example.fullenergy.R.id.pswd;
import static com.example.fullenergy.R.id.pswd_re;

/**
 * Created by apple on 2017/8/24.
 */
@EActivity(R.layout.green_userinfo_appo)
public class GreenUserInfoAppo extends FragmentActivity {

    private Activity activity;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    @ViewById
    LinearLayout page_return,pm_horizontal_linearLayout;
    @ViewById
    ViewPager view_page;
    @ViewById
    ImageView m_bottom_img;
    @ViewById
    HorizontalScrollView m_horizontal;

    private int mScreenWidth;
    private int item_width;
    private RelativeLayout nav_rel;
    private ArrayList<Fragment> fragments;
    private String[] str = new String[] { "预约进行中", "预约完成", "预约超时" };
    private int endPosition;
    private int beginPosition;
    private int currentFragmentIndex;
    private boolean isEnd;



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

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        item_width = (int) ((mScreenWidth / 3.0 + 0.5f));
        m_bottom_img.getLayoutParams().width = item_width;
        initViewPager();// 页面初始化
        initNav();// 导航初始化
        view_page.setCurrentItem(0);
        view_page.setOffscreenPageLimit(1);
    }

    private void initViewPager() {
        fragments = new ArrayList<Fragment>();
        for (int i = 0; i < str.length; i++) {
            Bundle bundle = new Bundle();
            bundle.putString("id",i+"");
            GreenUserInfoAppoItem greenUserInfoAppoItem = new GreenUserInfoAppoItem_();
            greenUserInfoAppoItem.setArguments(bundle);
            fragments.add(greenUserInfoAppoItem);
        }
        FragmentStatePagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments, activity);
        view_page.setAdapter(adapter);
        ViewPagerScroller scroller = new ViewPagerScroller(activity);
        scroller.setScrollDuration(1000);
        scroller.initViewPagerScroll(view_page);
        view_page.setOnPageChangeListener(new MyOnPageChangeListener());
        view_page.setCurrentItem(0);
    }

    private void initNav() {
        for (int i = 0; i < str.length; i++) {
            nav_rel = new RelativeLayout(activity);
            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.panel_shop_index_top_text, null);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(str[i]);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,     ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);//居中显示。
            nav_rel.addView(view,lp);
            pm_horizontal_linearLayout.addView(nav_rel, (int) (mScreenWidth / 3 + 0.5f), 50);
            nav_rel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view_page.setCurrentItem((Integer) v.getTag());
                }
            });
            nav_rel.setTag(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
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


    @Background
    void httpUploadPswd(String pswd) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/set_jy_password";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("jy_password", pswd));
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
                    returnSuccess(messageString);
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
                renturnError("服务器错误：httpReceiveAddress");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpReceiveAddress");
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(final int position) {
            Animation animation = new TranslateAnimation(endPosition, position * item_width, 0, 0);

            beginPosition = position * item_width;

            currentFragmentIndex = position;
            if (animation != null) {
                animation.setFillAfter(true);
                animation.setDuration(0);
                m_bottom_img.startAnimation(animation);
                m_horizontal.smoothScrollTo((currentFragmentIndex - 1) * item_width, 0);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isEnd) {
                if (currentFragmentIndex == position) {
                    endPosition = item_width * currentFragmentIndex + (int) (item_width * positionOffset);
                }
                if (currentFragmentIndex == position + 1) {
                    endPosition = item_width * currentFragmentIndex - (int) (item_width * (1 - positionOffset));
                }

                Animation mAnimation = new TranslateAnimation(beginPosition, endPosition, 0, 0);
                mAnimation.setFillAfter(true);
                mAnimation.setDuration(0);
                m_bottom_img.startAnimation(mAnimation);
                m_horizontal.invalidate();
                beginPosition = endPosition;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                isEnd = false;
            } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                isEnd = true;
                beginPosition = currentFragmentIndex * item_width;
                if (view_page.getCurrentItem() == currentFragmentIndex) {
                    // 未跳入下一个页面
                    m_bottom_img.clearAnimation();
                    Animation animation = null;
                    // 恢复位置
                    animation = new TranslateAnimation(endPosition, currentFragmentIndex * item_width, 0, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(1);
                    m_bottom_img.startAnimation(animation);
                    m_horizontal.invalidate();
                    endPosition = currentFragmentIndex * item_width;
                }
            }
        }
    }

}
