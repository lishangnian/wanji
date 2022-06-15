package com.enjoy.wanji.entity;

import android.os.SystemClock;

public class DataStorageToPC {

    //topic app
    public static volatile int stopgo = 0; //无效、出发、停止 0 1 2
    public static volatile int zoneName = 0;  //园区名称
    public static volatile int apsNum = 0;  //对应泊车点，该数字如果大于配置文件中泊车点数量，取第一个泊车点
    public static volatile int eStop = 0;  //触发临时停车  每按一下，切换状态，0-1来回切换
    private static volatile int park = 0;  //泊车 触发加载回停车场地图  发5秒1，再变0触发加载回停车场地图，按钮
    public static void setPark(){
        park = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(5000);
                park = 0;
            }
        }).start();
    }
    public static int getPark(){
        return park;
    }



}
