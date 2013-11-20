/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

/**
 *
 * @author Ruud van de Bovenkamp
 */
/*
 * Beacon is an implementation of the beacon algorithm designe to speed
 * up the convergence of Count and similar algorithms
 */

import java.awt.Color;
import java.util.*;

/**
 *
 * @author Ruud van de Bovenkamp
 */
public class BeaconFAB extends NodeFAB{
    private static final int REQUEST = 1;
    private static final int REPLY_DEFEAT = 2;
    private static final int REPLY_PATH_UPDATE = 3;
    private static final int REPLY_ARMY_MEMBER = 4;
    
    /**
     * Creates a new BeaconFAB
     * @param gen a random number generator
     * @param _peerSelection peer selection type<br>0 : random <br>1:peer list
     */
    public BeaconFAB(Random gen, int _peerSelection){
        super(gen);
        peerSelection = _peerSelection;
    }
    
    /**
     * A tag is just an identifier
     * @return return tag
     */
    @Override
    public int getTag(){
        return 1;
    }

    /**
     * When an army is revived the node starts its own one node army again
     */
    public void reviveFraction(){
        if(A == id) // can't revive if the army is still in its own army
            return;
        S = gen.nextInt();
        I = A;
        A = id;
        P = id;
        D = 0;
    }
    
    /**
     * Revive the army of a beaconfab, this could be a neighbour in
     * direct mode
     * @param f the beaconFAB whose army needs to be revived
     */
    public void reviveFraction(BeaconFAB f){
        f.S = f.gen.nextInt();
        if(f.S == 0)
            f.S++;
        f.I = f.A;
        f.A = f.id;
        f.P = f.id;
        f.D = 0;
    }
    
    /**
     * Celebrates the winner of a skirmish in message mode
     * @param m the message
     * @param winner the winner of the skirmish
     * @return the message bringing defeat to the other node
     * or null
     */
    public int[] celebrateWinner(int[] m, int winner){
        int[] res = null;
        if(winner == 0) // r won
        {
            res = new int[7];
            res[0] = REPLY_DEFEAT; // reply bringing defeat
            res[1] = A;
            res[2] = S;
            res[3] = I;
            res[4] = P;
            res[5] = D;
            res[6] = id;
            return res;
        }
        else
        {
            S = m[2];
            A = m[1];
            I = m[3];
            P = m[6];
            D = m[5] + 1;
            armyMembers.clear();
            armyMemberFound(m[6]);
        }
        return res;
    }
    
    /**
     * Returns the fraction of neighbours that are thought to be of 
     * the same army
     * @return fraction of neighbours in the same army
     */
    public double knownArmyFraction(){
        return (double)armyMembers.size()/(double)N.getDegree();
    }
    
    /**
     * The monitor can be used to observe the behaviour of the node
     * @return this FAB
     */
    @Override
    public NodeFAB monitor(){
        return this;
    };
    
    /**
     * Sends a message containing a skirmish request
     * @return message
     */
    @Override
    public int[] send(){
        int[] res = new int[7];
        res[0] = REQUEST; // request
        res[1] = A;
        res[2] = S;
        res[3] = I;
        res[4] = P;
        res[5] = D;
        res[6] = id;
        return res;
    }

    /**
     * Replies to a message depending on the request. If the incoming
     * message is a reply, no new message is generated
     * @param m the message
     * @return the reply or null if the incoming message was a reply
     */
    @Override
    public int[] receive(int[] m) {
        if(m == null)
        {
            return null;
        }
        actionTime++;
        if(m[0] == REQUEST) // Request
        {
            int[] reply = new int[3];
            reply[0] = -1; // reply not valid yet.
            // Check whether s is immune to r
            if(A == m[3])
            {
                return celebrateWinner(m,1);
            }
            // Check whether r is immune to s
            if(I == m[1])
            {
                return celebrateWinner(m,0);
            }
            // Check whether s and r are of the same fraction
            if(m[1] == A)
            {
                armyMemberFound(m[6]);
                if(P == m[6]) // the message comes from the peer
                {
                    D = m[5] + 1;
                    reply[0] = REPLY_ARMY_MEMBER;
                    reply[1] = id;
                    return reply;
                }
                if(D > (m[5] + 1) && m[4] != id) // s is closer to the beacon and r is not s' peer
                {
                    P = m[6];
                    D = m[5] + 1;
                    reply[0] = REPLY_ARMY_MEMBER;
                    reply[1] = id;
                    return reply;
                }
                if(m[5] > (D + 1) && P != m[6]) // r is closer to the beacon and s is not its peer
                {
                    reply[0] = REPLY_PATH_UPDATE; // path update
                    reply[1] = id;
                    reply[2] = D + 1;
                    return reply;
                }
                reply[0] = REPLY_ARMY_MEMBER;
                reply[1] = id;
                return reply;
            }
            if(m[2] > S) // s is stronger than r
            {
                return celebrateWinner(m,1);
            }
            else
            {
                return celebrateWinner(m,0);
            }
        }
        else
        {
            if(m[0] == REPLY_DEFEAT) // reply bringing defeat
            {
                S = m[2];
                A = m[1];
                I = m[3];
                P = m[6];
                D = m[5] + 1;
                armyMembers.clear();
                armyMemberFound(P);
            }
            if(m[0] == REPLY_PATH_UPDATE)
            {
                if(m[2] < D)
                {
                    D = m[2];
                    P = m[1];
                }
                armyMemberFound(m[1]);
            }
            if(m[0] == REPLY_ARMY_MEMBER)
            {
                armyMemberFound(m[1]);
            }
        }
        return null;
    }
    
    /**
     * Checks whether all neighbours are thought to be of the same army
     * @return true if all neighbours are of the same army, false otherwise
     */
    public boolean allMembersFound(){
        return (armyMembers.size() == N.getDegree());
    }
    
    /**
     * Adds a newly found army member to the set of known members
     * @param id id of the newly found member
     */
    public void armyMemberFound(int id){
        armyMembers.add(id);
    }
    
    /**
     * Gets a neighbour to gossip with. The selection process depends
     * on the type
     * @param type 0: random
     * @return a neibhour to gossip with
     */
    public Node getPeer(int type){
        Node peer = null;
        peer = N.getNeighbour(gen.nextInt(N.getDegree()));
        return peer;
    }
    
    /**
     * Direct interaction with the neighbours FAB
     * @param peer the FAB to interact with
     */
    public void interact(BeaconFAB peer){
        receive(peer.receive(send()));
    }
    
    /**
     * Interact with a peer. First a peer is selected, then a message
     * is sent and the reply processed.
     */
    @Override
    public void interact(){
        // Select peer
        Node peer = getPeer(peerSelection);
        // perform communication
        receive(peer.getFAB(fab).receive(send()));
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
        N = n;
        id = n.id;
        P = n.id;
        A = n.id;
        S = gen.nextInt();
        if(S ==0)
            S++;
        D = 0;
        I =-1;
        fab = _fab;
    }
    
    /**
     * Method to request debug strings
     * @return debug string
     */
    @Override
    public String debug() {
        return (id + "(" + A + "," + S + ") ");
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
    public boolean done(ArrayList<Node> nw, int fab){
        int a = ((BeaconFAB)nw.get(0).getFAB(fab)).A;
        Set<Integer> armies = new HashSet<Integer>();
        int min = Integer.MAX_VALUE;
        int d0 = 0;
        int d1 = 0;
        int d2 = 0;
        int d3 = 0;
        boolean res = true;
        int sw = 0;
        for(Node N:nw)
        {
            armies.add(((BeaconFAB)N.getFAB(fab)).A);
            sw = ((BeaconFAB)N.getFAB(fab)).D;
            min = min < sw ? min : sw;
            switch(sw)
            {
                case 0:
                    d0++;
                    break;
                case 1:
                    d1++;
                    break;
                case 2:
                    d2++;
                    break;
                case 3:
                    d3++;
                    break;
                default:
                    break;
            }
            if(((BeaconFAB)N.getFAB(fab)).A != a)
            {
                res = false;
            }
        }
        return res;
    }
    @Override
    NodeFAB make() {
        return new BeaconFAB(gen,peerSelection);
    }
    int peerSelection;
    int actionTime = 0;
    Node N;
    BeaconFAB s;
    BeaconFAB r;
    public int P;
    public int A;
    public int S;
    public int D;
    public int I;
    public int id;
    Map<Integer,Color> colorMap = new HashMap<>();
    Set<Integer> armyMembers = new TreeSet<>();
}
