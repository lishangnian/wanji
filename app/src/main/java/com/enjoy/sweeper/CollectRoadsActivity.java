package com.enjoy.sweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.amap.api.maps.model.Polyline;
import com.enjoy.sweeper.entity.DataStorage;
import com.enjoy.sweeper.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CollectRoadsActivity extends Activity {
    private static final String TAG = "PageActivityCollect";
    private RadioButton roadsLeftBtn, roadsMiddleBtn, roadsRightBtn, laneSingleBtn,
            pathDefaultRadioBtn, pathDumpRadioBtn, pathParkRadioBtn,
            forwardRadioBtn, backRadioBtn, speedSlowSelectRaBtn, speedNormalSelectRaBtn, speedFastSelectRaBtn;


    private Spinner collectAreaSpinner;
    private ArrayAdapter<String> collectAreaAdapter;
    private List<String> roadAreaList = new ArrayList<>();

    private ImageButton pageBackImgBtn,
            sweepEnableImgBtn, sweepSwitchImgBtn, pathValidImgBtn;
    private Button startCollectBtn, endCollectBtn, speedBtn, intOrOutBtn;
    //    private EditText mapNameView; switchBtn

    private ImageView carStatusImage;
    private TextView batteryText;

    private ScanUITask scanUITask;

    private static Handler handler;


    private static int collectMapSpeedTemp = 3;
//    private static String mapNameHead = "maping";
//    private static List<String> mapNameList = new ArrayList<>();


    //一键报警提示框
    private static AlertDialog.Builder mAlertDialog;
    private static AlertDialog dialog;

    //地图采集中时的提示
    private LinearLayout collectingTagLayout;

    private final static int UPDATE_SPINNER = 777;
    private final static int UPDATE_BASE_UI = 666;
    private final static int RELOAD_ROADS = 555;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置本activity长亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //设置屏幕格式为横屏
        setContentView(R.layout.activity_collect_roads);

        initView();

    }


    /**
     * 初始化界面
     */
    private void initView() {
        DataStorage.page = 2;
        DataStorage.mode = 2; //模式变更为采集地图模式
        DataStorage.collectCleanEnable = 0; //不使能

        collectingTagLayout = findViewById(R.id.roads_collecting_tag_layout);

        pageBackImgBtn = (ImageButton) findViewById(R.id.back_in_collect_img_btn);
        pageBackImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectMode == 1) { //正在采集中
                    ToastUtil.show(CollectRoadsActivity.this, "地图采集中，请结束后再返回");
                    return;
                }
                Intent backIntent = new Intent(CollectRoadsActivity.this, MainActivity.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backIntent);
            }
        });

        laneSingleBtn = (RadioButton) findViewById(R.id.roads_type_single_rb);
        roadsLeftBtn = (RadioButton) findViewById(R.id.roads_type_left_rb);
        roadsMiddleBtn = (RadioButton) findViewById(R.id.roads_type_middle_rb);
        roadsRightBtn = (RadioButton) findViewById(R.id.roads_type_right_rb);
        laneSingleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //修改背景图片
                    laneSingleBtn.setBackground(getResources().getDrawable(R.drawable.single_lane_checked));
                    DataStorage.collectMapLaneType = 1;
                } else {
                    laneSingleBtn.setBackground(getResources().getDrawable(R.drawable.single_lane_default));
                }

            }
        });
        roadsMiddleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    roadsMiddleBtn.setBackground(getResources().getDrawable(R.drawable.middle_lane_checked));
                    DataStorage.collectMapLaneType = 3;
                } else {
                    roadsMiddleBtn.setBackground(getResources().getDrawable(R.drawable.middle_lane_default));
                }
            }
        });
        roadsRightBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    roadsRightBtn.setBackground(getResources().getDrawable(R.drawable.right_lane_checked));
                    DataStorage.collectMapLaneType = 2;
                } else {
                    roadsRightBtn.setBackground(getResources().getDrawable(R.drawable.right_lane_default));
                }
            }
        });
        roadsLeftBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    roadsLeftBtn.setBackground(getResources().getDrawable(R.drawable.left_lane_checked));
                    DataStorage.collectMapLaneType = 4;
                } else {
                    roadsLeftBtn.setBackground(getResources().getDrawable(R.drawable.left_lane_default));
                }
            }
        });


        //设置路线类型，清扫、回站、入库
        intOrOutBtn = findViewById(R.id.in_or_out_btn);
        pathDumpRadioBtn = findViewById(R.id.path_dump_rbtn);
        pathParkRadioBtn = findViewById(R.id.path_park_rbtn);
        pathDefaultRadioBtn = findViewById(R.id.path_default_rbtn);
        intOrOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectGetInOrOut == 0) {
                    DataStorage.collectGetInOrOut = 1;
                    intOrOutBtn.setText("出站");
                } else {
                    DataStorage.collectGetInOrOut = 0;
                    intOrOutBtn.setText("进站");
                }
            }
        });
        pathDefaultRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                forwardRadioBtn.performClick();    //清扫模式，方向一定是前进
                forwardRadioBtn.setChecked(true);
                if (DataStorage.collectMode == 1 && DataStorage.collectPathProperty != 0) {
                    if (DataStorage.collectPathProperty == 1) pathDumpRadioBtn.performClick();
                    if (DataStorage.collectPathProperty == 2) pathParkRadioBtn.performClick();
                    isChecked = false;
                    //非清扫采集中，不可切换属性
                    return;
                }
                if (isChecked) {
                    DataStorage.collectPathProperty = 0;
                    pathDefaultRadioBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    pathDefaultRadioBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });
        pathDumpRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (DataStorage.collectMode == 1) { //地图采集中，不允许切换
                    if (DataStorage.collectPathProperty == 0) pathDefaultRadioBtn.performClick();
                    if (DataStorage.collectPathProperty == 2) pathParkRadioBtn.performClick();
                    return;
                }
                if (isChecked) {
                    DataStorage.collectPathProperty = 1; //（垃圾）场站路径
                    pathDumpRadioBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    pathDumpRadioBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });
        pathParkRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (DataStorage.collectMode == 1 && DataStorage.collectPathProperty != 2) {
                    ToastUtil.show(getApplicationContext(), "道路采集中，不可切换此属性");
                    return;
                }
                if (isChecked) {  //车库路径
                    DataStorage.collectPathProperty = 2;
                    pathParkRadioBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));
                } else {
                    pathParkRadioBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
                }
            }
        });

        speedNormalSelectRaBtn = findViewById(R.id.speed_normal_btn);
        speedFastSelectRaBtn = findViewById(R.id.speed_fast_btn);
        speedSlowSelectRaBtn = findViewById(R.id.speed_slow_btn);
        speedSlowSelectRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataStorage.collectMapSpeed = 3;
                    speedBtn.setText(DataStorage.collectMapSpeed + "");
                }
            }
        });
        speedNormalSelectRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataStorage.collectMapSpeed = 5;
                    speedBtn.setText(DataStorage.collectMapSpeed + "");
                }
            }
        });
        speedFastSelectRaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataStorage.collectMapSpeed = 10;
                    speedBtn.setText(DataStorage.collectMapSpeed + "");
                }
            }
        });


        forwardRadioBtn = findViewById(R.id.forward_radio_btn);
        backRadioBtn = findViewById(R.id.back_radio_btn);
        forwardRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (DataStorage.collectMode == 1) {    //地图采集中，
                    if (DataStorage.collectReverse == 1) {   //且是倒车模式，不切换
                        backRadioBtn.performClick();
                        isChecked = false;
                    }
                    return;
                }
                if (isChecked) {
                    DataStorage.collectReverse = 0;
                } else {
                    DataStorage.collectReverse = 1;
                }
            }
        });
        backRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (DataStorage.collectMode == 1 && DataStorage.collectReverse == 0) { //地图采集中，且是前进模式，不切换
                    forwardRadioBtn.performClick();
                    forwardRadioBtn.setChecked(true);
                    isChecked = false;
                    return;
                }
                if (DataStorage.collectPathProperty == 0) { //默认清扫模式下，点击方向无效，改为前进模式
                    return;
                }
                if (isChecked) {
                    DataStorage.collectReverse = 1;
                } else {
                    DataStorage.collectReverse = 0;
                }
            }
        });


        carStatusImage = findViewById(R.id.car_status_in_collect);
        startCollectBtn = findViewById(R.id.start_collect_btn);
        endCollectBtn = findViewById(R.id.end_collect_btn);

        batteryText = (TextView) findViewById(R.id.battery_in_collect);


        pathValidImgBtn = findViewById(R.id.path_valid_img_btn);
        sweepSwitchImgBtn = findViewById(R.id.switch_sweep_img_btn);
        sweepEnableImgBtn = findViewById(R.id.sweep_enable_img_btn);
        sweepEnableImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectCleanEnable == 0) { //转为清扫使能
                    DataStorage.collectCleanEnable = 1;
                    sweepEnableImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.sweep_enable_true));
                } else {                                //转为清扫不使能
                    DataStorage.collectCleanEnable = 0;
                    sweepEnableImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.sweep_enable_false));
                }
            }
        });
        sweepSwitchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectCleanEnable == 0) { //当前清扫非使能，无效
                    return;
                }
                if (DataStorage.collectSweepMode == 1) {
                    DataStorage.collectSweepMode = 2; //转为清扫
                    sweepSwitchImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.sweep_run));

                } else {
                    DataStorage.collectSweepMode = 1; //转为暂停，抬起扫把
                    sweepSwitchImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.sweep_pause));

                }
            }
        });
        pathValidImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectPathValid == 0) {
                    DataStorage.collectPathValid = 1;
                    pathValidImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.path_invalid));
                } else {
                    DataStorage.collectPathValid = 0;
                    pathValidImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.path_valid));
                }
            }
        });


        collectAreaSpinner = findViewById(R.id.collect_area_spinner);
        collectAreaAdapter = new ArrayAdapter(CollectRoadsActivity.this, android.R.layout.simple_spinner_item, DataStorage.getMapAreaListForCollectMap());
        //下拉风格
        collectAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collectAreaSpinner.setAdapter(collectAreaAdapter);
        collectAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String areaName = DataStorage.getMapAreaList().get(position);
                String areaName = DataStorage.getMapAreaListForCollectMap().get(position);
                DataStorage.setCollectArea(areaName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        speedBtn = findViewById(R.id.speed_click_select_btn);
        speedBtn.setText(DataStorage.collectMapSpeed + "");
        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳出速度选择框
                createSpeedPickerDialog();
            }
        });

        startCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectMode == 1) { //已经是采集模式了
                    return;
                }
                //判断道路属性选择与否
                if (DataStorage.collectMapLaneType < 1) { //没有选中道路属性
                    ToastUtil.show(getApplicationContext(), "请先选择道路属性");
                    return;
                }
                if (DataStorage.collectMapSpeed == 0) {
                    ToastUtil.show(getApplicationContext(), "速度不可为0");
                    return;
                }
                if (!Global.connectFlag) {
                    Log.i(TAG, "未连接");
                    return;
                }


                //新版不需要在pad端命名地图名称
                /**
                 for (String name : mapNameList) {
                 Log.i(TAG, "输出查找roadsMap名称" + name);
                 //查找，没有被使用的轨迹名字,赋值
                 if (!DataStorage.roadsMap.containsKey(name)) {
                 Log.i(TAG, "输出查找到roadsMap名称" + name);
                 DataStorage.collectMapName = name;
                 break;
                 }
                 }
                 **/
                //道路属性设为不可点击，包括---清扫、回垃圾站、回车库、前进后退
                changeRoadAttributionEnable(false);

                //准备打开发送采集地图任务
                DataStorage.collectMode = 1; //采集地图模式开启
                collectingTagLayout.setVisibility(View.VISIBLE);
                //开始按钮不可点击
                startCollectBtn.setClickable(false);

                startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
                startCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
                endCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

                //记录选择区域Spinner的position
                int selectAreaSpinnerPosition = collectAreaSpinner.getSelectedItemPosition();
                DataStorage.collectAreaSpinnerPosition = selectAreaSpinnerPosition;
            }
        });

        endCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.collectMode == 1) {
                    //结束地图采集
                    DataStorage.collectMode = 2;
//                    DataStorage.collectMapSpeed = 0;
//                    speedBtn.setText(DataStorage.collectMapSpeed + "");
                } else {
                    //不是正在采集中，点击采集结束无效
                    return;
                }

                //相关按钮点击属性打开
                changeRoadAttributionEnable(true);

                collectingTagLayout.setVisibility(View.INVISIBLE);
                startCollectBtn.setClickable(true);

                endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
                endCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

                startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
                startCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

//                reloadRoads();   //重新加载地图
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(800); //800ms后请求加载地图
//                        refreshRoads();
                        handler.sendEmptyMessage(RELOAD_ROADS);
                    }
                }).start();

                ToastUtil.show(getApplicationContext(), "采集成功");
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
        DataStorage.cleanMapNameForSpinner(); //清除选择下拉框中的地图部分
        Global.loadRoadsFlag = true;
        Log.i(TAG, "重置完成");
//        for (Map.Entry<String, Polyline> en : aMapLineListMap.entrySet()) {
//            en.getValue().remove();
//        }


//        aMapLineListMap.clear(); //移除已经显示的地图轨迹
//        clearMarkers();//移除覆盖物


        //2秒后认为所有轨迹已经返回，开始重新设置下拉区域框
        SystemClock.sleep(2000);
        handler.sendEmptyMessage(UPDATE_SPINNER);
    }

    /**
     * UI扫描任务
     */
    private class ScanUITask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... paramStr) {
            while (true) {
                if (isCancelled()) {
                    break;
                }
                handler.sendEmptyMessage(UPDATE_BASE_UI);
                SystemClock.sleep(200);
            }
            return null;
        }
    }


    /**
     * 创建速度选择框
     */
    private void createSpeedPickerDialog() {
        AlertDialog builder;
        NumberPicker numberPicker;
        View speedLayout;
        builder = new AlertDialog.Builder(CollectRoadsActivity.this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "picker 确定");
                        DataStorage.collectMapSpeed = collectMapSpeedTemp;
                        speedBtn.setText(DataStorage.collectMapSpeed + "");
                        //速度选择radiobutton设为非选择
                        speedFastSelectRaBtn.setChecked(false);
                        speedSlowSelectRaBtn.setChecked(false);
                        speedNormalSelectRaBtn.setChecked(false);
                    }
                }).setNegativeButton("取消", null).setTitle("速度选择：").create();
        builder.setCancelable(false);
        speedLayout = LayoutInflater.from(CollectRoadsActivity.this).inflate(R.layout.speed_selector_dialog_view, null);

        builder.setView(speedLayout);

        numberPicker = (NumberPicker) speedLayout.findViewById(R.id.speed_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);
        numberPicker.setWrapSelectorWheel(false); //不循环显示
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
//                i --- oldValue   i1---newValue
                Log.i(TAG, "打印picker oldValue = " + oldValue);
                Log.i(TAG, "打印picker newValue = " + newValue);
                collectMapSpeedTemp = newValue;
//                i1是选中的数值
            }
        });
        builder.show();

    }

    /**
     * handler处理消息
     *
     * @param what
     */
    private void myHandleMessage(int what) {
        switch (what) {
            case UPDATE_BASE_UI:
                //车辆gps状态，更新速度
                if (DataStorage.rtk >= 4) {
//                    carStatusImage.setImageResource(R.drawable.normal);
                    carStatusImage.setImageResource(R.drawable.car_status_green);
                } else {
                    carStatusImage.setImageResource(R.drawable.car_status_default);
                }

                //电量
                String batNum = "";
                if (DataStorage.batterySoc < 10) {
                    batNum = "0" + DataStorage.batterySoc;
                } else {
                    batNum = batNum + DataStorage.batterySoc;
                }
                batteryText.setText(batNum + "%");

                Log.i(TAG, "UI更新");
                break;
            case UPDATE_SPINNER:
                //重新设置区域下拉框
                collectAreaAdapter = new ArrayAdapter(CollectRoadsActivity.this, android.R.layout.simple_spinner_item, DataStorage.getMapAreaListForCollectMap());
                //下拉风格
                collectAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                collectAreaSpinner.setAdapter(collectAreaAdapter);

                //设置区域为上次所选的
                //Spinner中position是从0开始的，0--1  1--2
                collectAreaSpinner.setSelection(DataStorage.collectAreaSpinnerPosition, true);
                break;
            case RELOAD_ROADS:
                refreshRoads();
                break;

        }
    }

    //
    public void changeRoadAttributionEnable(boolean collectComponentEnable) {
        if (DataStorage.collectPathProperty == 0) { //是清扫路段
            //开始清扫，方向自动设为前进
            if (!collectComponentEnable) forwardRadioBtn.performClick();
            sweepEnableImgBtn.setEnabled(true);
        } else {     //非清扫路段
            if (collectComponentEnable) {  // 结束采集
                sweepEnableImgBtn.setEnabled(true);
            } else { // 开始采集
                //清扫使能设为false
                sweepEnableImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.sweep_enable_false));
                DataStorage.collectCleanEnable = 0;
                sweepEnableImgBtn.setEnabled(false);
            }
        }

        forwardRadioBtn.setEnabled(collectComponentEnable);
        backRadioBtn.setEnabled(collectComponentEnable);
        pathDefaultRadioBtn.setEnabled(collectComponentEnable);
        pathDumpRadioBtn.setEnabled(collectComponentEnable);
        pathParkRadioBtn.setEnabled(collectComponentEnable);

    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
//        stopCollectMap();

    }


    @Override
    protected void onResume() {
        super.onResume();
//        reloadRoads();
        //地图采集模式 0--否  1---是   2---结束
        Log.i(TAG, "collectMap获取mode = " + DataStorage.collectMode);
        DataStorage.page = 2;

        //初始化模式
        collectingTagLayout.setVisibility(View.INVISIBLE);
        if (DataStorage.collectMode == 1) {  //正在采集中
            collectingTagLayout.setVisibility(View.VISIBLE);

            startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
            startCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

            endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
            endCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));

        } else if (DataStorage.collectMode == 2) {
            collectingTagLayout.setVisibility(View.INVISIBLE);
            endCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_chk));
            endCollectBtn.setTextColor(getResources().getColor(R.color.pureColorBackgroundWhite));

            startCollectBtn.setBackground(getResources().getDrawable(R.drawable.button_start_end_collect_btn_dft));
            startCollectBtn.setTextColor(getResources().getColor(R.color.noCheckColorGray));
        }

        //初始化速度
        collectMapSpeedTemp = DataStorage.collectMapSpeed;
        speedBtn.setText(collectMapSpeedTemp + "");

        //初始化道路有无效属性
        if (DataStorage.collectPathValid == 0) {
            pathValidImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.path_valid));
        } else {
            pathValidImgBtn.setImageDrawable(getResources().getDrawable(R.drawable.path_invalid));
        }

        //初始化道路属性
        switch (DataStorage.collectMapLaneType) {
            case 1:
                laneSingleBtn.performClick();
                break;
            case 2:
                roadsRightBtn.performClick();
                break;
            case 3:
                roadsMiddleBtn.performClick();
                break;
            case 4:
                roadsLeftBtn.performClick();
                break;
        }

        //初始化道路清扫属性
        switch (DataStorage.collectPathProperty) {
            case 0:
                pathDefaultRadioBtn.performClick();
                break;
            case 1:
                pathDumpRadioBtn.performClick();
                break;
            case 2:
                pathParkRadioBtn.performClick();
                break;
        }


        //初始化清倒的进出属性
        if (DataStorage.collectGetInOrOut == 0) {
            intOrOutBtn.setText("进站");
        } else {
            intOrOutBtn.setText("出站");
        }
        //初始化 道路方向属性
        switch (DataStorage.collectReverse) {
            case 0:
                forwardRadioBtn.performClick();
                break;
            case 1:
                backRadioBtn.performClick();
                break;
        }

        if (scanUITask != null && scanUITask.getStatus() == AsyncTask.Status.RUNNING) {
            scanUITask.cancel(true);
        }
        scanUITask = new ScanUITask();
        scanUITask.execute("");


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (scanUITask != null && scanUITask.getStatus() == AsyncTask.Status.RUNNING) {
            scanUITask.cancel(true);
        }
//        collectingTagLayout.setVisibility(View.INVISIBLE);
        //采集地图标志更新，不采集
//        DataStorage.collectMode = 0;
        finish();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        Log.i(TAG, "CollectRoadsActivity onDestroy");
        super.onDestroy();

    }

}
