package com.enjoy.wanji;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * 弹出框，编辑控制命令
 */
public class MyDialogPopWindow extends PopupWindow {

    private Context mContext;
    private View view;
    private Button cancelBtn;


    public MyDialogPopWindow(Activity mContext, View.OnClickListener clickListener) {  //这里参数还可以传入ClickListener
        this.mContext = mContext;

        this.view = LayoutInflater.from(mContext).inflate(R.layout.dialog_win_layout, null);

        this.setOutsideTouchable(true);  //外部可点击
        this.setContentView(this.view);

        Window dialogWindow = mContext.getWindow();

        WindowManager m = mContext.getWindowManager();
        Display d = m.getDefaultDisplay(); //获取屏幕宽高

        WindowManager.LayoutParams p = dialogWindow.getAttributes(); //获取对话框当前参数

        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth((int) (d.getWidth() * 0.6));

        this.setFocusable(true);  //弹出框可点击

        cancelBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(clickListener);
    }

    /**
     * 设置取消按钮是否可点
     * @param enable
     */
    public void setCancelBtn(boolean enable) {
        cancelBtn.setEnabled(enable);
    }
}
