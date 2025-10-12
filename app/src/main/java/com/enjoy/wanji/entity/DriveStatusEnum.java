package com.enjoy.wanji.entity;

/**
 * 弹框语音提示类型枚举
 * 1--连接成功 2--连接失败   3--进入自驾
 * 4--退出自驾 5--刹车注意   6--故障报警  7--v2x提示
 */
public enum DriveStatusEnum {

    NO_AUTO(0, "人工驾驶"),
    AUTO(1, "自动驾驶");


    public final int key;
    public final String value;

    DriveStatusEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        if (key != 0) {
            for (DriveStatusEnum errorType : DriveStatusEnum.values()) {
                if (errorType.key == key) {
                    return errorType.value;
                }
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        if (key != 0) {
            for (DriveStatusEnum errorType : DriveStatusEnum.values()) {
                if (errorType.key == key) {
                    return true;
                }
            }
        }
        return false;
    }
}
