package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;
import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.ProgressDialog;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.lang.reflect.Method;
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

import static com.xinyu.ElectricCabinet.main.MyApplication.cab_version;

/**
 * Created by apple on 2017/9/5.
 */

public class NewControl extends Activity implements View.OnClickListener {

    private Activity activity;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    //输入相关
    private TextView number_1, number_2, number_3, number_4, number_5, number_6, number_7, number_8, number_9, number_0, number_clear, submit;
    private EditText username, password;
    private String username_string = "", password_string = "";
    private LinearLayout setup, number_back, username_cover, password_cover;

    private int setup_count = 0;

    private HttpLogin httpLogin;
    private HttpAllowLogin httpAllowLogin;

    private Handler returnErrorHandler, returnSuccessInfoHandler, allowLoginSuccessHandler;

    private int submit_state = 1;
    public static Handler getFocusHandler;


    //显示信息相关
    ImageView bar_1_bg_img, bar_2_bg_img, bar_3_bg_img, bar_4_bg_img, bar_5_bg_img, bar_6_bg_img, bar_7_bg_img, bar_8_bg_img, bar_9_bg_img, bar_10_bg_img, bar_11_bg_img, bar_12_bg_img;
    TextView bar_1_text, bar_2_text, bar_3_text, bar_4_text, bar_5_text, bar_6_text, bar_7_text, bar_8_text, bar_9_text, bar_10_text, bar_11_text, bar_12_text;
    private List<TextView> textViewList = new ArrayList<>();
    private List<ImageView> imageViewList = new ArrayList<>();
    TextView cabinet_id, version;
    ImageView wifi_image;
    TextView log;

    //信息显示相关数据
    private HttpUploadBatteryInfo httpUploadBatteryInfo;
    private HttpInputOldBar httpInputOldBar;
    private HttpOutputNewBar httpOutputNewBar;
    private HttpQcodeRequest httpQcodeRequest;
    private HttpUploadFullBarCount httpUploadFullBarCount;
    private HttpUpWebOpenDoor httpUpWebOpenDoor;
    private HttpWebOpenDoorRenturn httpWebOpenDoorRenturn;

    private String str[] = new String[]{"关闭", "打开", "损坏"};
    private String state[] = new String[]{"01", "00", "02"};

    private PL2303MultiLib mSerialMulti;
    private static final int DeviceIndex = 0;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";
    private UARTSettingInfo gUARTInfo = new UARTSettingInfo();
    private int iDeviceCount = 0; // 搜索的usb driver 数量
    private boolean gThreadStop = false;
    private boolean gRunningReadThread = false;

    private String cabinetID;
    private Handler getReturnTextHandler, sendMessageHandler, httpUploadBatteryInfoSuccessHandler, httpErrorHandler, httpInputOldBarSuccessHandler, httpInputOldBarErrorHandler, OutputNewBarSuccessHandler, setWifiInfo, setAdvPanelHandler, QcodeRequestSuccessHandler, setlogHandleer, webAdminOpenDoorHandler, adminCloseDoorHandler;
    public static Handler openNullDoorHandler, adminOpenDoorHandler;
    private int battery = 12;//电池数量
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
    private int[] is_null_ba; // 所有电池的舱室状态 个数以battery而定  0：空仓 1：满电 2：充电 3：找不到充电机 4：充电机故障
    private int[] is_open_ba; // 所有电池的门锁状态 0：开门 1：锁上 3：门锁故障
    private int is_null_count = -1;
    private int last_null_door = -1;
    private String[] barID;
    private String[] infoStr;

    private int IS_AREADY_RUN = 0;
    private int is_normal_action = 0;
    private int system_info = 0;

    private int thread_1_code = 0, thread_2_code = 0;


    //屏保 + 二维码 参数
    private int define_wait_time = 180;
    private int wait_time = 180;
    private int yijing_denglu = 0;
    private RelativeLayout adv_panel;
    private ImageView adv_image;
    private int showType = 0;
    private ImageView qcode_download, qcode_scan;


    private Thread thread_1 = new Thread() { // 此线程为给电机发送命令   获得整体电池的数据后会上传电池的数据
        @Override
        public void run() { // 柜门发送信息 启动线程
            super.run();
            while (thread_1_code == 0) {
                try {
                    if (sendMessageHandler != null) {
                        sendMessageHandler.sendMessage(new Message());
                    }
                    Thread.sleep(200);
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

                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Thread thread3 = new Thread() { // 此线程为给电机发送命令   获得整体电池的数据后会上传电池的数据
        @Override
        public void run() { //二维码请求接口
            super.run();
            while (true) {
                if (wait_time > 0) {
                    wait_time = wait_time - 1;
                    setlogHandleer.sendMessage(new Message());
                    if (yijing_denglu == 0) {
                        httpQcodeRequest = new HttpQcodeRequest(cabinetID, httpErrorHandler, QcodeRequestSuccessHandler);
                        httpQcodeRequest.start();
                    } else {
                        System.out.println("用户已经登录了");
                    }
                } else {
                    setAdvPanelHandler.sendMessage(new Message());
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Thread thread_4 = new Thread() { // 此线程为给电机发送命令   获得整体电池的数据后会上传电池的数据
        @Override
        public void run() { //上传电池信息
            super.run();
            while (true) {
                httpUploadFullBarCount = new HttpUploadFullBarCount(cabinetID, get_full_power_count() + "");
                httpUploadFullBarCount.start();

                httpUpWebOpenDoor = new HttpUpWebOpenDoor(cabinetID, webAdminOpenDoorHandler, httpErrorHandler);
                httpUpWebOpenDoor.start();


                try {
                    sleep(3000);
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
                } else {
                    if (!out_str.equals("")) { //数据不正常 两个数据叠在一起了 截取依次发送
                        if (out_str.length() > 140) {
                            String str = out_str.substring(0, 6);
                            String str1 = "";
                            String str2 = "";
                            if (str.equals("3c21c5")) {  //判断两个跌在一起的数据那个在前，那个在后
                                str1 = out_str.substring(0, 76);
                                str2 = out_str.substring(76, out_str.length());
                            } else if (str.equals("3c21c6")) {
                                str1 = out_str.substring(0, 66);
                                str2 = out_str.substring(66, out_str.length());
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
                        } else {//发送接受的正常数据
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
        setContentView(R.layout.new_control);
        MyApplication.getInstance().addActivity(this);

        init();
        main();
        handler();

        order_send_str = make_message(1, battery, 0, 0);

        thread_2.start();
        thread_4.start();

        Intent intent = new Intent(activity, MyService.class);
        PendingIntent sender = PendingIntent.getService(activity, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 1000, sender);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!thread_1.isAlive()) {
            init();    //初始化相关
            main();    //数据版相关
            handler(); //内部通信相关
            order_send_str = make_message(1, battery, 0, 0);
        }
        if (!thread_2.isAlive()) {
            thread_2.start();
        }
        if (!thread_4.isAlive()) {
            thread_4.start();
        }

        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE); //初始化简单数据库
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cabinetID = sharedPreferences.getString("cabinetNumber", "");
        if (cabinetID.equals("")) {
            editor.putString("cabinetNumber", "77016040001"); //出场的默认id
            editor.commit();
            cabinetID = "77016040001";
        } else {
            cabinet_id = (TextView) this.findViewById(R.id.cabinet_id);
            cabinet_id.setText(cabinetID);
        }
        cabinet_id.setText(cabinetID);
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
        progressDialog = new ProgressDialog(activity);
        sharedPreferences = getSharedPreferences("cabinetInfo", Activity.MODE_PRIVATE); //初始化简单数据库
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cabinetID = sharedPreferences.getString("cabinetNumber", "");
        if (cabinetID.equals("")) {
            editor.putString("cabinetNumber", "77016040001"); //出场的默认id
            editor.commit();
            cabinetID = "77016040001";
        } else {
            cabinet_id = (TextView) this.findViewById(R.id.cabinet_id);
            cabinet_id.setText(cabinetID);
        }


        is_null_ba = new int[battery]; //初始化柜 电池 状态
        is_open_ba = new int[battery]; //初始化柜 门锁 状态
        barID = new String[battery]; //初始化电池id
        infoStr = new String[battery];

        //输入信息初始化
        number_1 = (TextView) activity.findViewById(R.id.number_1);
        number_2 = (TextView) activity.findViewById(R.id.number_2);
        number_3 = (TextView) activity.findViewById(R.id.number_3);
        number_4 = (TextView) activity.findViewById(R.id.number_4);
        number_5 = (TextView) activity.findViewById(R.id.number_5);
        number_6 = (TextView) activity.findViewById(R.id.number_6);
        number_7 = (TextView) activity.findViewById(R.id.number_7);
        number_8 = (TextView) activity.findViewById(R.id.number_8);
        number_9 = (TextView) activity.findViewById(R.id.number_9);
        number_0 = (TextView) activity.findViewById(R.id.number_0);
        number_back = (LinearLayout) activity.findViewById(R.id.number_back);
        number_clear = (TextView) activity.findViewById(R.id.number_clear);
        number_1.setOnClickListener(this);
        number_2.setOnClickListener(this);
        number_3.setOnClickListener(this);
        number_4.setOnClickListener(this);
        number_5.setOnClickListener(this);
        number_6.setOnClickListener(this);
        number_7.setOnClickListener(this);
        number_8.setOnClickListener(this);
        number_9.setOnClickListener(this);
        number_0.setOnClickListener(this);
        number_back.setOnClickListener(this);
        number_clear.setOnClickListener(this);

        username_cover = (LinearLayout) activity.findViewById(R.id.username_cover);
        username_cover.setOnClickListener(this);
        password_cover = (LinearLayout) activity.findViewById(R.id.password_cover);
        password_cover.setOnClickListener(this);

        username = (EditText) activity.findViewById(R.id.username);
        disableShowSoftInput(username);
        username.setFocusable(true);
        username.setFocusableInTouchMode(true);
        username.requestFocus();
        username.requestFocusFromTouch();


        password = (EditText) activity.findViewById(R.id.password);
        disableShowSoftInput(password);
        password.setFocusable(true);
        password.setFocusableInTouchMode(true);

        submit = (TextView) activity.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        setup = (LinearLayout) activity.findViewById(R.id.setup);
        setup.setOnClickListener(this);


        //输出信息初始化

        bar_1_bg_img = (ImageView) this.findViewById(R.id.bar_1_bg_img);
        bar_1_text = (TextView) this.findViewById(R.id.bar_1_text);
        bar_2_bg_img = (ImageView) this.findViewById(R.id.bar_2_bg_img);
        bar_2_text = (TextView) this.findViewById(R.id.bar_2_text);
        bar_3_bg_img = (ImageView) this.findViewById(R.id.bar_3_bg_img);
        bar_3_text = (TextView) this.findViewById(R.id.bar_3_text);
        bar_4_bg_img = (ImageView) this.findViewById(R.id.bar_4_bg_img);
        bar_4_text = (TextView) this.findViewById(R.id.bar_4_text);
        bar_5_bg_img = (ImageView) this.findViewById(R.id.bar_5_bg_img);
        bar_5_text = (TextView) this.findViewById(R.id.bar_5_text);
        bar_6_bg_img = (ImageView) this.findViewById(R.id.bar_6_bg_img);
        bar_6_text = (TextView) this.findViewById(R.id.bar_6_text);
        bar_7_bg_img = (ImageView) this.findViewById(R.id.bar_7_bg_img);
        bar_7_text = (TextView) this.findViewById(R.id.bar_7_text);
        bar_8_bg_img = (ImageView) this.findViewById(R.id.bar_8_bg_img);
        bar_8_text = (TextView) this.findViewById(R.id.bar_8_text);
        bar_9_bg_img = (ImageView) this.findViewById(R.id.bar_9_bg_img);
        bar_9_text = (TextView) this.findViewById(R.id.bar_9_text);
        bar_10_bg_img = (ImageView) this.findViewById(R.id.bar_10_bg_img);
        bar_10_text = (TextView) this.findViewById(R.id.bar_10_text);
        bar_11_bg_img = (ImageView) this.findViewById(R.id.bar_11_bg_img);
        bar_11_text = (TextView) this.findViewById(R.id.bar_11_text);
        bar_12_bg_img = (ImageView) this.findViewById(R.id.bar_12_bg_img);
        bar_12_text = (TextView) this.findViewById(R.id.bar_12_text);

        textViewList.add(bar_1_text);
        textViewList.add(bar_2_text);
        textViewList.add(bar_3_text);
        textViewList.add(bar_4_text);
        textViewList.add(bar_5_text);
        textViewList.add(bar_6_text);
        textViewList.add(bar_7_text);
        textViewList.add(bar_8_text);
        textViewList.add(bar_9_text);
        textViewList.add(bar_10_text);
        textViewList.add(bar_11_text);
        textViewList.add(bar_12_text);

        imageViewList.add(bar_1_bg_img);
        imageViewList.add(bar_2_bg_img);
        imageViewList.add(bar_3_bg_img);
        imageViewList.add(bar_4_bg_img);
        imageViewList.add(bar_5_bg_img);
        imageViewList.add(bar_6_bg_img);
        imageViewList.add(bar_7_bg_img);
        imageViewList.add(bar_8_bg_img);
        imageViewList.add(bar_9_bg_img);
        imageViewList.add(bar_10_bg_img);
        imageViewList.add(bar_11_bg_img);
        imageViewList.add(bar_12_bg_img);


        cabinet_id = (TextView) this.findViewById(R.id.cabinet_id);
        wifi_image = (ImageView) this.findViewById(R.id.wifi_image);

        qcode_scan = (ImageView) this.findViewById(R.id.qcode_scan);
        qcode_scan.setImageBitmap(generateBitmap(cabinetID, 400, 400));

        qcode_download = (ImageView) this.findViewById(R.id.qcode_download);
        qcode_download.setImageBitmap(generateBitmap("http://www.huandianwang.com/index.php/Business/qrcode", 400, 400));


        adv_panel = (RelativeLayout) this.findViewById(R.id.adv_panel);
        adv_panel.setOnClickListener(this);

        log = (TextView) this.findViewById(R.id.log);

        version = (TextView) this.findViewById(R.id.version);
        version.setText("VERSION : " + cab_version);

        adv_image = (ImageView) this.findViewById(R.id.adv_image);
        Picasso.with(activity).load(R.drawable.bg_1).into(adv_image);
    }

    public void disableShowSoftInput(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }
        }
        editText.setSelection(editText.getText().length());
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

        setlogHandleer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (showType == 0) {
                    log.setText("请在 " + wait_time + " 秒内完成换电");
                } else if (showType == 1) {
                    log.setText("请在 " + wait_time + " 秒内拿走电池");
                } else if (showType == 2) {
                    log.setText("换电将在 " + wait_time + " 秒内结束");
                }
            }
        };

        returnErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                submit_state = 1;
                progressDialog.dismiss();
                String str = msg.getData().getString("message");
                showTheToast(str);
            }
        };
        returnSuccessInfoHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                submit_state = 1;
                progressDialog.dismiss();
                String str = msg.getData().getString("message");
                openNullDoorHandler.sendMessage(new Message());
            }
        };
        allowLoginSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setup_count = 0;
                startActivity(new Intent(activity, Setup.class));
                System.out.println("管理员登陆后台");
            }
        };

        getFocusHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (username != null) {
                    username.requestFocus();
                }
            }
        };

        getReturnTextHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = msg.getData().getString("msg");
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < s.length() / 2; i++) {
                    list.add(s.substring(i * 2, i * 2 + 2));
                }
                if (list.size() == 33) {
                    int local = Integer.parseInt(list.get(5), 16);
                    int dianliang = Integer.parseInt(list.get(24), 16);
                    int wendu = Integer.parseInt(list.get(23), 16) - 40;
                    if (wendu == -40) {
                        wendu = 0;
                    }

                    DecimalFormat decimalFormat = new DecimalFormat(".00");

                    float b = Integer.parseInt(list.get(14) + list.get(15), 16) * (float) 0.1;
                    String q = decimalFormat.format(b);
                    float dianya = Float.parseFloat(q);

                    float a = Integer.parseInt(list.get(20) + list.get(21), 16) * (float) 0.1;
                    String p = decimalFormat.format(a);
                    float dianliu = Float.parseFloat(p);

                    String BID = list.get(6) + list.get(7) + list.get(8) + list.get(9) + list.get(10) + list.get(11) + list.get(12) + list.get(13);
//                    String BID = list.get(7)+list.get(8)+list.get(9)+list.get(10)+list.get(11)+list.get(12);

                    infoStr[local - 1] = "温度：" + Integer.toString(wendu) + "C   电压：" + dianya + "V   电流：" + dianliu + "A";
                    Unit.barInfo = infoStr;


                    for (int i = 0; i < textViewList.size(); i++) {
                        if (i == local - 1) {
                            if (is_null_ba[local - 1] == 4) {
                                TextView t = textViewList.get(i);
                                t.setText("电机故障");
                                t.setTextColor(0xff000000);
                                ImageView iv = imageViewList.get(i);
                                iv.setImageResource(R.drawable.bar_low_img);
                            } else if (is_null_ba[local - 1] == 3) {
                                TextView t = textViewList.get(i);
                                t.setText("电机失联");
                                t.setTextColor(0xff000000);
                                ImageView iv = imageViewList.get(i);
                                iv.setImageResource(R.drawable.bar_low_img);
                            } else {
                                if (is_null_ba[local - 1] == 1) {
                                    TextView t = textViewList.get(i);
                                    t.setTextColor(0xff000000);
                                    ImageView iv = imageViewList.get(i);
                                    iv.setImageResource(R.drawable.bar_full_img);
                                    t.setText(dianliang + "%");
                                } else {
                                    TextView t = textViewList.get(i);
                                    t.setTextColor(0xffffffff);
                                    ImageView iv = imageViewList.get(i);
                                    iv.setImageResource(R.drawable.bar_half_img);
                                    if (dianliang == 0) {
                                        t.setText(dianliang + "%");
                                    } else {
                                        t.setText(dianliang - 1 + "%");
                                    }
                                }
                            }
                            if (wendu == 0 && dianya == 0 && dianliu == 0 && dianliu == 0) {
                                TextView t = textViewList.get(i);
                                t.setText("空");
                                t.setTextColor(0xff000000);
                                ImageView iv = imageViewList.get(i);
                                iv.setImageResource(R.drawable.bar_low_img);
                            }

                        }
                    }

                    barID[local - 1] = BID; // 设置每个电池的ID
                    Unit.barId = barID;//环境变量的电池id 方便后台拿去数据

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
                        jsonObject.put("cabinet", cabinet_id);
                        jsonObject.put("voltage", dianya);
                        jsonObject.put("electricity", dianliu);
                        jsonObject.put("temperture", wendu);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    send_jsonarray.put(jsonObject);
                } else {

                    if (list.size() == (8 + battery * 2)) {
                        List<String> list2 = new ArrayList<String>();
                        for (int i = 0; i < s.length() / 4; i++) {
                            list2.add(s.substring(i * 4, i * 4 + 4));
                        }
                        System.out.println(list2.toString());
                        set_door_state(list2);
                        Unit.barSta = is_null_ba;
                    }
                    if (send_jsonarray.length() == battery) {
                        httpUploadBatteryInfo = new HttpUploadBatteryInfo(activity, cabinetID, send_jsonarray, httpErrorHandler, httpUploadBatteryInfoSuccessHandler);
                        httpUploadBatteryInfo.start();
                        send_jsonarray = null;
                        send_jsonarray = new JSONArray();
                    } else if (send_jsonarray.length() > battery) {
                        send_jsonarray = null;
                        send_jsonarray = new JSONArray();
                    }
                }


            }
        };

        sendMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { // j：柜门配置要发送的最大次数  m：当前发送的是第几条  i： 电池循环发送
                super.handleMessage(msg);

                if (linshi_order_send.equals("")) {
                    if (order_send_str != "") {
                        send_message(order_send_str);
                        order_send_str = "";

                        if (is_null_count == 0 && is_normal_action == 0) {  // 打开最后一个空仓门
//                            linshi_order_send = make_message(1, battery, 0, last_null_door); // 打开一个充满电的舱门
//                            showTheToast("操作错误：取出电池，关上舱门后，重新换电！");
                        }

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
                } else {
                    System.out.println(linshi_order_send);
                    if (linshi_count > 2) {
                        linshi_order_send = "";
                        linshi_count = 0;
                    } else {
                        send_message(linshi_order_send);
                        linshi_count = linshi_count + 1;
                    }
                }
            }
        };

        adminOpenDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = msg.getData().getString("item");
                if (str.equals("0")) {
                    linshi_order_send = openAllDoor();
                } else {
                    int doorid_1 = Integer.parseInt(str);
                    linshi_order_send = make_message(1, battery, 0, doorid_1);
                }
                clearM(3);
            }
        };

        adminCloseDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                clearM(5);

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
                int is_have_open_door = 0;
                for (int i = 0; i < is_open_ba.length; i++) {
                    if (is_open_ba[i] == 0) {
                        is_have_open_door = 1;
                        break;
                    }
                }
                if (is_have_open_door == 1) {
                    showTheToast("请先手动确认柜门是否全部关上！谢谢配合！");
                    clearM(3);
                } else if (get_full_power_count() < 1) {
                    showTheToast("此电柜没有可更换的满电电池，请您耐心等候！");
                    clearM(3);
                } else {
                    open_null_door();
                    yijing_denglu = 1;
                }
                is_normal_action = 1;
            }
        };

        setWifiInfo = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.getData().getString("wifi").equals("true")) {
                    wifi_image.setImageResource(R.drawable.wifi_image);
                } else {
                    wifi_image.setImageResource(R.drawable.wifi_0);
                }
            }
        };

        setAdvPanelHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                adv_panel.setVisibility(View.VISIBLE);
                adv_panel.setClickable(true);
            }
        };

        QcodeRequestSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = msg.getData().getString("data");
                Toast.makeText(activity, "扫码成功，正在登录！", Toast.LENGTH_LONG).show();
                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                    String mobile = jsonObject.getString("mobile");
                    String password = jsonObject.getString("password");

                    httpLogin = new HttpLogin(activity, mobile, password, cabinetID, returnErrorHandler, returnSuccessInfoHandler);
                    httpLogin.start();
                    progressDialog.show();

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        };

        //web端 控制开门
        webAdminOpenDoorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String data = msg.getData().getString("data");
                String message = msg.getData().getString("message");

                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        String status = jsonObject.getString("status");
                        if (status.equals("true")) {
                            Message message_1 = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("item", Integer.toString(i + 1));
                            message_1.setData(bundle);
                            adminOpenDoorHandler.sendMessage(message_1);

                            httpWebOpenDoorRenturn = new HttpWebOpenDoorRenturn(cabinetID, i + 1 + "");
                            httpWebOpenDoorRenturn.start();

                            timer1(i);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        };
    }

    //web后台开门计时器
    void timer1(final int door_i) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Message message_1 = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("item", Integer.toString(door_i));
                message_1.setData(bundle);
                adminCloseDoorHandler.sendMessage(message_1);
            }
        }, 15000);// 设定指定的时间time,
    }

    private void set_door_state(List<String> list) { //  获取电池仓门锁的状态 1：关门 0：开门
        for (int i = 0; i < battery; i++) {
            String str = list.get(i + 3); // 单个状态 例：[1400]

            String a = str.substring(0, 2); //  例：[14]
            String b = str.substring(2, 4); //  例：[14]


            String A = Unit.hexString2binaryString(a); //  例：[0001 0100]
            String B = Unit.hexString2binaryString(b);//   例：[0000 0000]

            String A_1 = A.substring(0, 1); // 充电机快冲状态
            String A_2 = A.substring(1, 2); // 电池组接入状态
            String A_3 = A.substring(2, 3); //
            String A_4 = A.substring(3, 4); // 柜门开关状态

            String A_5 = A.substring(4, 5); // 柜门锁故障状态
            String A_6 = A.substring(5, 6); // 充电机通信故障
            String A_7 = A.substring(6, 7); // 电池组故障
            String A_8 = A.substring(7, 8); // 充电机故障

            String B_1 = B.substring(0, 1); // 满电状态
            String B_2 = B.substring(1, 2); // 电池组掉电状态
            String B_3 = B.substring(2, 3); // 充电低温
            String B_4 = B.substring(3, 4); // 充电高温

            String B_5 = B.substring(4, 5); // 充电过流
            String B_6 = B.substring(5, 6); // 低压故障
            String B_7 = B.substring(6, 7); // 高压故障
            String B_8 = B.substring(7, 8); // 充电状态

            if (A_5.equals("1")) {
                is_open_ba[i] = 2;
            } else {
                if (A_4.equals("0")) {
                    is_open_ba[i] = 0;
                } else if (A_4.equals("1")) {
                    is_open_ba[i] = 1;
                }
            }


            if (A_6.equals("1")) {
                is_null_ba[i] = 3; // 找不到充电机
            } else if (A_8.equals("1")) {
                is_null_ba[i] = 4; // 充电机故障
            } else {
                if (B_1.equals("1")) {
                    is_null_ba[i] = 1; // 满电
                } else {
                    if (A_2.equals("0")) {
                        is_null_ba[i] = 0; // 空仓
                    } else if (A_2.equals("1")) {
                        is_null_ba[i] = 2; // 充电
                    } else {
                        is_null_ba[i] = 0; // 空仓
                    }
                }
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

    private int get_full_power_count() { // 获得充满电的电池总数
        int count = 0;
        for (int i = 0; i < is_null_ba.length; i++) {
            if (is_null_ba[i] == 1) {
                count = count + 1;
            }
        }
        return count;
    }

    private int get_a_full_power_bar(int aID) { // 获得一个充满电的电池 返回0的话则为没有
        int j = 0;
        for (int i = 0; i < is_null_ba.length; i++) {
            if (is_null_ba[i] == 1 && i != aID) {
                j = i + 1;
                break;
            }
        }
        return j;
    }

    private void open_null_door() { // 打开一个空仓门（判断必须空仓大于2，小于2提示没有可更换电池）
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
                    showTheToast("请插入电池");
                    temp_state_n = is_open_ba[i];
                    linshi_order_send = make_message(1, battery, 0, i + 1);
                    temp_n = i + 1;
                    break;
                }
            }
        } else {
            showTheToast("柜内可更换电池仓不足！请联系店家！");
            clearM(3);
        }
    }

    private void null_door_is_open() { //检测空仓们是否打开
        int state = is_open_ba[temp_n - 1];
        if (state != temp_state_n) {
            n = temp_n;
            temp_state_n = 0;
            temp_n = 0;
        }
    }

    private int send_HttpInputOldBar_count = 0;

    private void check_change_door() {  // 检测是否有电池放入电柜 且 是否关闭舱门
        System.out.println("现在打开的舱门为：" + n);
        if (is_null_ba[n - 1] != 0 && is_open_ba[n - 1] == 1) {
            if (get_full_power_count() > 0) {
                final int full_bar = get_a_full_power_bar(n - 1);
                if (full_bar == 0) {
                    showTheToast("此电柜没有可更换的满电电池，请您耐心等候！");
                    clearM(3);
                } else {
                    if (system_info < 10) {
                        if(system_info == 0){
                            showTheToast("系统识别中......请等待！");
                        }
                        system_info = system_info + 1;
                    } else {
                        if (!barID[n - 1].equals("0000000000000000")) {
                            httpInputOldBarSuccessHandler = new Handler() {  //打开一个充满电的舱门
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    inputReturnJSON = httpInputOldBar.getJSONArray();
                                    n = 0;
                                    temp_state_o = is_open_ba[full_bar - 1];
                                    linshi_order_send = make_message(1, battery, 0, full_bar); // 打开一个充满电的舱门
                                    System.out.println("打开一个充满电的舱门：" + full_bar);
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
                                        clearM(3);
                                        showTheToast("该电池并未录入系统！请取走该电池！");
                                    } else {
                                        linshi_order_send = make_message(1, battery, 0, n);
                                        clearM(3);
                                        showTheToast(str);
                                    }
                                    send_HttpInputOldBar_count = 0;
                                }
                            };
                            if (send_HttpInputOldBar_count == 0) {
                                httpInputOldBar = new HttpInputOldBar(this, barID[n - 1], cabinetID, httpInputOldBarErrorHandler, httpInputOldBarSuccessHandler);
                                httpInputOldBar.start();
                                send_HttpInputOldBar_count = send_HttpInputOldBar_count + 1;
                            }
                            x = 0; //计数器归零
                        }
                    }
                }
            } else {
                showTheToast("此电柜没有可更换的满电电池，请您耐心等候！！");
                clearM(3);
            }
            x = 0; //计数器归零
        } else if (is_null_ba[n - 1] == 0 && is_open_ba[n - 1] == 1) { //空仓 关门 了 ，接着打开这个柜门
            System.out.println(n + "：关门空仓");
            x = x + 1;  // 计数器 执行7次空仓关门 发送开门命令
            if (x > 15) {
                showTheToast("换电结束！感谢您的使用！");
                clearM(5);
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

    private void remove_full_bar() {  //检测是否拿出满电的电池   1：关门  0：开门
        if (o > 0) {
            if (is_null_ba[o - 1] != 0 && is_open_ba[o - 1] == 1) {  //如果里面还有电池，并门   继续打开这个舱门
                System.out.println("没有拿出满电电池！");
                y = y + 1;  // 计数器 执行5次空仓关门 发送开门命令
                if (y > 10) {
                    linshi_order_send = make_message(1, battery, 0, o);
                    y = 0;
                }
            } else if (is_null_ba[o - 1] != 0 && is_open_ba[o - 1] == 0) {
                System.out.println("没有拿出满电电池！");
            } else if (is_null_ba[o - 1] == 0 && is_open_ba[o - 1] == 1) {

                OutputNewBarSuccessHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        showTheToast("更换电池成功");
                    }
                };

                try {
                    String member = barID[o - 1];
                    String old_member = inputReturnJSON.getString("old_member");
                    String inesrt_id = inputReturnJSON.getString("inesrt_id");
                    httpOutputNewBar = new HttpOutputNewBar(activity, member, old_member, cabinetID, inesrt_id, httpErrorHandler, OutputNewBarSuccessHandler);
                    httpOutputNewBar.start();
                    linshi_order_send = make_message(2, battery, o, 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                clearM(5);
                System.out.println("更换电池成功！！！！！！");
                is_normal_action = 0;
                system_info = 0;
            }
        }
    }

    private void clearM(int pingbaoshijian) {
        i = 0; // 电池数循环发送计数
        n = 0; // 放入的旧电池所在仓
        temp_n = 0;
        temp_state_n = 0;
        o = 0; // 需要更换的满电电池所在仓
        temp_o = 0;
        temp_state_o = 0;
        x = 0; // check_change_door的计数器
        y = 0; // remove_full_bar的计数器f
        inputReturnJSON = null;

        wait_time = pingbaoshijian;
        yijing_denglu = 0;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == number_1.getId()) {
            setStringToEditText(1);
        } else if (v.getId() == number_2.getId()) {
            setStringToEditText(2);
        } else if (v.getId() == number_3.getId()) {
            setStringToEditText(3);
        } else if (v.getId() == number_4.getId()) {
            setStringToEditText(4);
        } else if (v.getId() == number_5.getId()) {
            setStringToEditText(5);
        } else if (v.getId() == number_6.getId()) {
            setStringToEditText(6);
        } else if (v.getId() == number_7.getId()) {
            setStringToEditText(7);
        } else if (v.getId() == number_8.getId()) {
            setStringToEditText(8);
        } else if (v.getId() == number_9.getId()) {
            setStringToEditText(9);
        } else if (v.getId() == number_0.getId()) {
            setStringToEditText(0);
        } else if (v.getId() == number_back.getId()) {
            setStringToEditText(-1);
        } else if (v.getId() == number_clear.getId()) {
            setStringToEditText(-2);
        } else if (v.getId() == username_cover.getId()) {
            username.requestFocus();
        } else if (v.getId() == password_cover.getId()) {
            password.requestFocus();
        } else if (v.getId() == setup.getId()) {
            if (setup_count < 5) {
                setup_count = setup_count + 1;
            } else {
                httpAllowLogin = new HttpAllowLogin(activity, cabinetID, returnErrorHandler, allowLoginSuccessHandler);
                httpAllowLogin.start();
                setup_count = 0;
            }
        } else if (v.getId() == submit.getId() && submit_state == 1) {
            submit_state = 0;
            httpLogin = new HttpLogin(activity, username_string, password_string, cabinetID, returnErrorHandler, returnSuccessInfoHandler);
            httpLogin.start();
            progressDialog.show();
            username.setText("");
            password.setText("");
            username_string = "";
            password_string = "";
            username.requestFocus();
        } else if (adv_panel.getId() == v.getId()) {
            adv_panel.setClickable(false);
            adv_panel.setVisibility(View.GONE);
            getFocusHandler.sendMessage(new Message());
            if (!thread3.isAlive()) {
                thread3.start();
            }
            wait_time = define_wait_time;
            showType = 0;

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

    private void send_message(int z, int count, int tCount) { //z：模式  ... count:总电池块数  ... tCount:目标电池 ...
        String str = make_message(z, count, tCount, 0);
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

    private void send_message(String string) {
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

    private String openAllDoor() { // 打开所有门
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

        for (int i = 0; i < battery; i++) {
            sendList.add(state[1]);

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
        return renturn_str;
    }

    private String make_message(int z, int count, int tCount, int door) { // tCount模式1没用
        String renturn_str = "";
        List<String> sendList = new ArrayList<String>();
//      编辑信息行
        sendList.add("3C");
//      从机地址
        sendList.add("21");
//      通信指令
        if (z == 1) {
            sendList.add("05");
        } else if (z == 2) {
            sendList.add("06");
        }
//      版本协议
        sendList.add("01");
//      帧长度
        sendList.add(new Unit().get_count(count));
//      数据内容
        if (z == 1) {
            sendList.add(new Unit().get_car_count(count));
        } else if (z == 2) {
            sendList.add(new Unit().get_car_count(tCount));
        }

        if (door != 0 && z == 1) {
            for (int i = 0; i < count; i++) {
                if (i == door - 1) {
                    sendList.add(state[1]);
                } else {
                    sendList.add(state[0]);
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                sendList.add(state[0]);

            }
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
        return renturn_str;
    }

    private void setStringToEditText(int number) {
        if (number >= 0) {
            if (username.hasFocus()) {
                if (username_string.length() < 11) {
                    username_string = username_string + Integer.toString(number);
                    username.setText(username_string);
                    username.setSelection(username.getText().length());
                    if (username_string.length() > 10) {
                        password.requestFocus();
                    }
                } else {
                    password.requestFocus();
                }
            } else if (password.hasFocus()) {
                password_string = password_string + Integer.toString(number);
                int pasCount = password_string.length();
                String outString = "";
                for (int i = 0; i < pasCount; i++) {
                    outString = outString + "*";
                }
                password.setText(outString);
                password.setSelection(password.getText().length());
            }
        } else if (number == -1) { //退回
            if (username.hasFocus()) {
                if (username_string.length() > 0) {
                    username_string = username_string.substring(0, username_string.length() - 1);
                    username.setText(username_string);
                    username.setSelection(username.getText().length());
                }
            } else if (password.hasFocus()) {
                if (password_string.length() > 0) {
                    password_string = password_string.substring(0, password_string.length() - 1);
                    int pasCount = password_string.length();
                    String outString = "";
                    for (int i = 0; i < pasCount; i++) {
                        outString = outString + "*";
                    }
                    password.setText(outString);
                    password.setSelection(password.getText().length());
                }
            }
        } else if (number == -2) { //清空
            if (username.hasFocus()) {
                username_string = "";
                username.setText(username_string);
                username.setSelection(username.getText().length());
            } else if (password.hasFocus()) {
                password_string = "";
                password.setText(password_string);
                password.setSelection(password.getText().length());
            }
        }
    }

    private void showTheToast(String string) {
        Toast toast = Toast.makeText(activity, string, Toast.LENGTH_LONG);
        View view = LayoutInflater.from(activity).inflate(R.layout.toast_panel, null);
        TextView textView = (TextView) view.findViewById(R.id.text_1);
        textView.setText(string);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class HttpQcodeRequest extends Thread {


    private String catinetNumberString;
    private Handler errorHandler, successHanler;


    public HttpQcodeRequest(String catinetNumberString, Handler errorHandler, Handler successHanler) {
        this.catinetNumberString = catinetNumberString;
        this.errorHandler = errorHandler;
        this.successHanler = successHanler;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        String path = "http://www.huandianwang.com/index.php/Login/cabinetlogin";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("cabinetid", catinetNumberString + ""));
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
                    if (successHanler != null) {
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
            System.out.println(e.toString());
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

class HttpUploadFullBarCount extends Thread {


    private String cabID;
    private String fullBarCount;


    public HttpUploadFullBarCount(String cabID, String fullBarCount) {
        this.cabID = cabID;
        this.fullBarCount = fullBarCount;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String path = "http://www.huandianwang.com/index.php/Yz/update_surplus";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("cabinet", cabID + ""));
        list.add(new BasicNameValuePair("surplus", fullBarCount + ""));
        String str = Unit.getMd5(cabID + "!@23*#&(@912oOo388*@#(fslKK");
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
                System.out.println("上传电池数：" + jsonObject);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}

class HttpUploadBatteryInfo extends Thread {

    private Activity activity;
    private String cabinetid;
    private JSONArray jsonArrayn;
    private Handler errorHandler, successHanler;

    public HttpUploadBatteryInfo(Activity activity, String cabinetid, JSONArray jsonArrayn, Handler errorHandler, Handler successHanler) {
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
        list.add(new BasicNameValuePair("cabinet", cabinetid));
        list.add(new BasicNameValuePair("json_string", jsonArrayn.toString() + ""));
        String str = Unit.getMd5(cabinetid + "!@23*#&(@912oOo388*@#(fslKK");
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

                System.out.println(jsonObject);

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
                    bundle.putString("message", "HttpUploadBatteryInfo:返回错误信息！");
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                }
            } else {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpUploadBatteryInfo:服务器错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            if (activity != null) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "HttpUploadBatteryInfo:json解析错误！");
                message.setData(bundle);
                if (errorHandler != null) {
                    errorHandler.sendMessage(message);
                }
            }
        }
    }
}

class HttpUpWebOpenDoor extends Thread {


    private String cabID;
    private Handler successHandler, errorHandler;


    public HttpUpWebOpenDoor(String cabID, Handler success, Handler error) {
        this.cabID = cabID;
        this.successHandler = success;
        this.errorHandler = error;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String path = "http://www.huandianwang.com/index.php/Yz/update_surplus";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("cabinet", cabID + ""));
        String str = Unit.getMd5(cabID + "!@23*#&(@912oOo388*@#(fslKK");
        list.add(new BasicNameValuePair("token", str));
        list.add(new BasicNameValuePair("type", "2"));

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
                String data = jsonObject.getString("data");

                if (code.equals("200")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    message.setData(bundle);
                    if (errorHandler != null) {
                        errorHandler.sendMessage(message);
                    }
                } else if (code.equals("0")) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", messageStr);
                    bundle.putString("data", data);
                    message.setData(bundle);
                    if (errorHandler != null) {
                        successHandler.sendMessage(message);
                    }
                } else if (code.equals("1")) {
                    System.out.println("无操作！");
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
            bundle.putString("message", "jason解析失败");
            message.setData(bundle);
            if (errorHandler != null) {
                errorHandler.sendMessage(message);
            }
        }
    }
}

class HttpWebOpenDoorRenturn extends Thread {


    private String cabID;
    private String door;


    public HttpWebOpenDoorRenturn(String cabID, String door) {
        this.cabID = cabID;
        this.door = door;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        String path = "http://www.huandianwang.com/index.php/Yz/update_surplus";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("cabinet", cabID + ""));
        String str = Unit.getMd5(cabID + "!@23*#&(@912oOo388*@#(fslKK");
        list.add(new BasicNameValuePair("token", str));
        list.add(new BasicNameValuePair("type", "2"));
        list.add(new BasicNameValuePair("door", door));
        list.add(new BasicNameValuePair("source", "123"));

        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                System.out.println("关门回调：" + jsonObject);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}


