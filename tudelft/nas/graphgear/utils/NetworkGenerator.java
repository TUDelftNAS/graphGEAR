/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruud van de Bovenkamp
 * Creates a few synthetic networks. Also check the network generator in tudelft.nas.graphstream.gossip for
 * more network types
 */
public class NetworkGenerator {
    /**
     * Creates a new NetworkGenerator. See also gossip.networkgenerator
     * @param _verb verbose or not
     */
    public NetworkGenerator(boolean _verb){
        verb = _verb;
    }

    /**
     * Creates directed tree
     * @param n number of leafs per node
     * @param d depth of the tree
     * @return network
     */
    public Node[] directedTree(int n,int d){
        Node[] network = new Node[(int)Math.pow(n,d)-1];
        System.out.println(network.length);
        for(int i=0;i<(int)Math.pow(n,d-1)-1;i++)
        {
            network[i] = new Node(i,2);
        }
        for(int i=(int)Math.pow(n,d-1)-1;i<(int)Math.pow(n,d)-1;i++)
        {
            network[i] = new Node(i,0);
        }
        for(int i=0;i<(int)Math.pow(n,d-1)-1;i++)
        {
            for(int j = 0;j<n;j++)
            {
                network[i].addLink(network[i*n+j+1]);
            }
        }
        return network;
    }
    
    /**
     * Creates a naive Erdos-Renyi graph (see gossip.networkgenerator for a faster implementation)
     * @param n number of nodes
     * @param p link probability
     * @return the network
     */
    public Node[] naiveER(int n, double p){
        
        System.out.println("Value for p: " + p);
        Random gen = new Random();
        Set[] nodes = new Set[n];
        for(int i=0;i<n;i++)
        {
            nodes[i] = new HashSet<Integer>();
        }
        for(int i=0;i<n;i++)
        {
            for(int j = i+1;j<n;j++)
            {
                if(gen.nextDouble() <= p)
                {
                    nodes[i].add(j);
                    nodes[j].add(i);
                }
            }
        }
        // create the final network structure
        Node[] network = new Node[n];
        for(int i=0;i<network.length;i++)
        {
            network[i] = new Node(i,nodes[i].size());
        }
        // create the links
        int lc = 0;
        for(int i=0;i<network.length;i++)
        {
            for(int j : (Set<Integer>)nodes[i])
            {
                network[i].addLink(network[j]);
                lc++;
            }
        }
        System.out.println("Network has " + n + " nodes and " + lc/2 + " links");
        return network;
    }
    
    /**
     * Creates a naive ER network with a fixed number of nodes and links
     * @param n number of nodes
     * @param l number of links
     * @return  the network
     */
    public Node[] naiveER(int n, int l){
        double p = (double)(2*l)/(double)(n*(n-1));
        System.out.println("Value for p: " + p);
        Random gen = new Random();
        Set[] nodes = new Set[n];
        for(int i=0;i<n;i++)
        {
            nodes[i] = new HashSet<Integer>();
        }
        for(int i=0;i<n;i++)
        {
            for(int j = i+1;j<n;j++)
            {
                if(gen.nextDouble() <= p)
                {
                    nodes[i].add(j);
                    nodes[j].add(i);
                }
            }
        }
        // create the final network structure
        Node[] network = new Node[n];
        for(int i=0;i<network.length;i++)
        {
            network[i] = new Node(i,nodes[i].size());
        }
        // create the links
        int lc = 0;
        for(int i=0;i<network.length;i++)
        {
            for(int j : (Set<Integer>)nodes[i])
            {
                network[i].addLink(network[j]);
                lc++;
            }
        }
        System.out.println("Network has " + n + " nodes and " + lc/2 + " links");
        return network;
    }

    /**
     * Creates directed circle graph
     * @param n number of nodes
     * @return the network
     */
    public Node[] generateDirectedCircle(int n){
        Node[] network = new Node[n];
        for(int i=0;i<n;i++)
        {
            network[i] = new Node(i,1);
        }
        for(int i=0;i<n-1;i++)
        {
            network[i].addLink(network[i+1]);
        }
        network[n-1].addLink(network[0]);
        return network;
    }
    
    /**
     * Creates a Wats&Strogatz small world graph
     * @param n number of nodes
     * @param k number of links per node
     * @param p rewire probability
     * @return the network
     */
    public Node[] generateSW(int n, int k, double p){
        Random gen = new Random();
        Set[] nodes = new Set[n];
        // init node sets and create lattice
        int forward;
        int backward;
        for(int i=0;i<n;i++)
        {
            nodes[i] = new HashSet<Integer>();
            for(int j=0;j<k;j++)
            {
                forward = i+j+1;
                backward = i-j-1;
                if(backward < 0)
                {
                    backward += n;
                }
                if(forward >= n)
                {
                    forward -= n;
                }
                nodes[i].add(forward);
                nodes[i].add(backward);
            }
        }
        if(verb)
        {
            for(int i=0;i<n;i++)
            {
                System.out.print(i + ") ");
                for(int j:(HashSet<Integer>)nodes[i])
                {
                    System.out.print(j + " ");
                }
                System.out.println();
            }
        }
        int target;
        // Go over all the nodes in a clock wise fashion and rewire the links
        for(int j = 0; j<k;j++)
        {
            for(int i=0;i<n;i++)
            {
                // determine link to process
                forward = i+j+1;
                if(forward >= n)
                {
                    forward -= n;
                }
                // rewire?
                if(gen.nextDouble() < p)
                {
                    // remove current link
                    if(!nodes[i].remove(forward))
                    {
                        System.out.println("Link no longer there!");
                    }
                    if(!nodes[forward].remove(i))
                    {
                        System.out.println("Link no longer there!");
                    }
                    // find new target
                    target = gen.nextInt(n);
                    // check whether link already exists.
                    while(nodes[i].contains(target))
                    {
                        target = gen.nextInt(n);
                    }
                    // create new link
                    nodes[i].add(target);
                    nodes[target].add(i);
                }
            }
        }
        // create the final network structure
        Node[] network = new Node[n];
        for(int i=0;i<network.length;i++)
        {
            network[i] = new Node(i,nodes[i].size());
        }
        // create the links
        int lc = 0;
        for(int i=0;i<network.length;i++)
        {
            for(int j : (Set<Integer>)nodes[i])
            {
                network[i].addLink(network[j]);
                lc++;
            }
        }
        System.out.println("Network has " + n + " nodes and " + lc/2 + " links");
        return network;
    }
    
    
    public String[] readFromAdjacencyMatrix(String file){
        ArrayList<Node> nodes = new ArrayList<Node>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            String[] parts;
            line = in.readLine();
            parts = line.split(" ");
            String p;
            String[] res = new String[parts.length];
            for(int i=0;i<res.length;i++)
            {
                res[i] = "";
            }
            int s =0;

            int d;
            for(int i=0;i<parts.length; i++)
            {
                p = parts[i];
                d = Integer.parseInt(p);
                if(d == 1)
                {
                    res[s] += i + " ";
                }
            }
            while((line = in.readLine())!= null)
            {
                s++;
                parts = line.split(" ");
                for(int i=0;i<parts.length; i++)
                {
                    p = parts[i];
                    d = Integer.parseInt(p);
                    if(d == 1)
                    {
                        res[s] += i + " ";
                    }
                }
            }
            in.close();
            return res;
        }
        catch (Exception ex)
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean verb = false;
}