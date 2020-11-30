package com.enjoy.sweeper;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;

import com.enjoy.sweeper.entity.DataStorage;
import com.enjoy.sweeper.util.ConstantsUtil;
import com.enjoy.sweeper.util.MyStringUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import aps.entity.PublishEvent;
import de.greenrobot.event.EventBus;
import ros.ROSClient;
import ros.rosbridge.ROSBridgeClient;

public class EnjoyTrainShipApplication extends Application {

    private static volatile boolean connectTry = false;
    private static String TAG = "APP_TAG";


    String recvTopicSensorGps = "/sensorgpsPad"; //订阅消息TASKRE
    String recvTopicLoadMapsName = "/lonlatmap"; //订阅加载地图TOPIC
    String recvTopicBehaviordecision = "/behaviordecisionPad"; //订阅障碍物消息topic
    String recvTopicActuator = "/actuatorPad";   //订阅驾驶状态topic
    String recvTopicGarbageStation = "/station";  //垃圾站信息topic
    String recvTopicCarStation = "/carstation"; //车站信息topic


    String sendTopicNameAppMsg = "/appMsg"; //TASK 发送停止或出发、地图编号
    //    String sendTopicName = "/app"; //TASK 发送停止或出发、地图编号
    String roadsRequestMapTopic = "/requestmap";
    String sendTopicCollectMapForCar = "/collectmap";
    String sendTopicSetstation = "/setstation";
    String controlTopic = "/manualcontrol";

    String loadMapRequestData = "\"request\":";  //1--请求全部地图  2请求指定地图    0-不请求  3--删除地图
    String loadMapMapNameData = ",\"mapname\":";   //地图名
    String loadMsgAreaStr = ",\"filedirectoryname\":"; //区域，所作业的任务区域


    String rosIP = "192.168.6.100";
    //    String rosIP = "192.168.6.113";
    String appMsgStopGoStr = "\"stopgo\":";
    String appMsgMapNameStr = ",\"mapname\":";
    String appMsgCleanenableStr = ",\"cleanenable\":"; //是否清扫
    String appMsgDumprubbishStr = ",\"dumprubbish\":"; //是否清倒垃圾

    String appMsgGogarageStr = ",\"gogarage\":"; //是否返回停车场
    String appMsgGogarbagestationStr = ",\"gogarbagestation\":"; //是否返回垃圾站
    String appMsgGoCleanStr = ",\"goclean\":"; //是否执行扫地任务
    String appMsgAreaStr = ",\"filedirectoryname\":"; //区域，所作业的任务区域
    String appMsgPresentationStr = ",\"dumprubbishmode\":"; //演示  0--演示 1--实际清倒
    String appMsgPauseStr = ",\"stopcontrol\":"; //暂停  0--无效 1--停车
    String timestampCommonStr = ",\"timestamp\":"; //时间戳

    //    String mapNameStr = ",\"mapnum\":";
    String backLightStr = ",\"backLight\":";
    String hornStr = ",\"horn\":"; //鸣笛
    String ledLightStr = ",\"ledLight\":"; //警灯
    String leftFrontLightStr = ",\"leftFrontLight\":";
    String rightFrontLightStr = ",\"rightFrontLight\":";
    String removeWarning = ",\"removeWarning\":";


    String mapNameCollect = "\"mapname\":";
    String mapSpeedCollect = ",\"speed\":";
    String laneTypeCollect = ",\"lanetype\":";
    String collectModeCollect = ",\"collectmode\":";
    String cleanenableCollect = ",\"cleanenable\":";   //清扫使能 0--不使能 1---使能
    String collectAreaStr = ",\"filedirectoryname\":"; //区域，所作业的任务区域
    String collectPathProperty = ",\"pathproperty\":";   //路径属性      0 ：清扫       1:倾倒垃圾点路径       2:车库路径
    String collectReverse = ",\"reverse\":";      //道路方向 0---前进 1---倒车
    String collectInOrOut = ",\"inorout\":";      //进垃圾站或出垃圾站 0----进入    1----退出
    String collectPathValid = ",\"pathvalid\":"; //道路是否有效   0----有效   1-----无效

//    flagStr + (byte) flag + steerStr + nowSteer + speedStr + nowSpeed;

    String controlFlag = "\"flag\":";
    String steerStr = ",\"steer\":";
    String speedStr = ",\"speed\":";


    //语音媒体
    MediaPlayer mediaPlayer;

    ROSBridgeClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);//注册ros事件接收


        //初始化语音媒体
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { //播放完毕一次指向文件头
                mediaPlayer.seekTo(0);
            }
        });

        //为音频设置数据源，并准备播放
        AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.alter);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.v(TAG, "*********音频数据源读取设置有误********");

        }


        new ConnectThread().start();
        new PageThread().start();
        new SubscribeThread().start();
        new MediaPlayScanThread().start();
    }


    /**
     * 连接线程
     */
    class ConnectThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SystemClock.sleep(2000); //每2000毫秒扫描一次
                if (Global.connectFlag || connectTry) { //已经连接了或者在连接中继续循环
                    continue;
                }
                //开始连接
                connect(rosIP, "9090");
            }
        }
    }

    /**
     * 播放语音
     */
    private synchronized void playMedia() {
        if (mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.start();
    }

    class MediaPlayScanThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SystemClock.sleep(3000);
                if (!Global.connectFlag) {
                    continue;
                }
                if (DataStorage.pathError > 0) { //有报警
                    playMedia();
                }
            }
        }
    }


    /**
     * 订阅线程
     */
    class SubscribeThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SystemClock.sleep(500);
                if (!Global.connectFlag) {
                    continue;
                }
                startToSubscribe();
            }
        }
    }

    /**
     * 页面线程
     */
    class PageThread extends Thread {
        @Override
        public void run() {
            myLoop();
        }
    }

    private void myLoop() {
        Map<String, Object> dataMap = new LinkedHashMap<>(); //注意此处若使用HashMap遍历时不会按照存放的顺序，采用LinkedHashMap可实现
        while (true) {
            SystemClock.sleep(100);
            Log.i(TAG, "页面后台任务 connectFlag = " + Global.connectFlag + " page = " + DataStorage.page);
            if (!Global.connectFlag) { //未连接
                continue;
            }
            String taskMsg = "";

            /**********************主页面**********************/
            if (DataStorage.appMsgSendLock.tryLock()) { //获取锁则发送
                dataMap.put(appMsgStopGoStr, DataStorage.stopgo);

                dataMap.put(appMsgCleanenableStr, DataStorage.BROOM_CLEAN_CLICK());
                dataMap.put(appMsgDumprubbishStr, DataStorage.dumpRubbishClick);
                dataMap.put(appMsgGoCleanStr, DataStorage.goCleanClick);
                dataMap.put(appMsgGogarageStr, DataStorage.goGarageClick);
                dataMap.put(appMsgGogarbagestationStr, DataStorage.goGarbageStationClick);

                dataMap.put(appMsgPauseStr, DataStorage.goPauseClick);
                dataMap.put(appMsgPresentationStr, DataStorage.dumpRubbishMode);

                dataMap.put(appMsgAreaStr, DataStorage.getSelectAreaName());
                dataMap.put(appMsgMapNameStr, DataStorage.getSelectMapName());

                dataMap.put(timestampCommonStr, System.currentTimeMillis());
                taskMsg = MyStringUtil.sendDataStr(sendTopicNameAppMsg, dataMap);


                client.send(taskMsg);
                //用户点击了清倒，发送命令后转为默认
                DataStorage.dumpRubbishClick = DataStorage.dumpRubbishClick > 0 ? 0 : DataStorage.dumpRubbishClick;
                dataMap.clear();
                Log.i(TAG, "APP发送启停命令" + taskMsg);

                DataStorage.appMsgSendLock.unlock();
            }


            //加载地图
            if (Global.loadRoadsFlag) {
                dataMap.put(loadMapRequestData, 1);
                dataMap.put(loadMapMapNameData, DataStorage.requestMapName);
                dataMap.put(timestampCommonStr, System.currentTimeMillis());
                String roadData = MyStringUtil.sendDataStr(roadsRequestMapTopic, dataMap);
                client.send(roadData);
                dataMap.clear();
                Global.loadRoadsFlag = false;
                Log.i(TAG, "APP请求加载地图");
            }

            //删除地图
            if (Global.deleteRoadFlag) {
                dataMap.put(loadMapRequestData, 3);
                dataMap.put(loadMsgAreaStr, DataStorage.getSelectAreaName());
//                dataMap.put(loadMapMapNameData, DataStorage.deleteMapName);
                dataMap.put(loadMapMapNameData, DataStorage.getSelectMapName());
                String roadData = MyStringUtil.sendDataStr(roadsRequestMapTopic, dataMap);
                client.send(roadData);
                dataMap.clear();
                Global.deleteRoadFlag = false;
                Log.i(TAG, "APP请求删除地图 " + roadData);
            }


            /**********************采集地图页面**********************/
            //采集开始的判断
            // DataStorage.collectMode  0---不采集    1----采集开始   2---结束采集
//            if (DataStorage.collectMapName == null || DataStorage.collectMapName.equals("")) {
//
//                Log.i(TAG, "APP发送地图采集信息collectMapName为空");
//            } else {
            if (DataStorage.collectMode == 0) { //模式不采集
                Log.i(TAG, "非采集模式");
                continue;
            } else if (1 == DataStorage.collectMode || 2 == DataStorage.collectMode) { //1----开始采集 2--结束采集
                dataMap.put(mapNameCollect, DataStorage.collectMapName);
                dataMap.put(mapSpeedCollect, DataStorage.collectMapSpeed);
                dataMap.put(collectModeCollect, DataStorage.collectMode);
                dataMap.put(laneTypeCollect, DataStorage.collectMapLaneType);
                dataMap.put(cleanenableCollect, DataStorage.getCollectSweepEnable());
                dataMap.put(collectAreaStr, DataStorage.getCollectSelectedArea());
                dataMap.put(collectPathProperty, DataStorage.collectPathProperty);
                dataMap.put(collectReverse, DataStorage.collectReverse);
                dataMap.put(collectPathValid, DataStorage.collectPathValid);
                dataMap.put(collectInOrOut, DataStorage.collectGetInOrOut);
                dataMap.put(timestampCommonStr, System.currentTimeMillis());

                taskMsg = MyStringUtil.sendDataStr(sendTopicCollectMapForCar, dataMap);
                client.send(taskMsg);
                dataMap.clear();
                Log.i(TAG, "采集地图,采集模式 " + DataStorage.collectMode + " msg = " + taskMsg);

//                    //发送是否为垃圾倾倒点
//                    dataMap.put(collectPathProperty, DataStorage.collectPathProperty);
//                    dataMap.put(timestampCommonStr, System.currentTimeMillis());
//                    taskMsg = MyStringUtil.sendDataStr(sendTopicSetstation, dataMap);
//                    client.send(taskMsg);
//                    dataMap.clear();
//                    Log.i(TAG, "采集地图,设置站点 msg = " + taskMsg);

            } else if (2 == DataStorage.collectMode) { //采集结束
            }
//            }
        }
    }

    /**
     * 开始订阅消息
     */
    private void startToSubscribe() {
        if (!Global.connectFlag) {
            return;
        }
        //订阅GPS信息
        client.send(MyStringUtil.subscribeTopicStr(recvTopicSensorGps));
        //订阅障碍物信息
        client.send(MyStringUtil.subscribeTopicStr(recvTopicBehaviordecision));
        //订阅驾驶状态
        client.send(MyStringUtil.subscribeTopicStr(recvTopicActuator));
        //订阅加载地图消息
        client.send(MyStringUtil.subscribeTopicStr(recvTopicLoadMapsName));
        //订阅垃圾站信息
        client.send(MyStringUtil.subscribeTopicStr(recvTopicGarbageStation));
        //订阅车库信息
        client.send(MyStringUtil.subscribeTopicStr(recvTopicCarStation));

        Log.i(TAG, "APP发送订阅");
    }

    /**
     * 连接ros服务
     *
     * @param ip
     * @param port
     */
    private void connect(String ip, String port) {
        Log.i(TAG, "start to connect ros");
        connectTry = true;
        client = new ROSBridgeClient("ws://" + ip + ":" + port);
        client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                Global.connectFlag = true;
                connectTry = false;

            }


            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                Log.d(TAG, "ROS disconnect");
                Global.connectFlag = false;
                connectTry = false;
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "ROS communication error");
                Global.connectFlag = false;
                connectTry = false;
            }
        });
    }


    /**
     * 接收订阅的消息
     *
     * @param event
     */
    public void onEvent(final PublishEvent event) {

        ConstantsUtil.executorService.execute(new Runnable() {
            @Override
            public void run() {
                //解析json消息
                JSONParser parser = new JSONParser();
                JSONObject jsonObj = null;
                try {
                    jsonObj = (JSONObject) parser.parse(event.msg);
                    Log.i(TAG, "收到订阅消息topic =" + event.name);
                    if (recvTopicSensorGps.equals(event.name)) {
                        long timeStamp = Long.valueOf(jsonObj.get("timestamp").toString());
                        Log.i(TAG, "sensorGps time cost = " + (System.currentTimeMillis() - timeStamp) + "ms");
                        long rtkStatus = (long) jsonObj.get("status");     //定位状态
                        double lon = (double) jsonObj.get("lon");
                        double lat = (double) jsonObj.get("lat");
                        String heading = String.valueOf((double) jsonObj.get("heading"));
                        String speed = String.valueOf((int) ((double) jsonObj.get("velocity") * 3.6));

                        DataStorage.rtk = rtkStatus;
                        DataStorage.lon = lon;
                        DataStorage.lat = lat;
                        DataStorage.heading = heading;
                        DataStorage.speed = speed;

                    }

                    /**
                     if (DataStorage.mode != 1) {  //如果不是显示订阅信息模式，则不显示
                     return;
                     }
                     **/
                    if (recvTopicLoadMapsName.equals(event.name)) {  //接收到地图信息
                        Log.i("ROS加载地图", jsonObj.toString());
                        String mapNameOrigin = (String) jsonObj.get("mapname");//轨迹名称--带文件夹名称的
                        String[] mapNameArr = mapNameOrigin.split("/");
                        String roadArea = mapNameArr[0];  //轨迹的的作业区域
                        String mapName = mapNameArr[1]; //轨迹名称

                        if (DataStorage.roadsMap.get(mapNameOrigin) != null) {
                            Log.i(TAG, "重复地图忽略 mapName = " + mapNameOrigin);
                            return;
                        }
                        boolean isRoadToClean = !mapName.startsWith("maping_"); //是否为清倒道路
//                        DataStorage.nameBlockQueue.add(mapName);
                        DataStorage.nameBlockQueue.add(mapNameOrigin);
                        DataStorage.roadsMap.put(mapNameOrigin, jsonObj);

                        /********对地图名称装入map，key为汉语名，value为英文名*********/
                        if (isRoadToClean) {
//                            DataStorage.addRoadArea(roadArea);
                            DataStorage.addRoadAreaAndRoads(roadArea, mapName);
//                            roadNameToCh(mapName);
                        }
                        //轨迹没有选中,设置初步选定的轨迹，默认为先得到的清倒轨迹
                        /**
                         * 清扫地图（默认）mapingX
                         * 车库地图 maping_garage_X
                         * 车库倒车地图 maping_garage_reverse_X
                         * 场站地图 maping_garbagestation_X
                         * 场站倒车地图 maping_garbagestation_reverse_X
                         */
                        if (DataStorage.initSelectMapName == null || DataStorage.initSelectMapName.equals("")) {
                            if (isRoadToClean) {
                                DataStorage.initSelectMapName = mapName;
                            }
                        }
                        mapName = null;
                    } else if (recvTopicBehaviordecision.equals(event.name)) {   //收到障碍物信息 obs为空--无障碍物非空0--未分类 1-4为实体障碍物 >4虚拟障碍物
                        Log.i(TAG, "收到障碍物信息" + jsonObj.toString());  //50ms一次
                        //{"obs":[],"timestamp":0,"isvalid":0,"turnlights":0,"laneblock":0,"drivebehavior":1}
                        long timeStamp = Long.valueOf(jsonObj.get("timestamp").toString());
                        Log.i(TAG, "behaviorDecision time cost = " + (System.currentTimeMillis() - timeStamp) + "ms");
                        JSONArray jsonObsArray = (JSONArray) jsonObj.get("obs");
                        int carWorkMode = Integer.parseInt(jsonObj.get("carworkmode").toString());// 获取工作模式 1,清扫       2,去车库       3,去垃圾站
                        DataStorage.setCarWorkMode(carWorkMode);

                        //获取是否报警
                        Object sysErrorObj = jsonObj.get("patherror"); //0 默认 1 报警
                        if (sysErrorObj != null) {
                            int sysError = Integer.parseInt(sysErrorObj.toString());
                            Log.i(TAG, "收到报警" + sysError);
                            DataStorage.pathError = sysError;
                        } else {
                            DataStorage.pathError = 0;
                        }

                        int obsClass = 0;
                        if (jsonObsArray == null || jsonObsArray.size() == 0) { //没有障碍物
                            obsClass = -1;
                        } else {
                            for (int i = 0; i < jsonObsArray.size(); i++) {
                                JSONObject jn = (JSONObject) jsonObsArray.get(i);
                                int cation = Integer.valueOf(jn.get("classification").toString());
                                if (cation > obsClass) {
                                    obsClass = cation;
                                }
                            }
                        }
                        if (DataStorage.obs != obsClass) {
                            DataStorage.obs = obsClass; //障碍物状态有改变的时候发送广播
                        }
                    } else if (recvTopicActuator.equals(event.name)) {      //驾驶状态信息 1-自动驾驶  0-非自动驾驶      //50ms一次
                        Log.i(TAG, "收到驾驶状态信息" + jsonObj.toString());
                        int driverStatus = Integer.valueOf(jsonObj.get("sysstatus").toString());
                        int batterySoc = Integer.valueOf(jsonObj.get("soc").toString()); //电量  %

                        float battery = Float.valueOf(jsonObj.get("battery").toString()); //电压
                        int batteryInt = Math.round(battery);

                        int liftStatus = Integer.valueOf(jsonObj.get("liftstate").toString());     //倾倒状态  0--无  >0有
                        int sweepStatus = Integer.valueOf(jsonObj.get("sweepstate").toString());  //清扫状态   0---无   >0有

                        long timeStamp = Long.valueOf(jsonObj.get("timestamp").toString());
                        Log.i(TAG, "actuator time cost = " + (System.currentTimeMillis() - timeStamp) + "ms");

                        DataStorage.driverStatus = driverStatus;
                        DataStorage.batterySoc = batterySoc;  //电量  %
                        DataStorage.battery = batteryInt;    //电压
                        DataStorage.liftStatus = liftStatus;
                        DataStorage.sweepStatus = sweepStatus;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(TAG, "json转化出错");
                } finally {
                    parser = null;
                    jsonObj = null;
                }
            }
        });

        Log.d("df", event.msg);
    }


    /**
     * 地图名称转为汉字
     *
     * @param roadsNameEng
     * @return * 清扫地图（默认）mapingX
     * * 车库地图 maping_garage_X
     * * 车库倒车地图 maping_garage_reverse_X
     * * 场站地图 maping_garbagestation_X
     * * 场站倒车地图 maping_garbagestation_reverse_X
     *
     * 该部分只添加清扫道路
     */

    /**
     private void roadNameToCh(String roadsNameEng) {
     String roadName_CH = "";
     String numStr = ConstantsUtil.getNumStrFromStr(roadsNameEng); //地图的数字编号
     roadName_CH = "清扫路" + numStr;
     DataStorage.putRoadsNameCh_EngMap(roadName_CH, roadsNameEng);



     if (roadsNameEng.startsWith("maping_garage")) {//车库地图
     if (roadsNameEng.startsWith("maping_garage_reverse")) { //车库倒车地图
     roadName_CH = "倒库路" + numStr;
     } else roadName_CH = "入库路" + numStr;
     } else if (roadsNameEng.startsWith("maping_garbagestation")) { //场站地图
     if (roadsNameEng.startsWith("maping_garbagestation_reverse")) { //场站倒车地图
     roadName_CH = "倒站路" + numStr;
     } else roadName_CH = "入站路" + numStr;
     } else { //清扫地图
     roadName_CH = "清扫路" + numStr;
     }




     }

     */
}
