package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.enjoy.wanji.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

public class CarScene extends Renderer {

    private Object3D carModel, leftModel, rightModel;
    private Animation3D carAnimation;
    private boolean isAnimating = false;
//    private Object3D ground;

    public CarScene(Context context){
        super(context);
    }

    @Override
    protected void initScene() {
        //设置背景颜色
//        getCurrentScene().setBackgroundColor(0.87f,0.87f,0.87f, 0.9f);
        getCurrentScene().setBackgroundColor(1f,1f,1f, 0.7f);

        //添加灯光
//        DirectionalLight directionalLight = new DirectionalLight(1,-1,-1);
//        directionalLight.setColor(1,1,1);
//        directionalLight.setPower(1.0f);
//        getCurrentScene().addLight(directionalLight);
        //基础光
        DirectionalLight ambientLight = new DirectionalLight(1, 1, 1);
        ambientLight.setPower(0.2f); // 环境光强度通常较低
        getCurrentScene().addLight(ambientLight);

        //定向光源
//        DirectionalLight fillLight = new DirectionalLight(-1,-1,1);
//        fillLight.setColor(0.5f,0.5f,0.5f);
//        fillLight.setPower(0.8f);
//        getCurrentScene().addLight(fillLight);

        //平行光
        DirectionalLight keyLight = new DirectionalLight(0, -5f, -2.0f); // 方向向量
        keyLight.setPosition(0, 100, 0); // 位置对平行光不重要，方向才重要
        keyLight.setPower(0.6f); // 光的强度
        keyLight.setColor(1.0f, 1.0f, 0.9f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(keyLight);

        // （可选）添加补光/填充光 - 减弱主光产生的阴影
        DirectionalLight fillLight = new DirectionalLight(-0.5f, -0.5f, 0.5f);
        fillLight.setPower(0.3f);
        getCurrentScene().addLight(fillLight);


        addLaneLines(); //车道线


        try {
            LoaderOBJ loader  = new LoaderOBJ(this, R.raw.car);
            LoaderOBJ leftLoader = new LoaderOBJ(this,R.raw.car);
            LoaderOBJ rightLoader = new LoaderOBJ(this,R.raw.car);
            loader.parse();   //解析模型
            leftLoader.parse();
            rightLoader.parse();
            carModel = loader.getParsedObject();
            leftModel = leftLoader.getParsedObject();
            rightModel = rightLoader.getParsedObject();

            float[] colorArr = {0.7216f, 0.7608f, 0.8000f, 0.9f}; //灰色
            float[] colorARR = {0.9922f, 0.9333f, 0.9569f, 1.0f}; //珠光白
//            float[] colorARR = {1f, 0f, 0f, 1.0f}; //红色
            updateCarModel(carModel, colorARR);
            updateCarModel(leftModel, colorArr);
            updateCarModel(rightModel, colorArr);


//            carModel.setMaterial(material);
//            carModel.setColor(R.color.grey);

            // 调整车辆大小和位置
            carModel.setScale(0.08f);
            carModel.setPosition(0, 0, 1.2); //  z 正直 靠近观察者方向
            carModel.setRotY(180); // 调整朝向

            leftModel.setScale(0.08f);
            leftModel.setPosition(-1.2, 0, -1.5); //
            leftModel.setRotY(180); // 调整朝向

            rightModel.setScale(0.08f);
            rightModel.setPosition(1.2, 0, -2.8);
            rightModel.setRotY(180); // 调整朝向

            getCurrentScene().addChild(carModel);
            getCurrentScene().addChild(leftModel);
            getCurrentScene().addChild(rightModel);

        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }

        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(0, 2.1, 4.6);
        getCurrentCamera().setLookAt(0, 0, 0);

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

    private Object3D createRoadWithLines() {
        // 创建地面
        Plane ground = new Plane(20.0f, 20.0f, 1, 1);
        Material groundMaterial = new Material();
        groundMaterial.setColor(0xF0FFFF); // 深灰色地面   ADD8E6
        ground.setMaterial(groundMaterial);
//        ground.setRotX(-90);
        ground.setRotY(-90);
//        ground.setRotZ(180);
        ground.setY(-1);

        // 添加车道线
        addLaneLines();

        return ground;
    }


    private void addLaneLines() {
        // 创建车道线材质
        Material lineMaterial = new Material();
        lineMaterial.setColor(0x00bfff); // 蓝色线条
//        lineMaterial.setColor(0xFFFFFFFF); // 白色线条

        // 中心虚线
        for (int i = -28; i <= 28; i += 2) {
            Plane line = new Plane(0.08f, 0.8f, 1, 1);
            line.setMaterial(lineMaterial);
            line.setRotation(0,0,90);
            line.setY(-0.1f); // 稍微高于地面  z--向观察者
            line.setPosition(-0.6, 0f, i);
            getCurrentScene().addChild(line);


            Plane lineR = new Plane(0.08f, 0.8f, 1, 1);
            lineR.setMaterial(lineMaterial);
//            line.setRotX(-90);
            lineR.setRotation(0,0,90);
            lineR.setY(-0.1f); // 稍微高于地面  z--向观察者
            lineR.setPosition(0.6, 0f, i);
            getCurrentScene().addChild(lineR);
        }

        // 车道边界线
//        addSolidLine(3.5f);  // 右边线
//        addSolidLine(-3.5f); // 左边线
    }

    private void addSolidLine(float xPosition) {
        Material lineMaterial = new Material();
        lineMaterial.setColor(0xFFFFFFFF);

        Plane line = new Plane(0.1f, 20.0f, 1, 1);
        line.setMaterial(lineMaterial);
//        line.setRotX(-90);
        line.setY(-0.9f);
        line.setPosition(xPosition, -0.9f, 0);
        getCurrentScene().addChild(line);
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
                material.setColor(colorARR); // 灰色
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
        // 创建车辆左右移动的动画
        carAnimation = new TranslateAnimation3D(
                new Vector3(-3, -1, -4), // 起始位置
                new Vector3(3, -1, -4)   // 结束位置
        );
        carAnimation.setDurationMilliseconds(4000);
        carAnimation.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        carAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        carAnimation.setTransformable3D(carModel);
    }

    public void startCarAnimation() {
        if (carAnimation != null && !isAnimating) {
            carAnimation.play();
            isAnimating = true;
        }
    }

    public void stopCarAnimation() {
        if (carAnimation != null && isAnimating) {
            carAnimation.pause();
            isAnimating = false;
        }
    }

    public void resetCarPosition() {
        if (carModel != null) {
            carModel.setPosition(0, -1, -4);
        }
    }
}
