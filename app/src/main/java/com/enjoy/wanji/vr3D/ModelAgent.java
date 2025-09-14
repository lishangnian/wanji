package com.enjoy.wanji.vr3D;

import android.util.Log;
import com.enjoy.wanji.service.EnjoySocketService;
import org.rajawali3d.Object3D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ModelAgent {
   public static List<Object3D> list = null;


    public static void updatePosition() {
        if (EnjoySocketService.UpdateUIModelLock.tryLock()){
            try {
                updatePositionImp0();
            }finally {
                EnjoySocketService.UpdateUIModelLock.unlock();
            }
        }else {
            Log.i("msgTag","Lock not get 3D: 动画更新收未获取锁");
        }
    }


    private static void updatePositionImp0(){
        updatePositionImp1(ContainerObject3D.Obj0UnknownList,ContainerObject3D.ModelActive0UnknownQueue,ContainerObject3D.ModelWaite0UnknownQueue);
        updatePositionImp1(ContainerObject3D.Obj1PedestrianList,ContainerObject3D.ModelActive1PedestrianQueue,ContainerObject3D.ModelWaite1PedestrianQueue);
        updatePositionImp1(ContainerObject3D.Obj2VehicleList,ContainerObject3D.ModelActive2VehicleQueue,ContainerObject3D.ModelWaite2VehicleQueue);
    }


    //偏移量
    public static double X_OffSet_K = 2.4d;
    /**
     * 0-未知   1--行人   2--机动车
     */
    private static void updatePositionImp1(List<TrafficObj> objList,
                                           ConcurrentLinkedQueue<Object3D> activeQueue,
                                           ConcurrentLinkedQueue<Object3D> waiteQueue) {
        //活跃的数量大于检测到的
        while (activeQueue.size() > objList.size()) {
            //将活跃的移到等待中的
            Object3D objModel = activeQueue.poll();
            if (objModel!= null) {
                objModel.setVisible(false);
                waiteQueue.offer(objModel);
            }else break;
        }
        //活跃的数量小于检测到的
        while (activeQueue.size() < objList.size()) {
            //将等待的移到活跃中中的
            Object3D objModel = waiteQueue.poll();
            if (objModel != null) {
                activeQueue.offer(objModel);
            }else break;
        }
        if (objList.size() > 0){
            list = new ArrayList<>(activeQueue);
            //更新位置   检测物种 X右是正， Y前是正
            for (int i = 0; i < objList.size() && i < list.size(); i++) {
                TrafficObj obj = objList.get(i);
                Object3D model =  list.get(i);
                model.setVisible(true);
                model.setPosition(obj.getX()/X_OffSet_K, 0, 0-obj.getY());
            }
        }
    }
}
