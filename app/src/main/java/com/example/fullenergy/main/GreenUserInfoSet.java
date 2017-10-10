package com.example.fullenergy.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.CreateFile;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2017/8/24.
 */
@EActivity(R.layout.green_userinfo_setup)
public class GreenUserInfoSet extends Activity {

    private Activity activity;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    @ViewById
    LinearLayout page_return, feedback, version, password;
    @ViewById
    TextView logout,version_code;
    @ViewById
    ImageView point;


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

        PackageManager pm = activity.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        version_code.setText(pi.versionName.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        httpGetVersion();
        progressDialog.show();
    }


    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void password() {
        activity.startActivity(new Intent(activity, GreenUserInfoSetPswd_.class));
    }

    @Click
    void feedback() {
        activity.startActivity(new Intent(activity, GreenUserInfoSetFeedback_.class));
    }

    @Click
    void logout() {
        LayoutInflater inflater = LayoutInflater.from(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog mAlertDialog = builder.create();
        View view = inflater.inflate(R.layout.green_alertdialog, null);

        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
        title.setText("确认注销帐号？");

        TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
        titleSmall.setText("");
        titleSmall.setVisibility(View.GONE);

        TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
        content.setText("");
        content.setVisibility(View.GONE);

        ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
        divid.setVisibility(View.GONE);

        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
        success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("usrename", null);
                editor.putString("password", null);
                editor.putString("jy_password", null);
                editor.putString("PHPSESSID", null);
                editor.putString("api_userid", null);
                editor.putString("api_username", null);
                editor.commit();

                startActivity(new Intent(activity, Login_.class));
                activity.finish();
            }
        });
        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
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
    void returnHttpGetVersion(String str, String data) {
        try {

            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObjectVersion = (JSONObject) jsonTokener.nextValue();
            final int versionCode = Integer.parseInt(jsonObjectVersion.getString("version"));
            PackageManager manager = activity.getPackageManager();
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            final int versionCodeLocal = info.versionCode;
            if (versionCode > versionCodeLocal) {
                point.setVisibility(View.VISIBLE);
            }
            version.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    LayoutInflater inflater = LayoutInflater.from(activity);
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    final AlertDialog mAlertDialog = builder.create();
                    View view = inflater.inflate(R.layout.green_alertdialog, null);
                    TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
                    content.setText("");
                    content.setVisibility(View.GONE);
                    ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
                    divid.setVisibility(View.GONE);

                    if (versionCode > versionCodeLocal) {
                        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
                        title.setText("已发现新版本，是否下载？");

                        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
                        success.setText("下载");
                        success.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                HttpDownLoadApk("http://www.huandianwang.com/APP/huandian2.20.apk");
                                MyToast.showTheToast(activity,"正在后台进行下载，请稍后！");
                                mAlertDialog.dismiss();
                            }
                        });

                    } else {
                        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
                        title.setText("未检测到新版本！");

                        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
                        success.setText("确定");
                        success.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mAlertDialog.dismiss();
                            }
                        });
                    }

                    TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
                    error.setText("取消");
                    error.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            mAlertDialog.dismiss();
                        }
                    });
                    mAlertDialog.show();
                    mAlertDialog.getWindow().setContentView(view);
                }
            });

            progressDialog.dismiss();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Background
    void httpGetVersion() {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/home/and_version";
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
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        returnHttpGetVersion(messageStr, data);
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
                renturnError("服务器错误：httpGetVersion");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpGetVersion");
        }
    }

    @Background
    void HttpDownLoadApk(String httpUrl) {
        final String fileName = "updata.apk";
        File file = new File(CreateFile.SELFDIR + fileName);
        try {
            URL url = new URL(httpUrl);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                if (conn.getResponseCode() >= 400) {
                    Toast.makeText(activity, "连接超时", Toast.LENGTH_SHORT).show();
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                                System.out.println(buf);
                            }
                        } else {
                            break;
                        }
                    }
                }
                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        openFile(file);
    }

    @UiThread
    void openFile(File file) {
        // TODO Auto-generated method stub
        Toast.makeText(activity, "下载成功！", Toast.LENGTH_SHORT).show();
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }


}
