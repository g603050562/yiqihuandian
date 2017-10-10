package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by apple on 2017/9/12.
 */


public class MyService extends Service {

    private Context context;
    private String TAG = "MyService";
    private String updata_url = "http://www.huandianwang.com/APP/2.0test.apk";
    private Thread date_thread;

    private int is_stop_count = 0;
    private int is_stop_max_count = 5;

    private int is_updata_count = 0;
    private int is_updata_max_count = 20;

    private SharedPreferences sharedPreferences;

    private Handler success,error;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");

        execRootCmdSilent("pm install -r " + "/sdcard/" +"a.apk");

        context = this;
        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);

        date_thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    while (true) {
                        String is_thread_protection = sharedPreferences.getString("thread_protection_type", "1");

                        if (is_thread_protection.equals("1")) {

                            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
                            String date_str = df.format(new Date());

                            if (isRunningForeground(context) == false) {
                                is_stop_count = is_stop_count + 1;
                                if (is_stop_count == is_stop_max_count) {
                                    doStartApplicationWithPackageName("com.example.hasee.app8");
                                    doStartApplicationWithPackageName("com.example.hasee.app9");
                                } else if (is_stop_count > is_stop_max_count + 5) {
                                    is_stop_count = 0;
                                }
                            } else {
                                is_stop_count = 0;
                            }

                            System.out.println("现在时间：" + date_str + "  是否在前台运行： " + isRunningForeground(context));

                        }

                        String cabinetID = sharedPreferences.getString("cabinetNumber", "");

                        if(is_updata_count < is_updata_max_count){
                            is_updata_count = is_updata_count + 1;
                        }else{
                            is_updata_count = 0;
                            HttpGetUpdataInfo httpGetUpdataInfo = new HttpGetUpdataInfo(cabinetID , success ,error);
                            httpGetUpdataInfo.start();
                        }
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        date_thread.start();

        success = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String data = msg.getData().getString("data");
                double version = MyApplication.cab_version;

                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                    double duwu_version = jsonObject.getDouble("version");
                    if(duwu_version > version){
                        DownLoadApk downLoadApk = new DownLoadApk(context,updata_url);
                      downLoadApk.start();
                    }else{
                        System.out.println("不需要更新");
                    }

                }catch (Exception e){
                    System.out.println("更新错误！");
                }


            }
        };

        error = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                System.out.println("不允许更新！");
            }
        };
    }

    //判断app是否在前台运行
    private boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(getPackageName())) {
            return true;
        }

        return false;
    }

    //通过包名打开主程序
    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cn);
            startActivity(intent);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    public int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i("upload_system", cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}


class HttpGetUpdataInfo extends Thread {


    private String catinetNumberString;
    private Handler errorHandler, successHanler;


    public HttpGetUpdataInfo(String catinetNumberString, Handler successHanler, Handler errorHandler) {
        this.catinetNumberString = catinetNumberString;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        String path = "http://www.huandianwang.com/index.php/version/cabinet";
        HttpPost httpPost = new HttpPost(path);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
                String data = jsonObject.getString("data");

                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    bundle.putString("data", data);
                    message.setData(bundle);
                    if (errorHandler != null) {
                        successHanler.sendMessage(message);
                    }
                } else {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "返回错误信息！");
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                }
            } else {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "服务器错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("message", "网络解析失败！！");
            message.setData(bundle);
            if (errorHandler != null) {
                errorHandler.sendMessage(message);
            }
        }
    }
}

class DownLoadApk extends Thread {

    private Context activity;
    private String httpUrl;

    public DownLoadApk(Context activity, String httpUrl) {
        this.activity = activity;
        this.httpUrl = httpUrl;
    }

    @Override
    public void run() {
        super.run();
        downLoadFile(httpUrl);
    }

    protected File downLoadFile(String httpUrl) {
        // TODO Auto-generated method stub

        final String fileName = "updata.apk";
        File tmpFile = new File("/sdcard/");
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File("/sdcard/" + fileName);

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[256];
            conn.connect();
            double count = 0;
            if (conn.getResponseCode() >= 400) {
                Toast.makeText(activity, "Linking timeout....", Toast.LENGTH_SHORT).show();
            } else {
                while (count <= 100) {
                    if (is != null) {
                        int numRead = is.read(buf);
                        if (numRead <= 0) {
                            break;
                        } else {
                            fos.write(buf, 0, numRead);
                        }

                    } else {
                        break;
                    }

                }
            }

            conn.disconnect();
            fos.close();
            is.close();
//            openFile(file);
            execRootCmdSilent("pm install -r " + "/sdcard/" + fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }
    //打开APK程序代码

    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }

    public int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i("upload_system", cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
