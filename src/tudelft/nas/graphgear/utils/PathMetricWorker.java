/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruud
 * The path metric worker is used in a multithreaded version of the path metric calculation in General
 */
public class PathMetricWorker extends Thread {
    /**
     * Creates a new pathmetricworker. Every pathmetricworker has its own network in memory. This is 
     * not efficient. Just so you know.
     * @param _network the network
     */
    public PathMetricWorker(Node[] _network){
        network = _network;
        
    }
    public void run(){
        while(!stop)
        {
            if(task)
            {
                task = false;
                compute();
            }
        }
    }
    /**
     * Sets this worker to compute the path metrics for a node
     * @param n the node whose path metrics the worker should compute
     */
    public synchronized void setTask(int n){
        node = n;
        task = true;
    }
    private void compute(){
        if(dist == null)
        {
            dist = new int[network.length];
            sigma = new int[network.length];
            delta = new double[network.length];
            betw = new double[network.length];
        }
        int v;
        int w;
        ArrayList<ArrayList<Integer>> Pred = new ArrayList<>();
        Queue<Integer> Q = new LinkedList<>();
        Stack<Integer> S = new Stack<>();
        int s = node;
        // init
        Pred.clear();
        for(int i=0;i<network.length;i++)
        {
            Pred.add(new ArrayList<Integer>());
            dist[i] = Integer.MAX_VALUE;
            sigma[i] = 0;
            betw[i] = 0d;
            delta[i] = 0;
        }
        dist[s] = 0;
        sigma[s] = 1;
        Q.add(s);
        while(!Q.isEmpty())
        {
            v = Q.poll();
            S.push(v);
            for(Node W : network[v].links)
            {
                w = W.id;
                // path discovery
                if(dist[w] == Integer.MAX_VALUE)
                {
                    dist[w] = dist[v] + 1;
                    Q.add(w);
                }
                // path counting
                if(dist[w] == dist[v] + 1)
                {
                    sigma[w] = sigma[w] + sigma[v];
                    Pred.get(w).add(v);
                }
            }
        }
        
        // dist now contains the shortest paths from every node to s
        // Next we are going to accumulate the paths
        for(int i=0;i<network.length;i++)
        {
            delta[i] = 0d;
        }
        while(!S.isEmpty())
        {
            w = S.pop();
            for(int V : Pred.get(w))
            {
                delta[V] += ((double)sigma[V]/(double)sigma[w])*(1d+delta[w]);
            }
            if(w!=s)
            {
                betw[w] += delta[w];
            }
        }
        done = true;
    }
    public int getNode(){
        return node;
    }
    public double[] getBetweenness(){
        return betw;
    }
    public int[] getDist(){
        return dist;
    }
    boolean stop = false;
    boolean workerActive = true;
    Node[] network;
    int[] dist;
    int[] sigma;
    double[] betw;
    double[] delta;
    Random gen = new Random();
    int node = -1;
    int result = -1;
    public volatile boolean done = true;
    private volatile boolean task = false;
}
