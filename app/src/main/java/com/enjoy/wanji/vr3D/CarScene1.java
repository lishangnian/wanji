package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.enjoy.wanji.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CarScene1 extends Renderer {

    private Object3D carModel;


    List<Plane> lineList = new ArrayList<>();
//    private Object3D ground;
    float[] colorGrayArr = {0.7216f, 0.7608f, 0.8000f, 1f}; //灰色
    float[] colorLightGrayArr = {0.835f, 0.835f, 0.835f, 1f}; //灰色
    float[] colorDarkGrayArr = {0.6627f, 0.6627f, 0.6627f, 1f}; //深灰色
    float[] colorDeepGrayArr = {0.35f, 0.35f, 0.35f, 1f}; //较深灰色
    float[] colorPearArr = {0.9922f, 0.9333f, 0.9569f, 1.0f};; //珠光白
    public CarScene1(Context context){
        super(context);
    }

    @Override
    protected void initScene() {
        //设置背景颜色
        getCurrentScene().setBackgroundColor(0.98f,0.98f,0.98f, 0.1f);


        //平行光  从右边x   从上边来
        DirectionalLight keyLight = new DirectionalLight(1.5, -1.8f, -2.0f); // 方向向量
        keyLight.setPower(0.7f); // 光的强度
        keyLight.setColor(1.0f, 1.0f, 0.9f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(keyLight);
        //从左边  从上边
        DirectionalLight light = new DirectionalLight(-1.5, 1.8f, 1.5f); // 方向向量
        light.setPower(0.48f); // 光的强度
        light.setColor(1.0f, 1.0f, 0.9f); // 可选：设置光的颜色（略偏暖黄）
        getCurrentScene().addLight(light);


        // （可选）添加补光/填充光 - 减弱主光产生的阴影
        DirectionalLight fillLight = new DirectionalLight(-2.5f, -1.5f, 3f);
        fillLight.setPower(0.3f);
        fillLight.setColor(1f,1f,1f);
        getCurrentScene().addLight(fillLight);


//        addLaneLines(); //车道线

        drawLines(); //实时车道线
//        myDraw();


        //初始化本车
        carModel = initCenterCarModel(R.raw.car, colorLightGrayArr);
//        carModel = initCenterCarModel(R.raw.car, colorDeepGrayArr);
        carModel.setScale(0.08f);
        carModel.setPosition(0, 0, 1.2); //  z 正直 靠近观察者方向
        carModel.setRotY(180); // 调整朝向
        getCurrentScene().addChild(carModel);


        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(0, 3, 6.0);
//        getCurrentCamera().setPosition(0, 5, 0.5);
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
//                    SpecularMethod.Phong phong = new SpecularMethod.Phong();
////                    phong.setSpecularColor();     //高光颜色
//                    phong.setShininess(128);  //高光强度   值越大 反光点越小
//                    material.setSpecularMethod(phong);

                    // 启用颜色影响
                    material.setColorInfluence(0.92f);

                    child.setMaterial(material);
                }
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
//            LoaderOBJ leftLoader = new LoaderOBJ(this,R.raw.car);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            model.setScale(0.08f);
//        body.setPosition(-1.2, 0, -1.5); //
            model.setPosition(0,-100,0); //  初始位置把他放到地底下，看不见
            model.setRotY(180); // 调整朝向

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
        unknowModel.setScale(0.5);
        updateModelMaterial(unknowModel, colorPearArr);
        return unknowModel;
    }


    //初始化交通参与者
    public void initModelNPC(){
        //创建未知物体-3个 机动车3个

        for (int i =0; i < 3; i++){
            Object3D body = initVehicleModel3D(R.raw.car, colorPearArr);
            body.setVisible(false);  //设置不可见
            getCurrentScene().addChild(body);
            ContainerObject3D.ModelWaite2VehicleQueue.offer(body);

            Object3D unknowModel = intiUnknowModel();
            unknowModel.setVisible(false);
            getCurrentScene().addChild(unknowModel);
            ContainerObject3D.ModelWaite0UnknownQueue.offer(unknowModel);


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

    int startPoint = -16, endPoint = 4;
    private void addLaneLines() {
        // 创建车道线材质
        Material lineMaterial = new Material();
        lineMaterial.setColor(0x00bfff); // 蓝色线条
//        lineMaterial.setColor(0xFFFFFF); // 白色线条
        float lineLength = 1.2f, lineWith = 0.08f;

        // 中心虚线
        for (int i = startPoint; i <= endPoint; i += 2) {
            Plane line = new Plane(lineWith, lineLength, 1, 1);
            line.setMaterial(lineMaterial);
            line.setRotation(0,0,90);
            line.setY(-0.08f); // 稍微高于地面  z--向观察者
            line.setPosition(-0.6, 0f, i);
            getCurrentScene().addChild(line);


            Plane lineR = new Plane(lineWith, lineLength, 1, 1);
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
            if (v.z >= endPoint){
                v.z = startPoint;
            }
            line.setPosition(v);
        }

    }

    private void updateModelMaterial(Object3D model3D, float[] colorARR){
        // 设置车辆材质（如果没有纹理，使用默认材质）
        Material material = new Material();
        material.setColor(colorARR);
        material.enableLighting(true);
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


    /**
     *
     */
    private void drawLines(){
        final int NUM_PLANES = 50; // 使用的平面点个数
        final float RANGE = 5.0f; // x轴范围

        // 二次函数参数: y = a*x^2 + b*x + c
        final float A = -2f;
        final float B = 0f;
        final float C = 0f;



        Material material = new Material();
//        material.enableLighting(true);
        material.setColor(0x00bfff);
//        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        Stack<Vector3> stack = new Stack<>();
        List<Vector3> pointsList = new ArrayList<>();
        float  x = 0;
        // 创建多个平面形成二次曲线
        for (int i = 0; i < NUM_PLANES; i++){
            // 计算x坐标
             x = -RANGE + (2 * RANGE * i / (NUM_PLANES - 1));

            // 计算二次函数y值
            float y = A * x * x + B * x + C;

//                Math.
            Vector3 v = new Vector3(x, 0, y);
            pointsList.add(v);

        }
        //            Line3D line3D = new Line3D(points,1.5f, 0x00bfff);
        stack.addAll(pointsList);
        Line3D line3D = new Line3D(stack,5f, 0x00bfff); //thickness为线宽，单位是像素
        line3D.setMaterial(material);

        // 将平面添加到场景中
        getCurrentScene().addChild(line3D);

    }

}
