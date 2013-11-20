/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

import java.util.ArrayList;

/**
 *
 * @author rvandebovenkamp
 * Generates very basic layouts
 */
public class LayoutGenerator {
    
    public final static int GRID_LAYOUT = 0;
    public final static int CIRCLE_LAYOUT = 1;
   
    /**
     * Puts all nodes in a circle
     * @param size number of nodes
     */
    private void doCircleLayout(int size){
        double incr = (2d*Math.PI)/(double)(size);
        double angle = 0d;
        x = new double[size];
        y = new double[size];
        for(int i=0;i<size;i++)
        {
            x[i] = (0.5d*Math.sin(angle)) + 0.5d;
            y[i] = (0.5d*Math.cos(angle)) + 0.5d;
            angle += incr;
        }
    }
    
    /**
     * Puts all nodes in a grid (containing from top left to bottom right)
     * @param size number of nodes
     */
    private void doGridLayout(int size){
        int w = (int)Math.ceil(Math.sqrt(size));
        int h = (w*w == size) ? w : w-1;
        double incr = 1d/(double)(w-1);
        // create new coordinate arrays
        x = new double[size];
        y = new double[size];
        // create layout
        int c =0;
        double x_i = 0d;
        double y_i = 0d;
        while(c != size)
        {
            x[c] = x_i;
            y[c] = y_i;
            x_i += incr;
            c++;
            if(c % w == 0)
            {
                System.out.println("bliep: " + c);
                x_i = 0d;
                y_i += incr;
            }
        }
    }
    
    /**
     * Perform the actual layouts
     * @param size number of nodes
     * @param type type of layout
     */
    public void performLayout(int size, int type){
        switch(type)
        {
            case GRID_LAYOUT:
                layoutDone = false;
                doGridLayout(size);
                layoutDone = true;
                break;
            case CIRCLE_LAYOUT:
                layoutDone = false;
                doCircleLayout(size);
                layoutDone = true;
                break;
            default:
                System.err.println("Unkown layout. ");
                break;
        }
    }
    /**
     * 
     * @return the x coordinates of the network
     */
    public double[] getXcor(){
        return x;
    }
    
    /**
     * 
     * @return the y coordinates of the network
     */
    public double[] getYcor(){
        return y;
    }
    
    /**
     * 
     * @return true if layout is ready, false otherwise
     */
    public boolean hasLayout(){
        return layoutDone;
    }
    boolean layoutDone = false;
    double[] x;
    double[] y;
}