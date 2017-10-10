package com.xinyu.ElectricCabinet.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyu.ElectricCabinet.R;
import com.xinyu.ElectricCabinet.pub.Unit;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;


public class Control extends Activity implements View.OnClickListener{

    private TextView send,receive;
    private LinearLayout item_1_panel,item_2_panel,item_3_panel;
    private TextView item_1_state,item_2_state,item_3_state;
    private TextView item_1_info,item_2_info,item_3_info;
    private Button button1;
    private Button button2;

    private String str[] = new String[]{"关闭","打开","损坏"};
    private String state[] = new String[]{"01","01","01"};
    private List<String> sendList = new ArrayList<String>();

    private String simple_str = "3C2105010B030101012C3E";
    private PL2303MultiLib mSerialMulti;
    private static final int DeviceIndex = 0;
    private static final String ACTION_USB_PERMISSION = "com.prolific.pluartmultisimpletest.USB_PERMISSION";
    private UARTSettingInfo gUARTInfoList;
    private int iDeviceCount = 0; // 搜索的usb driver 数量
    private boolean gThreadStop = false;
    private boolean gRunningReadThread = false;

    private Handler getReturnTextHandler,sendMessageHandler;
    private int target_battery = 2; //需要查询的目标电池
    private int battery = 3;//电池数量

    private int info_type = 0;

    private Thread thread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Thread.sleep(1000);
                    sendMessageHandler.sendMessage(new Message());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);
        init();
        main();
        handler();
    }

    private void handler() {

        getReturnTextHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = msg.getData().getString("msg");
                receive.setText(s);
                List<String> list = new ArrayList<String>();
                for(int i = 0 ; i < s.length()/2 ; i ++){
                    list.add(s.substring(i * 2, i * 2 + 2));
                }
                if(list.get(5).equals("02")){
                    item_2_info.setText("电量："+ Integer.parseInt(list.get(24),16)+"%"+"  温度："+ (Integer.parseInt(list.get(23),16)-40)+"C"+"  电流："+ (Integer.parseInt(list.get(20)+list.get(21),16)*0.1)+"A"+"  电压："+ (Integer.parseInt(list.get(14)+list.get(15),16)*0.1)+"V");
                };
            }
        };
        sendMessageHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                send_message(info_type);
            }
        };
    }

    private void init() {
        send = (TextView) this.findViewById(R.id.send);
        receive = (TextView) this.findViewById(R.id.receive);

        item_1_panel = (LinearLayout) this.findViewById(R.id.item_1_panel);
        item_1_panel.setOnClickListener(this);
        item_1_state = (TextView) this.findViewById(R.id.item_1_state);
        item_1_info = (TextView) this.findViewById(R.id.item_1_info);
        item_1_state.setTag("0");

        item_2_panel = (LinearLayout) this.findViewById(R.id.item_2_panel);
        item_2_panel.setOnClickListener(this);
        item_2_state = (TextView) this.findViewById(R.id.item_2_state);
        item_2_info = (TextView) this.findViewById(R.id.item_2_info);
        item_2_state.setTag("0");

        item_3_panel = (LinearLayout) this.findViewById(R.id.item_3_panel);
        item_3_panel.setOnClickListener(this);
        item_3_state = (TextView) this.findViewById(R.id.item_3_state);
        item_3_info = (TextView) this.findViewById(R.id.item_3_info);
        item_3_state.setTag("0");

        button1 = (Button) this.findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (Button) this.findViewById(R.id.button2);
        button2.setOnClickListener(this);
        receive = (TextView) this.findViewById(R.id.receive);
        send = (TextView) this.findViewById(R.id.send);
    }

    private void main() {
        mSerialMulti = new PL2303MultiLib((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);
        iDeviceCount = mSerialMulti.PL2303Enumerate();

        //获得usb设备的数量
        if (0 == iDeviceCount) {
            Toast.makeText(this, "没有找到可以运行的设备！请插入设备后重启程序！", Toast.LENGTH_SHORT).show();
            return;
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(mSerialMulti.PLUART_MESSAGE);
            registerReceiver(PLMultiLibReceiver, filter);
            Toast.makeText(this, "找到 " + iDeviceCount + " 个可运行的设备！", Toast.LENGTH_SHORT)    .show();
        }//if( 0==iDevCnt )

        //循环创建 mSerialMulti 直到mSerialMulti不为空
        if (mSerialMulti == null||!mSerialMulti.PL2303IsDeviceConnectedByIndex(0)) {
            return;
        }

        UARTSettingInfo info = new UARTSettingInfo();
        PL2303MultiLib.BaudRate rate;

        //设置接收器的参数 以及 开启usb接收
        rate = PL2303MultiLib.BaudRate.B9600;
        info.mBaudrate = rate;
        int res = 0;
        try {
            res = mSerialMulti.PL2303SetupCOMPort(0, info.mBaudrate, info.mDataBits, info.mStopBits, info.mParity, info.mFlowControl);
            gUARTInfoList = info;
            OpenUARTDevice(DeviceIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res < 0) {
            return;
        }
    }

    protected void onDestroy() {
        if (mSerialMulti != null) {
            gThreadStop = true;
            if (iDeviceCount > 0)
                unregisterReceiver(PLMultiLibReceiver);
            mSerialMulti.PL2303Release();
            mSerialMulti = null;
        }
        super.onDestroy();
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
            Toast.makeText(getApplicationContext(), "mSerialMulti为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mSerialMulti.PL2303IsDeviceConnectedByIndex(index)) {
            Toast.makeText(getApplicationContext(), "mSerialMulti和设备没有链接", Toast.LENGTH_LONG).show();
            return;
        }
        boolean res;
        UARTSettingInfo info = gUARTInfoList;
        res = mSerialMulti.PL2303OpenDevByUARTSetting(index, info.mBaudrate, info.mDataBits, info.mStopBits, info.mParity, info.mFlowControl);
        if (!res) {
            Toast.makeText(this, "Can't set UART correctly!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!gRunningReadThread) {
            UpdateDisplayView(index);
        }
        Toast.makeText(this, "成功打开设备：[" + mSerialMulti.PL2303getDevicePathByIndex(index) + "]", Toast.LENGTH_SHORT).show();
        return;
    }//private void OpenUARTDevice(int index)

    private void UpdateDisplayView(int index) {
        gThreadStop = false;
        gRunningReadThread = true;
        new Thread(ReadLoop1).start();
    }

    /**
     * 从usb接受流文件信息模块
     */

    private String out_str = "";
    private int ReadLen1 = 0;
    private byte[] ReadBuf1 = new byte[4096];
    Handler mHandler1 = new Handler();
    private Runnable ReadLoop1 = new Runnable() {
        public void run() {
            for (; ; ) {
                ReadLen1 = mSerialMulti.PL2303Read(DeviceIndex, ReadBuf1);
                System.out.println("接受信号："+ ReadLen1);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ReadLen1 > 0) {
                    mHandler1.post(new Runnable() {
                        public void run() {
                            StringBuffer sbHex = new StringBuffer();
                            for (int j = 0; j < ReadLen1; j++) {
                                String hex = Integer.toHexString(ReadBuf1[j] & 0xFF);
                                if (hex.length() == 1) {
                                    hex = '0' + hex;
                                }
                                sbHex.append(hex);
                            }
                            out_str = out_str + sbHex.toString();
                        }//run
                    });//Handler.post
                }else{
                    if(!out_str.equals("")){
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg",out_str);
                        message.setData(bundle);
                        getReturnTextHandler.sendMessage(message);
                        out_str = "";
                    }
                }
                DelayTime(60);
                if (gThreadStop) {
                    gRunningReadThread = false;
                    return;
                }//if
            }//for(...)

        }//run
    };//Runnable

    /**
     * 命令写入
     */

    private void WriteToUARTDevice(int index) {

    }

    private void DelayTime(int dwTimeMS) {
        //Thread.yield();
        long StartTime, CheckTime;

        if (0 == dwTimeMS) {
            Thread.yield();
            return;
        }
        //Returns milliseconds running in the current thread
        StartTime = System.currentTimeMillis();
        do {
            CheckTime = System.currentTimeMillis();
            Thread.yield();
        } while ((CheckTime - StartTime) <= dwTimeMS);
    }

    @Override
    public void onClick(View v) {
        if(item_1_panel.getId() == v.getId()){
            setValue(item_1_state);
            setState(item_1_state);
        }else if(item_2_panel.getId() == v.getId()){
            setValue(item_2_state);
            setState(item_2_state);
        }else if(item_3_panel.getId() == v.getId()){
            setValue(item_3_state);
            setState(item_3_state);
        }else if(button1.getId() == v.getId()){
            info_type = 1;
            if(!thread.isAlive()){
                thread.start();
            }
        }else if(button2.getId() == v.getId()){
            info_type = 2;
            if(!thread.isAlive()){
                thread.start();
            }
        }
    }




    private void send_message(int z){
        //编辑信息行
        sendList.add("3C");
        sendList.add("21");
        if(z == 1){
            sendList.add("05");
        }else if(z == 2){
            sendList.add("06");
        }
        sendList.add("01");
        sendList.add(new Unit().get_count(3));
        if(z == 1){
            sendList.add("03");
        }else if(z == 2){
            sendList.add("02");
        }
        sendList.add(state[0]);
        sendList.add(state[1]);
        sendList.add(state[2]);

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
        sendList.add("3E");

        //合成写入的信息行
        String S = "";
        for(int i = 0 ; i < sendList.size();i++){
            S = S + sendList.get(i);
        }
        simple_str = S;

        //信息行处理
        send.setText(sendList.toString());
        sendList.clear();

        //写入命令
        if (mSerialMulti == null)
            return;
        if (!mSerialMulti.PL2303IsDeviceConnectedByIndex(0))
            return;
        byte[] bt = hexStringToBytes(simple_str);
        byte[] Sendbytes = Arrays.copyOf(bt, bt.length);
        int res = mSerialMulti.PL2303Write(0, Sendbytes);   // sendbtyes 为输入的命令
        if (res < 0) {
            return;
        }
    }

    private void setValue(TextView view){
        if(view.getTag().equals("0")){
            view.setTag("1");
            view.setText(str[1]);
        } else if(view.getTag().equals("1")){
            view.setTag("2");
            view.setText(str[2]);
        } else if(view.getTag().equals("2")){
            view.setTag("0");
            view.setText(str[0]);
        }
    }

    private void setState(TextView view){
        if(view.getId() == item_1_state.getId()){
            if(view.getTag().equals("0")){
                state[0] = "01";
            }else  if(view.getTag().equals("1")){
                state[0] = "00";
            }else  if(view.getTag().equals("2")){
                state[0] = "02";
            }
        }else if(view.getId() == item_2_state.getId()){
            if(view.getTag().equals("0")){
                state[1] = "01";
            }else  if(view.getTag().equals("1")){
                state[1] = "00";
            }else  if(view.getTag().equals("2")){
                state[1] = "02";
            }
        }else if(view.getId() == item_3_state.getId()){
            if(view.getTag().equals("0")){
                state[2] = "01";
            }else  if(view.getTag().equals("1")){
                state[2] = "00";
            }else  if(view.getTag().equals("2")){
                state[2] = "02";
            }
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
