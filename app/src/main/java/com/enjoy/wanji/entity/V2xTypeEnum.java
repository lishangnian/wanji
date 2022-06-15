package com.enjoy.wanji.entity;

public enum V2xTypeEnum {
    EMPTY(0, "无"),
    FCW(1, "前方碰撞预警"), //
    ICW(2, "交叉路口碰撞预警"),
    LTA(3, "左转辅助"),
    BSW(4, "盲区预警/变道预警"),
    DNPW(5, "逆向超车预警"),
    EBW(6, "紧急制动预警"),
    AVW(7, "异常车辆提醒"),
    CLW(8, "车辆失控预警"),
    HLW(9, "道路危险状况提示"),
    SLW(10, "限速预警"),
    RLVW(11, "闯红灯预警"),
    VRUCW(12, "弱势交通参与者碰撞预警"),
    GLOSA(13, "绿波车速引导"),
    IVS(14, "车内标牌"),
    TJW(15, "前方拥堵提醒"),
    EVW(16, "紧急车辆提醒");

    public final int key;
    public final String value;

    V2xTypeEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        if (key != 0) {
            for (V2xTypeEnum v2x : V2xTypeEnum.values()) {
                if (v2x.key == key) {
                    return v2x.value;
                }
            }
        }
        return "";
    }
}
