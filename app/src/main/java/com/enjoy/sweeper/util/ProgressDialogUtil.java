package com.enjoy.sweeper.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.enjoy.sweeper.R;


public class ProgressDialogUtil {


    private static AlertDialog.Builder mAlertDialog;
    private static AlertDialog dialog;

    /**
     * 弹出耗时对话框
     * @param context
     */
    public static void showProgressDialog(Context context) {
        if (mAlertDialog == null) {
//            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
                mAlertDialog = new AlertDialog.Builder(context);
        }


        View loadView = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog_view, null);
//        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setView(loadView);
        mAlertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("提示框","提示框点击");
            }
        });


        TextView tvTip =(TextView) loadView.findViewById(R.id.tvTip);
        tvTip.setText("加载中...");

//        mAlertDialog.show();

        if(dialog == null){
            dialog = mAlertDialog.show();
        }
    }


    /**
    public static void showProgressDialog(Context context, String tip) {
        if (TextUtils.isEmpty(tip)) {
            tip = "加载中...";
        }

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog_view, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = (TextView) loadView.findViewById(R.id.tvTip);
        tvTip.setText(tip);

        mAlertDialog.show();
    }

    /**
     * 隐藏耗时对话框
     */
    public static void dismiss() {
//        if (mAlertDialog != null && mAlertDialog.isShowing()) {
//            mAlertDialog.dismiss();
//        }
        if( dialog!= null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

}
