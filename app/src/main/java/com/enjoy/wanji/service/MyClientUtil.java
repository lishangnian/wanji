package com.enjoy.wanji.service;

import android.util.Log;


import com.enjoy.wanji.Global;
import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorage;
import com.enjoy.wanji.entity.DataStorageCollectMap;
import com.enjoy.wanji.entity.DataStorageToPC;
import com.enjoy.wanji.util.MyStringUtil;

import org.java_websocket.client.WebSocketClient;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyClientUtil {

    static String tag = "sendTag";


    public static void send(WebSocketClient client) {
        if (client == null || !Global.connectFlag) {
            Log.i(tag, "client is not connect");
            return;
        }
        Map<String, Object> dataMap = new LinkedHashMap<>();
        if (DataStorage.page == 1) {  //在第一界面
            //topic app
            dataMap.put(TopicAndParams.paramStopGoTopicApp, DataStorageToPC.stopgo);
            dataMap.put(TopicAndParams.paramZoneNameTopicApp, DataStorageToPC.zoneName);
            dataMap.put(TopicAndParams.paramApsNumTopicApp, DataStorageToPC.apsNum);
            dataMap.put(TopicAndParams.paramEstopTopicApp, DataStorageToPC.eStop);  //触发临时停车
            dataMap.put(TopicAndParams.paramParkTopicApp, DataStorageToPC.getPark());  //触发加载回停车场地图
            dataMap.put(TopicAndParams.timestamp, System.currentTimeMillis());
            String str = MyStringUtil.sendDataStr(TopicAndParams.topicSendApp, dataMap);
            client.send(str);
            dataMap.clear();
            Log.i(tag, "app send " + str);
        }

        if (Global.loadRoadsFlag || Global.deleteRoadFlag) {  //请求园区和地图或删除地图
            int request = 1;
            if (Global.deleteRoadFlag) {
                request = 3;
            }
            dataMap.put(TopicAndParams.paramRequestTopicRequestMap, request);  //0：不请求 1：请求全部地图；2：请求指定地图;3: 删除地图;4: 请求泊车点
            dataMap.put(TopicAndParams.paramMapNameTopicRequestMap, 0);  //指定的地图名
            dataMap.put(TopicAndParams.timestamp, System.currentTimeMillis());
            String roadData = MyStringUtil.sendDataStr(TopicAndParams.topicSendRequestMap, dataMap);
            client.send(roadData);
            dataMap.clear();
            Global.loadRoadsFlag = Global.loadRoadsFlag ? false : false;
            Global.deleteRoadFlag = Global.deleteRoadFlag ? false : false;
            Log.i(tag, "app request maps " + roadData);
        }

    }

    /**
     * 采集轨迹发送
     *
     * @param client
     */
    public static void collectMap(WebSocketClient client) {
        Map<String, Object> dataMap = new LinkedHashMap<>();

        //地图采集结束后，会重新请求园区
        if (Global.loadRoadsFlag) {  //请求园区
            int request = 1;

            dataMap.put(TopicAndParams.paramRequestTopicRequestMap, request);  //0：不请求 1：请求全部地图；2：请求指定地图;3: 删除地图;4: 请求泊车点
            dataMap.put(TopicAndParams.paramMapNameTopicRequestMap, 0);  //指定的地图名
            dataMap.put(TopicAndParams.timestamp, System.currentTimeMillis());
            String roadData = MyStringUtil.sendDataStr(TopicAndParams.topicSendRequestMap, dataMap);
            client.send(roadData);
            dataMap.clear();
            Global.loadRoadsFlag = Global.loadRoadsFlag ? false : false;
            Global.deleteRoadFlag = Global.deleteRoadFlag ? false : false;
            Log.i(tag, "app request maps " + roadData);

            Global.loadRoadsFlag = false;
        }


        if (DataStorageCollectMap.zoneName == 0 || DataStorageCollectMap.collectMode == 0) {
            return;
        }

        //轨迹采集
        dataMap.put(TopicAndParams.paramMapNameTopicCollectMap, DataStorageCollectMap.mapName);
        dataMap.put(TopicAndParams.paramZoneNameTopicCollectMap, DataStorageCollectMap.zoneName);
        dataMap.put(TopicAndParams.paramRoadPropertyTopicCollectMap, DataStorageCollectMap.roadProperty);
        dataMap.put(TopicAndParams.paramLaneAttrTopicCollectMap, DataStorageCollectMap.laneattr);
        dataMap.put(TopicAndParams.paramSideRoadWidthTopicCollectMap, DataStorageCollectMap.sideroadwidth);
        dataMap.put(TopicAndParams.paramCollectMapMergelaneTypeTopicCollectMap, DataStorageCollectMap.mergelanetype);
        dataMap.put(TopicAndParams.paramLeftSearchdisTopicCollectMap, DataStorageCollectMap.leftsearchdis); //左右安全距离
        dataMap.put(TopicAndParams.paramRightsearchdisTopicCollectMap, DataStorageCollectMap.rightsearchdis);

        dataMap.put(TopicAndParams.paramExpectSpeedTopicCollectMap, DataStorageCollectMap.exSpeed);
        dataMap.put(TopicAndParams.paramLeftLaneWidthTopicCollectMap, DataStorageCollectMap.leftWidthDis); //左道路宽
        dataMap.put(TopicAndParams.paramRightLaneWidthTopicCollectMap, DataStorageCollectMap.rightWidthDis); //右道路宽
        dataMap.put(TopicAndParams.paramLaneStatusTopicCollectMap, DataStorageCollectMap.laneStatus);  //道路状态
        dataMap.put(TopicAndParams.paramLaneWidthTopicCollectMap, DataStorageCollectMap.laneWidth);  //道路宽

        dataMap.put(TopicAndParams.paramLaneSwitchTopicCollectMap, DataStorageCollectMap.laneSwitch); //换道标志
        dataMap.put(TopicAndParams.paramSidePassTopicCollectMap, DataStorageCollectMap.sidePass); //借道标志
        dataMap.put(TopicAndParams.paramLaneNumTopicCollectMap, DataStorageCollectMap.laneNum); //车道总数
        dataMap.put(TopicAndParams.paramLaneSiteTopicCollectMap, DataStorageCollectMap.laneSite); //所在第几车道

        dataMap.put(TopicAndParams.timestamp, System.currentTimeMillis());
        String str = MyStringUtil.sendDataStr(TopicAndParams.topicSendCollectMap, dataMap);
        client.send(str);
        dataMap.clear();
        Log.i(tag, "collectMap send " + str);

        //点采集
        dataMap.put(TopicAndParams.paramZoneNameTopicCollectPoint, DataStorageCollectMap.zoneName);
        dataMap.put(TopicAndParams.paramIndexTopicCollectPoint, DataStorageCollectMap.index);
        dataMap.put(TopicAndParams.paramStopTimeTopicCollectPoint, DataStorageCollectMap.stopTime);
        dataMap.put(TopicAndParams.paramPropertyTopicCollectPoint, DataStorageCollectMap.stopProperty);
        dataMap.put(TopicAndParams.paramOrientationTopicCollectPoint, DataStorageCollectMap.stopOrientation);
        str = MyStringUtil.sendDataStr(TopicAndParams.topicSendCollectPoint, dataMap);
        client.send(str);
        dataMap.clear();
        Log.i(tag, "collectPoints send " + str);
    }


    public static void subscribe(WebSocketClient client) {
        if (client == null || !Global.connectFlag) {
            return;
        }
        String str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvActuator);
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvSensorgps);
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);


        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvControllon); //障碍物
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

//        str = MyStringUtil.subscribeTopicStr(TopicAndParams.recvTopicGlobalPlanning); // 轨迹点
//        client.send(str);
//        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvV2xapp); // vtox
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvLonlatmMappoints); // lonlatpoints
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);


    }
}
