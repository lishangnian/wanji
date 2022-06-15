package com.enjoy.wanji.entity;

/**
 * 故障枚举
 */
public enum AttentionContentEnum {

    //V2X_
    V2X_EMPTY(0, "无"),
    V2X_FCW(1, "前方碰撞预警"), //
    V2X_ICW(2, "交叉路口碰撞预警"),
    V2X_LTA(3, "左转辅助"),
    V2X_BSW(4, "盲区预警/变道预警"),
    V2X_DNPW(5, "逆向超车预警"),
    V2X_EBW(6, "紧急制动预警"),
    V2X_AVW(7, "异常车辆提醒"),
    V2X_CLW(8, "车辆失控预警"),
    V2X_HLW(9, "道路危险状况提示"),
    V2X_SLW(10, "限速预警"),
    V2X_RLVW(11, "闯红灯预警"),
    V2X_VRUCW(12, "弱势交通参与者碰撞预警"),
    V2X_GLOSA(13, "绿波车速引导"),
    V2X_IVS(14, "车内标牌"),
    V2X_TJW(15, "前方拥堵提醒"),
    V2X_EVW(16, "紧急车辆提醒"),

    //actuator 故障提醒
    错误101(101, "线控故障，请紧急接管"),
    ERROR102(102, "线控故障，请紧急接管"),
    ERROR103(103, "线控故障，请紧急接管"),
    ERROR104(104, "线控故障，请紧急接管"),
    ERROR105(105, "线控故障，请紧急接管"),
    ERROR106(106, "线控故障，请紧急接管"),
    ERROR107(107, "线控故障，请紧急接管"),
    ERROR108(108, "线控故障，请紧急接管"),
    ERROR109(109, "线控故障，请紧急接管"),
    ERROR201(201, "传感器故障，请紧急接管"),
    ERROR202(202, "传感器故障，请紧急接管"),
    ERROR203(203, "传感器故障，请紧急接管"),
    ERROR204(204, "传感器故障，请紧急接管"),
    ERROR205(205, "传感器故障，请紧急接管"),
    ERROR206(206, "传感器故障，请紧急接管"),
    ERROR207(207, "传感器故障，请紧急接管"),
    ERROR208(208, "传感器故障，请紧急接管"),
    ERROR219(209, "传感器故障，请紧急接管"),
    ERROR210(210, "传感器故障，请紧急接管"),
    ERROR211(211, "传感器故障，请紧急接管"),
    ERROR212(212, "GPS信号差，请注意接管"),
    ERROR301(301, "车辆偏离轨迹，请注意"),
    ERROR302(302, "车辆偏离轨迹，请接管"),
    ERROR303(303, "车辆超过设定速度，请注意"),
    ERROR304(304, "车辆超过设定速度，请接管"),
    ERROR_CONNECT305(305, "连接中断，请接管"),

    //连接提示
    CONNECT_SUCCESS(1001, "连接成功"),
    DISCONNECT(1002, "连接断开"),

    //进入或退出自动驾驶
    SWITCH_AUTO_DRIVE(1003, "进入自动驾驶，请注意"),
    SWITCH_MANUAL_DRIVE(1004, "退出自动驾驶，请注意"),

    //注意刹车
    BRAKE_ATTENTION(1005, "刹车请注意");


    public final int key;
    public final String value;

    AttentionContentEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        if (key != 0) {
            for (AttentionContentEnum errorType : AttentionContentEnum.values()) {
                if (errorType.key == key) {
                    return errorType.value;
                }
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        if (key != 0) {
            for (AttentionContentEnum errorType : AttentionContentEnum.values()) {
                if (errorType.key == key) {
                    return true;
                }
            }
        }
        return false;
    }
}
