package com.enjoy.wanji.entity;

/**
 * 故障枚举
 */
public enum ErrorContentEnum {

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
    ERROR_CONNECT305(305, "连接中断，请接管");


    public final int key;
    public final String value;

    ErrorContentEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(int key) {
        if (key != 0) {
            for (ErrorContentEnum errorType : ErrorContentEnum.values()) {
                if (errorType.key == key) {
                    return errorType.value;
                }
            }
        }
        return "";
    }

    public static boolean contains(int key) {
        if (key != 0) {
            for (ErrorContentEnum errorType : ErrorContentEnum.values()) {
                if (errorType.key == key) {
                    return true;
                }
            }
        }
        return false;
    }
}
