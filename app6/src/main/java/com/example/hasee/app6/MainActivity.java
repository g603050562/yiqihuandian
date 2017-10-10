package com.example.hasee.app6;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener{

    private TextView textView;
    private LinearLayout finish;
    public static Handler handler;

    private ListView listView;
    private SimpleAdapter simpleAdapter;

    private Activity activity;
    private DownLoadApk downLoadApk;
    private ProgressDialog progressDialog;

    private Handler successHandler,errorHandler;
    List<Map<String,String>> dataList = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;
        init();
        handler();
    }

    private void handler() {
        successHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = msg.getData().getString("data");
                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                    simpleAdapter = new SimpleAdapter(getApplicationContext(),getdata(jsonArray),R.layout.listview_item,new String[]{"text1"},new int[]{R.id.texxt_1});
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            String url = dataList.get(position).get("url");
                            downLoadApk = new DownLoadApk(activity,url,progressDialog);
                            downLoadApk.start();
                            progressDialog.show();
                            Toast.makeText(activity,"Downloading APP, please waiting....",Toast.LENGTH_LONG);
                        }
                    });
                    listView.setAdapter(simpleAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        };

        errorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = msg.getData().getString("msg");
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        };

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), "连接超时！请检查网络设置！", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PubFunction.isNetworkAvailable(this) == true){
            HttpGetInfo httpGetInfo = new HttpGetInfo(successHandler,errorHandler);
            httpGetInfo.start();
            progressDialog.show();
        }else{
            Toast.makeText(activity,"No Internet!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataList.clear();
    }

    private void init() {

        textView = (TextView) activity.findViewById(R.id.textView);
        textView.setOnClickListener(this);
        finish = (LinearLayout) activity.findViewById(R.id.finish);
        finish.setOnClickListener(this);
        progressDialog = new ProgressDialog(activity);

        listView = (ListView) activity.findViewById(R.id.lisview);
    }

    private List<Map<String,String>> getdata(JSONArray jsonArray) throws JSONException {
        for(int i = 0 ; i < jsonArray.length() ; i++){
            JSONObject jsonObject  = jsonArray.getJSONObject(i);
            String name_str = jsonObject.getString("name");
            String url_str = jsonObject.getString("url");
            Map<String,String> map = new HashMap<String, String>();
            map.put("text1",name_str);
            map.put("url",url_str);
            dataList.add(map);
        }
        return dataList;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == textView.getId()){

        }else{
            this.finish();
        }
    }


}

class DownLoadApk extends Thread{

    private Activity activity;
    private String httpUrl;
    private ProgressDialog progressDialog;

    public DownLoadApk(Activity activity, String httpUrl,ProgressDialog progressDialog){
        this.activity = activity;
        this.httpUrl = httpUrl;
        this.progressDialog = progressDialog;
    }

    @Override
    public void run() {
        super.run();
        downLoadFile(httpUrl);
    }

    protected File downLoadFile(String httpUrl) {
        // TODO Auto-generated method stub
        final String fileName = "updata.apk";
        File tmpFile = new File("/sdcard/update/");
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File("/sdcard/update/" + fileName);

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
                MainActivity.handler.sendMessage(new Message());
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
            progressDialog.dismiss();
            openFile(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            MainActivity.handler.sendMessage(new Message());
        }
        return file;
    }
    //打开APK程序代码

    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }
}

class HttpGetInfo extends Thread {

    private Handler success, error;


    public HttpGetInfo(Handler success, Handler error) {
        this.success = success;
        this.error = error;
    }

    @Override
    public void run() {
        super.run();
        String path = "http://www.huandianwang.com/index.php/version/app";
        HttpPost httpPost = new HttpPost(path);
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

        List<NameValuePair> list = new ArrayList<NameValuePair>();
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

                String code = jsonObject.getString("code");
                String messageString = jsonObject.getString("message");

                if (code.equals("0")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        sendMessage(success, messageString, data);
                    } else {
                        sendMessage(success, messageString);
                    }
                } else {
                    sendMessage(error, messageString);
                }
            } else {
                sendMessage(error, "error：HttpLogin");
            }
        } catch (Exception e) {
            sendMessage(error, "error：HttpLogin");
        }
    }

    private void sendMessage(Handler handler, String str) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void sendMessage(Handler handler, String str, String data) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        bundle.putString("data", data);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}