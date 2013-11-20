/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.linkset;

import java.nio.ByteBuffer;
import java.util.Date;
import tudelft.nas.graphgear.utils.NodeColor;

/**
 *
 * @author Ruud van de Bovenkamp
 * A Timed linkset node will keep the time information of every link weight increment in a certain
 * time interval
 */
public class TimedLinkSetNode extends LinkSetNode{
    /**
     * Creates a new timedlinksetnode from a byte array containing source destination and weight
     * @param in byte array containing source destination and weight
     */
    public TimedLinkSetNode(byte[] in){
        ByteBuffer bb = ByteBuffer.wrap(in);
        s = bb.getInt();
        d = bb.getInt();
        w = bb.getInt();
    }
    
    /**
     * Creates a new TimeLinkSetNode with the first time event at _time and a duration interval
     * specified by interval
     * @param _s source of the link
     * @param _d destination of the link
     * @param _interval time interval within which the increments are kept
     * @param _time time of creation
     */
    public TimedLinkSetNode(int _s, int _d, long _interval, long _time){
        super(_s,_d);
        interval = _interval;
        timematches = new timestamp(_time,null);
    }
    
    /**
     * Creates a new TimeLinkSetNode with the first time event at _time and a duration interval
     * specified by interval with tree information
     * @param s source of the link
     * @param d destination of the link
     * @param interval time interval within which the increments are kept
     * @param time time of creation
     * @param nodeColor colour of the node
     * @param left left child of this node
     * @param right right child of this node
     */
    public TimedLinkSetNode(int s, int d, NodeColor nodeColor, LinkSetNode left, LinkSetNode right,long interval,long time){
        super(s,d,nodeColor,left,right);
        this.interval = interval;
        this.timematches = new timestamp(time,null);
    }
    
    /**
     * The preamble consists of the following three bytes <br>0: id<br>1:empty<br>3:length of byte array
     * @return 
     */
    @Override
    public byte[] getPreamble(){
        return new byte[]{3,0,(byte)getByteArrayLength()};
    }
    @Override
    public byte[] toByteArray(){
        byte[] res = new byte[getByteArrayLength()];
        ByteBuffer bb = ByteBuffer.wrap(res);
        bb.putInt(s);
        bb.putInt(d);
        bb.putInt(w);
        return res;
    }
    @Override
    public int getByteArrayLength(){
        return 12;
    }
    public LinkSetNode newNode(int s, int d, NodeColor nodeColor, LinkSetNode left, LinkSetNode right, long time){
        return new TimedLinkSetNode(s,d,nodeColor,left,right,interval,time);
    }
    @Override
    public TimedLinkSetNode clone(){
        TimedLinkSetNode res = new TimedLinkSetNode(s,d,interval,timematches.time);
        timestamp ts = timematches;
        res.timematches = new timestamp(ts.time,null);
        timestamp tsres = res.timematches;
        ts = ts.next;
        while(ts != null)
        {
            tsres.next = new timestamp(ts.time,null);
            tsres = tsres.next;
            ts = ts.next;
        }
        res.w = w;
        return res;
    }
    
    @Override
    public String toString(){
        String res = (this.s + " -> " + this.d + " (" + this.w + ")");
        timestamp tm = timematches;
        Date d;
        while(tm != null)
        {
            d = new Date(tm.time);
            res += d.toString() + ", ";
            tm = tm.next;
        }
        return res.substring(0,res.length()-2);
    }
    
    @Override
    public void foundMatch(LinkSetNode n){
        TimedLinkSetNode tn = (TimedLinkSetNode)n;
        // The new time is placed at the beginning of the string of times
        timematches = new timestamp(tn.timematches.time,timematches);
        timestamp tm = timematches;
        int c =1;
        // Go over the entire string of timestamps and count how
        // many within the interval. If a time stamp is without the
        // interval it is cut of from the string
        while(tm.next != null)
        {
            if((timematches.time - tm.next.time) > interval)
            {
                tm.next = null;
            }
            else
            {
                c++;
                tm = tm.next;
            }
        }
        // Remeber the maximum number of matches within a time interval
        w = c > w ? c : w;
    }
    // timestamps are linked together to form a chain of timestamps that
    // are not more than interval ms seperated from eachother
    timestamp timematches;
    long interval;
}
/**
 * Internal class used to create a linked list of time stamps
 * @author rvandebovenkamp
 */
class timestamp {
    public timestamp(long time, timestamp next){
        this.time = time;
        this.next = next;
    }
    public long time;
    timestamp next;
}
