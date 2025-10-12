package com.enjoy.wanji.entity;

/**
 * 0-P  1-R  2-N  3-D
 */
public enum GearEnum {

    P(0, "P"),
    R(1, "R"),
    N(2, "N"),
    D(3, "D");


    public final int key;
    public final String value;

    GearEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        for (GearEnum errorType : GearEnum.values()) {
            if (errorType.key == key) {
                return errorType.value;
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        for (GearEnum errorType : GearEnum.values()) {
            if (errorType.key == key) {
                return true;
            }
        }
        return false;
    }
}
