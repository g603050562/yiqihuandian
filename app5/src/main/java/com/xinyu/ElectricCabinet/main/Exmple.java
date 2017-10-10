package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.Unit;


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


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;

/**
 * Created by hasee on 2017/2/28.
 */

public class Exmple extends FragmentActivity implements View.OnClickListener{

    private Activity activity;
    private TextView new_time,new_date;
    private ImageView imageView,wifi_image;
    private TextView b1_t2,b1_t3,b1_t4,b1_t5,b2_t2,b2_t3,b2_t4,b2_t5,b3_t2,b3_t3,b3_t4,b3_t5,b4_t2,b4_t3,b4_t4,b4_t5,b5_t2,b5_t3,b5_t4,b5_t5,b6_t2,b6_t3,b6_t4,b6_t5,b7_t2,b7_t3,b7_t4,b7_t5,b8_t2,b8_t3,b8_t4,b8_t5,b9_t2,b9_t3,b9_t4,b9_t5,b10_t2,b10_t3,b10_t4,b10_t5,b11_t2,b11_t3,b11_t4,b11_t5,b12_t2,b12_t3,b12_t4,b12_t5,b13_t2,b13_t3,b13_t4,b13_t5,b14_t2,b14_t3,b14_t4,b14_t5,b15_t2,b15_t3,b15_t4,b15_t5;
    private TextView cabinet_id;

    private EditText number;
    private TextView opendoor;
    private LinearLayout panel;

    private HttpUploadBatteryInfo httpUploadBatteryInfo;
    private HttpInputOldBar httpInputOldBar;
    private HttpOutputNewBar httpOutputNewBar;

    private String str[] = new String[]{"关闭","打开","损坏"};
    private String state[] = new String[]{"01","00","02"};

    private PL2303MultiLib mSerialMulti;
    private static final int DeviceIndex = 0;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";
    private UARTSettingInfo gUARTInfo = new UARTSettingInfo();
    private int iDeviceCount = 0; // 搜索的usb driver 数量
    private boolean gThreadStop = false;
    private boolean gRunningReadThread = false;

    private String cabinetID;
    private SharedPreferences sharedPreferences;
    private Handler getReturnTextHandler,sendMessageHandler,setDateHandler,httpUploadBatteryInfoSuccessHandler,httpErrorHandler,httpInputOldBarSuccessHandler,httpInputOldBarErrorHandler,OutputNewBarSuccessHandler,setWifiInfo;
    public static Handler openNullDoorHandler,adminOpenDoorHandler;
    private int battery = 15;//电池数量
    private int i = 0; // 电池数循环发送计数
    private int n = 0; // 放入的旧电池所在仓
    private int temp_n = 0; // 检测旧电池门锁是否打开参数
    private int temp_state_n = 0; // 放入的旧电池所在仓 — 门锁状态
    private int o = 0; // 需要更换的满电电池所在仓
    private int temp_o = 0; // 检测新电池门锁是否打开参数
    private int temp_state_o = 0; // 放入的新电池所在仓 — 门锁状态
    private int x = 0; // check_change_door的计数器
    private int y = 0; // remove_full_bar的计数器
    private JSONObject inputReturnJSON;

    private String order_send_str = ""; //发送的状态命令
    private String linshi_order_send = "";//临时添加的状态命令
    private int linshi_count = 0;

    private JSONArray send_jsonarray = new JSONArray();
    private int[] is_null_ba; // 所有电池的舱室状态 个数以battery而定  0：空仓 1：满电 2：充电 3：异常
    private int[] is_open_ba; // 所有电池的门锁状态 0：开门 1：锁上
    private int is_null_count = -1;
    private int last_null_door = -1;
    private String [] barID;

    private int IS_AREADY_RUN = 0;
    private int is_normal_action = 0;
    private int system_info = 0;

    private Thread thread_1 = new Thread(){ // 此线程为给电机发送命令   获得整体电池的数据后会上传电池的数据
        @Override
        public void run() { // 柜门发送信息 启动线程
            super.run();
            while (true) {
                try {
                    if (sendMessageHandler!=null){
                        sendMessageHandler.sendMessage(new Message());
                    }
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Thread thread_2 = new Thread(){ // 其他周边功能循环线程
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    //监听是否存在网络
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    if(Unit.isNetworkAvailable(activity) == false){
                        bundle.putString("wifi","false");
                    }else{
                        bundle.putString("wifi","true");
                    }
                    message.setData(bundle);
                    setWifiInfo.sendMessage(message);

                    //时间更新
                    setDateHandler.sendMessage(new Message());

                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 从usb接受流文件信息模块
     */

    private String out_str = "";
    private int ReadLen1 = 0;
    private byte[] ReadBuf1 = new byte[4096];
    private Thread ReadLoop1 = new Thread() {
        @Override
        public void run() {
            super.run();
            for (; ; ) {
                ReadLen1 = mSerialMulti.PL2303Read(DeviceIndex, ReadBuf1);
                if (ReadLen1 > 0) {
                    StringBuffer sbHex = new StringBuffer();
                    for (int j = 0; j < ReadLen1; j++) {
                        String hex = Integer.toHexString(ReadBuf1[j] & 0xFF);
                        if (hex.length() == 1) {
                            hex = '0' + hex;
                        }
                        sbHex.append(hex);
                    }
                    out_str = out_str + sbHex.toString();
                }else{
                    if(!out_str.equals("")){ //数据不正常 两个数据叠在一起了 截取依次发送
                        if(out_str.length() > 140){
                            String str = out_str.substring(0,6);
                            String str1 = "";
                            String str2 = "";
                            if(str.equals("3c21c5")){  //判断两个跌在一起的数据那个在前，那个在后
                                 str1 = out_str.substring(0,76);
                                 str2 = out_str.substring(76,out_str.length());
                            }else if(str.equals("3c21c6")){
                                str1 = out_str.substring(0,66);
                                str2 = out_str.substring(66,out_str.length());
                            }

                            Message message1 = new Message();
                            Bundle bundle1 = new Bundle();
                            bundle1.putString("msg", str1);
                            message1.setData(bundle1);
                            getReturnTextHandler.sendMessage(message1);

                            Message message2 = new Message();
                            Bundle bundle2 = new Bundle();
                            bundle2.putString("msg", str2);
                            message2.setData(bundle2);
                            getReturnTextHandler.sendMessage(message2);
                        }else{//发送接受的正常数据
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", out_str);
                            message.setData(bundle);
                            getReturnTextHandler.sendMessage(message);
                        }
                        out_str = "";
                    }
                }
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (gThreadStop) {
                    gRunningReadThread = false;
                    return;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_1);
        MyApplication.getInstance().addActivity(this);

        init();    //初始化相关
        main();    //数据版相关
        handler(); //内部通信相关
        order_send_str = make_message(1, battery, 0,0);

        thread_2.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!thread_1.isAlive()){
            init();    //初始化相关
            main();    //数据版相关
            handler(); //内部通信相关
            order_send_str = make_message(1, battery, 0,0);
        }
        if(!thread_2.isAlive()){
            thread_2.start();
        }
    }

    protected void onDestroy() {
        if(mSerialMulti!=null) {
            if(iDeviceCount>0) {
                unregisterReceiver(PLMultiLibReceiver);
                mSerialMulti.PL2303Release();
                mSerialMulti = null;
            }
        }
        super.onDestroy();
    }

    private void init() {
        activity = this;

        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE); //初始化简单数据库
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cabinetID = sharedPreferences.getString("cabinetNumber", "");
        if(cabinetID.equals("")){
            editor.putString("cabinetNumber", "77016040001"); //出场的默认id
            editor.commit();
        }else{
            cabinet_id = (TextView) this.findViewById(R.id.cabinet_id);
            cabinet_id.setText(cabinetID);
        }
        is_null_ba = new int[battery]; //初始化柜 电池 状态
        is_open_ba = new int[battery]; //初始化柜 门锁 状态
        barID = new String[battery]; //初始化电池id

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.userOperationPanel, new UserOperation());
        fragmentTransaction.commitAllowingStateLoss();

        new_time = (TextView) this.findViewById(R.id.new_time);
        new_date = (TextView) this.findViewById(R.id.new_date);
        imageView = (ImageView) this.findViewById(R.id.text1);
        imageView.setOnClickListener(this);
        wifi_image = (ImageView) this.findViewById(R.id.wifi_image);

        b1_t2 = (TextView) this.findViewById(R.id.b1_t2);
        b1_t3 = (TextView) this.findViewById(R.id.b1_t3);
        b1_t4 = (TextView) this.findViewById(R.id.b1_t4);
        b1_t5 = (TextView) this.findViewById(R.id.b1_t5);

        b2_t2 = (TextView) this.findViewById(R.id.b2_t2);
        b2_t3 = (TextView) this.findViewById(R.id.b2_t3);
        b2_t4 = (TextView) this.findViewById(R.id.b2_t4);
        b2_t5 = (TextView) this.findViewById(R.id.b2_t5);

        b3_t2 = (TextView) this.findViewById(R.id.b3_t2);
        b3_t3 = (TextView) this.findViewById(R.id.b3_t3);
        b3_t4 = (TextView) this.findViewById(R.id.b3_t4);
        b3_t5 = (TextView) this.findViewById(R.id.b3_t5);

        b4_t2 = (TextView) this.findViewById(R.id.b4_t2);
        b4_t3 = (TextView) this.findViewById(R.id.b4_t3);
        b4_t4 = (TextView) this.findViewById(R.id.b4_t4);
        b4_t5 = (TextView) this.findViewById(R.id.b4_t5);

        b5_t2 = (TextView) this.findViewById(R.id.b5_t2);
        b5_t3 = (TextView) this.findViewById(R.id.b5_t3);
        b5_t4 = (TextView) this.findViewById(R.id.b5_t4);
        b5_t5 = (TextView) this.findViewById(R.id.b5_t5);

        b6_t2 = (TextView) this.findViewById(R.id.b6_t2);
        b6_t3 = (TextView) this.findViewById(R.id.b6_t3);
        b6_t4 = (TextView) this.findViewById(R.id.b6_t4);
        b6_t5 = (TextView) this.findViewById(R.id.b6_t5);

        b7_t2 = (TextView) this.findViewById(R.id.b7_t2);
        b7_t3 = (TextView) this.findViewById(R.id.b7_t3);
        b7_t4 = (TextView) this.findViewById(R.id.b7_t4);
        b7_t5 = (TextView) this.findViewById(R.id.b7_t5);

        b8_t2 = (TextView) this.findViewById(R.id.b8_t2);
        b8_t3 = (TextView) this.findViewById(R.id.b8_t3);
        b8_t4 = (TextView) this.findViewById(R.id.b8_t4);
        b8_t5 = (TextView) this.findViewById(R.id.b8_t5);

        b9_t2 = (TextView) this.findViewById(R.id.b9_t2);
        b9_t3 = (TextView) this.findViewById(R.id.b9_t3);
        b9_t4 = (TextView) this.findViewById(R.id.b9_t4);
        b9_t5 = (TextView) this.findViewById(R.id.b9_t5);

        b10_t2 = (TextView) this.findViewById(R.id.b10_t2);
        b10_t3 = (TextView) this.findViewById(R.id.b10_t3);
        b10_t4 = (TextView) this.findViewById(R.id.b10_t4);
        b10_t5 = (TextView) this.findViewById(R.id.b10_t5);

        b13_t2 = (TextView) this.findViewById(R.id.b13_t2);
        b13_t3 = (TextView) this.findViewById(R.id.b13_t3);
        b13_t4 = (TextView) this.findViewById(R.id.b13_t4);
        b13_t5 = (TextView) this.findViewById(R.id.b13_t5);

        b11_t2 = (TextView) this.findViewById(R.id.b11_t2);
        b11_t3 = (TextView) this.findViewById(R.id.b11_t3);
        b11_t4 = (TextView) this.findViewById(R.id.b11_t4);
        b11_t5 = (TextView) this.findViewById(R.id.b11_t5);

        b12_t2 = (TextView) this.findViewById(R.id.b12_t2);
        b12_t3 = (TextView) this.findViewById(R.id.b12_t3);
        b12_t4 = (TextView) this.findViewById(R.id.b12_t4);
        b12_t5 = (TextView) this.findViewById(R.id.b12_t5);

        b14_t2 = (TextView) this.findViewById(R.id.b14_t2);
        b14_t3 = (TextView) this.findViewById(R.id.b14_t3);
        b14_t4 = (TextView) this.findViewById(R.id.b14_t4);
        b14_t5 = (TextView) this.findViewById(R.id.b14_t5);

        b15_t2 = (TextView) this.findViewById(R.id.b15_t2);
        b15_t3 = (TextView) this.findViewById(R.id.b15_t3);
        b15_t4 = (TextView) this.findViewById(R.id.b15_t4);
        b15_t5 = (TextView) this.findViewById(R.id.b15_t5);

        number = (EditText) this.findViewById(R.id.number);
        opendoor = (TextView) this.findViewById(R.id.opendoor);
        opendoor.setOnClickListener(this);

        panel = (LinearLayout) this.findViewById(R.id.panel);
        panel.setOnClickListener(this);
    }

    private void main() {

        mSerialMulti = new PL2303MultiLib((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);
        iDeviceCount = mSerialMulti.PL2303Enumerate();

        //获得usb设备的数量
        if (0 == iDeviceCount) {
            showTheToast("没有找到可以运行的设备！请插入设备后重启程序！");
            return;
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(mSerialMulti.PLUART_MESSAGE);
            registerReceiver(PLMultiLibReceiver, filter);
            showTheToast("找到 " + iDeviceCount + " 个可运行的设备！");
        }//if( 0==iDevCnt )

        //循环创建 mSerialMulti 直到mSerialMulti不为空
        if (mSerialMulti == null||!mSerialMulti.PL2303IsDeviceConnectedByIndex(0)) {
            return;
        }

        //设置接收器的参数 以及 开启usb接收
        gUARTInfo.mBaudrate = PL2303MultiLib.BaudRate.B9600;
        int res = 0;
        try {
            res = mSerialMulti.PL2303SetupCOMPort(0, gUARTInfo.mBaudrate, gUARTInfo.mDataBits, gUARTInfo.mStopBits, gUARTInfo.mParity, gUARTInfo.mFlowControl);
            OpenUARTDevice(DeviceIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res < 0) {
            return;
        }

        if(iDeviceCount!=0){
            if(IS_AREADY_RUN == 0){
                main();
                return;
            }
        }
    }

    private void handler() {
        getReturnTextHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = msg.getData().getString("msg");
                System.out.println(s.toString());
                List<String> list = new ArrayList<String>();
                for(int i = 0 ; i < s.length()/2 ; i ++){
                    list.add(s.substring(i * 2, i * 2 + 2));
                }
                if(list.size() == 33) {
                    int local = Integer.parseInt(list.get(5), 16);
                    int dianliang = Integer.parseInt(list.get(24), 16);
                    int wendu = Integer.parseInt(list.get(23), 16) - 40;
                    if(wendu == -40){
                        wendu = 0;
                    }

                    DecimalFormat decimalFormat = new DecimalFormat(".00");

                    float b = Integer.parseInt(list.get(14)+list.get(15),16) * (float) 0.1;
                    String q = decimalFormat.format(b);
                    float dianya = Float.parseFloat(q);

                    float a = Integer.parseInt(list.get(20)+list.get(21), 16) * (float) 0.1;
                    String p = decimalFormat.format(a);
                    float dianliu = Float.parseFloat(p);

                    String BID = list.get(6)+list.get(7)+list.get(8)+list.get(9)+list.get(10)+list.get(11)+list.get(12)+list.get(13);

                    if(local == 1){
                        b1_t2.setText("温度：" + wendu + "C");
                        b1_t3.setText("电压：" + dianya + "V");
                        b1_t4.setText("电流：" + dianliu + "A");
                        b1_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b1_t2,b1_t3,b1_t4,b1_t5);
                        }
                    } else if (local == 2) {
                        b2_t2.setText("温度：" + wendu + "C");
                        b2_t3.setText("电压：" + dianya + "V");
                        b2_t4.setText("电流：" + dianliu + "A");
                        b2_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b2_t2,b2_t3,b2_t4,b2_t5);
                        }
                    } else if (local == 3) {
                        b3_t2.setText("温度：" + wendu + "C");
                        b3_t3.setText("电压：" + dianya + "V");
                        b3_t4.setText("电流：" + dianliu + "A");
                        b3_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b3_t2,b3_t3,b3_t4,b3_t5);
                        }
                    } else if (local == 4) {
                        b4_t2.setText("温度：" + wendu + "C");
                        b4_t3.setText("电压：" + dianya + "V");
                        b4_t4.setText("电流：" + dianliu + "A");
                        b4_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b4_t2,b4_t3,b4_t4,b4_t5);
                        }
                    } else if (local == 5) {
                        b5_t2.setText("温度：" + wendu + "C");
                        b5_t3.setText("电压：" + dianya + "V");
                        b5_t4.setText("电流：" + dianliu + "A");
                        b5_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b5_t2,b5_t3,b5_t4,b5_t5);
                        }
                    } else if (local == 6) {
                        b6_t2.setText("温度：" + wendu + "C");
                        b6_t3.setText("电压：" + dianya + "V");
                        b6_t4.setText("电流：" + dianliu + "A");
                        b6_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b6_t2,b6_t3,b6_t4,b6_t5);
                        }
                    } else if (local == 7) {
                        b7_t2.setText("温度：" + wendu + "C");
                        b7_t3.setText("电压：" + dianya + "V");
                        b7_t4.setText("电流：" + dianliu + "A");
                        b7_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b7_t2,b7_t3,b7_t4,b7_t5);
                        }
                    } else if (local == 8) {
                        b8_t2.setText("温度：" + wendu + "C");
                        b8_t3.setText("电压：" + dianya + "V");
                        b8_t4.setText("电流：" + dianliu + "A");
                        b8_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b8_t2,b8_t3,b8_t4,b8_t5);
                        }
                    } else if (local == 9) {
                        b9_t2.setText("温度：" + wendu + "C");
                        b9_t3.setText("电压：" + dianya + "V");
                        b9_t4.setText("电流：" + dianliu + "A");
                        b9_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b9_t2,b9_t3,b9_t4,b9_t5);
                        }
                    }  else if (local == 10) {
                        b10_t2.setText("温度：" + wendu + "C");
                        b10_t3.setText("电压：" + dianya + "V");
                        b10_t4.setText("电流：" + dianliu + "A");
                        b10_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b10_t2,b10_t3,b10_t4,b10_t5);
                        }
                    }  else if (local == 11) {
                        b11_t2.setText("温度：" + wendu + "C");
                        b11_t3.setText("电压：" + dianya + "V");
                        b11_t4.setText("电流：" + dianliu + "A");
                        b11_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b11_t2,b11_t3,b11_t4,b11_t5);
                        }
                    } else if (local == 12) {
                        b12_t2.setText("温度：" + wendu + "C");
                        b12_t3.setText("电压：" + dianya + "V");
                        b12_t4.setText("电流：" + dianliu + "A");
                        b12_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b12_t2,b12_t3,b12_t4,b12_t5);
                        }
                    } else if (local == 13) {
                        b13_t2.setText("温度：" + wendu + "C");
                        b13_t3.setText("电压：" + dianya + "V");
                        b13_t4.setText("电流：" + dianliu + "A");
                        b13_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b13_t2,b13_t3,b13_t4,b13_t5);
                        }
                    } else if (local == 14) {
                        b14_t2.setText("温度：" + wendu + "C");
                        b14_t3.setText("电压：" + dianya + "V");
                        b14_t4.setText("电流：" + dianliu + "A");
                        b14_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b14_t2,b14_t3,b14_t4,b14_t5);
                        }
                    } else if (local == 15) {
                        b15_t2.setText("温度：" + wendu + "C");
                        b15_t3.setText("电压：" + dianya + "V");
                        b15_t4.setText("电流：" + dianliu + "A");
                        b15_t5.setText(dianliang + "%");
                        if(wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0){
                            setNullCabInfo(b15_t2,b15_t3,b15_t4,b15_t5);
                        }
                    }

                    barID[local-1] = BID; // 设置每个电池的ID
                    Unit.barId = barID;//环境变量的电池id 方便后台拿去数据

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("battery",BID);
                        jsonObject.put("door",local);
                        jsonObject.put("board", "0" + Integer.toString(local));
                        if(dianliang == 100){
                            jsonObject.put("is_full","1");
                        }else{
                            jsonObject.put("is_full","0");
                        }
                        if(dianliang == 0 && dianya== 0 && wendu == 0){
                            jsonObject.put("flag","0");
                        }else{
                            jsonObject.put("flag","1");
                        }
                        jsonObject.put("category","A");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    send_jsonarray.put(jsonObject);
                }else{
                    if(list.size() == (8 + battery*2)){
                        List<String> list2 = new ArrayList<String>();
                        for(int i = 0 ; i < s.length() / 4 ; i ++){
                            list2.add(s.substring(i * 4, i * 4 + 4));
                        }
                        set_door_state(list2);
                        Unit.barSta = is_null_ba;
                        System.out.println(list2);
                    }
                    if(send_jsonarray.length() == battery){
                        System.out.println(send_jsonarray.toString());
                        httpUploadBatteryInfo = new HttpUploadBatteryInfo(activity,cabinetID,send_jsonarray,httpErrorHandler,httpUploadBatteryInfoSuccessHandler);
                        httpUploadBatteryInfo.start();
                        send_jsonarray = null;
                        send_jsonarray = new JSONArray();
                    }
                }


            }
        };

        sendMessageHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) { // j：柜门配置要发送的最大次数  m：当前发送的是第几条  i： 电池循环发送
                super.handleMessage(msg);

                if(linshi_order_send.equals("")) {
                    if (order_send_str != "") {
                        send_message(order_send_str);
                        order_send_str = "";

//                        if(is_null_count == 0 && is_normal_action == 0){  // 打开最后一个空仓门
//                            linshi_order_send = make_message(1, battery, 0, last_null_door); // 打开一个充满电的舱门
//                            showTheToast("操作错误：取出电池，关上舱门后，重新换电！");
//                        }

                        if (temp_n != 0) {
                            null_door_is_open();
                        }

                        if (n != 0) { // 有要更换的舱门打开的时候 执行检查舱内是否有电池且已经关上了门
                            check_change_door();
                        }

                        if (temp_o != 0) {
                            new_door_is_open();
                        }

                        if (o != 0) {
                            remove_full_bar();
                        }
                    } else {
                        i = i + 1;
                        send_message(2, battery, i);
                        if (i == battery) {
                            i = 0;
                        }
                        order_send_str = make_message(1, battery, 0, 0);
                    }
                }else{
                    System.out.println(linshi_order_send);
                    if(linshi_count > 2){
                        linshi_order_send = "";
                        linshi_count = 0;
                    }else{
                        send_message(linshi_order_send);
                        linshi_count = linshi_count + 1;
                    }
                }
            }
        };

        adminOpenDoorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                clearM();
                String str = msg.getData().getString("item");
                if(str.equals("0")){
                    linshi_order_send = openAllDoor();
                }else{
                    int doorid_1 = Integer.parseInt(str);
                    linshi_order_send = make_message(1, battery, 0, doorid_1);
                    clearM();
                }
            }
        };

        setDateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                SimpleDateFormat formatter    =   new    SimpleDateFormat    (" HH : mm");
                Date    curDate    =   new Date(System.currentTimeMillis());//获取当前时间
                String    str    =    formatter.format(curDate);
                new_time.setText(str);
                new_date.setText(Unit.StringData());
            }
        };

        httpUploadBatteryInfoSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) { //上传信息成功的handler，不需要输出任何信息
                super.handleMessage(msg);
                String str = msg.getData().getString("message");
            }
        };


        httpErrorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = msg.getData().getString("message");
                System.out.println(str.toString());
            }
        };

        openNullDoorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                open_null_door();
                is_normal_action = 1;
            }
        };

        setWifiInfo = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.getData().getString("wifi").equals("true")){
                    wifi_image.setImageResource(R.drawable.wifi_2);
                }else{
                    wifi_image.setImageResource(R.drawable.wifi_0);
                }
            }
        };
    }

    private void setNullCabInfo (TextView t2,TextView t3,TextView t4,TextView t5){
        t2.setText("温度：- -");
        t3.setText("电压：- -");
        t4.setText("电流：- -");
        t5.setText("空");
    }

    private void set_door_state( List<String> list){ //  获取电池仓门锁的状态 1：关门 0：开门
        for (int i = 0 ; i < battery ; i++){
            String str = list.get(i + 3);
            String str_1 =  str.substring(0,1);
            if(str.equals("0c00")){
                is_open_ba[i] = 2;
            }else{
                if(str_1.equals("a")||str_1.equals("c")||str_1.equals("e")||str_1.equals("2")||str_1.equals("4")||str_1.equals("6")||str_1.equals("8")||str_1.equals("0")){
                    is_open_ba[i] = 0;
                }else if(str_1.equals("b")||str_1.equals("d")||str_1.equals("f")||str_1.equals("1")||str_1.equals("3")||str_1.equals("5")||str_1.equals("7")||str_1.equals("9")){
                    is_open_ba[i] = 1;
                }
            }
            int door = i + 1;
            String state = str;
            String state_3 = state.substring(2,3);
            if(state_3.equals("8")||state_3.equals("9")||state_3.equals("a")||state_3.equals("b")||state_3.equals("c")||state_3.equals("d")||state_3.equals("e")||state_3.equals("f")){
                is_null_ba[door-1] = 1; // 满电
            }else{
                if(state.equals("4202")){
                    is_null_ba[door-1] = 1; // 满电   电池组高压状态
                }else if(state.equals("4000")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("5000")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("5202")){
                    is_null_ba[door-1] = 1; // 满电
                }else if(state.equals("5282")){
                    is_null_ba[door-1] = 1; // 满电
                }else if(state.equals("5281")){
                    is_null_ba[door-1] = 1; // 满电
                }else if(state.equals("0000")){
                    is_null_ba[door-1] = 0; // 空仓
                }else if(state.equals("1000")){
                    is_null_ba[door-1] = 0; // 空仓
                }else if(state.equals("4001")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("5001")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("c000")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("d000")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("d001")){
                    is_null_ba[door-1] = 2; // 充电
                }else if(state.equals("c000")){
                    is_null_ba[door-1] = 3; // 没有电机
                }else{
                    is_null_ba[door-1] = 3; // 异常
                }
            }

            int count = 0;
            for(int m = 0 ; m < is_null_ba.length ; m++){
                int n = is_null_ba[m];
                if(n == 0){
                    count = count + 1;
                }
            }
            is_null_count = count;
            if(is_null_count == 1){
                for(int m = 0 ; m < is_null_ba.length ; m++){
                    int n = is_null_ba[m];
                    if(n == 0){
                        last_null_door = m+1;
                    }
                }
            }
        }
    }

    private int get_full_power_count(){ // 获得充满电的电池总数
        int count = 0;
        for (int i = 0 ; i < is_null_ba.length ; i++){
            if(is_null_ba[i] == 1){
                count = count + 1;
            }
        }
        return count;
    }

    private int get_a_full_power_bar(){ // 获得一个充满电的电池 返回0的话则为没有
        int j = 0;
        for (int i = 0 ; i < is_null_ba.length ; i++){
            if(is_null_ba[i] == 1){
                j = i+1;
                break;
            }
        }
        return j;
    }

    private void open_null_door(){ // 打开一个空仓门（判断必须空仓大于2，小于2提示没有可更换电池）
        int is_have_open_door = 0;

        for(int i = 0 ; i < is_open_ba.length ; i++){
            if(is_open_ba[i] == 0 ){
                is_have_open_door = 1;
                break;
            }
        }
        if(is_have_open_door == 1){
            showTheToast("请先手动确认柜门是否全部关上！谢谢配合！");
        } else if(get_full_power_count() < 1){
            showTheToast("此电柜没有可更换的满电电池，请您耐心等候！");
        }else {
            showTheToast("请插入电池");
            int null_count = 0;
            for (int i = 0; i < is_null_ba.length; i++) {
                int state = is_null_ba[i];
                if (state == 0) {
                    null_count = null_count + 1;
                }
            }
            if (null_count > 0) {
                for (int i = 0; i < is_null_ba.length; i++) {
                    int state = is_null_ba[i];
                    if (state == 0) {
                        temp_state_n =  is_open_ba[i];
                        linshi_order_send = make_message(1, battery, 0, i + 1);
                        temp_n = i + 1 ;
                        break;
                    }
                }
            } else {
                showTheToast("柜内可更换电池仓不足！请联系店家！");
            }
        }
    }

    private void null_door_is_open(){ //检测空仓们是否打开
        int state = is_open_ba[temp_n-1];
        if(state != temp_state_n){
            n = temp_n;
            temp_state_n = 0;
            temp_n = 0;
        }
    }

    private int send_HttpInputOldBar_count = 0;
    private void check_change_door(){  // 检测是否有电池放入电柜 且 是否关闭舱门
        System.out.println("现在打开的舱门为："+ n);
        if(is_null_ba[n-1] != 0 && is_open_ba[n-1] == 1){
            if(get_full_power_count() > 0){
                final int full_bar = get_a_full_power_bar();
                if(full_bar == 0){
                    showTheToast("此电柜没有可更换的满电电池，请您耐心等候！");
                }else{
                    if(system_info == 0){
                        showTheToast("系统识别中......请等待！");
                        system_info = 1;
                    }
                    if(!barID[n-1].equals("000000000000")) {
                        httpInputOldBarSuccessHandler = new Handler() {  //打开一个充满电的舱门
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                inputReturnJSON = httpInputOldBar.getJSONArray();
                                n = 0;
                                temp_state_o = is_open_ba[full_bar - 1];
                                linshi_order_send = make_message(1, battery, 0, full_bar); // 打开一个充满电的舱门
                                System.out.println("打开一个充满电的舱门："+ full_bar);
                                temp_o = full_bar;
                                send_HttpInputOldBar_count = 0;
                            }
                        };
                        httpInputOldBarErrorHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                String str = msg.getData().getString("message");
                                System.out.println(str);
                                if (str.equals("电池编号有误")) {
                                    linshi_order_send = make_message(1, battery, 0, n);
                                    clearM();
                                    showTheToast("该电池并未录入系统！请取走该电池！");
                                }else{
                                    linshi_order_send = make_message(1, battery, 0, n);
                                    clearM();
                                    showTheToast(str);
                                }
                                send_HttpInputOldBar_count = 0;
                            }
                        };
                        if(send_HttpInputOldBar_count == 0){
                            httpInputOldBar = new HttpInputOldBar(this, barID[n - 1], cabinetID, httpInputOldBarErrorHandler, httpInputOldBarSuccessHandler);
                            httpInputOldBar.start();
                            send_HttpInputOldBar_count = send_HttpInputOldBar_count + 1;
                        }
                        x = 0; //计数器归零
                    }
                }
            }else{
                showTheToast("此电柜没有可更换的满电电池，请您耐心等候！！");
            }
            x = 0; //计数器归零
        }else if(is_null_ba[n-1] == 0 && is_open_ba[n-1] == 1){ //空仓 关门 了 ，接着打开这个柜门
            System.out.println(n + "：关门空仓");
            x = x + 1;  // 计数器 执行7次空仓关门 发送开门命令
            if (x > 15 ) {
                showTheToast("换电结束！感谢您的使用！");
                clearM();
                x = 0; //计数器归零
            }
        }
    }

    private void new_door_is_open() { //检测新仓们是否打开
        int state = is_open_ba[temp_o - 1];
        if (state != temp_state_o) {
            o = temp_o;
            temp_state_o = 0;
            temp_o = 0;
        }
    }

    private void remove_full_bar(){  //检测是否拿出满电的电池   1：关门  0：开门
        if(o > 0){
            if(is_null_ba[o-1] != 0 && is_open_ba[o-1] == 1){  //如果里面还有电池，并门   继续打开这个舱门
                System.out.println("没有拿出满电电池！");
                y = y + 1;  // 计数器 执行5次空仓关门 发送开门命令
                if ( y > 10 ) {
                    linshi_order_send = make_message(1, battery, 0,o);
                    y = 0;
                }
            }else if(is_null_ba[o-1] != 0 && is_open_ba[o-1] == 0){
                System.out.println("没有拿出满电电池！");
            }else if(is_null_ba[o-1] == 0 && is_open_ba[o-1] == 1){
                OutputNewBarSuccessHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        showTheToast("更换电池成功");
                    }
                };
                try {
                    String member = barID[o];
                    String old_member = inputReturnJSON.getString("old_member");
                    String inesrt_id = inputReturnJSON.getString("inesrt_id");
                    httpOutputNewBar = new HttpOutputNewBar(activity,member,old_member,cabinetID,inesrt_id,httpErrorHandler,OutputNewBarSuccessHandler);
                    httpOutputNewBar.start();
                    linshi_order_send = make_message(2, battery, o, 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                clearM();
                System.out.println("更换电池成功！！！！！！");
                is_normal_action = 0;
                system_info = 0;
            }
        }
    }

    private void clearM(){
        i = 0; // 电池数循环发送计数
        n = 0; // 放入的旧电池所在仓
        temp_n = 0;
        temp_state_n = 0;
        o = 0; // 需要更换的满电电池所在仓
        temp_o = 0 ;
        temp_state_o = 0;
        x = 0; // check_change_door的计数器
        y = 0; // remove_full_bar的计数器
        inputReturnJSON = null;
    }


    @Override
    public void onClick(View v) {
        if(opendoor.getId() == v.getId()){
            String str = number.getText().toString();
            int doorid = Integer.parseInt(str);
            order_send_str = make_message(1, battery, 0, doorid);
        }else if(panel.getId() == v.getId()){
            panel.setClickable(false);
            panel.setVisibility(View.GONE);
            UserOperation.getFocusHandler.sendMessage(new Message());
        }
    }

    //返回信息接受器
    private final BroadcastReceiver PLMultiLibReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mSerialMulti.PLUART_MESSAGE)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String str = (String) extras.get(mSerialMulti.PLUART_DETACHED);
                    int index = Integer.valueOf(str);
                }
            }
        }//onReceive
    };

    /**
     * 打开按钮 打开uart服务
     */
    private void OpenUARTDevice(int index) {
        if (mSerialMulti == null) {
            showTheToast("mSerialMulti为空");
            return;
        }
        if (!mSerialMulti.PL2303IsDeviceConnectedByIndex(index)) {
            showTheToast("mSerialMulti和设备没有链接");
            return;
        }
        boolean res;
        res = mSerialMulti.PL2303OpenDevByUARTSetting(index, gUARTInfo.mBaudrate, gUARTInfo.mDataBits, gUARTInfo.mStopBits, gUARTInfo.mParity, gUARTInfo.mFlowControl);
        if (!res) {
            showTheToast("Can't set UART correctly!");
            return;
        }
        if (!gRunningReadThread) {
            UpdateDisplayView(index);
        }
        showTheToast("成功打开设备：[" + mSerialMulti.PL2303getDevicePathByIndex(index) + "]");

        thread_1.start();
        IS_AREADY_RUN = 1;


        return;
    }//private void OpenUARTDevice(int index)

    private void UpdateDisplayView(int index) {
        gThreadStop = false;
        gRunningReadThread = true;
        new Thread(ReadLoop1).start();
    }

    private void send_message(int z,int count,int tCount){ //z：模式  ... count:总电池块数  ... tCount:目标电池 ...
        String str = make_message(z,count,tCount,0);
        //写入命令
        if (mSerialMulti == null)
            return;
        if (!mSerialMulti.PL2303IsDeviceConnectedByIndex(0))
            return;
        byte[] bt = Unit.hexStringToBytes(str);
        byte[] Sendbytes = Arrays.copyOf(bt, bt.length);
        int res = mSerialMulti.PL2303Write(0, Sendbytes);   // sendbtyes 为输入的命令
        if (res < 0) {
            return;
        }
    }
    private void send_message(String string){
        //写入命令
        if (mSerialMulti == null)
            return;
        if (!mSerialMulti.PL2303IsDeviceConnectedByIndex(0))
            return;
        byte[] bt = Unit.hexStringToBytes(string);
        byte[] Sendbytes = Arrays.copyOf(bt, bt.length);
        int res = mSerialMulti.PL2303Write(0, Sendbytes);   // sendbtyes 为输入的命令
        if (res < 0) {
            return;
        }
    }

    private String openAllDoor(){ // 打开所有门
        String renturn_str = "";
        List<String> sendList = new ArrayList<String>();
//      编辑信息行
        sendList.add("3C");
//      从机地址
        sendList.add("21");
//      通信指令
        sendList.add("05");
//      版本协议
        sendList.add("01");
//      帧长度
        sendList.add(new Unit().get_count(battery));
//      数据内容
        sendList.add(new Unit().get_car_count(battery));

        for(int i = 0 ; i < battery ; i++){
            sendList.add(state[1]);

        }

//      校验位
        int j = battery + 8 - 3; // 异或的位数
        int[] by = new int[j];
        for(int i = 1 ; i < sendList.size() ;i++ ){
            int a =  Integer.parseInt(sendList.get(i),16);
            by[i-1] = a;
        }
        int b = new Unit().get_crc(by);
        String s_b =  Integer.toHexString(b);
        String S_B =  s_b.toUpperCase();
        if(S_B.length() == 1){
            S_B = "0"+ S_B;
        }
        sendList.add(S_B);
//      帧结束标志
        sendList.add("3E");
        //合成写入的信息行
        String S = "";
        for(int i = 0 ; i < sendList.size();i++){
            S = S + sendList.get(i);
        }
        renturn_str = S;
        //信息行处理
        sendList.clear();
        return renturn_str;
    }

    private String make_message(int z,int count,int tCount,int door){ // tCount模式1没用
        String renturn_str = "";
        List<String> sendList = new ArrayList<String>();
//      编辑信息行
        sendList.add("3C");
//      从机地址
        sendList.add("21");
//      通信指令
        if(z == 1){
            sendList.add("05");
        }else if(z == 2){
            sendList.add("06");
        }
//      版本协议
        sendList.add("01");
//      帧长度
        sendList.add(new Unit().get_count(count));
//      数据内容
        if(z == 1){
            sendList.add(new Unit().get_car_count(count));
        }else if(z == 2){
            sendList.add(new Unit().get_car_count(tCount));
        }

        if(door != 0 && z == 1){
            for(int i = 0 ; i < count ; i++){
                if(i == door - 1){
                    sendList.add(state[1]);
                }else{
                    sendList.add(state[0]);
                }
            }
        }else{
            for(int i = 0 ; i < count ; i++){
                sendList.add(state[0]);

            }
        }

//      校验位
        int j = battery + 8 - 3; // 异或的位数
        int[] by = new int[j];
        for(int i = 1 ; i < sendList.size() ;i++ ){
            int a =  Integer.parseInt(sendList.get(i),16);
            by[i-1] = a;
        }
        int b = new Unit().get_crc(by);
        String s_b =  Integer.toHexString(b);
        String S_B =  s_b.toUpperCase();
        if(S_B.length() == 1){
            S_B = "0"+ S_B;
        }
        sendList.add(S_B);
//      帧结束标志
        sendList.add("3E");
        //合成写入的信息行
        String S = "";
        for(int i = 0 ; i < sendList.size();i++){
            S = S + sendList.get(i);
        }
        renturn_str = S;
        //信息行处理
        sendList.clear();
        return renturn_str;
    }

    private void showTheToast(String string){
        Toast toast = Toast.makeText(this,string,Toast.LENGTH_LONG);
        View view = LayoutInflater.from(this).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

class HttpUploadBatteryInfo extends Thread {

    private Activity activity;
    private String cabinetid;
    private JSONArray jsonArrayn;
    private Handler errorHandler,successHanler;

    public HttpUploadBatteryInfo(Activity activity,String cabinetid,JSONArray jsonArrayn,Handler errorHandler,Handler successHanler) {
        this.activity = activity;
        this.cabinetid = cabinetid;
        this.jsonArrayn = jsonArrayn;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String path = "http://www.huandianwang.com/index.php/yz/check";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("cabinet", cabinetid ));
        list.add(new BasicNameValuePair("json_string", jsonArrayn.toString() + ""));
        String str = Unit.getMd5(cabinetid+"!@23*#&(@912oOo388*@#(fslKK");
        list.add(new BasicNameValuePair("token", str));

        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        successHanler.sendMessage(message);
                    }
                } else{
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "HttpUploadBatteryInfo:返回错误信息！");
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                }
            }else{
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpUploadBatteryInfo:服务器错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpUploadBatteryInfo:json解析错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        }
    }
}

//目前没有用的下载电池信息
//class HttpDownloadBatteryInfo extends Thread {
//
//    private Activity activity;
//    private String cabinetid;
//    private Handler errorHandler,successHanler;
//    private JSONArray jsonArray;
//
//    public HttpDownloadBatteryInfo(Activity activity,String cabinetid,Handler errorHandler,Handler successHanler) {
//        this.activity = activity;
//        this.cabinetid = cabinetid;
//        this.errorHandler = errorHandler;
//        this.successHanler = successHanler;
//    }
//
//    @Override
//    public void run() {
//        // TODO Auto-generated method stub
//        super.run();
//
//        String path = "http://www.huandianwang.com/index.php/yz/message";
//        HttpPost httpPost = new HttpPost(path);
//        List<NameValuePair> list = new ArrayList<NameValuePair>();
//        list.add(new BasicNameValuePair("cabinet", cabinetid + ""));
//        String str = Unit.getMd5(cabinetid+"!@23*#&(@912oOo388*@#(fslKK");
//        list.add(new BasicNameValuePair("token", str));
//
//        try {
//            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
//            httpPost.setEntity(entity);
//            HttpClient client = new DefaultHttpClient();
//            HttpResponse httpResponse = client.execute(httpPost);
//            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String result = EntityUtils.toString(httpResponse.getEntity());
//
//                JSONTokener jsonTokener = new JSONTokener(result);
//                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//
//                String messageStr = jsonObject.getString("message");
//                String code = jsonObject.getString("code");
//                if (code.equals("200")) {
//                    Message message = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("message", messageStr);
//                    message.setData(bundle);
//                    if(errorHandler != null){
//                        errorHandler.sendMessage(message);
//                    }
//                } else if (code.equals("100")) {
//                    Message message = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("message", messageStr);
//                    message.setData(bundle);
//                    if(errorHandler != null){
//                        jsonArray = jsonObject.getJSONArray("data");
//                        successHanler.sendMessage(message);
//                    }
//                } else{
//                    Message message = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("message", "HttpDownloadBatteryInfo:返回错误信息！");
//                    message.setData(bundle);
//                    if(errorHandler != null){
//                        errorHandler.sendMessage(message);
//                    }
//                }
//            }else{
//                Message message = new Message();
//                Bundle bundle = new Bundle();
//                bundle.putString("message", "HttpDownloadBatteryInfo:服务器错误！");
//                message.setData(bundle);
//                if(errorHandler != null){
//                    errorHandler.sendMessage(message);
//                }
//            }
//        } catch (Exception e) {
//            if (activity != null) {
//                Message message = new Message();
//                Bundle bundle = new Bundle();
//                bundle.putString("message", "HttpDownloadBatteryInfo:json解析错误！");
//                message.setData(bundle);
//                if(errorHandler != null){
//                    errorHandler.sendMessage(message);
//                }
//            }
//        }
//    }
//
//    public JSONArray getData(){
//        return jsonArray;
//    }
//}


class HttpInputOldBar extends Thread {

    private Activity activity;
    private String cabinetid;
    private Handler errorHandler,successHanler;
    private String member;
    private JSONObject jsonArray;
    private SharedPreferences sharedPreferences;

    public HttpInputOldBar(Activity activity,String member,String cabinetid,Handler errorHandler,Handler successHanler) {
        this.activity = activity;
        this.member = member;
        this.cabinetid = cabinetid;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
        sharedPreferences = activity.getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String PHPSESSID = sharedPreferences.getString("PHPSESSID", null);
        String api_userid = sharedPreferences.getString("web_userid", null);
        String api_username = sharedPreferences.getString("web_username", null);

        String path = "http://www.huandianwang.com/index.php/charg/input_verify";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "web_userid=" + api_userid + ";" + "web_username=" + api_username);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("member", member + ""));
        list.add(new BasicNameValuePair("cabinetid", cabinetid + ""));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,10000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,10000);
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                System.out.println(result.toString());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        jsonArray = jsonObject.getJSONObject("data");
                        successHanler.sendMessage(message);
                    }
                } else{
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "HttpInputOldBar:返回错误信息！");
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                }
            }else{
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpInputOldBar:服务器错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpInputOldBar:网络错误，请联系厂家！！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        }
    }

    public JSONObject getJSONArray(){
        return jsonArray;
    }

}


class HttpOutputNewBar extends Thread {

    private Activity activity;
    private String cabinetid;
    private Handler errorHandler,successHanler;
    private String member,old_member,insert_id;
    private JSONObject jsonArray;
    private SharedPreferences sharedPreferences;

    public HttpOutputNewBar(Activity activity,String member,String old_member,String cabinetid,String insert_id,Handler errorHandler,Handler successHanler) {
        this.activity = activity;
        this.member = member;
        this.old_member = old_member;
        this.cabinetid = cabinetid;
        this.insert_id = insert_id;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
        sharedPreferences = activity.getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String PHPSESSID = sharedPreferences.getString("PHPSESSID", null);
        String api_userid = sharedPreferences.getString("web_userid", null);
        String api_username = sharedPreferences.getString("web_username", null);

        String path = "http://www.huandianwang.com/index.php/charg/news_verify";
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "web_userid=" + api_userid + ";" + "web_username=" + api_username);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("member", member + ""));
        list.add(new BasicNameValuePair("old_member", old_member + ""));
        list.add(new BasicNameValuePair("insert_id", insert_id + ""));
        list.add(new BasicNameValuePair("cabinetid", cabinetid + ""));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject.toString());

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("100")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if(errorHandler != null){
                        jsonArray = jsonObject.getJSONObject("data");
                        successHanler.sendMessage(message);
                    }
                } else{
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "HttpOutputNewBar:返回错误信息！");
                    message.setData(bundle);
                    if(errorHandler != null){
                        errorHandler.sendMessage(message);
                    }
                }
            }else{
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpOutputNewBar:服务器错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpOutputNewBar:json解析错误！");
                message.setData(bundle);
                if(errorHandler != null){
                    errorHandler.sendMessage(message);
                }
            }
        }
    }

    public JSONObject getJSONArray(){
        return jsonArray;
    }

}
