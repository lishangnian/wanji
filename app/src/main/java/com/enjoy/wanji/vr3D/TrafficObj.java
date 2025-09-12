package com.enjoy.wanji.vr3D;

public class TrafficObj {
    private int id;   //跟踪id
    private int classification ; //类别   0--未知  1--行人 2--机动车
    //原点在后轮中心  右是正， 前是正
    private float x;  //横坐标
    private float y;   //纵坐标
    private float width;             //#宽度
    private float length;           //#长度


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassification() {
        return classification;
    }

    /**
     *    0--未知  1--行人		2--机动车
     */
    public void setClassification(int classification) {
        this.classification = classification;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }
}
