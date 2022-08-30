package com.enjoy.wanji.service;


import android.util.Log;

import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorageFromPC;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageHandle {

    static String tag = "msgTag";

    public static void handle(String topicName, JSONObject jsonObj) {
        switch (topicName) {
            case TopicAndParams.topicRecvSensorgps:
                long rtkStatus = (long) jsonObj.get("status");     //定位状态  4-好；5-一般；0-差
                double lon = (double) jsonObj.get("lon");
                double lat = (double) jsonObj.get("lat");
                String heading = String.valueOf((double) jsonObj.get("heading"));

                DataStorageFromPC.lon = lon;
                DataStorageFromPC.lat = lat;
                DataStorageFromPC.heading = heading;
                DataStorageFromPC.rtk = rtkStatus;
                break;
            case TopicAndParams.topicRecvActuator:
                Log.i(tag, "收到驾驶状态信息" + jsonObj.toString());
                int speedInt = (int) ((double) jsonObj.get("speed") * 3.6);
                String speed = speedInt >= 10 ? speedInt + "" : "0" + speedInt + "";
                int driverStatus = Integer.valueOf(jsonObj.get("sysstatus").toString());  //驾驶状态 0-人工； 1-自动
                int error = Integer.valueOf(jsonObj.get("error").toString());   //2，故障等级2 语音提示加弹框
                Object socObj = jsonObj.get("soc");

                if (socObj != null) {
                    float soc = Float.parseFloat(socObj.toString());
                    int socInt = (int) soc;
                    if (socInt < 10) {
                        DataStorageFromPC.soc = "0" + socInt + "%";
                    } else {
                        DataStorageFromPC.soc = socInt + "%";
                    }
                }

                if (DataStorageFromPC.driverStatus != driverStatus) {  //跳变
                    if (DataStorageFromPC.driverStatus > 0) {   //当前
                        DataStorageFromPC.driverStatusTip = 2;//退出自驾
                    } else {
                        DataStorageFromPC.driverStatusTip = 1;//进入自驾
                    }
                }

                DataStorageFromPC.driverStatus = driverStatus;

                DataStorageFromPC.speedStr = speed + "km/h";
                DataStorageFromPC.error = error;
                DataStorageFromPC.velocity = speedInt;


                break;
            case TopicAndParams.topicRecvLonlatmMappoints:        //轨迹点

                String zoneName = jsonObj.get("zonename").toString();  //园区名
//                jsonObj.get(" mapname").toString(); //轨迹名
                JSONArray pointsArray = (JSONArray) jsonObj.get("points");  //轨迹点

                if (pointsArray == null || pointsArray.isEmpty()) {
                    Log.e(tag, "接收轨迹点为空");
                }
                DataStorageFromPC.roadsMap.put(zoneName, jsonObj);

                //把同一个园区内的轨迹放在一个list中归类
                List<JSONObject> roadsJsonList = DataStorageFromPC.zoneNameJsonListMap.get(zoneName);
                if (roadsJsonList == null) {
                    roadsJsonList = new ArrayList<>();
                    roadsJsonList.add(jsonObj);
                }
                DataStorageFromPC.zoneNameJsonListMap.put(zoneName, roadsJsonList);
                break;
            case TopicAndParams.topicRecvControllon:  //获取障碍物距离
                double objDis = (double) jsonObj.get("objdis");  //单位米
                //刹车注意，当actuator发出的自动驾驶状态为1，且acc 由大于零跳变成小于零时触发
                int brakePedal = Integer.parseInt(jsonObj.get("brakePedal").toString());
                String objDisStr = String.format("%.1f", objDis);   //保留一位小数
                if (objDis >= 100) {
                    DataStorageFromPC.objDis = "---m";
                } else if (objDis < 10) {
                    DataStorageFromPC.objDis = "0" + objDisStr + "m";
                } else {
                    DataStorageFromPC.objDis = objDisStr + "m";
                }
                if (DataStorageFromPC.driverStatus > 0 && DataStorageFromPC.velocity / 3.6 > 1) {  //自驾状态 且速度大于1m/s

                    //uint8    brakePedal  当actuator发出的自动驾驶状态为1，且车速大于1m/s，且brakepedal 由等于零跳变成大于零时触发
                    if (DataStorageFromPC.brakePadel == 0 && brakePedal > 0) {
                        DataStorageFromPC.accBrake = 1;  //刹车
                    }
                }
                DataStorageFromPC.brakePadel = brakePedal;
                break;
            case TopicAndParams.topicRecvV2xapp: //V2x  红绿灯和限速
                //  trafficLight  0:无 1：红灯 2：绿灯 3：黄灯
//
                int v2xType = Integer.valueOf(jsonObj.get("v2xtype").toString());  //类型
                int trafficLight = Integer.valueOf(jsonObj.get("color").toString());
                int speedLimit = Integer.valueOf(jsonObj.get("speedlimit").toString());
                DataStorageFromPC.lightColor = trafficLight;
                DataStorageFromPC.v2xType = v2xType;
                break;
        }
    }
}
