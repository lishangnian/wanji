package com.enjoy.wanji.entity;

/**
 * 泊车方位枚举
 */
public enum StopOrientationEnum {

    MIDDLE(1, "中"),
    LEFT(0, "左"),
    RIGHT(2, "右");


    public final int key;
    public final String value;

    StopOrientationEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {

        for (StopOrientationEnum stopType : StopOrientationEnum.values()) {
            if (stopType.key == key) {
                return stopType.value;
            }
        }

        return "";
    }
}
