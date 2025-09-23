package com.enjoy.wanji.vr3D;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.enjoy.wanji.R;
import com.enjoy.wanji.entity.DataStorageFromPC;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
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
    Line3D leftCurveLine3D, rightCurveLine3D; //曲线
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


        addLaneLines(); //车道线

//        drawLines(); //实时车道线
//        myDraw();


        //初始化本车
        carModel = initCenterCarModel(R.raw.obj_car, colorLightGrayArr);
//        carModel = initCenterCarModel(R.raw.car1, colorLightGrayArr);
        carModel.setScale(0.25f);
        carModel.setPosition(0, 0, 1.2); //  z 正直 靠近观察者方向
        carModel.setRotY(180); // 调整朝向
        getCurrentScene().addChild(carModel);


        // 设置摄像机位置（固定）  x-右  y-高  z-纵深 靠近观察者为正
        getCurrentCamera().setPosition(0, 8, 13);
//        getCurrentCamera().setPosition(0, 25, 5);
        getCurrentCamera().setLookAt(0, 0, -3);


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
            model.setScale(0.25f);
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
//        unknowModel.setScale(0.5);
        updateModelMaterial(unknowModel, colorPearArr);
        return unknowModel;
    }

    /**
     * 初始化非机动车
     * @return
     */
    private Object3D initNoVehicle(int resourceId ){
        Object3D model = null;
        try {
            LoaderSTL loader = new LoaderSTL(mContext.getResources(),mTextureManager, resourceId);
            loader.parse();   //解析模型
            model = loader.getParsedObject();
            updateModelMaterial(model,colorPearArr);

        }catch (ParsingException pe){
            Log.e("objTag","parsing carObj error:",pe.fillInStackTrace());
        }
        return model;
    }

    //初始化交通参与者
    public void initModelNPC(){
        //创建未知物体-3个 机动车3个

        for (int i =0; i < 3; i++){
            // 机动车
            Object3D body = initVehicleModel3D(R.raw.obj_car, colorPearArr);
            body.setVisible(false);  //设置不可见
            getCurrentScene().addChild(body);
            ContainerObject3D.ModelWaite2VehicleQueue.offer(body);

            //未知
            Object3D unknowModel = intiUnknowModel();
            unknowModel.setVisible(false);
            getCurrentScene().addChild(unknowModel);
            ContainerObject3D.ModelWaite0UnknownQueue.offer(unknowModel);


            //行人
            Object3D nonVehicleModel = initNoVehicle(R.raw.person);
            nonVehicleModel.setVisible(false);
            nonVehicleModel.setScale(2.5f);
            nonVehicleModel.setPosition(0,0,50);
            nonVehicleModel.setRotY(250); // 调整朝向
            nonVehicleModel.setRotZ(10);
            nonVehicleModel.setRotX(-40);  //左右转
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

    int startPoint = -60, endPoint = 6;
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
