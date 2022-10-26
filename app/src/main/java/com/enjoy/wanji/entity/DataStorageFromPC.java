package com.enjoy.wanji.entity;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataStorageFromPC {

    public static Lock appMsgSendLock = new ReentrantLock(); //锁，当点击选择作业区域时，加锁，避免路径还没自动切换
    //appMsg把旧的路线发送给ros

    public static volatile double lon = 0;
    public static volatile double lat = 0;
    public static String heading = "0";
    public static String speedStr = "00km/h";
    public static int velocity = 0;  //速度数值
    public static volatile long rtk = 0;
    public static String objDis = "---m";  //障碍物距离单位
    public static float brakePadel = 0f;   //刹车信号
    public static int accBrake = 0;  //刹车提醒  0-无  1-有
    //v2x
    public static double lightColor = 0;  //0:无 1：红灯 2：绿灯 3：黄灯
    public static int v2xType = 0;  //默认0  1--16各类预警(具体看表)  2---

    public static int mode = 1; //app模式 1--显示订阅的信息模式      2--采集地图模式

    //障碍物标志
    public static int obs = 0;    //0 -- 无   1--有
    public static int driverStatusTip = 0;      //自驾状态跳变，0---无 1--进入自动驾驶 //退出自驾
    //驾驶状态标志
    public static int driverStatus = 0;      //0--非自动驾驶  1-自动驾驶
    //故障等级  语音提示加弹窗   为两位或三位，第一位是故障分类 后两位是故障码，具体见文档
    public static int error = 0;

    public static volatile long actuatorTimeStamp = System.currentTimeMillis();  //actuator的时间戳
    private static int carWorkMode = 0;   //0--等待中 1,清扫       2,去车库       3,去垃圾站
    public static volatile String battery = "0V";   //电压
    public static volatile int batterySoc = 0; //电量
    public static volatile String soc = "00%";   //电量
    private static long carWorkModeUpdateStamp = System.currentTimeMillis();
    public static volatile int pathError = 0; //报警   0--无   1---报警
    public static volatile int sysError = 0;//故障报警 gps无数据或gps状态为0--1  激光雷达无数据--2   毫米波雷达无数据--3  超声波雷达无数据--4  无数据--5-10
    public static volatile int carError = 0; //actor 中的error


    //所有路径地图的json集合
    public static volatile Map<String, JSONObject> roadsMap = new LinkedHashMap<>();     //使用LinkedHashMap会按插入顺序排列, key为轨迹园区名
    //key是园区   value是list，存放园区里各个的轨迹
    public static volatile Map<String, List<JSONObject>> zoneNameJsonListMap = new LinkedHashMap<>();
}
