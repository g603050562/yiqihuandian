package com.xinyu.ElectricCabinet.main;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by hasee on 2017/3/23.
 * 主要功能就是 完全退出APP
 */

public class MyApplication extends Application {
    private List<Activity> activitys = null;
    private static MyApplication instance;
    public static int is_wifi = 0;

    private MyApplication() {
        activitys = new LinkedList<Activity>();
    }
    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return
     */
    public static MyApplication getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }
    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        if (activitys != null && activitys.size() > 0) {
            if(!activitys.contains(activity)){
                activitys.add(activity);
            }
        }else{
            activitys.add(activity);
        }
    }
    // 遍历所有Activity并finish
    public void exit() {
        if (activitys != null && activitys.size() > 0) {
            for (Activity activity : activitys) {
                activity.finish();
            }
        }
        System.exit(0);
    }
}

