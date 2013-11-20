/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.utils;

/**
 *
 * @author Ruud van de Bovenkamp
 * The node is a simple data structure containing an id, name and an array of nodes that functions
 * as the links between this node and others
 */
public class Node {
    public Node(int _id, int degree){
        id = _id;
        name = _id;
        links = new Node[degree];
    }
    public String toString(){
        String res = id + "(" + name + ") ";
        for(Node N: links)
        {
            if(N != null)
            {
                res += N.id + " ";
            }
            else
            {
                res += "x ";
            }
        }
        return res;
    }
    
    /**
     * removes the last link if it links to node n
     * @param n the node the last link should link to
     */
    public void removeLastLink(int n){
        if(links[links.length-1].id != n)
        {
            System.out.println("error");
        }
        Node[] temp = new Node[links.length-1];
        System.arraycopy(links, 0, temp, 0, links.length-1);
        links = temp;
        temp = null;
        index = links.length;
    }
    
    /**
     * Checks whether this node links to node n
     * @param n the neighbour
     * @return true if n is a neighbour, false if not
     */
    public boolean isNeighbour(int n){
        for(Node l : links)
        {
            if(l!= null && l.id == n)
                return true;
        }
        return false;
    }
    
    /**
     * Add a link to this node
     * @param in the node this node links to
     */
    public void addLink(Node in){
        if(index == links.length)
        {
            Node[] temp = new Node[index+1];
            System.arraycopy(links, 0, temp, 0, links.length);
            links = temp;
            temp = null;
        }
        links[index] = in;
        index++;
    }
    
    public void addRewiredLink(Node in){
        if(free == -1)
        {
            System.err.println("Cannot add rewired link without first deleting one!");
            Thread.dumpStack();
            System.exit(2);
        }
        links[free] = in;
        free = -1;
    }
    
    /**
     * Remove a link from this node
     * @param n  the node to which we no longer want to link
     */
    public void removeLink(int n){
        if(free != -1)
        {
            System.err.println("Cannot remove more than one link!");
            Thread.dumpStack();
            System.exit(2);
        }
        for(int i=0;i<links.length;i++)
        {
            if(links[i].id == n)
            {
                links[i] = null;
                free = i;
                return;
            }
        }
    }
    public int id;
    public int name;
    public int index = 0;
    public Node[] links;
    public int free = -1;
    double evc = 1d;
    double evcT = 1d;
}
