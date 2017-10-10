package com.example.fullenergy.main;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.CreateFile;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Panel extends FragmentActivity implements OnClickListener {

    private LinearLayout panelIndexLayout, panelShopLayout, panelServiceLayout, panelMineLayout,panelFocus;
    private PanelIndex panelIndex = null;
    private PanelShop panelShop = null;
    private PanelService panelService = null;
    private PanelMine panelMine = null;
    private int currentPage;
    public static Handler mine2ShopHandler;

    public ImageView panelFoot1, panelFoot2, panelFoot3, panelFoot4;
    private TextView panelFootText1,panelFootText2,panelFootText3,panelFootText4;

    private InputMethodManager manager = null;
    public static Handler httpPanelVersionSuccessHandler, httpPanelVersionErrorHandler, httpPanelVersionUnknownHandler,
            turnToLogin, httpPanelCountSuccessHandler, httpPanelCountErrorHandler, httpChangeCount,downloadSuccess;
    private HttpPanelVersion th;
    private HttpPanelCount th1;

    private TextView panelCount;
    private Activity activity;

    private HttpDownLoadApk httpDownLoadApk;

    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel);
        activity = this;

        init();
        hanlder();
        main();
    }

    private void main() {
        th = new HttpPanelVersion(this);
        th.start();
        th1 = new HttpPanelCount(this);
        th1.start();
    }

    private void init() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        panelIndex = new PanelIndex();
        fragmentTransaction.add(R.id.panelFragement, panelIndex);
        fragmentTransaction.show(panelIndex);
        fragmentTransaction.commit();
        currentPage = 1;

        new StatusBar(this);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.title_black);

        progressDialog = new ProgressDialog(this);

        panelIndexLayout = (LinearLayout) this.findViewById(R.id.panelIndex);
        panelIndexLayout.setOnClickListener(this);
        panelShopLayout = (LinearLayout) this.findViewById(R.id.panelShop);
        panelShopLayout.setOnClickListener(this);
        panelServiceLayout = (LinearLayout) this.findViewById(R.id.panelService);
        panelServiceLayout.setOnClickListener(this);
        panelMineLayout = (LinearLayout) this.findViewById(R.id.panelMine);
        panelMineLayout.setOnClickListener(this);

        panelFocus = (LinearLayout) this.findViewById(R.id.panelFocus);

        panelFoot1 = (ImageView) this.findViewById(R.id.panelFoot1);
        panelFoot2 = (ImageView) this.findViewById(R.id.panelFoot2);
        panelFoot3 = (ImageView) this.findViewById(R.id.panelFoot3);
        panelFoot4 = (ImageView) this.findViewById(R.id.panelFoot4);

        panelFootText1 = (TextView) this.findViewById(R.id.panelFoot1Text);
        panelFootText2 = (TextView) this.findViewById(R.id.panelFoot2Text);
        panelFootText3 = (TextView) this.findViewById(R.id.panelFoot3Text);
        panelFootText4 = (TextView) this.findViewById(R.id.panelFoot4Text);

        panelCount = (TextView) this.findViewById(R.id.panelCount);

        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    private void hanlder() {

        mine2ShopHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                creatPanel(2);
                if (PanelShopIndex.setCurrentItemHandler != null) {
                    Message msg1 = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("page", 0);
                    msg1.setData(bundle);
                    PanelShopIndex.setCurrentItemHandler.sendMessage(msg1);
                }
            }
        };

        httpChangeCount = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                th1 = new HttpPanelCount(activity);
                th1.start();
            }
        };

        httpPanelVersionErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "返回版本信息出现错误！", Toast.LENGTH_SHORT).show();
            }
        };

        httpPanelVersionSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                try {
                    JSONObject jsonObjectVersion = th.getResult();
                    int versionCode;
                    versionCode = Integer.parseInt(jsonObjectVersion.getString("version"));
                    PackageManager manager = activity.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
                    final int versionCodeLocal = info.versionCode;
                    if (versionCode > versionCodeLocal) {
                        LayoutInflater inflater = LayoutInflater.from(activity);
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        final AlertDialog mAlertDialog = builder.create();
                        View view = inflater.inflate(R.layout.alertdialog, null);
                        TextView titleSmall = (TextView) view.findViewById(R.id.alertdialogTitleSmall);
                        titleSmall.setText("");
                        titleSmall.setVisibility(View.GONE);
                        TextView content = (TextView) view.findViewById(R.id.alertdialogContent);
                        content.setText("");
                        content.setVisibility(View.GONE);
                        ImageView divid = (ImageView) view.findViewById(R.id.alertDialogDivid);
                        divid.setVisibility(View.GONE);

                        TextView title = (TextView) view.findViewById(R.id.alertDialogTitle);
                        title.setText("已发现新版本，是否下载？");

                        TextView success = (TextView) view.findViewById(R.id.payAlertdialogSuccess);
                        success.setText("下载");
                        success.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                httpDownLoadApk = new HttpDownLoadApk("http://www.huandianwang.com/APP/huandian.apk",getApplicationContext(),1);
                                httpDownLoadApk.start();
                                Toast.makeText(getApplicationContext(),"正在后台进行下载，请稍后！",Toast.LENGTH_LONG).show();
                                mAlertDialog.dismiss();
                            }
                        });

                        TextView error = (TextView) view.findViewById(R.id.payAlertdialogError);
                        error.setText("取消");
                        error.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mAlertDialog.dismiss();
                            }
                        });
                        mAlertDialog.show();
                        mAlertDialog.getWindow().setContentView(view);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        httpPanelVersionUnknownHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        };

        turnToLogin = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("usrename", null);
                editor.putString("password", null);
                editor.putString("jy_password", null);
                editor.putString("PHPSESSID", null);
                editor.putString("api_userid", null);
                editor.putString("api_username", null);
                editor.commit();
                Intent intent = new Intent(activity, Login.class);
                intent.putExtra("type", "1");
                activity.startActivity(intent);
                activity.finish();
                activity.overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        };

        httpPanelCountSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                JSONObject jsonObject = th1.getResult();
                try {
                    String nums = jsonObject.getString("nums");
                    if (nums.equals("0")) {
                        panelCount.setVisibility(View.GONE);
                    } else {
                        panelCount.setVisibility(View.VISIBLE);
                        panelCount.setText(nums);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        httpPanelCountErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "返回消息出现错误！", Toast.LENGTH_SHORT).show();
            }
        };

        downloadSuccess = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                File file = httpDownLoadApk.getFile();
                Toast.makeText(getApplicationContext(), "下载成功！", Toast.LENGTH_SHORT).show();
                System.out.println(file.toString());
                openFile(file);
            }
        };
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        if (panelIndexLayout.getId() == arg0.getId()) {
            if (this.currentPage != 1) {
                creatPanel(1);
            }
        } else if (panelShopLayout.getId() == arg0.getId()) {
            if (this.currentPage != 2) {
                creatPanel(2);
            }
        } else if (panelServiceLayout.getId() == arg0.getId()) {
            if (this.currentPage != 3) {
                creatPanel(3);
            }
        } else if (panelMineLayout.getId() == arg0.getId()) {
            if (this.currentPage != 4) {
                creatPanel(4);
            }
        }
    }

    public void creatPanel(int page) {

        panelFoot1.setImageResource(R.drawable.foot_001);
        panelFoot2.setImageResource(R.drawable.foot_002);
        panelFoot3.setImageResource(R.drawable.foot_003);
        panelFoot4.setImageResource(R.drawable.foot_004);

        panelFootText1.setTextColor(0xff666666);
        panelFootText2.setTextColor(0xff666666);
        panelFootText3.setTextColor(0xff666666);
        panelFootText4.setTextColor(0xff666666);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (panelIndex != null) {
            fragmentTransaction.hide(panelIndex);
        }
        if (panelShop != null) {
            fragmentTransaction.hide(panelShop);
        }
        if (panelService != null) {
            fragmentTransaction.hide(panelService);
        }
        if (panelMine != null) {
            fragmentTransaction.hide(panelMine);
        }

        switch (page) {
            case 1:
                panelFoot1.setImageResource(R.drawable.foot_001_1);
                panelFootText1.setTextColor(0xffff9c2c);
                if (panelIndex == null) {
                    panelIndex = new PanelIndex();
                    fragmentTransaction.add(R.id.panelFragement, panelIndex);
                }
                fragmentTransaction.show(panelIndex);
                this.currentPage = 1;
                break;
            case 2:
                panelFoot2.setImageResource(R.drawable.foot_002_1);
                panelFootText2.setTextColor(0xffff9c2c);
                if (panelShop == null) {
                    panelShop = new PanelShop();
                    fragmentTransaction.add(R.id.panelFragement, panelShop);
                }
                fragmentTransaction.show(panelShop);
                this.currentPage = 2;
                break;
            case 3:
                panelFoot3.setImageResource(R.drawable.foot_003_1);
                panelFootText3.setTextColor(0xffff9c2c);
                if (panelService == null) {
                    panelService = new PanelService();
                    fragmentTransaction.add(R.id.panelFragement, panelService);
                }
                fragmentTransaction.show(panelService);
                this.currentPage = 3;
                break;
            case 4:
                panelFoot4.setImageResource(R.drawable.foot_004_1);
                panelFootText4.setTextColor(0xffff9c2c);
                if (panelMine == null) {
                    panelMine = new PanelMine();
                    fragmentTransaction.add(R.id.panelFragement, panelMine);
                } else {
                    PanelMine.panaelMineToIndexHandler.sendMessage(new Message());
                }
                fragmentTransaction.show(panelMine);
                this.currentPage = 4;
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                panelFocus.requestFocus();
            }
        }
        return super.onTouchEvent(event);
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

}

class HttpDownLoadApk extends Thread {

    private String httpUrl = null;
    private Context context = null;
    private File file = null;
    private int type;

    public HttpDownLoadApk(String httpUrl, Context context,int type) {
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
            Panel.downloadSuccess.sendMessage(new Message());
        }else if(type == 2){
            PanelMineSetUp.downloadSuccess.sendMessage(new Message());
        }
    }

    public File getFile(){
        return file;
    }
}

class HttpPanelVersion extends Thread {

    private JSONObject jsonObject;
    private Activity activity;

    public HttpPanelVersion(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/home/and_version";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie",
                "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                String code = jsonObject.getString("code");
                String messageStr = jsonObject.getString("message");
                this.jsonObject = jsonObject.getJSONObject("data");

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                message.setData(bundle);

                if (activity != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            Panel.turnToLogin.sendMessage(new Message());
                        } else {
                            Panel.httpPanelVersionErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        Panel.httpPanelVersionSuccessHandler.sendMessage(new Message());
                    } else {
                        Panel.httpPanelVersionUnknownHandler.sendMessage(new Message());
                    }
                }

            }
        } catch (Exception e) {
            if (activity != null) {
                Panel.httpPanelVersionUnknownHandler.sendMessage(new Message());
            }
        }
    }

    public JSONObject getResult() {
        return this.jsonObject;
    }
}

class HttpPanelCount extends Thread {

    private JSONObject jsonObject;
    private Activity activity;

    public HttpPanelCount(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        SharedPreferences preferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/no_read_nums";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie",
                "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String code = jsonObject.getString("code");
                String messageStr = jsonObject.getString("message");
                this.jsonObject = jsonObject.getJSONObject("data");

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                message.setData(bundle);

                if (activity != null) {
                    if (code.equals("200")) {
                        if (messageStr.equals("秘钥不正确,请重新登录")) {
                            Panel.turnToLogin.sendMessage(new Message());
                        } else {
                            Panel.httpPanelCountErrorHandler.sendMessage(message);
                        }
                    } else if (code.equals("100")) {
                        Panel.httpPanelCountSuccessHandler.sendMessage(new Message());
                    } else {
                        Panel.httpPanelVersionUnknownHandler.sendMessage(new Message());
                    }
                }

            }
        } catch (Exception e) {
            if (activity != null) {
                Panel.httpPanelVersionUnknownHandler.sendMessage(new Message());
            }
        }
    }

    public JSONObject getResult() {
        return this.jsonObject;
    }
}
