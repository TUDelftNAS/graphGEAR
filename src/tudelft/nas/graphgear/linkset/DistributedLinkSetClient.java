/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.linkset;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.utils.ArgumentParser;



/**
 *
 * @author Ruud
 * The DistributedLinkSetClient maintains a TCP connection to the server and inserts
 * all links it receives from the server in the linkset
 */
public class DistributedLinkSetClient {

    /**
     * Reads linksetnodes from a byte array and puts them into the link set
     * @param buffer the byte buffer
     * @return true if the max has been reached, false otherwise.
     */
    private boolean processData(byte[]  buffer){
        byte[] linkbuffer = new byte[12];
        int i =0;
        while(i < buffer.length)
        {
            System.arraycopy(buffer, i, linkbuffer, 0, linkbuffer.length);
            i += linkbuffer.length;
            LinkSetNode lsn = new LinkSetNode(linkbuffer);
            if(lsn.s == -26)
            {
                System.out.println("Received exit link.");
                System.out.println("Number of received links: " + recC);
                System.out.println("Size of tree: " + L.size());
                return true;
            }
            else
            {
                recC++;
                L.addLink(lsn);
                if(L.size() > maxSize)
                {
                    System.out.println("Tree exceeds maxSize.");
                    System.out.println("Number of received links: " + recC);
                    System.out.println("Size of tree: " + L.size());
                   return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Estimates the number of links that can be inserted in the tree. This uses
     * sun's operatingsystemMXbean which might not work on all JVMs. Check first!
     * A link without time information contains three integers and a byte indicating
     * the merge type. The node it is  packed in contains 3 pointers and a class pointer
     * so in total a link will take some 45 bytes.
     * @return 
     */
    private long estimateMaxCapacity(){
        int procs = Runtime.getRuntime().availableProcessors();
        OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        long mem = b.getTotalPhysicalMemorySize();
        return (mem/procs)/45l;
    }
    
    /**
     * make connection and start storing all links that are send over the connection
     * @param dest address of the server
     * @param _maxSize maximum number of links that can be stored by this client
     */
    public void start(String dest, long _maxSize){
        if(_maxSize < 0)
        {
            maxSize = estimateMaxCapacity();
            System.out.println("Maximum estimated size: " + maxSize);
        }
        else
        {
            maxSize = _maxSize;
        }
        try {
            // send request
            byte[] rb = new byte[12];
            InetAddress address = InetAddress.getByName(dest);
            System.out.println("Going to connect.");
            Socket socket = new Socket(address,4466);
            System.out.println("Connected. Opening stream");
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            System.out.println("Stream open.");
            
            // Open output stream and send maxSize
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            byte[] setSize = new byte[1024];
            ByteBuffer bb = ByteBuffer.wrap(setSize);
            bb.putLong(maxSize);
            out.write(setSize);
            out.flush();
            
            // Create read buffers and read stream till it closes
            byte[] readbuffer = new byte[120];
            byte[] buffer = new byte[1100];
            byte[] processBuffer = new byte[960];
            int r = 0;
            boolean done = false;
            int bufferLength = 0;
            while(!done)
            {
                while(bufferLength < 960)
                {
                    r = in.read(readbuffer);
                    System.arraycopy(readbuffer, 0, buffer, bufferLength, r);
                    bufferLength += r;
                }
                System.arraycopy(buffer, 0, processBuffer, 0, 960);
                for(int j=960;j<bufferLength;j++)
                {
                    buffer[j-960] = buffer[j];
                }
                bufferLength = bufferLength-960;
                done = processData(processBuffer);
            }
            System.out.println("Done");
            socket.close();
        } 
        catch (Exception ex) 
        {
            System.err.println("An error has happened!");
            Logger.getLogger(DistributedLinkSetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Start with argument string containing the ip address of the server and the number of 
     * links that should be stored here (a negative size will let the client estimate the number
     * of links it can store) so: "ip x.x.x.x size y"
     * @param args 
     */
    public static void main(String args[]){
        if(args.length != 1)
        {
            System.out.println("Specify IP address of the server and the max. tree size");
        }
        else
        {
            ArgumentParser ap = new ArgumentParser(args[0]);
            DistributedLinkSetClient dlsc = new DistributedLinkSetClient();
            dlsc.start(ap.getStringArgument("ip", true),ap.getLongArgument("size", true));
        }
    }
    LinkSet L = new LinkSet();
    long recC = 0;
    long maxSize;
}
