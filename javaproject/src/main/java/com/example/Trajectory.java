package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 18-9-18.
 */
public class Trajectory {

    public int num;
    public String ID;
    public List<roadpoint> pList;//原始的轨迹点坐标序列
    public List<String> lines;//原始的轨迹路径,字符串值
    public List<Integer> neibors;//在整个地图中,与该条路径尾部相连接的其他路径
    int cost;
    Trajectory(){
        lines=new ArrayList<String>();
        pList=new ArrayList<roadpoint>();
        neibors=new ArrayList<Integer>();
    }

    public Trajectory(File file,int _num) throws IOException {

        lines=new ArrayList<String>();
        pList=new ArrayList<roadpoint>();
        neibors=new ArrayList<Integer>();

        num=_num;
        ID=file.getName();

            /* 读入TXT文件 */
//        File file = new File(filename); // 要读取以上路径的input。txt文件
        InputStreamReader reader = null; // 建立一个输入流对象reader
        try {
            reader = new InputStreamReader(
                    new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
        String line = "";
        line = br.readLine();
        while (line != null) {
            lines.add(line);
            String[] splitstr=line.split("\t");
            roadpoint p=new roadpoint(Double.parseDouble(splitstr[1]),Double.parseDouble(splitstr[2]));
            pList.add(p);
            line = br.readLine(); // 一次读入一行数据

        }
        cost=pList.size();
        br.close();
        reader.close();


    }

}
