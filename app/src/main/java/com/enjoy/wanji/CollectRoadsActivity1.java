package com.enjoy.wanji;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.enjoy.wanji.data.Common;
import com.enjoy.wanji.entity.DataStorage;
import com.enjoy.wanji.entity.DataStorageCollectMap;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.entity.DataStorageUtil;
import com.enjoy.wanji.entity.HttpDataAboard;
import com.enjoy.wanji.entity.StopOrientationEnum;
import com.enjoy.wanji.util.EnjoyDialogUtil;
import com.enjoy.wanji.util.ToastUtil;

import java.util.List;


public class CollectRoadsActivity1 extends Activity {
    private static final String TAG = "PageActivityCollect";
    private CollectDataReceiver collectDataReceiver;

    //    private Spinner collectAreaSpinner;

    private ImageButton pageBackImgBtn;
    private Button startCollectBtn, endCollectBtn,
            mapZoneBtn, mapNameBtn, speedBtn, mapLaneBtn, stopSideRoadWidthBtn, mergeLaneBtn,
            leftSafeBtn, rightSafeBtn, stopTimeBtn, stopIndexBtn, stopPropertyBtn, stopOrientationBtn,
            exSpeedBtn, laneStatusBtn, laneWidthBtn,  //temporaryStopBtn,
            leftLaneWidthBtn, rightLaneWidthBtn;

    private ImageView carStatusImage, collectImg;
    private TextView collectFlagTxt;

    private static Handler handler;

    //提示框
    private static AlertDialog.Builder addAreaDialog;
    private static AlertDialog dialog;

    //地图采集中时的提示
//    private LinearLayout collectingTagLayout;

    private final static int UPDATE_SPINNER = 777;
    private final static int RELOAD_ROADS = 555;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置本activity长亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //设置屏幕格式为横屏
        setContentView(R.layout.activity_collect_roads);

        initView();

        collectDataReceiver = new CollectDataReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Common.COLLECT_RECEIVER_ACTION);
        registerReceiver(collectDataReceiver, intentFilter);

    }


    /**
     * 初始化界面
     */
    private void initView() {
        DataStorage.page = 2;
        DataStorage.mode = 2; //模式变更为采集地图模式
        DataStorage.collectCleanEnable = 0; //不使能

//        collectingTagLayout = findViewById(R.id.roads_collecting_tag_layout);

        pageBackImgBtn = findViewById(R.id.back_in_collect_img_btn);
        pageBackImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorageCollectMap.collectMode == 1) { //正在采集中
                    ToastUtil.showShort(CollectRoadsActivity1.this, "地图采集中，请结束后再返回");
                    return;
                }
                Intent backIntent = new Intent(CollectRoadsActivity1.this, MainActivity.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backIntent);
            }
        });

        mapZoneBtn = findViewById(R.id.map_area_select_btn);
        mapNameBtn = findViewById(R.id.map_name_btn);
        speedBtn = findViewById(R.id.map_speed_btn);
        mapLaneBtn = findViewById(R.id.map_lane_btn);
        stopSideRoadWidthBtn = findViewById(R.id.map_stop_width_btn);
        mergeLaneBtn = findViewById(R.id.map_merge_lane_btn);
        leftSafeBtn = findViewById(R.id.map_left_width_btn);
        rightSafeBtn = findViewById(R.id.map_right_width_btn);
        stopIndexBtn = findViewById(R.id.map_stop_index_btn);
        stopTimeBtn = findViewById(R.id.map_stop_time_btn);
        stopPropertyBtn = findViewById(R.id.map_stop_property_btn);
        stopOrientationBtn = findViewById(R.id.map_stop_orientation_btn);

        exSpeedBtn = findViewById(R.id.ex_speed_btn);  //期望速度
        laneWidthBtn = findViewById(R.id.lane_width_btn); //车道宽度
        // temporaryStopBtn = findViewById(R.id.temporary_stop_btn); //临时停车
        laneStatusBtn = findViewById(R.id.lane_status_btn); //道路状态
        leftLaneWidthBtn = findViewById(R.id.left_lane_width_btn); //左道路宽
        rightLaneWidthBtn = findViewById(R.id.right_lane_width_btn);//右道路宽

        exSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getExSpeedList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "期望速度选择", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String speedStr = list.get(EnjoyDialogUtil.WHICH);
                                    exSpeedBtn.setText(speedStr);
                                    DataStorageCollectMap.exSpeed = Integer.parseInt(speedStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        //车道宽度  0  2.5  2.8  3  3.2  3.3  3.5 3.6 3.8
        laneWidthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getLaneWidthList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "车道宽度选择", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String laneWidthStr = list.get(EnjoyDialogUtil.WHICH);
                                    laneWidthBtn.setText(laneWidthStr);
                                    DataStorageCollectMap.laneWidth = Float.parseFloat(laneWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        /**
         temporaryStopBtn.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        final List<String> list = DataStorageUtil.getTemporaryStopList();
        EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
        list.toArray(new String[list.size()]), "临时停车", new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialog, int which) {
        if (EnjoyDialogUtil.WHICH != -1) {
        String temporaryStopStr = list.get(EnjoyDialogUtil.WHICH);
        temporaryStopBtn.setText(temporaryStopStr);
        DataStorageCollectMap.temporaryStop = Float.parseFloat(temporaryStopStr);
        }
        dialog.dismiss();
        }
        });
        }
        });
         **/

        //道路状态
        laneStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getLaneStatusList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "道路状态", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String laneStatusStr = list.get(EnjoyDialogUtil.WHICH);
                                    laneStatusBtn.setText(laneStatusStr);
                                    DataStorageCollectMap.laneStatus = DataStorageUtil.getLaneStatus(laneStatusStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        leftLaneWidthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getLeftRightWidthDisList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "左道路宽", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String leftLaneWidthStr = list.get(EnjoyDialogUtil.WHICH);
                                    leftLaneWidthBtn.setText(leftLaneWidthStr);
                                    DataStorageCollectMap.leftWidthDis = Float.parseFloat(leftLaneWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        rightLaneWidthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getLeftRightWidthDisList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "右道路宽", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String rightWidthStr = list.get(EnjoyDialogUtil.WHICH);
                                    rightLaneWidthBtn.setText(rightWidthStr);
                                    DataStorageCollectMap.rightWidthDis = Float.parseFloat(rightWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        //停车方位
        stopOrientationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorageCollectMap.stopOrientation == StopOrientationEnum.RIGHT.key) {
                    DataStorageCollectMap.stopOrientation = StopOrientationEnum.LEFT.key;
                } else if (DataStorageCollectMap.stopOrientation == StopOrientationEnum.LEFT.key) {
                    DataStorageCollectMap.stopOrientation = StopOrientationEnum.MIDDLE.key;

                } else {
                    DataStorageCollectMap.stopOrientation = StopOrientationEnum.RIGHT.key;
                }
                stopOrientationBtn.setText(StopOrientationEnum.getValue(DataStorageCollectMap.stopOrientation));

            }
        });

        mapNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = DataStorageUtil.getCollectMapNameList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        list.toArray(new String[list.size()]), "地图编号选择", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "设置地图编号显示" + EnjoyDialogUtil.WHICH);
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String mapNameStr = list.get(EnjoyDialogUtil.WHICH);
                                    mapNameBtn.setText(mapNameStr);
                                    DataStorageCollectMap.mapName = Integer.parseInt(mapNameStr);
                                }
                                dialog.dismiss();
                            }
                        });

            }
        });

        mapZoneBtn.setOnClickListener(new View.OnClickListener() { //选择园区
            @Override
            public void onClick(View v) {
                final List<String> mapAreaList1 = DataStorageUtil.getMapZoneCollectMapList();

                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        mapAreaList1.toArray(new String[mapAreaList1.size()]), "选择园区", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "设置园区显示" + EnjoyDialogUtil.WHICH);
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String zoneNameStr = mapAreaList1.get(EnjoyDialogUtil.WHICH);
                                    mapZoneBtn.setText(zoneNameStr);
                                    DataStorageCollectMap.zoneName = DataStorageUtil.getZoneNum(zoneNameStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        mapLaneBtn.setOnClickListener(new View.OnClickListener() { //车道属性
            @Override
            public void onClick(View v) {
                final List<String> mapLaneList = DataStorageUtil.getMapLaneList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        mapLaneList.toArray(new String[mapLaneList.size()]), "车道属性选择", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String laneStr = mapLaneList.get(EnjoyDialogUtil.WHICH);
                                    mapLaneBtn.setText(laneStr);
                                    DataStorageCollectMap.laneattr = DataStorageUtil.getMapLane(laneStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        stopSideRoadWidthBtn.setOnClickListener(new View.OnClickListener() { //临时停车距离
            @Override
            public void onClick(View v) {
                final List<String> stopSideRoadList = DataStorageUtil.getStopSideRoadWidthList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        stopSideRoadList.toArray(new String[stopSideRoadList.size()]), "临时停车距离", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String sideRoadWidthStr = stopSideRoadList.get(EnjoyDialogUtil.WHICH);
                                    stopSideRoadWidthBtn.setText(sideRoadWidthStr);
                                    DataStorageCollectMap.sideroadwidth = Float.parseFloat(sideRoadWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        mergeLaneBtn.setOnClickListener(new View.OnClickListener() { //切换属性
            @Override
            public void onClick(View v) {
                final List<String> mapAreaList1 = DataStorageUtil.getMergeLaneList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        mapAreaList1.toArray(new String[mapAreaList1.size()]), "切换道路属性", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String mergeLaneStr = mapAreaList1.get(EnjoyDialogUtil.WHICH);
                                    mergeLaneBtn.setText(mergeLaneStr);
                                    DataStorageCollectMap.mergelanetype = DataStorageUtil.getMergeLane(mergeLaneStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        //设置左右道路宽度默认值
        leftSafeBtn.setOnClickListener(new View.OnClickListener() { //左侧道路宽
            @Override
            public void onClick(View v) {
                final List<String> mapAreaList1 = DataStorageUtil.getSearChdisList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        mapAreaList1.toArray(new String[mapAreaList1.size()]), "左安全距离", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String leftWidthStr = mapAreaList1.get(EnjoyDialogUtil.WHICH);
                                    leftSafeBtn.setText(leftWidthStr);
                                    DataStorageCollectMap.leftsearchdis = Float.parseFloat(leftWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        rightSafeBtn.setOnClickListener(new View.OnClickListener() { //右侧道路宽
            @Override
            public void onClick(View v) {
                final List<String> rightWidthList = DataStorageUtil.getSearChdisList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        rightWidthList.toArray(new String[rightWidthList.size()]), "右安全距离", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String rightWidthStr = rightWidthList.get(EnjoyDialogUtil.WHICH);
                                    rightSafeBtn.setText(rightWidthStr);
                                    DataStorageCollectMap.rightsearchdis = Float.parseFloat(rightWidthStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
//        停车点索引
        stopIndexBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> indexList = DataStorageUtil.getStopIndexList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        indexList.toArray(new String[indexList.size()]), "停车点索引", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String indexStr = indexList.get(EnjoyDialogUtil.WHICH);
                                    stopIndexBtn.setText(indexStr);
                                    DataStorageCollectMap.index = Integer.parseInt(indexStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        stopTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> stopTimeList = DataStorageUtil.getStopTimeList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        stopTimeList.toArray(new String[stopTimeList.size()]), "停车点时间", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String stopTimeStr = stopTimeList.get(EnjoyDialogUtil.WHICH);
                                    stopTimeBtn.setText(stopTimeStr);
                                    DataStorageCollectMap.stopTime = Integer.parseInt(stopTimeStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });
        stopPropertyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> stopPropertyList = DataStorageUtil.getStopPropertyList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        stopPropertyList.toArray(new String[stopPropertyList.size()]), "停车点属性", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String stopPropertyStr = stopPropertyList.get(EnjoyDialogUtil.WHICH);
                                    stopPropertyBtn.setText(stopPropertyStr);
                                    DataStorageCollectMap.stopProperty = DataStorageUtil.getStopProperty(stopPropertyStr);
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        carStatusImage = findViewById(R.id.car_status_in_collect);

        collectImg = findViewById(R.id.flag_collect_img);

        startCollectBtn = findViewById(R.id.start_collect_btn);

        endCollectBtn = findViewById(R.id.end_collect_btn);

        collectFlagTxt = findViewById(R.id.flag_collect_txt);

        //下拉风格

        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳出速度道路选择框
                final List<String> speedList = DataStorageUtil.getRoadPropertyList();
                EnjoyDialogUtil.selectItemDialog(CollectRoadsActivity1.this,
                        speedList.toArray(new String[speedList.size()]), "道路属性选择", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (EnjoyDialogUtil.WHICH != -1) {
                                    String speedStr = speedList.get(EnjoyDialogUtil.WHICH);
                                    speedBtn.setText(speedStr);
                                    DataStorageCollectMap.roadProperty = DataStorageUtil.getRoadProperty(speedStr);
                                }
                                dialog.dismiss();
                            }
                        });

//                createSpeedPickerDialog();
            }
        });

        startCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorageCollectMap.collectMode == 1) { //已经是采集模式了
                    return;
                }
                //判断道路属性选择与否
                if (DataStorageCollectMap.roadProperty == 0 && DataStorageCollectMap.stopProperty == 0) {
                    ToastUtil.showShort(getApplicationContext(), "速度和停车属性不可同时为0");
                    return;
                }
                if (DataStorageCollectMap.zoneName == 0) {  //园区没选择
                    ToastUtil.showShort(getApplicationContext(), "请先选择区域");
                    return;
                }

                if (!Global.connectFlag) {
                    Log.i(TAG, "未连接");
                    return;
                }

                //准备打开发送采集地图任务
                DataStorageCollectMap.collectMode = 1; //采集地图模式开启

                //开始按钮不可点击
                startCollectBtn.setClickable(false);

                startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
                startCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
                endCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

            }
        });

        endCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorageCollectMap.collectMode == 1) {
                    //结束地图采集
                    DataStorageCollectMap.collectMode = 2;
                } else {
                    //不是正在采集中，点击采集结束无效
                    return;
                }

                DataStorageCollectMap.roadProperty = 0;
                speedBtn.setText(DataStorageCollectMap.roadProperty + "");
                DataStorageCollectMap.stopProperty = 0;
                stopPropertyBtn.setText(DataStorageUtil.getStopPropertyList().get(0));
                //相关按钮点击属性打开
                startCollectBtn.setClickable(true);

                endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
                endCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
                startCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

                //重新加载园区
                handler.sendEmptyMessageDelayed(RELOAD_ROADS, 1000);

                ToastUtil.showShort(getApplicationContext(), "采集成功");
            }
        });

        handler = new

                Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        myHandleMessage(msg.what);
                    }
                }

        ;

    }


    /**
     * 接收数据
     */
    public class CollectDataReceiver extends BroadcastReceiver {

        //接收到监测数据
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(Common.ACTION_NAME, -1);
            Log.i(TAG, "get BroadcastReceiver type = " + type);
            if (type != -1) {
                handler.sendEmptyMessage(type);
            }
        }

    }

    public void addAreaView() {
        final TableLayout addAreaForm = (TableLayout) getLayoutInflater()
                .inflate(R.layout.add_area, null);

        new AlertDialog.Builder(this)
                .setTitle("新增园区")
                .setView(addAreaForm)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        EditText areaNameTxt = findViewById(R.id.area_edit_txt);
                        EditText areaNameTxt = addAreaForm.findViewById(R.id.area_edit_txt);
                        Editable editable = areaNameTxt.getText();
                        String areaNameStr = editable == null ? "" : editable.toString();
                        areaNameStr = areaNameStr.replaceAll(" ", "");
                        areaNameStr = areaNameStr.replaceAll("\n", "");
                        if ("".equals(areaNameStr)) {
                            ToastUtil.showShort(CollectRoadsActivity1.this, "园区名称不能为空");
                            return;
                        }
                        if (!Global.connectFlag) {
                            ToastUtil.showShort(CollectRoadsActivity1.this, "未连接");
                            return;
                        }
                        if (DataStorage.areasNameWithChnKeyMap.get(areaNameStr) != null) {
                            ToastUtil.showShort(CollectRoadsActivity1.this, "该园区名称已存在");
                            return;
                        }
                        HttpDataAboard.addAreaNameParam = areaNameStr;
                        HttpDataAboard.addAreaFlag = true;

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    /**
     * handler处理消息
     *
     * @param what
     */
    private void myHandleMessage(int what) {
        switch (what) {
            case Common.ACTION_UI_UPDATE:
                //车辆gps状态
                if (DataStorageFromPC.rtk >= 4) {
//                    carStatusImage.setImageResource(R.drawable.normal);
                    carStatusImage.setImageResource(R.drawable.car_status_green);
                } else {
                    carStatusImage.setImageResource(R.drawable.car_status_default);
                }

                Log.i(TAG, "UI更新");
                break;
            case RELOAD_ROADS:
                refresh();
                break;
            case Common.ACTION_UPDATE_COLLECT:
                Drawable drawable = getResources().getDrawable(R.drawable.collect_no);
                int collectTextId = R.string.collect_no;
                if (DataStorageCollectMap.collectMode == 1) { //采集中的，
                    if (System.currentTimeMillis() % 1000 < 500) { //让图标一秒闪两次,当前时间偶数秒就变图标
                        drawable = getResources().getDrawable(R.drawable.collect_doing);
                    }
                    collectTextId = R.string.collect_doing;
                }
                collectImg.setImageDrawable(drawable);
                collectFlagTxt.setText(collectTextId);
                break;

        }
    }

    /**
     * 刷新
     */
    private void refresh() {
        Log.i(TAG, "开始刷新");
        if (Global.loadRoadsFlag) {
            return;
        }
        //清除已有路径信息
        DataStorageFromPC.roadsMap.clear();      //原始轨迹名称与经纬度Map
        DataStorageFromPC.zoneNameJsonListMap.clear();
        Global.loadRoadsFlag = true;

        Log.i(TAG, "重置完成");

    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onResume() {
        super.onResume();
//        reloadRoads();
        //地图采集模式 0--否  1---是   2---结束
        Log.i(TAG, "collectMap获取mode = " + DataStorageCollectMap.collectMode);
        DataStorage.page = 2;

        //初始化模式
        if (DataStorageCollectMap.collectMode == 1) {  //正在采集中

            startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
            startCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

            endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
            endCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

        } else if (DataStorageCollectMap.collectMode == 2) {
            endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
            endCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

            startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
            startCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
        }

        //初始显示采集参数
        //注意button.setText()方法 显示为int类型数字时，
        // 不要将数字直接放进去，因为这里setText方法是将int的参数作为string资源的id去获取string资源
        mapZoneBtn.setText(DataStorageUtil.getZoneName());
        mapNameBtn.setText(DataStorageCollectMap.mapName + "");
        speedBtn.setText(DataStorageUtil.getRoadPropertyList().get(DataStorageCollectMap.roadProperty));
        mapLaneBtn.setText(DataStorageUtil.getMapLaneList().get(DataStorageCollectMap.laneattr - 1));
        stopSideRoadWidthBtn.setText(DataStorageCollectMap.sideroadwidth + "");
        mergeLaneBtn.setText(DataStorageUtil.getMergeLaneList().get(DataStorageCollectMap.mergelanetype - 1));
        leftSafeBtn.setText(DataStorageCollectMap.leftsearchdis + "");
        rightSafeBtn.setText(DataStorageCollectMap.rightsearchdis + "");
        stopIndexBtn.setText(DataStorageCollectMap.index + "");
        stopTimeBtn.setText(DataStorageCollectMap.stopTime + "");
        stopPropertyBtn.setText(DataStorageUtil.getStopPropertyList().get(DataStorageCollectMap.stopProperty));
        stopOrientationBtn.setText(StopOrientationEnum.getValue(DataStorageCollectMap.stopOrientation));


        exSpeedBtn.setText(DataStorageCollectMap.exSpeed + ""); //期望速度
        laneWidthBtn.setText(DataStorageCollectMap.laneWidth + ""); //车道宽度
        //temporaryStopBtn.setText(DataStorageCollectMap.temporaryStop + "");  //临时停车

        //道路状态   ("1-正常道路")   ("2-颠簸道")  ("3-闸机口");
        laneStatusBtn.setText(DataStorageUtil.getLaneStatusList().get(DataStorageCollectMap.laneStatus - 1));

        leftLaneWidthBtn.setText(DataStorageCollectMap.leftWidthDis + "");
        rightLaneWidthBtn.setText(DataStorageCollectMap.rightWidthDis + "");
//        refreshRoads();
    }


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        Log.i(TAG, "CollectRoadsActivity onDestroy");
        super.onDestroy();
        //注销监广播听器
        unregisterReceiver(collectDataReceiver);
    }

}
