package com.enjoy.wanji;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.enjoy.wanji.entity.AttentionInfo;
import com.enjoy.wanji.entity.AttentionTypeEnum;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.entity.AttentionContentEnum;
import com.enjoy.wanji.entity.V2xTypeEnum;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnjoyTrainShipApplication extends Application {

    private static volatile boolean connectTry = false;
    private static String TAG = "APP_TAG";
    private static String mediaTag = "media_tag";


    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static final Object mediaObj = new Object();
    public static Lock mediaLock = new ReentrantLock();

    //语音媒体
    MediaPlayer mediaPlayer;
    AssetFileDescriptor file;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        new MediaPlayScanThread().start();
    }


    static boolean playCompletionFlag = true;  //播放完成标志

    /**
     * 播放语音
     */
    private void playMedia() {
        if (!playCompletionFlag) {
            return;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i(mediaTag, "语音is playing return");
            return;
        }
        playCompletionFlag = false;
        //为音频设置数据源，并准备播放
        if (AttentionInfo.type == AttentionTypeEnum.CONNECT_SUCCESS.key) {  //连接成功
            file = getResources().openRawResourceFd(R.raw.connect_ok);
        } else if (AttentionInfo.type == AttentionTypeEnum.DISCONNECT.key) {  //连接失败
            file = getResources().openRawResourceFd(R.raw.connect_break1002);
        } else if (AttentionInfo.type == AttentionTypeEnum.AUTO_DRIVE.key) {  //进入自动驾驶
            file = getResources().openRawResourceFd(R.raw.drive_auto1001);
        } else if (AttentionInfo.type == AttentionTypeEnum.MANUAL_DRIVE.key) {  //退出自动驾驶
            file = getResources().openRawResourceFd(R.raw.drive_manual1002);
        } else if (AttentionInfo.type == AttentionTypeEnum.BRAKE.key) {  //刹车，请注意
            file = getResources().openRawResourceFd(R.raw.brake_attention1003);
        } else if (AttentionInfo.type == AttentionTypeEnum.ERROR.key &&
                AttentionContentEnum.contains(AttentionInfo.attentionKey)) {   //故障语音,error分为3类，左数第一位是分类
            int errorClass = DataStorageFromPC.error / 100;  //故障类别

            if (errorClass == 1) {  //故障第一类  线控故障，接管
                file = getResources().openRawResourceFd(R.raw.error100);
            } else if (errorClass == 2) {//故障第二类
                if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR212.key) {  //GPS信号差，请注意接管
                    file = getResources().openRawResourceFd(R.raw.error212);
                } else {        //传感器故障，请紧急接管
                    file = getResources().openRawResourceFd(R.raw.error200);
                }
            } else {  //第三类故障
                if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR301.key) {  //车辆偏离轨迹  请注意
                    file = getResources().openRawResourceFd(R.raw.error301);
                } else if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR302.key) {  //车辆偏离轨迹，请接管
                    file = getResources().openRawResourceFd(R.raw.error302);
                } else if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR303.key) { //车辆超过设定速度，清注意
                    file = getResources().openRawResourceFd(R.raw.error303);
                } else if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR304.key) { //车辆超过设定速度，清接管
                    file = getResources().openRawResourceFd(R.raw.error304);
                } else if (AttentionInfo.attentionKey == AttentionContentEnum.ERROR_CONNECT305.key) {    //连接中断，请接管
                    file = getResources().openRawResourceFd(R.raw.error305);
                } else {
                    playCompletionFlag = true;
                    return;   //没有该编码， 返回
                }
            }
        } else if (AttentionInfo.type == AttentionTypeEnum.V2X.key) {    //播放v2x语音
            if (AttentionInfo.attentionKey == V2xTypeEnum.FCW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x1);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.ICW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x2);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.LTA.key) {
                file = getResources().openRawResourceFd(R.raw.v2x3);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.BSW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x4);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.DNPW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x5);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.EBW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x6);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.AVW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x7);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.CLW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x8);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.HLW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x9);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.SLW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x10);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.RLVW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x11);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.VRUCW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x12);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.GLOSA.key) {
                file = getResources().openRawResourceFd(R.raw.v2x13);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.IVS.key) {
                file = getResources().openRawResourceFd(R.raw.v2x14);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.TJW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x15);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.EVW.key) {
                file = getResources().openRawResourceFd(R.raw.v2x16);

            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V34.key) {
                file = getResources().openRawResourceFd(R.raw.v2x34);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V242.key) {
                file = getResources().openRawResourceFd(R.raw.v2x242);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V501.key) {
                file = getResources().openRawResourceFd(R.raw.v2x501);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V103.key) {
                file = getResources().openRawResourceFd(R.raw.v2x103);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V707.key) {
                file = getResources().openRawResourceFd(R.raw.v2x707);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V415.key) {
                file = getResources().openRawResourceFd(R.raw.v2x415);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V904.key) {
                file = getResources().openRawResourceFd(R.raw.v2x904);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V425.key) {
                file = getResources().openRawResourceFd(R.raw.v2x425);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V435.key) {
                file = getResources().openRawResourceFd(R.raw.v2x435);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V311.key) {
                file = getResources().openRawResourceFd(R.raw.v2x311);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V305.key) {
                file = getResources().openRawResourceFd(R.raw.v2x305);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V301.key) {
                file = getResources().openRawResourceFd(R.raw.v2x301);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V407.key) {
                file = getResources().openRawResourceFd(R.raw.v2x407);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V507.key) {
                file = getResources().openRawResourceFd(R.raw.v2x507);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V903.key) {
                file = getResources().openRawResourceFd(R.raw.v2x903);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V902.key) {
                file = getResources().openRawResourceFd(R.raw.v2x902);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V990.key) {
                file = getResources().openRawResourceFd(R.raw.v2x990);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V598.key) {
                file = getResources().openRawResourceFd(R.raw.v2x598);
            } else if (AttentionInfo.attentionKey == V2xTypeEnum.V599.key) {
                file = getResources().openRawResourceFd(R.raw.v2x599);
            } else {
                playCompletionFlag = true;
                return;   //没有该编码， 返回
            }
        }


        //初始化语音媒体
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { //播放完毕一次指向文件头
                mediaPlayer.seekTo(0);
                playCompletionFlag = true;
            }
        });

        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.v(TAG, "*********音频数据源读取设置有误********");
        }
        mediaPlayer.start();
        Log.i(mediaTag, "播放语音开始");
    }

    class MediaPlayScanThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (mediaObj) {  //线程等待，等待唤醒再继续
                        Log.i(mediaTag, "播放线程等待中");
                        mediaObj.wait();
                    }
                } catch (InterruptedException interruptedException) {
                    Log.e(mediaTag, "thread wait interruptedException");
                }
                Log.i(mediaTag, "播放线程唤醒");
                playMedia();
            }
        }
    }

}
