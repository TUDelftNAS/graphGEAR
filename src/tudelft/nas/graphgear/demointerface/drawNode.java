/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;

/**
 *
 * @author Ruud
 * A draw node is simply a node with coordinates and size info
 */
public class drawNode {
    /**
     * Creates new draw node
     * @param _x x-coordinate of the node
     * @param _y y-coordinate of the node
     * @param _r size of the node
     * @param _id id of the node
     * @param _color color of the node
     */
    public drawNode(double _x, double _y, double _r, int _id, String _color){
        x = _x;
        y = _y;
        r = _r;
        id = _id;
        
    }
    public Color getColor(){
        return color;
    }
    public double x;
    public double y;
    public double r;
    public int id;
    public Color color = Color.blue;
}
