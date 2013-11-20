/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random; 

/**
 * nodeFAB is an abstract class describing the methods that a
 * gossip algorithm should implement in this framework. FAB stands
 * for fields and behaviour.
 * @author Ruud van de Bovenkamp
 */
public abstract class NodeFAB {
    public NodeFAB(Random _gen){
        gen = _gen;
    }
    abstract public int getTag();
    abstract public void interact();
    abstract public int[] send();
    abstract public int[] receive(int[] m);
    abstract public void init(Node n, int flag, int _fab);
    abstract public String debug();
    abstract public boolean done(ArrayList<Node> nw, int fab);
    abstract NodeFAB monitor();
    abstract NodeFAB make();
    Random gen;
    int fab = -1;
}
