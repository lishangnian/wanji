package com.enjoy.wanji;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.enjoy.wanji.data.Common;
import com.enjoy.wanji.util.MyStringUtil;
import com.enjoy.wanji.util.ToastUtil;

public class InitActivity extends Activity {

    EditText ipEdt;
    Button ipBtn;
    final String ipHead = "192.168.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置本activity长亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_init);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //设置屏幕格式为横屏

        ipEdt = findViewById(R.id.ip_edt);
        ipBtn = findViewById(R.id.ip_btn);

        String ipStr = EnjoyTrainShipApplication.sharedPreferences.getString(Common.ROS_IP, "");
        if (!ipStr.equals("")) {
            String[] ipArr = ipStr.split("\\.");
            ipEdt.setText(ipArr[2] + "." + ipArr[3]);
        }

        ipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = ipEdt.getText();
                if (editable == null) {
                    ToastUtil.showShort(InitActivity.this, "请先输入IP");
                    return;
                }
                String ipTail = editable.toString();
                String[] ipTailArr = ipTail.split("\\.");
                if (ipTailArr.length != 2) {

                    ToastUtil.showShort(InitActivity.this, "IP输入格式有误");
                    return;
                }
                if (!MyStringUtil.isNumeric(ipTailArr[0]) || !MyStringUtil.isNumeric(ipTailArr[1])) {
                    Log.i("aaaaaaaaaaa","ipTailArr Numeric error" );
                    ToastUtil.showShort(InitActivity.this, "IP输入格式有误");
                    return;
                }

                EnjoyTrainShipApplication.editor.putString(Common.ROS_IP, ipHead + ipTail);
                EnjoyTrainShipApplication.editor.commit();

                //跳转
                Intent intent = new Intent(InitActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

}
