/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

/**
 *
 * @author rvandebovenkamp
 * double-dobule pair
 */
public class DoublePair implements Comparable<DoublePair>{
    public DoublePair(double index, double value){
        i = index;
        v = value;
    }
    
    public double i;
    public double v;

    @Override
    public int compareTo(DoublePair o) {
        return Double.compare(i, o.i);
    }
}
