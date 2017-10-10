package com.example.fullenergy.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by apple on 2017/8/21.
 */

@EActivity(R.layout.green_userinfo)
public class GreenUserInfo extends FragmentActivity {

    private Activity activity;

    @ViewById
    LinearLayout page_return, l_1, l_2, l_3, l_4, l_5, l_6, l_7, l_8 ,l_9;
    @ViewById
    CircleImageView user_head;
    @ViewById
    TextView edit_info, code, count, name;

    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private Handler panelMineIndexTransactionPasswordSuccessHandler,panelMineIndexTransactionPasswordErrorHandler;
    private String head_img_url = "";

    @Override
    protected void onResume() {
        super.onResume();
        preferences = this.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        httpPanelMineIndex();
        progressDialog.show();
    }

    @AfterViews
    void afterviews() {
        init();
        main();
    }

    private void main() {
    }


    private void init() {

        activity = this;

        progressDialog = new ProgressDialog(activity);

        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0x00000000);
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void edit_info() {
        Intent intent = new Intent(activity,GreenUserInfoEdit_.class);
        intent.putExtra("url",head_img_url);
        activity.startActivity(intent);
    }

    @Click
    void l_1() {
        activity.startActivity(new Intent(activity,GreenUserInfoAddress_.class));
    }

    @Click
    void l_2() { activity.startActivity(new Intent(activity,GreenUserInfoCertified_.class));}

    @Click
    void l_3() { activity.startActivity(new Intent(activity,GreenUserInfoPswd_.class));}

    @Click
    void l_4() { activity.startActivity(new Intent(activity,GreenUserInfoAppo_.class));}

    @Click
    void l_5() { activity.startActivity(new Intent(activity,GreenUserInfoReward_.class));}

    @Click
    void l_6() { activity.startActivity(new Intent(activity,GreenUserInfoRecord_.class));}

    @Click
    void l_7() { activity.startActivity(new Intent(activity,GreenUserInfoMessage_.class));}

    @Click
    void l_8() {activity.startActivity(new Intent(activity,GreenUserInfoSet_.class));}

    @Click
    void l_9() {activity.startActivity(new Intent(activity,GreenUserInfoBandBar_.class));}

    @UiThread
    void returnHttpPanelMineIndex(String str, String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            String avater = jsonObject.getString("avater");
            if(avater.equals("")){
                avater = "statics/images/avater/1.jpg";
            }
            Picasso.with(activity).load(PubFunction.www + avater).into(user_head);
            head_img_url = avater;

            String nickname = jsonObject.getString("nickname");
            name.setText(nickname);

            String yqm = jsonObject.getString("yqm");
            if (yqm.equals("null")) {
                code.setText("无");
            } else {
                code.setText(yqm.toString());
            }

            String surplus = jsonObject.getString("surplus");
            count.setText("剩余 " + surplus.toString() + " 次");

            int trade_password = jsonObject.getInt("trade_password");
            if (trade_password == 0) {
                LayoutInflater inflater = LayoutInflater.from(activity);
//						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//						final AlertDialog mAlertDialog = builder.create();

                final Dialog mAlertDialog = new Dialog(activity);

                View view = inflater.inflate(R.layout.alertdialog_transaction_password, null);

                final EditText panelMineIndexAlertDialogPsw = (EditText) view
                        .findViewById(R.id.panelMineIndexAlertDialogPsw);
                final EditText panelMineIndexAlertDialogPswRe = (EditText) view
                        .findViewById(R.id.panelMineIndexAlertDialogPswRe);

                TextView submit = (TextView) view.findViewById(R.id.panelMineIndexAlertDialogSubmit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        String psw = "";
                        String pswRe = "";
                        psw = panelMineIndexAlertDialogPsw.getText().toString().trim();
                        pswRe = panelMineIndexAlertDialogPswRe.getText().toString().trim();
                        if (psw.equals("") || pswRe.equals("")) {
                            Toast.makeText(activity, "输入内容不能为空！", Toast.LENGTH_SHORT).show();
                        } else if (!psw.equals(pswRe)) {
                            Toast.makeText(activity, "两次密码输入不相同！", Toast.LENGTH_SHORT).show();
                            panelMineIndexAlertDialogPsw.setText("");
                            panelMineIndexAlertDialogPswRe.setText("");
                        } else {
                            if (PubFunction.isNetworkAvailable(activity)) {
                                httpPanelMineIndexTransactionPasswordSubmit(psw);
                            } else {
                                Toast.makeText(activity, "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
                mAlertDialog.setCancelable(true);
                mAlertDialog.show();
                mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                mAlertDialog.getWindow().setContentView(view);

                panelMineIndexTransactionPasswordSuccessHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        Toast.makeText(activity, "交易密码设置成功！", Toast.LENGTH_SHORT).show();
                        mAlertDialog.dismiss();
                    }
                };

                panelMineIndexTransactionPasswordErrorHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        String message = msg.getData().getString("message");
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        panelMineIndexAlertDialogPsw.setText("");
                        panelMineIndexAlertDialogPswRe.setText("");
                    }

                    ;
                };



            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
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
    void httpPanelMineIndex() {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/my_center";
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
                        returnHttpPanelMineIndex(messageStr, data);
                    } else {
                        returnSuccess(messageStr);
                    }
                } else if (code.equals("200")) {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        renturnError(messageStr);
                    }
                }else {
                    renturnError(messageStr);
                }
            } else {
                renturnError("服务器错误：httpPanelMineIndex");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpPanelMineIndex");
        }
    }


    //类内的handler ， 没有用注入式
    @Background
    void httpPanelMineIndexTransactionPasswordSubmit(String password) {
        String PHPSESSID = preferences.getString("PHPSESSID", null);
        String api_userid = preferences.getString("api_userid", null);
        String api_username = preferences.getString("api_username", null);
        String path = PubFunction.www + "api.php/member/set_jy_password";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("jy_password", password));
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

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", messageStr);
                bundle.putString("jy_password", password);
                message.setData(bundle);


                if (code.equals("100")) {
                    panelMineIndexTransactionPasswordSuccessHandler.sendMessage(new Message());
                } else if (code.equals("200")) {
                    if (messageStr.equals("秘钥不正确,请重新登录")) {
                        turnToLogin();
                    } else {
                        panelMineIndexTransactionPasswordErrorHandler.sendMessage(message);
                    }
                } else {
                    panelMineIndexTransactionPasswordErrorHandler.sendMessage(message);
                }
            } else {
                renturnError("服务器错误：httpPanelMineIndexTransactionPasswordSubmit");
            }
        } catch (Exception e) {
            renturnError("json解析错误：httpPanelMineIndexTransactionPasswordSubmit");
        }
    }
}
