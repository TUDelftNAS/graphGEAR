/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Ruud van de Bovenkamp
 */
public class GossipicoFAB extends NodeFAB{
    /**
     * The gossipicoFAB contains two coupled gossip layers. The count layer
     * and the beacon layer. 
     * @param gen a random generator
     */
    public GossipicoFAB(Random gen){
        super(gen);
        count = new CountFAB(gen);
        beacon = new BeaconFAB(gen,0);
        conv = new CountFAB(gen);
    }
    
    /**
     * A tag is just an identifier
     * @return return tag
     */
    @Override
    public int getTag() {
        return -1;
    }
 
    /**
     * A recount is triggered. The estimate is saved and the 
     * node is set as a border node
     */
    public void recountTriggered(){
        prevEstimate = estimate;
        ratio = 0d;
        cycleCounter =-50;
        quietTime = 0;
        previousCount = 1;
        LPF = 0d;
        deriv = 10d;
        zeroCounter = 0;
        borderNode = true;
    }

    /**
     * Interact in direct mode
     */
    public void interact(){
        // First run the beacon layer
        if(N.getDegree() > 0)
        {
            pf = (GossipicoFAB)beacon.getPeer(beaconPeerSelection).getFAB(fab);
            lastBeaconPeer = pf.beacon.id;
            int armyP = pf.beacon.A;
            int army = beacon.A;
            beacon.interact(pf.beacon);
            if(layersCoupled)
            {
                if(army != beacon.A)
                {
                    count.resetCount();
                    conv.resetCount(0);
                    recountTriggered();
                }
                if(armyP != pf.beacon.A)
                {
                    pf.count.resetCount();
                    pf.conv.resetCount(0);
                    pf.recountTriggered();
                }
            }
            // Then run the count layer
            // Check whether message is IC
            pf = null;
            if(count.MT == 1)
            {
                if(beacon.P != N.id)
                {
                    pf = (GossipicoFAB)N.getNeighbour(N.linkIndexForNode(beacon.P)).getFAB(fab);
                }
            }
            else
            {
                pf = (GossipicoFAB)N.getNeighbour(gen.nextInt(N.getDegree())).getFAB(fab);
            }
            if(pf != null)
            {
                if(layersCoupled)
                {
                    // Check whether peer is of the same army
                    if(pf.beacon.A == beacon.A)
                    {
                        count.interact(pf.count);
                    }
                }
                else
                {
                    count.interact(pf.count);
                }
            }
            // Then run the conv layer
            // Check whether message is IC
            pf = null;
            if(conv.MT == 1)
            {
                if(beacon.P != N.id)
                {
                    pf = (GossipicoFAB)N.getNeighbour(N.linkIndexForNode(beacon.P)).getFAB(fab);
                }
            }
            else
            {
                pf = (GossipicoFAB)N.getNeighbour(gen.nextInt(N.getDegree())).getFAB(fab);
            }
            if(pf != null)
            {
                if(layersCoupled)
                {
                    // Check whether peer is of the same army
                    if(pf.beacon.A == beacon.A)
                    {
                        conv.interact(pf.conv);
                    }
                }
                else
                {
                    conv.interact(pf.conv);
                }
            }
        }
        // update the estimate
        if(count.SC >= prevEstimate)
        {
            ratio = 1d;
        }
        estimate = (int) (((1d - ratio) * prevEstimate) + (ratio * count.SC));
        ratio = 1d/(1 + Math.exp(-3*(quietTime-2*beacon.D-5)));
        // Check whether count has updated
        if(count.countUpdate)
        {
            count.countUpdate = false;
        }
        else
        {
            quietTime++;
        }
        if(beacon.allMembersFound() && borderNode)
        {
            borderNode = false;
            if(conv.MT == 1)
            {
                conv.MC++;
                conv.MF++;
            }
            else
            {
                conv.MC = 1;
                conv.MF = 1;
                conv.MT = 1;
            }
        }
        if(conv.SC == count.SC && count.SC >= N.getDegree() && beacon.D == 0 )
        {
            convDetected = true;
        }
    }

    /**
     * Create message in message passing mode. Not implemented.
     * @return message
     */
    @Override
    public int[] send() {
        return null;
    }

    /**
     * Receive message in message passing mode. Not implemented.
     * @param m the received message
     * @return reply
     */
    @Override
    public int[] receive(int[] m) {
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
        count.init(n, flag,-1);
        conv.init(n,-1,-1);
        beacon.init(n, flag,-1);
        fab = _fab;
        N = n;
    }

    /**
     * Method to request debug strings
     * @return debug string
     */
    @Override
    public String debug() {
        return Integer.toString(count.MC);
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
        GossipicoFAB f;
        int army = -1;
        for(Node No:nw)
        {
            f = (GossipicoFAB)No.getFAB(fab);
            if(f.convDetected)
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    NodeFAB monitor() {
        return this;
    }
    boolean borderNode = true;
    Node N;
    double incr;
    double LPF = 0d;
    double deriv = 10d;
    double factor = 0.8;
    int zeroCounter = 0;
    int prev;
    int estimate = 1;
    int prevEstimate;
    int cycleCounter;
    int lastUpdate = 0;
    int quietTime = 0;
    double ratio = 1d;
    CountFAB count;
    CountFAB conv;
    BeaconFAB beacon;
    GossipicoFAB nf;
    GossipicoFAB pf;
    int previousCount = 1;
    int convCounter = 0;
    boolean flaggedConvergence = false;
    int convDetectedCounter = 0;
    boolean convDetected = false;
    boolean layersCoupled = true;
    int beaconPeerSelection = 0;
    int lastBeaconPeer = 0;

    @Override
    NodeFAB make() {
        return new GossipicoFAB(gen);
    }

}
