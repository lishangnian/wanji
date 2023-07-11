package com.enjoy.wanji.entity;

public class DataStorageCollectMap {


    public static int collectMode = 0; //采集地图模式     0--否     1--开始采集   2---结束采集
    public static int mapName = 1;  //地图名称  ---即编号
    public static int zoneName = 0;  //园区名称  ---即编号
    public static int roadProperty = 0; //设定的地图属性 property
    public static int laneattr = 1; //车道   1--4 单道， 右车道、中间车道、左车道
    public static float sideroadwidth = 0f;//临时停车距离
    public static int mergelanetype = 1;//切换道路属性，1：默认 2：切换SLAM  3：左并道   4：右并道
    public static float leftsearchdis = 1.7f; //  左安全距离，默认1.7
    public static float rightsearchdis = 1.7f;  //右安全距离，默认1.7

    public static int exSpeed = 7;  //期望速度
    public static float laneWidth = 3.5f;  //道路宽度
    //public static float temporaryStop = 0f;  //临时停车
    public static int laneStatus = 1; //道路状态
    public static float leftWidthDis = 3.5f;  //左道路宽
    public static float rightWidthDis = 3.5f;  //右道路宽
//    public static float laneStartNo = 1;  //起点编号
//    public static float laneEndNo = 1;  //终点编号

    //时间戳

    //对停车点的采集
    public static int index = 1;  //索引值（默认1  1-10）
    public static int stopTime = 5;  //停车时间单位S（只针对站点停车）
    public static int stopProperty = 0;   //停车属性  0：不采集      1:垂直泊车点     2:水平泊车点     3:站点停车点     4：红绿灯停车点
    public static int stopOrientation = 0;   //泊车位置，默认 0--右  1--左


}
