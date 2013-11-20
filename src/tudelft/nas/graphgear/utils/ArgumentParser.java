/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruud
 * The argument parser can be used to parse all arguments for a method in a single string
 */
public class ArgumentParser {
    public ArgumentParser(String arguments){
        M = getArgumentMap(arguments);
    }
    
    /**
     * Prints error message if a argument is missing. Also exits the program
     * @param argument 
     */
    private void argumentMissing(String argument){
        System.err.println("Argument missing: " + argument);
        Thread.dumpStack();
        System.exit(22);
    }
    
    /**
     * @return the argument string
     */
    public String getArgumentString(){
        String res = "";
        for(String key : M.keySet())
        {
            res += key + " " + M.get(key) + " ";
        }
        return res;
    }
    
    /**
     * Gets the argument based on the class description
     * @param arg argument name
     * @param classDescription class description
     * @return argument
     */
    public Object getArgument(String arg, String classDescription){
        switch(classDescription)
        {
            case "boolean":
                return getBooleanArgument(arg,true);
            case "double":
                return getDoubleArgument(arg,true);
            case "long":
                return getLongArgument(arg,true);
            case "int":
                return getIntArgument(arg,true);
            case "java.lang.String":
                    return getStringArgument(arg,true);
            default:
                System.err.println("Warning. Unknown classDescription: " + classDescription + " returning null");
                return null;
        }
    }
    /**
     * Checks if an argument is present
     * @param arg the name of the argument
     * @return true if present, false otherwise
     */
    public boolean argumentPresent(String arg){
        return M.containsKey(arg);
    }
    
    /**
     * Overwrites the value of an argument
     * @param key name of the argument
     * @param value new value of the argument
     * @return true if the argument was found, false otherwise
     */
    public boolean overwriteArgument(String key, String value){
        if(M.containsKey(key))
        {
            M.put(key, value);
            return true;
        }
        return false;
    }
    
    /**
     * Returns a string argument
     * @param name name of the argument
     * @param required required or not
     * @return the argument
     */
    public String getStringArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            return M.get(name);
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Returns a boolean argument
     * @param name name of the argument
     * @param required required or not
     * @return the argument
     */
    public boolean getBooleanArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            String val = M.get(name);
            return val.trim().equalsIgnoreCase("true");
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return false;
    }
    
    /**
     * Returns a integer argument
     * @param name name of the argument
     * @param required required or not
     * @return the argument
     */
    public int getIntArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            return Integer.parseInt(M.get(name));
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return -1;
    }
    
    /**
     * Prints ill formed array message
     * @param a ill formed string
     * @param message message 
     */
    private void illFormedArray(String a, String message){
        System.err.println("Ill formed array: " + a + " " + message);
        System.exit(2);
    }
    
    /**
     * Doubles the size of a string array
     * @param in array to be resized
     * @return the resized array
     */
    private String[] growArray(String[] in){
        String[] temp = new String[in.length*2];
        System.arraycopy(in, 0, temp, 0, in.length);
        return temp;
    }
    
    /**
     * Reads the contents of a file in a string array
     * @param name name of the file
     * @return string array containing the contents of the file
     */
    private String[] readFileContents(String name){
        File f = new File(name);
        if(f.exists())
        {
            try 
            {
                BufferedReader in = new BufferedReader(new FileReader(f));
                String line;
                String[] res = new String[2];
                int i =0;
                while((line = in.readLine()) != null)
                {
                    if(i>=res.length){res = growArray(res);}
                    res[i++] = line;
                }
                in.close();
                return Arrays.copyOf(res, i);
            } 
            catch (Exception ex) 
            {
                Logger.getLogger(ArgumentParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            System.err.println("File does not exist: " + name);
            System.exit(2);
        }
        return null;
    }
    
    /**
     * Returns a double array argument
     * @param name name of the argument
     * @param required required or not
     * @return the argument
     */
    public double[] getDoubleArrayFileArgument(String name, boolean required){
        return getDoubleArrayFileArgument(name,0,Integer.MAX_VALUE,required);
    }
    
    /**
     * Returns a double file array argument
     * @param name name of the argument
     * @param required required or not
     * @return the argument
     */
    public double[] getDoubleArrayFileArgument(String name,int start, int stop, boolean required){
        if(M.containsKey(name))
        {
            String[] content = readFileContents(M.get(name));
            if(stop > content.length-1)
            {
                if(stop != Integer.MAX_VALUE)
                {
                    System.err.println("Warning requested array position does not exist");
                }
                stop = content.length-1;
            }
            double[] res = new double[(stop-start)+1];
            for(int i=start;i<=stop;i++)
            {
                res[i-start] = Double.parseDouble(content[i]);
            }
            return res;
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Returns a double from a file
     * @param name name of the argument
     * @param required required or not
     * @param pos position in the file
     * @return the argument
     */
    public double getDoubleFromFile(String name, int pos, boolean required){
        double[] t =getDoubleArrayFileArgument(name,pos,pos,required);
        if(t.length > 0)
        {
            return t[0];
        }
        return Double.NaN;
    }
    
    /**
     * Returns an int argument from a file
     * @param name file name
     * @param pos position of the argument in the file
     * @param required required or not
     * @return the int
     */
    public int getIntFromFile(String name, int pos, boolean required){
        int[] t = getIntArrayFileArgument(name,pos,pos,required);
        if(t.length > 0)
        {
            return t[0];
        }
        return Integer.MIN_VALUE;
    }
    
    /**
     * Gets an int array from a file. This means the integers are stored in a file,
     * one per line
     * @param name name of the file
     * @param required required or not
     * @return int array.
     */
    public int[] getIntArrayFileArgument(String name, boolean required){
        return getIntArrayFileArgument(name,0,Integer.MAX_VALUE,required);
    }
    
    /**
     * Gets an int array containing a part of the numbers found in a file
     * @param name name of the file
     * @param start start line
     * @param stop end line
     * @param required required or not
     * @return the int array
     */
    public int[] getIntArrayFileArgument(String name,int start, int stop, boolean required){
        if(M.containsKey(name))
        {
            String[] content = readFileContents(M.get(name));
            if(stop > content.length-1)
            {
                if(stop != Integer.MAX_VALUE)
                {
                    System.err.println("Warning requested array position does not exist");
                }
                stop = content.length-1;
            }
            int[] res = new int[(stop-start)+1];
            for(int i=start;i<=stop;i++)
            {
                res[i-start] = Integer.parseInt(content[i]);
            }
            return res;
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Returns an double array argument. The double array contains consecutive integers from start to stop
     * with step in between
     * @param name name of the argument
     * @param required required or not?
     * @return double array
     */
    public double[] getDoubleArrayArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            String a = M.get(name).trim();
            if("dD".indexOf(a.charAt(0)) == -1){illFormedArray(a," expected double array: d[a:b:c]");}
            if(a.charAt(1) != '[' || a.charAt(a.length()-1) != ']'){illFormedArray(a,"");}
            String[] parts = a.split(":");
            if(parts.length != 3){illFormedArray(a,"");}
            double start = Double.parseDouble(parts[0].trim().substring(2));
            double step = Double.parseDouble(parts[1].trim());
            double stop = Double.parseDouble(parts[2].trim().substring(0,parts[2].trim().length()-1));
            int l = (int)(Math.ceil((stop-start)/step))+1;
            System.out.println(l);
            double[] res = new double[l];
            int i =0;
            while(i < res.length && Double.compare(start, stop) <= 0)
            {
                res[i++] = start;
                start += step;
            }
            if(i < res.length)
            {
                return Arrays.copyOf(res, i);
            }
            return res;
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Gets the explicitly given int array
     * @param name name of the array
     * @param required required or not
     * @return the int array
     */
    public int[] getExplicitIntArrayArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            String a = M.get(name).trim();
            if("iI".indexOf(a.charAt(0)) == -1){illFormedArray(a," expected integer array");}
            if(a.charAt(1) != '[' || a.charAt(a.length()-1) != ']'){illFormedArray(a,"");}
            a = a.substring(2, a.length()-1);
            String[] parts = a.split("-");
            int[] res = new int[parts.length];
            for(int i=0;i<res.length;i++)
            {
                res[i] = Integer.parseInt(parts[i]);
            }
            return res;
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Returns an int array argument. The int array contains consecutive integers from start to stop
     * with step in between
     * @param name name of the argument
     * @param required required or not?
     * @return int array
     */
    public int[] getIntArrayArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            String a = M.get(name).trim();
            if("iI".indexOf(a.charAt(0)) == -1){illFormedArray(a," expected integer array");}
            if(a.charAt(1) != '[' || a.charAt(a.length()-1) != ']'){illFormedArray(a,"");}
            String[] parts = a.split(":");
            if(parts.length != 3){illFormedArray(a,"");}
            int start = Integer.parseInt(parts[0].trim().substring(2));
            int step = Integer.parseInt(parts[1].trim());
            int stop = Integer.parseInt(parts[2].trim().substring(0,parts[2].trim().length()-1));
            int l = ((stop-start)/step) +1;

            int[] res = new int[l];
            int i =0;
            while(i< res.length && start <= stop)
            {
                res[i++] = start;
                start += step;
            }
            return res;
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return null;
    }
    
    /**
     * Gets a long argument
     * @param name argument name
     * @param required required or not
     * @return a long
     */
    public long getLongArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            return Long.parseLong(M.get(name));
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return -1l;
    }
    
    /**
     * Gets a double argument
     * @param name argument name
     * @param required required or not
     * @return a double
     */
    public double getDoubleArgument(String name, boolean required){
        if(M.containsKey(name))
        {
            return Double.parseDouble(M.get(name));
        }
        else
        {
            if(required)
            {
                argumentMissing(name);
            }
        }
        return -1;
    }
    
    /**
     * Returns the argument map. That is, all the names and all the strings
     * @param arguments the input string. Arguments and values are space separated
     * @return argument map
     */
    private static Map<String,String> getArgumentMap(String arguments){
        Map<String,String> M = new HashMap<>();
        String[] parts = arguments.split(" ");
        String[] argument;
        int a = -1;
        int i = 0;
        while(i<parts.length)
        {
            if(!parts[i].equals(" "))
            {
                if(a == -1)
                {
                    a = i;
                }
                else
                {
                    M.put(parts[a].trim(), parts[i].trim());
                    a = -1;
                }
            }
            i++;
        }
        return M;
    }
    Map<String,String> M;
}
