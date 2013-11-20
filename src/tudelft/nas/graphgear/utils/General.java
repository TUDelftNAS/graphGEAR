/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.utils;

import tudelft.nas.graphgear.linkset.LinkSet;
import tudelft.nas.graphgear.linkset.LinkSetNode;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file contains a group of General util methods, including a few network metrics
 * @author Ruud van de Bovenkamp
 */
public class General {

    /**
     * Prints the element of an array to the console
     * @param A The array to be printed
     */
    public static void printArray(double[] A){
            printArray(A, A.length);
        }
    /**
     * Prints the element of an array to the console up to index
     * @param A the array to be printed
     * @param index the index at which to stop printing
     */
    public static void printArray(double[] A, int index){
            for(int i=0;i<index;i++)
            {
                System.out.print(A[i] + " ");
            }
            System.out.println();
        }
    
    /**
     * Checks whether a character is a number
     * @param in character
     * @return true if number, false otherwise
     */
    private boolean isNumber(int in){
        return (in >= 48 && in <= 57);
    } 
    
    /**
     * Returns a string representation of a double with the given number of digits
     * @param val the double
     * @param digits number of digits
     * @return string representation of value
     */
    public String formatDouble(double val, int digits){
        byte[] format = new byte[digits + 2];
        format[0] = '0';
        format[1] = '.';
        for(int i =2;i<format.length;i++)
        {
            format[i] = '0';
        }
        DecimalFormat df = new DecimalFormat(new String(format));
        return df.format(val).replace(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()), "p");
    }
    
    /**
     * Reads a string input from the console
     * @param question question to be written to the console
     * @return the users' response
     */
    public String CLStringInput(String question){
        Scanner in = new Scanner(System.in);
        System.out.println(question);
        return in.nextLine();
    }
    
    /**
     * Reads an int input from the console
     * @param question question to be written to the console
     * @return the users' response
     */
    public int CLintInput(String question){
        Scanner in = new Scanner(System.in);
        while(true)
        {
            System.out.println(question);
            String answer = in.nextLine();
            try
            {
                int a = Integer.parseInt(answer);
                return a;
            }
            catch(Exception e)
            {
                System.out.println("I'm expecting an interger.");
            }
        }
    }
    
    /**
     * Returns the product of the degrees of every node with every other node in the order of
     * first n-1 from 1 to the rest, then n-2 from 2 to the rest etc. If repeated
     * is true, every product is repeated.
     * @param file file containing the network (either simple list or adjacency matrix)
     * @param repeated true for repeat, false no repeat
     * @return 
     */
    public int[] getDegreeProducts(String file, boolean repeated){
        Node[] network = readNetworkFromSimpleListPreserveIDs(file);
        if(network == null)
        {
            network = readNetworkFromAdjacencyMatrix(file);
        }
        int[] res;
        if(repeated)
        {
            res = new int[network.length*network.length-1];
        }
        else
        {
            res = new int[(network.length*(network.length-1))/2];
        }
        int ind = 0;
        for(int i = 0;i < network.length;i++)
        {
            for(int j= i+1;j<network.length;j++)
            {
                res[ind++] = network[i].links.length*network[j].links.length;
                if(repeated)
                {
                    res[ind++] = network[i].links.length*network[j].links.length;
                }
            }
        }
        return res;
    }
    
    
    /**
     * Returns the hopcounts from every node to every other node in the order of
     * first n-1 from 1 to the rest, then n-2 from 2 to the rest etc. If bothlinks
     * is true, every link is repeated.
     * @param file file containing the network (either simple list or adjacency matrix)
     * @param bothlinks true for both links, false for only 1 link
     * @return 
     */
    public int[] getHopCounts(String file, boolean bothlinks){
        int[][] D;
        Node[] network = readNetworkFromSimpleListPreserveIDs(file);
        if(network == null)
        {
            network = readNetworkFromAdjacencyMatrix(file);
        }
        D = (int[][])pathMetrics(network)[4];
        int[] res;
        if(bothlinks)
        {
            res = new int[D.length*D.length-1];
        }
        else
        {
            res = new int[(D.length*(D.length-1))/2];
        }
        int ind = 0;
        for(int i = 0;i < D.length;i++)
        {
            for(int j= i+1;j<D.length;j++)
            {
                res[ind++] = D[i][j];
                if(bothlinks)
                {
                    res[ind++] = D[i][j];
                }
            }
        }
        return res;
    }
    

    /**
     * Returns the node and link count of a network written in the adjacencylist format
     * @param adjacencyList the file containing the adjacencylist
     * @return [N,L]
     */
    public int[] getNodeAndLinkCount(String adjacencyList){
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(adjacencyList));
            String line;
            line = in.readLine();
            if(!line.equals("#Number of nodes:"))
            {
                return new int[]{-1,-1};
            }
            int[] res = new int[2];
            res[0] = Integer.parseInt(in.readLine().trim());
            in.readLine();
            while(!(line = in.readLine()).equals("#Links:"))
            {
                res[1] += Integer.parseInt(line.split(" ")[1].trim());
            }
            res[1] = res[1]/2;
            in.close();
            return res;
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new int[]{-1,-1};
    }
    
    /**
     * Gets the norm of the eigenvalue estimate vector
     * @param nw the network 
     * @return the norm of the eigenvalue estimate vector
     */
    private double getNorm(Node[] nw){
        double sum = 0d;
        for(Node N : nw)
        {
            sum += Math.pow(N.evcT, 2);
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Computes the leading eigenvalue and vector using the power method. To do this,
     * each node has keeps an eigenvectorcomponent value (N.evc)
     * @param nw the network
     * @param epsilon the desired accuracy
     * @return a leadingEigenPair (Vector and Value)
     */
    public LeadingEigenPair powerMethode(Node[] nw, double epsilon){
        // init ev components
        for(Node N : nw)
        {
            N.evc = 1d;
        }
        double curr = getNorm(nw);
        double prev = 2d;
        while(Math.abs(curr-prev) > epsilon)
        {
            // perform multiplication
            for(Node N : nw)
            {
                for(Node P : N.links)
                {
                    N.evcT += P.evc;
                }
            }
            // keep prev
            prev = curr;
            // normalise
            curr = getNorm(nw);
            for(Node N : nw)
            {
                N.evc = N.evcT/curr;
                N.evcT = 0;
            }
        }
        double[] v = new double[nw.length];
        for(int i=0;i<v.length;i++)
        {
            v[i] = nw[i].evc;
        }
        return new LeadingEigenPair(v,curr);
    }
    
    /**
     * Finds a number in a string
     * @param in string 
     * @return number
     */
    public int findNumberInString(String in){
        return findNumberInString(in,0);
    }
    
    /**
     * Finds a number embedded in a string
     * @param in String in 
     * @param after after which string the search should start
     * @return F the number that was found. If the string does not contain a number
     * things will probably get ugly.
     */
    public int findNumberInString(String in, String after){ 
        return findNumberInString(in,in.lastIndexOf(after));
    }
    /**
     * Finds a number embedded in a string
     * @param in String in 
     * @param pos start position
     * @return the number that was found. If the string does not contain a number
     * things will probably get ugly.
     */
    public int findNumberInString(String in, int pos){
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
        if(start == -1)
        {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(in.substring(start, stop));
    }
    
    /**
     * Determines the size of the union of two sets.
     * @param <T> 
     * @param A Set A
     * @param B Set B
     * @return the size of the union of the two
     */
    public <T> int unionSize(Set<T> A, Set<T> B){
        int res = 0;
        if(A.size() > B.size())
        {
            for(Object o : B)
            {
                if(A.contains(o))
                {
                    res++;
                }
            }
        }
        else
        {
            for(Object o : A)
            {
                if(B.contains(o))
                {
                    res++;
                }
            }
        }
        return res;
    }
    
    /**
     * Prints the element of an array to the console
     * @param A The array to be printed
     */
    public static void printArray(int[] A){
            printArray(A, A.length);
        }
    
    /**
     * Adds a value to the map counter
     * @param M map counter
     * @param key key
     */
    private void addToMapCounter(Map<Integer,Integer> M, int key){
        int val = M.containsKey(key) ? (M.get(key) + 1) : 1;
        M.put(key,val);
    }
    
    /**
     * Returns a matrix containing all the links in the network
     * @param network the network
     * @return matrix containing all links
     */
    public int[][] getLinksFromNetwork(Node[] network){
        int l = 0;
        for(Node N : network)
        {
            l += N.links.length;
        }
        int[][] res = new int[l/2][2];
        int c = 0;
        int s;
        int d;
        for(Node N : network)
        {
            s = N.id;
            for(int i =0;i<N.links.length;i++)
            {
                d = N.links[i].id;
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
     * Shuffles an array
     * @param a the array
     */
    public void shuffleOrder(int[] a){
        Random gen = new Random();
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
     * Creates a network from a directed linkset
     * @param L the linkset
     * @return the network
     */
    public Node[] createNetworkFromDirectedLinkSet(LinkSet L){
       
        LinkSetNode N;
        L.initTreeTraversal();
        Map<Integer,Integer> nameMap = new HashMap<>();
        Set<Integer> nameSet = new HashSet<>();
        Map<Integer,Integer> degreeMap = new HashMap<>();
        // first get all node degrees
        int idCounter =0;
        int s,d = -1;
        int degree = -1;
        while((N=L.getNextInOrder())!= null)
        {
            s = N.s;
            d = N.d;
            nameSet.add(s);
            nameSet.add(d);
            if(!nameMap.containsKey(s))
            {
                nameMap.put(s, idCounter++);
            }
            if(!nameMap.containsKey(d))
            {
                nameMap.put(d, idCounter++);
            }
            switch(N.w)
            {
                case 3: // bidir
                    addToMapCounter(degreeMap,N.s);
                    addToMapCounter(degreeMap,N.d);
                    break;
                case 1: // source -> dest
                    addToMapCounter(degreeMap,N.s);
                    break;
                case 2: // dest -> source
                    addToMapCounter(degreeMap,N.d);
                    break;
            }
        }
        // Create nodes
        Node[] nw = new Node[nameMap.size()];
        int index =0;
        for(int name : nameMap.keySet())
        {
            index = nameMap.get(name);
            nw[index] = new Node(index,degreeMap.get(name) == null ? 0 : degreeMap.get(name));
            nw[index].name = name;
        }
        // Create links
        L.initTreeTraversal();
        while((N=L.getNextInOrder())!= null)
        {
            switch(N.w)
            {
                case 3: //bidir
                    nw[nameMap.get(N.s)].addLink(nw[nameMap.get(N.d)]);
                    nw[nameMap.get(N.d)].addLink(nw[nameMap.get(N.s)]);
                    break;
                case 1 : // source -> dest
                    nw[nameMap.get(N.s)].addLink(nw[nameMap.get(N.d)]);
                    break;
                case 2 : // dest -> source
                    nw[nameMap.get(N.d)].addLink(nw[nameMap.get(N.s)]);
                    break;
            }
            
        }
        return nw;
    }
    /**
     * Creates a network from a linkset
     * @param L the linkset
     * @param threshold the threshold
     * @param forbidden a set of excluded nodes
     * @return the network
     */
    public Node[] createNetworkFromLinkSet(LinkSet L, int threshold, Set<Integer> forbidden){
        if(forbidden == null)
        {
            forbidden = new HashSet<>();
        }
        LinkSetNode N;
        L.initTreeTraversal();
        Map<Integer,Integer> nameMap = new HashMap<>();
        Set<Integer> nameSet = new HashSet<>();
        Map<Integer,Integer> degreeMap = new HashMap<>();
        // first get all node degrees
        int idCounter =0;
        int s,d = -1;
        int degree = -1;
        while((N=L.getNextInOrder())!= null)
        {
            if(N.w < threshold || forbidden.contains(N.s) || forbidden.contains(N.d))
                continue;
            s = N.s;
            d = N.d;
            nameSet.add(s);
            nameSet.add(d);
            if(!nameMap.containsKey(s))
            {
                nameMap.put(s, idCounter++);
            }
            if(!nameMap.containsKey(d))
            {
                nameMap.put(d, idCounter++);
            }
            if(degreeMap.containsKey(s))
            {
                degree = degreeMap.get(s) + 1;
            }
            else
            {
                degree = 1;
            }
            degreeMap.put(s, degree);
            if(degreeMap.containsKey(d))
            {
                degree = degreeMap.get(d) + 1;
            }
            else
            {
                degree = 1;
            }
            degreeMap.put(d, degree);
        }
        // Create nodes
        Node[] nw = new Node[nameMap.size()];
        int index =0;
        for(int name : nameMap.keySet())
        {
            index = nameMap.get(name);
            nw[index] = new Node(index,degreeMap.get(name));
            nw[index].name = name;
        }
        // Create links
        L.initTreeTraversal();
        while((N=L.getNextInOrder())!= null)
        {
            if(N.w < threshold || forbidden.contains(N.s) || forbidden.contains(N.d))
                continue;
            nw[nameMap.get(N.s)].addLink(nw[nameMap.get(N.d)]);
            nw[nameMap.get(N.d)].addLink(nw[nameMap.get(N.s)]);
        }
        return nw;
    }   
    
    /**
     * Converts the 6 LSB bits of an integer to a bit string
     * @param c integer to be converted
     * @return six bit string
     */
    public String to6bitString(int c){
        // Check if LSB is set
        String res = (c%2==0) ? "0" : "1";

        for(int i=0;i<5;i++)
        {
            c = c >> 1;
            res = ((c%2==0) ? "0" : "1") + res;
        }
        return res;    
    }
    
    /**
     * Creates a network from a bitstring
     * @param bs the bit string represents the uppper triangle of the
     * adjacency matrix ordered like this: (0,1),(0,2),(1,2),(0,3),..,(n-1,n)
     * @return network constructed from a bitstring
     */
    private Node[] createNetworkFromBitString(String bs, int n){
        int row = 0;
        int col = 1;
        Node[] nw = new Node[n];
        for(int i=0;i<n;i++)
        {
            nw[i] = new Node(i,1);
        }
        char[] bsa = bs.toCharArray();
        for(char c : bsa)
        {
            if(c == '1')
            {
                nw[row].addLink(nw[col]);
                nw[col].addLink(nw[row]);
            }
            row++;
            if(row == col)
            {
                row = 0;
                col++;
            }
        }
        return nw;
    }
    
    /**
     * Returns the eigenvalue decomposition of the laplacian of the network
     * @param network the network to be used
     * @return the eigenvalue decomposition of the laplacian matrix of the network
     */
    public EigenvalueDecomposition getEVDLaplacian(Node[] network){
        DoubleMatrix2D L = new SparseDoubleMatrix2D(network.length,network.length);
        for(Node N : network)
        {
            for(int i = 0 ;i<N.links.length;i++)
            {
               L.set(N.id, N.links[i].id, -1);
            }
            L.set(N.id,N.id,N.links.length);
        }
        return new EigenvalueDecomposition(L);
    }
    
     /**
     * Creates an adjacency matrix of the network and computes the eigenvalue
     * decomposition   
     * @param list the network
     * @return eigenvalue decomposition of the adjacency matrix of the network
     */
    public EigenvalueDecomposition getEVDAdjacencyFromList(String list){
        // first read list to find number of nodes
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(list));
            String line;
            String[] parts;
            int s,d,max = 0;
            while((line=in.readLine())!= null)
            {
                parts = line.split(" ");
                s = Integer.parseInt(parts[0].trim());
                max = max > s ? max : s;
                d = Integer.parseInt(parts[1].trim());
                max = max > d ? max : d;
            }
            DoubleMatrix2D A = new SparseDoubleMatrix2D(max+1,max+1);
            in.close();
            in = new BufferedReader(new FileReader(list));
            while((line=in.readLine())!= null)
            {
                parts = line.split(" ");
                s = Integer.parseInt(parts[0].trim());
                d = Integer.parseInt(parts[1].trim());
                A.set(s, d, 1);
                A.set(d, s, 1);
            }
            in.close();
            return new EigenvalueDecomposition(A);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(22);
        }
        return null;
    }
    
    /**
     * Returns a DoubleMatrix2D representation of the adjacency matrix of the network
     * @param network the network
     * @return adjacency matrix
     */
    public DoubleMatrix2D getAasDoubleMatrix2D(Node[] network){
        DoubleMatrix2D A = new SparseDoubleMatrix2D(network.length,network.length);
        for(Node N : network)
        {
            for(int i = 0 ;i<N.links.length;i++)
            {
               A.set(N.id, N.links[i].id, 1);
            }
        }
        return A;
    }
    
     /**
     * Creates an adjacency matrix of the network and computes the eigenvalue
     * decomposition   
     * @param network the network
     * @return eigenvalue decomposition of the adjacency matrix of the network
     */
    public EigenvalueDecomposition getEVDAdjacency(Node[] network){
        DoubleMatrix2D A = new SparseDoubleMatrix2D(network.length,network.length);
        for(Node N : network)
        {
            for(int i = 0 ;i<N.links.length;i++)
            {
               A.set(N.id, N.links[i].id, 1);
            }
        }
        return new EigenvalueDecomposition(A);
    }
    
    /**
     * Change the sign of an array
     * @param in array
     */
    private void changeSign(double[] in){
        for(int i=0;i<in.length;i++)
        {
            in[i] = -in[i];
        }
    }
    
    /**
     * Checks whether an array contains only negative entries
     * @param in the array
     * @return true if all entries are negative
     */
    private boolean allNegative(double[] in){
        boolean an = true;
        for(double d : in)
        {
            if(Double.compare(d,0.0)>=0)
                an = false;
        }
        return an;
    }
    
    /**
     * Computes the eigenvector centrality scores of the network. The eigenvector
     * centrality score for a node is nothing but the element in the
     * eigenvector belonging to the largest eigenvalue that corresponds to the node
     * @param nw the network 
     * @return the eigenvector centrality scores.
     */
    public double[] eigenvectorCentrality(EigenvalueDecomposition evd){
        double[] res;
        if(Double.compare(evd.getRealEigenvalues().get(0),evd.getRealEigenvalues().get(evd.getRealEigenvalues().size()-1)) < 0)
        {
            res = evd.getV().viewColumn(evd.getRealEigenvalues().size()-1).toArray();
        }
        else
        {
            res = evd.getV().viewColumn(0).toArray();
        }
        if(allNegative(res))
        {
            changeSign(res);
        }
        return res;
    }
    
    /**
     * Computes the eigenvector centrality scores of the network. The eigenvector
     * centrality score for a node is nothing but the element in the
     * eigenvector belonging to the largest eigenvalue that corresponds to the node
     * @param nw the network 
     * @return the eigenvector centrality scores.
     */
    public double[] eigenvectorCentrality(Node[] nw){
        EigenvalueDecomposition evd = getEVDAdjacency(nw);
        double[] res;
        if(Double.compare(evd.getRealEigenvalues().get(0),evd.getRealEigenvalues().get(nw.length-1)) < 0)
        {
            res = evd.getV().viewColumn(nw.length-1).toArray();
        }
        else
        {
            res = evd.getV().viewColumn(0).toArray();
        }
        if(allNegative(res))
        {
            changeSign(res);
        }
        return res;
    }
    
    /**
     * Normalises a vector
     * @param in the vector
     */
    public void normalise(double[] in){
        double norm = 0d;
        for(double d: in)
        {
            norm += Math.pow(d, 2);
        }
        norm = Math.sqrt(norm);
        for(int i=0;i<in.length;i++)
        {
            in[i] = in[i]/norm;
        }
    }
    
    /**
     * Gets eigenvalue decomposition of a matrix
     * @param A matrix
     * @return eigenvalue decomposition
     */
    public EigenvalueDecomposition getEVD(DoubleMatrix2D A){
        return new EigenvalueDecomposition(A);
    }
    
    /**
     * Gets the leading eigenpair from an eigenvaluedecomposition
     * @param evd the decomposition
     * @return the leading eigenpair
     */
    public LeadingEigenPair leadingEigenPair(EigenvalueDecomposition evd){
        LeadingEigenPair ep = new LeadingEigenPair();
        if(Double.compare(evd.getRealEigenvalues().get(0),evd.getRealEigenvalues().get(evd.getRealEigenvalues().size()-1) ) < 0)
        {
            ep.v = evd.getV().viewColumn(evd.getRealEigenvalues().size()-1).toArray();
            ep.e = evd.getRealEigenvalues().get(evd.getRealEigenvalues().size()-1);
        }
        else
        {
            ep.v = evd.getV().viewColumn(0).toArray();
            ep.e = evd.getRealEigenvalues().get(0);
        }
        if(allNegative(ep.v))
        {
            changeSign(ep.v);
        }
        return ep;
    }
    
    /**
     * Computes the algebraic connectivity of a network. The algebraic connectivity
     * is the second smallest eigenvalue of the Laplacian
     * @param nw network
     * @return algebraic connectivity of the network
     */
    public double algebraicConnectivity(Node[] nw){
        EigenvalueDecomposition e = getEVDLaplacian(nw);
        double[] evs = e.getRealEigenvalues().toArray();
        Arrays.sort(evs);
        return evs[1];
    }
    
    /**
     * Eigenvalue number k
     * @param k eigenvalue
     * @param network network array
     * @return eigenvalue k
     */
    public double getEigenvalue(int k, Node[] network){
        EigenvalueDecomposition  E = getEVDAdjacency(network);
        double[] evs = E.getRealEigenvalues().toArray();
        Arrays.sort(evs);
        if(k < 1 || k > evs.length)
        {
            System.out.print("Eigenvalue should be between 1 and N");
            return Double.NaN;
        }
        return evs[evs.length-k];
    }
    
    /**
     * Eigenvalue number k from a list
     * @param k
     * @param list
     * @return 
     */
    public double getEigenvalueAL(int k, String list){
        EigenvalueDecomposition  E = getEVDAdjacencyFromList(list);
        double[] evs = E.getRealEigenvalues().toArray();
        Arrays.sort(evs);
        if(k < 1 || k > evs.length)
        {
            System.out.print("Eigenvalue should be between 1 and N");
            return Double.NaN;
        }
        return evs[evs.length-k];
    }
    
    /**
     * Counts the number of links in the network
     * @param nw network to count the links of
     * @return the number of links in the network
     */
    public int countLinks(Node[] nw){
        int l = 0;
        for(Node n:nw)
        {
            l += n.links.length;
        }
        return l/2;
    }
    
/**
 * Reads a g6 string of a compressed network and creates a node array
 * @param network location of the compressed network
 * @return node array representing compressed network
 */
public Node[] readNetworkFromg6(String network){
    char[] chars = network.toCharArray();
    String bitString = "";
    char c;
    // decode number of nodes in network
    int n = ((int)chars[0])-63;
    // determine length of bitstring
    int bsl = n*(n-1)/2;
    for(int i = 1 ; i<chars.length;i++)
    {
        c = chars[i];
        bitString += to6bitString(((int)c)-63);
    }
    bitString = bitString.substring(0, bsl);
    Node[] nw = createNetworkFromBitString(bitString,n);
    return nw;
}

/**
 * Prints the element of an array to the console up to index
 * @param A the array to be printed
 * @param index the index at which to stop printing
 */
public static void printArray(int[] A, int index){
        for(int i=0;i<index;i++)
        {
            System.out.print(A[i] + " ");
        }
        System.out.println();
    }
/**
 * Sorts a collection based on its natural ordering
 * @param <T>
 * @param c
 * @return
 */
public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
  List<T> list = new ArrayList<T>(c);
  java.util.Collections.sort(list);
  return list;
}

/**
 * Returns a histogram of the degrees 
 * @param nw network location
 * @return histogram of the degrees
 */
public int[][] degreeHist(String nw){
    Node[] net = readNetworkFromAdjacencyList(nw); 
    Analyser A = new Analyser();
    for(Node n : net)
    {
        A.addToBin(0, n.links.length, 1);
    }
    return A.getBins(0);
}

/**
 * Returns a histogram with zeros for non-existing degrees between min degree and max degree
 * @param nw the network 
 * @return padded degree histogram
 */
public int[] paddedDegreeHist(Node[] nw){
    int[][] H = degreeHist(nw);
    int max = H[H.length-1][0];
    System.out.println(max);
    int[] res = new int[max+1];
    for(int[] h :H)
    {
        res[h[0]] = h[1];
    }
    return res;
}

/**
 * Gets the degree histogram of the network
 * @param nw network
 * @return degree histogram
 */
public int[][] degreeHist(Node[] nw){
    Analyser A = new Analyser();
    for(Node n : nw)
    {
        A.addToBin(0, n.links.length, 1);
    }
    return A.getBins(0);
}

/**
 * Computes the communities using Blondel's method
 * @param adj
 * @return 
 */
public int[] community(String adj){
    
    try 
    {
        // Check whether the shell script is present
        File F = new File("doBlondel.sh");
        if(!F.exists())
        {
            System.out.println("Cannot find shell script");
            return null;
        }
        // Execute blondel shell script
        String command = "./doBlondel.sh " + adj;
        System.out.println("Going to execute: ");
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        // Read output
        BufferedReader in = new BufferedReader(new FileReader(adj+".membership"));
        String line;
        ArrayList<String> contents = new ArrayList<String>();
        while((line = in.readLine())!= null)
        {
            contents.add(line);
        }
        int[] res = new int[contents.size()];
        int index = 0;
        for(String s : contents)
        {
            res[index] = Integer.parseInt(s.split(" ")[1]);
            index++;
        }
        return res;
    } 
    catch (Exception ex) 
    {
        Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
}
/**
 * 
 * @param a Array A
 * @param b Array B
 * @param sorted true if arrays A and B are sorted in ascending order
 * @return the number of elements that are both in A and in B
 */
public static int countMatches(int[] a, int[] b, boolean sorted){
    int[] ac = null;
    int[] bc = null;
    if(!sorted)
    {
        ac = new int[a.length];
        bc = new int[b.length];
        System.arraycopy(a, 0, ac, 0, a.length);
        System.arraycopy(b, 0, bc, 0, b.length);
        Arrays.sort(ac);
        Arrays.sort(bc);
    }
    else
    {
        ac = a;
        bc = b;
    }
    int bi =0;
    int c = 0;
    for(int ai =0;ai<ac.length;ai++)
    {
        if(ac[ai] >= bc[bi])
        {
            if(ac[ai] == bc[bi])
            {
                c++;
                if(bi < bc.length-1)
                {
                    bi++;
                }
            }
            else
            {
                while(bi < bc.length-2 && ac[ai] > bc[bi+1])
                {
                    bi++;
                }
                if(bi < bc.length-1 && ac[ai] == bc[bi+1])
                {
                    if(bi < bc.length-1)
                    {
                        bi++;
                    }
                    c++;
                }
            }
        }
    }
    return c;
}

    /**
     * Prints a matrix to the console
     * @param A matrix
     */
    public void printMatrix(int[][] A){
        for(int i=0;i<A.length;i++)
        {
            for(int j =0;j<A[i].length;j++)
            {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Finds the maximum value in the array
     * @param A array
     * @return maximum value
     */
    public double max(double[] A){
        double max = Double.MIN_VALUE;
        for(double a:A)
            max = a>max?a:max;
        return max;
    }
    
    /**
     * Finds the maximum value in the array
     * @param A array
     * @return maximum value
     */
    public int max(int[] A){
        int max = Integer.MIN_VALUE;
        for(int a : A)
           max = a>max?a:max;
        return max;
    }
    
    /**
     * Finds the minimum value in the array
     * @param A array
     * @return minimum value
     */
    public int min(int[] A){
        int min = Integer.MAX_VALUE;
        for(int a : A)
           min = a<min?a:min;
        return min;
    }
    
    /**
     * Finds the average value in the array
     * @param A array
     * @return average value
     */
    public double average(double[] A){
        double av = 0d;
        for(double a:A)
        {
            av += (double)a;
        }
        av = av/(double)A.length;
        return av;
    }
    
    /**
     * Finds the average value in the array
     * @param A array
     * @return average value
     */
    public double average(int[] A){
        double av = 0d;
        for(int a:A)
        {
            av += (double)a;
        }
        av = av/(double)A.length;
        return av;
    }
    
    /**
     * Finds the standard deviation of the values in the array
     * @param A array
     * @return standard deviation
     */
    public double std(double[] A){
        double av = average(A);
        double std = 0d;
        for(double a:A)
        {
            std += Math.pow(((double)a - av), 2);
        }
        std = Math.sqrt(std/(double)(A.length-1));
        return std;
    }
    
   
    /**
     * Returns the degree distribution of a network written in the file as an adjacency list
     * @param in network file
     * @return the degree distribution
     */
    public int[][] degreeDistribution(String in){
        Node[] nw = readNetworkFromAdjacencyList(in);
        return degreeDistribution(nw);
    }
    
    /**
     * Determines the degree distribution of the network represented by the
     * Node array
     * @param network network under consideration
     * @return two dimensional array with the first column representing the degree
     * and the second column the number of occurrences.
     */
    public int[][] degreeDistribution(Node[] network){
        Analyser A = new Analyser();
        for(Node N : network)
        {
            A.addToBin(0, N.links.length, 1);
        }
        return A.getBins(0);
    }
    
    /**
     * Finds the standard deviation of the values in the array
     * @param A array
     * @return standard deviation
     */
    public double std(int[] A){
        double av = average(A);
        double std = 0d;
        for(int a:A)
        {
            std += Math.pow(((double)a - av), 2);
        }
        std = Math.sqrt(std/(double)(A.length-1));
        return std;
    }
    
    /**
     * Checks whether a node with name node is present in the network
     * @param nw network to check
     * @param node node to find
     * @return true if node is found
     */
    public boolean containsNode(Node[] nw, int node){
        for(Node N : nw)
        {
            if(N.name==node)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds the connected component that contains the requested node
     * @param nw network
     * @param node the node who's component we want.
     * @return 
     */
    public Node[] getComponentOfNode(Node[] nw, int node){
        Node[][] components = getComponentsAsNetworks(nw);
        for(Node[] comp : components)
        {
            if(containsNode(comp,node))
            {
                return comp;
            }
        }
        return null;
    }
    
    /**
     * Returns all the individual connected components as separate networks
     * in an array of node arrays. NOTE that the original network will cloned in
     * order for it not to be destroyed
     * in the process. 
     * @param in The original network
     * @return  all the connected components as individual networks
     */
    public Node[][] getComponentsAsNetworks(Node[] _in){
        Node[] in = cloneNetwork(_in);
        Set<Set<Integer>> compSet = new HashSet<Set<Integer>>();
        int components = 0;        
        // Declare variables / create structures
        Set<Integer> S = new HashSet<Integer>();
        Set<Integer> C;
        Stack<Integer> T = new Stack<Integer>();
        Set<Integer> largest = null;
        // Add all nodes to the set of nodes
        for(Node N : in)
        {
            S.add(N.id);
        }
        // do until the set is empty
        while(!S.isEmpty())
        {
            // create a new set to keep the nodes of this component
            C = new HashSet<Integer>();
            // construct an iterator over the set of nodes to get a start node
            Iterator its = S.iterator();
            int s = (Integer)its.next();
            its.remove();
            // Push the start node onto the stack
            T.push(s);
            // Add the start node to the set of node of this collection
            C.add(s);
            // do until the stack is empty
            while(!T.isEmpty())
            {
                // pop the next node from the stack
                s = T.pop();
                // go over all the neighbours of the start node
                for(Node N :in[s].links)
                {
                    // if the neighbour is stilll in the set of nodes
                    if(S.contains(N.id))
                    {
                        // remove it from the set
                        S.remove(N.id);
                        // push it onto the stack to be processed later
                        T.push(N.id);
                        // add to the set of nodes of this component
                        C.add(N.id);
                    }
                }
            }
            // add the component to the set of components
            compSet.add(C);
            components++;
        }
        Node[][] res = new Node[components][];
        int index = -1;
        int idCounter =0;
        Map<Integer,Integer> idMap = new HashMap<Integer,Integer>();
        for(Set<Integer> c : compSet)
        {
            index++;
            res[index] = new Node[c.size()];
            idCounter = 0;
            idMap.clear();
            // first go over the set to create an idMap
            for(int node:c)
            {
                idMap.put(node, idCounter);
                idCounter++;
            }
            // then go over the set to put the nodes in the array
            for(int node:c)
            {
                res[index][idMap.get(node)] = new Node(idMap.get(node),1);
                res[index][idMap.get(node)].name = in[node].name;
            }
            // finally go over the nodes to fix the links
            for(int node : c)
            {
                // Go over all the links in the original network
                for(Node neigh : in[node].links)
                {
                    try
                    {
                        res[index][idMap.get(node)].addLink(res[index][idMap.get(neigh.id)]);
                    }
                    catch(Exception e)
                    {
                        System.out.println("index: " + index + " node: " + node);
                        System.out.println("Neighbour: " + neigh);
                        System.out.println("Node in map: " + idMap.containsKey(node));
                        System.out.println("Neighbour in map: " + idMap.containsKey(neigh.id));
                        System.out.println("Nodes in component: ");
                        for(int i: c)
                        {
                            System.out.print(i + " ");
                        }
                        System.out.println();
                        System.exit(22);
                    }
                }
            }
        }
        return res;
    }
    
    /**
     * Returns the component size distribution of the network
     * @param in network
     * @param preserveNetwork whether network can be destroyed or not (if not, takes 
     * extra memory)
     * @return component size distribution. array n x 2 a[i][0] = component size
     * a[i][1] = number of components of this size. n = number of different component sizes
     */
    public int[][] getComponentSizeDistribution(Node[] in, boolean preserveNetwork){
        if(preserveNetwork)
        {
            System.err.println("Preserving part not implemented yet");
            System.exit(22);
        }
        Node[][] components = getComponentsAsNetworks(in);
        Analyser A = new Analyser();
        for(Node[] comp:components)
        {
            A.addToBin(0, comp.length, 1);
        }
        return A.getBins(0);
    }
    
    /**
     * Prints the distribution of the component sizes of the network
     * @param in network
     * @param preserveNetwork whether the network can be destroyed or not. Takes extra
     * memory if not
     */
    public void printComponentSizeDistribution(Node[] in, boolean preserveNetwork){
        int[][] C = getComponentSizeDistribution(in,preserveNetwork);
        int total = 0;
        System.out.println("Comp Size Count" );
        for(int[] c:C)
        {
            for(int i : c)
            {
                System.out.print(i + " ");
            }
            System.out.println();
            total += c[0]*c[1];
        }
        System.out.println("total number of nodes: " + total);
    }
    
    /**
     * Creates a map from player id to component id
     * @param in the input network
     * @return map from player id to component
     */
    public Map<Integer,Integer> getComponents(Node[] in){
        
        Map<Integer, Integer> componentMap = new HashMap<Integer,Integer>();
        int components = 0;
        lastDecomposition.clear();
        // create analyser object
        Analyser A = new Analyser();
        // Declare variables / create structures
        Set<Integer> S = new HashSet<Integer>();
        Set<Integer> C;
        Stack<Integer> T = new Stack<Integer>();
        Set<Integer> largest = null;
        // Add all nodes to the set of nodes
        for(Node N : in)
        {
            S.add(N.id);
        }
        // do until the set is empty
        while(!S.isEmpty())
        {
            // create a new set to keep the nodes of this component
            C = new HashSet<Integer>();
            // construct an iterator over the set of nodes to get a start node
            Iterator its = S.iterator();
            int s = (Integer)its.next();
            its.remove();
            // Push the start node onto the stack
            T.push(s);
            // Add the start node to the set of node of this collection
            C.add(s);
            // do until the stack is empty
            while(!T.isEmpty())
            {
                // pop the next node from the stack
                s = T.pop();
                // go over all the neighbours of the start node
                for(Node N :in[s].links)
                {
                    // if the neighbour is stilll in the set of nodes
                    if(S.contains(N.id))
                    {
                        // remove it from the set
                        S.remove(N.id);
                        // push it onto the stack to be processed later
                        T.push(N.id);
                        // add to the set of nodes of this component
                        C.add(N.id);
                    }
                }
            }
            // add the component size to the analyser object
            A.addToBin(0, C.size(), 1);
            // keep a reference to the largest set found so far
            if(largest == null || C.size() > largest.size())
            {
                largest = C;
            }
            for(int node : C)
            {
                componentMap.put(in[node].name,components);
            }
            components++;
        }
        return componentMap;
    }
    
    /**
     * Prints the network to the console
     * @param in the network that has to be printed to the console
     */
    public void printNetwork(Node[] in){
        for(Node N : in)
        {
            System.out.println(N);
        }
    }
    
    /**
     * Extracts the largest connected component from a network and prints
     * the component size distribution. NOTE that the original network will be
     * destroyed. After calling this function the array Node[] in will no longer
     * be usable.
     * @param in Node array representing the network
     * @return the largest connected component
     */
    public Node[] extractLargestComponent(Node[] in){
        lastDecomposition.clear();
        // create analyser object
        Analyser A = new Analyser();
        // Declare variables / create structures
        Set<Integer> S = new HashSet<Integer>();
        Set<Integer> C;
        Stack<Integer> T = new Stack<Integer>();
        Set<Integer> largest = null;
        // Add all nodes to the set of nodes
        for(Node N : in)
        {
            S.add(N.id);
        }
        // do until the set is empty
        while(!S.isEmpty())
        {
            // create a new set to keep the nodes of this component
            C = new HashSet<Integer>();
            // construct an iterator over the set of nodes to get a start node
            Iterator its = S.iterator();
            int s = (Integer)its.next();
            its.remove();
            // Push the start node onto the stack
            T.push(s);
            // Add the start node to the set of node of this collection
            C.add(s);
            // do until the stack is empty
            while(!T.isEmpty())
            {
                // pop the next node from the stack
                s = T.pop();
                // go over all the neighbours of the start node
                for(Node N :in[s].links)
                {
                    // if the neighbour is stilll in the set of nodes
                    if(S.contains(N.id))
                    {
                        // remove it from the set
                        S.remove(N.id);
                        // push it onto the stack to be processed later
                        T.push(N.id);
                        // add to the set of nodes of this component
                        C.add(N.id);
                    }
                }
            }
            // add the component size to the analyser object
            A.addToBin(0, C.size(), 1);
            // keep a reference to the largest set found so far
            if(largest == null || C.size() > largest.size())
            {
                largest = C;
            }
        }
        // print al the component sizes to the console
        //A.printBins(0);
        int[][] comps = A.getBins(0);
        for(int r = 0 ;r<comps.length;r++)
        {
            lastDecomposition.add((comps[r][0] + " " + comps[r][1]));
        }
        // construct a new network based on the nodes in the component
        Node[] res = new Node[largest.size()];
        Iterator it = largest.iterator();
        int s;
        int p;
        int index = 0;
        // First we have to create a map to map nodes to their new network
        // we also put the nodes in their new network. We give nodes a new id
        // that equals the position in the node array.
        Map<Integer,Integer> newId = new HashMap<Integer,Integer>();
        while(it.hasNext())
        {
            s = (Integer)it.next();
            if(!newId.containsKey(s))
            {
                newId.put(s, index);
                index++;
            }
            res[newId.get(s)] = in[s];
        }
        // Now we have to process all the added nodes to correct their link lists
        // to point to the right position in the new network
        for(Node N: res)
        {
            for(int i=0;i<N.links.length;i++)
            {
                N.links[i] = res[newId.get(N.links[i].id)];
            }
        }
        // Finally we need to change all the node ids
        for(Node N: res)
        {
            N.id = newId.get(N.id);
        }
        return res;
    }
    
    /**
     * Returns the number of components of all size of the last decomposition
     * @return 
     */
    public String[] getLastDecompostion(){
        return lastDecomposition.toArray(new String[lastDecomposition.size()]);
    }
    
    /**
     * Get the assortativity of a network stored as an adjacencylist in the file
     * @param in adjacency list
     * @return assortativity
     */
    public double assortativity(String in){
        return assortativity(readNetworkFromAdjacencyList(in));
    }
    
    /**
     * Outputs the assortativity constant of the network in in
     * @param in the network to be analysed
     * @return the assortativity of the network
     */
    public double assortativity(Node[] in){
       double t1 = 0d;
       double t2 = 0d;
       double t3 = 0d;
       int s;
       int d;
       int lc =0;
       // Calculate the coefficient
       int counter =0;
       int perc = (int)Math.ceil((double)in.length/100d);
       long start = System.currentTimeMillis();
       long expectedEnd;
       long last = System.currentTimeMillis();
       for(Node N : in)
       {
           for(Node P : N.links)
           {
               s = N.links.length;
               d = P.links.length;
               t1 += (double)(d*s);
               t2 += (double)(d+s)/2d;
               t3 += (double)(Math.pow(d,2) + Math.pow(s,2))/2d;
               lc++;
           }
           counter++;
           if(counter % perc == 0)
           {
                expectedEnd = start + (long)((double)(System.currentTimeMillis()-start)/(double)(counter/perc)) * (100-(counter/perc));
                last = System.currentTimeMillis();
           }
       }
       t1 = t1/(double)lc;
       t2 = t2/(double)lc;
       t2 = (double)Math.pow(t2,2);
       t3 = t3/(double)lc;
       return (t1-t2)/(t3-t2);
    }
    
    /**
     * Gets the clusteringcoefficient for the network stored as adjacency list in the file
     * @param in location of the adjacencylist
     * @return the clustering coefficient
     */
    public double[] clusteringCoefficient(String in){
        return clusteringCoefficient(readNetworkFromAdjacencyList(in));
    }
    
    /**
     * Calculates the clustering coefficient of the network in in
     * @param in the network that has to be analysed
     */
    public double[] clusteringCoefficient(Node[] in){
        Set<Integer> neighbours = new HashSet<Integer>();
        int link = 0;
        double[] cc = new double[in.length];
        int index = 0;
        int counter =0;
        int perc = (int)Math.ceil((double)in.length/100d);
        long start = System.currentTimeMillis();
        long expectedEnd;
        long last = System.currentTimeMillis();
        for(Node N: in)
        {
            
            link = 0;
            // put all neighbours in set.
            neighbours.clear();
            for(Node n:N.links)
            {
                neighbours.add(n.id);
            }
            // count number of links among neighbours
            for(Node n:N.links)
            {
                for(Node l:n.links)
                {
                    if(neighbours.contains(l.id))
                    {
                        link++;
                    }
                }
            }
            cc[index] = (double)link/Math.max(N.links.length*(N.links.length-1), 1);
            index++;
            counter++;
            if(counter % perc == 0)
            {
                expectedEnd = start + (long)((double)(System.currentTimeMillis()-start)/(double)(counter/perc)) * (100-(counter/perc));
                System.out.println((counter/perc) + " percent done. Last percent done in " + (System.currentTimeMillis()-last) + " ms. Expected finish time: " + (new Date(expectedEnd)).toString());
                last = System.currentTimeMillis();
            }
        }
        return cc;
    }
    
    /**
     * Clones the network
     * @param in network in
     * @return clone out
     */
    public Node[] cloneNetwork(Node[] in){
        Node[] out = new Node[in.length];
        for(int i=0; i<in.length;i++)
        {
            out[i] = new Node(in[i].id, in[i].links.length);
            out[i].name = in[i].name;
            out[i].index = in[i].index;
        }
        for(int i = 0;i<in.length;i++)
        {
            for(int l=0;l<in[i].links.length;l++)
            {
                out[i].links[l] = out[in[i].links[l].id]; 
            }
        }
        return out;
    }
    
    /**
     * Returns the path metrics for the network stored as adjacencylist in file
     * @param in location of the file
     * @return pathmetrics
     */
    public Object[] pathMetrics(String in){
        return pathMetrics(readNetworkFromAdjacencyList(in));
    } 
    
    /**
     * Calculates path based metrics : betweenness, radius, diameter, average distance
     * based on Brande's algorithm. Running time can be very long
     * @param in the network that has to be analysed
     * @return Object array wit elements:<br>
     * res[0] = eccentricity <br>
     * res[1] = average hop count <br>
     * res[2] = betweenness <br>
     * res[3] = hop count distribution <br>
     */
    public Object[] pathMetrics(Node[] in){
        int[] dist = new int[in.length];
        ArrayList<ArrayList<Integer>> Pred = new ArrayList<ArrayList<Integer>>();
        Queue<Integer> Q = new LinkedList<Integer>();
        Stack<Integer> S = new Stack<Integer>();
        int[] sigma = new int[in.length];
        int[] eccentricity = new int[in.length];
        double[] delta = new double[in.length];
        double[] betw = new double[in.length];
        for(int r =0;r<betw.length;r++)
        {
            betw[r] = 0d;
        }
        int[][] D = new int[in.length][in.length];
        int s;
        int v;
        int w;
        double avg = 0d;
        int maxN;
        int max = Integer.MIN_VALUE;
        int counter =0;
        double perc;
        double percpersec;
        long start = System.currentTimeMillis();
        long expectedEnd;
        long last = System.currentTimeMillis();
        long time;
        SimpleTimer st = new SimpleTimer(10000);
        st.start();
        int ct = 0;
        Analyser A = new Analyser();
        for(Node N : in)
        {
            s = N.id;
            // init
            Pred.clear();
            for(int i=0;i<in.length;i++)
            {
                Pred.add(new ArrayList<Integer>());
                dist[i] = Integer.MAX_VALUE;
                sigma[i] = 0;
            }
            dist[s] = 0;
            sigma[s] = 1;
            Q.add(s);
            while(!Q.isEmpty())
            {
                v = Q.poll();
                S.push(v);
                for(Node W : in[v].links)
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
            for(int i:dist)
            {
                avg += i;
                if(i != 0)
                {
                    A.addToBin(0, i, 1);
                }
            }
            maxN = max(dist);
            eccentricity[s] = maxN;
            max = max > maxN ? max : maxN;
            // dist now contains the shortest paths from every node to s
            // Next we are going to accumulate the paths
            for(int i =0;i<in.length;i++)
            {
                D[s][i] = dist[i];
            }
            for(int i=0;i<in.length;i++)
            {
                delta[i] = 0d;
            }

            while(!S.isEmpty())
            {
                w = S.pop();
                for(int V : Pred.get(w))
                {
                    delta[V] += ((double)sigma[V]/(double)sigma[w])*(1d+delta[w]);
                    ct++;
                }
                if(w!=s)
                {
                    betw[w] += delta[w];
                    
                }
            }
            counter++;
            if(st.tick)
            {
                st.tick = false;
                time = System.currentTimeMillis();
                perc = 100d*(double)counter/(double)in.length;
                percpersec = perc/((double)(time-start));
                expectedEnd = start + (long)(100d/percpersec);
                System.out.println(counter + " out of " + in.length + " done. (" + perc + "%) estimated finish time: " + new Date(expectedEnd).toString() + " running time: " + ((double)(expectedEnd - start))/60000d  + " min.");
                last = System.currentTimeMillis();
            }
        }
        // Normalise the betweenness by dividing by (N-1)(N-2)
        double f = (double)(in.length-1)*(double)(in.length-2);
        for(int r =0;r<betw.length;r++)
        {
            betw[r] = betw[r]/f;
        }
        Object[] res = new Object[5];
        res[0] = eccentricity;
        res[1] = avg/((double)in.length*((double)(in.length-1)));
        res[2] = betw;
        res[3] = A.getBins(0);
        res[4] = D;
        return res;
    }
    
    /**
     * Checks whether a network is connected or not
     * @param network the network
     * @return true if connected, false otherwise
     */
    public boolean isConnected(Node[] network){
        Set<Integer> Q = new HashSet<>();
        for(int i=0;i<network.length;i++)
        {
            Q.add(i);
        }
        Stack<Integer> S = new Stack<>();
        int q = Q.iterator().next();
        S.add(q);
        while(!S.isEmpty() && !Q.isEmpty())
        {
            q = S.pop();
            Q.remove(q);
            for(Node N : network[q].links)
            {
                if(!S.contains(N.id) && Q.contains(N.id))
                {
                    S.add(N.id);
                    Q.remove(N.id);
                }
            }
        }
        return Q.isEmpty();
    }
    
    /**
     * Calculates path based metrics : betweenness, radius, diameter, average distance
     * based on Brande's algorithm. Running time can be very long
     * @param in the network that has to be analysed
     * @param threads the number of threads to use
     */
    public Object[] pathMetricsMT(Node[] in, int threads){
        PathMetricWorker[] workers = new PathMetricWorker[threads];
        for(int i = 0; i<workers.length;i++)
        {
            workers[i] = new PathMetricWorker(cloneNetwork(in));
            workers[i].start();
        }
        int[] dist;
        int[] eccentricity = new int[in.length];
        double[] betw = new double[in.length];
        for(int r =0;r<betw.length;r++)
        {
            betw[r] = 0d;
        }
        double avg = 0d;
        int maxN;
        int max = Integer.MIN_VALUE;
        int counter =0;
        double perc;
        double percpersec;
        long start = System.currentTimeMillis();
        long expectedEnd;
        long time;
        long last = System.currentTimeMillis();
        SimpleTimer st = new SimpleTimer(10000);
        st.start();
        Analyser A = new Analyser();
       
        double[] betwIncr;
        boolean done = false;
        int node = 0;
        boolean submittingDone = false;
        int workersDone = 0;
        int resultCounter = 0;
        while(!done)
        {
            workersDone = 0;
            for(int j=0;j<workers.length;j++)
            {
                if(workers[j].done)
                {
                    if(submittingDone)
                    {
                        workersDone++;
                    }
                    // proess results
                    dist = workers[j].getDist();
                    betwIncr = workers[j].getBetweenness();
                    if(dist != null && workers[j].workerActive)
                    {
                        resultCounter++;
                        for(int i:dist)
                        {
                            avg += i;
                            if(i != 0)
                            {
                                A.addToBin(0, i, 1);
                            }
                        }
                        for(int i = 0;i<betwIncr.length;i++)
                        {
                            betw[i] += betwIncr[i];
                        }
                        maxN = max(dist);
                        eccentricity[workers[j].getNode()] = maxN;
                        max = max > maxN ? max : maxN;
                        counter++;
                    }
                    if(!submittingDone)
                    {
                        workers[j].done = false;
                        workers[j].setTask(node);
                        node++;
                        workersDone = 0;
                    }
                    else
                    {
                        workers[j].workerActive = false;
                    }
                }
                if(node == in.length)
                {
                    submittingDone = true;
                }
            }
            if(submittingDone && workersDone == workers.length)
            {
                done = true;
            }
            if(st.tick)
            {
                st.tick = false;
                time = System.currentTimeMillis();
                perc = 100d*(double)counter/(double)in.length;
                percpersec = perc/((double)(time-start));
                expectedEnd = start + (long)(100d/percpersec);
                System.out.println(counter + " out of " + in.length + " done. (" + perc + "%) estimated finish time: " + new Date(expectedEnd).toString() + " running time: " + ((double)(expectedEnd - start))/60000d  + " min.");
                last = System.currentTimeMillis();
            }
        }
        for(int i=0;i<workers.length;i++)
        {
            workers[i].stop = true;
        }
        // Normalise the betweenness by dividing by (N-1)(N-2)
        double f = (double)(in.length-1)*(double)(in.length-2);
        for(int r =0;r<betw.length;r++)
        {
            betw[r] = betw[r]/f;
        }
        System.out.println("betweenness scores: ");
        for(int i=0;i<in.length;i++)
        {
            System.out.println(in[i].id + " " + betw[i]);
        }
        System.out.println("radius: " + min(eccentricity));
        System.out.println("diameter: " + max(eccentricity));
        System.out.println("average dist. " + avg/(double)(in.length*(in.length-1)));
        System.out.println("Result counter: " + resultCounter);
        Object[] res = new Object[4];
        res[0] = eccentricity;
        res[1] = avg/(double)((double)in.length*(double)(in.length-1));
        res[2] = betw;
        res[3] = A.getBins(0);
        
        return res;
    }
    
    /**
     * Computes the coreness of a network
     * @param in location of the network (adjacency list)
     * @return coreness scores for all nodes
     */
    public int[] coreness(String in){
        return coreness(readNetworkFromAdjacencyList(in));
    }
    
    /**
     * Computes the coreness of a network
     * @param network the network
     * @return coreness scores for all nodes
     */
    public int[] coreness(Node[] network){
        int n=network.length,md=0,start=1,num;
        int w,du,pu,pw;
        int[] vert = new int[network.length+1];
        int[] pos = new int[network.length+1];
        int[] deg = new int[network.length+1];
        int[] bin = new int[network.length];
        for(int v = 1;v<=network.length;v++)
        {
            deg[v] = network[v-1].links.length;
            md = md > deg[v] ? md : deg[v];
        }
        System.out.println("md: " + md);
        for(int v =1;v<=network.length;v++)
        {
            bin[deg[v]]++;
        }
        for(int d =0;d<=md;d++)
        {
            num = bin[d];
            bin[d] = start;
            start += num;
        }
        for(int v = 1;v<=n;v++)
        {
            pos[v] = bin[deg[v]];
            vert[pos[v]] = v;
            bin[deg[v]]++;
        }
        for(int d=md; d>= 1;d--)
        {
            bin[d] = bin[d-1];
        }
        bin[0] = 1;
        int v;
        System.out.print("bin: ");
        printArray(bin);
        System.out.print("deg: ");
        printArray(deg);
        System.out.print("vert: ");
        printArray(vert);
        System.out.print("pos: ");
        printArray(pos);
        for(int i =1;i<=n;i++)
        {
            v = vert[i];
            System.out.println(v);
            for(Node u : network[v-1].links)
            {
                if(deg[u.id+1] > deg[v])
                {
                    du = deg[u.id+1];
                    pu = pos[u.id+1];
                    pw = bin[du];
                    w = vert[pw];
                    if(u.id+1 != w)
                    {
                        pos[u.id+1] = pw;
                        pos[w] = pu;
                        vert[pu] = w;
                        vert[pw] = u.id+1;
                    }
                    bin[du]++;
                    deg[u.id+1]--;
                }
            }
        }
        for(int i=0;i<deg.length;i++)
        {
            System.out.println(i + " " + deg[i]);
        }
        int[] res = new int[network.length];
        for(int r=1;r<=network.length;r++)
        {
            res[r-1] = deg[r];
        }
        return res;
    }
    
    /**
     * Writes the network in in to the pajek file format
     * @param in network to be written to pajek
     * @param file file destination for the pajek file
     */
    public void writeNetworkToPajek(Node[] in,String file){
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            Set<String> done = new HashSet<String>();
            out.write("*Vertices " + in.length + "\n");
            out.write("*Edges\n");
            int s;
            int d;
            String l;
            for(Node N : in)
            {
                s = N.id;
                for(Node p:N.links)
                {
                    d = p.id;
                    l = (s + " " + d);
                    if(!done.contains(l))
                    {
                        out.write(((s+1) + " " + (d+1) + "\n"));
                        done.add((d + " " + s));
                    }
                }
            }
            out.flush();
            out.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * writes the names of the nodes in the network to disk
     * @param in network
     * @param file output file
     */
    public void writeToSimpleTextListNames(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            Set<String> done = new HashSet<String>();
            int s;
            int d;
            String l;
            for(Node N : in)
            {
                s = N.name;
                for(Node p:N.links)
                {
                    d = p.name;
                    l = (s + " " + d);
                    if(!done.contains(l))
                    {
                        out.write(((s) + " " + (d) + "\n"));
                        done.add((d + " " + s));
                    }
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * writes the names of the nodes in the network to disk
     * @param in network
     * @param file output file
     */
    public void writeToSimpleTextListDirectedNamed(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            int s;
            int d;
            String l;
            for(Node N : in)
            {
                s = N.name;
                for(Node p:N.links)
                {
                    d = p.name;
                    out.write(((s) + " " + (d) + "\n"));
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * writes the names of the nodes in the network to disk
     * @param in network
     * @param file output file
     */
    public void writeToSimpleTextListDirected(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            int s;
            int d;
            String l;
            for(Node N : in)
            {
                s = N.id;
                for(Node p:N.links)
                {
                    d = p.id;
                    out.write(((s) + " " + (d) + "\n"));
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes the network to an adjacency matrix
     * @param in network
     * @param file output file
     */
    public void writeToAdjacencyMatrix(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            byte[] line = new byte[(2*in.length)-1];
            for(int i=1;i<line.length;i+=2)
            {
                line[i] = ' ';
            }
            for(int i=0;i<line.length;i+=2)
            {
                line[i] = '0';
            }
            for(Node N : in)
            {
                for(Node p : N.links)
                {
                    line[p.id*2] = '1';
                }
                out.write(new String(line)+"\n");
                for(Node p : N.links)
                {
                    line[p.id*2] = '0';
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Writes the network to an adjacency list
     * @param in network
     * @param file output file
     */
    public void writeToAdjacencyList(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("#Number of nodes:\n");
            out.write(in.length + "\n");
            out.write("#Node degrees:\n");
            for(Node N : in)
            {
                out.write(N.name + " " + N.links.length + "\n");
            }
            out.write("#Links:\n");
            for(Node N : in)
            {
                for(Node L : N.links)
                {
                    if(N.name > L.name)
                    {
                        out.write(N.name + " " + L.name + "\n");
                    }
                }
            }
            out.flush();
            out.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Writes the network to a simple list
     * @param in network
     * @param file output file
     */
    public void writeToSimpleTextList(Node[] in, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            Set<String> done = new HashSet<String>();
            int s;
            int d;
            String l;
            for(Node N : in)
            {
                s = N.id;
                for(Node p:N.links)
                {
                    d = p.id;
                    l = (s + " " + d);
                    if(!done.contains(l))
                    {
                        out.write(((s) + " " + (d) + "\n"));
                        done.add((d + " " + s));
                    }
                }
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Reads an adjacency list and converts it to the pajek file format
     * @param fin source file
     * @param fout destination file
     */
    public void convertAdjacencyListToPajek(String fin, String fout){
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(fin));
            BufferedWriter out = new BufferedWriter(new FileWriter(fout));

            String line;
            // Get the number of nodes
            line = in.readLine();
            if(!line.equals("#Number of nodes:"))
            {
                System.out.println("wrong file type");
            }
            int n = Integer.parseInt(in.readLine());
            out.write("*Vertices " + n + "\n");
            out.write("*Edges\n");
            // Get the degrees
            line = in.readLine();
            if(!line.equals("#Node degrees:"))
            {
                System.out.println("wrong file type");
            }
            int s;
            int d;
            String[] parts;
            line = in.readLine();
            int i =1;
            Map<Integer,Integer> idM = new HashMap<>();
            while(!line.equals("#Links:"))
            {
                parts = line.split(" ");
                s = Integer.parseInt(parts[0]);
                d = Integer.parseInt(parts[1]);
                if(!idM.containsKey(s))
                {
                    idM.put(s, i);
                    i++;
                }
                line = in.readLine();
            }
            // Get the links
            while((line = in.readLine())!= null)
            {
                parts = line.split(" ");
                s = idM.get(Integer.parseInt(parts[0]));
                d = idM.get(Integer.parseInt(parts[1]));
                out.write((s + " " + d + "\n"));
            }
            out.flush();
            out.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get those elements in A that are not in B
     * @param A Set A
     * @param B Set B
     * @return  those elements that are in A but not in B, or an empty
     * set if A is a subset of B
     */
    public Set getComplement(Set A, Set B){
        Set C = new HashSet();
        for(Object a : A)
        {
            if(!B.contains(a))
            {
                C.add(a);
            }
        }
        return C;
    }
    
    /**
     * Reads the network from a simple list and preserves ids
     * @param list location of the list
     * @return network
     */
    public Node[] readNetworkFromSimpleListPreserveIDs(String list){
        Node[] network = null;
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(list));
            // Read file ones to count the number of nodes
            String line;
            String[] parts;
            int s;
            int d;
            int maxId =0;
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
                        System.out.println("No suitable separator found for simple list");
                        return null;
                    }
                }
                parts = line.split(separators[separator]);
                s = Integer.parseInt(parts[0]);
                d = Integer.parseInt(parts[1]);
                maxId = maxId > s ? maxId : s;
                maxId = maxId > d ? maxId : d;
            }
            network = new Node[maxId + 1];
            for(int i = 0; i<network.length;i++)
            {
                network[i] = new Node(i,0);
                network[i].name = i;
            }
            in.close();
            in = new BufferedReader(new FileReader(list));
            while((line = in.readLine()) != null)
            {
                parts = line.split(separators[separator]);
                s = Integer.parseInt(parts[0]);
                d = Integer.parseInt(parts[1]);
                if(!network[s].isNeighbour(d))
                {
                    network[s].addLink(network[d]);
                }
                if(!network[d].isNeighbour(s))
                {
                    network[d].addLink(network[s]);
                }
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
        return network;
    }
    
    /**
     * Reads a network from a simple list. NOTE. nodes might be relabeled.
     * @param list location of the list file
     * @return the network
     */
    public Node[] readNetworkFromSimpleList(String list){
        Node[] network = null;
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(list));
            // Read file ones to count the number of nodes
            String line;
            String[] parts;
            int s;
            int d;
            Map<Integer,Integer> idMap = new HashMap<>();
            int idCounter =0;
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
                }
                parts = line.split(separators[separator]);
                s = Integer.parseInt(parts[0]);
                d = Integer.parseInt(parts[1]);
                if(!idMap.containsKey(s))
                {
                    idMap.put(s, idCounter);
                    idCounter++;
                }
                if(!idMap.containsKey(d))
                {
                    idMap.put(d, idCounter);
                    idCounter++;
                }
            }
            network = new Node[idMap.size()];
            for(int i : idMap.keySet())
            {
                network[idMap.get(i)] = new Node(idMap.get(i),0);
                network[idMap.get(i)].name = i;
            }
            in.close();
            in = new BufferedReader(new FileReader(list));
            while((line = in.readLine()) != null)
            {
                parts = line.split(separators[separator]);
                s = idMap.get(Integer.parseInt(parts[0]));
                d = idMap.get(Integer.parseInt(parts[1]));
                if(!network[s].isNeighbour(d))
                {
                    network[s].addLink(network[d]);
                }
                if(!network[d].isNeighbour(s))
                {
                    network[d].addLink(network[s]);
                }
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
        return network;
    }
    
    /**
     * Counts the number of non zero elements in a space separated row of a matrix
     * @param row String representation of a matrix row (space separated)
     * @return number of nonzeor elements
     */
    public int countNonzeros(String row){
        int c=0;
        for(String s : row.split(" "))
        {
            if(Integer.parseInt(s) != 0)
            {
                c++;
            }
        }
        return c;
    }
    
    /**
     * Constructs a network based on the adjacency matrix stored in matrix
     * @param matrix the location of the adjacency matrix
     * @return a network based on the adjacency matrix
     */
    public Node[] readNetworkFromAdjacencyMatrix(String matrix){
        Node[] network = null;
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(matrix));
            String line;
            int r =0;
            // First read file once to create network and set degrees
            while((line = in.readLine())!= null)
            {
                if(network == null)
                {
                    network = new Node[line.split(" ").length];
                }
                network[r] = new Node(r,countNonzeros(line));
                r++;
            }
            in.close();
            in = new BufferedReader(new FileReader(matrix));
            String[] parts;
            r=0;
            // Then read the file for a second time to create the links
            while((line = in.readLine())!= null)
            {
                parts = line.split(" ");
                for(int i=0;i<parts.length;i++)
                {
                    if(Integer.parseInt(parts[i]) != 0)
                    {
                        network[r].addLink(network[i]);
                    }
                }
                r++;
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
        return network;
    }
    
    /**
      * Constructs a network based on the input adjacency list. The adjacency list
      * is in the file format that LinkSet creates.
     * @param list file location of the adjacency list
     * @return node array representing the network in the adjacency list.
     */
    public Node[] readNetworkFromAdjacencyList(String list){
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(list));
            String line;
            int lc = 0;
            // Get the number of nodes
            line = in.readLine();
            lc++;
            if(!line.equals("#Number of nodes:"))
            {
                System.out.println("wrong file type expected Number of nodes");
                return null;
            }
            int n = Integer.parseInt(in.readLine());
            lc++;
            Node[] network = new Node[n];
            // Get the degrees
            line = in.readLine();
            lc++;
            if(!line.equals("#Node degrees:"))
            {
                System.out.println("wrong file type, expected node degrees");
                return null;
            }
            int s;
            int d;
            String[] parts;
            line = in.readLine();
            lc++;
            int i =0;
            Map<Integer,Integer> idM = new HashMap<>();
            while(!line.equals("#Links:"))
            {
                lc++;
                parts = line.split(" ");
                s = Integer.parseInt(parts[0]);
                d = Integer.parseInt(parts[1]);
                if(!idM.containsKey(s))
                {
                    idM.put(s, i);
                    i++;
                }
                network[idM.get(s)] = new Node(idM.get(s),d);
                network[idM.get(s)].name = s;
                line = in.readLine();
            }
            // Get the links
            while((line = in.readLine())!= null)
            {
                lc++;
                parts = line.split(" ");
                if(parts.length != 2)
                {
                    System.out.println(list + " is corrupt at line " + lc + " line too short") ;
                }
                if(!idM.containsKey(Integer.parseInt(parts[0])) || !idM.containsKey(Integer.parseInt(parts[1])))
                {
                    System.out.println(list + " is corrupt at line " + lc + " id not in map");
                }
                s = idM.get(Integer.parseInt(parts[0]));
                d = idM.get(Integer.parseInt(parts[1]));
                network[s].addLink(network[d]);
                network[d].addLink(network[s]);
            }
            return network;
        }
        catch (Exception ex)
        {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    

    ArrayList<String> lastDecomposition = new ArrayList<String>();
    
    /**
     * The leadingEigenPair contains the eigenvector and eigenvalue
     */
    public class LeadingEigenPair{
        public LeadingEigenPair(){}
        public LeadingEigenPair(double[] _v, double _e){
            e = _e;
            v = _v;
        }
        public double[] v;
        public double e;
    }

}
