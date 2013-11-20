/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

/**
 *
 * @author rvandebovenkamp
 * Double-Int pair
 */
public class DoubleIntPair implements Comparable<DoubleIntPair>{
    public DoubleIntPair(double _d, int _i){
        d = _d;
        i = _i;   
    }

    /**
     * Returns the double of this double-int pair
     * @return 
     */
    public double getDouble(){
        return d;
    }
    
    /**
     * Returns the int value of the pair
     * @return 
     */
    public int getInt(){
        return i;
    }
    @Override
    public int compareTo(DoubleIntPair o) {
        return Double.compare(d, o.d);
    }
    public String toString(){
        return d + " " + i;
    }
    double d;
    int i;
}
