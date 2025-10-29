package com.enjoy.wanji.service;


import android.util.Log;

import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.entity.GearEnum;
import com.enjoy.wanji.entity.TrafficObjClassEnum;
import com.enjoy.wanji.vr3D.ContainerObject3D;
import com.enjoy.wanji.vr3D.TrafficObj;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MessageHandle {

    static String tag = "msgTag";

    public static void handle(String topicName, JSONObject jsonObj) {
        switch (topicName) {
            case TopicAndParams.topicRecvSensorgps:
                double lon = (double) jsonObj.get("lon");
                double lat = (double) jsonObj.get("lat");
                String heading = String.valueOf((double) jsonObj.get("heading"));

                DataStorageFromPC.lon = lon;
                DataStorageFromPC.lat = lat;
                DataStorageFromPC.heading = heading;
                break;
            case TopicAndParams.topicRecvActuator:
                Log.i(tag, "收到驾驶状态信息" + jsonObj.toString());
                int speedInt = (int) ((double) jsonObj.get("speed") * 3.6);
                int driverStatus = Integer.valueOf(jsonObj.get("sysstatus").toString());  //驾驶状态 0-人工； 1-自动
                int gear = Integer.valueOf(jsonObj.get("gear").toString()); //档位 0-P  1-R  2-N  3-D
                int turnLight = Integer.valueOf(jsonObj.get("turnLight").toString());  //转向 0--无  1--左转  2--右转

                // int error = Integer.valueOf(jsonObj.get("error").toString());   //2，故障等级2 语音提示加弹框
                Object socObj = jsonObj.get("soc");

                //电量
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
                //驾驶状态  0-人工  1--自动
                if (DataStorageFromPC.driverStatus != driverStatus){   //跳变
                    if (DataStorageFromPC.driverStatus > 0) {   //当前
                        DataStorageFromPC.driverStatusTip = 2;//退出自驾
                    } else {
                        DataStorageFromPC.driverStatusTip = 1;//进入自驾
                    }
                    DataStorageFromPC.driverStatus = driverStatus;
                }

                //档位 0-P  1-R  2-N  3-D
                DataStorageFromPC.GearInt = gear;
                DataStorageFromPC.Gear = GearEnum.getValue(gear);

                DataStorageFromPC.speedStr = String.valueOf(speedInt);
                DataStorageFromPC.velocity = speedInt;

                //转向 0--无  1--左转  2--右转
                DataStorageFromPC.turnLight = turnLight;
                break;
            case TopicAndParams.topicRecvTrafficPart:       //交通参与者，3D动画
                if (DataStorageFromPC.TRAFFIC_DATA_SEND){
                    DataStorageFromPC.TRAFFIC_DATA_SEND = false;
                    return;
                }
                if (EnjoySocketService.UpdateUIModelLock.tryLock()) {   //先获取锁，避免动画正在处理，有线程安全问题
                    try {
                        Object sensorObjects = jsonObj.get("obs");
                        //把上次传来的物都返回到预留集合中
                        ContainerObject3D.TrafficObjHomeQueue.addAll(ContainerObject3D.Obj0UnknownList);
                        ContainerObject3D.TrafficObjHomeQueue.addAll(ContainerObject3D.Obj1PedestrianList);
                        ContainerObject3D.TrafficObjHomeQueue.addAll(ContainerObject3D.Obj2VehicleList);
                        ContainerObject3D.TrafficObjHomeQueue.addAll(ContainerObject3D.Obj3NoVehicleList);

                        //清空上次传来的检测物
                        ContainerObject3D.Obj0UnknownList.clear();
                        ContainerObject3D.Obj1PedestrianList.clear();
                        ContainerObject3D.Obj2VehicleList.clear();
                        ContainerObject3D.Obj3NoVehicleList.clear();


                        if (sensorObjects != null) {
                            JSONArray objectArray = (JSONArray) sensorObjects;
                            for (Object ob : objectArray) {
                                JSONObject obJson = (JSONObject) ob;
                                TrafficObj ob3D = transOb_2Object_3D(obJson);
                                if (ob3D != null){
                                    //原点在后轮中心  右是正， 前是正
                                    Log.i(tag, "get obj class:" + ob3D.getClassification() + ", id:" + ob3D.getId() +
                                            ", x:" + ob3D.getX() + ", y:" + ob3D.getY() +
                                            ", with:" + ob3D.getWidth() + ", length:" + ob3D.getLength());
                                }
                            }
                        }
                        DataStorageFromPC.TRAFFIC_DATA_SEND = true;
                    } finally {
                        EnjoySocketService.UpdateUIModelLock.unlock();  //释放锁
                    }
                } else {
                    Log.i(tag, "Lock not get data: 数据接收未获取锁");
                }

                break;

            case TopicAndParams.topicRecvLonlatmMappoints:        //轨迹点
                Log.i(tag, "get map points:" + jsonObj.toString());
                JSONArray pointsArray = (JSONArray) jsonObj.get("points");  //轨迹点
                if (pointsArray == null || pointsArray.isEmpty()) {
                    Log.e(tag, "接收轨迹点为空");
                }
                DataStorageFromPC.mappingJSON = jsonObj;

                break;

            case TopicAndParams.topicRecvV2xapp: //V2x  红绿灯和限速
                int v2xType = Integer.valueOf(jsonObj.get("v2xtype").toString());  //类型
                int speedLimitInt = (int) (Integer.valueOf(jsonObj.get("speedlimit").toString()) * 3.6);  //限速  m/s
                DataStorageFromPC.v2xType = v2xType;
                if (DataStorageFromPC.speedLimit != speedLimitInt){
                    DataStorageFromPC.speedLimit = speedLimitInt;
                    DataStorageFromPC.speedLimitStr = String.valueOf(speedLimitInt);
                }
                DataStorageFromPC.v2xTimestamp = System.currentTimeMillis();
                break;
            case TopicAndParams.topicRecvCloudLight:          //云端
                //0:无 1：红灯 2：绿灯 3：黄灯
                int trafficLight = Integer.valueOf(jsonObj.get("color").toString());

                int remainingTime =  Integer.parseInt(jsonObj.get("remainingtime").toString()); //剩余时间

                DataStorageFromPC.lightColor = trafficLight;
                DataStorageFromPC.remainingTime = remainingTime;
                break;
            case TopicAndParams.topicRecvCloudPath:

                JSONArray pointsArr = (JSONArray) jsonObj.get("points");
                if (pointsArr != null && pointsArr.size() > 0){
                    JSONObject element = (JSONObject)pointsArr.get(0);
                    int targetSpeed =  (int) (Double.parseDouble(element.get("speed").toString()) * 3.6); //指导速度
                    DataStorageFromPC.guideSpeed = targetSpeed;
                    DataStorageFromPC.guideSpeedTimeStamp = System.currentTimeMillis();
                }
                break;

        }
    }

    static int classification;

    private static TrafficObj transOb_2Object_3D(JSONObject obJson) {

        classification = Integer.valueOf(obJson.get("classification").toString());

        TrafficObj trafficObj = ContainerObject3D.TrafficObjHomeQueue.poll();
        if (trafficObj == null) {  //没有了，说明展示数量超出了，可不显示
            return null;
        }

        trafficObj.setId(Integer.valueOf(obJson.get("id").toString()));
        trafficObj.setClassification(classification);
        trafficObj.setX(Float.parseFloat(obJson.get("x").toString()));    //float #横坐标  单位m
        trafficObj.setY(Float.parseFloat(obJson.get("y").toString()));
        trafficObj.setWidth(Float.parseFloat(obJson.get("width").toString()));
        trafficObj.setLength(Float.parseFloat(obJson.get("length").toString()));
        trafficObj.setAzimuth(Float.parseFloat(obJson.get("azimuth").toString()));

        if (classification == TrafficObjClassEnum.Unknown.key) {
            ContainerObject3D.Obj0UnknownList.add(trafficObj);
        } else if (classification == TrafficObjClassEnum.Pedestrian.key) {
            ContainerObject3D.Obj1PedestrianList.add(trafficObj);
        } else if (classification == TrafficObjClassEnum.Vehicle.key){
            ContainerObject3D.Obj2VehicleList.add(trafficObj);
        }else if (classification == TrafficObjClassEnum.NoVehicle.key){  //非机动车
            ContainerObject3D.Obj3NoVehicleList.add(trafficObj);
        }
        return trafficObj;
    }
}
