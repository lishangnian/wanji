package com.example;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import javafx.util.Pair;

/**
 * Created by root on 18-9-21.
 */

 class MypQ{

    Comparator<Pair<Integer,Double>> OrderIsDn=new Comparator<Pair<Integer, Double>>() {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            return (int)(o1.getValue()-o2.getValue());
        }
    };

    Queue<Pair<Integer,Double>> priorityQueue;

    MypQ(int length)
    {
        priorityQueue=new PriorityQueue<Pair<Integer,Double>>(length,OrderIsDn);
    }

    boolean empty()
    {
        return priorityQueue.isEmpty();
    }

    void push(int priority,double item)
    {
        priorityQueue.add(new Pair<>(priority,item));
    }

//    double pop()
//    {
//        double best_item=0;
//        if(!priorityQueue.isEmpty())
//        {
//            best_item=priorityQueue.poll().getValue();
//        }
//        return best_item;
//    }

    int pop()
    {
        int best_item=0;
        if(!priorityQueue.isEmpty())
        {
            best_item=priorityQueue.poll().getKey();
        }
        return best_item;
    }
}

class Graph{
    Map<Integer,Trajectory>  map;

    Graph(Map<String,Trajectory> g){
         map = new HashMap<Integer,Trajectory>();

        for (Map.Entry<String,Trajectory> entry : g.entrySet()) {
            int num=entry.getValue().num;
            map.put(num,entry.getValue());
        }
    }

    Vector<Integer> neighbors(int index)
    {
        Vector<Integer> temp=new Vector<>();

        int size=map.get(index).neibors.size();
        System.out.println("neibor size:"+size);
        for(int i=0;i<size;i++)
        {
            int num=map.get(index).neibors.get(i);
            temp.add(num);
        }
        return temp;
    }

    double cost(int index)
    {
        return map.get(index).cost;
    }

    List<String> get(int index)
    {
        return map.get(index).lines;
    }

    String getName(int index)
    {
        return map.get(index).ID;
    }

}


public class Dijkstra {
    Dijkstra(){};

    Map<Integer,Integer> dijkstra_search(
            int map_num,
            Graph graph,
            int start,
            int goal,
            Map<Integer,Integer> came_from,
            Map<Integer,Double> cost_so_far){

        MypQ frontier=new MypQ(map_num);
        frontier.push(start,0);

        came_from.put(start,start);
        cost_so_far.put(start,0.0);

        while(!frontier.empty())
        {
            int current=(int)frontier.pop();

            if(current==goal)
            {
                break;
            }

            Vector<Integer> num=graph.neighbors(current);
            for(int next : graph.neighbors(current)){
                double new_cost=100000;
                if(cost_so_far.containsKey(current))
                {
                    new_cost=cost_so_far.get(current)+graph.cost(next);
                }
                
                if(!cost_so_far.containsKey(next)
                        || new_cost<cost_so_far.get(next)){
                    if(!cost_so_far.containsKey(next))
                    {
                        cost_so_far.put(next,graph.cost(next));
                    }
                    else
                    {
                        cost_so_far.put(next,new_cost);
                    }

                    came_from.put(next,current);
                    frontier.push(next,new_cost);
                }
            }
        }
        return came_from;

    }

    Vector<Integer> reconstruct_path(int start,int goal,Map<Integer,Integer> came_from)
    {
        Vector<Integer> path=new Vector<>();
        int current = goal;
        while (current != start) {
            path.add(current);

            if(!came_from.containsKey(current))
            {
//                cout<<"can't find "<<current<<endl;
                break;
            }
            else
            {
                current = came_from.get(current);
            }

        }
        path.add(start); // optional
        int num=path.size();
        Vector<Integer> output=new Vector<>(num);
        for(int i=num-1;i>=0;i--)
        {
            output.add(path.get(i));
        }
        return output;
    }

}
