package com.xinyu.ElectricCabinet.pub;

/**
 * Created by hasee on 2017/3/13.
 */
public class State {

    private static String[] battery_state = new String[]{"0000","0400","c201","4102"};

    /*
    0000：没有此仓
    0400：充电机失联，开门
    1400：充电机失联，关门
    c201：充电，开门
    4102：满电（电池组高压状态），开门
    5102：满点（电池组高压状态），关门
    0c00：。。。（！！！有问题）
    */


}
