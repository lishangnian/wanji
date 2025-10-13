package com.enjoy.wanji.entity;

/**
 *  * 0-未知   1--行人   2--机动车
 */
public enum TrafficObjClassEnum {

    Unknown(0, "未知"),
    Pedestrian(1, "行人"),
    Vehicle(2, "机动车"),
    NoVehicle(3, "非机动车"),
    ;


    public final int key;
    public final String value;

    TrafficObjClassEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        for (TrafficObjClassEnum errorType : TrafficObjClassEnum.values()) {
            if (errorType.key == key) {
                return errorType.value;
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        for (TrafficObjClassEnum errorType : TrafficObjClassEnum.values()) {
            if (errorType.key == key) {
                return true;
            }
        }
        return false;
    }
}
