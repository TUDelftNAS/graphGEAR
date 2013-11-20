/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author Ruud van de Bovenkamp
 The Analyser class contains some statistics tools built around the appache statistics tools
 */
public class Analyser {
    public void setDoublePrecision(double _p){
        p = _p;   
    }
    
    /**
     * Returns the empirical CDF of the data in the data array
     * @param data data array
     * @return empirical CDF
     */
    public double[][] getEmpiricalCDF(int[] data){
        HashSet<Integer> S = new HashSet<>();
        for(int i: data)
        {
            S.add(i);
        }
        Arrays.sort(data);
        double[][] res = new double[S.size()][2];
        int old = data[0];
        int c = 1;
        int index = 0;
        for(int i = 1;i<data.length;i++)
        {
            if(data[i] == old)
            {
                c++;
            }
            else
            {
                res[index][0] = old;
                res[index][1] = (double)c/(double)data.length;
                index++;
                old = data[i];
                c++;
            }
        }
        res[index][0] = old;
        res[index][1] = (double)c/(double)data.length;
        index++;
        return res;
             
    }
    
    /**
     * Adds a value to a histogram bin
     * @param index index of the histogram
     * @param bin bin of the histogram
     * @param value value to add
     */
    public void addToBin(int index,double bin, int value){
        long b = (long)(bin * p);
        Map<Long,Integer> m = null;
        if(distD.containsKey(index))
        {
            m = distD.get(index);
        }
        else
        {
            m = new HashMap<>();
            distD.put(index, m);
        }
        if(m.containsKey(b))
        {
            value += m.get(b);
        }
        m.put(b, value);
    }
    
    /**
     * Adds a value to a histogram bin
     * @param index index of the histogram
     * @param bin bin of the histogram
     * @param value value to add
     */
    public void addToBin(int index,int bin, int value){
        Map<Integer,Integer> m = null;
        if(dist.containsKey(index))
        {
            m = dist.get(index);
        }
        else
        {
            m = new HashMap<>();
            dist.put(index, m);
        }
        if(m.containsKey(bin))
        {
            value += m.get(bin);
        }
        m.put(bin, value);
    }
    
    /**
     * Returns the number of (integer) histograms 
     * @return the number of (integer) histograms
     */
    public int getNumDist(){
        return dist.size();
    }
    
    /**
     * Returns the number of (double) histograms 
     * @return the number of (double) histograms
     */
    public int getNumDistD(){
        return dist.size();
    }
    
    /**
     * Returns a string representation of the (double) histogram
     * @param index index of the histogram
     * @param numBins number of bins to be included
     * @return a string representation of the (double) histogram
     */
    public String getBinsD(int index, int numBins){
        String res ="";
        Map<Long,Integer> m = distD.get(index);
        List<Long> k = General.asSortedList(m.keySet());
        double largest = k.get(k.size()-1)/p;
        double smallest = k.get(0)/p;
        double binWidth = (largest-smallest)/(double)numBins;
        int[] bins = new int[numBins];
        long i;
        bins[0] += m.get(0l);
        System.out.println(largest + " " + smallest + " " + binWidth);
        for(int in = 1;in<k.size();in++)
        {
            i = k.get(in);
            System.out.println((i/p) + " " + (Math.ceil((((double)i)/p - smallest)/binWidth)-1) );
            bins[(int)Math.ceil((((double)i)/p - smallest)/binWidth)-1] += m.get(i);
        }
        for(int in=1;in<=bins.length;in++)
        {
            System.out.println((smallest + ((double)in)*binWidth/2) + " " + bins[in-1]);
        }
        System.out.println(largest + " " + smallest + " " + binWidth);
        return res;
    }
    
    /**
     * Gets the number of bins in the histogram
     * @param index the histogram
     * @return the number of bins
     */
    public int getBinEntries(int index){
        if(dist.containsKey(index))
        {
            return dist.get(index).size();
        }
        else
        {
            System.out.println("Index not found.");
        }
        return -1;
    }
    
    /**
     * Gets the number of bins in the (double) histogram
     * @param index the histogram
     * @return the number of bins
     */
    public String getBinsD(int index){
        String res = "";
        if(distD.containsKey(index))
        {
            Map<Long,Integer> m = distD.get(index);
            List<Long> s = General.asSortedList(m.keySet());
            for(long i : s)
            {
                res += (i/p + " " + m.get(i) + "\n");
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
        return res;
    }
    
    /**
     * Prints the bins in the histogram to a file
     * @param index index of the histogram
     * @param file location of the file
     */
    public void printBinsToFile(int index, String file){
        if(distD.containsKey(index))
        {
            try
            {
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                Map<Long,Integer> m = distD.get(index);
                List<Long> s = General.asSortedList(m.keySet());
                for(long i : s)
                {
                    out.write((i/p) + " " + m.get(i) + "\n");
                }
                out.flush();
                out.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
    }
    
    /**
     * Prints the (double) histogram to the console
     * @param index index of the histogram
     */
    public void printBinsD(int index){
        if(distD.containsKey(index))
        {
            Map<Long,Integer> m = distD.get(index);
            List<Long> s = General.asSortedList(m.keySet());
            for(long i : s)
            {
                System.out.println(i/p + " " + m.get(i));
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
    }
    
    /**
     * Gets the bin counts of the histogram
     * @param index index of the histogram
     * @return bin counts of the histogram
     */
    public int[] getBinCounts(int index){
        int[] res =null;
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            List<Integer> v = General.asSortedList(m.values());
            res = new int[v.size()];
            int ind =0;
            for(int i : v)
            {

                res[ind] = i;
                ind++;
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
        return res;
    }
    
    /**
     * Prints the all the (double) histograms in the analyser object to the console
     */
    public void printAllBinsD(){
        List<Integer> keys = General.asSortedList(distD.keySet());
        for(int b : keys)
        {
            System.out.println("Bin: " + b);
            printBinsD(b);
        }
    }
    
    /**
     * Prints the all the histograms in the analyser object to the console
     */
    public void printAllBins(){
        for(int b : dist.keySet())
        {
            System.out.println("Bin: " + b);
            printBins(b);
        }
    }
    
    /**
     * Gives the mean, std and 5th 50th and 95th percentile
     * @param index index of the distribution
     */
    public void getStatistics(int index){
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            DescriptiveStatistics DS = new DescriptiveStatistics();
            for(int i : m.keySet())
            {
                for(int j=0;j<m.get(i);j++)
                {
                    DS.addValue(i);
                }
            }
            System.out.println(5 + " percentile: " + DS.getPercentile(5));
            System.out.println(50 + " percentile: " + DS.getPercentile(50));
            System.out.println(95 + " percentile: " + DS.getPercentile(95));
            System.out.println("mean: " + DS.getMean() + " std: " + DS.getStandardDeviation());
        }
        else
        {
            System.out.println("Index not found.");
        }
    }
    
    /**
     * Prints the percentile to the console
     * @param index index of the dataset
     * @param p percentile
     */
    public void getPercentile(int index, double p){
        System.out.print("Percentile " + p + ": " + Percentile(index,p));
    }
    
    /**
     * 
     * @param index index of the dataset
     * @param p percentile between 0 and 100
     * @return 
     */
    public double Percentile(int index, double p){
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            List<Integer> s = General.asSortedList(m.keySet());
            DescriptiveStatistics DS = new DescriptiveStatistics();
            for(int i : s)
            {
                for(int j=0;j<m.get(i);j++)
                {
                    DS.addValue(i);
                }
            }
            return DS.getPercentile(p);
        }
        else
        {
            System.out.println("Index not found.");
        }
        return -1;
    }
    
    /**
     * Clears a histogram
     * @param index histogram to be cleared 
     */
    public void clearBin(int index){
        if(dist.containsKey(index))
        {
            dist.get(index).clear();
        }
    
    }
    
    /**
     * Returns a histogram made of the data in the bins
     * @param index the desired bin
     * @return 
     */
    public int[][] getHistogramFromBin(int index){
        int[][] res = null;
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            if(m.isEmpty())
            {
                return new int[0][2];
            }
            List<Integer> s = General.asSortedList(m.keySet());
            int last = s.get(s.size()-1);
            int first = s.get(0);
            res = new int[(last-first) +1][2];
            int i = 0;
            for(int c = first; c <= last; c++)
            {
                res[i][0] = c;
                res[i][1] = m.containsKey(c) ? m.get(c) : 0;
                i++;
            }
        }
        else
        {
            System.out.println("index not found!");
        }
        return res;
    }
    
    /**
     * Returns a matrix representation of the histogram
     * @param index index of the histogram
     * @return matrix where the first column is the bin index and the second the bin count
     */
    public int[][] getBins(int index){
        int[][] res = null;
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            List<Integer> s = General.asSortedList(m.keySet());
            res = new int[s.size()][2];
            int c = 0;
            for(int i : s)
            {
                res[c][0] = i;
                res[c][1] = m.get(i);
                c++;
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
        return res;
    }
    
    /**
     * Prints the histogram to the console
     * @param index index of the histogram
     */
    public void printBins(int index){
        if(dist.containsKey(index))
        {
            Map<Integer,Integer> m = dist.get(index);
            List<Integer> s = General.asSortedList(m.keySet());
            for(int i : s)
            {
                System.out.println(i + " " + m.get(i));
            }
        }
        else
        {
            System.out.println("Index not found.");
        }
    }

    /**
     * Adds a value to a distribution
     * @param index index of the distribution
     * @param v value
     */
    public void addValue(int index, double v){
        if(M.containsKey(index))
        {
            M.get(index).addValue(v);
        }
        else
        {
            M.put(index, new SummaryStatistics());
            M.get(index).addValue(v);
            n = index;
        }
    }
    
    /**
     * Adds a value to a counter
     * @param index counter
     * @param value value
     */
    public void addToCounter(int index, int value){
        if(counters.containsKey(index))
        {
            value += counters.get(index);
        }
        counters.put(index, value);
    }
    
    /**
     * Gets the value of a counter
     * @param index counter index
     * @return the value of the counter
     */
    public int getCounter(int index){
        return counters.get(index);
    }
    
    /**
     * Gets the keys of all histograms
     * @return key set of histograms
     */
    public Set<Integer> getDistKeys(){
        return dist.keySet();
    }
    
    /**
     * Gets the keys of all datasets
     * @return key set of datasets
     */
    public Set<Integer> getKeys(){
        return M.keySet();
    }
    
    /**
     * Checks for existence of a dataset
     * @param index index of the dataset
     * @return true if present, false otherwise
     */
    public boolean containsKey(int index){
        return M.containsKey(index);
    }
    
    /**
     * Returns the mean value of a dataset
     * @param index index of the dataset
     * @return mean value
     */
    public double getMean(int index){
        if(!M.containsKey(index))
        {
            System.out.println("index " + index + " does not exist");
        }
        return M.get(index).getMean();
    }
    
    /**
     * Returns the maximum value in a dataset
     * @param index index of the dataset
     * @return maximum value
     */
    public double getMax(int index){
        if(!M.containsKey(index))
        {
            System.out.println("index " + index + " does not exist");
        }
        return M.get(index).getMax();
    }
    
    /**
     * Returns the minimum value in a dataset
     * @param index index of the dataset
     * @return  the minimum value
     */
    public double getMin(int index){
        return M.get(index).getMin();

    }
    
    /**
     * Gets the standard deviation of a dataset
     * @param index index of the dataset
     * @return standard deviation
     */
    public double getStd(int index){
        return M.get(index).getStandardDeviation();
    }
    
    
    public int getLength(){
        return n;
    }
    
    double p = 10000d;
    private int n;
    private final Map<Integer,SummaryStatistics> M = new HashMap<>();
    private final Map<Integer,Map<Integer,Integer>> dist = new HashMap<>();
    private final Map<Integer,Map<Long,Integer>> distD = new HashMap<>();
    private final Map<Integer,Integer> counters = new HashMap<>();
}
