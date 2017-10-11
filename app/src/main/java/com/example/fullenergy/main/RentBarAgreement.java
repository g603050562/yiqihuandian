package com.example.fullenergy.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.MyToast;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.example.fullenergy.pub.scanCode.CaptureActivity;
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

/**
 * Created by apple on 2017/8/29.
 */

@EActivity(R.layout.rentbar_agreement)
public class RentBarAgreement extends Activity {

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    @ViewById
    WebView webview;

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView button;

    private Activity activity;

    @AfterViews
    void afterviews(){
        init();
        handler();
    }

    private void handler() {

    }

    private void init() {

        activity = this;
        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);

        String type = getIntent().getStringExtra("type");
        String url = "";
        if(type.equals("car")){
            url = "http://www.huandianwang.com/page/rent_car.html";
        }else if(type.equals("bar")){
            url = "http://www.huandianwang.com/page/rent_car.html";
        }else if(type.equals("other")){
            url = "http://www.huandianwang.com/page/rent_car.html";
        }

        webview.loadUrl(url);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
    }

    @Click
    void page_return(){
        this.finish();
    }
    @Click
    void button(){
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, 0x0001);
    }

}
