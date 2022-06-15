package com.enjoy.wanji.entity;

public class AttentionInfo {

    public static String title = "";
    public static String message = "";
    //1--连接成功 2--连接失败   3--进入自驾  4--退出自驾 5--刹车注意   6--故障报警  7--v2x提示
    public static int type = 0;

    public static int attentionKey = 0;  //具体的信息对应的Key

    public static volatile long timestamp = 0;  //弹框弹出时的时间戳

}
