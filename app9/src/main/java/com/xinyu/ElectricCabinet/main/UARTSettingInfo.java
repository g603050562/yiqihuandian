package com.xinyu.ElectricCabinet.main;

import tw.com.prolific.pl2303multilib.PL2303MultiLib;

/**
 * Created by hasee on 2017/3/7.
 */
public class UARTSettingInfo {
    public PL2303MultiLib.BaudRate mBaudrate = PL2303MultiLib.BaudRate.B115200;
    public PL2303MultiLib.DataBits mDataBits = PL2303MultiLib.DataBits.D8;
    public PL2303MultiLib.Parity mParity = PL2303MultiLib.Parity.NONE;
    public PL2303MultiLib.StopBits mStopBits = PL2303MultiLib.StopBits.S1;
    public PL2303MultiLib.FlowControl mFlowControl = PL2303MultiLib.FlowControl.OFF;
}
