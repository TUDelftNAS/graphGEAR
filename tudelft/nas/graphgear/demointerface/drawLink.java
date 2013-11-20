/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;

/**
 *
 * @author Ruud
 * A draw link is simply a link with coordinates for its endpoints
 */
public class drawLink {
    /**
     * Creates a new draw link
     * @param _xs x-coordinate of start point
     * @param _ys y-coordinate of start point
     * @param _xe x-coordinate of end point
     * @param _ye y-coordinate of end point
     * @param _sid id of start node
     * @param _eid id of end node
     */
    public drawLink(double _xs, double _ys, double _xe, double _ye, int _sid, int _eid)
    {
        xs = _xs;
        ys = _ys;
        xe = _xe;
        xs = _xs;
        sid = _sid;
        eid = _eid;
    }
    
    public String toString(){
        return sid + " -> " + eid;
    }
    public double xs;
    public double ys;
    public double xe;
    public double ye;
    public int sid;
    public int eid;
    public Color color = Color.white;
}
