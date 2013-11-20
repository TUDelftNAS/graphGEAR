/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;


/**
 *
 * @author rvandebovenkamp
 */
public class Main {

    /**
     * @param args the command line arguments
     * Runs an example experiment where Gossipico is ran in a sparse 300 node
     * network. The process is visualised using the demo interface. Nodes can be clicked
     * to add or remove links. Simply click two nodes in succession and a link will be created
     * if there is no link present or it will be deleted if there is a link present.
     */
    public static void main(String[] args)  {
        gossipicoExperiment e = new gossipicoExperiment();
        e.run();
    }
}
