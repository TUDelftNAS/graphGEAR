/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

/**
 *
 * @author rvandebovenkamp
 * int-double pair
 */
public class IntDoublePair implements Comparable<IntDoublePair>{
    public IntDoublePair(int _i, double _d)
    {
        i = _i;
        d = _d;
    }
    
    @Override
    public int compareTo(IntDoublePair p){
        return Integer.compare(i, p.i);
    }
    int i;
    double d;
}
