/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.linkset;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rvandebovenkamp
 * A streamed link set is a datafile that behaves the same as a link set
 */
public class StreamedLinkSet extends LinkSet{
    /**
     * Creates a new streamedLinkSet from the specified file
     * @param _file file location of the link set
     */
    public StreamedLinkSet(String _file){
        file = _file;
    }
    
    /**
     * Readies the tree for traversal
     */
    @Override
    public void initTreeTraversal(){
        try
        {
            if(memory != null)
            {
                memory.rewind();
                return;
            }
            if(in != null)
            {
                in.close();
            }
            
            in = new BufferedInputStream(new FileInputStream(file));
            // Read the size of the nodes (size of the nodes is stored in the last
            // byte of the preamble
            b = new byte[3];
            in.read(b);
            // Get Link type info
            type = b[0];
            // Create a buffer with the appropriate size
            b = new byte[b[2]];
            if(fromMemory)
            {
                File F = new File(file);
                int fs = (int)(F.length()-3);
                System.out.println("allocating byte buffer of size: " + fs);
                memory = ByteBuffer.allocateDirect(fs);
                System.out.println("Reading file contents");
                while(in.read(b) == b.length)
                {
                    memory.put(b);
                }
                System.out.println("Done");
                memory.rewind();
            }
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Prints the size of the file and the number of links in the file the console
     */
    public void printFileStats(){
        File F = new File(file);
        long fs = F.length()-3;
        int ls = b.length;
        System.out.println("File size: " + fs + " bytes. Number of links: " + (fs/ls));
    }
    
    /**
     * Counts the number of links that have a linkweight strictly higher than the threshold
     * @param threshold threshold value
     * @return the number of links that have a linkweight strictly higher than the threshold
     */
    public int getNumberOfLinks(int threshold){
        initTreeTraversal();
        LinkSetNode L = getNextInOrder();
        int c = 0;
        while(L != null)
        {
            if(L.w > threshold)
            {
                c++;
            }
            L = getNextInOrder();
        }
        return c;
    }
    
    /**
     * Closes the data file
     */
    public void close(){
        try 
        {
            in.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(StreamedLinkSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prints the tree to the console (don't do this if the tree is big)
     */
    public void printStreamedTree(){
        initTreeTraversal();
        LinkSetNode L = getNextInOrder();
        while(L != null)
        {
            System.out.println(L);
            L = getNextInOrder();
        }
    }
    
    /**
     * Gets the next link in the order of the tree
     * @return next linksetnode
     */
    @Override
    public LinkSetNode getNextInOrder(){
        try
        {
        if(memory == null)
        {
            int r = in.read(b);
            if(r != b.length)
            {
                in.close();
                return null;
            }
        }
        else
        {
            if(memory.position() == memory.capacity())
            {
                return null;
            }
            memory.get(b);
        }
        switch(type)
        {
            case 1:
                return new LinkSetNode(b);
            case 3:
                return new TimedLinkSetNode(b);
            default:
                System.out.println("Error. Unknown LinkSetNode type in preamble");
                in.close();
                System.exit(22);
                break;
        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    String file;
    byte[] b;
    byte type;
    BufferedInputStream in;
    ByteBuffer memory = null;
    public boolean fromMemory = false;
}
