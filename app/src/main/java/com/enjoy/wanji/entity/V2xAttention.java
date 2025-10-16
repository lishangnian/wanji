package com.enjoy.wanji.entity;

public class V2xAttention {

    public static String title = "";
    public static String message = "";
    //0--无 1--云端打开  2--云端关闭   3--碰撞风险
    public static int v2xType = 0;

    public static volatile long timestamp = 0;  //弹框弹出时的时间戳
}
