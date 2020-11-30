package com.enjoy.sweeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import aps.RCApplication;
import ros.ROSClient;
import ros.rosbridge.ROSBridgeClient;

/**
 * Created by jintong on 17-8-23.
 */
public class load extends Activity {
    private static final String TAG = "MainActivity";

    EditText etIP;
    EditText etPort;
    Button conBtn;

    ROSBridgeClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
        etIP=(EditText) findViewById(R.id.et_ip);
        etPort=(EditText)findViewById(R.id.et_port);
        conBtn=(Button)findViewById(R.id.btn_connect);

        conBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = etIP.getText().toString();
                String port = etPort.getText().toString();
                showTip(ip);
                connect(ip, port);
            }
        });

    }

    private void connect(String ip, String port) {
        client = new ROSBridgeClient("ws://" + ip + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication)getApplication()).setRosClient(client);
                showTip("Connect ROS success");
                Log.d(TAG,"Connect ROS success");
                startActivity(new Intent(load.this,MainActivity.class)); //PubMsgActivity
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                showTip("ROS disconnect");
                Log.d(TAG,"ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                showTip("ROS communication error");
                Log.d(TAG,"ROS communication error");
            }
        });
    }

    //单独线程提示程序运行信息
    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(load.this, tip,Toast.LENGTH_SHORT).show();
            }
        });
    }


}
