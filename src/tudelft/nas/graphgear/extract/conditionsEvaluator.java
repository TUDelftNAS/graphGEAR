/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.extract;

import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Ruud van de Bovenkamp
 */
public class conditionsEvaluator {

    /**
     * Checks whether a character is an operator or not.
     * @param in character that might be an operator
     * @return true if the character is an operator (& or |), false if it isn't
     */
    private boolean isOperator(char in){
        return "&|".indexOf(Character.toString(in)) != -1;
    }
    
    /**
     * Reads a boolean expression and converts it into reverse polish notation.
     * @param in the expression to be evaluated
     * @return String array containing the operators and operands in reverse polish notation.
     * Note that operands are expressions in their own right.
     */
    public String[] Evaluate(String in){
        Stack<String> S = new Stack<String>();
        int index = 0;
        char c;
        String res = "";
        String t = "";
        int i;
        ArrayList<String> temp = new ArrayList<String>();
        while(index < in.length())
        {
            c = in.charAt(index);
            switch(c)
            {
                // ignore spaces
                case ' ':
                    index++;
                    break;
                // opening brackets are pushed onto the stack
                case '(':
                    S.push("(");
                    index++;
                    break;
                // when a closing bracket is found items are popped from the
                // stack until the accompanying opening brackt is found
                case ')':
                    t = "";
                    while(!t.equals("("))
                    {
                        t = S.pop();
                        if(!t.equals("("))
                        {
                            temp.add(t);
                        }
                    }
                    index++;
                    break;
                // when an operand is found, it is added to the string list
                // note that operands here are expressions in their own right
                case '[':
                    i = in.indexOf("]", index);
                    temp.add(in.substring(index, i+1));
                    index = i+1;
                    break;
                default:
                    // check for operators.
                    if(isOperator(c))
                    {
                        S.push(Character.toString(c));
                        index++;
                    }
                    else
                    {
                        System.out.println("error parsing expression: " + c);
                    }
                    break;
            }
        }
        if(!S.isEmpty())
            temp.add(S.pop());
        return temp.toArray(new String[temp.size()]);
    }
}
