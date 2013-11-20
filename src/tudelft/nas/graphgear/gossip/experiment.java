/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

import java.io.BufferedWriter; 
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruud van de Bovenkamp
 * The experiment class is used a basis for experiments with gossip
 * algorithms. It contains all frequently used methods
 */
public class experiment {
    
    /**
     * Shuffles the array passed as argument
     * @param a array to be shuffled
     */
    public void shuffleOrder(int[] a){
        int temp;
        int pos;
        for (int i=0; i<a.length; i++)
        {
            pos = gen.nextInt(a.length-i);
            temp = a[i];
            a[i] = a[i+pos];
            a[i+pos] = temp;
        }
    }
    
    /**
     * Sets the seed for the random generator.
     * @param seed the seed for the random generator. If the value is 
     * smaller than zero, the current system time is used as a seed
     */
    public void initRandom(long seed){
        if(seed < 0)
        {
            seed = System.currentTimeMillis();
        }
        System.out.println("Using seed: " + seed);
        gen = new Random(seed);
    }
    
    /**
     * makes the thread go to sleep for ms milliseconds.
     * @param ms number of milliseconds to make the thread sleep
     */
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * orders all the links in the network with highest id first
     * @return 2d array of links
     */
    public int[][] getLinks(){
        int l = 0;
        for(Node N : network)
        {
            l += N.getDegree();
        }
        int[][] res = new int[l/2][2];
        int c = 0;
        int s;
        int d;
        for(Node N : network)
        {
            s = N.id;
            for(int i =0;i<N.getDegree();i++)
            {
                d = N.getNeighbour(i).id;
                if(s > d)
                {
                    res[c][0] = s;
                    res[c][1] = d;
                    c++;
                }
            }
        }
        return res;
    }
    
    /**
     * Removes a node from the network
     * @param index index in the network array
     */
    public void removeNode(int index){
        Node N = network.get(index);
        Node P;
        for(int i=0;i<N.getDegree();i++)
        {
            P = N.getNeighbour(i);
            P.removeLink(P.linkIndexForNode(N.id));
        }
        network.remove(index);
    }

    /**
     * Adds a FAB to all the nodes in the network
     * @param f the FAB ti be added
     * @param clear true: remove all current FABS
     */
    public void addFAB(NodeFAB f, boolean clear){
        if(clear)
            fabs.clear();
        int numB = 5;
        fabs.add(f.make());
        for(Node N : network)
        {
            if(clear)
            {
                N.clearFABS();
            }
            N.addFAB(f.make());
            N.initFAB(N.getNumFab()-1,0);
        }
    }
    
    /**
     * Creates the order array
     * @return array of of length network size
     */
    public int[] createOrder(){
        int[] order = new int[network.size()];
        for(int i=0;i<order.length;i++)
        {
            order[i] = i;
        }
        return order;
    }
    
    /**
     * Reads a network from a pajek file
     * @param file location of the pajek file
     */
    public void readPajek(String file){
        if(network!= null)
            network.clear();
        network = netGen.readPajek(file);
    }
    
    /**
     * reads a network from an adjacency matrix (columns are separated by spaces)
     * @param file location of the adjacency matrix
     */
    public void initNetworkFromAdj(String file){
        if(network!= null)
            network.clear();
        network = netGen.readFromAdjacencyMatrix(file);
        Runtime.getRuntime().gc();
    }
    
    /**
     * Generates a grid network.
     * @param n dimensions of the grid. The resulting network will be nxn
     * @return nxn grid
     */
    public ArrayList<Node> getGrid(int n){
        return netGen.generateGrid(n, n);
    }
    
    /**
     * Write the current network to an adjacency matrix
     * @param file destination of the adjacency matrix
     */
    public void writeToAdjacencyMatrix(String file){
        int[][] A = new int[network.size()][network.size()];
        for(Node N:network)
        {
            for(int i=0;i<N.getDegree();i++)
            {
                A[N.id][N.getNeighbour(i).id] = 1;
            }
        }
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            String row = "";
            for(int i=0;i<A.length;i++)
            {
                row = "";
                for(int j =0;j<A.length;j++)
                {
                    row += A[i][j] + " ";
                }
                row += "\n";
                out.write(row);
            }
            out.flush();
            out.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Used to generate or read the network. NOTE this procedure also calls the
     * garbage collection. (is needed on the cluster for some reason)
     * @param n Number of nodes
     * @param p depending on the type: link probability, number of links per node, width of grid
     * @param type 0: connected ER<br>1: PA<br>2: grid(n by p)<br>3: path<br>4: hardcoded read from matrix
     * <br>5: star<br>6: double star<br>7: complete<br>8: Random Geometric Graph<br>9: ring
     */
    public void initNetwork(int n, double p, int type){
        if(network!= null)
        {
            network.clear();
        }
        switch(type)
        {
            case 0:
                if(Math.abs(p+1) < 0.0001)
                {
                    p = (2*Math.log((double)n))/(double)n;
                }
                network = netGen.generateConnectedER(n, p);
                break;
            case 1:
                int m = -1;
                if(Math.abs(p+1) < 0.0001)
                {
                    m = (int)((double)(n-1)*Math.log(n)/(double)n);
                }
                else
                {
                    m = (int)p;
                }
                System.out.println("N: " + n + " m: " + m);
                network = netGen.generatePA(n, m);
                break;
            case 2:
                network = netGen.generateGrid(n, (int)p);
                break;
            case 3:
                network = netGen.generatePath(n);
                break;
            case 4:
                network = netGen.readFromAdjacencyMatrix("d:/4ntest.txt");
                break;
            case 5:
                network = netGen.generateStar(n-1, 1);
                break;
            case 6:
                network = netGen.generateDoubleStart(n, (int)p);
                break;
            case 7:
                network = netGen.generateCompleteGraph(n);
                break;
            case 8:
                network = netGen.generateRGG(n, p);
                break;
            case 9:
                network = netGen.generateRing(n);
                break;
            case 10:
                network = netGen.generateER(n, p);
                break;
            case 11:
                network = netGen.generateConnectedERFixedAvDegree(n, p);
                break;
            default:
                System.out.println("network type unknown");
                break;
        }
        Runtime.getRuntime().gc();
    }
    String[] networkTypes = new String[]{"ER","PA","GRID","PATH","from file","STAR","DOUBLESTAR","COMPLETE","RGG","RING","uncheked ER","ER fixed av degree"};
    Random gen = new Random();
    ArrayList<Node> network;
    NetworkGenerator netGen = new NetworkGenerator(false);
    ArrayList<NodeFAB> fabs = new ArrayList<>();
}
