package com.enjoy.sweeper;

public class Global {
    static boolean pubtaskflag = false;
    static boolean subtaskflag = false;

    //加载地图标志符
    static volatile boolean loadRoadsFlag = true;
    static volatile boolean deleteRoadFlag = false; //删除地图标志

    static float posX = 0, posY = 0, velocity = 0, hdg = 0; // rad
    static int nextid = 0;
    static int onpathid = 0;


    public static volatile boolean connectFlag = false;

    static double gpsX = 0;
    static double gpsY = 0;

    static String name = "name";
    static String connectROS = "connectROS";
    static String msg = "msg";
    static String stopOrGoMsg = "stopOrGoMsg";
    static String subMessage = "start_sub_message"; //开始订阅消息

    static String mapSpeedMsg = "mapSpeedMsg";
    static String subRoadsMap = "subRoadsMap";
    static String deleteRoadsMap = "deleteRoadsMap";

    static String connectTry = "connectTry";
    static String connectSuccess = "connectSuccess";
    static String connectError = "connectError";
    static String connectDis = "disConnect";

    static String sensorgps = "sensorgps";
    static String loadRoads = "loadRoads";
    static String collectMapActivityRtk = "mode2_rtk";

    static String driverStatus = "DRIVER_STATUS"; //驾驶状态广播msg
    static String obsStatus = "OBS_STATUS"; //障碍物状态广播msg

    static String alterFromCar = "Alter_From_Car"; //一键报警

    static String removeAlterFromCar = "Remove_Alter_From_Car"; //解除一键报警

    static String collectMapForCar = "collectMapForCar";
    static String controlByMan = "controlByMan";//进入遥控器模式

    static public void setGPSVehicle(double igpsX, double igpsY, double heading) {
        // heading - deg   hdg - rad
        gpsX = igpsX;
        gpsY = igpsY;
        hdg = (float) (heading * Math.PI / 180);
        posX = 200 + 100 + (float) ((igpsY - 34.7777) * 2976.19);
        posY = 30 + (float) ((igpsX - 113.8363) * 2274.31);
    }

    static public void setOnPathId(int segid) {
        switch (segid) {
            // arc belongs to the next path
            case 1:
                onpathid = 1;
                break;
            case 2:
            case 3:
                onpathid = 7;
                break;
            case 4:
            case 5:
            case 6:
            case 26:
                onpathid = 8;
                break;
            case 7:
                onpathid = 9;
                break;
            case 8:
            case 9:
                onpathid = 10;
                break;
            case 10:
            case 11:
            case 12:
            case 27:
                onpathid = 11;
                break;
            case 13:
                onpathid = 12;
                break;
            case 22:
            case 23:
                onpathid = 2;
                break;
            case 24:
            case 14:
                onpathid = 3;
                break;
            case 15:
            case 16:
            case 17:
            case 25:
                onpathid = 4;
                break;
            case 19:
                onpathid = 5;
                break;
            case 20:
                onpathid = 6;
                break;
        }
    }
}
