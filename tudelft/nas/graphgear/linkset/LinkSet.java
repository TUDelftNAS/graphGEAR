/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.linkset;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.utils.NodeColor;

/**
 *
 * @author Ruud van de Bovenkamp
 * A linkset uses a red and black tree data set to store all the links
 * if a link is already present in the set, its weight is incremented.
 */
public class LinkSet {
    public int combineType = 0;
    public static final int NODE_TYPE_BASIC = 1;
    public static final int NODE_TYPE_HYPERLINK = 2;
    public static final int NODE_TYPE_TIMED = 3;
    public static final int NODE_TYPE_INTERVAL = 4;
    public LinkSet(){
        T = new RBTree();
    }
    public LinkSet(LinkSetNode _template){
         T = new RBTree(_template);
    }
    /**
     * Adds a simple link
     * @param s source
     * @param d destination
     */
    public void addLink(int s, int d){
        T.insert(s,d);
    }
    /**
     * Adds a timed link
     * @param s source
     * @param d destination
     * @param time time
     */
    public void addLink(int s, int d, long time){
        T.insert(s,d,time);
    }
    /**
     * adds a link as a linksetnode
     * @param L linksetnode that has to be added
     * @return true if inserted<br>false if updated
     */
    public boolean addLink(LinkSetNode L){
        return T.insert(L);
    }
    /**
     * readies the tree for traversal
     */
    public void initTreeTraversal(){
        T.initTreeTraversal();
    }
    
    /**
     * Gets the next link in the tree
     * @return the next link in the tree
     */
    public LinkSetNode getNextInOrder(){
        return T.getNextInOrder();
    }
    /**
     * Prints the adjacency list of the tree
     */
    public void printAdjList(){
        T.traverseTreeInOrder(T.root);
    }
    /**
     * Prints the tree
     */
    public void printTree(){
        T.print();
    }
    /**
     * Gets the size of the tree
     * @return tree size
     */
    public long size(){
        return T.size();
    }
    /**
     * Changes every source into a destination and vice versa
     */
    public void flipTree(){
        RBTree Tf = new RBTree(T.template);
        LinkSetNode n = T.getNextInOrder(T.root);
        while(n != null)
        {
            T.delete(n);
            n.flip();
            Tf.insert(n);
            n = T.getNextInOrder(T.root);
        }
        T = Tf;
    }
    /**
     * Clears the tree
     */
    public void clear(){
        T.root = null;
        T.size = 0l;
        Runtime.getRuntime().gc();
    }
    /**
     * Writes all links that with a weight above the threshold to a file
     * @param file destination of the adjacency list
     * @param threshold threshold on the link weight. The threshold can include
     * both upper and lower bounds. e.g. 10 < t < 20
     */
    public void writeValidLinksToFile(String file, String threshold){
        System.out.println("Filtering links with weight " + threshold);
        try
        {
            // open a stream to the outout location
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            // init tree traversal
            initTreeTraversal();
            LinkSetNode n = T.getNextInOrder();
            byte[] b = new byte[4];
            // split the threshold at the spaces.
            String[] parts = threshold.trim().split(" ");
            char comp = 'x';
            int t = 0;
            int t2 = 0;
            // Check whether the threshold is an upper or lower bound
            if(parts.length == 2)
            {
                // the second part of the expression is the threshold value t
                t = Integer.parseInt(parts[1]);
                // if the first part of the expression has length 1 it is an excluding
                // threshold (> or <) or an equality (=)
                if(parts[0].length() == 1)
                {
                    comp = parts[0].charAt(0);
                }
                else // else it is an including threshold, we encode those as rounded brackets
                {
                    if(parts[0].equals(">="))
                    {
                        comp = ')';
                    }
                    if(parts[0].equals("<="))
                    {
                        comp = '(';
                    }
                }
            }
            else // the threshold contains both an upper and a lower bound
            {
                // the second and fourth part of the expression contain
                // the threshold values t and t2
                t = Integer.parseInt(parts[1]);
                t2 = Integer.parseInt(parts[3]);
                int v = 0;
                /* The expression is now encoded as
                 * a: t > x < t2
                 * b: t > x <= t2
                 * c: t >= x < t2
                 * d: t >= x <= t2
                 */
                if(parts[0].equals(">"))
                {
                    v = 1;
                }
                if(parts[0].equals(">="))
                {
                    v = 2;
                }
                if(parts[1].equals("<"))
                {
                    v++;
                }
                if(parts[2].equals("<="))
                {
                    v += 2;
                }
                comp = (char)(v+96);
            }
            System.out.println("comp = " + comp + " thresholds: " + t + " and "+ t2);
            // Go over all the links andd write the nodes if the link weight is
            // within the threshold
            while(n!=null)
            {
                switch(comp)
                {
                    case '>':
                        if(n.w/2 > t)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }
                        break;
                    case '<':
                        if(n.w/2 < t)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }
                        break;
                    case '=':
                        if(n.w/2 == t)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }
                        break;
                    case '(': // <=
                        if(n.w/2 >= t)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }
                        break;
                    case ')': // >=
                        if(n.w/2 >= t)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }
                        break;
                    case 'a': // t > x < t2
                        if(n.w/2 > t && n.w/2 < t2)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        }    
                        break;
                    case 'b': // t > x <= t2
                        if(n.w/2 > t && n.w/2 <= t2)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        } 
                        break;
                    case 'c': // t >= x < t2
                        if(n.w/2 >= t && n.w/2 < t2)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        } 
                        break;
                    case 'd': // t >= x <= t2
                        if(n.w/2 >= t && n.w/2 <= t2)
                        {
                            writeNode(out,b,n.s);
                            writeNode(out,b,n.d);
                        } 
                        break;
                    default:
                        System.out.println("Unknown operator: " + comp);
                        break;
                }
                n = T.getNextInOrder();
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
     * Writes a link to the file using the buffer b
     * @param out the output destination
     * @param b byte array buffer
     * @param n the LinkSetNode that has to be written to a file
     * @throws IOException
     */
    private void writeLink(BufferedOutputStream out, byte[] b, LinkSetNode n) throws IOException{
        b[0] = (byte)(n.d >>> 24);
        b[1] = (byte)(n.d >>> 16);
        b[2] = (byte)(n.d >>> 8);
        b[3] = (byte)(n.d);
        out.write(b);
        b[0] = (byte)(n.w >>> 24);
        b[1] = (byte)(n.w >>> 16);
        b[2] = (byte)(n.w >>> 8);
        b[3] = (byte)(n.w);
        out.write(b);
    }
    /**
     * Writes a node id to a file
     * @param out the output destination
     * @param b a byte array buffer
     * @param n the node id that has to be written to a file
     * @throws IOException
     */
    private void writeNode(BufferedOutputStream out, byte[] b, int n) throws IOException{
        b[0] = (byte)(n >>> 24);
        b[1] = (byte)(n >>> 16);
        b[2] = (byte)(n >>> 8);
        b[3] = (byte)(n);
        out.write(b);
    }
    /**
     * Writes a break token to the file
     * @param out the output destination
     * @param b a byte array buffer
     * @throws IOException
     */
    private void writeBreak(BufferedOutputStream out, byte[] b) throws IOException{
        writeNode(out,b,-1);
    }
    /**
     * Reads an integer coded as a byte array from a file
     * @param in the input stream
     * @param b a byte array buffer
     * @return the integer read from the stream or -1 if no integer could be read
     * @throws IOException
     */
    private int readInt(BufferedInputStream in, byte[] b) throws IOException{
        if(in.read(b) == -1)
        {
            return -1;
        }
        // Shift the bytes into an int
        return ((b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF));
    }
    /**
     * Writes the entire tree to a simple adjacency list, either weighted or not.
     * Every link will occur only once in the list
     * @param file destination file
     * @param threshold threshold
     * @param weighted when true, the weight of the link will be included in the file
     */
    public void writeSimpleAdjList(String file, int threshold, boolean weighted){
        initTreeTraversal();
        LinkSetNode N;
        int nodeCounter = 0;
        Map<Integer,Integer> M = new HashMap<>();
        Map<Integer,Integer> RM = new HashMap<>();
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            while((N = getNextInOrder()) != null)
            {
                if(N.w >= threshold)
                {
                    if(!M.containsKey(N.s))
                    {
                        M.put(N.s, nodeCounter);
                        RM.put(nodeCounter,N.s);
                        nodeCounter++;
                    }
                    if(!M.containsKey(N.d))
                    {
                        M.put(N.d, nodeCounter);
                        RM.put(nodeCounter,N.d);
                        nodeCounter++;
                    }
                    out.write(M.get(N.s) + " " + M.get(N.d) + (weighted ? " " + N.w + "\n" : "\n"));
                }
            }
            out.flush();
            out.close();
            out = new BufferedWriter(new FileWriter(file+".map"));
            for(int i=0;i<nodeCounter;i++)
            {
                out.write(i + " " + RM.get(i) + "\n");
            }
            out.flush();
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Writes the entire tree to a file as an adjacency list. The file contains
     * the number of nodes, the degree of every node and all the links. An inclusive
     * threshold is performed on the link weight
     * @param threshold linkweights have to be equal to or larger than the threshold
     * @param file destination file
     */
    public void writeAdjList(String file, int threshold){
        try
        {
            // Map to store the degrees of every node in
            Map<Integer,Integer> M = new HashMap<>();
            // Init tree for traversal
            initTreeTraversal();
            // declare variables
            int[] nodes;
            LinkSetNode N;
            int val = 1;
            // Go over the tree and count the number of occurances of each node
            // (ie their degree) in the map
            while((N = getNextInOrder()) != null)
            {
                if(N.w >= threshold)
                {
                    nodes = N.getNodes();
                    for(int n:nodes)
                    {
                        val = 1;
                        if(M.containsKey(n))
                        {
                            val = M.get(n) + 1;
                        }
                        M.put(n, val);
                    }
                }
            }
            // Open output stream
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            // Write number of nodes
            out.write("#Number of nodes:\n");
            out.write(M.size() + "\n");
            // Write degrees of the nodes
            out.write("#Node degrees:\n");
            for(int i : M.keySet())
            {
                out.write(i + " " + M.get(i) + "\n");
            }
            out.write("#Links:\n");
            // Init tree for traversal
            initTreeTraversal();
            // go over all the links in the tree
            while((N = getNextInOrder()) != null)
            {
                if(N.w >= threshold)
                {
                    // and write them to the file
                    nodes = N.getNodes();
                    out.write(Integer.toString(nodes[0]));
                    for(int i=1;i<nodes.length;i++)
                    {
                        out.write(" " + Integer.toString(nodes[i]));
                    }
                    out.write("\n");
                }
            }
            out.flush();
            out.close();

        }
        catch (IOException ex)
        {
            Logger.getLogger(LinkSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Reads a linksetnode from the bytearray
     * @param in byte array that has to be turned into a linksetnode
     * @param type type of linksetnode that has to be created
     * @return desired linksetnode based on data in in.
     */
    public LinkSetNode readLinkSetNode(byte[] in, int type){
        switch(type)
        {
            case NODE_TYPE_BASIC:
                return new LinkSetNode(in);
            case NODE_TYPE_INTERVAL:
                System.out.print("Not supported yet!");
                System.exit(33);
                break;
            case NODE_TYPE_TIMED:
                System.out.print("Not supported yet!");
                System.exit(33);
                break;
            default:
                System.out.println("Error. Unknown link node type!");
                System.exit((33));
                break;
        }
        return null;
    }
    
    /**
     * Combines two linksetnodes
     * @param A linksetnode A
     * @param B linksetnode B
     * @return the combined linkset nodes
     */
    private LinkSetNode combine(LinkSetNode A, LinkSetNode B){
        switch(combineType)
        {
            case 0: //add
                A.w += B.w;
                break;
            case 1: // min
                A.w = Math.min(A.w, B.w);
                break;
            default:
                System.err.println("Unknown combine type");
                System.exit(2);
                break;
        }
        
        return A;
    }
    
    /**
     * writes the tree to disk. If the file already exists, the tree is merged
     * with the tree on disk
     * @param file destination file
     */
    public void writePartFile(String file){
        if(T.size == 0)
        {
            System.out.println("Tree is empty. Not writing any files");
            return;
        }
        try
        {
            File f = new File(file);
            // Check whether the file already exists.
            // If the file exists, the current tree has to be merged with the tree
            // that is in the file.
            if(f.exists())
            {
                int type = 0;
                // create temp file
                File t = new File(f + ".tmp");
                // Open input stream for existing file
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                // Open output stream for the temp file
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(t));
                // declare buffers
                byte[] b = new byte[3];
                // read preamble
                in.read(b);
                // store linksetnode type
                type = (int)b[0];
                // write preamble
                out.write(b);
                // set the buffer to the right size
                b = new byte[T.root.getByteArrayLength()];
                // nodeF is the LinkSetNode in the file
                LinkSetNode nodeF;
                // nodeT is the LinkSetNode in the current tree
                LinkSetNode nodeT;
                // init tree for traversal
                initTreeTraversal();
                // get next node in order for current tree
                nodeT = getNextInOrder();
                // read next node in order for the tree on disk
                in.read(b);
                nodeF = readLinkSetNode(b,type);//new HyperLinkSetNode(b);
                // declare flags to signify the end of the tree on disk
                // or current tree
                boolean doneF = false;
                boolean doneT = false;
                // Do some time keeping
                long start = System.currentTimeMillis();
                while(!doneF || !doneT)
                {
                    if(doneF && !doneT)
                    {
                        while(nodeT != null)
                        {
                            out.write(nodeT.toByteArray());
                            nodeT = getNextInOrder();
                        }
                        doneT = true;
                        continue;
                    }
                    while(!doneT && nodeT.compareTo(nodeF) < 0)
                    {
                        out.write(nodeT.toByteArray());
                        nodeT = getNextInOrder();
                        if(null == nodeT)
                        {
                            doneT = true;
                        }
                    }
                    if((!doneT && !doneF) && nodeT.compareTo(nodeF) == 0)
                    {
                        combine(nodeT,nodeF);
                        out.write(nodeT.toByteArray());
                        nodeT = getNextInOrder();
                        if(null == nodeT)
                        {
                            doneT = true;
                        }
                        if(in.read(b) == -1)
                        {
                            doneF = true;
                        }
                        else
                        {
                            nodeF = readLinkSetNode(b,type);//nodeF.updateNodeSet(b);
                        }
                        if(doneT && doneF)
                            continue;
                    }
                    if(doneT && !doneF)
                    {
                        out.write(nodeF.toByteArray());
                        while(in.read(b) != -1)
                        {
                            nodeF = readLinkSetNode(b,type);//nodeF.updateNodeSet(b);
                            out.write(nodeF.toByteArray());
                        }
                        doneF = true;
                        continue;
                    }
                    while(!doneF && nodeF.compareTo(nodeT) < 0)
                    {
                        out.write(nodeF.toByteArray());
                        if(in.read(b) == -1)
                        {
                            doneF = true;
                        }
                        else
                        {
                            nodeF = readLinkSetNode(b,type);//nodeF.updateNodeSet(b);
                        }
                    }
                }
                // Flush and close output
                out.flush();
                out.close();
                // Close input
                in.close();
                // delete the tree file
                f.delete();
                // rename the temp file to the tree file
                t.renameTo(f);
            }
            else // the file does not exist, so the tree can simply be written to file
            {
                // open output file
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                // write preamble to the file
                byte[] b = T.root.getPreamble();
                out.write(b);
                // init tree for traversal
                initTreeTraversal();
                LinkSetNode N = null;
                // go over the linkset entries and write them to the file
                while((N = getNextInOrder()) != null)
                {
                    out.write(N.toByteArray());
                }
                // flush and close output file
                out.flush();
                out.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Quite strange way to write the file to a real adjacency matrix. Involves flipping the tree
     * etc.
     * @param file
     */
    public void toFile(String file){
        T.node = T.root;
        T.nodes.clear();
        try
        {
            long start = System.currentTimeMillis();
            System.out.println("Writing upper half to disk");
            String lowerF = (file.substring(0, file.length()-4) + "-lower" + file.substring(file.length()-4));
            // First write the upper half of the adjacency matrix;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(lowerF));
            byte[] b = new byte[4];
            LinkSetNode n = T.getNextInOrder();
            int cs = -1;
            int v;
            while(n!=null)
            {
                v = n.s;
                if(v != cs)
                {
                    if(cs != -1)
                    {
                        writeBreak(out,b);
                    }
                    cs = v;
                    writeNode(out,b,v);
                }
                writeLink(out,b,n);
                n = T.getNextInOrder();
            }
            writeBreak(out,b);
            out.flush();
            out.close();
            System.out.println("done! That took me " + (System.currentTimeMillis()-start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("Flipping tree...");
            // Now flip the tree and copy the lower half per line followed by the upper half;
            // We must look out for nodes that have all their links in only one half of the adj matrix
            flipTree();
            System.out.println("done! That took me " + (System.currentTimeMillis()-start) + " ms");
            start = System.currentTimeMillis();
            T.node = T.root;
            T.nodes.clear();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(lowerF));
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bin = new byte[4];
            // The first row consist entirely of upper half entries.
            System.out.println("Copying upper half and adding lower half...");
            n = T.getNextInOrder();
            int nextUpper = n.s;
            writeNode(out,b,nextUpper);
            writeLink(out,b,n);
            n = T.getNextInOrder();
            while(nextUpper == n.s)
            {
                writeLink(out,b,n);
                n = T.getNextInOrder();
            }
            writeBreak(out,b);
            nextUpper = n.s;
            int nextLowerT = -1;
            int nextLower = readInt(in,bin);
            while(n != null)
            {
                if(nextLower <= nextUpper)
                {
                    writeNode(out,b,nextLower);
                    while( (v = readInt(in,bin)) >= 0)
                    {
                        writeNode(out,b,v);
                    }
                    nextLowerT = readInt(in,bin);
                }
                if(nextUpper <= nextLower)
                {
                    if(nextUpper < nextLower)
                    {
                        writeNode(out,b,nextUpper);
                    }
                    while(n != null && nextUpper == n.s)
                    {
                        writeLink(out,b,n);
                        n = T.getNextInOrder();
                    }
                    if(n != null)
                    {
                        nextUpper = n.s;
                    }
                }
                writeBreak(out,b);
                if(nextLowerT >= 0)
                {
                    nextLower = nextLowerT;
                }
            }
            // Write out remaining lower half entries
            v = -1;
            if(nextLower != -2)
            {
                writeNode(out,b,nextLower);
            }
            while( (v = readInt(in,bin)) != -2)
            {
                writeNode(out,b,v);
            }
            if(v != -2)
            {
                writeBreak(out, b);
            }
            out.flush();
            out.close();
            in.close();
            System.out.println("done! That took me " + (System.currentTimeMillis()-start) + " ms");
            start = System.currentTimeMillis();
            File f = new File(lowerF);
            f.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    RBTree T;
}
/**
 * Implements a Red and Black Tree
 * source: en.literateprograms.org/Red-black_tree_(Java)
 */
class RBTree{

    public RBTree(){
        root = null;
    }
    public RBTree(LinkSetNode _template) {
        template = _template;
        if(template instanceof TimedLinkSetNode)
        {
            dummy = ((TimedLinkSetNode)template).newNode(0, 0, NodeColor.RED, null, null,0l);
        }
        else
        {
            dummy = template.newNode(0, 0, NodeColor.RED, null, null);
        }
        root = null;
    }
    public long size() {
        return size;
    }
    public void initTreeTraversal(){
        nodes.clear();
        node = root;
    }
    public LinkSetNode getNextInOrder(LinkSetNode node) {
        nodes.clear();
        LinkSetNode returnNode;
        while (!nodes.isEmpty() || null != node) {
            if (null != node)
            {
                nodes.push(node);
                node = node.left;
            }
            else
            {
                node = nodes.pop();
                returnNode = node;
                node = node.right;
                return returnNode;
            }
        }
        return null;
    }
    public LinkSetNode getNextInOrder(){
        LinkSetNode returnNode;
        while (!nodes.isEmpty() || null != node) {
            if (null != node)
            {
                nodes.push(node);
                node = node.left;
            }
            else
            {
                node = nodes.pop();
                returnNode = node;
                node = node.right;
                return returnNode;
            }
        }
        return null;
    }
    public void traverseTreeInOrder(LinkSetNode node) {
        //incoming node is root
        Stack<LinkSetNode> nodes = new Stack();
        while (!nodes.isEmpty() || null != node) {
            if (null != node)
            {
                nodes.push(node);
                node = node.left;
            }
            else
            {
                node = nodes.pop();
                System.out.println(node);
                node = node.right;
            }
        }
    }
    private void replaceNode(LinkSetNode oldn, LinkSetNode newn) {
        if(oldn.parent == null){
            root = newn;
        }
        else
        {
            if(oldn == oldn.parent.left)
            {
                oldn.parent.left = newn;
            }
            else
            {
                oldn.parent.right = newn;
            }
        }
        if(newn != null)
        {
            newn.parent = oldn.parent;
        }

    }
    private void rotateLeft(LinkSetNode n) {
        LinkSetNode r = n.right;
        replaceNode(n,r);
        n.right = r.left;
        if(r.left != null){
            r.left.parent = n;
        }
        r.left = n;
        n.parent = r;
    }
    private void rotateRight(LinkSetNode n) {
        LinkSetNode l = n.left;
        replaceNode(n,l);
        n.left = l.right;
        if(l.right != null){
            l.right.parent = n;
        }
        l.right = n;
        n.parent = l;
    }
    private static NodeColor nodeColor(LinkSetNode n) {
        return n == null ? NodeColor.BLACK : n.color;
    }
    /**
     * Inserts the linksetnode into the tree or calls .foundMatch(NodeIn) on the node in the tree
     * that matches this node. If there is no matching linksetnode in the tree a clone of 
     * NodeIn is inserted. Returns true if the node is inserted, false if the tree already contained
     * the node.
     * @param NodeIn  The node that has to be inserted in the tree.
     * @return true if the node was inserted<br>false if the tree already contained the node
     */
    public boolean insert(LinkSetNode NodeIn){
        LinkSetNode insertedNode;

        if (root == null)
        {
            insertedNode = NodeIn.clone();
            insertedNode.color = NodeColor.RED;
            insertedNode.left = null;
            insertedNode.right = null;
            insertedNode.parent = null;
            root = insertedNode;
        }
        else
        {
            LinkSetNode n = root;
            while (true)
            {
                compResult = NodeIn.compareTo(n);
                if (compResult == 0)
                {
                    n.foundMatch(NodeIn);
                    return false;
                }
                else if (compResult < 0)
                {
                    if (n.left == null)
                    {
                        insertedNode = NodeIn.clone();
                        insertedNode.color = NodeColor.RED;
                        insertedNode.left = null;
                        insertedNode.right = null;
                        insertedNode.parent = null;
                        n.left = insertedNode;
                        break;
                    }
                    else
                    {
                        n = n.left;
                    }
                }
                else
                {
                    assert compResult > 0;
                    if (n.right == null)
                    {
                        insertedNode = NodeIn.clone();
                        insertedNode.color = NodeColor.RED;
                        insertedNode.left = null;
                        insertedNode.right = null;
                        insertedNode.parent = null;
                        n.right = insertedNode;
                        break;
                    }
                    else
                    {
                        n = n.right;
                    }
                }
            }
            insertedNode.parent = n;
        }
        insertCase1(insertedNode);
        size++;
        return true;
    }
    public void insert(int s, int d, long time) {
        if(s > d)
        {
            dummy.s = s;
            dummy.d = d;
        }
        else
        {
            dummy.s = d;
            dummy.d = s;
        }
        ((TimedLinkSetNode)dummy).timematches.time = time;
        if (root == null)
        {
            root = ((TimedLinkSetNode)template).newNode(s, d, NodeColor.RED, null, null, time);
            insertCase1(root);
        }
        else
        {
            LinkSetNode n = root;
            while (true)
            {
                compResult = dummy.compareTo(n);
                if (compResult == 0)
                {
                    n.foundMatch((TimedLinkSetNode)dummy);
                    return;
                }
                else if (compResult < 0)
                {
                    if (n.left == null)
                    {
                        n.left = ((TimedLinkSetNode)template).newNode(s, d, NodeColor.RED, null, null, time);
                        n.left.parent = n;
                        insertCase1(n.left);
                        break;
                    }
                    else
                    {
                        n = n.left;
                    }
                }
                else
                {
                    assert compResult > 0;
                    if (n.right == null)
                    {
                        n.right = ((TimedLinkSetNode)template).newNode(s, d, NodeColor.RED, null, null, time);
                        n.right.parent = n;
                        insertCase1(n.right);
                        break;
                    }
                    else
                    {
                        n = n.right;
                    }
                }
            }
        }
        size++;
    }
    public void insert(int s, int d) {
        if(s > d)
        {
            dummy.s = s;
            dummy.d = d;
        }
        else
        {
            dummy.s = d;
            dummy.d = s;
        }
        if (root == null)
        {
            root = template.newNode(s,d,NodeColor.RED,null,null);
            insertCase1(root);
        }
        else
        {
            LinkSetNode n = root;
            while (true)
            {
                compResult = dummy.compareTo(n);
                if (compResult == 0)
                {
                    n.foundMatch(dummy);
                    return;
                }
                else if (compResult < 0)
                {
                    if (n.left == null)
                    {
                        n.left = template.newNode(s,d,NodeColor.RED,null,null);
                        n.left.parent = n;
                        insertCase1(n.left);
                        break;
                    }
                    else
                    {
                        n = n.left;
                    }
                }
                else
                {
                    assert compResult > 0;
                    if (n.right == null)
                    {
                        n.right = template.newNode(s,d,NodeColor.RED,null,null);
                        n.right.parent = n;
                        insertCase1(n.right);
                        break;
                    }
                    else
                    {
                        n = n.right;
                    }
                }
            }
        }
        size++;
    }
    private void insertCase1(LinkSetNode n) {
        if (n.parent == null)
        {
            n.color = NodeColor.BLACK;
        }
        else
        {
            insertCase2(n);
        }
    }
    private void insertCase2(LinkSetNode n) {
        if (nodeColor(n.parent) == NodeColor.BLACK)
        {
            return; // Tree is still valid
        }
        else
        {
            insertCase3(n);
        }
    }
    void insertCase3(LinkSetNode n) {
        if (nodeColor(n.uncle()) == NodeColor.RED)
        {
            n.parent.color = NodeColor.BLACK;
            n.uncle().color = NodeColor.BLACK;
            n.grandparent().color = NodeColor.RED;
            insertCase1(n.grandparent());
        }
        else
        {
            insertCase4(n);
        }
    }
    void insertCase4(LinkSetNode n) {
        if (n == n.parent.right && n.parent == n.grandparent().left)
        {
            rotateLeft(n.parent);
            n = n.left;
        }
        else if (n == n.parent.left && n.parent == n.grandparent().right)
        {
            rotateRight(n.parent);
            n = n.right;
        }
        insertCase5(n);
    }
    void insertCase5(LinkSetNode n) {
        n.parent.color = NodeColor.BLACK;
        n.grandparent().color = NodeColor.RED;
        if (n == n.parent.left && n.parent == n.grandparent().left)
        {
            rotateRight(n.grandparent());
        } else
        {
            assert n == n.parent.right && n.parent == n.grandparent().right;
            rotateLeft(n.grandparent());
        }
    }
    private static LinkSetNode maximumNode(LinkSetNode n) {
        assert n != null;
        while (n.right != null) {
            n = n.right;
        }
        return n;
    }
    public void delete(LinkSetNode n) {
        if (n == null)
            return;  // Key not found, do nothing
        if (n.left != null && n.right != null) {
            // Copy key/value from predecessor and then delete it instead
            LinkSetNode pred = maximumNode(n.left);
            n.s = pred.s;
            n.d = pred.d;
            n.w = pred.w;
            n = pred;
        }
        assert n.left == null || n.right == null;
        LinkSetNode child = (n.right == null) ? n.left : n.right;
        if (nodeColor(n) == NodeColor.BLACK)
        {
            n.color = nodeColor(child);
            deleteCase1(n);
        }
        replaceNode(n, child);
        if (nodeColor(root) == NodeColor.RED)
        {
            root.color = NodeColor.BLACK;
        }
        size--;
    }
    private void deleteCase1(LinkSetNode n) {
        if (n.parent == null)
            return;
        else
            deleteCase2(n);
    }
    private void deleteCase2(LinkSetNode n) {
        if (nodeColor(n.sibling()) == NodeColor.RED) {
            n.parent.color = NodeColor.RED;
            n.sibling().color = NodeColor.BLACK;
            if (n == n.parent.left)
                rotateLeft(n.parent);
            else
                rotateRight(n.parent);
        }
        deleteCase3(n);
    }
    private void deleteCase3(LinkSetNode n) {
        if (nodeColor(n.parent) == NodeColor.BLACK &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.BLACK &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            deleteCase1(n.parent);
        }
        else
            deleteCase4(n);
    }
    private void deleteCase4(LinkSetNode n) {
        if (nodeColor(n.parent) == NodeColor.RED &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.BLACK &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.parent.color = NodeColor.BLACK;
        }
        else
            deleteCase5(n);
    }
    private void deleteCase5(LinkSetNode n) {
        if (n == n.parent.left &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.RED &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.sibling().left.color = NodeColor.BLACK;
            rotateRight(n.sibling());
        }
        else if (n == n.parent.right &&
                 nodeColor(n.sibling()) == NodeColor.BLACK &&
                 nodeColor(n.sibling().right) == NodeColor.RED &&
                 nodeColor(n.sibling().left) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.sibling().right.color = NodeColor.BLACK;
            rotateLeft(n.sibling());
        }
        deleteCase6(n);
    }
    private void deleteCase6(LinkSetNode n) {
        n.sibling().color = nodeColor(n.parent);
        n.parent.color = NodeColor.BLACK;
        if (n == n.parent.left) {
            assert nodeColor(n.sibling().right) == NodeColor.RED;
            n.sibling().right.color = NodeColor.BLACK;
            rotateLeft(n.parent);
        }
        else
        {
            assert nodeColor(n.sibling().left) == NodeColor.RED;
            n.sibling().left.color = NodeColor.BLACK;
            rotateRight(n.parent);
        }
    }
    public void print() {
        printHelper(root, 0);
    }

    private static void printHelper(LinkSetNode n, int indent) {
        if (n == null) {
            System.out.print("<empty tree>");
            return;
        }
        if (n.right != null) {
            printHelper(n.right, indent + INDENT_STEP);
        }
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        if (n.color == NodeColor.BLACK)
            System.out.println(n.toString());
        else
            System.out.println("<" + n.toString() + ">");
        if (n.left != null) {
            printHelper(n.left, indent + INDENT_STEP);
        }
    }
    private static final int INDENT_STEP = 4;
    int compResult;
    
    LinkSetNode root;
    Stack<LinkSetNode> nodes = new Stack<LinkSetNode>();
    LinkSetNode node;
    LinkSetNode template;
    LinkSetNode dummy;
    public long size = 0;

}


