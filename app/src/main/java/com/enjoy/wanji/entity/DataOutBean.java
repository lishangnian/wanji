package com.enjoy.wanji.entity;

import android.os.SystemClock;

public class DataOutBean {

    //地图编号，默认0
    private static volatile int MAP_NUM = 0;
    private static volatile int MAP_NUM_SEND = 0;

    public static void setMapNum(int num) {
        MAP_NUM = num;
        MAP_NUM_SEND = num;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //3秒后发送的轨迹编号设为0；
                SystemClock.sleep(3000);
                MAP_NUM_SEND = 0;
            }
        }).start();
    }

    public static int getMapNumSend() {
        return MAP_NUM_SEND;
    }

    public static int getMapNum() {
        return MAP_NUM;
    }
}
