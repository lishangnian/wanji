package com.enjoy.wanji.entity;

/**
 * 泊车方位枚举
 */
public enum StopOrientationEnum {
    RIGHT(0, "右"),
    LEFT(1, "左");

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
