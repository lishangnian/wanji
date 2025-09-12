package com.enjoy.wanji.vr3D;

import android.util.Log;
import com.enjoy.wanji.service.EnjoySocketService;
import org.rajawali3d.Object3D;
import java.util.ArrayList;
import java.util.List;

public class ModelAgent {
   public static List<Object3D> list = null;


    public static void updatePosition() {
        if (EnjoySocketService.UpdateUIModelLock.tryLock()){
            try {
                updatePositionImp();
            }finally {
                EnjoySocketService.UpdateUIModelLock.unlock();
            }
        }else {
            Log.i("msgTag","Lock not get 3D: 动画更新收未获取锁");
        }
    }




    //偏移量
    public static double X_OffSet_K = 2.4d;
    /**
     * 0-未知   1--行人   2--机动车
     */
    private static void updatePositionImp() {
        /***************** 2 类物检测定位 **********************/
        //活跃的数量大于检测到的
        while (ContainerObject3D.ModelActive2VehicleQueue.size() > ContainerObject3D.Obj2VehicleList.size()) {
            //将活跃的移到等待中的
            Object3D objModel = ContainerObject3D.ModelActive2VehicleQueue.poll();
            if (objModel!= null) {
                objModel.setVisible(false);
                ContainerObject3D.ModelWaite2VehicleQueue.offer(objModel);
            }else break;
        }
        //活跃的数量小于检测到的
        while (ContainerObject3D.ModelActive2VehicleQueue.size() < ContainerObject3D.Obj2VehicleList.size()) {
            //将等待的移到活跃中中的
            Object3D objModel = ContainerObject3D.ModelWaite2VehicleQueue.poll();
            if (objModel != null) {
                ContainerObject3D.ModelActive2VehicleQueue.offer(objModel);
            }else break;
        }
        if (ContainerObject3D.Obj2VehicleList.size() > 0){
            list = new ArrayList<>(ContainerObject3D.ModelActive2VehicleQueue);
            //更新位置   检测物种 X右是正， Y前是正
            for (int i = 0; i < ContainerObject3D.Obj2VehicleList.size() && i < list.size(); i++) {
                TrafficObj obj = ContainerObject3D.Obj2VehicleList.get(i);
                Object3D model =  list.get(i);
                model.setVisible(true);
                model.setPosition(obj.getX()/X_OffSet_K, 0, 0-obj.getY());
            }
        }
    }

}
