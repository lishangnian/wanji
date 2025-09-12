package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.enjoy.wanji.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
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
    private TranslateAnimation3D carAnimation;
    private boolean isAnimating = false;

    List<Plane> lineList = new ArrayList<>();
//    private Object3D ground;
    float[] colorGrayArr = {0.7216f, 0.7608f, 0.8000f, 1f}; //灰色
    float[] colorDarkGrayArr = {0.6627f, 0.6627f, 0.6627f, 1f}; //深灰色
    float[] colorDeepGrayArr = {0.35f, 0.35f, 0.35f, 1f}; //较深灰色
    float[] colorPearArr = {0.9922f, 0.9333f, 0.9569f, 1.0f};; //珠光白
    public CarScene(Context context){
        super(context);
    }

    @Override
    protected void initScene() {
        //设置背景颜色
//        getCurrentScene().setBackgroundColor(0.87f,0.87f,0.87f, 0.9f);
//        getCurrentScene().setBackgroundColor(1f,1f,1f, 0.7f);
        getCurrentScene().setBackgroundColor(0.98f,0.98f,0.98f, 0.8f);

        //基础光
        DirectionalLight ambientLight = new DirectionalLight(-1, 1, 1);
        ambientLight.setPower(0.2f); // 环境光强度通常较低
        getCurrentScene().addLight(ambientLight);


        //平行光
        DirectionalLight keyLight = new DirectionalLight(0, -1.8f, -2.0f); // 方向向量
        keyLight.setPosition(0, 100, 0); // 位置对平行光不重要，方向才重要
        keyLight.setPower(0.7f); // 光的强度
        keyLight.setColor(1.0f, 1.0f, 0.9f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(keyLight);

        // （可选）添加补光/填充光 - 减弱主光产生的阴影
        DirectionalLight fillLight = new DirectionalLight(1f, -0.5f, 0.5f);
        fillLight.setPower(0.3f);
        getCurrentScene().addLight(fillLight);

        addLaneLines(); //车道线

        //初始化本车
//        carModel = initVehicleModel3D(R.raw.car, colorDarkGrayArr);
        carModel = initCenterCarModel(R.raw.car, colorDeepGrayArr);
        carModel.setScale(0.08f);
        carModel.setPosition(0, 0, 1.2); //  z 正直 靠近观察者方向
        carModel.setRotY(180); // 调整朝向
        getCurrentScene().addChild(carModel);

        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(0, 2.1, 5.0);
        getCurrentCamera().setLookAt(0, 0, 0);


    }

    private Object3D initCenterCarModel(int resourceId, float[] colorARR){
        Object3D model = null;
        try {
            LoaderOBJ loader  = new LoaderOBJ(this, resourceId);
//            LoaderOBJ leftLoader = new LoaderOBJ(this,R.raw.car);
            loader.parse();   //解析模型
            model = loader.getParsedObject();

            if (model != null && model.getNumChildren() > 0){
                for(int i = 0; i < model.getNumChildren(); i++){
                    Object3D child = model.getChildAt(i);

                    // 设置车辆材质（如果没有纹理，使用默认材质）
                    Material material = new Material();
                    material.setColor(colorARR);
                    material.enableLighting(true);
                    material.setDiffuseMethod(new DiffuseMethod.Lambert());

                    // 设置镜面反射 - 实现光滑表面
                    SpecularMethod.Phong phong = new SpecularMethod.Phong();
                    material.setSpecularMethod(phong);
//                    material.setSpecularColor(0xFFFFFFFF); // 白色高光
//                    material.setShininess(256); // 高光泽度，值越大表面越光滑
                    // 启用颜色影响
                    material.setColorInfluence(1.0f);

                    child.setMaterial(material);
                }
            }
        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }

        return model;

    }

    private Object3D initVehicleModel3D(int resourceId, float[] colorARR){
        Object3D model = null;
        try {
            LoaderOBJ loader  = new LoaderOBJ(this, resourceId);
//            LoaderOBJ leftLoader = new LoaderOBJ(this,R.raw.car);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            updateCarModel(model, colorARR);

        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }
        return model;
    }

    //初始化交通参与者
    public void initModelNPC(){
        //创建未知物体  初始化三个
        for (int i =0; i < 3; i++){
            Object3D body = initVehicleModel3D(R.raw.car, colorPearArr);
            body.setScale(0.08f);
//        body.setPosition(-1.2, 0, -1.5); //
            body.setPosition(0,-100,0); //  初始位置把他放到地底下，看不见
            body.setRotY(180); // 调整朝向
            body.setVisible(false);  //设置不可见
            getCurrentScene().addChild(body);
            ContainerObject3D.ModelWaite2VehicleQueue.offer(body);
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

    private void addLaneLines() {
        // 创建车道线材质
        Material lineMaterial = new Material();
        lineMaterial.setColor(0x00bfff); // 蓝色线条
//        lineMaterial.setColor(0xFFFFFF); // 白色线条

        // 中心虚线
        for (int i = -20; i <= 20; i += 2) {
            Plane line = new Plane(0.08f, 0.8f, 1, 1);
            line.setMaterial(lineMaterial);
            line.setRotation(0,0,90);
            line.setY(-0.08f); // 稍微高于地面  z--向观察者
            line.setPosition(-0.6, 0f, i);
            getCurrentScene().addChild(line);


            Plane lineR = new Plane(0.08f, 0.8f, 1, 1);
            lineR.setMaterial(lineMaterial);
            lineR.setRotation(0,0,90);
            lineR.setY(-0.08f); // 稍微高于地面  z--向观察者
            lineR.setPosition(0.6, 0f, i);
            getCurrentScene().addChild(lineR);
            lineList.add(line);
            lineList.add(lineR);
        }

        // 车道边界线
//        addSolidLine(3.5f);  // 右边线
//        addSolidLine(-3.5f); // 左边线
    }

    public void updateLinesMove(double z){
        for (Plane line: lineList){
            Vector3 v = line.getPosition();
            v.z = v.z + z;
            if (v.z > 20){
                v.z = -20;
            }
            line.setPosition(v);
        }
    }

    private void createCarModel(){
        float[] floatArr = {0.2f,0.6f,0.9f};

        //创建立方体代替车
        Cube carBody = new Cube(1.5f,true,true);
        Material material = new Material();
        material.setColor(floatArr);
        material.enableLighting(true);
        carBody.setMaterial(material);
        carBody.setPosition(0, -1 ,-4);
        getCurrentScene().addChild(carBody);

        //添加轮子
        Cube wheel1 = new Cube(0.5f,true,true);
        wheel1.setMaterial(material);
        wheel1.setPosition(-0.8f, -1.4f, -2.5f);
        getCurrentScene().addChild(wheel1);

        Cube wheel2 = new Cube(0.5f,true,true);
        wheel2.setMaterial(material);
        wheel2.setPosition(0.8f, -1.4f, -2.5f);
        getCurrentScene().addChild(wheel2);

        Cube wheel3 = new Cube(0.5f,true,true);
        wheel3.setMaterial(material);
        wheel3.setPosition(-0.8f, -1.4f, 0.5f);
        getCurrentScene().addChild(wheel3);

        Cube wheel4 = new Cube(0.5f,true,true);
        wheel4.setMaterial(material);
        wheel4.setPosition(0.8f, -1.4f, -2.5f);
        getCurrentScene().addChild(wheel4);

        carModel = carBody;

    }

    private void updateCarModel(Object3D model3D, float[] colorARR){
        if (model3D != null && model3D.getNumChildren() > 0){
            for(int i = 0; i < model3D.getNumChildren(); i++){
                Object3D child = model3D.getChildAt(i);

                // 设置车辆材质（如果没有纹理，使用默认材质）
                Material material = new Material();
                material.setColor(colorARR);
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());
                child.setMaterial(material);
            }
        }
    }

    /**
     * 测试移动车辆
     */
    private void createCarAnimation() {
        // 创建车辆前后移动的动画
        carAnimation = new TranslateAnimation3D(
                new Vector3(-1.2, 0, -24), // 起始位置
                new Vector3(-1.2, 10, 24)   // 结束位置
        );
        carAnimation.setDurationMilliseconds(4000);
        carAnimation.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        carAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
//        carAnimation.setTransformable3D(leftModel);
        Log.i("objTag","car animation created");
    }

    public void startCarAnimation() {
        boolean ss  = carAnimation == null;
        Log.i("objTag","animation:"+ ss +", isAnimating:"+ isAnimating);


        if (carAnimation != null && !isAnimating) {
            carAnimation.play();
            isAnimating = true;
            Log.i("objTag","car animation start play");
        }
    }

    // 测试模型移动
//    public void startMoveLeft(){
//        Vector3 vector3 = leftModel.getPosition();
//        Log.i("objTag","start to move left z = :" + vector3.z);
//        double z = vector3.z +0.1;
//        if (z > 1.5){
//            z = -20;
//        }
//        vector3.z = z;
//        leftModel.setPosition(vector3);
//    }

    public void stopCarAnimation() {
        if (carAnimation != null && isAnimating) {
            carAnimation.pause();
            isAnimating = false;
        }
    }

    public void resetCarPosition() {
//        if (carModel != null) {
//            carModel.setPosition(0, -1, -4);
//        }

//        if (leftModel != null) {
//            carModel.setPosition(-1.2, 0, -24);
//        }
    }
}
