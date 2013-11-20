/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.utils;

/**
 *
 * @author Ruud van de Bovenkamp
 */
public class IntegerPair implements Comparable<IntegerPair> {
    /**
     * Creates a new IntegerPair
     * @param _a int a
     * @param _b int b
     * @param ordered if true the integers are stored in descending order
     */
        public IntegerPair(int _a, int _b, boolean ordered){
            if(ordered && _b < _a)
            {
                a = _b;
                b = _a;
            }
            else
            {
                a = _a;
                b = _b;
            }
        }
        public IntegerPair(int _a, int _b){
            a = _a;
            b = _b;
        }
        public int getA(){
            return a;
        }
        public int getB(){
            return b;
        }
        public boolean equals(Object o){
            if(o instanceof IntegerPair)
            {
                IntegerPair t = (IntegerPair)o;
                if((t.getA() == a) && (t.getB() == b))
                {
                    return true;
                }
            }
            return false;
        }
        public int hashCode(){
            return a.hashCode() + b.hashCode();
        }
        public int compareTo(IntegerPair o) {
                if(a > o.getA())
                {
                    return -10;
                }
                if(a == o.getA())
                {
                    if(b > o.getB())
                    {
                        return -5;
                    }
                }
                return 1;
        }

    private Integer a;
    private Integer b;
    }