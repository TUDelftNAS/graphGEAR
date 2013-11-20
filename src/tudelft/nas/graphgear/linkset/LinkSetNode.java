/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.linkset;

import java.nio.ByteBuffer;
import tudelft.nas.graphgear.utils.NodeColor;

/**
 *
 * @author Ruud van de Bovenkamp
 * A linkset node is a node in the red and black tree. It has a colour and pointers
 * to family members.
 */
public class LinkSetNode implements Comparable <LinkSetNode>{
    /**
     * Creates new linksetnode from a byte array
     * @param in byte array (4 ints represented as bytes)
     */
    public LinkSetNode(byte[] in){
        int index = -1;
        ByteBuffer bb = ByteBuffer.wrap(in);
        s = bb.getInt();
        d = bb.getInt();
        w = bb.getInt();
    }
    public LinkSetNode(){}
    
    /**
     * Creates a new linksetnode. Note that the source and destination are sorted
     * @param _s source
     * @param _d destination
     * @param _w weight
     */
    public LinkSetNode(int _s, int _d, int _w){
        if(_s > _d)
        {
            this.s = _s;
            this.d = _d;
        }
        else
        {
            this.s = _d;
            this.d = _s;
        }
        w = _w;
    }
    
    /**
     * Creates new linksetnode with weight 1. Note that the source and destination are sorted
     * @param _s source
     * @param _d destination
     */
    public LinkSetNode(int _s, int _d){
        if(_s > _d)
        {
            this.s = _s;
            this.d = _d;
        }
        else
        {
            this.s = _d;
            this.d = _s;
        }
        w = 1;
    }
    
    /**
     * Get the set of nodes that are linked by this link
     * @return array with the source and destination node of this link
     */
    public int[] getNodes(){
        int[] res = new int[2];
        res[0]  = s;
        res[1] = d;
        return res;
    }
    
    /**
     * Creates a new linksetnode with a specified colour and the left and right children set
     * @param s source
     * @param d destination
     * @param nodeColor node colour
     * @param left right child
     * @param right left child
     */
    public LinkSetNode(int s, int d, NodeColor nodeColor, LinkSetNode left, LinkSetNode right){
        if(s > d)
        {
            this.s = s;
            this.d = d;
        }
        else
        {
            this.s = d;
            this.d = s;
        }

        this.color = nodeColor;
        this.left = left;
        this.right = right;
    }
    
    /**
     * Processes a match between this linksetnode and another. Depending on the mergeType the content
     * of the two linkset nodes will be merged
     * @param m the other link set node
     */
    public void foundMatch(LinkSetNode m){
        switch(mergeType)
        {
            case MERGE_ADD:
                this.w += m.w;
                break;
            case MERGE_MIN:
                this.w = this.w < m.w ? this.w : m.w;
                break;
            default:
                System.err.println("unknown merge type");
                break;
        }
    }
    
    /**
     * Creates a new linkset node with the same merge type as the current node
     * @param s source of the link
     * @param d destination of the link
     * @param nodeColor link set node colour
     * @param left left child
     * @param right right child
     * @return new linkSetNode
     */
    public LinkSetNode newNode(int s, int d, NodeColor nodeColor, LinkSetNode left, LinkSetNode right){
        LinkSetNode res = new LinkSetNode(s,d,nodeColor,left,right);
        res.mergeType = mergeType;
        return res;
    }
    
    /**
     * Returns the grandparent of this node
     * @return the grandparent of this node
     */
    public LinkSetNode grandparent(){
        assert parent != null;
        assert parent.parent != null;
        return parent.parent;
    }
    
    /**
     * Returns the sibling of this node
     * @return the sibling of this node 
     */
    public LinkSetNode sibling(){
        assert parent != null;
        if(this == parent.left)
        {
            return parent.right;
        }
        else
        {
            return parent.left;
        }
    }
    
    /**
     * Returns the uncle of this node
     * @return the uncle of this node
     */
    public LinkSetNode uncle(){
        assert parent != null;
        assert parent.parent != null;
        return parent.sibling();
    }
    @Override
    public String toString(){
        return (this.s + " -> " + this.d + " (" + this.w + ")");
    }
    
    /**
     * Flips source and destination of the link.
     */
    public void flip(){
        int t = s;
        s = d;
        d = t;
    }
    /**
     * The preamble consists of the following three bytes <br>0: id<br>1:empty<br>3:length of byte array
     * @return 
     */
    public byte[] getPreamble(){
        return new byte[]{1,0,(byte)getByteArrayLength()};
    }
    /**
     * Creates a byte array of the linksetnode
     * @return byte array 
     */
    public byte[] toByteArray(){
        byte[] res = new byte[getByteArrayLength()];
        ByteBuffer bb = ByteBuffer.wrap(res);
        bb.putInt(s);
        bb.putInt(d);
        bb.putInt(w);
        return res;
    }
    public int getByteArrayLength(){
        return 12;
    }
    @Override
    public LinkSetNode clone(){
        LinkSetNode res = new LinkSetNode(s,d);
        res.w = w;
        res.mergeType = mergeType;
        return res;
    }
    public int compareTo(LinkSetNode o) {
        if(o.s == this.s)
        {
            if(o.d == this.d)
            {
                return 0;
            }
            if(o.d < this.d)
            {
                return 1;
            }
            return -1;
        }
        if(o.s < this.s)
        {
            return 1;
        }
        return -1;
    }
    public final static byte MERGE_ADD = 0;
    public final static byte MERGE_MIN = 1;
    public LinkSetNode left;
    public LinkSetNode right;
    public LinkSetNode parent;
    public int s = -1;
    public int d = -1;
    public int w = 1;
    public NodeColor color;
    public byte mergeType = MERGE_ADD;
}