/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.linkset.LinkSet;
import tudelft.nas.graphgear.linkset.LinkSetNode;


/**
 *
 * @author Ruud van de Bovenkamp
 */
public class NetworkGenerator {
    /**
     * Creates a new instance of the network generator.
     * @param _verb verbose or not
     */
    public NetworkGenerator(boolean _verb){
        verb = _verb;
    }
    
    
    /**
     * Creates a network from a utils.node array
     * @param in utils network in
     * @return network
     */
    public ArrayList<Node> fromNodeArray(tudelft.nas.graphgear.utils.Node[] in){
        ArrayList<Node> nw = new ArrayList<>();
        for(int i=0;i<in.length;i++)
        {
            nw.add(new Node(i));
        }
        for(tudelft.nas.graphgear.utils.Node N : in){
            for(tudelft.nas.graphgear.utils.Node P : N.links)
            {
                nw.get(N.id).addLink(new Link(nw.get(P.id)));
            }
        }
        return nw;
    }
    
    
    /**
     * Converts a network of the gossip type to the utils type
     * @param nw network in gossip format
     * @return network in utils format
     */    
    public tudelft.nas.graphgear.utils.Node[] toNodeArray(ArrayList<Node> nw){
        tudelft.nas.graphgear.utils.Node[] res = new tudelft.nas.graphgear.utils.Node[nw.size()];
        for(int i=0;i<nw.size();i++)
        {
            res[i] = new tudelft.nas.graphgear.utils.Node(nw.get(i).id,nw.get(i).getDegree());
        }
        for(int i=0;i<nw.size();i++)
        {
            for(Node P : nw.get(i).getNeighbours())
            {
                res[i].addLink(res[P.id]);
            }
        }
        return res;
    }
    
    /**
     * Saves the network as a linkset from the utils class
     * @param nw the network to be saved
     * @param file destination
     */
    public void createLinkSet(ArrayList<Node> nw, String file){
        LinkSet L = new LinkSet();
        Node P;
        for(Node N : nw)
        {
            for(int i=0;i<N.getDegree();i++)
            {
               P = N.getNeighbour(i);
               L.addLink(new LinkSetNode(N.id,P.id));
            }
        }
        L.writePartFile(file);
    }
    
    /**
     * Saves the network as a sparse adjacency matrix. That is, the output file
     * will consist of three columns the first contains the row index of the non-zero
     * elements the second the column index and the third consists of all ones.
     * @param nw the network to be saved
     * @param file destination file location
     */
    public void saveMatlabSparse(ArrayList<Node> nw, String file){
        try 
        {
            // create output stream
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            // Go over all nodes and write the row and column index to the output
            for(Node N : nw)
            {
                for(int i=0;i<N.getDegree();i++)
                {
                    out.write((N.id+1) + " " + (N.getNeighbour(i).id+1) + " 1\n");
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates a ring graph of n nodes
     * @param n number of nodes
     * @return ring graph of n nodes
     */
    public ArrayList<Node> generateRing(int n){
        ArrayList<Node> nodes = generatePath(n);
        nodes.get(0).addLink(new Link(nodes.get(n-1)));
        nodes.get(n-1).addLink(new Link(nodes.get(0)));
        return nodes;
    }
    
    /**
     * Creates a grid
     * @param w width of the grid
     * @param h height of the grid
     * @return a w x h grid
     */
    public ArrayList<Node> generateGrid(int w, int h){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int i=0;i<(w*h);i++)
        {
            Node n = new Node(i);
            nodes.add(n);
        }
        int nc = 0;
        int lc = 0;
        Link link;
        for(int j = 0 ; j<h;j++)
        {
            for(int i = 0; i<w;i++)
            {
                if(i < w-1) // link to the right
                {
                    link = new Link(nodes.get(nc+1));
                    nodes.get(nc).addLink(link);
                    lc++;
                }
                if(i > 0) // link to the left
                {
                    link = new Link(nodes.get(nc-1));
                    nodes.get(nc).addLink(link);
                    lc++;
                }
                if(j < h-1) // link down
                {
                    link = new Link(nodes.get(nc+w));
                    nodes.get(nc).addLink(link);
                    lc++;
                }
                if(j > 0) // link up
                {
                    link = new Link(nodes.get(nc-w));
                    nodes.get(nc).addLink(link);
                    lc++;
                }
                nc++;
            }
        }
        if(verb)
        {
            System.out.println("Generated grid with " + nc + " nodes and " + (double)lc/2d + " links.");
        }
        return nodes;
    }
    
    /**
     * Creates a complete graph
     * @param numberOfNodes number of nodes
     * @return complete graph of numbeOfNodes nodes
     */
    public ArrayList<Node> generateCompleteGraph(int numberOfNodes){
        ArrayList<Node> nodes = new ArrayList<>();
        int linkCounter = 0;
        for(int i=0;i<numberOfNodes;i++)
        {
            Node n = new Node(i);
            nodes.add(n);
        }
        Node N;
        for(int n =0;n<nodes.size();n++)
        {
            N = nodes.get(n);
            for(int l =0;l<nodes.size();l++)
            {
                if(l!=n)
                {
                    Link L = new Link(nodes.get(l));
                    N.addLink(L);
                    linkCounter++;
                }
            }
        }
        if(verb)
        {
            System.out.println("added " + linkCounter + " links.");
        }
        return nodes;
    }
    
    /**
     * checks whether a graph is connected
     * @param nw graph to be tested
     * @return true if connected, false otherwise
     */
    public boolean connected(ArrayList<Node> nw){
        Set<Node> nodes = new HashSet<>();
        for(int i = 1;i<nw.size();i++)
        {
            nodes.add(nw.get(i));
        }
        Stack<Node> S = new Stack<>();
        S.push(nw.get(0));
        Node N;
        while(!S.empty())
        {
            N = S.pop();
            for(int i=0;i<N.getDegree();i++)
            {
                if(nodes.contains(N.getNeighbour(i)))
                {
                    S.push(N.getNeighbour(i));
                    nodes.remove(N.getNeighbour(i));
                }
            }
        }

        return nodes.isEmpty();
    }
    
    /**
     * Creates two ER graphs and connects them with some nodes
     * @param n1 number of nodes in graph 1
     * @param n2 number of nodes in graph 2
     * @param p1 link density in graph 1
     * @param p2 link density in graph 2
     * @param b number of bridges
     * @return network
     */
    public ArrayList<Node> generateBridgedER(int n1,int n2, double p1, double p2, int b){
        ArrayList<Node> c1 = generateConnectedER(n1,p1);
        ArrayList<Node> c2 = generateConnectedER(n2,p2);
        for(int i= 0;i<n2;i++)
        {
            c2.get(i).resetID(i+n1);
        }
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int i=0;i<n1;i++)
        {
            nodes.add(c1.remove(0));
        }
        for(int i=0;i<n2;i++)
        {
            nodes.add(c2.remove(0));
        }
        Random gen = new Random();
        Link link;
        Set<Integer> usedNodes = new HashSet<Integer>();
        int s,d;
        while(usedNodes.size() < 2*b)
        {
            s = gen.nextInt(n1);
            while(usedNodes.contains(s))
            {
                s = gen.nextInt(n1);
            }
            usedNodes.add(s);
            d = n1 + gen.nextInt(n2);
            while(usedNodes.contains(d))
            {
                d = n1 + gen.nextInt(n2);
            }
            usedNodes.add(d);
            link = new Link(nodes.get(s));
            nodes.get(d).addLink(link);
            link = new Link(nodes.get(d));
            nodes.get(s).addLink(link);
        }
        return nodes;
    }
    
    
    private int[] sizeLargestComponent(ArrayList<Node> nw){
        Set<Integer> Q = new HashSet<Integer>();
        ArrayList<Integer> D = new ArrayList<Integer>();
        for(Node N : nw)
        {
            Q.add(N.id);
        }
        Node N;
        int n;
        int q;
        int r;
        int maxS  = 0;
        int maxId = -1;
        while(!Q.isEmpty())
        {
            r = 0;
            Iterator it = Q.iterator();
            q = (Integer)it.next();
            it.remove();
            D.add(q);
            while(!D.isEmpty())
            {
                q  = D.remove(0);
                r++;
                N = nw.get(q);
                for(int i=0;i<N.getDegree();i++)
                {
                    n = N.getNeighbour(i).id;
                    if(Q.contains(n))
                    {
                        Q.remove(n);
                        if(!D.contains(n))
                        {
                            D.add(n);
                        }
                    }
                }
            }
            if(r > maxS)
            {
                maxS = r;
                maxId = q;
            }
        }
        return new int[]{maxS,maxId};
    }
    /**
     * Extracts the connected component that contains node n
     * @param in the network
     * @param n the node
     * @return node n's component
     */
    public ArrayList<Node> extractComponent(ArrayList<Node> in, int n){
        Set<Integer> S = new HashSet<>();
        ArrayList<Integer> D = new ArrayList<>();
        int d;
        D.add(n);
        Node N;
        int p;
        while(!D.isEmpty())
        {
            d = D.remove(0);
            S.add(d);
            N = in.get(d);
            for(int i=0;i<N.getDegree();i++)
            {
                p = N.getNeighbour(i).id;
                if(!S.contains(p))
                {
                    S.add(p);
                    if(!D.contains(p))
                    {
                        D.add(p);
                    }
                }
            }
        }
        Iterator it = S.iterator();
        ArrayList<Node> res = new ArrayList<Node>();
        while(it.hasNext())
        {
            d = (Integer)it.next();
            res.add(in.get(d));
        }
        in.clear();
        for(int i=0;i<res.size();i++)
        {
            res.get(i).id=i;
        }
        return res;
    }
    
    /**
     * Generates a connected Erdos-Renyi grap with a fix average degree
     * @param n number of nodes
     * @param degree average degree
     * @return connected ER graph with fixed specified average degree
     */
    public ArrayList<Node> generateConnectedERFixedAvDegree(int n, double degree){
        ArrayList<Node> nodes;
        double p = degree/(double)(n-1);
        //get rough estimate of largest cluster size
        int c = 0;
        for(int i=0;i<10;i++)
        {
            nodes = generateER(n,p);
            c += sizeLargestComponent(nodes)[0];
        }
        int lc = c / 10;
        System.out.println("Appr. largest component: " + lc);
        int nn = n + (n - lc);
        p = degree/(double)(nn-1);
        System.out.println("Trying with new n = " + nn + " and p = " + p);
        c = 0;
        nodes = generateER(nn,p);
        int[] s = sizeLargestComponent(nodes);
        while(s[0] != n)
        {
            c++;
            nodes = generateER(nn,p);
            s = sizeLargestComponent(nodes);
            if(c > 500)
            {
                System.out.println("I have given it " + c + " tries to generate network. I'm giving up!");
                System.exit(2);;
            }
        }
        System.out.println("Found right network after " + c+ " iterations");
        ArrayList<Node> res = extractComponent(nodes,s[1]);
        return res;
    }
    public ArrayList<Node> generateConnectedER(int n, double p){
        ArrayList<Node> nodes = generateER(n,p);
        while(!connected(nodes))
        {
            nodes = generateER(n,p);
        }
        return nodes;
    }
    
    /**
     * Generates an Erdos-Renyi graph
     * @param n number of nodes
     * @param p link density
     * @return ER graph
     */
    public ArrayList<Node> generateER(int n,double p){
        int linkCounter = 0;
        int v = 1;
        int w = -1;
        double r;
        long start = System.currentTimeMillis();
        Random gen = new Random();
        ArrayList<Node> nodes = new ArrayList<Node>();
        Link link;
        for(int i=0;i<n;i++)
        {
            Node newnode = new Node(i);
            nodes.add(newnode);
        }
        while(v<n)
        {
            r = gen.nextDouble();
            w = w + 1 + (int)Math.floor( Math.log(1d-r) / Math.log(1d-p)) ;
            while( (w >= v) && (v<n))
            {
                w = w - v;
                v++;
            }
            if(v < n)
            {
                link = new Link(nodes.get(w));
                nodes.get(v).addLink(link);
                link = new Link(nodes.get(v));
                nodes.get(w).addLink(link);
                linkCounter++;
            }
        }
        if(verb)
        {
            System.out.println("Generated ER with N = " + nodes.size() + " and L = " + linkCounter + " in " + (System.currentTimeMillis()-start) + " ms.");
        }
        return nodes;
    }
    
    /**
     * Generates a preferential attachment graph
     * @param n number of nodes
     * @param d links added per node
     * @return PA graph
     */
    public ArrayList<Node> generatePA(int n, int d){
        int[] M = null;
        int cf = 0;
        
        if(d > 2)
        {
            M = new int[(2*(n-d)*d) + 2*d];
            cf = d*(d-1);
        }
        if(d == 2)
        {
            M = new int[(4*(n-2)) + 2];
            cf = 3;
        }
        if(d == 1)
        {
            M = new int[2*(n-1)];
            cf = 1;
        }

        int r = -1;
        int linkCounter = 0;
        long start = System.currentTimeMillis();
        Random gen = new Random();
        ArrayList<Node> nodes = new ArrayList<>();
        Link link;
        Set<Integer> addedLinks = new HashSet<>();
        for(int i=0;i<n;i++)
        {
            Node newnode = new Node(i);
            nodes.add(newnode);
        }
        // init the first nodes as ring
        if(d > 1)
        {
            for(int i =0;i<d-1;i++)
            {
                M[(2*i)] = i;
                M[(2*i)+1] = i+1;
            }
            if(d > 2)
            {
                M[(2*(d-1))] = d-1;
                M[(2*(d-1))+1] = 0;
            }
        }
        for(int v =d;v<n;v++)
        {
            for(int i=0;i<d;i++)
            {
                M[2*((v*d) + i - cf)]  = v;
                if(2*((v*d) + i - cf) == 0)
                {
                    M[0] = 0;
                    M[1] = 1;
                    continue;
                }
                else
                {
                    r = gen.nextInt(2*((v*d) + i - cf));
                }
                addedLinks.add(v);
                while(addedLinks.contains(M[r]))
                {
                    r = gen.nextInt(2*((v*d) + i - cf));
                }
                addedLinks.add(M[r]);
                M[(2*((v*d) + i - cf)) + 1] = M[r];
            }
            addedLinks.clear();
        }
        for(int i=0;i<M.length/2;i++)
        {
            if(M[2*i] != M[(2*i)+1])
            {
                    link = new Link(nodes.get(M[(2 * i) + 1]));
                    nodes.get(M[2*i]).addLink(link);
                    link = new Link(nodes.get(M[2*i]));
                    nodes.get(M[(2*i)+1]).addLink(link);
                    linkCounter++;
            }
        }
        if(verb)
        {
            System.out.println("Generated PA with N = " + nodes.size() + " and L = " + linkCounter + " in " + (System.currentTimeMillis()-start) + " ms.");
        }
        return nodes;
    }
    
    /**
     * Generates a double star graph connected by a single link
     * @param k number of nodes per star
     * @param type connection type 0: centre-centre, 1:leaf-leaf 2:centre-leaf
     * @return double star graph
     */
    public ArrayList<Node> generateDoubleStart(int k, int type){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int i =0;i<2*k;i++)
        {
            nodes.add(new Node(i));
        }
        Link link;
        // Create first star with centre node k-1
        for(int i=0;i<k-1;i++)
        {
            link = new Link(nodes.get(i));
            nodes.get(k-1).addLink(link);
            link = new Link(nodes.get(k-1));
            nodes.get(i).addLink(link);
        }
        // Create second start with centre node (2*k) - 1
        for(int i=k;i<(2*k)-1;i++)
        {
            link = new Link(nodes.get(i));
            nodes.get((2*k)-1).addLink(link);
            link = new Link(nodes.get((2*k)-1));
            nodes.get(i).addLink(link);
        }
        // Connect stars
        switch (type)
        {
            case 0: // center centre
                link = new Link(nodes.get(k-1));
                nodes.get((2*k)-1).addLink(link);
                link = new Link(nodes.get((2*k)-1));
                nodes.get(k-1).addLink(link);
            break;
            case 1: // leaf leaf
                link = new Link(nodes.get(0));
                nodes.get(k).addLink(link);
                link = new Link(nodes.get(k));
                nodes.get(0).addLink(link);
            break;
            case 2: // leaf centre
                link = new Link(nodes.get(0));
                nodes.get((2*k)-1).addLink(link);
                link = new Link(nodes.get((2*k)-1));
                nodes.get(0).addLink(link);
        }
        return nodes;
    }
    public void setTempLocation(String in){
        tempLocation = in;
    }
    
    /**
     * Returns a connected random geometric graph
     * @param n number of nodes
     * @param r diameter or -1 to used 1.5 times the connection threshold
     * @return A connected random geometric graph
     */
    public ArrayList<Node> generateRGG(int n, double r){
        long start = System.currentTimeMillis();
        File F = new File(tempLocation);
        if(!F.exists())
        {
            F.mkdir();
        }
        Random gen = new Random();
        int num = gen.nextInt(Integer.MAX_VALUE);
        File script = new File(F.getAbsolutePath() + F.separator +"Rscript"+num+".R");
        String Rout = F.getAbsolutePath() + F.separator +"Rscript"+num+".Rout";
        String res = F.getAbsolutePath() + F.separator +"Rres" + num + ".net";
        res = res.replace("\\", "/");
        String command = "library(\"igraph\")\n"
                + "n = " + n + "\n"
                + "g <- grg.game(n,1.5*(sqrt(log(n)/(n*pi))))\n"
                + "while(no.clusters(g) != 1)\n"
                + "g <- grg.game(n,1.5*(sqrt(log(n)/(n*pi))))\n"
                + "write.graph(g, \""+res+"\",format=\"pajek\" )";
        try
        {
            // Write script
            BufferedWriter out = new BufferedWriter(new FileWriter(script));
            out.write(command);
            out.flush();
            out.close();
            // Excecute script
            Runtime ru = Runtime.getRuntime();
            String com = "R CMD BATCH " + script + " " + Rout;
            System.out.println(com);
            Process p = ru.exec(com);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line=null;
            while((line=input.readLine()) != null) 
            {
                System.out.println(line);
            }
            p.waitFor();
            // Read results
            ArrayList<Node> network = readPajek(res);
            // Delete files
//          if(verb)
            {
                System.out.println("That took " + (System.currentTimeMillis()-start) + " ms");
            }
            return network;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Generates a star graph with a certain arm length. Basically this is
     * a graph consisting of path graphs connected at one end
     * @param arms number of arms
     * @param armLength length of arms
     * @return star graph
     */
    public ArrayList<Node> generateStar(int arms, int armLength){
        int numberOfNodes = (arms * armLength) + 1;
        ArrayList<Node> nodes = new ArrayList<Node>();
        Link link;
        for(int i=0;i<numberOfNodes;i++)
        {
            Node newnode = new Node(i);
            nodes.add(newnode);
        }
        // First construct the arms
        int c =0;
        int prev = 0;
        for(int i = 1;i<nodes.size();i++)
        {
            link = new Link(nodes.get(i));
            nodes.get(prev).addLink(link);
            link = new Link(nodes.get(prev));
            nodes.get(i).addLink(link);
            c++;
            prev = i;
            if(c == armLength)
            {
                c = 0;
                prev = 0;
            }
        }
        return nodes;
    }
    
    /**
     * Geneates a path graph
     * @param n length of path
     * @return path graph
     */
    public ArrayList<Node> generatePath(int n){
        ArrayList<Node> nodes = new ArrayList<Node>();
        Link link;
        int nc = 0;
        int lc = 0;
        for(int i=0;i<n;i++)
        {
            Node newnode = new Node(i);
            nodes.add(newnode);
            nc++;
        }
        for(int i=0;i<n-1;i++)
        {
            link = new Link(nodes.get(i+1));
            nodes.get(i).addLink(link);
            link = new Link(nodes.get(i));
            nodes.get(i+1).addLink(link);
            lc++;
        }
        System.out.println("Created grid with " + nc + " nodes and " + lc + " links.");
        return nodes;
    }
    
    /**
     * Reads a graph from the pajek file format
     * @param file file location
     * @return graph read from file
     */
    public ArrayList<Node> readPajek(String file){
        ArrayList<Node> nodes = new ArrayList<Node>();
        String line;
        String[] parts;
        int s;
        int d;
        int n;
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            line = in.readLine(); //reads the *vertices line
            parts = line.split(" ");
            n = Integer.parseInt(parts[1]);
            for(int i=0;i<n;i++)
            {
                nodes.add(new Node(i));
            }
            if(verb)
            {
                System.out.println("Created " + nodes.size() + " nodes. On to creating links...");
            }
            line = in.readLine(); // edges
            int lc = 0;
            while((line = in.readLine()) != null)
            {
                parts = line.split(" ");
                s = Integer.parseInt(parts[0]) -1;
                d = Integer.parseInt(parts[1]) -1;
                nodes.get(s).addLink(new Link(nodes.get(d)));
                nodes.get(d).addLink(new Link(nodes.get(s)));
                lc++;
                if(verb && lc % 10000 == 0)
                {
                    System.out.println("Created " + lc + " links");
                }
            }
            in.close();
            System.out.println("Finished.");
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        return nodes;
    }
    
    /**
     * Writes the graph to a simple adjaceny list
     * @param network network to be written to file
     * @param file file location
     */
    public void writeToSimpleAdjacencyList(ArrayList<Node> network, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            Set<String> links = new HashSet<String>();
            for(Node N : network)
            {
                if(N.id % 1000 == 0)
                {
                    System.out.println("Node " + N.id);
                }
                for(int i=0;i<N.getDegree();i++)
                {
                    if(!links.contains((N.getNeighbour(i).id + " " + N.id)))
                    {
                        links.add((N.id + " " + N.getNeighbour(i).id));
                        out.write(N.id + " " + N.getNeighbour(i).id + "\n");
                    }
                }
            }
            out.flush();
            out.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Reads a graph from the utis adjacency list format
     * @param file file location
     * @return network
     */
    public ArrayList<Node> readFromAdjacencyList(String file){
        ArrayList<Node> network = new ArrayList<Node>();
        boolean start = false;
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            int s;
            int d;
            String[] parts;
            while((line = in.readLine())!= null)
            {
                if(start)
                {
                    parts = line.split(" ");
                    s = Integer.parseInt(parts[0]);
                    d = Integer.parseInt(parts[1]);
                    network.get(s).addLink(new Link(network.get(d)));
                    network.get(d).addLink(new Link(network.get(s)));
                    continue;
                }
                if(line.contains("#Number of nodes"))
                {
                    int n = Integer.parseInt(in.readLine());
                    for(int i=0;i<n;i++)
                    {
                        network.add(new Node(i));
                    }
                }
                if(line.contains("#Links"))
                {
                    start = true;
                }
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return network;
    }
    
    /**
     * Writes a simple adjacency list where each node is written for
     * each endpoint
     * @param network the network
     * @param file file location
     */
    public void writeDoubleList(ArrayList<Node> network, String file){
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(Node N : network)
            {
                for(int i=0;i<N.getDegree();i++)
                {
                    out.write(N.id +"," + N.getNeighbour(i).id + "\n");
                }
            }
            out.flush();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes a graph to the utils adjacency list format
     * @param network the network
     * @param file file location
     */
    public void writeToAdjacencyList(ArrayList<Node> network, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("#Number of nodes:\n" + network.size() + "\n");
            out.write("#Node degrees:\n");
            for(int i=0;i<network.size();i++)
            {
                out.write(i + " " + network.get(i).getDegree() + "\n");
            }
            out.write("#Links:\n");
            Set<String> links = new HashSet<String>();
            for(Node N : network)
            {
                for(int i=0;i<N.getDegree();i++)
                {
                    if(!links.contains((N.getNeighbour(i).id + " " + N.id)))
                    {
                        links.add((N.id + " " + N.getNeighbour(i).id));
                        out.write(N.id + " " + N.getNeighbour(i).id + "\n");
                    }
                }
            }
            out.flush();
            out.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Reads a network from an adjacency matrix
     * @param file file location of the adjacency matrix
     * @return network
     */
    public ArrayList<Node> readFromAdjacencyMatrix(String file){
        ArrayList<Node> nodes = new ArrayList<>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            String[] parts;
            line = in.readLine();
            parts = line.split(" ");
            String p;
            int s =0;
            for(int i=0;i<parts.length;i++)
            {
                nodes.add(new Node(i));
            }
            int d;
            for(int i=0;i<parts.length; i++)
            {
                p = parts[i];
                d = Integer.parseInt(p);
                if(d == 1)
                {
                    nodes.get(s).addLink(new Link(nodes.get(i)));
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
                        nodes.get(s).addLink(new Link(nodes.get(i)));
                    }
                }
            }
            in.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodes;
    }
    
    /**
     * Reads a network from and simple list file trying different seperators
     * 
     * @param file file location
     * @return network
     */
    public ArrayList<Node> generateFromFile(String file){
        ArrayList<Node> nodes = new ArrayList<Node>();
        Map<Integer, Integer> M = new HashMap<Integer,Integer>();
        int idCounter = 0;
        try
        {
            String line;
            int s;
            int d;
            BufferedReader in = new BufferedReader(new FileReader(file));
            int separator = -1;
            String[] separators = new String[]{" ",",","\t"};
            while((line = in.readLine()) != null)
            {
                if(separator == -1)
                {
                    separator++;
                    while(separator < separators.length && line.split(separators[separator]).length != 2)
                    {
                        separator++;
                    }
                    if(separator >= separators.length)
                    {
                        System.out.println("No suitable separator found.");
                        System.exit(22);
                    }
                    else
                    {
                        System.out.println("Using separator : '" + separators[separator] + "'");

                    }
                }
                if(!line.startsWith("#"))
                {
                  s = Integer.parseInt(line.split(separators[separator])[0]);
                  d = Integer.parseInt(line.split(separators[separator])[1]);
                  if(s!=d)
                  {
                      if(!M.containsKey(s))
                      {
                          M.put(s, idCounter);
                          idCounter++;
                      }
                      if(!M.containsKey(d))
                      {
                          M.put(d, idCounter);
                          idCounter++;
                      }
                  }
                }
            }
            in.close();
            for(int i=0;i<idCounter;i++)
            {
                nodes.add(new Node(i));
            }
            Link link;
            in = new BufferedReader(new FileReader(file));
            while((line = in.readLine()) != null)
            {
                if(!line.startsWith("#"))
                {
                  s = Integer.parseInt(line.split(separators[separator])[0]);
                  d = Integer.parseInt(line.split(separators[separator])[1]);
                  if(s!=d)
                  {
                     link = new Link(nodes.get(M.get(d)));
                     nodes.get(M.get(s)).addLink(link);
                     link = new Link(nodes.get(M.get(s)));
                     nodes.get(M.get(d)).addLink(link);
                  }
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(NetworkGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodes;
    }
    private boolean verb = false;
    private String tempLocation = "c:/temp/";
}