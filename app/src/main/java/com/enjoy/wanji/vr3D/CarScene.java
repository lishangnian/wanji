package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.enjoy.wanji.R;
import com.enjoy.wanji.entity.DataStorageFromPC;
import com.enjoy.wanji.service.EnjoySocketService;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CarScene extends Renderer {

    private Object3D carModel;


    List<Plane> lineList = new ArrayList<>();
    Line3D leftCurveLine3D, rightCurveLine3D; //曲线
//    private Object3D ground;
    float[] colorGrayArr = {0.7216f, 0.7608f, 0.8000f, 1f}; //灰色
    float[] colorLightGrayArr = {0.835f, 0.835f, 0.835f, 1f}; //灰色
    float[] colorSliverGrayArr = {0.85f, 0.85f, 0.87f, 1f}; //浅银灰色
    float[] colorAuroraSliverGrayArr = {0.92f, 0.92f, 0.94f, 1f}; //极光银
    float[] colorScienceGrayArr = {0.88f, 0.88f, 0.90f, 1f}; //科技灰色
    float[] colorDarkGrayArr = {0.6627f, 0.6627f, 0.6627f, 1f}; //深灰色
    float[] colorDeepGrayArr = {0.35f, 0.35f, 0.35f, 1f}; //较深灰色
    float[] colorPearArr = {0.9922f, 0.9333f, 0.9569f, 1.0f};; //珠光白

//    float[] colorLightPearArr = {0.98f, 0.96f, 0.90f, 1.0f};; //标准乳白色
    float[] colorColdWhiteArr = {0.85f, 0.90f, 1.00f, 1.0f};; //冷白色

    float carPositionX = 0, carPositionY = 0.9f, carPositionZ = -1.5f;
    float cameraX = 0, cameraY = 8f, cameraZ = 9f;
//    float cameraX = 15, cameraY = 1f, cameraZ = 5f;      //测试 侧面视角

    float lookAtX = 0, lookAtY = 0, lookAtZ = -6f;

    public CarScene(Context context){
        super(context);
    }

    @Override
    protected void initScene() {
        //设置背景颜色
//        getCurrentScene().setBackgroundColor(0.98f,0.98f,0.98f, 1f);
//        getCurrentScene().setBackgroundColor(0.10f,0.10f,0.12f, 1f);//深灰色
        getCurrentScene().setBackgroundColor(0.92f,0.94f,0.96f, 1f);


        DirectionalLight keyLight = new DirectionalLight(1.5, -1.8f, -2.0f); // 方向向量
        keyLight.setPower(0.45f); // 光的强度
        keyLight.setColor(1.0f, 1.0f, 0.98f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(keyLight);

        DirectionalLight keyLight1 = new DirectionalLight(-1.5, -1.5f, -2.0f);
        keyLight1.setPower(0.35f);
        keyLight1.setColor(1.0f, 1.0f, 0.98f);
        getCurrentScene().addLight(keyLight1);

        DirectionalLight light = new DirectionalLight(-1.5, 1.8f, 1.5f);
        light.setPower(0.25f); // 光的强度
        light.setColor(1.0f, 1.0f, 0.98f);
        getCurrentScene().addLight(light);

        DirectionalLight light1 = new DirectionalLight(1.5, 1.8f, 1.5f);
        light1.setPower(0.20f);
        light1.setColor(1.0f, 1.0f, 0.98f);
        getCurrentScene().addLight(light1);

        addLaneLines(); //车道线

        float scale = 3.2f;
//        carModel = initMainCarModel(R.raw.obj_main_car);
        carModel = initCenterCarModel(R.raw.stl_main_car, colorAuroraSliverGrayArr);
//        carModel = initCenterCarModel(R.raw.stl_main_car, colorGrayArr);
        carModel.setScale(scale);
        carModel.setRotX(180);
        carModel.setRotZ(-90);

        carModel.setPosition(carPositionX, carPositionY, carPositionZ);
        getCurrentScene().addChild(carModel);

        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(cameraX, cameraY, cameraZ);
//        getCurrentCamera().setPosition(0, 25, 5);  //俯视
        getCurrentCamera().setLookAt(lookAtX, lookAtY, lookAtZ);


        initModelNPC(); //初始化 背景交通物体

    }

    /**
     *
     * @param resourceId
     * @return
     */
    /**
     *


    private Object3D initMainCarModel(int resourceId){
        Object3D model = null;
        try {
            InputStream objInputStream = mContext.getAssets().open("model/obj_main_car.obj");
//            LoaderOBJ loader  = new LoaderOBJ(this, resourceId);
//            LoaderOBJ loader  = new LoaderOBJ(mContext.getResources(), getTextureManager(), resourceId);
//            LoaderOBJ loader = new LoaderOBJ(mContext.getResources(),getTextureManager(),objInputStream);
//            loader.parse();   //解析模型
//            model = loader.getParsedObject();



        }catch (ParsingException | IOException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }

        return model;
    }
     */

    private Object3D initCenterCarModel(int resourceId, float[] colorARR){
        Object3D model = null;
        try {
//            LoaderOBJ loader  = new LoaderOBJ(this, resourceId);
            LoaderSTL loader = new LoaderSTL(mContext.getResources(), mTextureManager, resourceId);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            // 设置车辆材质（如果没有纹理，使用默认材质）
            Material material = new Material();
            material.setColor(colorARR);
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());

            // 设置镜面反射 - 实现光滑表面
            material.setSpecularMethod(new SpecularMethod.Phong(Color.WHITE, 100));

            // 启用颜色影响
            material.setColorInfluence(0.97f);
            if (model != null && model.getNumChildren() > 0){
                for(int i = 0; i < model.getNumChildren(); i++){
                    Object3D child = model.getChildAt(i);
                    Log.i("main tag","num child >0 ");
                    child.setMaterial(material);
                }
            }else if (model != null){
                model.setMaterial(material);
            }
        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }

        return model;

    }

    /**
     * 初始化机动车模型
     * @param resourceId
     * @param colorARR
     * @return
     */
    private Object3D initVehicleModel3D(int resourceId, float[] colorARR){
        Object3D model = null;
        try {
            LoaderOBJ loader  = new LoaderOBJ(this, resourceId);
//            LoaderSTL loader = new LoaderSTL(mContext.getResources(),mTextureManager,resourceId);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            model.setScale(3.8f);
            model.setRotY(180);
            model.setPosition(0,-100,0);
            updateModelMaterial(model, colorARR);

        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }
        return model;
    }

    /**
     * 初始化未知物模型
     * @return
     */
    private Object3D intiUnknowModel(){
        Object3D unknowModel = new Cube(1,true,true);
        unknowModel.setPosition(0,0.5,-1);
//        unknowModel.setScale(0.5);
        updateModelMaterial(unknowModel, colorColdWhiteArr);
        return unknowModel;
    }

    /**
     * 初始化非机动车
     * @return
     */
    private Object3D initNoVehicle(int resourceId, float[] colorArr){
        Object3D model = null;
        try {
            LoaderSTL loader = new LoaderSTL(mContext.getResources(),mTextureManager, resourceId);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            updateModelMaterial(model,colorArr);

        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }
        return model;
    }

    public void initModelNPC(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (EnjoySocketService.UpdateUIModelLock.tryLock()) {
                    try {
                        initModelNPC_Imp();
                    } finally {
                        EnjoySocketService.UpdateUIModelLock.unlock();
                    }
                } else {
                    Log.i("msgTag", "Lock not init 3D NPC: 未获取锁");
                }
            }
        });
        t.start();

    }


    //初始化交通参与者
    private void initModelNPC_Imp(){
        //创建未知物体-3个 机动车3个

        for (int i =0; i < 3; i++){
            // 机动车
            Object3D body = initVehicleModel3D(R.raw.obj_bg_car, colorColdWhiteArr);
            body.setVisible(false);  //设置不可见
            getCurrentScene().addChild(body);
            ContainerObject3D.ModelWaite2VehicleQueue.offer(body);

            //未知
            Object3D unknowModel = intiUnknowModel();
            unknowModel.setVisible(false);
            getCurrentScene().addChild(unknowModel);
            ContainerObject3D.ModelWaite0UnknownQueue.offer(unknowModel);


            //行人
            Object3D nonVehicleModel = initNoVehicle(R.raw.stl_man, colorColdWhiteArr);
            nonVehicleModel.setVisible(false);
            nonVehicleModel.setScale(1.8f);
            nonVehicleModel.setPosition(0,0,50);
            ContainerObject3D.ModelWaite1PedestrianQueue.offer(nonVehicleModel);
            getCurrentScene().addChild(nonVehicleModel);
        }

    }




    @Override
    protected void onRender(long elapsedTime, double deltaTime){
        super.onRender(elapsedTime, deltaTime);
        //添加每帧更新的逻辑


    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    int startPoint = -48, endPoint = 0;
    private void addLaneLines() {
        // 创建车道线材质
        Material lineMaterial = new Material();
        lineMaterial.setColor(0x00bfff); // 蓝色线条
//        lineMaterial.setColor(0xFFFFFF); // 白色线条
        float lineLength = 4f, lineWith = 0.15f;

        // 中心虚线
        for (int i = startPoint; i <= endPoint; i += 6) {
            Plane line = new Plane(lineWith, lineLength, 1, 1);
            line.setMaterial(lineMaterial);
            line.setRotation(0,0,90);
            line.setY(-0.08f); // 稍微高于地面  z--向观察者
            line.setPosition(-1.7, 0f, i);
            getCurrentScene().addChild(line);


            Plane lineR = new Plane(lineWith, lineLength, 1, 1);
            lineR.setMaterial(lineMaterial);
            lineR.setRotation(0,0,90);
            lineR.setY(-0.08f); // 稍微高于地面  z--向观察者
            lineR.setPosition(1.7, 0f, i);
            getCurrentScene().addChild(lineR);
            lineList.add(line);
            lineList.add(lineR);
        }
    }


    public void updateLinesMove(double z){
        //轨迹是曲线转弯，隐藏直线
        if (DataStorageFromPC.CurveA !=0){
            for (Plane line: lineList){
                if (line.isVisible()){
                    line.setVisible(false);
                }
            }
            drawLines(); //绘制曲线
            return;
        }

        //曲线隐藏
        if (null != leftCurveLine3D && leftCurveLine3D.isVisible()){
            leftCurveLine3D.setVisible(false);
        }
        if (null != rightCurveLine3D && rightCurveLine3D.isVisible()){
            rightCurveLine3D.setVisible(false);
        }

        for (Plane line: lineList){
            Vector3 v = line.getPosition();
            v.z = v.z + z;
            if (v.z >= endPoint){
                v.z = startPoint;
            }
            if (!line.isVisible()){
                line.setVisible(true);
            }

            line.setPosition(v);
        }

    }

    private void updateModelMaterial(Object3D model3D, float[] colorARR){
        // 设置车辆材质（如果没有纹理，使用默认材质）
//        Material material = new Material();
//        material.setColor(colorARR);
//        material.enableLighting(true);
//        material.setDiffuseMethod(new DiffuseMethod.Lambert());



        Material material = new Material();
        material.setColor(colorARR);
        material.enableLighting(true);
        material.setAmbientColor(colorARR);
        material.setAmbientIntensity(0.4,0.4,0.4);

        material.setDiffuseMethod(new DiffuseMethod.Lambert());



        if (model3D != null && model3D.getNumChildren() > 0){
            for(int i = 0; i < model3D.getNumChildren(); i++){
                Object3D child = model3D.getChildAt(i);
                child.setMaterial(material);
            }
        }else if (model3D != null){
            model3D.setMaterial(material);
        }
    }


    static double A, B, C;
    float thickness = 8f;
    static float CURVE_OFFSET_X = 1.5f;   //x偏移量
    final int NUM_PLANES = 60; // 使用的平面点个数
    final float RANGE = 6.0f; // x轴范围

    double startY = 0.5;
    /**
     *
     */
    private void drawLines(){
//        Log.i("lineTag","A="+A +", cA="+ DataStorageFromPC.CurveA
//        +", B="+B+", cB="+DataStorageFromPC.CurveB
//        +", C="+C +", cC="+DataStorageFromPC.CurveC);
        if (A == DataStorageFromPC.CurveA
                && B == DataStorageFromPC.CurveB
                && C == DataStorageFromPC.CurveC){
            //与上次曲线一样，直接显示不用再绘制
            if (leftCurveLine3D != null && !leftCurveLine3D.isVisible()){
                leftCurveLine3D.setVisible(true);
            }
            if (rightCurveLine3D != null && !leftCurveLine3D.isVisible()){
                rightCurveLine3D.setVisible(true);
            }
            return;
        }
        A = DataStorageFromPC.CurveA;
        B = DataStorageFromPC.CurveB;
        C = DataStorageFromPC.CurveC;



        Material material = new Material();
        material.setColor(0x00bfff);
//        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        Stack<Vector3> lStack = new Stack<>();
        Stack<Vector3> rStack = new Stack<>();
        List<Vector3> lPointsList = new ArrayList<>();
        List<Vector3> rPointsList = new ArrayList<>();
        float  x;
        // 创建多个平面形成二次曲线
        for (int i = 0; i < NUM_PLANES; i++){
            // 计算x坐标
             x = -RANGE + (2 * RANGE * i / (NUM_PLANES - 1));

            // 计算二次函数y值
            float leftX = x + CURVE_OFFSET_X;   //左平移
            float rightX = x - CURVE_OFFSET_X;  //右平移

        //   中轴为        float xMedian = B/2A;
//            Log.i("lineTag","lx ="+ leftX +" ,x="+x+", rx="+ rightX);

            double leftY = -A * leftX * leftX + B * leftX + C;
            double rightY = -A * rightX * rightX + B * rightX + C;

//            Vector3 lV = new Vector3(x, 0, leftY);
//            Vector3 rV = new Vector3(x, 0, rightY);
//            lPointsList.add(lV);
//            rPointsList.add(rV);


            if (B > 0){
                if (leftY < startY && x > (B/(2*A) - CURVE_OFFSET_X) ){  //只取车头前的轨迹，只要抛物线的右边部分 中线为B/2-CURVE_OFFSET_X
                    Vector3 lV = new Vector3(x, 0, leftY);
                    lPointsList.add(lV);
                }
                if (rightY < startY && x > (B/(2*A) + CURVE_OFFSET_X)){
                    Vector3 rV = new Vector3(x, 0, rightY);
                    rPointsList.add(rV);
                }
            }else if (B < 0){
                if (leftY < startY && x < (B/(2*A) - CURVE_OFFSET_X) ){  //只取车头前的轨迹，只要抛物线的左边部分 中线为B/2-CURVE_OFFSET_X
                    Vector3 lV = new Vector3(x, 0, leftY);
                    lPointsList.add(lV);
                }
//                Log.i("lineTag",", x="+x+", rightY="+rightY);
                if (rightY < startY && x < (B/(2*A) + CURVE_OFFSET_X)){
//                    Log.i("lineTag","x="+x+", rightY="+rightY +"is OK\n");
                    Vector3 rV = new Vector3(x, 0, rightY);
                    rPointsList.add(rV);
                }
            }


        }
        lStack.addAll(lPointsList);
        rStack.addAll(rPointsList);
        if (leftCurveLine3D != null){
            getCurrentScene().removeChild(leftCurveLine3D);
        }
        if (rightCurveLine3D != null){
            getCurrentScene().removeChild(rightCurveLine3D);
        }

        leftCurveLine3D = new Line3D(lStack,thickness, 0x00bfff); //thickness为线宽，单位是像素
        rightCurveLine3D = new Line3D(rStack,thickness, 0x00bfff);
        leftCurveLine3D.setMaterial(material);
        rightCurveLine3D.setMaterial(material);

        // 将平面添加到场景中
        getCurrentScene().addChild(leftCurveLine3D);
        getCurrentScene().addChild(rightCurveLine3D);

    }

}
