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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.enjoy.wanji.data.Common;
import com.enjoy.wanji.entity.AttentionInfo;
import com.enjoy.wanji.entity.AttentionTypeEnum;
import com.enjoy.wanji.entity.DataStorage;
import com.enjoy.wanji.entity.DataStorageCollectMap;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.entity.DataStorageToPC;
import com.enjoy.wanji.entity.DataStorageUtil;
import com.enjoy.wanji.entity.AttentionContentEnum;
import com.enjoy.wanji.entity.ErrorContentEnum;
import com.enjoy.wanji.service.EnjoySocketService;
import com.enjoy.wanji.util.AMapUtil;
import com.enjoy.wanji.util.EnjoyDialogUtil;
import com.enjoy.wanji.util.ToastUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity extends Activity implements LocationSource, AMapLocationListener,
        GeocodeSearch.OnGeocodeSearchListener, OnMapTouchListener {//定位接口
    private static final String TAG = "PageActivityMain";
    private static String mediaTag = "media_tag";

    private Context mContext;
    private AMap aMap;//地图控制器类
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;

    private RadioButton endRaBtn, goRaBtn; //defaultRaBtn
    private TextView speedText, errorText, objDisTxt;
    //  连接  采集地图 遥控器  删除轨迹 重载
    private ImageButton collectRoads, resetImgBtn,  // deleteImgBtn,
            errorImgBtn, speedPanelImg;
    private Button mapZoneSelectBtn, mapParkBtn, renameBtn, estopBtn;  //parkBtn
    private ImageView obs_img, gps_img, auto_drive_img, trafficLight;

    //遥控器，采集地图，重载， 删除地图 组合控件
    private LinearLayout collectTabLayout, reloadRoadTabLayout;   //deleteRoadTabLayout

    private UiSettings uiSettings;

    /********************************************************************************/
    static AlertDialog.Builder errorDialog = null;
    static MyDialogPopWindow dialogPopWindow = null;
    TextView titleTxt, msgTxt;

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
        mapView = findViewById(R.id.map); //获取地图控件引用
        mapView.onCreate(savedInstanceState);// 此方法必须重写  创建地图

        //初始化控件
        initView();
        //注册广播接收器
        mainActivityDataReceiver = new MainActivityDataReceiver();
        //接收器设置指定action
        IntentFilter filter = new IntentFilter();
        filter.addAction(Common.MAIN_RECEIVER_ACTION);
        registerReceiver(mainActivityDataReceiver, filter);
        //启动服务连接
        Intent intent = new Intent(MainActivity.this, EnjoySocketService.class);
        intent.putExtra(Common.ACTION_NAME, Common.ACTION_CONNECT);
        startService(intent);

    }


    /**
     * 绘制轨迹
     *
     * @param zoneName
     */
    private void drawRoadInMap(String zoneName) {
        List<JSONObject> jsonList = DataStorageFromPC.zoneNameJsonListMap.get(zoneName);
        if (jsonList == null || jsonList.size() == 0) {
            return;
        }
        //清除已有的轨迹
        for (Polyline line : polylineList) {
            line.remove();
        }

        for (JSONObject jsonObject : jsonList) {

            List<LatLng> temp = new ArrayList();
            JSONArray pointsArray = (JSONArray) jsonObject.get("points");
            if (pointsArray == null || pointsArray.isEmpty()) {
                continue;
//                return;
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
                    .width(20).color(Color.argb(255, 0, 255, 1));
            Polyline poly = aMap.addPolyline(po);
            polylineList.add(poly);
        }

        Log.i(TAG, "画路线完成" + zoneName);
    }


    /**
     * 初始化界面
     */
    private void initView() {

        DataStorage.mode = 1;    //订阅模式 1--显示订阅信息模式 2--采集地图模式

        RelativeLayout leftLayout = findViewById(R.id.left_menu_panel);
        leftLayout.bringToFront(); //让左侧菜单栏置顶

        collectTabLayout = findViewById(R.id.collect_tab); //采集地图跳转组件
        reloadRoadTabLayout = findViewById(R.id.reload_tab); //重载轨迹组件
//        deleteRoadTabLayout = findViewById(R.id.delete_road_tab); //删除轨迹组件

        errorImgBtn = findViewById(R.id.error_img_btn);  //电量
        speedPanelImg = findViewById(R.id.speed_panel_img);

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
            aMap.animateCamera(CameraUpdateFactory.zoomTo(16)); //放大等级
            this.normalRouteBlue = BitmapDescriptorFactory.fromAsset("blue.png");
            this.normalRouteGreen = BitmapDescriptorFactory.fromAsset("green.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("yellow.png");
            this.normalRouteYellow = BitmapDescriptorFactory.fromAsset("grey.png");

            //读取缓存经纬度数据  116.416797,40.037969
            String lonStr = EnjoyTrainShipApplication.sharedPreferences.getString("lon", "116.416797");
            String latStr = EnjoyTrainShipApplication.sharedPreferences.getString("lat", "40.037969");
            Log.i(TAG, "获取缓存经纬度" + lonStr + "," + latStr);
            LatLng latLng1 = ChangeLatlon.transform(Double.parseDouble(latStr), Double.parseDouble(lonStr));
            aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng1)); //中心点

            aMap.setMyLocationRotateAngle(90);

        }
        //采集地图按钮
        collectRoads = findViewById(R.id.collect_map_img_btn);
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
//                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity.class);
                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity1.class);
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
//                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity.class);
                Intent mapIntent = new Intent(MainActivity.this, CollectRoadsActivity1.class);
                startActivity(mapIntent);
            }
        });

        //重载按钮
        resetImgBtn = findViewById(R.id.reload_img_btn);
        reloadRoadTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh("reloadRoadTabLayout");  //刷新按钮
            }
        });
        resetImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh("resetBtn");  //刷新按钮
            }
        });

        //删除轨迹按钮
//        deleteImgBtn = findViewById(R.id.delete_road_img_btn);
//        deleteImgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteRoadsSets();
//            }
//        });
//        deleteRoadTabLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteRoadsSets();
//            }
//        });


        //速度与电池文字显示
        speedText = findViewById(R.id.speed_txt);
        errorText = findViewById(R.id.error_txt);
        objDisTxt = findViewById(R.id.obj_dis_txt);

        //障碍物图片
        obs_img = findViewById(R.id.obstacle_img);
        //gps图片显示
        gps_img = findViewById(R.id.gps_img);
        //自动驾驶状态图片
        auto_drive_img = findViewById(R.id.auto_drive_img);
        //红绿灯
        trafficLight = findViewById(R.id.traffic_light);

        //故障图片
//        errorImg = findViewById(R.id.error_img);

        //结束按钮  //出发按钮 radioButton
        endRaBtn = findViewById(R.id.end_radio_btn);
        goRaBtn = findViewById(R.id.go_radio_btn);
        //0无效、1出发、停止2
        goRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataStorageToPC.stopgo = 1;
                    DataStorage.stopGoTimeStamp = System.currentTimeMillis();
                    goRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    goRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });
        endRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataStorage.stopGoTimeStamp = System.currentTimeMillis();
                    DataStorageToPC.stopgo = 2;
                    endRaBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    endRaBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });


        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        progDialog = new ProgressDialog(this);

        estopBtn = findViewById(R.id.estop_btn); //临时停车
        //parkBtn = findViewById(R.id.park_btn);  //泊车
        estopBtn.setOnClickListener(new View.OnClickListener() { //临时停车0-1切换
            @Override
            public void onClick(View v) {
                if (DataStorageToPC.eStop == 0) {
                    DataStorageToPC.eStop = 1;
                    estopBtn.setTextColor(Color.WHITE);
                    estopBtn.setBackground(getResources().getDrawable(R.drawable.button_shape_activate));
                } else {
                    DataStorageToPC.eStop = 0;
                    estopBtn.setTextColor(Color.BLACK);
                    estopBtn.setBackground(getResources().getDrawable(R.drawable.button_shape_default));
                }
            }
        });
        //泊车
        /**
         parkBtn.setOnClickListener(new View.OnClickListener() {  //发5秒1，再变0触发加载回停车场地图
         @Override public void onClick(View v) {
         DataStorageToPC.setPark();
         parkBtn.setTextColor(Color.WHITE);
         parkBtn.setBackground(getResources().getDrawable(R.drawable.button_shape_activate));
         //5秒后更新返回非点击状态
         handler.sendEmptyMessageDelayed(Common.ACTION_UI_UPDATE_PARK, 5500);
         }
         });
         **/

        renameBtn = findViewById(R.id.edit_btn);
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorageToPC.zoneName == 0) {
                    ToastUtil.showShort(MainActivity.this, "请先选择园区");
                    return;
                }
                tipViewShow();
            }
        });

        //园区选择按钮
        mapZoneSelectBtn = findViewById(R.id.map_zone_select_btn);
        mapZoneSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getMapZoneMainList();
                EnjoyDialogUtil.selectItemDialog(MainActivity.this, list.toArray(new String[list.size()]),
                        "选择园区", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 != EnjoyDialogUtil.WHICH) {
                                    String zoneName = list.get(EnjoyDialogUtil.WHICH);
                                    mapZoneSelectBtn.setText(zoneName);
                                    //提取园区编号 例： yuanqu2或2---公园
                                    DataStorageToPC.zoneName = DataStorageUtil.getZoneNum(zoneName);
                                    //发送去绘制轨迹
                                    handler.sendEmptyMessage(Common.ACTION_UI_ROADS_SHOW);
                                }
                            }
                        });
            }
        });

        //路线选择下拉框部分
        final List<String> parkList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            parkList.add(i + "");
        }
        mapParkBtn = findViewById(R.id.map_park_btn);  //停车点选择
        mapParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnjoyDialogUtil.selectItemDialog(MainActivity.this, parkList.toArray(new String[parkList.size()]),
                        "选择停车点", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 != EnjoyDialogUtil.WHICH) {
                                    String parkStr = parkList.get(EnjoyDialogUtil.WHICH);
                                    mapParkBtn.setText(parkStr);
                                    DataStorageToPC.apsNum = Integer.parseInt(parkStr);
                                }
                            }
                        });
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
     * 园区重命名编辑框
     */
    private void tipViewShow() {
        LinearLayout editeLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.edit_zone_layout, null);

        final EditText roadNameEdt = editeLayout.findViewById(R.id.zone_name_edt);
        TextView roadNumTxt = editeLayout.findViewById(R.id.zone_num_txt);
        roadNumTxt.setText("园区编号：" + DataStorageToPC.zoneName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("园区命名");
        builder.setView(editeLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int num = DataStorageToPC.zoneName;
                Log.i("vvvvvvvvvvvvvvvv", "get num = " + num);
                Editable roadEdt = roadNameEdt.getText();
                if (roadEdt == null) {
                    ToastUtil.showShort(MainActivity.this, "名称不可为空");
                    return;
                }
                String zoneNameNew = roadEdt.toString();
                if ("".equals(zoneNameNew)) {
                    ToastUtil.showShort(MainActivity.this, "名称不可为空");
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
            case Common.ACTION_REFRESH:
                refresh("after delete");  //删除轨迹后的
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
                //连接状态
                if (Global.connectFlag) {
                    //todo 改变logo连接颜色
                } else {
                    //todo 改变logo连接颜色
                    //红绿灯
                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_null));
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
                if (!Global.connectFlag) {
                    obs_img.setImageResource(R.drawable.obstacle_no);
                } else {
                    if (DataStorageFromPC.objDis.contains("-")) {  //无障碍物
                        obs_img.setImageResource(R.drawable.obstacle_no);
                    } else {             //有障碍物
                        obs_img.setImageResource(R.drawable.obstacle);
                    }
                }
                //障碍物距离
                objDisTxt.setText(DataStorageFromPC.objDis);

                //  GPS显示
                if (!Global.connectFlag) {
                    gps_img.setImageResource(R.drawable.gps0);
                } else {
                    Log.i("aaaaaaaaaaa", "更新gps rtk = " + DataStorageFromPC.rtk);
                    if (DataStorageFromPC.rtk >= 4) {
                        gps_img.setImageResource(R.drawable.gps3);
                    } else if (1 < DataStorageFromPC.rtk && DataStorageFromPC.rtk < 4) {
                        gps_img.setImageResource(R.drawable.gps2);
                    } else {
                        gps_img.setImageResource(R.drawable.gps1);
                    }
                }

                //驾驶状态
                if (!Global.connectFlag) {
                    auto_drive_img.setImageResource(R.drawable.no_auto_drive);
                } else {
                    if (DataStorageFromPC.driverStatus == 0) { //非自动驾驶状态
                        auto_drive_img.setImageResource(R.drawable.manual_drive_img);
                    } else {                         //自动驾驶状态
                        auto_drive_img.setImageResource(R.drawable.auto_drive_img);
                    }
                }

                //设置电量
                if (!Global.connectFlag) { //未连接
                    errorImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.error_default));
                } else {    //连接
                    errorImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.error_img));
                }
                errorText.setText(DataStorageFromPC.error + "");

                //GPS速度等数据
                if (!Global.connectFlag) {
                    speedPanelImg.setImageDrawable(getResources().getDrawable(R.drawable.speed_panel0));
                } else {
                    speedPanelImg.setImageDrawable(getResources().getDrawable(R.drawable.speed_panel));
                }
                speedText.setText(DataStorageFromPC.speedStr);

                //重载按钮
                if (Global.loadRoadsFlag) {
                    resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_check));
                } else {
                    resetImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.reload_no_check));
                }
                //故障报警
                attentionDialogShow();

                Log.i(TAG, "主页面UI更新");
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
                String roadName = Common.ZONE_HEAD + DataStorageToPC.zoneName;
                drawRoadInMap(roadName);
                break;
            case Common.ACTION_UI_V2X:  //v2x
                //0:无 1：红灯 2：绿灯 3：黄灯
                if (0 == DataStorageFromPC.lightColor) {
                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_null));
                } else if (1 == DataStorageFromPC.lightColor) {  //红
                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_red));
                } else if (2 == DataStorageFromPC.lightColor) {  //绿
                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_green));
                } else {  //黄
                    trafficLight.setImageDrawable(getResources().getDrawable(R.drawable.light_yellow));
                }
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

    String testTag = "hhhhhhhhhhhhh";

    private void attentionDialogShowImp(int type, int key) {
        Log.i(testTag, "type =" + type + ". key = " + key);
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
            Log.i(testTag, "语音唤醒线程开始唤醒1");
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
        if (type == AttentionTypeEnum.ERROR.key) {
            AttentionInfo.message = AttentionContentEnum.getValue(key) + "\n" + "故障码" + DataStorageFromPC.error;
        } else {
            AttentionInfo.message = AttentionContentEnum.getValue(key);
        }

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

        titleTxt.setText(AttentionInfo.title);
        msgTxt.setText(AttentionInfo.message);
        dialogPopWindow.showAtLocation(findViewById(R.id.activity_main), Gravity.CENTER, 0, 0);
        Log.i(testTag, "语音唤醒线程开始唤醒2");
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
                    Log.i(testTag, "语音唤醒线程开始唤醒3");
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
        mapView.onResume();

        //按钮恢复为未点击
//        controlImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.control_img_no_check));

        collectRoads.setImageDrawable(getResources().getDrawable(R.drawable.collect_map_no_check));
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
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;

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
        for(Polyline line: polylineList){
            line.remove();
        }
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
                ToastUtil.showShort(MainActivity.this, addressName);
            } else {
                ToastUtil.showShort(MainActivity.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.showShort(MainActivity.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.showShort(MainActivity.this, R.string.error_key);
        } else {
            ToastUtil.showShort(MainActivity.this,
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
                ToastUtil.showShort(MainActivity.this, addressName);
            } else {
                ToastUtil.showShort(MainActivity.this, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.showShort(MainActivity.this, R.string.error_network);
        } else if (rCode == 32) {
            ToastUtil.showShort(MainActivity.this, R.string.error_key);
        } else {
            ToastUtil.showShort(MainActivity.this,
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
