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
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;

/**
 * Created by hasee on 2017/2/28.
 */

public class Control extends FragmentActivity implements View.OnClickListener {

    private Activity activity;
    private TextView new_time, new_date;
    private ImageView imageView, wifi_image;
    private TextView b1_t2, b1_t3, b1_t4, b1_t5, b2_t2, b2_t3, b2_t4, b2_t5, b3_t2, b3_t3, b3_t4, b3_t5, b4_t2, b4_t3, b4_t4, b4_t5, b5_t2, b5_t3, b5_t4, b5_t5, b6_t2, b6_t3, b6_t4, b6_t5, b7_t2, b7_t3, b7_t4, b7_t5, b8_t2, b8_t3, b8_t4, b8_t5, b9_t2, b9_t3, b9_t4, b9_t5, b10_t2, b10_t3, b10_t4, b10_t5, b11_t2, b11_t3, b11_t4, b11_t5, b12_t2, b12_t3, b12_t4, b12_t5;
    private List<Map<String, TextView>> textViewList = new ArrayList<>();
    private TextView cabinet_id;

    private LinearLayout panel;

    private HttpUploadBatteryInfo httpUploadBatteryInfo;
    private HttpInputOldBar httpInputOldBar;
    private HttpOutputNewBar httpOutputNewBar;

    private String str[] = new String[]{"关闭", "打开", "异常"};
    private String state[] = new String[]{"00", "4B", "02"};
    private String bat_door_state[] = new String[]{"00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00"};
    private JSONArray errorList = new JSONArray();

    private PL2303MultiLib mSerialMulti;
    private static final int DeviceIndex = 0;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";
    private UARTSettingInfo gUARTInfo = new UARTSettingInfo();
    private int iDeviceCount = 0; // 搜索的usb driver 数量
    private boolean gThreadStop = false;
    private boolean gRunningReadThread = false;

    private String cabinetID;
    private SharedPreferences sharedPreferences;
    private Handler getReturnTextHandler, sendMessageHandler, setDateHandler, httpUploadBatteryInfoSuccessHandler, httpErrorHandler, setWifiInfo, httpInputOldBarSuccessHandler, httpInputOldBarErrorHandler, OutputNewBarSuccessHandler;
    public static Handler openNullDoorHandler, adminOpenDoorHandler, adminCloseDoorHandler;
    private int battery = 12;//电池数量

    private int five_or_six = 0; //发送的状态命令

    private JSONObject inputReturnJSON;
    private JSONArray send_jsonarray = new JSONArray();
    private int[] is_null_ba; // 所有电池的舱室状态 个数以battery而定  0：空仓 1：满电 2：充电 3：找不到充电机 4：充电机故障
    private int[] is_open_ba; // 所有电池的门锁状态 0：开门 1：锁上 3：门锁故障
    private String[] bar_per;
    private String[] barID; //电池编号的数组

    private int i = 0; // 轮询到地几块电池

    private int IS_AREADY_RUN = 0;

    private int thread_1_code = 0, thread_2_code = 0;


    private int is_null_count = -1;
    private int last_null_door = -1;
    private int meiyou_kong_diangui = 0;
    private int mubiao_men_id = -1;
    private int dakai_namdian_men_id = -1;
    private int x = 0; //是否插入电池 计数器


    private Thread thread_1 = new Thread() { // 此线程为给电机发送命令   获得整体电池的数据后会上传电池的数据
        @Override
        public void run() { // 柜门发送信息 启动线程
            super.run();
            while (thread_1_code == 0) {
                try {
                    if (sendMessageHandler != null) {
                        sendMessageHandler.sendMessage(new Message());
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Thread thread_2 = new Thread() { // 其他周边功能循环线程
        @Override
        public void run() {
            super.run();
            while (thread_2_code == 0) {
                try {
                    //监听是否存在网络
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    if (Unit.isNetworkAvailable(activity) == false) {
                        bundle.putString("wifi", "false");
                    } else {
                        bundle.putString("wifi", "true");
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
    private byte[] ReadBuf1 = new byte[500];

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
                } else {
                    ReadBuf1 = new byte[500];
                    if (!out_str.equals("")) {
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", out_str);
                        message.setData(bundle);
                        if (getReturnTextHandler != null) {
                            getReturnTextHandler.sendMessage(message);
                        }
                    }
                    out_str = "";
                }
                try {
                    Thread.sleep(40);
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

        thread_2.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!thread_1.isAlive()) {
            init();    //初始化相关
            main();    //数据版相关
            handler(); //内部通信相关
        }
        if (!thread_2.isAlive()) {
            thread_2.start();
        }
    }

    protected void onDestroy() {
        if (mSerialMulti != null) {
            if (iDeviceCount > 0) {
                unregisterReceiver(PLMultiLibReceiver);
                mSerialMulti.PL2303Release();
                mSerialMulti = null;
            }
        }

        thread_1_code = 1;
        thread_2_code = 1;
        super.onDestroy();
    }

    private void init() {
        activity = this;

        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE); //初始化简单数据库
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cabinetID = sharedPreferences.getString("cabinetNumber", "");
        if (cabinetID.equals("")) {
            editor.putString("cabinetNumber", "77016040001"); //出场的默认id
            editor.commit();
        } else {
            cabinet_id = (TextView) this.findViewById(R.id.cabinet_id);
            cabinet_id.setText(cabinetID);
        }
        is_null_ba = new int[battery]; //初始化柜 电池 状态
        is_open_ba = new int[battery]; //初始化柜 门锁 状态
        barID = new String[]{"0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000", "0000000000000000"}; //初始化电池id
        bar_per = new String[]{"0","0","0","0","0","0","0","0","0","0","0","0"};
        for (int i = 0; i < 12; i++) {
            JSONObject jsonObject = new JSONObject();
            errorList.put(jsonObject);
        }

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
        Map<String, TextView> map_1 = new HashMap<>();
        map_1.put("2", b1_t2);
        map_1.put("3", b1_t3);
        map_1.put("4", b1_t4);
        map_1.put("5", b1_t5);
        textViewList.add(map_1);

        b2_t2 = (TextView) this.findViewById(R.id.b2_t2);
        b2_t3 = (TextView) this.findViewById(R.id.b2_t3);
        b2_t4 = (TextView) this.findViewById(R.id.b2_t4);
        b2_t5 = (TextView) this.findViewById(R.id.b2_t5);
        Map<String, TextView> map_2 = new HashMap<>();
        map_2.put("2", b2_t2);
        map_2.put("3", b2_t3);
        map_2.put("4", b2_t4);
        map_2.put("5", b2_t5);
        textViewList.add(map_2);

        b3_t2 = (TextView) this.findViewById(R.id.b3_t2);
        b3_t3 = (TextView) this.findViewById(R.id.b3_t3);
        b3_t4 = (TextView) this.findViewById(R.id.b3_t4);
        b3_t5 = (TextView) this.findViewById(R.id.b3_t5);
        Map<String, TextView> map_3 = new HashMap<>();
        map_3.put("2", b3_t2);
        map_3.put("3", b3_t3);
        map_3.put("4", b3_t4);
        map_3.put("5", b3_t5);
        textViewList.add(map_3);

        b4_t2 = (TextView) this.findViewById(R.id.b4_t2);
        b4_t3 = (TextView) this.findViewById(R.id.b4_t3);
        b4_t4 = (TextView) this.findViewById(R.id.b4_t4);
        b4_t5 = (TextView) this.findViewById(R.id.b4_t5);
        Map<String, TextView> map_4 = new HashMap<>();
        map_4.put("2", b4_t2);
        map_4.put("3", b4_t3);
        map_4.put("4", b4_t4);
        map_4.put("5", b4_t5);
        textViewList.add(map_4);

        b5_t2 = (TextView) this.findViewById(R.id.b5_t2);
        b5_t3 = (TextView) this.findViewById(R.id.b5_t3);
        b5_t4 = (TextView) this.findViewById(R.id.b5_t4);
        b5_t5 = (TextView) this.findViewById(R.id.b5_t5);
        Map<String, TextView> map_5 = new HashMap<>();
        map_5.put("2", b5_t2);
        map_5.put("3", b5_t3);
        map_5.put("4", b5_t4);
        map_5.put("5", b5_t5);
        textViewList.add(map_5);

        b6_t2 = (TextView) this.findViewById(R.id.b6_t2);
        b6_t3 = (TextView) this.findViewById(R.id.b6_t3);
        b6_t4 = (TextView) this.findViewById(R.id.b6_t4);
        b6_t5 = (TextView) this.findViewById(R.id.b6_t5);
        Map<String, TextView> map_6 = new HashMap<>();
        map_6.put("2", b6_t2);
        map_6.put("3", b6_t3);
        map_6.put("4", b6_t4);
        map_6.put("5", b6_t5);
        textViewList.add(map_6);

        b7_t2 = (TextView) this.findViewById(R.id.b7_t2);
        b7_t3 = (TextView) this.findViewById(R.id.b7_t3);
        b7_t4 = (TextView) this.findViewById(R.id.b7_t4);
        b7_t5 = (TextView) this.findViewById(R.id.b7_t5);
        Map<String, TextView> map_7 = new HashMap<>();
        map_7.put("2", b7_t2);
        map_7.put("3", b7_t3);
        map_7.put("4", b7_t4);
        map_7.put("5", b7_t5);
        textViewList.add(map_7);

        b8_t2 = (TextView) this.findViewById(R.id.b8_t2);
        b8_t3 = (TextView) this.findViewById(R.id.b8_t3);
        b8_t4 = (TextView) this.findViewById(R.id.b8_t4);
        b8_t5 = (TextView) this.findViewById(R.id.b8_t5);
        Map<String, TextView> map_8 = new HashMap<>();
        map_8.put("2", b8_t2);
        map_8.put("3", b8_t3);
        map_8.put("4", b8_t4);
        map_8.put("5", b8_t5);
        textViewList.add(map_8);

        b9_t2 = (TextView) this.findViewById(R.id.b9_t2);
        b9_t3 = (TextView) this.findViewById(R.id.b9_t3);
        b9_t4 = (TextView) this.findViewById(R.id.b9_t4);
        b9_t5 = (TextView) this.findViewById(R.id.b9_t5);
        Map<String, TextView> map_9 = new HashMap<>();
        map_9.put("2", b9_t2);
        map_9.put("3", b9_t3);
        map_9.put("4", b9_t4);
        map_9.put("5", b9_t5);
        textViewList.add(map_9);

        b10_t2 = (TextView) this.findViewById(R.id.b10_t2);
        b10_t3 = (TextView) this.findViewById(R.id.b10_t3);
        b10_t4 = (TextView) this.findViewById(R.id.b10_t4);
        b10_t5 = (TextView) this.findViewById(R.id.b10_t5);
        Map<String, TextView> map_10 = new HashMap<>();
        map_10.put("2", b10_t2);
        map_10.put("3", b10_t3);
        map_10.put("4", b10_t4);
        map_10.put("5", b10_t5);
        textViewList.add(map_10);

        b11_t2 = (TextView) this.findViewById(R.id.b11_t2);
        b11_t3 = (TextView) this.findViewById(R.id.b11_t3);
        b11_t4 = (TextView) this.findViewById(R.id.b11_t4);
        b11_t5 = (TextView) this.findViewById(R.id.b11_t5);
        Map<String, TextView> map_11 = new HashMap<>();
        map_11.put("2", b11_t2);
        map_11.put("3", b11_t3);
        map_11.put("4", b11_t4);
        map_11.put("5", b11_t5);
        textViewList.add(map_11);

        b12_t2 = (TextView) this.findViewById(R.id.b12_t2);
        b12_t3 = (TextView) this.findViewById(R.id.b12_t3);
        b12_t4 = (TextView) this.findViewById(R.id.b12_t4);
        b12_t5 = (TextView) this.findViewById(R.id.b12_t5);
        Map<String, TextView> map_12 = new HashMap<>();
        map_12.put("2", b12_t2);
        map_12.put("3", b12_t3);
        map_12.put("4", b12_t4);
        map_12.put("5", b12_t5);
        textViewList.add(map_12);

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
        if (mSerialMulti == null || !mSerialMulti.PL2303IsDeviceConnectedByIndex(0)) {
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

        if (iDeviceCount != 0) {
            if (IS_AREADY_RUN == 0) {
                main();
                return;
            }
        }
    }

    private void handler() {
        getReturnTextHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String s = msg.getData().getString("msg");
                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < s.length() / 2; i++) {
                        list.add(s.substring(i * 2, i * 2 + 2));
                    }

                    if (list.size() < 50) {
                        System.out.println("数据返回信息缺失错误！");
                        return;
                    }

//                    System.out.println(s.toString());

                    if (list.get(2).equals("c6")) { //电池数据
                        //柜号

                        System.out.println(s.toString());

                        String local_str = list.get(5);
                        String local_a = local_str.substring(1, 2);
                        int local = Integer.parseInt(local_a, 16);

                        if (local > 0) {
                            //电池编号
                            String BID = list.get(6) + list.get(7) + list.get(8) + list.get(9) + list.get(10) + list.get(11) + list.get(12) + list.get(13);
                            barID[local - 1] = BID; // 设置每个电池的ID
                            Unit.barId = barID;//环境变量的电池id 方便后台拿去数据

                            if (BID.equals("0000000000000000")) {
                                for (int j = 0; j < textViewList.size(); j++) {
                                    if (j == local - 1) {
                                        TextView t_2 = textViewList.get(j).get("2");
                                        TextView t_3 = textViewList.get(j).get("3");
                                        TextView t_4 = textViewList.get(j).get("4");
                                        TextView t_5 = textViewList.get(j).get("5");
                                        setNullCabInfo(t_2, t_3, t_4, t_5);
                                    }
                                }

                                int dianliang = 0;
                                int dianya = 0;
                                int wendu = 0;

                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("battery", BID);
                                    jsonObject.put("door", local);
                                    jsonObject.put("board", "0" + Integer.toString(local));
                                    if (dianliang == 100) {
                                        jsonObject.put("is_full", "1");
                                    } else {
                                        jsonObject.put("is_full", "0");
                                    }
                                    if (dianliang == 0 && dianya == 0 && wendu == 0) {
                                        jsonObject.put("flag", "0");
                                    } else {
                                        jsonObject.put("flag", "1");
                                    }
                                    jsonObject.put("category", "A");

                                    //二次更改上传的参数
                                    jsonObject.put("cabinet",cabinet_id);
                                    jsonObject.put("voltage","0");
                                    jsonObject.put("electricity","0");
                                    jsonObject.put("temperature","0");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                send_jsonarray.put(jsonObject);
                            } else {
                                //内电池电压
                                int SMALL_B_COUNT = Integer.parseInt(list.get(14), 16);//小电池路数
                                int[] SMALL_B_STR = new int[SMALL_B_COUNT];
                                for (int i = 0; i < SMALL_B_STR.length; i++) {
                                    SMALL_B_STR[i] = Integer.parseInt(list.get(15 + i * 2) + list.get(16 + i * 2), 16); // 具体数值
                                }
                                //电流状态
                                int DIANLIU_STATE = Integer.parseInt(list.get(15 + SMALL_B_COUNT * 2), 16);
                                //实时电流
                                int DIANLIU_SHISHI = Integer.parseInt(list.get(16 + SMALL_B_COUNT * 2) + list.get(17 + SMALL_B_COUNT * 2), 16);
                                //温度路数
                                int WENDU_LUSHU = Integer.parseInt(list.get(18 + SMALL_B_COUNT * 2), 16);
                                int[] WENDU_LUSHU_STR = new int[WENDU_LUSHU];
                                for (int i = 0; i < WENDU_LUSHU_STR.length; i++) {
                                    WENDU_LUSHU_STR[i] = Integer.parseInt(list.get(19 + SMALL_B_COUNT * 2 + i), 16); //具体数值
                                }
                                //电压状态
                                String DIANYA_STATRE = list.get(19 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(20 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //电流状态
                                String DIANLIU_STATRE = list.get(21 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(22 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //温度状态
                                String WENDU_STATRE = list.get(23 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(24 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //故障状态
                                String GUZHANG_STATRE = list.get(25 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(26 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //FET状态
                                String FET_STATRE = list.get(27 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //过充状态
                                String GUOCHONG_STATRE = list.get(28 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(29 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //过放状态
                                String GUOFANG_STATRE = list.get(30 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(31 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //高压状态
                                String GAOYA_STATRE = list.get(32 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(33 + SMALL_B_COUNT * 2 + WENDU_LUSHU);

                                //低压状态
                                String DAYA_STATRE = list.get(34 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(35 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //均衡状态
                                String JUNHENG_STATRE = list.get(36 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(37 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //放电次数
                                String FANGDIAN_STATRE = list.get(38 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(39 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //充电次数
                                String CHONGDIAN_STATRE = list.get(40 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(41 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //电池剩余百分比
                                String SHENGYU_BAIFENBI = list.get(42 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int dianchi_shengyu = Integer.parseInt(SHENGYU_BAIFENBI, 16);
                                //当前容量
                                String DANGQIANRONGLIANG = list.get(43 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(44 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //当前容量
                                String MANCHONGRONGLIANG = list.get(45 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(46 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //绝对剩余容量百分比
                                String JUEDUISHENGYU_BAIFENBI = list.get(47 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int jueduidianchi_shengyu = Integer.parseInt(JUEDUISHENGYU_BAIFENBI, 16);
                                //已用容量
                                String YIRONGRONGLIANG = list.get(48 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(49 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //设计容量
                                String SHEJIRONGLIANG = list.get(50 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(51 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                //健康状态
                                String JIANKANGZHUANGTAI = list.get(52 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int jiankangzhuangtai = Integer.parseInt(JIANKANGZHUANGTAI, 16);
                                //充满时间
                                String CHONGMANSHIJIAN = list.get(53 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(54 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int chongmanshijian = Integer.parseInt(CHONGMANSHIJIAN, 16);
                                //充满电压
                                String CHONGMANDIANYA = list.get(55 + SMALL_B_COUNT * 2 + WENDU_LUSHU) + list.get(56 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int chongmandianya = Integer.parseInt(CHONGMANDIANYA, 16);
                                //输出状态
                                String SHUCHUZHUANGTAI = list.get(57 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int shuchuzhuangtai = Integer.parseInt(SHUCHUZHUANGTAI, 16);
                                //充电机温度
                                String CHONGDIANJIWENDU = list.get(58 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int chongdianjiwendu = Integer.parseInt(CHONGDIANJIWENDU, 16);
                                //实际功率
                                String SHIJIGONGLV = list.get(59 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int shijigonglv = Integer.parseInt(SHIJIGONGLV, 16);
                                //最大功率
                                String ZUIDAGONGLV = list.get(60 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                int zuidagonglv = Integer.parseInt(ZUIDAGONGLV, 16);
                                //舱门状态
                                String CANGMENZHUANGTAI = list.get(61 + SMALL_B_COUNT * 2 + WENDU_LUSHU);
                                if (CANGMENZHUANGTAI.equals("01")) {
                                    is_open_ba[local - 1] = 1;
                                } else if (CANGMENZHUANGTAI.equals("02")) {
                                    is_open_ba[local - 1] = 0;
                                } else if (CANGMENZHUANGTAI.equals("03")) {
                                    is_open_ba[local - 1] = 2;
                                }
                                //温度
                                int wendu = 0;
                                for (int i = 0; i < WENDU_LUSHU_STR.length; i++) {
                                    wendu = wendu + WENDU_LUSHU_STR[i];
                                }
                                wendu = wendu / WENDU_LUSHU_STR.length;
                                wendu = wendu - 40;
                                if (wendu == -40) {
                                    wendu = 0;
                                }
                                //电压
                                int dianya = 0;
                                for (int i = 0; i < SMALL_B_STR.length; i++) {
                                    dianya = dianya + SMALL_B_STR[i];
                                }
                                DecimalFormat decimalFormat = new DecimalFormat(".00");
                                float b = dianya * (float) 0.001;
                                String q = decimalFormat.format(b);
                                //电流
                                int dianliu = DIANLIU_SHISHI;
                                float c = dianliu * (float)0.01;
                                String p = decimalFormat.format(c);
                                //电量
                                int dianliang = dianchi_shengyu;

                                for (int i = 0; i < textViewList.size(); i++) {
                                    if (i == local - 1) {
                                        TextView t_2 = textViewList.get(i).get("2");
                                        t_2.setText("温度：" + wendu + "C");
                                        TextView t_3 = textViewList.get(i).get("3");
                                        t_3.setText("电压：" + q + "V");
                                        TextView t_4 = textViewList.get(i).get("4");
                                        t_4.setText("电流：" + p + "A");
                                        TextView t_5 = textViewList.get(i).get("5");

                                        if(is_null_ba[i] == 1){
                                            t_5.setText( "100%");
                                        }else{
                                            t_5.setText(dianliang + "%");
                                        }
                                    }
                                }

                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("battery", BID);
                                    jsonObject.put("door", local);
                                    jsonObject.put("board", "0" + Integer.toString(local));
                                    if (dianliang >= 95) {
                                        jsonObject.put("is_full", "1");
                                    } else {
                                        jsonObject.put("is_full", "0");
                                    }
                                    if (dianliang == 0 && dianya == 0 && wendu == 0) {
                                        jsonObject.put("flag", "0");
                                    } else {
                                        jsonObject.put("flag", "1");
                                    }
                                    jsonObject.put("category", "A");

                                    //二次更改上传的参数
                                    jsonObject.put("cabinet",cabinet_id);
                                    jsonObject.put("voltage",q);
                                    jsonObject.put("electricity",p);
                                    jsonObject.put("temperature",wendu);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                send_jsonarray.put(jsonObject);
                            }
                        }else{

                        }
                    } else if (list.get(2).equals("c5")) { //状态数据

                        String head_info = list.get(0) + list.get(1) + list.get(2) + list.get(3) + list.get(4);
                        int DIANCHI_COUNT = Integer.parseInt(list.get(5), 16);
                        List<Map<String, String>> listMap = new ArrayList<>();

                        for (int i = 0; i < DIANCHI_COUNT; i++) {
                            Map<String, String> map = new HashMap<>();
                            map.put("is_ok", list.get(6 + i * 9));
                            map.put("asoc", list.get(7 + i * 9));
                            map.put("bms_state", list.get(8 + i * 9));
                            map.put("cab_state", list.get(9 + i * 9));
                            map.put("door_state", list.get(10 + i * 9));
                            map.put("bms_wendu", list.get(11 + i * 9));
                            map.put("huanjing_wendu", list.get(12 + i * 9));
                            map.put("chongdian_shijian", list.get(13 + i * 9) + list.get(14 + i * 9));
                            listMap.add(map);
                        }

                        set_door_state(listMap);
                        listMap.clear();

                        Unit.barSta = is_null_ba;

                        if (send_jsonarray.length() == battery) {
                            httpUploadBatteryInfo = new HttpUploadBatteryInfo(activity, cabinetID, send_jsonarray, errorList, httpErrorHandler, httpUploadBatteryInfoSuccessHandler);
                            httpUploadBatteryInfo.start();
                            System.out.println(send_jsonarray.toString());
                            send_jsonarray = null;
                            send_jsonarray = new JSONArray();
                        } else if(send_jsonarray.length() > battery){
                            send_jsonarray = null;
                            send_jsonarray = new JSONArray();
                        }

                    } else { //其他 就是错误
                        System.out.println("返回信息缺失 ：数据返回错误！" + s);
                    }
                } catch (Exception e) {
                    System.out.println("数组超出 ： 数据返回错误");
                }
            }
        };


        sendMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (mubiao_men_id != -1) {
                    huoqu_charu_dianchi();
                } else if (dakai_namdian_men_id != -1) {
                    huoqu_nachu_dianchi();
                } else {
                    if (five_or_six == 0) { // 05数据 发送
                        five_or_six = 1;
                        send_message_05(battery);

                        //此模块 为 假设电柜被塞满电池 打开最后的舱门 把电池取走
                        if (is_null_count == 0) {  // 打开最后一个空仓门
                            meiyou_kong_diangui = 1;
                            bat_door_state[11] = "4B";
                            showTheToast("操作错误：此电柜已经没有空电柜,请取出电池，关上舱门后，重新换电！");
                        } else {
                            if (meiyou_kong_diangui == 1) {
                                bat_door_state[11] = "00";
                                meiyou_kong_diangui = -0;
                            }
                        }
                    } else {                      // 06数据 发送
                        i = i + 1;
                        five_or_six = 0;
                        send_message_06(battery, i);
                        if (i == battery) {
                            i = 0;
                        }
                    }
                }
            }
        };

        adminOpenDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                clearM();
                String str = msg.getData().getString("item");
                int local_str = Integer.parseInt(str);
                for (int i = 0; i < bat_door_state.length; i++) {
                    if (i == local_str) {
                        bat_door_state[i] = "4B";
                    }
                }
            }
        };

        adminCloseDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                clearM();
                String str = msg.getData().getString("item");
                int local_str = Integer.parseInt(str);
                for (int i = 0; i < bat_door_state.length; i++) {
                    if (i == local_str) {
                        bat_door_state[i] = "00";
                    }

                }
            }
        };

        setDateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SimpleDateFormat formatter = new SimpleDateFormat(" HH : mm");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate);
                new_time.setText(str);
                new_date.setText(Unit.StringData());
            }
        };

        httpUploadBatteryInfoSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { //上传信息成功的handler，不需要输出任何信息
                super.handleMessage(msg);
                String str = msg.getData().getString("message");
            }
        };

        httpErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = msg.getData().getString("message");
                System.out.println(str.toString());
            }
        };

        openNullDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                open_null_door();
            }
        };

        setWifiInfo = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.getData().getString("wifi").equals("true")) {
                    wifi_image.setImageResource(R.drawable.wifi_2);
                } else {
                    wifi_image.setImageResource(R.drawable.wifi_0);
                }
            }
        };
    }

    private void setNullCabInfo(TextView t2, TextView t3, TextView t4, TextView t5) {
        t2.setText("温度：- -");
        t3.setText("电压：- -");
        t4.setText("电流：- -");
        t5.setText("空");
    }


    private void open_null_door() { // 打开一个空仓门（判断必须空仓大于2，小于2提示没有可更换电池）
        int is_have_open_door = 0;

        for (int i = 0; i < is_open_ba.length; i++) {
            if (is_open_ba[i] == 0) {
                is_have_open_door = 1;
                break;
            }
        }

        if (is_have_open_door == 1) {
            showTheToast("请先手动确认柜门是否全部关上！谢谢配合！");
        } else if (get_full_power_count() < 1) {
            showTheToast("此电柜没有可更换的满电电池，请您耐心等候！");
        } else {
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
                        bat_door_state[i] = "4B";
                        mubiao_men_id = i;
                        break;
                    }
                }
            } else {
                showTheToast("柜内可更换电池仓不足！请联系店家！");
            }
        }
    }

    private int get_full_power_count() { // 获得充满电的电池总数
        int count = 0;
        for (int i = 0; i < is_null_ba.length; i++) {
            if (is_null_ba[i] == 1) {
                count = count + 1;
            }
        }
        return count;
    }

    private int send_HttpInputOldBar_count = 0;

    private void huoqu_charu_dianchi() { // 获取插入的电池数据
        send_message_06(battery, mubiao_men_id + 1);
        String mubiao_dianchi_id = barID[mubiao_men_id];

        if (mubiao_dianchi_id.equals("0000000000000000")) {
            System.out.println("等待电池插入");
            x = x + 1;  // 计数器 执行7次空仓关门 发送开门命令
            if (x > 40) {
                showTheToast("换电结束！感谢您的使用！");
                bat_door_state[mubiao_men_id] = "00";
                clearM();
            }
        } else {
            bat_door_state[mubiao_men_id] = "00";
            int a = mubiao_men_id;

            if (is_open_ba[a] == 1) {
                showTheToast("正在验证电池，请稍后！");
                System.out.println("正在验证电池，请稍后！");
                clearM();
                final int full_bar = get_a_full_power_bar();

                httpInputOldBarSuccessHandler = new Handler() {  //打开一个充满电的舱门
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        inputReturnJSON = httpInputOldBar.getJSONArray();
                        bat_door_state[full_bar - 1] = "4B";
                        dakai_namdian_men_id = full_bar - 1;
                        System.out.println("打开一个充满电的舱门：" + full_bar);
                        send_HttpInputOldBar_count = 0;    //此参数 为 只能发送一次请求
                    }
                };
                httpInputOldBarErrorHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        String str = msg.getData().getString("message");
                        showTheToast(str);
                        send_HttpInputOldBar_count = 0;     //此参数 为 只能发送一次请求
                    }
                };
                if (send_HttpInputOldBar_count == 0) {
                    httpInputOldBar = new HttpInputOldBar(this, mubiao_dianchi_id, cabinetID, httpInputOldBarErrorHandler, httpInputOldBarSuccessHandler);
                    httpInputOldBar.start();
                    send_HttpInputOldBar_count = send_HttpInputOldBar_count + 1;  //此参数 为 只能发送一次请求
                }
            }
        }
    }

    private int get_a_full_power_bar() { // 获得一个充满电的电池 返回0的话则为没有
        int j = 0;
        for (int i = 0; i < is_null_ba.length; i++) {
            if (is_null_ba[i] == 1) {
                j = i + 1;
                break;
            }
        }
        return j;
    }

    private void huoqu_nachu_dianchi() {
        send_message_06(battery, dakai_namdian_men_id + 1);

        String mubiao_dianchi_id = barID[dakai_namdian_men_id];
        if (mubiao_dianchi_id.equals("0000000000000000")) {
            OutputNewBarSuccessHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    showTheToast("更换电池成功,感谢您的使用！");
                }
            };
            try {
                String member = barID[dakai_namdian_men_id];
                String old_member = inputReturnJSON.getString("old_member");
                String inesrt_id = inputReturnJSON.getString("inesrt_id");
                httpOutputNewBar = new HttpOutputNewBar(activity, member, old_member, cabinetID, inesrt_id, httpErrorHandler, OutputNewBarSuccessHandler);
                httpOutputNewBar.start();
                bat_door_state[dakai_namdian_men_id] = "00";

            } catch (JSONException e) {
                e.printStackTrace();
            }
            clearM();

        } else {
            System.out.println("等待电池拿走");
            x = x + 1;  // 计数器 执行7次空仓关门 发送开门命令
            if (x > 60) {
                showTheToast("换电结束！感谢您的使用！");
                bat_door_state[dakai_namdian_men_id] = "00";
                clearM();
            }
        }
    }


    private void set_door_state(List<Map<String, String>> list) { //  获取电池仓门锁的状态 1：关门 0：开门
        for (int i = 0; i < battery; i++) {

            Map<String, String> map = list.get(i);
            String asoc = map.get("asoc");


            String door_state = map.get("door_state");
            String A = Unit.hexString2binaryString(door_state);
            String A_1 = A.substring(0, 1);
            String A_2 = A.substring(1, 2);
            String A_3 = A.substring(2, 3);
            String A_4 = A.substring(3, 4);
            String B_1 = A.substring(4, 5);
            String B_2 = A.substring(5, 6);
            String B_3 = A.substring(6, 7);
            String B_4 = A.substring(7, 8);

            String bms_state = map.get("bms_state");
            String C = Unit.hexString2binaryString(bms_state);
            String C_1 = C.substring(0, 1);
            String C_2 = C.substring(1, 2);
            String C_3 = C.substring(2, 3);
            String C_4 = C.substring(3, 4);
            String D_1 = C.substring(4, 5);
            String D_2 = C.substring(5, 6);
            String D_3 = C.substring(6, 7);
            String D_4 = C.substring(7, 8);

            String cab_state = map.get("cab_state");
            String E = Unit.hexString2binaryString(cab_state);
            String E_1 = E.substring(0, 1);
            String E_2 = E.substring(1, 2);
            String E_3 = E.substring(2, 3);
            String E_4 = E.substring(3, 4);
            String F_1 = E.substring(4, 5);
            String F_2 = E.substring(5, 6);
            String F_3 = E.substring(6, 7);
            String F_4 = E.substring(7, 8);


            if (B_2.equals("0")) {
                is_open_ba[i] = 1;
            } else {
                is_open_ba[i] = 0;
            }

            if (B_1.equals("1")) {
                int dianliang_baifenbi = Integer.parseInt(asoc, 16);
                bar_per[i] = dianliang_baifenbi+"";
                if (dianliang_baifenbi >= 95) {
                    is_null_ba[i] = 1; // 满电
                } else {
                    is_null_ba[i] = 2; // 充电
                }
            } else {
                is_null_ba[i] = 0; // 空仓
            }

            int a = 0;
            try {
                JSONObject errorMap = new JSONObject();
                errorMap.put("id", i + "");
                if (B_3.equals("1")) {
                    errorMap.put("error1", "1");//柜门工作故障
                    a = a + 1;
                    System.out.println(i + "柜门工作故障");
                }
                if (B_4.equals("1")) {
                    errorMap.put("error2", "2");//柜门通信故障
                    a = a + 1;
                    System.out.println(i + "柜门通信故障");
                }
                if (D_3.equals("1")) {
                    errorMap.put("error3", "3");//电池工作故障
                    //不算错误
                    System.out.println(i + "电池工作故障");
//                    a = a + 1;
                }
                if (F_3.equals("1")) {
                    errorMap.put("error4", "4");//充电机工作故障
                    a = a + 1;
                    System.out.println(i + "充电机工作故障");
                }
                if (F_4.equals("1")) {
                    errorMap.put("error5", "5");//充电机通信故障
                    System.out.println(i + "充电机通信故障");
                    a = a + 1;
                }
                if (a > 0) {
                    is_null_ba[i] = 3; //故障
                }
                errorList.put(i,errorMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            int count = 0;
            for (int m = 0; m < is_null_ba.length; m++) {
                int n = is_null_ba[m];
                if (n == 0) {
                    count = count + 1;
                }
            }

            is_null_count = count;
            if (is_null_count == 1) {
                for (int m = 0; m < is_null_ba.length; m++) {
                    int n = is_null_ba[m];
                    if (n == 0) {
                        last_null_door = m + 1;
                    }
                }
            }
        }
    }

    private void clearM() { // 初始化数据
        meiyou_kong_diangui = 0;
        mubiao_men_id = -1;
        x = 0;
        dakai_namdian_men_id = -1;
    }


    @Override
    public void onClick(View v) {
        if (panel.getId() == v.getId()) {
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

    private void send_message_05(int count) {
        String renturn_str = "";
        List<String> sendList = new ArrayList<String>();
//      编辑信息行
        sendList.add("3C");
//      从机地址
        sendList.add("21");
//      通信指令
        sendList.add("05");
//      版本协议
        sendList.add("02");
//      帧长度
        sendList.add(new Unit().get_count(count));
//      柜门数
        sendList.add(new Unit().get_car_count(count));
        //数据位
        for (int i = 0; i < count; i++) {
            sendList.add(bat_door_state[i]);
        }
//      校验位
        int j = battery + 8 - 3; // 异或的位数
        int[] by = new int[j];
        for (int i = 1; i < sendList.size(); i++) {
            int a = Integer.parseInt(sendList.get(i), 16);
            by[i - 1] = a;
        }
        int b = new Unit().get_crc(by);
        String s_b = Integer.toHexString(b);
        String S_B = s_b.toUpperCase();
        if (S_B.length() == 1) {
            S_B = "0" + S_B;
        }
        sendList.add(S_B);
//      帧结束标志
        sendList.add("3E");
        //合成写入的信息行
        String S = "";
        for (int i = 0; i < sendList.size(); i++) {
            S = S + sendList.get(i);
        }
        renturn_str = S;
        //信息行处理
        sendList.clear();

        String str = renturn_str;
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

    private void send_message_06(int count, int tCount) { //z：模式  ... count:总电池块数  ... tCount:目标电池 ...
        String renturn_str = "";
        List<String> sendList = new ArrayList<String>();
//      编辑信息行
        sendList.add("3C");
//      从机地址
        sendList.add("21");
//      通信指令
        sendList.add("06");
//      版本协议
        sendList.add("02");
//      帧长度
        sendList.add(new Unit().get_count(count));
//      数据内容
        sendList.add(new Unit().get_car_count(tCount));
        for (int i = 0; i < count; i++) {
            sendList.add(bat_door_state[i]);
        }
//      校验位
        int j = battery + 8 - 3; // 异或的位数
        int[] by = new int[j];
        for (int i = 1; i < sendList.size(); i++) {
            int a = Integer.parseInt(sendList.get(i), 16);
            by[i - 1] = a;
        }
        int b = new Unit().get_crc(by);
        String s_b = Integer.toHexString(b);
        String S_B = s_b.toUpperCase();
        if (S_B.length() == 1) {
            S_B = "0" + S_B;
        }
        sendList.add(S_B);
//      帧结束标志
        sendList.add("3E");
        //合成写入的信息行
        String S = "";
        for (int i = 0; i < sendList.size(); i++) {
            S = S + sendList.get(i);
        }
        renturn_str = S;
        //信息行处理
        sendList.clear();

        String str = renturn_str;
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

    private void showTheToast(String string) {
        Toast toast = Toast.makeText(this, string, Toast.LENGTH_LONG);
        View view = LayoutInflater.from(this).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}




class HttpInputOldBar extends Thread {

    private Activity activity;
    private String cabinetid;
    private Handler errorHandler, successHanler;
    private String member;
    private JSONObject jsonArray;
    private SharedPreferences sharedPreferences;

    public HttpInputOldBar(Activity activity, String member, String cabinetid, Handler errorHandler, Handler successHanler) {
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
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());


                System.out.println(result.toString()+"aaaaaaa");

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
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
                    message.setData(bundle);
                    if (errorHandler != null) {
                        jsonArray = jsonObject.getJSONObject("data");
                        successHanler.sendMessage(message);
                    }
                } else {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "HttpInputOldBar:返回错误信息！");
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                }
            } else {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpInputOldBar:服务器错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpInputOldBar:网络错误，请联系厂家！！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        }
    }

    public JSONObject getJSONArray() {
        return jsonArray;
    }

}


class HttpOutputNewBar extends Thread {

    private Activity activity;
    private String cabinetid;
    private Handler errorHandler, successHanler;
    private String member, old_member, insert_id;
    private JSONObject jsonArray;
    private SharedPreferences sharedPreferences;

    public HttpOutputNewBar(Activity activity, String member, String old_member, String cabinetid, String insert_id, Handler errorHandler, Handler successHanler) {
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

                System.out.println(jsonObject + "bbbbbbb");

                String messageStr = jsonObject.getString("message");
                String code = jsonObject.getString("code");
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
                    message.setData(bundle);
                    if (errorHandler != null) {
                        successHanler.sendMessage(message);
                    }
                } else {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "HttpOutputNewBar:返回错误信息！");
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                }
            } else {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpOutputNewBar:服务器错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpOutputNewBar:json解析错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        }
    }

    public JSONObject getJSONArray() {
        return jsonArray;
    }

}


