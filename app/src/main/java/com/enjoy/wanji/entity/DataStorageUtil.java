package com.enjoy.wanji.entity;

import com.enjoy.wanji.EnjoyTrainShipApplication;
import com.enjoy.wanji.Global;
import com.enjoy.wanji.data.Common;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DataStorageUtil {


    private static List<String> collectMapNameList = new ArrayList<>();
    private static List<String> speedList = new ArrayList<>();
    private static List<String> stopPropertyList = new ArrayList<>();
    private static List<String> stopTimeList = new ArrayList<>();
    private static List<String> stopIndexList = new ArrayList<>(); //停车索引列表
    private static List<String> searChdisList = new ArrayList<>();    //侧边道路宽度
    private static List<String> mergeLaneList = new ArrayList<>();    //道路切换属性
    private static List<String> stopSideRoadWidthList = new ArrayList<>();    //道路切换属性
    private static List<String> mapLaneList = new ArrayList<>();    //车道属性

    private static List<String> exSpeedList = new ArrayList<>();// 期望速度
    private static List<String> laneWidthList = new ArrayList<>(); //道路宽度
    private static List<String> temporaryStopList = new ArrayList<>(); //临时停车
    private static List<String> laneStatusList = new ArrayList<>(); //车道状态
    private static List<String> leftRightSafeDisList = new ArrayList<>(); // 左， 右安全距离
//    1.2 1.4 1.5 1.7 2 2.5 3 3.5 4


    /**
     * 车道属性  1--5
     *
     * @return
     */
    public static List<String> getMapLaneList() {
//        1--4 单道， 右车道、中间车道、左车道
        if (mapLaneList.size() == 0) {
            mapLaneList.add("1-单道");
            mapLaneList.add("2-右车道");
            mapLaneList.add("3-中间车道");
            mapLaneList.add("4-左车道");
            mapLaneList.add("5-借道");
        }
        return mapLaneList;
    }

    /**
     * 临时停车距离
     *
     * @return
     */
    public static List<String> getStopSideRoadWidthList() {
        if (stopSideRoadWidthList.size() == 0) {
            for (int i = 0; i < 10; i++) {
                stopSideRoadWidthList.add(0.5f * i + "");
            }
        }
        return stopSideRoadWidthList;
    }

    /*
     * 道路切换属性
     * 1：默认
     * 2：切换SLAM
     * 3：左并道
     * 4：右并道
     * 5：结束并道
     * @return
     */
    public static List<String> getMergeLaneList() {
//        0直行，1左并道，2右并道
        if (mergeLaneList.size() == 0) {
            mergeLaneList.add("1-默认");
            mergeLaneList.add("2-切换SLAM");
            mergeLaneList.add("3-左并道");
            mergeLaneList.add("4-右并道");
            mergeLaneList.add("5-结束并道");
        }
        return mergeLaneList;
    }

    /*
     * 侧边左右道路安全 默认1.7
     * 单位 m
     * 1.2 1.4 1.5 1.7 2 2.5 3 3.5 4
     *
     * @return
     */
    public static List<String> getSearChdisList() {
        if (searChdisList.size() == 0) {
            searChdisList.add("1.2");
            searChdisList.add("1.4");
            searChdisList.add("1.5");
            searChdisList.add("1.7");
            searChdisList.add("2");
            searChdisList.add("2.5");
            searChdisList.add("3");
            searChdisList.add("3.5");
            searChdisList.add("4");
        }

        return searChdisList;
    }

    /**
     * 停车索引列表
     * 1到20
     */
    public static List<String> getStopIndexList() {
        if (stopIndexList.size() == 0) {
            for (int i = 0; i <= 20; i++) {
                stopIndexList.add(i + "");
            }
        }
        return stopIndexList;
    }


    /**
     * 停车时间列表
     */
    public static List<String> getStopTimeList() {
        if (stopTimeList.size() == 0) {
            for (int i = 1; i <= 10; i++) {
                stopTimeList.add(i * 5 + "");
            }
        }
        return stopTimeList;
    }

    /**
     * 停车时间列表
     */
    public static List<String> getStopPropertyList() {
        if (stopPropertyList.size() == 0) {
            stopPropertyList.add("0-不采集");
            stopPropertyList.add("1-垂直泊车点");
            stopPropertyList.add("2-水平泊车点");
            stopPropertyList.add("3-站点停车点");
            stopPropertyList.add("4-红绿灯停车点");
            stopPropertyList.add("5-模式切换点");
            stopPropertyList.add("6-倒车目标点");
        }
        return stopPropertyList;
    }

    /**
     * 速度列表（道路属性）
     */
    public static List<String> getRoadPropertyList() {
        if (speedList.size() == 0) {
            speedList.add("0-默认");
            speedList.add("1-普通道路");
            speedList.add("2-路口");
            speedList.add("3-环岛");
            speedList.add("4-匝道");
            speedList.add("5-水平泊车");
            speedList.add("6-垂直泊车");
            speedList.add("7-隧道");
            speedList.add("8-路口中");
            speedList.add("9-路口后段");
            speedList.add("10-切换倒车");
        }
        return speedList;
    }


    /*
     * 左 或 右道路距离
     * default  3.5
     * @return
     *
     * 0  2.5  2.8  3  3.2  3.3  3.5 3.6 3.8
     */
    public static List<String> getLeftRightWidthDisList() {
        if (leftRightSafeDisList.size() == 0) {
            leftRightSafeDisList.add("0");
            leftRightSafeDisList.add("2.5");
            leftRightSafeDisList.add("2.8");
            leftRightSafeDisList.add("3");
            leftRightSafeDisList.add("3.2");
            leftRightSafeDisList.add("3.3");
            leftRightSafeDisList.add("3.5");
            leftRightSafeDisList.add("3.6");
            leftRightSafeDisList.add("3.8");
        }
        return leftRightSafeDisList;
    }

    /*
     * laneStatus
     * 1：正常道路
     * 2：颠簸道路
     * 3：闸机口
     * 道路状态
     * @return
     */
    public static List<String> getLaneStatusList() {
        if (laneStatusList.size() == 0) {
            laneStatusList.add("1-正常道路");
            laneStatusList.add("2-颠簸道");
            laneStatusList.add("3-闸机口");
        }

        return laneStatusList;
    }

    /*
     * 临时停车
     * @return
     */
    //    0  0.5  1  1.5 2 2.5 3 3.5 4 4.5 5  temporaryStop
    public static List<String> getTemporaryStopList() {
        if (temporaryStopList.size() == 0) {
            for (int i = 0; i <= 10; i++) {
                temporaryStopList.add(0.5 * i + "");
            }
        }
        return temporaryStopList;
    }


    /*
     * 车道宽度
     * @return
     * 0  2.5  2.8  3  3.2  3.3  3.5 3.6 3.8
     */
    public static List<String> getLaneWidthList() {
        if (laneWidthList.size() == 0) {
            laneWidthList.add(0 + "");
            laneWidthList.add(2.5 + "");
            laneWidthList.add(2.8 + "");
            laneWidthList.add(3 + "");
            laneWidthList.add(3.2 + "");
            laneWidthList.add(3.3 + "");
            laneWidthList.add(3.5 + "");
            laneWidthList.add(3.6 + "");
            laneWidthList.add(3.8 + "");
        }
        return laneWidthList;
    }


    //    5,7,10,15,20,25,30,35,40,50,60
    /*
     * 期望速度选择
     */
    public static List<String> getExSpeedList() {
        if (exSpeedList.size() == 0) {
            exSpeedList.add(5 + "");
            exSpeedList.add(7 + "");
            exSpeedList.add(10 + "");
            exSpeedList.add(15 + "");
            exSpeedList.add(20 + "");
            exSpeedList.add(25 + "");
            exSpeedList.add(30 + "");
            exSpeedList.add(35 + "");
            exSpeedList.add(40 + "");
            exSpeedList.add(50 + "");
            exSpeedList.add(60 + "");
        }
        return exSpeedList;
    }

    /**
     * 获取
     *
     * @return
     */
    public static List<String> getCollectMapNameList() {
        if (collectMapNameList.size() == 0) {
            for (int i = 1; i < 10; i++) {
                collectMapNameList.add(i + "");
            }
        }
        return collectMapNameList;
    }


    /**
     * 返回采集地园区列表
     */
    public static List<String> getMapZoneMainList() {
        List<String> list = new ArrayList<>();
        if (!Global.connectFlag) {//没有连接，直接返回
            return list;
        }
        for (Map.Entry<String, JSONObject> entry : DataStorageFromPC.roadsMap.entrySet()) {
            String zoneNameKey = entry.getKey();
            String zoneName = EnjoyTrainShipApplication.sharedPreferences.getString(zoneNameKey, null);
            zoneName = zoneName == null ? zoneNameKey : zoneName;
            list.add(zoneName);
        }

        //给园区名按照数字排序
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return getZoneNum(s1) - getZoneNum(s2);
            }
        });

        return list;
    }

    /**
     * 返回采集地图园区列表
     */
    public static List<String> getMapZoneCollectMapList() {
        List<String> list = new ArrayList<>();
        if (!Global.connectFlag) {//没有连接，直接返回
            return list;
        }
        int zoneNum = 0;
        List<Integer> arrList = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : DataStorageFromPC.roadsMap.entrySet()) {
            String zoneNameKey = entry.getKey();
            String zoneName = EnjoyTrainShipApplication.sharedPreferences.getString(zoneNameKey, null);
            zoneName = zoneName == null ? zoneNameKey : zoneName;
            list.add(zoneName);

            String zoneNameIntStr = zoneNameKey.replaceAll(Common.ZONE_HEAD, "");   //获取园区编号的数字
            arrList.add(Integer.parseInt(zoneNameIntStr));
        }

        if (arrList.size() == 0) {   //当前没有园区，从园区1开始
            list.add(Common.ZONE_HEAD + 1);
            return list;
        }
        int[] arr = new int[arrList.size()];
        for (int i = 0; i < arrList.size(); i++) {
            arr[i] = arrList.get(i);
        }
        Arrays.sort(arr);
        boolean flag = false;
        for (Integer i : arr) {
            if (i - 1 > zoneNum) {
                zoneNum = i - 1;  //找到需新增园区数字
                flag = true;
                break;
            }
            zoneNum = i;
        }
        if (!flag) {   //上面园区数组中的数字正好是依次增加的，没有跳的
            zoneNum++;
        }
        list.add(Common.ZONE_HEAD + zoneNum);


        //给园区名按照数字排序
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return getZoneNum(s1) - getZoneNum(s2);
            }
        });

        return list;
    }


    /*
     * 车道状态
     *    laneStatus
     */
    public static int getLaneStatus(String laneStatus) {
        return getCommonProperty(laneStatus);
    }


    /**
     * //提取园区编号
     *
     * @param zoneName 例： 原始的为yuanqu2或名字被修改了 2-公园
     * @return
     */
    public static int getZoneNum(String zoneName) {
        String[] zoneArr = zoneName.split("-");
        if (zoneArr.length > 1) { //此园区名称被重命名了,取前面的数字
            return Integer.parseInt(zoneArr[0]);
        } else {    //  此园区名称为原始的，找出数字即可
            return Integer.parseInt(zoneArr[0].replaceAll(Common.ZONE_HEAD, ""));
        }
    }

    public static String getZoneName() {
        if (DataStorageCollectMap.zoneName == 0) {  //如果是0，说明没有选中，返回空
            return "";
        }
        String zoneNameOri = Common.ZONE_HEAD + DataStorageCollectMap.zoneName;
        String zoneName = EnjoyTrainShipApplication.sharedPreferences.getString(zoneNameOri, null);
        zoneName = zoneName == null ? zoneNameOri : zoneName;
        return zoneName;
    }


    /**
     * 车道属性
     *
     * @param laneStr
     * @return
     */
    public static int getMapLane(String laneStr) {
        return getCommonProperty(laneStr);
    }

    /**
     * 道路切换属性
     *
     * @param mergeLaneStr
     * @return
     */
    public static int getMergeLane(String mergeLaneStr) {
        return getCommonProperty(mergeLaneStr);
    }

    /**
     * 获取停车点属性
     */
    public static int getStopProperty(String stopPropertyStr) {
        return getCommonProperty(stopPropertyStr);
    }

    /**
     * 获取道路属性
     *
     * @param roadPropertyStr
     * @return
     */
    public static int getRoadProperty(String roadPropertyStr) {
        return getCommonProperty(roadPropertyStr);
    }

    private static int getCommonProperty(String str) {
        String[] strArr = str.split("-");
        return Integer.parseInt(strArr[0]);
    }


}
