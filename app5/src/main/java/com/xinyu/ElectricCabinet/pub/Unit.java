package com.xinyu.ElectricCabinet.pub;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinyu.ElectricCabinet.R;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by hasee on 2016/12/30.
 */
public class Unit {

    private static String mYear;
    private static String mMonth;
    private static String mDay;
    private static String mWay;

    public static String[] barId = null;
    public static int[] barSta = null;

    //返回发送帧的 crc校验位
    public int get_crc(int[] bt){
        int m = 0;
        for(int i = 0 ; i < bt.length;i++){
            if(i == 0){
                m = bt[i];
            }else{
                m = m^bt[i];
            }
        }
        return  m;
    }

    //返回 16位进制 大写   LEN
    public String get_count(int m){
        int n = 0;
        n = 8 + m;
        String a =  Integer.toHexString(n);
        String A =  a.toUpperCase();
        if(A.length() == 1){
            A = "0"+ A;
        }
        return A;
    }

    //返回 16位进制 大写   LEN
    public String get_car_count(int m){
        int n = m;
        String a =  Integer.toHexString(n);
        String A =  a.toUpperCase();
        if(A.length() == 1){
            A = "0"+ A;
        }
        return A;
    }

    //返回 日期 星期
    public static String StringData(){
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if("1".equals(mWay)){
            mWay ="天";
        }else if("2".equals(mWay)){
            mWay ="一";
        }else if("3".equals(mWay)){
            mWay ="二";
        }else if("4".equals(mWay)){
            mWay ="三";
        }else if("5".equals(mWay)){
            mWay ="四";
        }else if("6".equals(mWay)){
            mWay ="五";
        }else if("7".equals(mWay)){
            mWay ="六";
        }
        return  mMonth + "月" + mDay+"日"+" 星期"+mWay;
    }

    // 16转10
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

    public static String hexString2binaryString(String hexString)
    {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++)
        {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    //md5加密
    public static String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setBarProgram(ImageView imageView_1,ImageView imageView_2,TextView textView,int count){
        setBarImage(imageView_1,count);
        setRoundImage(imageView_2,count);
        setTextColor(textView, count);
    }


    private void setBarImage(ImageView imageView,int count){
        if(count >= 0 && count < 10){
            imageView.setImageResource(R.drawable.p_10);
        }else if(count >= 10 && count < 20){
            imageView.setImageResource(R.drawable.p_10);
        }else if(count >= 20 && count < 30){
            imageView.setImageResource(R.drawable.p_20);
        }else if(count >= 30 && count < 40){
            imageView.setImageResource(R.drawable.p_30);
        }else if(count >= 40 && count < 50){
            imageView.setImageResource(R.drawable.p_40);
        }else if(count >= 50 && count < 60){
            imageView.setImageResource(R.drawable.p_50);
        }else if(count >= 60 && count < 70){
            imageView.setImageResource(R.drawable.p_60);
        }else if(count >= 70 && count < 80){
            imageView.setImageResource(R.drawable.p_70);
        }else if(count >= 80 && count < 90){
            imageView.setImageResource(R.drawable.p_80);
        }else if(count >= 90 && count < 100){
            imageView.setImageResource(R.drawable.p_90);
        }else if(count == 100){
            imageView.setImageResource(R.drawable.p_100);
        }
    }
    private void setRoundImage(ImageView imageView,int count){
        if(count < 33){
            imageView.setImageResource(R.drawable.pr_1);
        }else if(count >= 33 && count < 66){
            imageView.setImageResource(R.drawable.pr_2);
        }else if(count >= 66){
            imageView.setImageResource(R.drawable.pr_3);
        }
    }

    private void setTextColor(TextView textView,int count){
        if(count < 33){
            textView.setTextColor(0xff03aac6);
        }else if(count >= 33 && count < 66){
            textView.setTextColor(0xff538d2d);
        }else if(count >= 66){
            textView.setTextColor(0xffe8db07);
        }
    }

    //判读是否有网
    public static boolean isNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
//                    System.out.println(i + "===状态===" + networkInfo[i].getState());
//                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }                }
            }
        }
        return false;
    }
}
