package com.example.fullenergy.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.CreateFile;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.green_main)
public class GreenMain extends FragmentActivity {


    private GreenMainPanel GreenMainPanel = null;
    private Activity activity;
    private HttpDownLoadApk_1 httpDownLoadApk_1;

    @ViewById
    ImageView bottom_image, shop, userinfo, bottom_view_white_img;

    @ViewById
    LinearLayout fragment;

    public static Handler showHandler, dismissHandler,downsuccessHandler;

    private int is_open = 0;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    String userNameString, passWordString;

    @AfterViews
    void afterViews() {
        init();
        handler();

        preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        userNameString = preferences.getString("usrename", null);
        passWordString = preferences.getString("password", null);

        HttpGetVersion();
    }

    private void handler() {
        showHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String data = msg.getData().getString("data");

                if (is_open == 0) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    float w_d = Float.parseFloat(dm.widthPixels + "");
                    ObjectAnimator transXAnim = ObjectAnimator.ofFloat(fragment, "translationY", -w_d / 1080 * 220);
                    transXAnim.setRepeatMode(ObjectAnimator.REVERSE);
                    transXAnim.setDuration(500);
                    transXAnim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            bottom_image.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            bottom_image.setClickable(true);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    transXAnim.start();
                    GreenMainPanel.show(data);
                    is_open = 1;
                } else {
                    ObjectAnimator transXAnim = ObjectAnimator.ofFloat(fragment, "translationY", 0);
                    transXAnim.setRepeatMode(ObjectAnimator.REVERSE);
                    transXAnim.setDuration(500);
                    transXAnim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            bottom_image.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            DisplayMetrics dm = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(dm);
                            float w_d = Float.parseFloat(dm.widthPixels + "");
                            ObjectAnimator transXAnim = ObjectAnimator.ofFloat(fragment, "translationY", -w_d / 1080 * 220);
                            transXAnim.setRepeatMode(ObjectAnimator.REVERSE);
                            transXAnim.setDuration(500);
                            transXAnim.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    bottom_image.setClickable(false);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    bottom_image.setClickable(true);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                            transXAnim.start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    transXAnim.start();
                    GreenMainPanel.reShow(data);
                }
            }
        };

        dismissHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (is_open == 1) {
                    ObjectAnimator transXAnim = ObjectAnimator.ofFloat(fragment, "translationY", 0);
                    transXAnim.setRepeatMode(ObjectAnimator.REVERSE);
                    transXAnim.setDuration(500);
                    transXAnim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            bottom_image.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            bottom_image.setClickable(true);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    transXAnim.start();
                    GreenMainPanel.dismiss();
                    is_open = 0;
                }
            }
        };
        downsuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                File file = httpDownLoadApk_1.getFile();
                Toast.makeText(getApplicationContext(), "下载成功！", Toast.LENGTH_SHORT).show();
                System.out.println(file.toString());
                openFile(file);
            }
        };
    }

    //    打开APK程序代码
    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }


    private void init() {


        activity = this;

        progressDialog = new ProgressDialog(activity);
        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);

        FragmentManager fragmentManager1 = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
        fragmentTransaction1.addToBackStack(null);
        fragmentTransaction1.replace(R.id.panel, new GreenMainMap_());
        fragmentTransaction1.commit();

        if (GreenMainPanel == null) {
            GreenMainPanel = new GreenMainPanel_();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.fragment, GreenMainPanel);
            fragmentTransaction.commit();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lacksPermission("Manifest.permission.CAMERA");
            lacksPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            lacksPermission("LOCATION_SERVICE");
        }
    }

    // 判断是否缺少权限
    private void lacksPermission(String permission) {
        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int checkCallPhonePermission = activity.checkSelfPermission(permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //弹出对话框接收权限
                activity.requestPermissions(new String[]{permission}, 1);
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Click
    void bottom_image() {


        Intent intent = new Intent(this, CaptureActivity.class);
        activity.startActivityForResult(intent, 0x0000);
    }

    @Click
    void shop() {
        activity.startActivity(new Intent(activity, GreenShop_.class));
    }

    @Click
    void userinfo() {
        activity.startActivity(new Intent(activity, GreenUserInfo_.class));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 创建退出对话框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 设置对话框标题
            isExit.setTitle("系统提示");
            // 设置对话框消息
            isExit.setMessage("确定要退出吗");
            // 添加选择按钮并注册监听
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            // 显示对话框
            isExit.show();
        }
        return false;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传

        if (requestCode == 0x0000 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");

                HttpUploadQcode(content, userNameString, passWordString);
                progressDialog.show();
            }
        }
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
    void HttpUploadQcode(String cab_id, String username, String password) {

        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "index.php/Login/login";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("cabinetid", cab_id));
        list.add(new BasicNameValuePair("password", password));
        list.add(new BasicNameValuePair("mobile", username));
        list.add(new BasicNameValuePair("type", "2"));

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

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");

                if (code.equals("100")) {
                    returnSuccess(messageStr);
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
                renturnError("服务器错误：HttpGetMarkerInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpGetMarkerInfo");
        }
    }

    @UiThread
    void returnSuccessVersion(String str,String data) {

        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int versionCode;
            versionCode = Integer.parseInt(jsonObject.getString("version"));
            PackageManager manager = activity.getPackageManager();
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            final int versionCodeLocal = info.versionCode;
            if (versionCode > versionCodeLocal) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                final AlertDialog mAlertDialog = builder.create();
                View view = inflater.inflate(R.layout.green_alertdialog, null);
                TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
                content.setText("");
                content.setVisibility(View.GONE);
                ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
                divid.setVisibility(View.GONE);

                TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
                title.setText("已发现新版本，是否下载？");

                TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
                success.setText("下载");
                success.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        httpDownLoadApk_1 = new HttpDownLoadApk_1("http://www.huandianwang.com/APP/huandian2.20.apk",getApplicationContext(),1);
                        httpDownLoadApk_1.start();
                        MyToast.showTheToast(activity,"正在后台进行下载，请稍后！");
                        mAlertDialog.dismiss();
                    }
                });

                TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
                error.setText("取消");
                error.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mAlertDialog.dismiss();
                        activity.finish();
                    }
                });
                mAlertDialog.setCancelable(false);
                mAlertDialog.show();
                mAlertDialog.getWindow().setContentView(view);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Background
    void HttpGetVersion() {

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
                    String data = jsonObject.getString("data");
                    returnSuccessVersion(messageStr,data);
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
                renturnError("服务器错误：HttpGetMarkerInfo");
            }
        } catch (Exception e) {
            renturnError("json解析错误：HttpGetMarkerInfo");
        }
    }
}

class HttpDownLoadApk_1 extends Thread {

    private String httpUrl = null;
    private Context context = null;
    private File file = null;
    private int type;

    public HttpDownLoadApk_1(String httpUrl, Context context,int type) {
        this.httpUrl = httpUrl;
        this.context = context;
        this.type = type;
    }

    @Override
    public void run() {
        super.run();
        // TODO Auto-generated method stub
        final String fileName = "updata.apk";
        file = new File(CreateFile.SELFDIR + fileName);
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
                    Toast.makeText(context, "连接超时", Toast.LENGTH_SHORT).show();
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
        if(type == 1){
            GreenMain.downsuccessHandler.sendMessage(new Message());
        }else if(type == 2){
            PanelMineSetUp.downloadSuccess.sendMessage(new Message());
        }
    }

    public File getFile(){
        return file;
    }
}
