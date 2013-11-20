package tudelft.nas.graphgear.gossip;

public class Link {
    /**
     * Creates a new link to a node. Links form a linked list
     * each link has a pointer to the next link (or a null pointer if
     * it is the last link)
     * @param _node the node this links to
     */
    public Link(Node _node){
        node = _node;
    }
    
    /**
     * Gets the next link
     * @return pointer to the next link. Note: can be null
     */
    public Link getNext(){
        return next;
    }

    /**
     * Set the pointer to the next link
     * @param in next link
     */
    public void setNext(Link in){
        next = in; 
    }

    /**
     * Checks wether there is a pointer to a next link
     * @return true if there is a next link, false otherwise
     */
    public boolean hasNext(){
        return (next != null);
    }

    /**
     * Gets the node this link links to
     * @return the node this link links to
     */
    public Node getNode(){
        return node;
    }
    public Node node;
    public Link next;
}
