package com.enjoy.wanji.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
//import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.List;

/**
 * 对话框工具类
 * <p/>
 * <p>普通对话框 {@link #getDialog(Context)}</p>
 * <p>进度对话框 {@link #getProgressDialog(Context, String)}</p>
 * <p>消息对话框(取消) {@link #getMessageDialog(Context, String)}</p>
 * <p>消息对话框(确认,取消) {@link #getMessageDialog(Context, String, DialogInterface.OnClickListener)}</p>
 * <p>确认对话框(单按钮) {@link #getConfirmDialog(Context, String, DialogInterface.OnClickListener)}</p>
 * <p>单选对话框 {@link #getSingleChoiceDialog(Context, String, String[], int, DialogInterface.OnClickListener)}</p>
 * <p>单选对话框(不含标题) {@link #getSingleChoiceDialog(Context, String[], int, DialogInterface.OnClickListener)}</p>
 *
 * @author John
 * @version 1.0
 * @date 2015-09-20 7:50 PM
 */
public class DialogUtil {

    /***
     * 获取一个普通对话框构建器
     *
     * @param context 上下文
     * @return 对话框构建器
     */
    public static AlertDialog.Builder getDialog(Context context) {
        return new AlertDialog.Builder(context);
    }

    /***
     * 获取一个进度对话框
     *
     * @param context 上下文
     * @param message 消息
     * @return 进度对话框
     */
    public static ProgressDialog getProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(message)) {
            progressDialog.setMessage(message);
        }
        return progressDialog;
    }

    /***
     * 获取一个信息对话构建器(单个按钮)
     *
     * @param context         上下文
     * @param message         消息内容
     * @param onClickListener 确认按钮监听器
     * @return 带有按钮的对话框
     */
    public static AlertDialog.Builder getMessageDialog(Context context, String message,
                                                       DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMessage(message);
        builder.setPositiveButton("确定", onClickListener);
        return builder;
    }

    /**
     * 获取消息对话框构建器
     *
     * @param context 上下文
     * @param message 消息内容
     * @return 消息对话框构建器
     */
    public static AlertDialog.Builder getMessageDialog(Context context, String message) {
        return getMessageDialog(context, message, null);
    }

    /**
     * 获取确认消息对话框构建器
     *
     * @param context         上下文
     * @param message         消息内容
     * @param onClickListener 确认按钮监听器(两个按钮)
     * @return 确认消息对话框构建器
     */
    public static AlertDialog.Builder getConfirmDialog(Context context, String message,
                                                       DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMessage(Html.fromHtml(message));
        builder.setPositiveButton("确定", onClickListener);
        builder.setNegativeButton("取消", null);
        return builder;
    }


    /**
     * 获取多选对话框构建器
     *
     * @param context         上下文
     * @param title           标题
     * @param onClickListener 点击选项的监听器
     * @return 多选对话框构建器
     */
    public static void showSelectDialog(Context context, String title, String message,
                                        List<String> arrays,
                                        DialogInterface.OnClickListener onClickListener) {

        AlertDialog.Builder builder = getDialog(context);
        ListAdapter arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, arrays);
        builder.setAdapter(arrayAdapter, onClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        builder.setPositiveButton("取消", null);
        builder.show();

    }

    public static int selectItemDialog(Context context, final String[] items,String title) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.alert_dark_frame);
        if(!TextUtils.isEmpty(title)){
            builder.setTitle(title);
        }

//        final String[] items = mapAreaList1.toArray(new String[mapAreaList1.size()]);

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("dialog", "选择的是：" + items[which] + which);
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
        return -1;
    }


    /**
     * 获取单选对话框构建器
     *
     * @param context         上下文
     * @param title           标题
     * @param arrays          选项数据
     * @param selectIndex     选择索引
     * @param onClickListener 点击选项监听器
     * @return 单选对话框构建器
     */
    public static AlertDialog.Builder getSingleChoiceDialog(Context context, String title,
                                                            String[] arrays, int selectIndex,
                                                            DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setSingleChoiceItems(arrays, selectIndex, onClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setNegativeButton("取消", null);
        return builder;
    }

    /**
     * 获取单选对话框构建器(无标题)
     *
     * @param context         上下文
     * @param arrays          选项数据
     * @param selectIndex     选择索引
     * @param onClickListener 点击选项监听器
     * @return 单选对话框构建器
     */
    public static AlertDialog.Builder getSingleChoiceDialog(Context context, String[] arrays,
                                                            int selectIndex, DialogInterface.OnClickListener onClickListener) {
        return getSingleChoiceDialog(context, "", arrays, selectIndex, onClickListener);
    }
}
