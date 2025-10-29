package com.enjoy.wanji.service;

import android.util.Log;


import com.enjoy.wanji.Global;
import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorage;
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
//            String str = MyStringUtil.sendDataStr(TopicAndParams.topicSendApp, dataMap);
            String str = MyStringUtil.sendDataStr(TopicAndParams.topicSendCloudApp, dataMap);
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

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvTrafficPart); //交通参与者
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvV2xapp); // vtox
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvLonlatmMappoints); // lonlatpoints
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvCloudLight); // 云端数据
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);

        str = MyStringUtil.subscribeTopicStr(TopicAndParams.topicRecvCloudPath); // 云端
        client.send(str);
        Log.i(tag, "发送订阅消息" + str);
    }
}
