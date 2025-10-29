package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.enjoy.wanji.R;
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
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;


public class CarScene extends Renderer {

    private Object3D carModel;


    List<Plane> lineList = new ArrayList<>();

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

    float carPositionX = 0, carPositionY = 0.7f, carPositionZ = -1.5f;
    float cameraX = 0, cameraY = 8f, cameraZ = 9f;
//    float cameraX = 13, cameraY = 1f, cameraZ = 5f;      //测试 侧面视角

    float lookAtX = 0, lookAtY = 0, lookAtZ = -6f;

    public CarScene(Context context){
        super(context);
    }

    @Override
    protected void initScene() {
        //设置背景颜色
        getCurrentScene().setBackgroundColor(0.92f,0.94f,0.96f, 1f);

        DirectionalLight keyLight = new DirectionalLight(1.5, -1.8f, -2.0f); // 方向向量
        keyLight.setPower(0.45f); // 光的强度
        keyLight.setColor(1.0f, 1.0f, 0.98f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(keyLight);

        DirectionalLight keyLight1 = new DirectionalLight(-1.5, -1.5f, -2.0f);
        keyLight1.setPower(0.30f);
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
        carModel = initCenterCarModel(R.raw.stl_main_car, colorAuroraSliverGrayArr);
        carModel.setScale(scale);
        carModel.setRotX(180);
        carModel.setRotZ(-90);

        carModel.setPosition(carPositionX, carPositionY, carPositionZ);
        getCurrentScene().addChild(carModel);

        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(cameraX, cameraY, cameraZ);
        getCurrentCamera().setLookAt(lookAtX, lookAtY, lookAtZ);

        initModelNPC(); //初始化 背景交通物体

    }

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
        //创建各类交通参与者，每类8个

        for (int i =0; i < 8; i++){
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
            Object3D pedestrianModel = initNoVehicle(R.raw.stl_man, colorColdWhiteArr);
            pedestrianModel.setVisible(false);
            pedestrianModel.setScale(1.8f);
            pedestrianModel.setPosition(0,0,50);
            ContainerObject3D.ModelWaite1PedestrianQueue.offer(pedestrianModel);
            getCurrentScene().addChild(pedestrianModel);

            //非机动车
            Object3D bikeModel = initNoVehicle(R.raw.stl_bike, colorColdWhiteArr);
            bikeModel.setVisible(false);
            bikeModel.setScale(1.8f);
            bikeModel.setPosition(0,0,50);
            ContainerObject3D.ModelWaite3NoVehicleQueue.offer(bikeModel);
            getCurrentScene().addChild(bikeModel);
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

}
