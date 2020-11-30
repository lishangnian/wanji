package com.enjoy.sweeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceCastReceiver extends BroadcastReceiver {

    private String Receiver_Tag = "Service_Receiver_Tag";
    private ServiceReceiveListener receiveListener;

    //接收到的广播
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra(Global.msg);
        Log.i(Receiver_Tag,"Service接收到广播 msg = " + msg);
        if(receiveListener != null){
            receiveListener.onReceive(msg);
        }
    }

    //定义接口用于activity回调
    public interface ServiceReceiveListener {
        void onReceive(String msg);
    }

    public void setServiceReceiveListener(ServiceReceiveListener serviceReceiveListener) {
        this.receiveListener = serviceReceiveListener;
    }
}
