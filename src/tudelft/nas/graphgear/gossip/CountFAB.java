/*
 * Message Combining Counting (or something like that)
 */

package tudelft.nas.graphgear.gossip;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Ruud van de Bovenkamp
 */
public class CountFAB extends NodeFAB {
    static final int REQUEST = 1;
    static final int REPLY = 0;
    static final int REJECT = 2;
    public CountFAB(Random gen){
        super(gen);
    }
    
    /**
     * A tag is just an identifier
     * @return return tag
     */
    @Override
    public int getTag(){
        return 2;
    }
    
    /**
     * The monitor can be used to observe the behaviour of the node
     * @return this FAB
     */
    @Override
    public NodeFAB monitor(){
        return this;
    }
    
    /**
     * Interact in direct mode
     * @param peer FAB to interact with
     */
    public void interact(CountFAB peer){
        receive(peer.receive(send()));
    }
    
    /**
     * Interact in message mode. A random peer is selected and then contacted
     */
    @Override
    public void interact(){
        Node P = N.getNeighbour(gen.nextInt(N.getDegree()));
        CountFAB cf = (CountFAB) P.getFAB(0);
        interact(cf);
    }
    
    /**
     * Create a request message
     * @return request message
     */
    @Override
    public int[] send(){
        int[] res = new int[7];
        res[0] = REQUEST; // Request
        res[1] = MC;
        res[2] = MF;
        res[3] = MT;
        res[4] = id;
        res[5] = depth;
        res[6] = identifier;
        MF = SF;
        MC = SC;
        MT = 0;
        return res;
    }
    
    /**
     * Resets the count. This can be in response of a link removal or
     * becaus the node joins a different army (in gossipico)
     * @param c state count
     */
    public void resetCount(int c){
        MF = 1;
        MC = c;
        MT = 1;
        SC = c;
        SF = 1;

        depth = 0;
        
        if(c == 0)
        {
            MT = 0;

        }
    }
    
    /**
     * Reset count
     */
    public void resetCount(){
        MF = 1;
        MC = 1;
        MT = 1;
        SC = 1;
        SF = 1;

        depth = 0;

    }
    
    /**
     * Checks whether the message should be accepted. Messages are
     * accepted if they come from the same army
     * @param m the message
     * @return true if accepted, false othewise
     */
    public boolean accept(int[] m){
        return (m[6] == identifier);
    }
    
    /**
     * Receive a message and create reply if needed
     * @param m the incoming message
     * @return the reply
     */
    @Override
    public int[] receive (int[] m) {
        if(m == null)
            return null;
        int[] reply = new int[3];
        if(m[0] == REQUEST) // message is a request
        {
            if(!accept(m))
            {
                m[0] = 2;
                return m;
            }
            reply[0] = -1; // reply not valid yet
            int situation = m[3]*2 + MT; // s.MT*2 + MT
            switch(situation)
            {
                case 3: // Ts = 1, Tr = 1 => Combine
                    MC = MC + m[1]; // MC = MC + s.MC
                    MF = MF + m[2]; // MF = MF + s.MF
                    depth = depth > m[5] ? depth + 1: m[5] + 1;
                    break;
                case 2: // Ts = 1, Tr = 0 => take over message
                    MC = m[1];// MC = s.MC
                    MF = m[2];// MF = s.MF
                    MT = m[3];// MT = s.MT
                    id = m[4]; // id = s.id
                    depth = m[5];
                    break;
                case 1: // Ts = 0, Tr = 1 => discard
                    if(MF > m[2]) // MF > s.MF
                    {
                        reply[0] = REPLY; // reply valid
                        reply[1] = MC;
                        reply[2] = MF;
                    }
                    break;
                case 0: // Ts = 0, Tr = 0 => take over if fresher
                    if(m[2] > MF) // s.MF > MF
                    {
                        MC = m[1];// MC = s.MC
                        MF = m[2];// MF = s.MF
                    }
                    if(MF > m[2]) // MF > s.MF
                    {
                        reply[0] = REPLY; // reply valid
                        reply[1] = MC;
                        reply[2] = MF;
                    }
                    break;
            }
            // update node state if message is fresher
            if(MF > SF)
            {
                SF = MF;
                SC = MC;
                countUpdate = true;
            }
            if(reply[0] != -1)
            {
                return reply;
            }
        }
        if(m[0] == REPLY) // Message is a reply to an earlier request
        {
            if(MT != 1)
            {
                MC = m[1];
                MF = m[2];
            }
            // update node state if message is fresher
            if(MF > SF)
            {
                SF = MF;
                SC = MC;
                countUpdate = true;
            }
        }
        if(m[0] == REJECT)
        {
            MC = m[1];
            MF = m[2];
            MT = m[3];
            id = m[4];
            depth = m[5];
            identifier = m[6]; 
        }
        return null;
    }

    
    /**
     * Initialises the FAB. This method is general, not all values
     * are used necessarily.  
     * @param n the node (owner) of the FAB
     * @param flag a flag that can be used in the initialisation
     * @param _fab the FAB id
     */
    @Override
    public void init(Node n, int flag, int _fab) {
        MC = 1;
        MF = 1;
        MT = 1;
        SC = 1;
        SF = 1;
        if(flag == -1)
        {
            MT = 0;
            MC = 0;
            SC = 0;
        }
        fab = _fab;
        N = n;
    }

    /**
     * Method to request debug strings
     * @return debug string
     */
    @Override
    public String debug() {
        return (N.id + "(" + SC + ")");
    }

    @Override
    NodeFAB make() {
        return new CountFAB(gen);
    }
    
    /**
     * This method is used in simulation mode to check whether
     * the FAB has converged. In a deployment scenario this is, of course,
     * not possible
     * @param nw the network the algorithm is running in
     * @param fab the FAB
     * @return true if done, false otherwise
     */
    @Override
    public boolean done(ArrayList<Node> nw, int fab) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int v;
        for(Node n : nw)
        {
            v = ((CountFAB)n.getFAB(fab)).SC;
            max = v > max ? v : max;
            min = v < min ? v : min;
        }
        return max == min && min == nw.size();
    }
    Node N;
    public int MC;
    public int MF;
    public int MT;
    public int SC;
    public int SF;
    public int ICpeer = -1;
    public int id = -1;
    public int depth = 0;
    public int identifier = -1;
    public int layer = -1;
    boolean countUpdate = false;
    public int numComb = 0;
}
