package com.enjoy.wanji.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

/**
 * 对话框工具类
 *
 * @author John
 * @version 1.0
 * @date
 */
public class EnjoyDialogUtil {

    public static int WHICH = -1;


    public static void selectItemDialog(Context context, final String[] items, String title, DialogInterface.OnClickListener listener) {
        WHICH = -1;
        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.dialog_frame);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("dialog", "选择的是：" + items[which] + which);
                WHICH = which;
            }
        });

        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", null);
        builder.show();

    }
}
