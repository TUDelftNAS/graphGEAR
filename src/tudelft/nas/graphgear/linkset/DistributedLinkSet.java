/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.linkset;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.utils.ArgumentParser;

/**
 *
 * @author Ruud
 * The DistributedLinkSet is the server node of a distributedlinkset setup. It maintains
 * TCP connections to the clients. When the server node is started it waits until it has
 * enough clients connected and can then accept links
 */
public class DistributedLinkSet {
    public DistributedLinkSet(int n, int k){
        socketHandlers = new socketHandler[k];
    }
    /**
     * recursively builds the tree
     * @param b the upper and lower limits of the parts
     * @param pos is really just the index of the part starting at 1
     * @param dist the distance between this part and its left and right neighbours
     * @return the partLinkSet starting at the specified position
     */
    private partLinkSet getPartLinkSet(int[][] b , int pos, int dist){
        //System.out.println("pos: " + pos + " dist: " + dist);
        partLinkSet res =  new partLinkSet(b[pos-1][0],b[pos-1][1],pos);
        if(dist >= 1)
        {
            res.smaller = getPartLinkSet(b,pos-dist,dist/2);
            res.larger = getPartLinkSet(b,pos+dist,dist/2);
        }
        if(pos == b.length-1)
        {
            res.larger = new partLinkSet(b[pos][0],b[pos][1],pos+1);
        }
        return res;
    }
    
    /**
     * Returns the hash of a link between s and d. The hash is based on the uniform random number
     * generator. The concatenation of source and destination is used as a seed for the generator
     * and the hash is the next random integer
     * @param s link source
     * @param d link destination
     * @return hashed value
     */
    private int getHash(int s, int d){
        long l = (long)s << 32 | d & 0xFFFFFFFFL;
        gen.setSeed(l);
        return gen.nextInt(Integer.MAX_VALUE);
    }
    
    /**
     * Adds a link to the distributed linkset
     * @param L the link
     */
    private void addLink(LinkSetNode L){
        T.addLink(L,getHash(L.s,L.d));
    }
    
    /**
     * Prints the tree
     */
    private void printTree(){
        ArrayList<partLinkSet> Q = new ArrayList<>();
        Q.add(T);
        partLinkSet p;
        int c = 0;
        int lim = 1;
        while(!Q.isEmpty())
        {
            p = Q.remove(0);
            System.out.println(p);
                
            if(p.smaller != null)
            {
                Q.add(p.smaller);
            }
            if(p.larger != null)
            {
                Q.add(p.larger);
            }
        }
    }
    
    /**
     * Creates a tree that can store n links and has k parts
     * @param n number of links to store
     * @param k number of parts to use
     */
    public void createTree(int n, int k){
        if(Integer.bitCount(k) != 1)
        {
            System.err.println("number of parts must be a power of 2");
            System.exit(-1);
        }
        
        // get the maximum number of links that can be stored
        boolean failed = true;
        double maxLinks = 0d;
        while(failed)
        {
            maxLinks = 0d;
            failed = false;
            for(socketHandler h : socketHandlers)
            {
                if(h.maxSize < 0)
                {
                    failed = true;
                }
                maxLinks += h.maxSize;
            }
        }
        System.out.println("Maximum number of links I can store: " + maxLinks);
        int nodesPerPart = (int) Math.floor((double)n/(double)(k));
        int l = 0;
        int u = l + (int)Math.floor((((double)socketHandlers[0].maxSize)/maxLinks) * (double)n);
        int[][] b = new int[k][2]; 
        for(int i=0;i<k-1;i++)
        {
            b[i][0] = l;
            b[i][1] = u;
            l = u + 1;
            u = l + (int)Math.floor((((double)socketHandlers[i+1].maxSize)/maxLinks) * (double)n);
        }
        u = n;
        b[k-1][0] = l;
        b[k-1][1] = u;
        // creates the tree recursively with T being the centre part
        T = getPartLinkSet(b,k/2,k/4);
        System.exit(2);
    }
    
    /**
     * Blocks until all clients have connected. Misconfiguring the number
     * of expected clients and the number of running clients could lead to a
     * very long waiting time indeed.
     */
    private void collectClients(){
        try 
        {
            int i = 0;
            byte[] buf = new byte[2];
            byte[] sb = new byte[8];
            int req = 0;
            DatagramPacket packet = new DatagramPacket(buf,buf.length);
            System.out.println("Reachable at: " + InetAddress.getLocalHost());
            ServerSocket socket = new ServerSocket(4466);
            while(i<socketHandlers.length)
            {
                
                Socket clientSocket = socket.accept();
                socketHandlers[i] = new socketHandler(clientSocket);
                new Thread(socketHandlers[i]).start();
                i++;
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Test code to test performance
     * @param n number of nodes to create random links between
     * @param iter number of links to create
     */
    public void testTree(int n, int iter){
        Random r = new Random();
        long start = System.currentTimeMillis();
        for(int i = 0;i < iter;i++)
        {
            LinkSetNode lsn = new LinkSetNode(r.nextInt(n),r.nextInt(n));
            addLink(lsn);
            if(i % 1000000 == 0)
            {
                System.out.println(i + " done in " + (System.currentTimeMillis()-start) + " ms.");
            }
        }
        sendEndLinks();
        System.out.println("All done in " + (System.currentTimeMillis()-start) + " ms");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(26);
    }
    
    /**
     * Informs clients that the last link was sent
     */
    private void sendEndLinks(){
            LinkSetNode L = new LinkSetNode(-26,-26);
            for(int i=0;i<socketHandlers.length;i++)
            {
                for(int j =0;j<80;j++)
                {
                    socketHandlers[i].addLink(L.toByteArray());
                }
                socketHandlers[i].flush();
            }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 1)
        {
            System.out.println("I need some arguments: number of parts k, highest node id n, iterations it");
            System.out.println("I got " + args.length + " arguments: ");
            for(String s : args)
            {
                System.out.println(s);
            }
        }
        else
        {
            ArgumentParser ap = new ArgumentParser(args[0]);
            DistributedLinkSet dls = new DistributedLinkSet(Integer.MAX_VALUE,ap.getIntArgument("k", true));
            dls.collectClients();
            dls.createTree(Integer.MAX_VALUE, ap.getIntArgument("k", true));
            dls.testTree(ap.getIntArgument("n", true),ap.getIntArgument("it",true));
        }
    }
    Random gen = new Random();
    socketHandler[] socketHandlers;
    InetAddress[] linkSetAddress;
    int[] linkSetPorts;
    partLinkSet T = null;

    /**
     * The socketHandler maintains the connection to the client
     */
    private class socketHandler implements Runnable{
        public socketHandler(Socket S){
            socket = S;
            try 
            {
                out = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                byte[] sizeBytes = new byte[8];
                int r = in.read(sizeBytes);
                if(r!= sizeBytes.length)
                {
                    System.out.println("Couldn't read all bytes...");
                }
                else
                {
                    ByteBuffer bb = ByteBuffer.wrap(sizeBytes);
                    maxSize = bb.getLong();
                    System.out.println("maxSize: " + maxSize);
                }
            } 
            catch (IOException ex) 
            {
                System.err.println("error opening stream");
                Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void run() {
            System.out.println("Socket handler running for " + socket.toString());
            while(!done)
            {
                try 
                {
                    Thread.sleep(100);
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        /**
         * Flushes the output stream to the buffer
         */
        public synchronized void flush(){
            System.out.println("Flushing buffer");
            try 
            {
                byte[] temp = new byte[index];
                System.arraycopy(buffer, 0, temp, 0, index);
                out.write(temp);
                out.flush();
                
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
            }
            index = 0;
        }
        
        /**
         * Adds link to the part linkset. Effectively the method writes the bytes
         * to the output stream of the TPC socket.
         * @param in 
         */
        public synchronized void addLink(byte[] in){
            System.arraycopy(in, 0, buffer, index, in.length);
            index += in.length;
            if(index >= buffer.length)
            {
                try 
                {
                    out.write(buffer);
                } 
                catch (IOException ex) 
                {
                    System.out.println("Error happend while writing. Connection probabily closed. Exiting thread");
                    done = true;
                   
                    Logger.getLogger(DistributedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
                }
                index = 0;
            }
        }
        
        long maxSize = -1l;
        boolean send = false;
        boolean done = false;
        byte[] buffer = new byte[960];
        int index = 0;
        Socket socket;
        BufferedOutputStream out;
    }
    
    /**
     * The partlinkset is a part of a tree structure. If the offered link
     * does not fall in the interval of this partlinkset it is passed on to
     * the neighbour to the left or right.
     */
    private class partLinkSet{
        public partLinkSet(int min, int max, int id){
            this.min = min;
            this.max = max;
            this.id = id;
        }
        
        @Override
        public String toString(){
            return id + ": " + linkCounter +  " [" + min + "," + max + "]";
        }
        /**
         * Add a link to the linksetnode
         * @param L the link
         * @param hash the hash of the link
         */
        public void addLink(LinkSetNode L, int hash){
            if(hash < min) // link too small to store here
            {
                smaller.addLink(L,hash);
                return;
            }
            
            if(hash > max) // link too large to store here
            {
                larger.addLink(L, hash);
                return;
            }
            
            if(hash >= min && hash <= max) // store here (that is, send to client)
            {
                if(!socketHandlers[id-1].done)
                {
                    socketHandlers[id-1].addLink(L.toByteArray());
                }
            }
            else
            {
                System.out.println("Correct Part cannot be found for " + L + " with hash "+ hash+ " in " + this);
            }
        }
        int min;
        int max;
        int id = -1;
        private partLinkSet smaller = null;
        private partLinkSet larger = null;
        long linkCounter = 0;
    }
}
