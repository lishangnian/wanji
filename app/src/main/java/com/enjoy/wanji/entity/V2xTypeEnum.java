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
    EVW(16, "紧急车辆提醒"),

    V34(34,"事故高发路段减速预警"),
    V242(242,"学校路段减速预警"),
    V501(501,"前方道路临时施工预警"),
    V103(103,"前方突发事件预警"),
    V707(707,"前方拥堵预警"),
    V415(415,"行人/非机动车闯红灯预警"),
    V904(904,"机动车逆向行驶预警"),
    V425(425,"非机动车逆向行驶预警"),
    V435(435,"非机动车占用机动车道预警"),
    V311(311,"低能见度条件下信号红色提示"),
    V305(305,"雾环境下速度控制与风险预警"),
    V301(301,"降雨境下速度控制与风险预警"),
    V407(407,"路面积水预警"),
    V507(507,"车道临时占用检测/预警"),
    V903(903,"道路交通事件提醒"),
    V902(902,"慢行交通预警"),
    V990(990,"动态限速"),
    V598(598,"可变车道"),
    V599(599,"潮汐车道");

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
