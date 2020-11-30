package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MyClass {
    static Map<String,Trajectory> graph;
    static List<String> pathFileLines;
    static int mapnum=0;
    static double neiborThreshold;

    public static void main(String[] args) throws IOException {
        graph=new HashMap<String,Trajectory>();
        neiborThreshold=4e-5;
        pathFileLines=new ArrayList<>();

        loadMap();
        FindNeibors();

        //加载point
        List<roadpoint> keypoints=new ArrayList<>(2);
        keypoints.add(new roadpoint(120.77366030701293,31.59040124724451));
        keypoints.add(new roadpoint(120.78171149557816,31.58970821068374));

        //start point
        int index=getNearestTrack(keypoints.get(0), graph);

        //end point
        int index1=getNearestTrack(keypoints.get(1), graph);

        //lastest
        Graph mygraph=new Graph(graph);
        Dijkstra dijk=new Dijkstra();
        Map<Integer, Integer> came_form=new HashMap<>();
        Map<Integer,Double> cost_so_far=new HashMap<>();
        Map<Integer, Integer> came_form1=dijk.dijkstra_search(mapnum,mygraph,index,index1,came_form,cost_so_far);

        Vector<Integer> path= dijk.reconstruct_path(index,index1,came_form1);

        File f = new File("/home/jintong/newsave.txt");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        for(int i=0;i<path.size();i++)
        {
            List<String> pointList=mygraph.get(path.get(i));
            for(int j=0;j<pointList.size();j++)
            {
                pathFileLines.add(pointList.get(j));
                output.write(pointList.get(j));
                output.write("\n");
            }
            System.out.println(mygraph.getName(path.get(i)));

        }

        output.close();
    }

    //方法getFiles根据指定路径获取所有的文件
    public static ArrayList<File> getFiles(String path) throws Exception {
        //目标集合fileList
        ArrayList<File> fileList = new ArrayList<File>();
        File file = new File(path);
        if(file.isDirectory()){
            File []files = file.listFiles();
            for(File fileIndex:files){
                //如果这个文件是目录，则进行递归搜索
                if(fileIndex.isDirectory()){
                    getFiles(fileIndex.getPath());
                    }else {
                   //如果文件是普通文件，则将文件句柄放入集合中
                    fileList.add(fileIndex);
                }
            }
        }
        return fileList;
    }


    public static void loadMap()
    {

        try {
            List<File> file=getFiles("/home/jintong/globalpathplanning/路网");
            mapnum=file.size();
            for(int i=0;i<file.size();i++)
            {
                Trajectory trajectory=new Trajectory(file.get(i),i+1);
                graph.put(trajectory.ID,trajectory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static double getDistance(roadpoint p1,roadpoint p2)
    {
        double delta_lon = p1.x - p2.x;
        double delta_lat = p1.y - p2.y;
        return Math.sqrt(delta_lon * delta_lon + delta_lat * delta_lat);
    }

    public static boolean closeTo(roadpoint p1,roadpoint p2)
    {
        double dis=getDistance(p1,p2);
        return dis<neiborThreshold;
    }

    public static boolean isConnect(Trajectory t1,Trajectory t2)
    {
        if (t1.pList.size() == 0 || t2.pList.size() == 0)
            return false;
        roadpoint t1_end = t1.pList.get(t1.pList.size() - 1);
        roadpoint t2_start = t2.pList.get(0);
        return closeTo(t1_end, t2_start)&&(t1.ID!=t2.ID);
    }

    //find neibor trajectory
    public static void FindNeibors()
    {
        for (String t1: graph.keySet()) {
        for (String t2: graph.keySet()) {
            if(isConnect(graph.get(t1),graph.get(t2)))
            {
                graph.get(t1).neibors.add(graph.get(t2).num);
                System.out.println(t1+"---------->"+t2);
            }
        }
    }
    }
    //get the key point belong to which road by the min distance
    public static int getNearestTrack(roadpoint p,Map<String,Trajectory> g)
    {
        double mindistance=1000000;
        String minId="";
        for (Trajectory t1: g.values()) {
        for (roadpoint p2: t1.pList) {
            if(getDistance(p,p2)<mindistance){
                mindistance=getDistance(p,p2);
                minId=t1.ID;
            }
        }
    }
        return g.get(minId).num;
    }


}
