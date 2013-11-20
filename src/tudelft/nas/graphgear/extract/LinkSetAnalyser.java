/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.extract;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.Frequency;
import tudelft.nas.graphgear.linkset.LinkSet;
import tudelft.nas.graphgear.linkset.LinkSetNode;
import tudelft.nas.graphgear.linkset.StreamedLinkSet;
import tudelft.nas.graphgear.utils.Analyser;
import tudelft.nas.graphgear.utils.General;
import tudelft.nas.graphgear.utils.Node;


/**
 *
 * @author rvandebovenkamp
 * The linksetanalyser class contains a collection of tools to analyse
 * and compare linksets.
 */
public class LinkSetAnalyser {
    /**
     * Checks wether a character is a number
     * @param in character
     * @return true if number, false otherwise
     */
    public boolean isNumber(int in){
        return (in >= 48 && in <= 57);
    }
    /**
     * Finds the first number in a string
     * @param in string
     * @return the first number found in the string or -1 if none found
     */
    public int findNumberInString(String in){
        int pos = 0;
        int start = -1;
        int stop = -1;
        while(pos < in.length() )
        {
            if(isNumber(in.codePointAt(pos)))
            {
                if(start == -1)
                {
                    start = pos;
                }
            }
            else
            {
                if(start != -1 && stop == -1)
                {
                    stop = pos;
                }
            }
            pos++;
        }
        if(stop == -1)
        {
            stop = in.length();
        }
        return Integer.parseInt(in.substring(start, stop));
    }
    
    /**
     * Analyses the link weight distribution of all linksets in the
     * given director. A new subdirectory will be created to store
     * the results. All linkset files should end in .res
     * @param dir the directory containing link set files
     */
    public void AnalyseDirectory(String dir){
        File F = new File(dir);
        String[] fi = F.list();
        String of ;
        File fil = new File(dir + "/dist/");
        if(!fil.exists())
        {
            fil.mkdir();
        }
        for(String f : fi)
        {
            if(new File(f).isDirectory())
                continue;
            if(!f.endsWith(".res"))
                continue;
            of = dir + "/dist/" + f.substring(0,f.length()-4) + "_dist.txt";
            StreamedLinkSet L = new StreamedLinkSet(dir+"/"+f);
            LinkSetNode n;
            L.initTreeTraversal();
            Frequency Fr = new Frequency();
            Set<Integer> S = new HashSet<Integer>();
            System.out.println("Processing " + f);
            while((n=L.getNextInOrder()) != null)
            {
                Fr.addValue(n.w);
                S.add(n.w);
            }
            
            List<Integer> k = General.asSortedList(S);
            try 
            {
                FileWriter out = new FileWriter(of);
                System.out.println("Writing to: " + of);
                for(int i:k)
                {
                   out.write(i + " " + Fr.getCumPct(i) + " " + Fr.getPct(i) + " " + Fr.getCount(i) + " \n");
                }
                out.flush();
                out.close();
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(LinkSetAnalyser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Takes two streamed link sets and outputs which nodes and links
     * are present in one set but not in the other
     * @param setA linkset A
     * @param setB linkset B
     * @param t the threshold value to consider (only links with weights higher are counted)
     * @param reverse check B against A.
     */
    public void compareStreamedLinkSets(String setA, String setB, int t, boolean reverse)
    {
        StreamedLinkSet A = new StreamedLinkSet(reverse ? setB : setA);
        StreamedLinkSet B = new StreamedLinkSet(reverse ? setA : setB);
        //First create in memory linkset of set A
        //And node set of set A
        System.out.println("Creating in-memory link and node set of set " + (reverse ? "B" : "A"));
        Set<Integer> NA = new HashSet<Integer>();
        LinkSet L = new LinkSet();
        A.initTreeTraversal();
        LinkSetNode a;
        while((a = A.getNextInOrder())!= null)
        {
            if(a.w >= t)
            {
                if(!L.addLink(a))
                {
                    System.out.println("link " + a+ " twice in LinkSet!");
                }
                NA.add(a.s);
                NA.add(a.d);
            }
        }
        System.out.println("LinkSet now contains " + L.size() + " links");
        System.out.println("Node set now contains " + NA.size() + " nodes");
        //Count the number of nodes and links that are in B but not in A
        if(reverse)
        {
            System.out.println("Counting the nodes and links in A that are not in B");
        }
        else
        {
            System.out.println("Counting the nodes and links in B that are not in A");
        }
        B.initTreeTraversal();
        Set<Integer> NB = new HashSet<Integer>();
        int linkInCounter = 0;
        int linkOutCounter = 0;
        // Count the number of links that are not in the target set
        while((a = B.getNextInOrder())!= null)
        {
            if(a.w >= t)
            {
                NB.add(a.s);
                NB.add(a.d);
                if(L.addLink(a))
                {
                    linkOutCounter++;
                }
                else
                {
                    linkInCounter++;
                }
            }
        }
        // Count the number of nodes that are not in the target set
        int nodeInCounter = 0;
        int nodeOutCounter = 0;
        for(int b : NB)
        {
            if(NA.contains(b))
            {
                nodeInCounter++;
            }
            else
            {
                nodeOutCounter++;
            }
        }
        if(reverse)
        {
            System.out.println("Set A contains " + linkOutCounter + " links NOT in B and " + linkInCounter + " links in B");   
            System.out.println("Set A contains " + nodeOutCounter + " nodes NOT in B and " + nodeInCounter + " nodes in B");   
        }
        else
        {
            System.out.println("Set B contains " + linkOutCounter + " links NOT in A and " + linkInCounter + " links in A");
            System.out.println("Set B contains " + nodeOutCounter + " nodes NOT in A and " + nodeInCounter + " nodes in A");
        }
        L.clear();
        A.close();
        B.close();
    }
    
    /**
     * Helper method to compare both linksets against each other
     * @param setA linkset A
     * @param setB linkset B
     * @param t threshold value
     */
    public void compareStreamedLinkSets(String setA, String setB, int t){
        compareStreamedLinkSets(setA,setB,t,false);
        compareStreamedLinkSets(setA,setB,t,true);
    }
    
    /**
     * Check whether network network contains node node
     * @param network network under test
     * @param node node to be found
     * @return true if node @param node is in network @param network, false otherwise
     */
    private boolean containsNode(Node[] network, int node){
        for(Node N : network)
        {
            if(N.name == node)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds the cluster that contains node @param node from an array of clusters
     * @param clusters the array of clusters
     * @param node the node
     * @return the cluster containing node @node as network
     */
    private Node[] findNodeInCluster(Node[][] clusters, int node){
        for(Node[] cluster : clusters)
        {
            if(containsNode(cluster,node))
            {
                for(Node N : cluster)
                {
                    return cluster;
                }
            }
        }
        return null;
    }
    
    /**
     * Prints the network view of the cluster that contains @param node
     * @param set linkset to be used
     * @param threshold thresholds to be applied
     * @param node node to be found
     */
    public void printClusterOfNode(String set, int threshold, int node){
        String tempFile = "d:/temp/adjl.tmp";
        General u = new General();
        
        StreamedLinkSet S = new StreamedLinkSet(set);
        S.writeAdjList(tempFile, threshold);
        
        
        Node[] network = u.readNetworkFromAdjacencyList(tempFile);
        Node[][] clusters = u.getComponentsAsNetworks(network);
        Node[] cluster = findNodeInCluster(clusters,node);
        
        printNetwork(cluster);
    }
    
    /**
     * Prints the network in @param network to the console
     * @param network network to be printed
     */
    private void printNetwork(Node[] network){
        for(Node N : network)
        {
            System.out.println(N);
        }
    }
    
    /**
     * Returns the network view of the cluster of node @param node
     * @param set the linkset to be used
     * @param thresholds the thresholds to be used
     * @param node the node to be found
     * @return the network view of the cluster of node @param node applying thresholds @thresholds taken from
     * linkset @set
     */
    private Node[] getNodeInCluster(String set, int threshold, int node){
        String tempFile = "d:/temp/adjl.tmp";
        General u = new General();
        StreamedLinkSet S;
        Node[] network;
        Node[][] clusters;
        Node[] cluster; 
        
        S = new StreamedLinkSet(set);
        S.writeAdjList(tempFile, threshold);
        S.close();
        
        network = u.readNetworkFromAdjacencyList(tempFile);
        clusters = u.getComponentsAsNetworks(network);
        
        return findNodeInCluster(clusters,node);
    }
    
    /**
     * Computes the fraction of nodes in the largest component
     * for successive threshold values and outputs them to the console
     * @param set streamed link set location
     */
    public void largestComponentAnalysis(String set){
        StreamedLinkSet sls = new StreamedLinkSet(set);
        sls.fromMemory = true;
        LinkSetNode N;
        sls.initTreeTraversal();
        int max = 0;
        Set<Integer> players = new HashSet<Integer>();
        while((N=sls.getNextInOrder())!= null)
        {
            max = N.w < max ? max : N.w;
            players.add(N.s);
            players.add(N.d);
        }
        System.out.println("max weight: " + max);
        General u = new General();
        int w = 6;
        int s = 0;
        while(w >= 0)
        {
            s = u.extractLargestComponent(u.createNetworkFromLinkSet(sls, w, new HashSet<Integer>())).length;
            System.out.println(w + " " + (double)s/(double)players.size());
            w -= 2;
        }
    }
    
    /**
     * Helper method to use a map as counter
     * @param M map
     * @param id id to be added or incremented
     */
    private void addInMap(Map<Integer,Integer> M, int id){
        int v = M.containsKey(id) ? (M.get(id) + 1) : 1;
        M.put(id,v);
    }

    
    /**
     * Prints the directed degree distribution to the console
     * @param L streamedlinkset location
     */
    public void printDirectedDegreeDistribution(LinkSet L){
        
        LinkSetNode N;
        L.initTreeTraversal();
        Map<Integer,Integer> inDegree = new HashMap<Integer,Integer>();
        Map<Integer,Integer> outDegree = new HashMap<Integer,Integer>();
        Set<Integer> nodeSet = new HashSet<Integer>();
        int d;
        while((N=L.getNextInOrder()) != null)
        {

            nodeSet.add(N.s);
            nodeSet.add(N.d);
            switch(N.w)
            {
                case 3: // bidirectional
                    addInMap(inDegree,N.s);
                    addInMap(inDegree,N.d);
                    
                    addInMap(outDegree,N.s);
                    addInMap(outDegree,N.d);
                    break;
                case 1: // source -> dest
                    addInMap(outDegree,N.s);
                    addInMap(inDegree,N.d);
                    break;
                case 2: // dest -> source
                    addInMap(outDegree,N.d);
                    addInMap(inDegree,N.s);
                    break; 
            }
        }
        int ind;
        int outd;
        for(int i : nodeSet)
        {
            ind = inDegree.containsKey(i) ? inDegree.get(i) : 0;
            outd = outDegree.containsKey(i) ? outDegree.get(i) : 0;
            System.out.println(i + " " + ind + " " + outd + " " + (double)ind/(double)(ind+outd));
        }
    }
    
    /**
     * Writes the duration of all matches to a file
     * @param input streamed link set
     * @param output output file
     */
    public void writeMatchDuration(String input, String output){
        BufferedWriter out = null;
        try {
            MatchData M = new MatchData(input);
            Match m;
            int c = 0;
            out = new BufferedWriter(new FileWriter(output));
            while((m = M.getNext()) != null)
            {
                if(m.getDuration() > 0 && m.winner != -2)                
                {
                    out.write(m.getDuration() + "\n");
                }
            }
            M.close();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(LinkSetAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prints all matches of a certain player to the console
     * @param player player id
     * @param data matchdata
     */
    public void printPlayerGames(int player, String data){
        MatchData M = new MatchData(data);
        Match m;
        while((m=M.getNext())!= null)
        {
            if(m.containsPlayer(player))
            {
                System.out.println(m.timeAndPlayers());
            }
        }
    }
    

    /**
     * Grows a network by continously lowering the threshold from the heaviest
     * link that the seed has until the network around the seed has a maximum of
     * limit nodes.
     * @param set The linknodeset
     * @param node seed node
     * @param limit maximum number of nodes in the returned network
     */
    public void growNetworkFromSeed(String set, int node, int limit, int nodeWeight){
        General u = new General();
        LinkSetNode N;
        StreamedLinkSet sls = new StreamedLinkSet(set);
        sls.initTreeTraversal();
        // Go over all links to find the heaviest
        int maxWeight = 0;
        LinkSet linkSet = new LinkSet();
        Map<Integer,Integer> nodeWeights = new HashMap<Integer,Integer>();
        int nw;
        while((N=sls.getNextInOrder())!= null)
        {
            if(N.s==node || N.d==node)
            {
                maxWeight = maxWeight > N.w ? maxWeight : N.w;
            }
            if(nodeWeights.containsKey(N.s))
            {
                nw = nodeWeights.get(N.s)+1;
            }
            else
            {
                nw = 1;
            }
            nodeWeights.put(N.s,nw);
            if(nodeWeights.containsKey(N.d))
            {
                nw = nodeWeights.get(N.d)+1;
            }
            else
            {
                nw = 1;
            }
            nodeWeights.put(N.d,nw);
        }
        Set<Integer> forbidden = new HashSet<Integer>();
        for(int i : nodeWeights.keySet())
        {
            if(nodeWeights.get(i) < nodeWeight)
            {
                forbidden.add(i);
            }
        }
        System.out.println("Heaviest link for seed: " + maxWeight);
        int linkWeight = maxWeight;
        int componentSize =0;
        Node[] component;
        while(linkWeight > 0 && componentSize <= limit)
        {
            component = u.getComponentOfNode(u.createNetworkFromLinkSet(sls, linkWeight, forbidden), node);
            if(componentSize < component.length)
            {
                System.out.println("==="+linkWeight+"===");
                u.printNetwork(component);
                componentSize = component.length;
            }
            linkWeight -= 10;
            System.out.println("weight: " + linkWeight);
        }
        
    }
    
   
    /**
     * Writes an adjacency list limited to the nodes in the set
     * @param nodes nodes that the adjacency list should be limited to
     * @param set streamed link set containing the links
     * @param output destination file
     */
    public void writeSimpleAdjacenclyListOfNodeSet(Set<Integer> nodes, String set, String output, boolean weighted){
        StreamedLinkSet SLS = new StreamedLinkSet(set);
        SLS.initTreeTraversal();
        LinkSetNode N;
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(output));
            while((N = SLS.getNextInOrder())!= null)
            {
                if(nodes.contains(N.s) && nodes.contains(N.d))
                {
                    out.write(N.s + "," + N.d + (weighted ? "," + N.w + "\n" : "\n"));
                }
            }
            out.flush();
            out.close();
            SLS.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Reads a file containing a simple integer map. The keys will be the first integer
     * on every line and the values the second integer in normal order or reversed when specified.
     * @param map The location of the file that contains the map
     * @param reversed true: map is read in reversed order i.e. keys and values are swapped when reading the file
     * @return an integer HashMap containing the data from @param map
     */
    public Map<Integer,Integer> readMap(String map, boolean reversed){
        Map<Integer,Integer> M = new HashMap<Integer,Integer>();
        try
        {
            // Create map
            BufferedReader in = new BufferedReader(new FileReader(map));
            String line;
            
            String[] parts;
            while((line = in.readLine())!= null)
            {
                parts = line.split(" ");
                if(reversed)
                {
                    M.put(Integer.parseInt(parts[1].trim()),Integer.parseInt(parts[0].trim()));
                }
                else
                {
                    M.put(Integer.parseInt(parts[0].trim()),Integer.parseInt(parts[1].trim()));
                }
            }
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return M;
    }
    
    /**
     * Takes a set of node ids and returns the set of node names
     * @param nodes Set of node ids
     * @param map the map that should be used to map the nodes to names
     * @return set of names of the nodes in the set @param nodes
     */
    public Set<Integer> mapBackToNodeNames(Set<Integer> nodes, String map){
        Map<Integer,Integer> M = readMap(map,false);
        Set<Integer> res = new HashSet<Integer>();
        for(int i : nodes)
        {
            res.add(M.get(i));
        }
        return res;
    }
    
    /**
     * Returns a set of Blondel Node ids of all the nodes that are members of one
     * of the clusters specified in the set @param clusters
     * @param clusters The set of Blondel clusters that we want the members of
     * @param clusterMembership Blondel's tree file that contains a mapping from node
     * id to cluster id
     * @return Set of all members of the clusters specified in @param clusters
     */
    public Set<Integer> getClusterMembers(Set<Integer> clusters, String clusterMembership){
        Map<Integer,Integer> CM = readMap(clusterMembership, false);
        Set<Integer> clusterMembers = new HashSet<Integer>();
        Map<Integer,Integer> MemCount = new HashMap<Integer,Integer>();
        for(int i : clusters)
        {
            MemCount.put(i, 0);
        }
        for(int i : CM.keySet())
        {
            if(clusters.contains(CM.get(i)))
            {
                clusterMembers.add(i);
                int c = MemCount.get(CM.get(i));
                MemCount.put(CM.get(i),++c);
            }
        }
        for(int i : clusterMembers)
        {
            System.out.println(i);
        }
        System.out.println("Number of nodes per cluster: ");
        List<Integer> ks = new ArrayList<Integer>(MemCount.keySet());
        Collections.sort(ks);
        for(int i : ks)
        {
            System.out.println(i + ": " + MemCount.get(i));
        }
        return clusterMembers;
    }
    
    /**
     * Prints the distribution of connected component sizes to the console
     * @param set streamed link set file
     * @param threshold theshold to be used
     */
    public void printComponentSizeDistribution(String set, int threshold){
        String tempFile = "d:/temp/adjl.tmp";
        StreamedLinkSet S = new StreamedLinkSet(set);
        S.writeAdjList(tempFile, threshold);
        General u = new General();
        Node[] network = u.readNetworkFromAdjacencyList(tempFile);
        Node[][] clusters = u.getComponentsAsNetworks(network);
        int ci = 1;
        Analyser A = new Analyser();
        for(int i=0;i<clusters.length-1;i++)
        {
            Node[] cluster = clusters[i];
            A.addToBin(0, cluster.length, 1);
        }
        
        int[][] bins = A.getBins(0);
        int sum = 0;
        for(int i=0;i<bins.length;i++)
        {
            sum += bins[i][1]*bins[i][0];
        }
        for(int i=0;i<bins.length;i++)
        {
            System.out.println(bins[i][0] + " " + " " + bins[i][1] + " " + (double)(bins[i][0]*bins[i][1])/(double)sum);
        }
        System.out.println("Nodes in small clusters: " + sum);
        System.out.println("Nodes in largest cluster: " + bins[bins.length-1][0]);
    }
    
    /**
     * Prints all links in a streamed link set to the console
     * @param set streamed link set
     * @param threshold threshold to be used
     */
    public void printLinkSet(String set, int threshold){
        StreamedLinkSet SLS = new StreamedLinkSet(set);
        LinkSetNode N = null;
        SLS.initTreeTraversal();
        while((N=SLS.getNextInOrder())!=null)
        {
            System.out.println(N.s + "<->" + N.d+ " " + N.w);
        }
    }
    
    /**
     * Takes a set of node names and finds all Blondel's cluster ids
     * @param nodes the node names to find
     * @param map the map that maps node names to Blondel node IDs
     * @param clusters Blondel's tree file containing the cluster mapping
     * @return a set of cluster ids that cover all the nodes in @param nodes
     */
    public Set<Integer> getClustersIdsBySet(Set<Integer> nodes, String map, String clusters){
        Map<Integer,Integer> NM = readMap(map,true);
        Map<Integer,Integer> CM = readMap(clusters,false);
        Set<Integer> cs = new HashSet<Integer>();
        for(int n : nodes)
        {
            cs.add(CM.get(NM.get(n)));
        }
        return cs;
    }
    
    /** 
     * Writes a file that lists for each node in @param nodes the cluster it belongs to in
     * Blondel's tree file
     * @param nodes set of nodes that have to be found
     * @param map file that contains the mapping of node name to Blondel node id
     * @param clusters file containing Blondel's tree mapping
     * @param output destination file
     */
    public void createClusterMembershipList(Set<Integer> nodes, String map, String clusters, String output){
        try 
        {
            // Create map
            Map<Integer,Integer> M = readMap(map,false);
            String line;
            String[] parts;
            BufferedReader in = new BufferedReader(new FileReader(clusters));
            BufferedWriter out = new BufferedWriter(new FileWriter(output));
            int node;
            while((line = in.readLine())!= null)
            {
                parts = line.split(" ");
                node = M.get(Integer.parseInt(parts[0].trim()));
                if(nodes.contains(node))
                {
                    out.write(node + " " + parts[1].trim() + "\n");
                }
            }
            out.flush();
            out.close();
            in.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(LinkSetAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Writes a weighted adjacency list of all links exceeding the thresholds
     * @param set location of the linkset
     * @param threshold thresholds value to be used
     * @param dest destination file
     */
    public void writeWeightedAdjacencyList(String set, int threshold, String dest){
        StreamedLinkSet SLS = new StreamedLinkSet(set);
        SLS.writeSimpleAdjList(dest, threshold, true);
    }
    
    /**
     * Returns the set of nodes that are in the same cluster as node @param node
     * @param set linkset node to use
     * @param threshold thresholds to use
     * @param node nod to be found
     * @return set of nodes that are in the same cluster as node @param node
     */
    public Set<Integer> getClusterAsNodeSet(String set, int threshold, int node){
        Node[] cluster = getNodeInCluster(set,threshold,node);
        Set<Integer> nodes = new HashSet<Integer>();
        for(Node N : cluster)
        {
            nodes.add(N.name);
        }
        System.out.println("Node set is of size " + nodes.size());
        return nodes;
    }
    
    /**
     * Writes a network to a simple adjacency list
     * @param dest destination file
     * @param network network to be written to file
     */
    public void writeToSimpleAdjacencyList(String dest, Node[] network){
        General u = new General();
        u.writeToSimpleTextListNames(network, dest);
    }
    
    /**
     * Writes the adjacency lists of all clusters/connected components to
     * files if they are of the desired size
     * @param set streamed link set location
     * @param threshold threshold to be used
     * @param size desired component size
     * @param file base name of the result files. Files will be numbered
     */
    public void createAdjacencyListForAllClusters(String set, int threshold, int size, String file){
        String tempFile = "d:/temp/adjl.tmp";
        StreamedLinkSet S = new StreamedLinkSet(set);
        S.writeAdjList(tempFile, threshold);
        General u = new General();
        Node[] network = u.readNetworkFromAdjacencyList(tempFile);
        Node[][] clusters = u.getComponentsAsNetworks(network);
        int ci = 1;
        for(Node[] cluster : clusters)
        {
            if(cluster.length == size)
            {
                u.writeToSimpleTextList(cluster, file+ci+".txt");
                ci++;
            }
        }
    }
    
    
    /**
     * Finds all clusters with size @param size
     * @param set the linkset
     * @param threshold the thresholds on the link weight
     * @param size size of the clusters
     */
    public void findClusters(String set, int threshold, int size){
        String tempFile = "d:/temp/adjl.tmp";
        StreamedLinkSet S = new StreamedLinkSet(set);
        S.writeAdjList(tempFile, threshold);
        General u = new General();
        Node[] network = u.readNetworkFromAdjacencyList(tempFile);
        Node[][] clusters = u.getComponentsAsNetworks(network);
        for(Node[] cluster : clusters)
        {
            if(cluster.length == size)
            {
                for(Node N : cluster)
                {
                    System.out.println(N);
                }
                System.out.println("==");
            }
        }
    }
}
