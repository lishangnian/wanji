package com.enjoy.wanji.util;

import android.content.Context;
import android.os.Environment;

public class OffLineMapUtils {
	/**
	 * 获取map 缓存和读取目录
	 */
	public static String getSdCacheDir(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			java.io.File fExternalStorageDirectory = Environment
					.getExternalStorageDirectory();
			java.io.File autonaviDir = new java.io.File(
					fExternalStorageDirectory, "amapSDK");
			boolean result = false;
			if (!autonaviDir.exists()) {
				result = autonaviDir.mkdir();
			}
			java.io.File minimapDir = new java.io.File(autonaviDir,
					"offlineMAP");
			if (!minimapDir.exists()) {
				result = minimapDir.mkdir();
			}
			return minimapDir.toString() + "/";
		} else {
			return "";
		}
	}
}
