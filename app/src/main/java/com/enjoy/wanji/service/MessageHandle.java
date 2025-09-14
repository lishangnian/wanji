package com.enjoy.wanji.service;


import android.util.Log;

import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.vr3D.ContainerObject3D;
import com.enjoy.wanji.vr3D.ModelAgent;
import com.enjoy.wanji.vr3D.TrafficObj;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageHandle {

    static String tag = "msgTag";

    public static void handle(String topicName, JSONObject jsonObj) {
        switch (topicName) {
            case TopicAndParams.topicRecvSensorgps:
//                long rtkStatus = (long) jsonObj.get("status");     //定位状态  4-好；5-一般；0-差
                double lon = (double) jsonObj.get("lon");
                double lat = (double) jsonObj.get("lat");
                String heading = String.valueOf((double) jsonObj.get("heading"));

                DataStorageFromPC.lon = lon;
                DataStorageFromPC.lat = lat;
                DataStorageFromPC.heading = heading;
//                DataStorageFromPC.rtk = rtkStatus;
                break;
            case TopicAndParams.topicRecvActuator:
                Log.i(tag, "收到驾驶状态信息" + jsonObj.toString());
                int speedInt = (int) ((double) jsonObj.get("speed") * 3.6);
                int driverStatus = Integer.valueOf(jsonObj.get("sysstatus").toString());  //驾驶状态 0-人工； 1-自动

                int gear = Integer.valueOf(jsonObj.get("gear").toString()); //档位 0-P  1-R  2-N  3-D
                int turnLight = Integer.valueOf(jsonObj.get("turnLight").toString());  //转向 0--无  1--左转  2--右转

               // int error = Integer.valueOf(jsonObj.get("error").toString());   //2，故障等级2 语音提示加弹框
                Object socObj = jsonObj.get("soc");

                if (socObj != null) {
                    float soc = Float.parseFloat(socObj.toString());
                    int socInt = (int) soc;
                    DataStorageFromPC.batterySoc = socInt;
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
                //档位 0-P  1-R  2-N  3-D
                if (gear == 0){
                    DataStorageFromPC.Gear = "P";
                }else if (gear == 1){
                    DataStorageFromPC.Gear = "R";
                } else if (gear == 2){
                    DataStorageFromPC.Gear = "N";
                } else if (gear == 3){
                    DataStorageFromPC.Gear = "D";
                }
                //转向 0--无  1--左转  2--右转
                DataStorageFromPC.turnLight = turnLight;
                DataStorageFromPC.driverStatus = driverStatus;
                DataStorageFromPC.speedStr = String.valueOf(speedInt);
//                DataStorageFromPC.error = error;
                DataStorageFromPC.velocity = speedInt;
                break;
            case TopicAndParams.topicRecvTrafficPart:       //交通参与者，3D动画
                if (EnjoySocketService.UpdateUIModelLock.tryLock()){   //先获取锁，避免动画正在处理，有线程安全问题
                    try {
                        Object sensorObjects = jsonObj.get("obs");
                        //清空上次传来的检测物
                        ContainerObject3D.Obj0UnknownList.clear();
                        ContainerObject3D.Obj1PedestrianList.clear();
                        ContainerObject3D.Obj2VehicleList.clear();

                        if ( sensorObjects != null){
                            JSONArray objectArray = (JSONArray) sensorObjects;
                            for (Object ob : objectArray){
                                JSONObject obJson = (JSONObject) ob;
                                TrafficObj ob3D =  transOb_2Object_3D(obJson);
                                //原点在后轮中心  右是正， 前是正
                                Log.i(tag, "get obj class:" + ob3D.getClassification() +", id:"+ob3D.getId() +
                                        ", x:"+ ob3D.getX()+ ", y:" + ob3D.getY()+
                                        ", with:" + ob3D.getWidth()+", length:" + ob3D.getLength());
                            }
                        }
                    }finally {
                        EnjoySocketService.UpdateUIModelLock.unlock();  //释放锁
                    }
                }else {
                    Log.i(tag,"Lock not get data: 数据接收未获取锁");
                }

                break;
            case TopicAndParams.topicRecvLonlatmMappoints:        //轨迹点
                Log.i(tag,"get map points:" + jsonObj.toString());
//                lonlatmappoints
                String mapName = jsonObj.get("mapname").toString();  //轨迹名称 maping1
                String zoneName = jsonObj.get("zonename").toString();  //园区名称 yuanqu1
                JSONArray pointsArray = (JSONArray) jsonObj.get("points");  //轨迹点
                if (pointsArray == null || pointsArray.isEmpty()) {
                    Log.e(tag, "接收轨迹点为空");
                }
                DataStorageFromPC.mappingJSON = jsonObj;

                /**
                 *

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
                **/

                break;
            /**
             *
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
             ***/
            case TopicAndParams.topicRecvV2xapp: //V2x  红绿灯和限速
                //  trafficLight  0:无 1：红灯 2：绿灯 3：黄灯
//
//                int v2xType = Integer.valueOf(jsonObj.get("v2xtype").toString());  //类型
                int trafficLight = Integer.valueOf(jsonObj.get("color").toString());
                int speedLimitInt = (int) (Integer.valueOf(jsonObj.get("speedlimit").toString()) * 3.6);  //限速  m/s
                DataStorageFromPC.lightColor = trafficLight;
//                DataStorageFromPC.v2xType = v2xType;
                DataStorageFromPC.speedLimit = speedLimitInt;
                DataStorageFromPC.speedLimitStr = String.valueOf(speedLimitInt);
                break;
        }
    }

    private static TrafficObj transOb_2Object_3D(JSONObject obJson){

       int classification = Integer.valueOf(obJson.get("classification").toString());

        TrafficObj trafficObj = new TrafficObj();
        trafficObj.setId(Integer.valueOf(obJson.get("id").toString()));
        trafficObj.setClassification(classification);
        trafficObj.setX(Float.parseFloat(obJson.get("x").toString()));    //float #横坐标  单位m
        trafficObj.setY(Float.parseFloat(obJson.get("y").toString()));
        trafficObj.setWidth(Float.parseFloat(obJson.get("width").toString()));
        trafficObj.setLength(Float.parseFloat(obJson.get("length").toString()));

        if (classification == 0){
            ContainerObject3D.Obj0UnknownList.add(trafficObj);
        }else if (classification == 1){
            ContainerObject3D.Obj1PedestrianList.add(trafficObj);
        }else {
            ContainerObject3D.Obj2VehicleList.add(trafficObj);


           /**
            * 测试使用
            * **/
            TrafficObj obj = new TrafficObj();
            obj.setId(Integer.valueOf(obJson.get("id").toString()));
            obj.setClassification(classification);
            obj.setX(Float.parseFloat(obJson.get("x").toString()));    //float #横坐标  单位m
            obj.setY(Float.parseFloat(obJson.get("y").toString())  - 6 );
            obj.setWidth(Float.parseFloat(obJson.get("width").toString()));
            obj.setLength(Float.parseFloat(obJson.get("length").toString()));
            ContainerObject3D.Obj0UnknownList.add(obj);
        }
        return trafficObj;
    }
}
