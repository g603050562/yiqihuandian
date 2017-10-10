package com.example.fullenergystore.main;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.StatusBar.statusBar;
import com.example.fullenergystore.pub.CreateFile;
import com.example.fullenergystore.pub.ProgressDialog;
import com.example.fullenergystore.pub.PubFunction;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.annotation.SuppressLint;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Panel extends FragmentActivity implements OnClickListener {

    private LinearLayout panelIndexLayout, panelShopLayout, panelMessageLayout, panelRentLayout, panelFocus;
    private ImageView panelIndexImg, panelShopImg, panelMessageImg, panelRentImg;
    private TextView panelIndexText, panelShopText, panelMessageText, panelRentText;
    private int currentPage;
    private PanelIndex panelIndex = null;
    private PanelMessage panelMessagePanel = null;
    private PanelRentCar panelRentCar = null;
    private PanelShop panelShop = null;

    private HttpDownLoadApk httpDownLoadApk;
    private HttpPanelVersion th;

    public static Handler downloadSuccess,turnToLogin,httpPanelVersionErrorHandler,httpPanelVersionSuccessHandler,httpPanelVersionUnknownHandler;
    private Activity activity;
    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel);
        activity = this;

        init();
        handler();
        main();
    }

    private void main() {
        th = new HttpPanelVersion(this);
        th.start();
    }

    private void handler() {
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

                        TextView success = (TextView) view.findViewById(R.id.AlertdialogSuccess);
                        success.setText("下载");
                        success.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                httpDownLoadApk = new HttpDownLoadApk("http://www.huandianwang.com/APP/huandianBus.apk",getApplicationContext(),1);
                                httpDownLoadApk.start();
                                Toast.makeText(getApplicationContext(),"正在后台进行下载，请稍后！",Toast.LENGTH_LONG).show();
                                mAlertDialog.dismiss();
                            }
                        });

                        TextView error = (TextView) view.findViewById(R.id.AlertdialogCancel);
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
    }

    private void init() {

        new statusBar(this);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);

        progressDialog = new ProgressDialog(this);

        panelIndexLayout = (LinearLayout) this.findViewById(R.id.panelIndex);
        panelIndexLayout.setOnClickListener(this);
        panelShopLayout = (LinearLayout) this.findViewById(R.id.panelShop);
        panelShopLayout.setOnClickListener(this);
        panelMessageLayout = (LinearLayout) this.findViewById(R.id.panelMessage);
        panelMessageLayout.setOnClickListener(this);
        panelRentLayout = (LinearLayout) this.findViewById(R.id.panelRent);
        panelRentLayout.setOnClickListener(this);

        panelIndexImg = (ImageView) this.findViewById(R.id.panelFoot1);
        panelShopImg = (ImageView) this.findViewById(R.id.panelFoot2);
        panelMessageImg = (ImageView) this.findViewById(R.id.panelFoot3);
        panelRentImg = (ImageView) this.findViewById(R.id.panelFoot4);

        panelIndexText = (TextView) this.findViewById(R.id.panelFoot1Text);
        panelShopText = (TextView) this.findViewById(R.id.panelFoot2Text);
        panelMessageText = (TextView) this.findViewById(R.id.panelFoot3Text);
        panelRentText = (TextView) this.findViewById(R.id.panelFoot4Text);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        panelIndex = new PanelIndex();
        fragmentTransaction.add(R.id.panelFragement, panelIndex);
        fragmentTransaction.show(panelIndex);
        fragmentTransaction.commit();
        currentPage = 1;
    }


    @Override
    public void onClick(View arg0) {
        if (panelIndexLayout.getId() == arg0.getId()) {
            if (this.currentPage != 1) {
                creatPanel(1);
            }
        } else if (panelShopLayout.getId() == arg0.getId()) {
            if (this.currentPage != 2) {
                creatPanel(2);
            }
        } else if (panelMessageLayout.getId() == arg0.getId()) {
            if (this.currentPage != 3) {
                creatPanel(3);
            }
        } else if (panelRentLayout.getId() == arg0.getId()) {
            if (this.currentPage != 4) {
                creatPanel(4);
            }
        }
    }

    public void creatPanel(int page) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (panelIndex != null) {
            fragmentTransaction.hide(panelIndex);
        }
        if (panelShop != null) {
            fragmentTransaction.hide(panelShop);
        }
        if (panelMessagePanel != null) {
            fragmentTransaction.hide(panelMessagePanel);
        }
        if (panelRentCar != null) {
            fragmentTransaction.hide(panelRentCar);
        }
        switch (page) {
            case 1:
                if (panelIndex == null) {
                    panelIndex = new PanelIndex();
                    fragmentTransaction.add(R.id.panelFragement, panelIndex);
                }
                fragmentTransaction.show(panelIndex);
                this.currentPage = 1;
                changFootText(1);
                break;
            case 2:
                if (panelShop == null) {
                    panelShop = new PanelShop();
                    fragmentTransaction.add(R.id.panelFragement, panelShop);
                }
                fragmentTransaction.show(panelShop);
                this.currentPage = 2;
                changFootText(2);
                break;
            case 3:
                if (panelMessagePanel == null) {
                    panelMessagePanel = new PanelMessage();
                    fragmentTransaction.add(R.id.panelFragement, panelMessagePanel);
                }
                fragmentTransaction.show(panelMessagePanel);
                this.currentPage = 3;
                changFootText(3);
                break;
            case 4:
                if (panelRentCar == null) {
                    panelRentCar = new PanelRentCar();
                    fragmentTransaction.add(R.id.panelFragement, panelRentCar);
                }
                fragmentTransaction.show(panelRentCar);
                this.currentPage = 4;
                changFootText(4);
                break;

            default:
                break;
        }
        fragmentTransaction.commit();

    }

    @SuppressLint("ResourceAsColor")
    private void changFootText(int i) {
        panelIndexText.setTextColor(0xff666666);
        panelShopText.setTextColor(0xff666666);
        panelMessageText.setTextColor(0xff666666);
        panelRentText.setTextColor(0xff666666);

        panelIndexImg.setImageResource(R.drawable.foot_001);
        panelShopImg.setImageResource(R.drawable.foot_002);
        panelMessageImg.setImageResource(R.drawable.foot_003);
        panelRentImg.setImageResource(R.drawable.foot_004);

        if (i == 1) {
            panelIndexText.setTextColor(0xff40b15d);
            panelIndexImg.setImageResource(R.drawable.foot_001_1);
        } else if (i == 2) {
            panelShopText.setTextColor(0xff40b15d);
            panelShopImg.setImageResource(R.drawable.foot_002_1);
        } else if (i == 3) {
            panelMessageText.setTextColor(0xff40b15d);
            panelMessageImg.setImageResource(R.drawable.foot_003_1);
        } else if (i == 4) {
            panelRentText.setTextColor(0xff40b15d);
            panelRentImg.setImageResource(R.drawable.foot_004_1);
        }
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
            SetupIndex.downloadSuccess.sendMessage(new Message());
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
        String apibus_businessid = preferences.getString("apibus_businessid", null);
        String apibus_username = preferences.getString("apibus_username", null);
        String path = PubFunction.www + "api_business.php/home/version";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "apibus_businessid=" + apibus_businessid + ";" + "apibus_username=" + apibus_username);

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
                System.out.println(e.toString());
            }
        }
    }

    public JSONObject getResult() {
        return this.jsonObject;
    }
}
