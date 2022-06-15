package com.enjoy.wanji.entity;

/**
 * 弹框语音提示类型枚举
 * 1--连接成功 2--连接失败   3--进入自驾
 * 4--退出自驾 5--刹车注意   6--故障报警  7--v2x提示
 */
public enum AttentionTypeEnum {

    CONNECT_SUCCESS(1, "连接成功"),
    DISCONNECT(2, "连接断开"),
    AUTO_DRIVE(3, "进入自动驾驶"),
    MANUAL_DRIVE(4, "退出自动驾驶"),
    BRAKE(5, "刹车请注意"),
    ERROR(6, "故障预警"),
    V2X(7, "v2x");


    public final int key;
    public final String value;

    AttentionTypeEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        if (key != 0) {
            for (AttentionTypeEnum errorType : AttentionTypeEnum.values()) {
                if (errorType.key == key) {
                    return errorType.value;
                }
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        if (key != 0) {
            for (AttentionTypeEnum errorType : AttentionTypeEnum.values()) {
                if (errorType.key == key) {
                    return true;
                }
            }
        }
        return false;
    }
}
