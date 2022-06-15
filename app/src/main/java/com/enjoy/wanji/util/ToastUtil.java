/**
 * 
 */
package com.enjoy.wanji.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

	public static void showShort(Context context, String info) {
		Toast toast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}
	public static void showShort(Context context, int info) {
		Toast toast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}

	public static void showLong(Context context, String info) {
		Toast toast = Toast.makeText(context, info, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}
	public static void showLong(Context context, int info) {
		Toast toast = Toast.makeText(context, info, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}
}
