package com.enjoy.sweeper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.enjoy.sweeper.entity.DataStorage;
import com.enjoy.sweeper.util.AMapUtil;
import com.enjoy.sweeper.util.ToastUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity extends Activity implements LocationSource, AMapLocationListener,
        GeocodeSearch.OnGeocodeSearchListener, OnMapTouchListener {//定位接口
    private static final String TAG = "PageActivityMain";

    private Context mContext;
    private AMap aMap;//地图控制器类
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;

    private Button endRaBtn, goRaBtn; //defaultRaBtn
    private TextView speedText, batteryText, voltageTxt, connectTxt, workModeTxt;
    //  连接  采集地图 遥控器  删除轨迹 重载
    private ImageButton connectBtn, collectRoads, controlImgBtn, deleteImgBtn, resetImgBtn, broomImgBtn, garbageImgBtn,
            voltageImgBtn, batteryImgBtn, speedPanelImg;
    // 一键报警 警灯 警笛 前灯 尾灯
    private ImageButton alarmFromPoliceImgBtn, policeLightImgBtn, policeWhistleImgBtn, tailLightImgBtn;
    private ImageView obs_img, gps_img, auto_drive_img;

    private Spinner areaSpinner, roadSpinner;
    private ArrayAdapter<String> roadsAdapter;
    private ArrayAdapter<String> areasAdapter;
    private List<String> roadAreaList = new ArrayList<>();
    private List<String> roadNameChList = new ArrayList<>();

    private Button clean_enable_default;
    private Button goGarageBtn, goCleanBtn, goGarbagestationBtn, goPauseBtn, presentationBtn;

    //遥控器，采集地图，重载， 删除地图 组合控件
    private LinearLayout controlTabLayout, collectTabLayout, reloadRoadTabLayout, deleteRoadTabLayout,
            warningLayout, ledlightLayout, hornLayout, frontlightLayout, backlightLayout;
    //    private Stoa stoa;
    private UiSettings uiSettings;

    private RelativeLayout relativeLayout;

    private Timer sendGoOrStopTimer = null;

    /********************************************************************************/
    private ScanUITask scanUITask;
    private ScanRoadsLoadTask scanRoadsLoadTask;
    private static ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();

    /*********************************************************************************/

    private ProgressDialog progDialog = null;
    private GeocodeSearch geocoderSearch;
    private String addressName;

//    private LatLng startPoint = new LatLng(39.98823831, 116.42157257); //本部联大地库门口

    private Marker geoMarker;
    private Marker regeoMarker;

    private Marker carMarker;

    private String speed;
    private String gps;
    private String heading;
    private String[] split;
    private LatLng latLng = new LatLng(30.617127, 114.253258);
    private BitmapDescriptor normalRouteBlue = null;
    private BitmapDescriptor normalRouteGreen = null;
    private BitmapDescriptor normalRouteYellow = null;
    private BitmapDescriptor normalRouteGrey = null;
    private static Handler handler;


    //存储地图Poyline的list
    private static Map<String, Polyline> aMapLineListMap = new HashMap<>();
    static volatile int mapNum = 1;


    //一键报警提示框
//    private static AlertDialog.Builder mAlertDialog;
//    private static AlertDialog dialog;

    private static boolean dialogCreateFlag = false;

    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    private static Lock stopGoLock = new ReentrantLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置本activity长亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //设置屏幕格式为横屏
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mContext = getApplicationContext();
        mapView = (MapView) findViewById(R.id.map); //获取地图控件引用
        mapView.onCreate(savedInstanceState);// 此方法必须重写  创建地图


        sharedPreferences = getSharedPreferences("latlon", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initView();

    }


    /**
     * 绘制轨迹
     *
     * @param roadName
     */
    private void drawRoadInMap(String roadName) {
        JSONObject jsonObject = DataStorage.roadsMap.get(roadName);
        List<LatLng> temp = new ArrayList();
        JSONArray pointsArray = (JSONArray) jsonObject.get("points");
        if (pointsArray == null || pointsArray.isEmpty()) {
            return;
        }

        int size = pointsArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject pointJson = (JSONObject) pointsArray.get(i);
            double lat = (Double) pointJson.get("lat");
            double lon = (Double) pointJson.get("lon");
            LatLng latLngPoint = ChangeLatlon.transform(lat, lon);
            temp.add(latLngPoint);
        }

        addStartEndMarker(temp.get(0), temp.get(temp.size() - 1));

        //获取第一个清扫轨迹的名字
        //获取初步选中的轨迹
        if ((DataStorage.getSelectMapName() == null || DataStorage.getSelectMapName().equals("")) &&
                (DataStorage.initSelectMapName != null && !DataStorage.initSelectMapName.equals(""))) {
//            DataStorage.selectMapName = DataStorage.initSelectMapName;
            DataStorage.setSelectAreaNo(DataStorage.initSelectMapName);
        }
        PolylineOptions po = new PolylineOptions().addAll(temp).setUseTexture(true).setCustomTexture(normalRouteBlue)
                .width(20).color(Color.argb(255, 0, 255, 1));
        //如果是默认的选中的道路，设置颜色
        if (roadName.equals(DataStorage.getSelectMapName())) {
            po.setCustomTexture(normalRouteYellow);
        }

        Polyline pLine = aMap.addPolyline(po);
        aMapLineListMap.put(roadName, pLine);
        DataStorage.roadsNameList.add(roadName);
        Log.i(TAG, "画路线完成" + roadName);
    }


    /**
     * UI扫描任务
     */
    private class ScanUITask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... paramStr) {
            while (true) {
                if (isCancelled()) {
                    Log.i(TAG, "扫描任务中断");
                    break;
                }
                handler.sendEmptyMessage(666);
                SystemClock.sleep(200);
                Log.i(TAG, "扫描任务发送");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progresses) {
            //text_info2.setText("sent\n"+progresses[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("AsyncTask", "end scanUI");
        }
    }

    /**
     * 地图加载扫描任务
     */
    private class ScanRoadsLoadTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... paramStr) {
            while (true) {
                if (isCancelled()) {
                    Log.i(TAG, "轨迹扫描任务中断");
                    break;
                }
                if (DataStorage.nameBlockQueue.size() > 0) {
                    handler.sendEmptyMessage(777);
                    Log.i(TAG, "轨迹扫描任务发送");
                }
                SystemClock.sleep(100);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progresses) {
            //text_info2.setText("sent\n"+progresses[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("AsyncTask", "end scanUI");
        }
    }


    private TimerTask sendGoOrStopTask = null;


    /**
     * 发送出发或停止的定时任务
     */
    private TimerTask getSendGoOrStopTask() {
        if (sendGoOrStopTask != null) {
            sendGoOrStopTask.cancel();
        }
        sendGoOrStopTask = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent();
                i.setAction("serviceCastAction");
                i.putExtra(Global.msg, Global.stopOrGoMsg);
                getApplicationContext().sendBroadcast(i);
                Log.i(TAG, "发送命令" + Global.stopOrGoMsg);

            }
        };
        return sendGoOrStopTask;
    }

    /**
     * 初始化界面
     */
    private void initView() {

        DataStorage.mode = 1;    //订阅模式 1--显示订阅信息模式 2--采集地图模式

        RelativeLayout leftLayout = findViewById(R.id.left_menu_panel);
        leftLayout.bringToFront(); //让左侧菜单栏置顶

//        controlTabLayout = findViewById(R.id.control_tab); //遥控器跳转组件
        collectTabLayout = findViewById(R.id.collect_tab); //采集地图跳转组件
        reloadRoadTabLayout = findViewById(R.id.reload_tab); //重载轨迹组件
        deleteRoadTabLayout = findViewById(R.id.delete_road_tab); //删除轨迹组件

        frontlightLayout = findViewById(R.id.frontlight_layout); //前灯组件---电压

        voltageImgBtn = findViewById(R.id.front_light_img_btn);         //前灯----电压
        batteryImgBtn = findViewById(R.id.battery_img_btn);  //电量
        speedPanelImg = findViewById(R.id.speed_panel_img);


        broomImgBtn = findViewById(R.id.broom_img_btn);
        garbageImgBtn = findViewById(R.id.garbage_img_btn);
        clean_enable_default = findViewById(R.id.clean_enable_btn);
        //清扫使能
        clean_enable_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.cleanEnable) { //清扫使能true
                    DataStorage.cleanEnable = false;  //设为无效
                    clean_enable_default.setBackground(getResources().getDrawable(R.drawable.clean_enable_false));
                } else {
                    DataStorage.cleanEnable = true; //使能设为有效
                    clean_enable_default.setBackground(getResources().getDrawable(R.drawable.clean_enable_true));
                }
            }
        });


//        清扫点击
        broomImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (DataStorage.sweepStatus > 0) {   //车上目前是清扫
//                    //0：默认    1：使能    2：不使能
//                    DataStorage.cleanEnableClick = 2; //通知车转为不清扫
//                } else {
//                    DataStorage.cleanEnableClick = 1; //通知车转为清扫
//                }
                if (DataStorage.broomCleanClick == 2) { //2变1
                    DataStorage.broomCleanClick = 1;
                    broomImgBtn.setBackgroundColor(Color.parseColor("#fcf9f2"));
                } else {                           //1变2
                    DataStorage.broomCleanClick = 2;
                    broomImgBtn.setBackgroundColor(Color.GRAY);
                }

            }
        });
        //清倒点击
        garbageImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.dumpRubbishClick < 1) {
                    DataStorage.dumpRubbishClick = 1; //通知车转为清倒垃圾
                    ToastUtil.show(MainActivity.this, "倾倒打开");
                } else {
                    DataStorage.dumpRubbishClick = 0;
                    ToastUtil.show(MainActivity.this, "倾倒关闭");
                }

            }
        });

        if (aMap == null) {
            aMap = mapView.getMap();
//            设置路线点击事件
            aMap.setOnPolylineClickListener(new AMap.OnPolylineClickListener() {
                @Override
                public void onPolylineClick(Polyline line) {
                    Log.i(TAG, "路线点击开始" + aMapLineListMap.size());

                    //先将其他所有line风格恢复原样
                    for (Map.Entry<String, Polyline> entry : aMapLineListMap.entrySet()) {
                        Polyline p = entry.getValue();
                        PolylineOptions po = p.getOptions();
                        String roadName = entry.getKey();
                        if (p.equals(line)) {
                            po.setCustomTexture(normalRouteYellow);
                            line = p;
//                            DataStorage.selectMapName = roadName;
                        } else {
                            po.setCustomTexture(normalRouteBlue);
                        }
                        p = aMap.addPolyline(po);
                    }
                }
            });
            geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //设置地图属性
            setUpMap();
            //获取当前设备位置，赋值为地图中心（如果没有车位置的话）
//            setCenterForMap();

            aMap.animateCamera(CameraUpdateFactory.zoomTo(16)); //放大等级
            this.normalRouteBlue = BitmapDescriptorFactory.fromAsset("blue.png");
            this.normalRouteGreen = BitmapDescriptorFactory.fromAsset("green.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("yellow.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("grey.png");


            //读取缓存经纬度数据  116.416797,40.037969
            String lonStr = sharedPreferences.getString("lon", "116.416797");
            String latStr = sharedPreferences.getString("lat", "40.037969");
            Log.i(TAG, "获取缓存经纬度" + lonStr + "," + latStr);
            LatLng latLng1 = ChangeLatlon.transform(Double.parseDouble(latStr), Double.parseDouble(lonStr));
            aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng1)); //中心点

            aMap.setMyLocationRotateAngle(90);

        }
        //采集地图按钮
        collectRoads = (ImageButton) findViewById(R.id.collect_map_img_btn);
        collectRoads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectRoads.setImageDrawable(getResources().getDrawable(R.drawable.collect_map_check));
                Log.i(TAG, "跳转地图采集界面");
                //判断轨迹数量是否大于10
                /**
                 if (DataStorage.roadsMap.size() >= 10) {  //路径数量>=10提示用户删除路径
                 ToastUtil.show(getApplicationContext(), "路径数量已最大，请删除部分路径");
                 return;
                 }
                 **/
                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity.class);
                startActivity(mapIntent);
            }
        });
        collectTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectRoads.setImageDrawable(getResources().getDrawable(R.drawable.collect_map_check));
                Log.i(TAG, "跳转地图采集界面");
                //判断轨迹数量是否大于10
                /**
                 if (DataStorage.roadsMap.size() >= 10) {  //路径数量>=10提示用户删除路径
                 ToastUtil.show(getApplicationContext(), "路径数量已最大，请删除部分路径");
                 return;
                 }
                 **/
                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity.class);
                startActivity(mapIntent);
            }
        });
        //连接按钮
        connectBtn = (ImageButton) findViewById(R.id.connect_img_btn);

        //重载按钮
        resetImgBtn = (ImageButton) findViewById(R.id.reload_img_btn);
        reloadRoadTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshRoads();  //刷新按钮
            }
        });
        resetImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshRoads();  //刷新按钮
            }
        });

        //删除轨迹按钮
        deleteImgBtn = (ImageButton) findViewById(R.id.delete_road_img_btn);
        deleteImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRoadsSets();
            }
        });
        deleteRoadTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRoadsSets();
            }
        });

        goCleanBtn = findViewById(R.id.go_clean_btn); //清扫作业按钮
        goGarageBtn = findViewById(R.id.go_garage_btn);//回库按钮
        goGarbagestationBtn = findViewById(R.id.go_garbagestation_btn); //回站按钮
        goPauseBtn = findViewById(R.id.go_pause_btn); //暂停按钮
        presentationBtn = findViewById(R.id.presentation_btn); //演示按钮


        goCleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.setGoCleanClick()) {
                    goGarageBtn.setActivated(false);
                    goGarbagestationBtn.setActivated(false);
                    goCleanBtn.setActivated(true);
                } else {
                    goCleanBtn.setActivated(false);
                }

            }
        });

        goGarageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.setGoGarageClick()) {
                    goCleanBtn.setActivated(false);
                    goGarbagestationBtn.setActivated(false);
                    goGarageBtn.setActivated(true);
                } else {
                    goGarageBtn.setActivated(false);
                }
            }
        });
        goGarbagestationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.setGoGarbageStationClick()) {
                    goCleanBtn.setActivated(false);
                    goGarageBtn.setActivated(false);
                    goGarbagestationBtn.setActivated(true);
                } else {
                    goGarbagestationBtn.setActivated(false);
                }
            }
        });

        goPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.setGoPauseClick()) {
                    goPauseBtn.setActivated(true);
                    goPauseBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    goPauseBtn.setActivated(false);
                    goPauseBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });

        presentationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.presentationClick()) {
                    presentationBtn.setActivated(true);
                } else {
                    presentationBtn.setActivated(false);
                }
            }
        });


        //速度与电池文字显示
        speedText = (TextView) findViewById(R.id.speed_txt);
        batteryText = (TextView) findViewById(R.id.battery_txt);
        voltageTxt = findViewById(R.id.voltage_txt);
        connectTxt = findViewById(R.id.connect_txt);
        workModeTxt = findViewById(R.id.work_mode_txt);
        //障碍物图片
        obs_img = (ImageView) findViewById(R.id.obstacle_img);
        //gps图片显示
        gps_img = (ImageView) findViewById(R.id.gps_img);
        //自动驾驶状态图片
        auto_drive_img = (ImageView) findViewById(R.id.auto_drive_img);

        //结束按钮  //出发按钮 radioButton
        endRaBtn = findViewById(R.id.end_radio_btn);
        goRaBtn = findViewById(R.id.go_radio_btn);
//        goRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    DataStorage.stopGoTimeStamp = System.currentTimeMillis();
//                    DataStorage.stopgo = 1;
//                    goRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
//                    Log.i(TAG, "Go按钮点击***************");
//                } else {
//                    goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
//                }
//
//            }
//        });
        goRaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - DataStorage.stopGoTimeStamp < 2000) {
                    return;
                }

                stopGoLock.lock();
                DataStorage.stopGoTimeStamp = System.currentTimeMillis();
                DataStorage.stopgo = 1;
                goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
                goRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
                endRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                stopGoLock.unlock();
            }
        });
        endRaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - DataStorage.stopGoTimeStamp < 2000) {
                    return;
                }
                stopGoLock.lock();
                DataStorage.stopGoTimeStamp = System.currentTimeMillis();
                DataStorage.stopgo = 2;

                endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
                endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
                goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

                stopGoLock.unlock();
            }
        });

//        endRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//
//                if (b) {
//                    DataStorage.stopGoTimeStamp = System.currentTimeMillis();
//                    DataStorage.stopgo = 2;
//                    endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
//                } else {
//                    endRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
//                }
//
//            }
//        });


        //离线地图
//        relativeLayout = (RelativeLayout) findViewById(R.id.down);
//        relativeLayout.setOnClickListener(this);

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        progDialog = new ProgressDialog(this);

        //路线选择下拉框部分
        areaSpinner = findViewById(R.id.area_spinner);
        roadSpinner = findViewById(R.id.road_spinner);
        roadAreaList = DataStorage.getMapAreaList();
        roadNameChList = DataStorage.getMapName_CH_list();
        areasAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, roadAreaList);
        roadsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, roadNameChList);
        //下拉风格
        roadsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //被选中的选项
                String nameCh = DataStorage.getMapName_CH_list().get(position);
                DataStorage.setSelectMapNo(nameCh);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DataStorage.appMsgSendLock.lock(); //切换中去持有锁，appMsg不可发送消息

                //被选中的选项
                String areaNameCh = roadAreaList.get(position);
//                DataStorage.setSelectMapNo(nameCh);
                DataStorage.setSelectAreaNo(areaNameCh);

                //区域被点击，路径下拉框一起改变
                roadNameChList = DataStorage.getMapName_CH_list();
                roadsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, roadNameChList);
                roadsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roadSpinner.setAdapter(roadsAdapter);

                //释放
                DataStorage.appMsgSendLock.unlock();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                myHandleMessage(msg.what);
            }
        };
    }

    /**
     * 删除轨迹调用
     */
    private void deleteRoadsSets() {
        if (DataStorage.getSelectMapName() == null || "".equals(DataStorage.getSelectMapName())) {
            ToastUtil.show(getApplicationContext(), "先选择路线");

        }
        AlertDialog.Builder deleteRoadsBuild = new AlertDialog.Builder(this);
        deleteRoadsBuild.setTitle("提示");
        deleteRoadsBuild.setMessage("删除轨迹" + DataStorage.getSelectMapName() + "?");
        deleteRoadsBuild.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /****
                 将当前选中路线名称赋值与另外变量（要删除轨迹的名称），这么做是防止一个问题发生即：
                 设置了deleteRoadFlag = true，然后调用重载方法resetRoads，但应用发送ros周期是100ms，
                 可能还没等ros发送deleteRoadFlag，resetRoads就已经执行完毕了，
                 resetRoads方法会先将selectMapName置为空字符串""，过了100ms，应用发送deleteRoad，地图名字已经是空的了
                 ****/

                //标记删除
//                DataStorage.deleteMapName = DataStorage.selectMapName;
                Global.deleteRoadFlag = true;
//                DataStorage.selectMapName = ""; //删除轨迹，则当前的选中轨迹名称被初始化
                //删除轨迹后重载
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(800); //800毫秒后再重载，给系统一定时间，等车载端删除轨迹
                        handler.sendEmptyMessage(400);
                    }
                }).start();
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
     * 处理message
     *
     * @param msgWhat
     */
    private void myHandleMessage(int msgWhat) {
        switch (msgWhat) {
            case 400:
                refreshRoads();
                break;
            case 666: //更新UI
                //连接状态
                if (Global.connectFlag) {
                    connectBtn.setImageDrawable(getResources().getDrawable(R.drawable.have_connect));
                    connectTxt.setText(getResources().getText(R.string.connect_success));
                } else {
                    connectBtn.setImageDrawable(getResources().getDrawable(R.drawable.not_connect));
                    connectTxt.setText(getResources().getText(R.string.connect_off_line));
                }

                if (stopGoLock.tryLock()) { //更新stopgo的按钮显示，先获取锁
                    if (!Global.connectFlag) {   //未连接
                        if (System.currentTimeMillis() - 2000 > DataStorage.stopGoTimeStamp) { //距离上次点击时间超过2秒
                            //显示为结束
                            endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
                            endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                            goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
                            goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                            DataStorage.stopgo = 0;
                            goRaBtn.setEnabled(true);
                            endRaBtn.setEnabled(true);

                        } else {  //距离点击按钮时间小于2秒
                            goRaBtn.setEnabled(false);
                            endRaBtn.setEnabled(false);
                        }
                    } else {   //已经连接了
//                        if (System.currentTimeMillis() - DataStorage.actuatorTimeStamp > 4000) { //已经超过4秒没收到车状态数据了，认为车不在线，显示为结束
//                            if (System.currentTimeMillis() - 2000 > DataStorage.stopGoTimeStamp) {
//                                endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
//                                endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
//
//                                goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
//                                goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
//                                DataStorage.stopgo = 0;
//                                goRaBtn.setEnabled(true);
//                                endRaBtn.setEnabled(true);
//                            } else {
//                                goRaBtn.setEnabled(false);
//                                endRaBtn.setEnabled(false);
//                            }
//                        } else
                        if (System.currentTimeMillis() - 2000 > DataStorage.stopGoTimeStamp) { //actuator数据及时，更新出发结束按钮的反馈
                            goRaBtn.setEnabled(true);
                            endRaBtn.setEnabled(true);
                            //大于2000ms时间为了让stopgo能发送2s时间再和反馈做判断
                            if (DataStorage.driverStatus > 0) {
                                //DataStorage.stopgo  1--出发  2--结束     车反馈为自驾状态，则发送的出发命令变为默认
                                goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
                                goRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                                endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
                                endRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                                DataStorage.stopgo = 0;
                            } else if (DataStorage.driverStatus == 0) {
                                endRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_checked));
                                endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                                goRaBtn.setBackground(getResources().getDrawable(R.drawable.stop_go_btn_default));
                                goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                                DataStorage.stopgo = 0;
                            }
                        } else if (System.currentTimeMillis() - 2000 < DataStorage.stopGoTimeStamp) {
                            goRaBtn.setEnabled(false);
                            endRaBtn.setEnabled(false);
                        }
                    }

                    stopGoLock.unlock();
                }


                //障碍物
                if (!Global.connectFlag) {
                    obs_img.setImageResource(R.drawable.obstacle_no);
                } else {
                    if (DataStorage.obs == 0) {  //无障碍物
                        obs_img.setImageResource(R.drawable.obstacle_no);
                    } else {             //有障碍物
                        obs_img.setImageResource(R.drawable.obstacle);
                    }
                }

                //文字显示工作模式
                //0--未知等待中 1,清扫       2,去车库       3,去垃圾站
                if (0 == DataStorage.getCarWorkMode()) {
                    workModeTxt.setText("等待中..");
                } else if (1 == DataStorage.getCarWorkMode()) {
                    workModeTxt.setText("清扫中..");
                    if (goCleanBtn.isActivated()) { //如果清扫按钮是被点击选中的，就触发点击，取消显示
                        goCleanBtn.performClick();
                    }
                } else if (2 == DataStorage.getCarWorkMode()) {
                    workModeTxt.setText("回车库..");
                    if (goGarageBtn.isActivated()) {
                        goGarageBtn.performClick();
                    }
                } else {
                    workModeTxt.setText("去垃圾站");
                    if (goGarbagestationBtn.isActivated()) {
                        goGarbagestationBtn.performClick();
                    }
                }

                //  GPS显示
                if (!Global.connectFlag) {
                    gps_img.setImageResource(R.drawable.gps0);
                } else {
                    if (DataStorage.rtk >= 4) {
                        gps_img.setImageResource(R.drawable.gps3);
                    } else if (1 < DataStorage.rtk && DataStorage.rtk < 4) {
                        gps_img.setImageResource(R.drawable.gps2);
                    } else {
                        gps_img.setImageResource(R.drawable.gps1);
                    }
                }


                //驾驶状态
                if (!Global.connectFlag) {
                    auto_drive_img.setImageResource(R.drawable.no_auto_drive);
                } else {
                    if (DataStorage.driverStatus == 0) { //非自动驾驶状态
                        auto_drive_img.setImageResource(R.drawable.no_auto_drive);
                    } else {                         //自动驾驶状态
                        auto_drive_img.setImageResource(R.drawable.auto_drive_img);
                    }
                }


//                清扫扫帚旋转   清倒旋转
                long timeFlag = System.currentTimeMillis() % 1000;
                if (!Global.connectFlag) {
                    broomImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.broom0));
                    garbageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.garbage_box0));
                } else {
                    if (DataStorage.sweepStatus > 0) { //清扫
                        if (timeFlag > 500) {
                            broomImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.broom01));
                        } else {
                            broomImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.broom));
                        }
                    } else {
                        broomImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.broom));
                    }

                    if (DataStorage.liftStatus == 1 || DataStorage.liftStatus == 2) { //清倒
                        //0--默认  1--准备  2---执行中  3---结束
                        if (timeFlag > 500) {
                            garbageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.garbage_box01));
                        } else {
                            garbageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.garbage_box));
                        }
                    } else if (DataStorage.liftStatus == 3) { //结束
                        DataStorage.dumpRubbishClick = 0; //倾倒发送变为0
                        garbageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.garbage_box0));
                    }
                }


                //设置电量
                if (!Global.connectFlag) { //未连接
                    batteryImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.battery_img0));
                } else {    //连接
                    batteryImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.battery_img));
                }
                batteryText.setText(DataStorage.batterySoc + "%");

                //设置电压
                if (!Global.connectFlag) {
                    voltageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.voltage0));
                } else {
                    //低压报警闪烁
                    if (DataStorage.lowBattery > 0 && timeFlag > 500) {  //有低压报警
                        voltageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.voltage_error1));
                    } else if (DataStorage.lowBattery > 0 && timeFlag <= 500) {
                        voltageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.voltage_error0));
                    } else {
                        voltageImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.voltage));
                    }
                }
                voltageTxt.setText(DataStorage.battery);     //电压

                //GPS速度等数据
                if (!Global.connectFlag) {
                    speedPanelImg.setImageDrawable(getResources().getDrawable(R.drawable.speed_panel0));
                } else {
                    speedPanelImg.setImageDrawable(getResources().getDrawable(R.drawable.speed_panel));
                }
                double lon = DataStorage.lon;
                double lat = DataStorage.lat;
                heading = DataStorage.heading;
                speed = DataStorage.speed;
                speedText.setText(speed + "km/h");
                if (lon > 0 && lat > 0) {
                    LatLng latLngPoint = ChangeLatlon.transform(lat, lon);
                    latLng = new LatLng(latLngPoint.latitude, latLngPoint.longitude);
                    if (DataStorage.driverStatus > 0) { //如果是自驾状态，则设为中心点
                        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng)); //设置当前点为中心位置
                    }

                    //将此经纬度设置成公共的，也就是说，我们在有网络的情况下响应实际的坐标，无网络响应发送的经纬度，且发送的经纬度优先级高于一切。
                    addCarMarker(latLngPoint.latitude, latLngPoint.longitude, Float.valueOf(heading));

                    //将经纬度存入缓存中
                    editor.putString("lon", lon + "");
                    editor.putString("lat", lat + "");
                    editor.commit();

                }

                //重载按钮
                if (Global.loadRoadsFlag) {
                    resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_check));
                } else {
                    resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_no_check));
                }

                Log.i(TAG, "主页面UI更新");
                break;
            case 777:
                String roadName = DataStorage.nameBlockQueue.poll(); //移除并返回队列头部元素
                /***********************************高德地图不绘制轨迹****************************************/
//                boolean drawFlag = (roadName != null) && (!roadName.equals(""));
//                if (drawFlag) {
//                    drawRoadInMap(roadName);
//                }

                //给下拉框添加轨迹
                roadNameChList = DataStorage.getMapName_CH_list();
                roadsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, roadNameChList);
                roadsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roadSpinner.setAdapter(roadsAdapter);
                //给下拉框添加区域
                roadAreaList = DataStorage.getMapAreaList();
                areasAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, roadAreaList);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                areaSpinner.setAdapter(areasAdapter);

//                Log.i(TAG, "主页面轨迹更新 drawFlag = " + drawFlag);
                break;
        }
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
        DataStorage.page = 1;
        DataStorage.mode = 1;


        if (scanUITask != null && scanUITask.getStatus() == AsyncTask.Status.RUNNING) {
            scanUITask.cancel(true);
        }
        scanUITask = new ScanUITask();
        scanUITask.executeOnExecutor(FULL_TASK_EXECUTOR);

        if (scanRoadsLoadTask != null && scanRoadsLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            scanRoadsLoadTask.cancel(true);
        }
        scanRoadsLoadTask = new ScanRoadsLoadTask();
        scanRoadsLoadTask.executeOnExecutor(FULL_TASK_EXECUTOR);

        super.onResume();
        mapView.onResume();

        //按钮恢复为未点击
//        controlImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.control_img_no_check));

        collectRoads.setImageDrawable(getResources().getDrawable(R.drawable.collect_map_no_check));
        reloadRoadTabLayout.performClick(); //重载轨迹
        if (DataStorage.collectMode == 2) {  //采集轨迹状态已经结束了,到主页面变为0
            DataStorage.collectMode = 0;

        }

        //初始化右侧控制按钮
        if (DataStorage.goGarageClick > 0) {
            goGarageBtn.setActivated(true);
        } else goGarageBtn.setActivated(false);
        if (DataStorage.goCleanClick > 0) {
            goCleanBtn.setActivated(true);
        } else goCleanBtn.setActivated(false);
        if (DataStorage.goGarbageStationClick > 0) {
            goGarbagestationBtn.setActivated(true);
        } else goGarbagestationBtn.setActivated(false);
        if (DataStorage.goPauseClick > 0) {
            goPauseBtn.setActivated(true);
            goPauseBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
        } else {
            goPauseBtn.setActivated(false);
            goPauseBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
        }
        if (DataStorage.dumpRubbishMode < 1) {
            presentationBtn.setActivated(true);
        } else presentationBtn.setActivated(false);

        refreshRoads(); //页面onResume重载
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
        if (sendGoOrStopTimer != null) { //停止发送出发、停止消息
            sendGoOrStopTimer.cancel();
        }
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
        Log.i("onStop", "enter");
        if (scanUITask != null && scanUITask.getStatus() == AsyncTask.Status.RUNNING) {
            scanUITask.cancel(true);
        }
        if (scanRoadsLoadTask != null && scanRoadsLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            scanRoadsLoadTask.cancel(true);
        }
        super.onStop();
        Log.i("onStop", "end");
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
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;

        if (sendGoOrStopTimer != null) {
            sendGoOrStopTimer.purge();
            sendGoOrStopTimer.cancel();
        }
        if (sendGoOrStopTask != null) {
            sendGoOrStopTask.cancel();
        }

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
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            Log.e("tag", "激活定位");
            /*
             * mAMapLocManager.setGpsEnable(false);
             * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
             * API定位采用GPS和网络混合定位方式
             * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
             */
            mAMapLocationManager.requestLocationUpdates(
                    LocationProviderProxy.AMapNetwork, 2000, 10, this);
//                // API定位采用GPS定位方式，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
//                mAMapLocationManager.requestLocationUpdates(
//                        LocationManagerProxy.GPS_PROVIDER, 2000, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        Log.e("tag", "停止定位");
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;
    }

    //清楚所有的marker
    private void clearMarkers() {
        aMap.clear();
    }

    /**
     * 发送请求加载路径
     */
    private void refreshRoads() {
        Log.i(TAG, "开始重置轨迹");
        if (Global.loadRoadsFlag) {
            return;
        }
        //清除已有路径信息
        DataStorage.nameBlockQueue.clear(); //原始轨迹名称队列
        DataStorage.roadsNameList.clear();  //所有轨迹名称
        DataStorage.roadsMap.clear();      //原始轨迹名称与经纬度Map
        DataStorage.initSelectMapName = "";  //初始化已经初步选中地图的名称
        for (Map.Entry<String, Polyline> en : aMapLineListMap.entrySet()) {
            en.getValue().remove();
        }

        DataStorage.cleanMapNameForSpinner(); //清除选择下拉框中的地图部分
        aMapLineListMap.clear(); //移除已经显示的地图轨迹
        clearMarkers();//移除覆盖物

        Global.loadRoadsFlag = true;
        resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_check));
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
            carMarker.remove();
//            carMarker.setPosition(new LatLng(lat, lot));
//            carMarker.setRotateAngle(360 - heading);
//            return;

        }
        //绘制marker
        carMarker = aMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lot))
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.bus)))
                .draggable(true));
        carMarker.setRotateAngle(360 - heading);
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
                ToastUtil.show(MainActivity.this, addressName);
            } else {
                ToastUtil.show(MainActivity.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.show(MainActivity.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.show(MainActivity.this, R.string.error_key);
        } else {
            ToastUtil.show(MainActivity.this,
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
                ToastUtil.show(MainActivity.this, addressName);
            } else {
                ToastUtil.show(MainActivity.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.show(MainActivity.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.show(MainActivity.this, R.string.error_key);
        } else {
            ToastUtil.show(MainActivity.this,
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
