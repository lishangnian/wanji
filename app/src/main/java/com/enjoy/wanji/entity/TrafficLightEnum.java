package com.enjoy.wanji.entity;

/**
 0:无 1：红灯 2：绿灯 3：黄灯
 */
public enum TrafficLightEnum {

    NO(0, "无"),
    RED(1, "红灯"),
    GREEN(2, "绿灯"),
    YELLOW(3, "黄灯");


    public final int key;
    public final String value;

    TrafficLightEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
            for (TrafficLightEnum errorType : TrafficLightEnum.values()) {
                if (errorType.key == key) {
                    return errorType.value;
                }
            }
        return "";
    }

    public static boolean contains(int key) {
        for (TrafficLightEnum errorType : TrafficLightEnum.values()) {
            if (errorType.key == key) {
                return true;
            }
        }
        return false;
    }
}
