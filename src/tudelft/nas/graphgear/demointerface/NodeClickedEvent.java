/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.util.EventObject;

/**
 *
 * @author rvandebovenkamp
 */
public class NodeClickedEvent extends EventObject{
    /**
     * Creates a new NodeClickedEvent
     * @param source source of the event
     * @param _node node id that was clicked
     * @param _modifier indicates modifier (such as swift down)
     */
    public NodeClickedEvent(Object source, int _node, int _modifier){
        super(source);
        node = _node;
        modifier = _modifier;
    }
    
    
    /**
     * Gets the modifier when the node was clicked
     * @return modifier 0: no modifier<br>1: shift down
     */
    public int getModifier(){
        return modifier;
    }
    /**
     * Gets the id of the node that was clicked
     * @return id of the node that was clicked
     */
    public int getNode(){
        return node;
    }
    int node = -1;
    int modifier = 0;
}
