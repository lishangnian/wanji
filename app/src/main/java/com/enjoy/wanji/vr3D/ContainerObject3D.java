package com.enjoy.wanji.vr3D;

import org.rajawali3d.Object3D;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ContainerObject3D {
    //0--未知  1--行人 2--机动车  待命定位中的model
    public static ConcurrentLinkedQueue<Object3D> ModelWaite0UnknownQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Object3D> ModelWaite1PedestrianQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Object3D> ModelWaite2VehicleQueue = new ConcurrentLinkedQueue<>();

    //活跃中的model
    public static ConcurrentLinkedQueue<Object3D> ModelActive0UnknownQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Object3D>  ModelActive1PedestrianQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Object3D> ModelActive2VehicleQueue = new ConcurrentLinkedQueue<>();


    //
    public static volatile LinkedList<TrafficObj> Obj0UnknownList = new LinkedList<>();
    public static volatile LinkedList<TrafficObj> Obj1PedestrianList = new LinkedList<>();
    public static volatile LinkedList<TrafficObj> Obj2VehicleList = new LinkedList<>();

    //用于存放预new的 trafficObj,避免每次接收都new一个
    public static volatile ConcurrentLinkedQueue<TrafficObj> TrafficObjHomeQueue = new ConcurrentLinkedQueue<>();

    //先new9个待用
    static {
        while (TrafficObjHomeQueue.size() <= 9){
            TrafficObjHomeQueue.add(new TrafficObj());
        }
    }


}
