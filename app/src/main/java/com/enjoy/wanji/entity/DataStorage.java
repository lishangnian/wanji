package com.enjoy.wanji.entity;

import com.enjoy.wanji.util.ConstantsUtil;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {


    public static double lon = 0;
    public static double lat = 0;
    public static String heading = "0";
    public static String speed = "0";

    public static long rtk = 0;


    public static volatile int stopgo = 0; //无效、出发、停止 0 1 2
    public static volatile long stopGoTimeStamp = System.currentTimeMillis(); //用户点击stogo出发或结束按钮时标记时间戳

    public static int mapNum = 1;   //map序号
    public static String requestMapName = "0"; //请求指定map路径的名字

    public static Map<String, Object> areasNameWithEngKeyMap = new HashMap();
    public static Map<String, Object> areasNameWithChnKeyMap = new HashMap();

    /********************地图采集部分*********************/
    public static int collectMode = 0; //采集地图模式     0--否     1--开始采集   2---结束采集
    public static volatile String collectMapName = "";  //主页选中的轨迹名称
    //    public static volatile String deleteMapName = "";  //主页选中要删除的轨迹名称
    public static volatile int collectMapSpeed = 3;      //地图采集轨迹的速度,默认3Km/H
    public static volatile int collectMapLaneType = 1;   //道路属性 1-单车道  2-多道右 3-多道中 4-多道左  默认1单车道
    public static volatile int collectCleanEnable = 0; //地图采集清扫暂停，清扫使能  0--不使能 1--使能
    public static volatile int collectSweepMode = 2; //1--暂停清扫默认  2--使能清扫
    public static volatile int collectPathProperty = 0; // 道路属性 0 ：清扫路径   1:回垃圾站  2:会车库
    public static volatile int collectReverse = 0; // 道路属性 0 ：前进   1:倒车
    public static volatile int collectGetInOrOut = 0; // 进垃圾站时道路属性 0 ：进站   1:出站
    public static volatile int collectPathValid = 0; //道路是否有效   0----有效   1-----无效
    private static volatile int collectSelectedAreaNo = 1; //默认是1
    private static String collectSelectedArea = "";
    public static volatile int sideType = 0;           //靠边模式， 默认0--不贴边   1---左侧贴边  2---右侧贴边    3---两侧贴边

    public static volatile int collectAreaSpinnerPosition = 0;   //园区下拉框的的选择位置，用于保持

    public static void setCollectArea(String areaName) {
//        String numStr = ConstantsUtil.getNumStrFromStr(areaName);
//        collectSelectedAreaNo = Integer.parseInt(numStr);
//        Object areaNameObj = areasNameWithEngKeyMap.get(areaName);
        Object areaNameObj = areasNameWithChnKeyMap.get(areaName);
        if (areaNameObj != null) {
            collectSelectedArea = areaNameObj.toString();
        }

    }



    public static int mode = 1; //app模式 1--显示订阅的信息模式      2--采集地图模式
    public static volatile int page = 1; //1--主页     2--采集地图页面  3--遥控器页面

    //障碍物标志
    public static int obs = 0;    //0 -- 无   1--有
    //驾驶状态标志
    public static int driverStatus = 0;      //0--非自动驾驶  1-自动驾驶
    public static volatile long actuatorTimeStamp = System.currentTimeMillis();  //actuator的时间戳
    private static int carWorkMode = 0;   //0--等待中 1,清扫       2,去车库       3,去垃圾站
    public static volatile String battery = "0V";   //电压
    public static volatile int batterySoc = 0; //电量
    public static volatile String soc = "00%";   //电量
    private static long carWorkModeUpdateStamp = System.currentTimeMillis();
    public static volatile int pathError = 0; //报警   0--无   1---报警
    public static volatile int sysError = 0;//故障报警 gps无数据或gps状态为0--1  激光雷达无数据--2   毫米波雷达无数据--3  超声波雷达无数据--4  无数据--5-10
    public static volatile int carError = 0; //actor 中的error

    public static void setCarWorkMode(int workMode) {
        carWorkModeUpdateStamp = System.currentTimeMillis();
        carWorkMode = workMode;
    }

    public static int getCarWorkMode() {
        if (System.currentTimeMillis() - carWorkModeUpdateStamp > 5000) {
            //超过5秒没有更新工作模式，认为是0等待中
            return 0;
        }
        return carWorkMode;
    }


    /************扫地车部分****************/

    /**
     * 清扫点击 0：默认1：使能清扫 2：不清扫,主页面控制清扫部分
     */
    public static int BROOM_CLEAN_CLICK() {
        if (cleanEnable) {
            return broomCleanClick;
        } else {
            return 0;
        }
    }

    public static volatile int sysEnable = 0;  //使能位

    public static volatile boolean cleanEnable = false; //清扫使能
    public static volatile int broomCleanClick = 2;  //清扫点击 0：默认1：使能 2：不使能
    public static volatile int sweepStatus = 0; //得到的车上清扫状态   0---无   >0有

    public static volatile int dumpRubbishClick = 0; //清倒 0：默认     1：使能 ( >0)
    public static volatile int liftStatus = 0; //得到车的倾倒状态  0--无  >0有


    //以下三个指令最多只能有一个是有效--------为1，当一个是1，其他两个都是0
    public static volatile int goCleanClick = 0; //清倒作业   0----不清扫  1---清扫
    public static volatile int goGarageClick = 0; //返回停车场    0--不返回  1--返回
    public static volatile int goGarbageStationClick = 0; //返回垃圾站  0--不返回  1--返回
    public static volatile int goPauseClick = 0;  //暂停  0---不暂停  1--暂停
    public static volatile int dumpRubbishMode = 0; // 0---演示　1----实际倾倒

    public static boolean setGoCleanClick() {
        //要变为1，其余两个为0
        if (goCleanClick == 0) {
            goGarageClick = 0; //返回停车场    0--不返回  1--返回
            goGarbageStationClick = 0;
            goCleanClick = 1;
        } else goCleanClick = 0;
        return goCleanClick > 0;
    }

    public static boolean setGoGarageClick() {
        if (goGarageClick == 0) {
            goCleanClick = 0; //返回停车场    0--不返回  1--返回
            goGarbageStationClick = 0;
            goGarageClick = 1;
        } else goGarageClick = 0;
        return goGarageClick > 0;
    }

    public static boolean setGoGarbageStationClick() {
        if (goGarbageStationClick == 0) {
            goCleanClick = 0; //清倒作业   0----不清扫  1---清扫
            goGarageClick = 0; //返回停车场    0--不返回  1--返回
            goGarbageStationClick = 1;
        } else goGarbageStationClick = 0;
        return goGarbageStationClick > 0;
    }

    public static boolean setGoPauseClick() { //点击暂停
        goPauseClick = goPauseClick == 0 ? 1 : 0;
        return goPauseClick > 0; // >0为已经点击激活
    }

    public static boolean presentationClick() { //点击暂停
        dumpRubbishMode = dumpRubbishMode == 0 ? 1 : 0;
        return dumpRubbishMode < 1; //          0 演示　１　实际倾倒
    }


    //所有路径地图的json集合
    public static List<String> roadsNameList = new ArrayList<>();
    public static BlockingQueue<String> nameBlockQueue = new ArrayBlockingQueue<>(128); //先进先出
    public static Map<String, JSONObject> roadsMap = new LinkedHashMap<>();     //使用LinkedHashMap会按插入顺序排列
    //    public static volatile String selectMapName = "";  //选中的地图名称
    public static volatile String initSelectMapName = "";  //初步选中的地图名称

    // private static Map<String, String> roadsNameCh_EngMap = new HashMap<>(); //装入地图名字，key-value分别为汉字名和英文名
    private static List<String> mapName_CH_list = new ArrayList<>();   //地图汉语名字list
    private static List<String> mapAreaList = new ArrayList<>();        //作业区名字list
    private static volatile String selectedToDeleteMapName = "";     //选中的要删除的轨迹
    //    private static Set<String> mapAreaSet = new HashSet<>();
    private static Map<String, List<String>> areaAndListMap = new ConcurrentHashMap<>(); //用ConcurrentHashMap是因为存在线程安全问题


    private static volatile int selectMapNO = 1;
    private static volatile int selectFileDirectoryAreaNO = 1;

    private static String selectedArea = "";

//    public static void addRoadArea(String area) {
//        mapAreaSet.add(area);
//    }

    //把路径的区域和路径放入HashMap中
    public static void addRoadAreaAndRoads(String area, String roadName) {
        List<String> roadList = areaAndListMap.get(area);
        if (roadList == null) {
            roadList = new ArrayList();
        }
        if (!roadName.startsWith("maping_")) {  //如果是清扫路段则加入
            roadList.add(roadName);
        }
        areaAndListMap.put(area, roadList);
    }


    /**
     * 获取园区列表
     *
     * @return
     */
    public static List<String> getMapAreaList() {
        List<String> areaList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : areaAndListMap.entrySet()) {
            String area = entry.getKey();
            Object areaNameObj = areasNameWithEngKeyMap.get(area);
            if (areaNameObj != null) {
                areaList.add(areaNameObj.toString());
            }
        }
        return areaList;
    }

    public static List<String> getMapAreaListForCollectMap() {
        List<String> areaList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : areaAndListMap.entrySet()) {
            String area = entry.getKey();
            Object areaObj = areasNameWithEngKeyMap.get(area);
            if (areaObj != null) {
                areaList.add(areaObj.toString());
            }
        }
        //文件夹没有，但是数库中有的园区，要给加上
        for (Map.Entry entry : areasNameWithChnKeyMap.entrySet()) {
            if (!areaList.contains(entry.getKey())) {
                areaList.add(entry.getKey().toString());
            }
        }
        return areaList;
    }


    public static List<String> getMapName_CH_list() {
        List<String> resultList = new ArrayList<>();
        String areaName = getSelectAreaName(); //选择当前选中的区域
        Object areaObj = areasNameWithChnKeyMap.get(areaName);
        if (areaObj != null) {
            areaName = areaObj.toString();
        }
        List<String> nameList = areaAndListMap.get(areaName); //获取区域中所有路径
        Set<String> numStrSet = new TreeSet();
        if (nameList != null) {
            for (String name : nameList) {
                String numStr = ConstantsUtil.getNumStrFromStr(name);
                numStrSet.add(numStr);
            }
        }
        for (String str : numStrSet) {  //给道路按照数字排序
            resultList.add("清扫" + str);
        }
        return resultList;
    }


    public static void setSelectMapNo(String mapNameItem) {
        String numStr = ConstantsUtil.getNumStrFromStr(mapNameItem);
        selectMapNO = Integer.parseInt(numStr);
    }

    public static void setSelectAreaNo(String areaNameItem) {
        if ("".equals(areaNameItem.trim())) {
            return;
        }

        selectedArea = areaNameItem;
    }

    public static String getSelectMapName() {
        return "maping" + selectMapNO;
    }

    public static String getSelectAreaName() {
        Object selectAreaObj = areasNameWithChnKeyMap.get(selectedArea);

        if (selectAreaObj != null) {
            return selectAreaObj.toString();
        } else {
            return "";
        }
    }

    public static void putRoadsNameCh_EngMap(String nameCh, String nameEng) {
//        roadsNameCh_EngMap.put(nameCh, nameEng);
        mapName_CH_list.add(nameCh);
    }

    public static void cleanMapNameForSpinner() {
        mapName_CH_list.clear();
//        roadsNameCh_EngMap.clear();
        selectedToDeleteMapName = "";
        areaAndListMap.clear();
        selectFileDirectoryAreaNO = 1;
        selectMapNO = 1;
    }

}
