package com.enjoy.wanji.vr3D;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.primitives.Cylinder;
import org.rajawali3d.primitives.Sphere;

public class HumanModel extends Object3D {

    float[] colorPearArr = {0.9922f, 0.9333f, 0.9569f, 1.0f};; //珠光白
    public HumanModel() {
        super("HumanModel"); // 给模型命名

        // 创建并添加所有身体部位
        createHead();
        createTorso();
        createArms();
        createLegs();
    }

    private void createHead() {
        Sphere head = new Sphere(0.8f, 16, 16);
        head.setPosition(0, 5.3f, 0);
        head.setName("Head");

        // 设置材质
        Material headMaterial = new Material();
        headMaterial.setColor(colorPearArr);
        headMaterial.enableLighting(true);
        headMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        head.setMaterial(headMaterial);

        // 添加到当前对象（this）
        addChild(head);
    }

    private void createTorso() {
//        Cylinder torso = new Cylinder(1.5f, 1.5f, 4.0f, 12, 8);
        Cylinder torso = new Cylinder(2.6f,1.1f,4,12,true,true,true);
        torso.setPosition(0, 3.0f, 0);
//        torso.setRotationZ(90);
        torso.setRotation(0,0,90);
        torso.setName("Torso");

        Material torsoMaterial = new Material();
        torsoMaterial.setColor(colorPearArr);
        torsoMaterial.enableLighting(true);
        torsoMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        torso.setMaterial(torsoMaterial);

        addChild(torso);
    }

    private void createArms() {
        // 左臂
//        Cylinder leftArm = new Cylinder(0.4f, 0.4f, 3.0f, 12, 8);
        Cylinder leftArm = new Cylinder(2.0f, 0.4f, 3, 12);
        leftArm.setPosition(-1.5f, 3.2f, 0);
//        leftArm.setRotationZ(90);
        leftArm.setRotation(0,0,90);
        leftArm.setName("LeftArm");

        Material armMaterial = new Material();
        armMaterial.setColor(colorPearArr);
        armMaterial.enableLighting(true);
        armMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        leftArm.setMaterial(armMaterial);

        addChild(leftArm);

        // 右臂
//        Cylinder rightArm = new Cylinder(0.4f, 0.4f, 3.0f, 12, 8);
        Cylinder rightArm = new Cylinder(2.0f, 0.4f, 3, 12);
        rightArm.setPosition(1.5f, 3.2f, 0);
//        rightArm.setRotationZ(90);
        rightArm.setRotation(0,0,90);
        rightArm.setName("RightArm");
        rightArm.setMaterial(armMaterial);

        addChild(rightArm);
    }

    private void createLegs() {
        // 左腿
//        Cylinder leftLeg = new Cylinder(0.5f, 0.5f, 3.5f, 12, 8);
        Cylinder leftLeg = new Cylinder(1.8f, 0.5f, 3, 12);
        leftLeg.setPosition(-0.7f, 0.78f, 0);
        leftLeg.setRotation(0,0,90);
        leftLeg.setName("LeftLeg");

        Material legMaterial = new Material();
        legMaterial.setColor(colorPearArr);
        legMaterial.enableLighting(true);
        legMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        leftLeg.setMaterial(legMaterial);

        addChild(leftLeg);

        // 右腿
//        Cylinder rightLeg = new Cylinder(0.5f, 0.5f, 3.5f, 12, 8);
        Cylinder rightLeg = new Cylinder(1.8f, 0.5f, 3, 12);
        rightLeg.setPosition(0.7f, 0.78f, 0);
        rightLeg.setRotation(0,0,90);
        rightLeg.setName("RightLeg");
        rightLeg.setMaterial(legMaterial);

        addChild(rightLeg);
    }

    // 获取特定部位的方法
    public Object3D getHead() {
        return getChildByName("Head");
    }

    public Object3D getLeftArm() {
        return getChildByName("LeftArm");
    }

    public Object3D getRightArm() {
        return getChildByName("RightArm");
    }


}
