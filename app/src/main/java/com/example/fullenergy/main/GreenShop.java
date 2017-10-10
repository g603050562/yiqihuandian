package com.example.fullenergy.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
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
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;


/**
 * Created by apple on 2017/8/21.
 */

@EActivity(R.layout.green_shop)
public class GreenShop extends FragmentActivity {

    private Activity activity;
    private SharedPreferences preferences;

    @ViewById
    LinearLayout page_return, horizontal_linearLayout;
    @ViewById
    ViewPager viewpager;
    @ViewById
    HorizontalScrollView m_horizontal;
    @ViewById
    ImageView horizontal_img;

    RelativeLayout nav_rel;

    private int mScreenWidth;
    private int item_width;

    private ArrayList<Fragment> fragments;

    private int endPosition;
    private int beginPosition;
    private int currentFragmentIndex;
    private boolean isEnd;

    private ProgressDialog progressDialog;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @AfterViews
    void afterViews() {
        init();
        main();
    }

    private void init() {
        activity = this;
        progressDialog = new ProgressDialog(activity);
        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);
    }

    private void main() {
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        httpPanelShopIndex();
        progressDialog.show();
    }

    private void data(JSONArray jsonArray) {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        item_width = (int) ((mScreenWidth / 4.0 + 0.5f));
        horizontal_img.getLayoutParams().width = item_width;
        initViewPager(jsonArray);// 页面初始化
        initNav(jsonArray); //
        viewpager.setCurrentItem(0);
        viewpager.setOffscreenPageLimit(1);
    }

    private void initViewPager(JSONArray jsonArray) {
        fragments = new ArrayList<Fragment>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String catId = jsonObject.getString("id");

                GreenShopItemFragment_ greenShopItemFragment = new GreenShopItemFragment_();
                Bundle bundle = new Bundle();
                bundle.putString("catID",catId);
                greenShopItemFragment.setArguments(bundle);
                fragments.add(greenShopItemFragment);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        FragmentStatePagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments, activity);
        viewpager.setAdapter(adapter);
        ViewPagerScroller scroller = new ViewPagerScroller(activity);
        scroller.setScrollDuration(1000);
        scroller.initViewPagerScroll(viewpager);
        viewpager.setOnPageChangeListener(new MyOnPageChangeListener());
        viewpager.setCurrentItem(0);
    }

    private void initNav(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String catname_goods = jsonObject.getString("catname_goods");
                nav_rel = new RelativeLayout(activity);

                LayoutInflater inflater = LayoutInflater.from(activity);
                View view = inflater.inflate(R.layout.green_shop_top_textview, null);
                TextView textView = (TextView) view.findViewById(R.id.text);
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                if (i == 0) {
                    Picasso.with(activity).load(R.drawable.shop_1).into(imageView);
                } else if (i == 1) {
                    Picasso.with(activity).load(R.drawable.shop_2).into(imageView);
                } else if (i == 2) {
                    Picasso.with(activity).load(R.drawable.shop_3).into(imageView);
                } else if (i == 3) {
                    Picasso.with(activity).load(R.drawable.shop_4).into(imageView);
                }
                textView.setText(catname_goods);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);//居中显示。
                nav_rel.addView(view, lp);
//                nav_rel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        viewpager.setCurrentItem((Integer) nav_rel.getTag());
//                    }
//                });
                horizontal_linearLayout.addView(nav_rel, (int) (mScreenWidth / 4 + 0.5f), PubFunction.dip2px(activity, 80));
                nav_rel.setTag(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    @Click
    void page_return() {
        this.finish();
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
                horizontal_img.startAnimation(animation);
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
                horizontal_img.startAnimation(mAnimation);
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
                if (viewpager.getCurrentItem() == currentFragmentIndex) {
                    // 未跳入下一个页面
                    horizontal_img.clearAnimation();
                    Animation animation = null;
                    // 恢复位置
                    animation = new TranslateAnimation(endPosition, currentFragmentIndex * item_width, 0, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(1);
                    horizontal_img.startAnimation(animation);
                    m_horizontal.invalidate();
                    endPosition = currentFragmentIndex * item_width;
                }
            }
        }
    }

    @UiThread
    void renturnError(String str){
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnSuccess(String str){
        MyToast.showTheToast(activity, str);
        progressDialog.dismiss();
    }

    @UiThread
    void returnHttpPanelShopIndex(String str,String data){
        progressDialog.dismiss();
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            JSONArray jsonArray = jsonObject.getJSONArray("catname");
            data(jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Background
    void httpPanelShopIndex() {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);

        String path = PubFunction.www + "api.php/shop/category_goods";
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

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    if(jsonObject.has("data")){
                        String data = jsonObject.getString("data");
                        returnHttpPanelShopIndex(messageStr,data);
                    }else{
                        returnSuccess(messageStr);
                    }
                } else  {
                    renturnError(messageStr);
                }
            } else{
                renturnError( "服务器错误：HttpPanelShopIndex");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpPanelShopIndex");
        }

    }
}
