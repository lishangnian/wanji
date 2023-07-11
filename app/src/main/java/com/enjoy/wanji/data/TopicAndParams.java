package com.enjoy.wanji.data;

public class TopicAndParams {

    public static final String topicRecvSensorgps = "/sensorgps"; //订阅消息TASKRE
    public static final String topicRecvLoadMapsName = "/lonlatmap"; //订阅加载地图TOPIC
    public static final String topicRecvBehaviordecision = "/behaviordecision"; //订阅障碍物消息topic
    public static final String topicRecvActuator = "/actuator";   //订阅驾驶状态topic
    public static final String topicRecvControllon = "/controllon";   //订阅障碍物topic
    public static final String topicRecvGlobalPlanning = "/globalplanning";   //订阅
    public static final String topicRecvLonlatmMappoints = "/lonlatmappoints";  //轨迹点topic
    public static final String topicRecvV2xapp = "/v2xapp";


    public static String topicSendApp = "/app"; //TASK 发送停止或出发、地图编号
    public static String topicSendAppMs = "/appMsg"; //TASK 发送停止或出发、地图编号
    public static String topicSendRequestMap = "/requestmap";
    public static String topicSendCollectMap = "/collectmap";
    public static String topicSendCollectPoint = "/collectpoint";

    //地图请求
    public static String paramRequestTopicRequestMap = "\"request\":";  // 0：不请求 1：请求全部地图；2：请求指定地图;3: 删除地图;4: 请求泊车点
    public static String paramMapNameTopicRequestMap = ",\"mapname\":";   //地图名

    //app topicAppParam
    public static String paramStopGoTopicApp = "\"stopgo\":";
    public static String paramZoneNameTopicApp = ",\"zonename\":";
    public static String paramApsNumTopicApp = ",\"apsnum\":";  //决策加载对应泊车点
    public static String paramParkTopicApp = ",\"park\":"; //停车点
    public static String paramEstopTopicApp = ",\"estop\":"; //  触发临时停车


    //采集轨迹部分 param
    public static String paramMapNameTopicCollectMap = "\"mapname\":";
    public static String paramZoneNameTopicCollectMap = ",\"zonename\":";
    public static String paramRoadPropertyTopicCollectMap = ",\"property\":";  //即属性
    public static String paramLaneAttrTopicCollectMap = ",\"laneattr\":";
    public static String paramSideRoadWidthTopicCollectMap = ",\"sideroadwidth\":"; //临时停车距离
    public static String paramCollectMapMergelaneTypeTopicCollectMap = ",\"mergelanetype\":"; //切换道路属性，0直行，1左并道，2右并道
    public static String timestamp = ",\"timestamp\":";
    public static String paramLeftSearchdisTopicCollectMap = ",\"leftsearchdis\":";   //左安全距离，默认1.7
    public static String paramRightsearchdisTopicCollectMap = ",\"rightsearchdis\":";   //右安全距离，默认1.7

    public static String paramExpectSpeedTopicCollectMap = ",\"speed\":";   //期望速度
    public static String paramLaneStatusTopicCollectMap = ",\"sensorlanetype\":";   //道路状态
//    public static String paramTemporaryStopTopicCollectMap = ",\"sideroadwidth\":";   //临时停车
    public static String paramLaneWidthTopicCollectMap = ",\"lanewidth\":";   //道路宽度
    public static String paramLeftLaneWidthTopicCollectMap = ",\"leftlanewidth\":";  //左道路宽度
    public static String paramRightLaneWidthTopicCollectMap = ",\"rightlanewidth\":";  //右道路宽度
//    public static String paramLaneStartNoTopicCollectMap = ",\"start\":";  //起点编号
//    public static String paramLaneEndNoTopicCollectMap = ",\"end\":";  //终点编号



    //    collectpoint param
    public static String paramZoneNameTopicCollectPoint = "\"zonename\":";
    public static String paramIndexTopicCollectPoint = ",\"index\":";
    public static String paramStopTimeTopicCollectPoint = ",\"stoptime\":";
    public static String paramPropertyTopicCollectPoint = ",\"property\":";
    public static String paramOrientationTopicCollectPoint = ",\"orientation\":";

}
