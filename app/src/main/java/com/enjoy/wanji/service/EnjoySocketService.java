package com.enjoy.wanji.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
<<<<<<< HEAD
//import android.support.annotation.Nullable;
=======
>>>>>>> ac9649c (bug修复播报语音时，文字不显示问题)
import android.support.annotation.Nullable;
import android.util.Log;

//import androidx.annotation.Nullable;

import com.enjoy.wanji.EnjoyTrainShipApplication;
import com.enjoy.wanji.Global;
import com.enjoy.wanji.data.Common;
import com.enjoy.wanji.data.TopicAndParams;
import com.enjoy.wanji.entity.DataStorage;
import com.enjoy.wanji.util.ToastUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class EnjoySocketService extends IntentService {

    //        String rosIP = "192.168.1.100";
//    String rosIP = "192.168.6.117";
//    String rosIP = "192.168.6.115";
    String rosIP = EnjoyTrainShipApplication.sharedPreferences.getString(Common.ROS_IP, "");
    private static String tag = "service_tag";
    private static String connectTag = "connectTag";
    private static Object connectObjLock = new Object();  //连接锁
    public static WebSocketClient client = null;
    private static Thread connectThread = null;
    private static Thread sendLoopThread = null;

    public EnjoySocketService() {
        super("my service");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int type = intent.getIntExtra(Common.ACTION_NAME, -1);

        switch (type) {
            case Common.ACTION_CONNECT:
                Log.i(tag, "service start to connect");
                startConnect();
                startLoop();
                break;
        }
    }

    private synchronized void startConnect() {
        if (connectThread != null) {
            return;
        }
        connectThread = new ConnectThread();
        connectThread.start();

    }

    private synchronized void startLoop() {
        if (sendLoopThread != null) {
            return;
        }
        sendLoopThread = new SendLoopThread();
        sendLoopThread.start();
    }

    /**
     * 连接线程
     */
    class ConnectThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SystemClock.sleep(2000); //每2000毫秒扫描一次

                if (rosIP == null || "".equals(rosIP)) {
                    ToastUtil.showShort(EnjoySocketService.this, "IP设置有误，请返回检查");
                    continue;
                }
                //开始连接
                connect(rosIP, "9090");
                synchronized (connectObjLock) {
                    try {
                        Log.i(connectTag, "Connect Thread waite......");
                        connectObjLock.wait();
                    } catch (InterruptedException interruptedException) {
                        Log.e(connectTag, "webSocket connect exception");
                    }
                }
            }
        }
    }

    class SendLoopThread extends Thread {
        @Override
        public void run() {
            int count = 0;
            while (true) {
                SystemClock.sleep(500);
                if (client == null || !Global.connectFlag) {
                    continue;
                }
                //连接成功后，发送消息订阅
                if (count % 10 == 0 && DataStorage.page == 1) {   //订阅消息5秒发一次 且在第一主页面
                    MyClientUtil.subscribe(client);
                }
                if (DataStorage.page == 1) {  //在页面一，发送实时命令信息
                    MyClientUtil.send(client);
                } else if (DataStorage.page == 2) {  //第二页面，发送采集轨迹命令
                    MyClientUtil.collectMap(client);
                    sendCast(Common.COLLECT_RECEIVER_ACTION, Common.ACTION_UPDATE_COLLECT);
                }
                count++;
                if (count > 50) {
                    count = 0;
                }

            }
        }
    }


    /**
     * 连接ros服务
     *
     * @param ip
     * @param port
     */
    private void connect(String ip, String port) {
        Log.i(connectTag, "start to connect ros ip =" + ip);
        try {
//            Draft 是版本
            client = new WebSocketClient(new URI("ws://" + ip + ":" + port), new Draft_17()) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // 连接成功
                    Global.connectFlag = true;
                    Log.i(connectTag, "ros connect success");
                    //发送广播
                    sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_UPDATE);
                    Log.i(connectTag, "广播connect success");
                    Global.connectTip = 1;  //连接成功，语音提示标记
                }

                //收到消息
                @Override
                public void onMessage(String s) {
                    Log.i(tag, "webSocket get message: " + s);
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = (JSONObject) parser.parse(s);
                        Object nameObj = jsonObj.get("topic");
                        String nameTopic = null;
                        if (nameObj != null) {
                            nameTopic = nameObj.toString();
                        } else {
                            return;
                        }
                        Log.i(tag, "收到订阅消息topic =" + nameTopic);
                        Object msgObj = jsonObj.get("msg");
                        JSONObject msgJson = msgObj == null ? null : (JSONObject) msgObj;
                        if (msgObj == null) {
                            return;
                        }
                        MessageHandle.handle(nameTopic, msgJson);
                        if (nameTopic.equals(TopicAndParams.topicRecvLonlatmMappoints)) {  //接收到轨迹点
                            sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_ROADS_SHOW);
                        } else if (nameTopic.equals(TopicAndParams.topicRecvV2xapp)) { //接收v2x
                            sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_V2X);
                        } else if (nameTopic.equals(TopicAndParams.topicRecvSensorgps)) {  //更新位置定位
                            if (System.currentTimeMillis() % 1000 < 200) {  //gps频率高，这里降一下，防止频繁更新轨迹
                                sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_LOCATION);
                            }
                        } else {  //其他
                            //发送广播
                            sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_UPDATE);
                            sendCast(Common.COLLECT_RECEIVER_ACTION, Common.ACTION_UI_UPDATE);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e(tag, "json转化出错");
                    }
                }

                //退出连接
                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.d(connectTag, "ROS onClose");
                    if (Global.connectFlag) {
                        Global.connectTip = 2;  //连接断开  语音提示标记
                    }
                    Global.connectFlag = false;

                    //发送广播
                    sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_UPDATE);
                    Log.i(connectTag, "广播disconnect");

                    synchronized (connectObjLock) {
                        connectObjLock.notify();  //唤醒连接线程
                        Log.i(connectTag, "Connect Thread notify");
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    Log.d(connectTag, "ROS communication error" + e.getMessage());
                    Global.connectFlag = false;

                    //发送广播
                    sendCast(Common.MAIN_RECEIVER_ACTION, Common.ACTION_UI_UPDATE);
                    Log.d(connectTag, "广播 ROS error");
                }
            };
            client.connect();
        } catch (Exception ex) {
            Log.e(connectTag, "webSocket connect error");
        }

    }

    private void sendCast(String action, int actionType) {
        //发送广播
        Intent intent = new Intent(action);
        intent.putExtra(Common.ACTION_NAME, actionType);
        sendBroadcast(intent);
    }


    //该线程池用来唤醒语音提示线程的 ,ThreadPoolExecutor.DiscardPolicy() 任务丢弃但不抛异常
    private static BlockingQueue<Runnable> threadQueue = new ArrayBlockingQueue(1);
    public static ExecutorService threadMediaPoolService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS, threadQueue, new ThreadPoolExecutor.DiscardPolicy());

}
