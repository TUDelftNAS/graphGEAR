package tudelft.nas.graphgear.gossip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
/**
 * The node class is used as the node datastructure in all code in the
 * tudelft.nas.graphstream.gossip package. The node class contains methods
 * for executing gossip algorithms implemented by extending nodeFAB
 * @author Ruud
 */
public class Node {
    /**
     * Creates a new node with the specified node id
     * @param _id 
     */
    public Node (int _id){
        id = _id;
    }
    
    /**
     * Returns the nodeFAB at the specified id
     * @param id nodeFAB id
     * @return nodeFAB at id
     */
    public NodeFAB getFAB(int id){
        return fabs.get(id);
    }
    
    /**
     * Returns the number of nodeFABs registered
     * @return number of nodeFABs
     */
    public int getNumFab(){
        return fabs.size();
    }
    
    /**
     * Removes all nodeFABs
     */
    public void clearFABS(){
        fabs.clear();
    }
    
    /**
     * inits the FAB at location id
     * @param id FAB location
     * @param flag flag possibly used in initialisation
     */
    public void initFAB(int id, int flag){
        fabs.get(id).init(this,flag,id);
    }
    
    /**
     * Method used to access the debug method of the nodeFAB at fab
     * @param fab index of the nodeFAB
     * @return the debug string of the nodeFAB at fab
     */
    public String debug(int fab){
        return fabs.get(fab).debug();
    }
    
    /**
     * Adds a nodeFAB
     * @param f nodeFAB to be added
     */
    public void addFAB(NodeFAB f){
        fabs.add(f);
    }
    
    /**
     * Method to acces the monitor of the nodeFAB at fab
     * @param fab index of the nodeFAB
     * @return monitor
     */
    public NodeFAB monitor(int fab){
        return fabs.get(fab).monitor();
    }
    
    /**
     * 
     * @return this node's degree 
     */
    public int getDegree(){
        return degree;
    }
    
    /**
     * prints the id of this node
     */
    public void printId(){
        System.out.print(" " + id + " ");
    }
    
    /**
     * Returns all neighbours of this node
     * @return all neighbours of this node or null of the node has no neighbours
     */
    public Node[] getNeighbours(){
        if(degree == 0)
        {
            return null;
        }
        Node[] res = new Node[degree];
        int c =0;
        Link l = link;
        res[c++] = l.node;
        while(l.hasNext())
        {
            l = l.next;
            res[c++] = l.node;
        }
        return res;
    }
    
    /**
     * gets the neighbour at index
     * @param index neighbour index
     * @return neighbour
     */
    public Node getNeighbour(int index){
        if(index < 0 || index > degree)
        {
            System.out.println("oh dear! We have a problem. I'm node " + id + " and I'm asked for a link I don't have");
            System.out.println("My degree is: " + degree + ". They want neighbour " + index);
            System.out.println("Here's my adjacency list. ");
            System.out.println(AdjList());
            return null;
        }
        Link l = link;
        for(int i=0;i<index;i++)
        {
            l = l.next;
        }
        if(l == null)
        {
            System.out.println("oh dear! We have a problem. I'm node " + id + " and I'm asked for a link I don't have");
            System.out.println("My degree is: " + degree + ". They want neighbour " + index);
            System.out.println("Here's my adjacency list. ");
            System.out.println(AdjList());
        }
        return l.getNode();
    }
    
    /**
     * Gets a string representation of the adjacency list of this node
     * @return adjacency list of this node
     */
    public String AdjList(){
        String L = (id + ")");
        Link l = link;
        int counter = 0;
        while (l != null)
        {
            counter++;
            L += " " + l.node.id;
            l = l.next;
        }
        if(counter != degree)
        {
            System.out.println("Whohaa, my degree and list no longer agree!");
        }
        return L;
    }
    public void createLookUp(){
        TM = new HashMap<Integer,Integer>();
        for(int i=0;i<degree;i++)
        {
            TM.put(getNeighbour(i).id, i);
        }
    }
    
    /**
     * Finds the link index to the node with id id
     * @param id node id 
     * @return link index of node id or -1 if node not found
     */
    public int linkIndexForNode(int id){
        // check for existence of lookup 
        if(TM != null)
        {
            return (Integer)TM.get(id);
        }
        
        if(link == null)
        {
            return -1;
        }
        Link L = link;
        if(L.node.id == id)
        {
            return 0;
        }
        int counter = 1;
        while(L.hasNext()){
            L = L.next;
            if(L.node.id == id)
            {
                return counter;
            }
            counter++;
        }
        return -1;
    }
    public void printAdjList(){
        System.out.print(id + ")");
        if(link != null)
        {
            Link L = link;
            L.getNode().printId();
            while(L.hasNext())
            {
                L = L.getNext();
                L.getNode().printId();
            }
        }
        System.out.print("\n");
    }
    
    /**
     * Chances the node id of this node
     * @param _id new node id
     */
    public void resetID(int _id){
        id = _id;
    }
    
    /**
     * Removes a link from this node
     * @param index index of the link to be removed
     */
    public void removeLink(int index){
        Link L = link;
        Link P = null;
        if(index == 0)
        {
            link = link.next;
        }
        else
        {
            for (int i = 0; i < index; i++)
            {
                P = L;
                L = L.next;
            }
            P.next = L.next;
            if(index == degree - 1)
            {
                last = P;
            }
        }
        degree--;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        return new Integer(id).hashCode();
    }
    
    /**
     * Adds a link to this node
     * @param l link to be added
     */
    public void addLink(Link l){
        if(l.getNode().id == id)
        {
            System.out.println("Oops, you want me to make a self-loop!");
        }
        if(link == null)
        {
            link = l;
            last = link;
        }
        else
        {
            last.setNext(l);
            last = l;
        }
        degree++;
    }

private Map TM;
public Link last;
public Link link;
private int degree;
public int id;
private ArrayList<NodeFAB> fabs = new ArrayList<>();
}