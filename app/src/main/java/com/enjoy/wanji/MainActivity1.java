package com.enjoy.wanji;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.enjoy.wanji.data.Common;
import com.enjoy.wanji.entity.AttentionContentEnum;
import com.enjoy.wanji.entity.AttentionInfo;
import com.enjoy.wanji.entity.AttentionTypeEnum;
import com.enjoy.wanji.entity.DataStorage;
import com.enjoy.wanji.entity.DataStorageCollectMap;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.entity.DataStorageToPC;
import com.enjoy.wanji.entity.ErrorContentEnum;
import com.enjoy.wanji.entity.V2xTypeEnum;
import com.enjoy.wanji.service.EnjoySocketService;
import com.enjoy.wanji.util.AMapUtil;
import com.enjoy.wanji.util.ToastUtil;
import com.enjoy.wanji.vr3D.CarScene;
import com.enjoy.wanji.vr3D.ModelAgent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rajawali3d.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity1 extends Activity implements LocationSource, AMapLocationListener,
        GeocodeSearch.OnGeocodeSearchListener, OnMapTouchListener {//定位接口
    private static final String TAG = "PageActivityMain1";
    private static String mediaTag = "media_tag";

    private Context mContext;
    private AMap aMap;//地图控制器类
    private MapView mapView;
    private OnLocationChangedListener mListener;

    private SurfaceView surfaceView;
    private CarScene carScene;


    private UiSettings uiSettings;

    /********************************************************************************/
    static AlertDialog.Builder errorDialog = null;
    static MyDialogPopWindow dialogPopWindow = null;
    TextView titleTxt, msgTxt, speedTxt, speedLimitTxt, gearTxt, socTxt;

    ImageView connectImg, leftLight, rightLight, driveImg, socImg;
    AnimationDrawable leftAnimation, rightAnimation;


    private ProgressDialog progDialog = null;
    private GeocodeSearch geocoderSearch;
    private String addressName;
    private Marker geoMarker;
    private Marker regeoMarker;

    private Marker carMarker;

    private String heading;
    private LatLng latLng = new LatLng(30.617127, 114.253258);
    private BitmapDescriptor normalRouteBlue = null;
    private BitmapDescriptor normalRouteGreen = null;
    private BitmapDescriptor normalRouteYellow = null;
    private BitmapDescriptor normalRouteGrey = null;
    private static Handler handler;


    //存储地图Poyline的list
//    private static Map<String, Polyline> aMapLineListMap = new HashMap<>();
    Polyline polyline = null;
    private static List<Polyline> polylineList = new ArrayList<>();


    private static Lock stopGoLock = new ReentrantLock();

    MainActivityDataReceiver mainActivityDataReceiver;
    private Bundle instanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置本activity长亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //设置屏幕格式为横屏
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        instanceState = savedInstanceState;
        mContext = getApplicationContext();
        mapView = findViewById(R.id.map); //获取地图控件引用
//        mapView.onCreate(savedInstanceState);// 此方法必须重写  创建地图,改方法耗时约200ms，放在initViewDelay里

        //3D动画界面初始化
        surfaceView = findViewById(R.id.rajawali_surface);
        carScene = new CarScene(mContext);
        surfaceView.setSurfaceRenderer(carScene);
        //初始化控件
        initView();

        //再初始化 耗时的组件
        handler.sendEmptyMessageDelayed(Common.ACTION_INIT_VIEW_DELAY, 2000);

    }


    /**
     * 绘制轨迹
     *
     * @param
     */
    private void drawRoadInMap( ) {
//        List<JSONObject> jsonList = DataStorageFromPC.zoneNameJsonListMap.get(zoneName);
//        if (jsonList == null || jsonList.size() == 0) {
//            return;
//        }
        //清除已有的轨迹
        for (Polyline line : polylineList) {
            line.remove();
        }

        JSONObject jsonObject = DataStorageFromPC.mappingJSON;

        List<LatLng> temp = new ArrayList();
        JSONArray pointsArray = (JSONArray) jsonObject.get("points");
        if (pointsArray == null || pointsArray.isEmpty()) {
                Log.i(TAG,"轨迹点没有啊！！");
                return;
        }

        //clearMarkers();   //清除 始终点标记
        int size = pointsArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject pointJson = (JSONObject) pointsArray.get(i);
            double lat = (Double) pointJson.get("lat");
            double lon = (Double) pointJson.get("lon");
            LatLng latLngPoint = ChangeLatlon.transform(lat, lon);
            temp.add(latLngPoint);
        }
        addStartEndMarker(temp.get(0), temp.get(temp.size() - 1));
        PolylineOptions po = new PolylineOptions().addAll(temp).setUseTexture(true).setCustomTexture(normalRouteBlue)
                .width(15).color(Color.argb(255, 0, 255, 1));
        Polyline poly = aMap.addPolyline(po);
        polylineList.add(poly);

        Log.i(TAG, "画路线完成");
    }


    /**
     * 初始化界面
     */
    private void initView() {

        DataStorage.mode = 1;    //订阅模式 1--显示订阅信息模式 2--采集地图模式
        connectImg = findViewById(R.id.connect_flag);
        leftLight = findViewById(R.id.turn_left_light_img);
        rightLight = findViewById(R.id.turn_right_light_img);
        leftLight.setImageResource(R.drawable.turn_left_animation);
        rightLight.setImageResource(R.drawable.turn_right_animation);
        leftAnimation = (AnimationDrawable) leftLight.getDrawable();
        rightAnimation = (AnimationDrawable) rightLight.getDrawable();

        driveImg = findViewById(R.id.auto_drive_img);
        socImg = findViewById(R.id.soc_img);
        speedTxt = findViewById(R.id.speed_txt);
        speedLimitTxt = findViewById(R.id.limit_speed_txt);
        gearTxt = findViewById(R.id.gear_txt);
        socTxt = findViewById(R.id.soc_txt);

        progDialog = new ProgressDialog(this);


        //路线选择下拉框部分
//        final List<String> parkList = new ArrayList<>();
//        for (int i = 1; i <= 3; i++) {
//            parkList.add(i + "");
//        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                myHandleMessage(msg.what);
            }
        };
    }

    private void delayInitView() {
        mapView.onCreate(instanceState);// 此方法必须重写  创建地图
        if (aMap == null) {
            aMap = mapView.getMap();
            geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //设置地图属性
            setUpMap();
            aMap.animateCamera(CameraUpdateFactory.zoomTo(18)); //放大等级
            this.normalRouteBlue = BitmapDescriptorFactory.fromAsset("blue.png");
            this.normalRouteGreen = BitmapDescriptorFactory.fromAsset("green.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("yellow.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("grey.png");

            //读取缓存经纬度数据  116.416797,40.037969      117.35513990,39.06378240,
            String lonStr = EnjoyTrainShipApplication.sharedPreferences.getString("lon", "117.35513990");
            String latStr = EnjoyTrainShipApplication.sharedPreferences.getString("lat", "39.06378240");
            Log.i(TAG, "获取缓存经纬度" + lonStr + "," + latStr);
            LatLng latLng1 = ChangeLatlon.transform(Double.parseDouble(latStr), Double.parseDouble(lonStr));
            aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng1)); //中心点

            /**
             *

             aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
             latLng1,       //目标位置的经纬度
             20,          //缩放级别
             80,  //可视区域的倾斜角，单位为度
             0   //可视区域指向方向，单位为角度。从正北向顺时针计算，0-360
             )));
             */

        }

        AMapLocationClient.updatePrivacyAgree(mContext, true);
        AMapLocationClient.updatePrivacyShow(mContext, true, true);
        try {
            geocoderSearch = new GeocodeSearch(this);
        } catch (AMapException e) {
            Log.e(TAG, "new GeocodeSearch error");
//            throw new RuntimeException(e);
        }
        geocoderSearch.setOnGeocodeSearchListener(this);

        carScene.initModelNPC();  //初始化3D中的NPC

        //注册广播接收器
        mainActivityDataReceiver = new MainActivityDataReceiver();
        //接收器设置指定action
        IntentFilter filter = new IntentFilter();
        filter.addAction(Common.MAIN_RECEIVER_ACTION);
        registerReceiver(mainActivityDataReceiver, filter);

        //启动连接
        Intent intent = new Intent(MainActivity1.this, EnjoySocketService.class);
        intent.putExtra(Common.ACTION_NAME, Common.ACTION_CONNECT);
        startService(intent);
    }

    /**
     * 园区重命名编辑框
     */
    private void tipViewShow() {
        LinearLayout editeLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.edit_zone_layout, null);

        final EditText roadNameEdt = editeLayout.findViewById(R.id.zone_name_edt);
        TextView roadNumTxt = editeLayout.findViewById(R.id.zone_num_txt);
        roadNumTxt.setText("园区编号：" + DataStorageToPC.zoneName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity1.this);
        builder.setTitle("园区命名");
        builder.setView(editeLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int num = DataStorageToPC.zoneName;
                Log.i("vvvvvvvvvvvvvvvv", "get num = " + num);
                Editable roadEdt = roadNameEdt.getText();
                if (roadEdt == null) {
                    ToastUtil.showShort(MainActivity1.this, "名称不可为空");
                    return;
                }
                String zoneNameNew = roadEdt.toString();
                if ("".equals(zoneNameNew)) {
                    ToastUtil.showShort(MainActivity1.this, "名称不可为空");
                    return;
                }
                //名称存入缓存
                zoneNameNew = DataStorageToPC.zoneName + "-" + zoneNameNew;
                EnjoyTrainShipApplication.editor.putString(Common.ZONE_HEAD + DataStorageToPC.zoneName, zoneNameNew);
                EnjoyTrainShipApplication.editor.commit();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }


    /**
     * 删除轨迹调用
     */
    private void deleteRoadsSets() {
        if (DataStorageToPC.zoneName == 0) {
            ToastUtil.showShort(getApplicationContext(), "先选择路线");
            return;
        }
        AlertDialog.Builder deleteRoadsBuild = new AlertDialog.Builder(this);
        deleteRoadsBuild.setTitle("提示");
        deleteRoadsBuild.setMessage("删除轨迹当前轨迹" + "?");
        deleteRoadsBuild.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Global.deleteRoadFlag = true;
                handler.sendEmptyMessageDelayed(Common.ACTION_REFRESH, 2000);  //删除轨迹后重载
            }
        });
        deleteRoadsBuild.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Global.deleteRoadFlag = false;

            }
        });
        deleteRoadsBuild.create().show();

    }

    /**
     * 广播接收器类
     */
    public class MainActivityDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int what = intent.getIntExtra(Common.ACTION_NAME, -1);
            Log.i(TAG, "get broadcast onReceive intent what=" + what);
            handler.sendEmptyMessage(what);
        }
    }


    /**
     * 处理message
     *
     * @param msgWhat
     */
    private void myHandleMessage(int msgWhat) {
        switch (msgWhat) {
            case Common.ACTION_INIT_VIEW_DELAY:  //初始化耗时组件
                delayInitView();
                break;
            case Common.ACTION_REFRESH:
//                refresh("after delete");  //删除轨迹后的
                break;
            case Common.ACTION_UI_CONNECT:        //连接成功
                //加载轨迹
                Global.loadRoadsFlag = true;
                break;
            /**
             case Common.ACTION_UI_UPDATE_PARK:
             if (DataStorageToPC.getPark() < 1) {  //泊车按钮显示为默认状态
             parkBtn.setTextColor(Color.BLACK);
             parkBtn.setBackground(getResources().getDrawable(R.drawable.button_shape_default));
             }
             break;
             **/

            case Common.ACTION_UI_UPDATE: //更新UI
                Log.i(TAG, "更新UI");
                //连接状态
                if (Global.connectFlag) {
                    //改变连接logo连接颜色
                    connectImg.setImageDrawable(getResources().getDrawable(R.drawable.connect));
                    Log.i(TAG, "更新UI  连接标志");
                } else {
                    connectImg.setImageDrawable(getResources().getDrawable(R.drawable.disconnect));
                    //红绿灯
//                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_null));
                }

                //更新stopgo的按钮显示
                /*************************控显一体本项目不用*******
                 if (Global.connectFlag && System.currentTimeMillis() - 3000 > DataStorage.stopGoTimeStamp) {
                 //大于3000ms时间为了让stopgo能发送3s时间再和反馈做判断
                 if (DataStorageFromPC.driverStatus > 0 && DataStorageToPC.stopgo != 1) {
                 //已经是自驾状态但发送指令为不是出发  修改按钮显示为出发
                 //DataStorage.stopgo 0--默认  1--出发  2--结束    车反馈为自驾状态，则发送的出发命令变为默认
                 goRaBtn.setChecked(true);
                 DataStorage.stopgo = 0;
                 } else if (DataStorageFromPC.driverStatus == 0 && DataStorageToPC.stopgo == 1) {
                 //已经退出自驾状态但发送指令是出发  修改按钮显示为结束
                 endRaBtn.setChecked(true);
                 DataStorage.stopgo = 0;
                 }
                 }
                 **************************/


                //障碍物

                //  GPS显示

                //设置电量
                if (!Global.connectFlag) { //未连接
                    socTxt.setText(R.string.soc_default);
                    socImg.setImageDrawable(getResources().getDrawable(R.drawable.soc1));
                } else {
                    socTxt.setText(DataStorageFromPC.soc);
                    if (DataStorageFromPC.batterySoc <= 20 ){
                        socImg.setImageDrawable(getResources().getDrawable(R.drawable.soc1));
                    }else if (DataStorageFromPC.batterySoc > 20 && DataStorageFromPC.batterySoc <=50){
                        socImg.setImageDrawable(getResources().getDrawable(R.drawable.soc2));
                    }else if (DataStorageFromPC.batterySoc > 50 && DataStorageFromPC.batterySoc <=80){
                        socImg.setImageDrawable(getResources().getDrawable(R.drawable.soc3));
                    }else {
                        socImg.setImageDrawable(getResources().getDrawable(R.drawable.soc4));
                    }
                }
                //设置速度
                if (!Global.connectFlag) { //未连接
                    speedTxt.setText("0");
                    Log.i(TAG, "更新speed:" + DataStorageFromPC.speedStr);
//                    speedTxt.setText(DataStorageFromPC.speedStr);
                } else {
                    speedTxt.setText(DataStorageFromPC.speedStr);
                    carScene.updateLinesMove(DataStorageFromPC.velocity / 180f);
                }

                //驾驶状态
                if (!Global.connectFlag) { //未连接
                    driveImg.setImageDrawable(getResources().getDrawable(R.drawable.no_auto_drive));
                } else {
                    if (0 == DataStorageFromPC.driverStatus) {  // 人工
                        driveImg.setImageDrawable(getResources().getDrawable(R.drawable.no_auto_drive));
                    } else {  //1 自动
                        driveImg.setImageDrawable(getResources().getDrawable(R.drawable.auto_drive));
                    }
                }

                //档位
                if (Global.connectFlag) {
                    gearTxt.setText(DataStorageFromPC.Gear);
                    if ("P".equals(DataStorageFromPC.Gear) || "R".equals(DataStorageFromPC.Gear)) {
                        gearTxt.setTextColor(Color.RED);
                    } else {
                        gearTxt.setTextColor(getResources().getColor(R.color.deepGreen));
                    }
                } else {
                    gearTxt.setTextColor(Color.GRAY);
                }

                //设置转向  0--无  1--左转  2--右转
                if (!Global.connectFlag) {
                    if (leftAnimation.isRunning()) {
                        leftAnimation.stop();
                        leftLight.setImageResource(R.drawable.turn_left_animation);
                        leftAnimation = (AnimationDrawable) leftLight.getDrawable();
                    }
                    if (rightAnimation.isRunning()) {
                        rightAnimation.stop();
                        rightLight.setImageResource(R.drawable.turn_right_animation);
                        rightAnimation = (AnimationDrawable) rightLight.getDrawable();
                    }
                } else {
                    if (0 == DataStorageFromPC.turnLight) { //无转向
                        if (leftAnimation.isRunning()) {
                            leftAnimation.stop();
                            leftLight.setImageResource(R.drawable.turn_left_animation);
                            leftAnimation = (AnimationDrawable) leftLight.getDrawable();
                        }
                        if (rightAnimation.isRunning()) {
                            rightAnimation.stop();
                            rightLight.setImageResource(R.drawable.turn_right_animation);
                            rightAnimation = (AnimationDrawable) rightLight.getDrawable();
                        }

                    } else if (1 == DataStorageFromPC.turnLight) {  //左转
                        if (!leftAnimation.isRunning()) {
                            leftAnimation.start();
                        }
                        if (rightAnimation.isRunning()) {
                            rightAnimation.stop();
                            rightLight.setImageResource(R.drawable.turn_right_animation);
                            rightAnimation = (AnimationDrawable) rightLight.getDrawable();
                        }
                    } else if (2 == DataStorageFromPC.turnLight) {  //右转
                        if (!rightAnimation.isRunning()) {
                            rightAnimation.start();
                        }
                        if (leftAnimation.isRunning()) {
                            leftAnimation.stop();
                            leftLight.setImageResource(R.drawable.turn_left_animation);
                            leftAnimation = (AnimationDrawable) leftLight.getDrawable();
                        }
                    }
                }
                //限速
                if (DataStorageFromPC.velocity > DataStorageFromPC.speedLimit) {  //当前速度大于限速
                    //显示限速
                    speedLimitTxt.setText(DataStorageFromPC.speedLimitStr);
                    speedLimitTxt.setVisibility(View.VISIBLE); //可见
                    //设置限速外圈颜色变化
                    if (System.currentTimeMillis() % 1000 > 500){
                        speedLimitTxt.setBackground(getResources().getDrawable(R.drawable.limit_speed));
                    }else {
                        speedLimitTxt.setBackground(getResources().getDrawable(R.drawable.limit_speed0));
                    }
                } else {
                    speedLimitTxt.setVisibility(View.GONE);  //不可见
                }
                //重载按钮

                //故障报警
//                attentionDialogShow();

                Log.i(TAG, "主页面UI更新");
                break;
            case Common.ACTION_UI_3D:   //更新3D动画
                ModelAgent.updatePosition();
                break;
            case Common.ACTION_UI_LOCATION:   //更新位置定位
                double lon = DataStorageFromPC.lon;
                double lat = DataStorageFromPC.lat;
                heading = DataStorageFromPC.heading;
                if (lon > 0 && lat > 0) {
                    LatLng latLngPoint = ChangeLatlon.transform(lat, lon);
                    latLng = new LatLng(latLngPoint.latitude, latLngPoint.longitude);

                    // farLeft左上角  farRight右上角  nearLeft左下角 可从visibleRegion获取
                    VisibleRegion visibleRegion = aMap.getProjection().getVisibleRegion();
                    LatLngBounds latLngBounds = visibleRegion.latLngBounds;         //可视区域的四个顶点形成的经纬度范围
                    if (DataStorageFromPC.velocity > 0 || !latLngBounds.contains(latLng)) {
                        //车速大于0或判断位置点是否在视角范围内，如果不是则定位在中心点
                        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng)); //设置当前点为中心位置
                    }
                    //将此经纬度设置成公共的，也就是说，我们在有网络的情况下响应实际的坐标，无网络响应发送的经纬度，且发送的经纬度优先级高于一切。
                    addCarMarker(latLngPoint.latitude, latLngPoint.longitude, Float.valueOf(heading));

                    //将经纬度存入缓存中
                    EnjoyTrainShipApplication.editor.putString("lon", lon + "");
                    EnjoyTrainShipApplication.editor.putString("lat", lat + "");
                    EnjoyTrainShipApplication.editor.commit();

                }
                break;
            case Common.ACTION_UI_ROADS_SHOW:  //更新轨迹
                drawRoadInMap( );
                break;
            case Common.ACTION_UI_V2X:  //v2x
                //0:无 1：红灯 2：绿灯 3：黄灯

                attentionDialogShow();
                break;
        }
    }


    /**
     * 弹出框警告
     */
    private void attentionDialogShow() {
        int type = 0, key = 0;
        if (Global.connectTip == 2) {   //连接失败信息
            type = AttentionTypeEnum.DISCONNECT.key;
            key = AttentionContentEnum.DISCONNECT.key;
            Global.connectTip = 0;
        } else if (Global.connectTip == 1) {
            type = AttentionTypeEnum.CONNECT_SUCCESS.key;  //连接成功
            key = AttentionContentEnum.CONNECT_SUCCESS.key;
            Global.connectTip = 0;
        } else if (DataStorageFromPC.driverStatusTip == 2) {
            type = AttentionTypeEnum.MANUAL_DRIVE.key;//退出自驾
            key = AttentionContentEnum.SWITCH_MANUAL_DRIVE.key;
            DataStorageFromPC.driverStatusTip = 0;
        } else if (DataStorageFromPC.driverStatusTip == 1) {
            type = AttentionTypeEnum.AUTO_DRIVE.key; //进入自动驾驶
            key = AttentionContentEnum.SWITCH_AUTO_DRIVE.key;
            DataStorageFromPC.driverStatusTip = 0;
        } else if (DataStorageFromPC.accBrake > 0) {
            type = AttentionTypeEnum.BRAKE.key;  //刹车请注意
            key = AttentionContentEnum.BRAKE_ATTENTION.key;
            DataStorageFromPC.accBrake = 0;
        } else if (DataStorageFromPC.driverStatus > 0 && ErrorContentEnum.contains(DataStorageFromPC.error)) {  //自驾状态，有故障
            type = AttentionTypeEnum.ERROR.key;
            key = DataStorageFromPC.error;
        } else if (DataStorageFromPC.driverStatus > 0 && DataStorageFromPC.v2xType > 0) {  //自驾状态， v2x信息
            type = AttentionTypeEnum.V2X.key;
            key = DataStorageFromPC.v2xType;
        }
        if ((type == AttentionTypeEnum.ERROR.key || type == AttentionTypeEnum.V2X.key)   //第故障或v2x的信息，且两次信息没超过2500ms,直接返回
                && System.currentTimeMillis() - AttentionInfo.timestamp < 2500) {
            return;
        }

        if (EnjoyTrainShipApplication.mediaLock.tryLock()) {
            attentionDialogShowImp(type, key);
        }
        EnjoyTrainShipApplication.mediaLock.unlock();
    }

    String attentionTag = "attentionTag";

    private void attentionDialogShowImp(int type, int key) {
        Log.i(attentionTag, "type =" + type + ". key = " + key);
        if (type == 0 && (System.currentTimeMillis() - AttentionInfo.timestamp) > 2500) {  //没有任何弹框消息，且距离上次弹框时间超过2.5秒
            if (dialogPopWindow != null && dialogPopWindow.isShowing()) {
                dialogPopWindow.dismiss();
            }
            return;
        }
        if (type == 0) {
            return;
        }
        AttentionInfo.timestamp = System.currentTimeMillis();
        //如果此次弹框信息和上次的一样，且弹框还在，就直接报语音
        if (type == AttentionInfo.type && key == AttentionInfo.attentionKey
                && dialogPopWindow != null && dialogPopWindow.isShowing()) {
            Log.i(attentionTag, "语音唤醒线程开始唤醒1");
            notifyMediaThread();
            return;
        }


        AttentionInfo.type = type;
        AttentionInfo.attentionKey = key;
        if (type == AttentionTypeEnum.CONNECT_SUCCESS.key || type == AttentionTypeEnum.DISCONNECT.key
                || type == AttentionTypeEnum.MANUAL_DRIVE.key || type == AttentionTypeEnum.AUTO_DRIVE.key) {
            AttentionInfo.title = "提示";
        } else {
            AttentionInfo.title = "警告";
        }
        String attentionMsg = AttentionContentEnum.getValue(key);
        if (attentionMsg == null || "".equals(attentionMsg)) {
            attentionMsg = V2xTypeEnum.getValue(key);
        }
        if (type == AttentionTypeEnum.ERROR.key) {
            attentionMsg = attentionMsg + "\n" + "故障码" + DataStorageFromPC.error;
        }
        AttentionInfo.message = attentionMsg;


        if (dialogPopWindow == null) {
            dialogPopWindow = new MyDialogPopWindow(this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogPopWindow.dismiss();
                }
            });
        }
        if (msgTxt == null) {
            msgTxt = dialogPopWindow.getContentView().findViewById(R.id.alarm_msg_txt);
        }
        if (titleTxt == null) {
            titleTxt = dialogPopWindow.getContentView().findViewById(R.id.title_txt);
        }

        if ("警告".equals(AttentionInfo.title)){
            msgTxt.setTextColor(getResources().getColor(R.color.red));
        }else {
            msgTxt.setTextColor(getResources().getColor(R.color.lightBlack));
        }

        titleTxt.setText(AttentionInfo.title);
        msgTxt.setText(AttentionInfo.message);
        dialogPopWindow.showAtLocation(findViewById(R.id.activity_main), Gravity.CENTER, 0, 0);
        Log.i(attentionTag, "语音唤醒线程开始唤醒2");
        notifyMediaThread();
    }

    /**
     * 唤醒媒体播放线程
     */
    private void notifyMediaThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //唤醒媒体播放线程
                synchronized (EnjoyTrainShipApplication.mediaObj) {
                    Log.i(attentionTag, "语音唤醒线程开始唤醒3");
                    EnjoyTrainShipApplication.mediaObj.notify();
                }
            }
        });
        EnjoySocketService.threadMediaPoolService.execute(thread);
    }

    boolean center_flag = true;//车辆图片是否居中

    /**
     * 设置amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        //设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationRotateAngle(180);//旋转：可触发进入应用后，直接进入定位点
        aMap.setLocationSource(this);
        aMap.setOnMapTouchListener(this); //设置触摸控件
        aMap.showBuildings(false);       //不显示3D楼
        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);// 设置地磁按钮是否显示
        //uiSettings.setMyLocationButtonEnabled(true);//显示定位按钮
        uiSettings.setScaleControlsEnabled(true);//显示比例尺控件
        uiSettings.setZoomControlsEnabled(false);//隐藏缩放按钮
    }

    private void addMarker(LatLng end, String s) {
        MarkerOptions options1 = new MarkerOptions();
        options1.title(s)
                .position(end)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.amap_end)));
        Marker marker1 = aMap.addMarker(options1);
        marker1.setObject(s);
    }

    /**
     * 给路线添加起点终点标志
     *
     * @param start
     * @param end
     */
    private void addStartEndMarker(LatLng start, LatLng end) {
        MarkerOptions options = new MarkerOptions();
        options.title("start").position(start)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.amap_start)));
        Marker marker = aMap.addMarker(options);
        marker.setObject("start");

        MarkerOptions options1 = new MarkerOptions();
        options1.title("end").position(end)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.amap_end)));
        Marker marker1 = aMap.addMarker(options1);
        marker1.setObject("end");
    }

    /**
     * 方法必须重写
     * activity重启方法
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "onResume start");
        DataStorage.page = 1;
        DataStorage.mode = 1;

        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        if (surfaceView != null) {
            surfaceView.onResume();
        }

        //按钮恢复为未点击
//        controlImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.control_img_no_check));

//        collectRoads.setImageDrawable(getResources().getDrawable(R.drawable.collect_map_no_check));
//        reloadRoadTabLayout.performClick(); //重载轨迹
        if (DataStorageCollectMap.collectMode == 2) {  //采集轨迹状态已经结束了,到主页面变为0
            DataStorageCollectMap.collectMode = 0;

        }

        //初始化右侧控制按钮
        refresh("onResume"); //页面onResume重载
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (surfaceView != null) {
            surfaceView.onPause();
        }

        deactivate();
        Log.i(TAG, "主页执行onPause*************");

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //返回键被按下，则退出程序
        Log.i(TAG, "返回键onBackPressed");
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "返回键被按下");
            return super.onKeyDown(keyCode, event); //如果不返回父类的这个方法就不会走到onBackPressed方法里
        }
        return false;

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        //停止定位
        mListener = null;
//        if (mAMapLocationManager != null) {
//            mAMapLocationManager.removeUpdates(this);
//            mAMapLocationManager.destory();
//        }
//        mAMapLocationManager = null;


        System.exit(0); //退出应用
    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        if (mListener != null && aLocation != null) {
            // mListener.onLocationChanged(aLocation);// 显示系统小蓝点,不显示蓝点
            Log.e("tag", "定位成功");
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        /**
         *

         if (mAMapLocationManager == null) {
         mAMapLocationManager = LocationManagerProxy.getInstance(this);
         Log.e("tag", "激活定位");
         /*
         * mAMapLocManager.setGpsEnable(false);
         * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
         * API定位采用GPS和网络混合定位方式
         * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
         */
        /**
         *
         mAMapLocationManager.requestLocationUpdates(
         LocationProviderProxy.AMapNetwork, 2000, 10, this);
         //                // API定位采用GPS定位方式，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
         //                mAMapLocationManager.requestLocationUpdates(
         //                        LocationManagerProxy.GPS_PROVIDER, 2000, 10, this);
         }
         */
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        Log.e("tag", "停止定位");
        mListener = null;
        /**
         *

         if (mAMapLocationManager != null) {
         mAMapLocationManager.removeUpdates(this);
         mAMapLocationManager.destory();
         }
         mAMapLocationManager = null;
         */
    }

    //清楚所有的marker
    private void clearMarkers() {
        aMap.clear();
    }

    /**
     * 发送请求加载路径
     */
    private void refresh(String tag) {
        Log.i(TAG, "开始重置轨迹 " + tag);
        if (Global.loadRoadsFlag) {
            return;
        }
        //清除已有路径信息
        DataStorageFromPC.roadsMap.clear();      //原始轨迹名称与经纬度Map
        DataStorageFromPC.zoneNameJsonListMap.clear();
        clearMarkers();//移除覆盖物
        /**
         if (polyline != null) {  //删除显示的轨迹
         polyline.remove();
         }
         **/
        //删除显示的轨迹
        for (Polyline line : polylineList) {
            line.remove();
        }
        Global.loadRoadsFlag = true;
//        resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_check));
        Log.i(TAG, "重置完成");

    }

    /**
     * 添加车辆图标
     *
     * @param lat     纬度
     * @param lot     经度
     * @param heading 航向角
     */
    private void addCarMarker(double lat, double lot, float heading) {
        Log.i(TAG, "addCarMarker() called with: lat = [" + lat + "], lot = [" + lot + "], heading = [" + heading + "]");
        if (carMarker != null) {
//            carMarker.remove();
//            carMarker.setPosition(new LatLng(lat, lot));
//            carMarker.setRotateAngle(360 - heading);
//            return;

            carMarker.setPosition(new LatLng(lat, lot));
            carMarker.setRotateAngle(360 - heading);

        } else {
            //绘制marker
            carMarker = aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lot))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.car)))
                    .draggable(true));
            carMarker.setRotateAngle(360 - heading);
        }
        carMarker.setZIndex(100f);
    }


    /**
     * 网络连接情况
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        dismissDialog();
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        AMapUtil.convertToLatLng(latLonPoint), 15));
//                regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
                ToastUtil.showShort(MainActivity1.this, addressName);
            } else {
                ToastUtil.showShort(MainActivity1.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.showShort(MainActivity1.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.showShort(MainActivity1.this, R.string.error_key);
        } else {
            ToastUtil.showShort(MainActivity1.this,
                    getString(R.string.error_other) + rCode);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        dismissDialog();
        if (rCode == 0) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
                geoMarker.setPosition(AMapUtil.convertToLatLng(address
                        .getLatLonPoint()));
                addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
                        + address.getFormatAddress();
                ToastUtil.showShort(MainActivity1.this, addressName);
            } else {
                ToastUtil.showShort(MainActivity1.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.showShort(MainActivity1.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.showShort(MainActivity1.this, R.string.error_key);
        } else {
            ToastUtil.showShort(MainActivity1.this,
                    getString(R.string.error_other) + rCode);
        }
    }


    /**
     * 隐藏进度条对话框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    //    @Override
    public void onLocationChanged(Location location) {

    }

    //    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    //    @Override
    public void onProviderEnabled(String s) {

    }

    //    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        center_flag = false;

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
}
