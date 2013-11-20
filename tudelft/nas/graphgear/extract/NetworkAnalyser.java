/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.extract;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.linkset.StreamedLinkSet;
import tudelft.nas.graphgear.utils.Analyser;
import tudelft.nas.graphgear.utils.General;
import tudelft.nas.graphgear.utils.Node;



/**
 *
 * @author rvandebovenkamp
 */

/**
 * 
 * @author rvandebovenkamp
 * The NetworkAnalyser class contains utility methods to analyse networks extracted
 * using the Filter class.
 */
public class NetworkAnalyser {
    
    /**
     * Little helper method to write nx2 matrices to a file
     * @param data data to be written
     * @param file file to be written to
     */
    public void writeIntColumnsToFile(int[][] data, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(int i=0;i<data.length;i++)
            {
                out.write(data[i][0] + " " + data[i][1] + "\n");
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Little helper method to write int arrays to a file combined with node ids
     * @param data data to be written
     * @param nodes the nodes whose ids should be used
     * @param file file to be written to
     */
    public void writeIntsToFile(int[] data, Node[] nodes, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(int i=0;i<data.length;i++)
            {
                out.write(nodes[i].name + " " + data[i] + "\n");
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Little helper method to write double arrays to a file
     * @param data data to be written
     * @param file file to be written to
     */
    public void writeDoublesToFile(double[] data, Node[] nodes, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(int i=0;i<data.length;i++)
            {
                out.write(nodes[i].name + " " + data[i] + "\n");
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Little helper method to write string arrays to a file
     * @param data data to be written
     * @param file file to be written to
     */
    public void writeStringsToFile(String[] data, String file){
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(String d:data)
            {
                out.write(d+ "\n");
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks whether a character encodes a number
     * @param in character
     * @return true if it encodes a number, false otherwise
     */
    public boolean isNumber(int in){
        return (in >= 48 && in <= 57);
    }
    
    /**
     * Finds a number in a string
     * @param in string
     * @return the first number found in the string or -1 if no number can be found
     */
    public int findNumberInString(String in){
        int pos = 0;
        int start = -1;
        int stop = -1;
        while(pos < in.length() )
        {
            if(isNumber(in.codePointAt(pos)))
            {
                if(start == -1)
                {
                    start = pos;
                }
            }
            else
            {
                if(start != -1 && stop == -1)
                {
                    stop = pos;
                }
            }
            pos++;
        }
        if(stop == -1)
        {
            stop = in.length();
        }
        return Integer.parseInt(in.substring(start, stop));
    }
    
    /**
     * Compares the node set of two networks
     * @param netA
     * @param netB 
     */
    public void compareNetworks(String netA, String netB){
        General u = new General();
        Node[] nA = u.readNetworkFromAdjacencyList(netA);
        Node[] nB = u.readNetworkFromAdjacencyList(netB);
        
        Set<Integer> nodesA = new HashSet<>();
        Set<Integer> nodesB = new HashSet<>();
        
        for(Node n : nA)
        {
            nodesA.add(n.name);
        }
        
        for(Node n : nB)
        {
            nodesB.add(n.name);
        }
        
        int notInB = 0;
        for(int a : nodesA)
        {
            if(!nodesB.contains(a))
            {
                notInB++;
            }
        }
        
        int notInA = 0;
        for(int b : nodesB)
        {
            if(!nodesA.contains(b))
            {
                notInA++;
            }
        }
        System.out.println("Nodes in A: " + nodesA.size());
        System.out.println("Nodes in B: " + nodesB.size());
        System.out.println("Nodes in A but not in B: " + notInB);
        System.out.println("Nodes in B but not in A: " + notInA);
    }
    
    /**
     * Copies the contents of one file to another
     * @param source source file
     * @param dest destination file
     */
    public void copyFile(File source, File dest){
        System.out.println("copying " + source.getPath() + " to " + dest.getPath());
        FileChannel s = null;
        FileChannel d = null;
        try
        {
            dest.createNewFile();
            s = new FileInputStream(source).getChannel();
            d = new FileOutputStream(dest).getChannel();
            d.transferFrom(s, 0, s.size());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(s != null)
                {
                    s.close();
                }
                if(d != null)
                {
                    d.close();
                }
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
            }
        }
    }
    
    /**
     * Combines all the text files in the directory. Note that this function assumes
     * all files in the dir a summary files.
     * @param dir 
     */
    public void analyseLargestComponentMembership(String dir){
        File[] files = new File(dir).listFiles();
        String line;
        String[] parts;
        int[] monthsT = new int[files.length];
        int index =0;
        Map<Integer,File> fileMap = new HashMap<Integer,File>();
        for(File file : files)
        {
            if(file.isFile() && file.getName().contains("betweenness"))
            {
                monthsT[index] = findNumberInString(file.getName());
                fileMap.put(monthsT[index], file);
                index++;
            }
        }
        int[] months = new int[index];
        System.arraycopy(monthsT, 0, months, 0, months.length);
        Arrays.sort(months);
        Analyser A = new Analyser();
        try 
        {
            for(int i : months)
            {
                index = 0;
                if(!fileMap.containsKey(i))
                    continue;
                BufferedReader read = new BufferedReader(new FileReader(fileMap.get(i)));
                while((line=read.readLine())!= null)
                {
                    A.addToBin(0, Integer.parseInt(line.split(" ")[0]), 1);
                }
                read.close();
            }
            int[][] B = A.getBins(0);
            for(int[] b : B)
            {
               A.addToBin(1, b[1], 1);
            }
            System.out.println("Here we go!");
            A.printBins(1);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(NetworkAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Reads a summary file and parses the content as a comma separated string
     * @param file summary file
     * @return comma separated string containing the content of the summary file
     */
    private String readAndParseSummaryFileContent(String file){
        String res = "";
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            int c = 0;
            while((line = in.readLine())!= null)
            {
                c++;
                res += line.split(":")[1].trim();
                if(c < 10)
                {
                    res += ",";
                }
            }
            in.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(NetworkAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("content of file " + file + ": ");
        System.out.println(res);
        return res;
    }
    
    /**
     * Combine all the filter output summary files to create a comma separated file for
     * each threshold value
     * @param folder folder containing summary files
     */
    public void combineFilterSummaries(String folder){
        File Folder = new File(folder);
        Map<Integer,File> M =  new HashMap<>();
        General g = new General();
        ArrayList<Integer> thresholds = new ArrayList<>();
        // first create a map of all files and thresholds found in this folder
        for(File f : Folder.listFiles())
        {
            if(f.getName().contains("summary") && !f.getName().equals(Folder.getName()+"summary.txt"))
            {
                int t = g.findNumberInString(f.getName());
                System.out.println("t: " + t +" file: " + f.getAbsolutePath());
                M.put(t, f);
                thresholds.add(t);
            }
        }
        String header = Folder.getName() + "_Threshold,";
        String[] names = new String[]{"N","L","Ng","Lg","dav","dia","cc","rho","bet","corM","corA"};
        for(int i =0;i<names.length;i++)
        {
            header += Folder.getName() + "_" + names[i];
            if(i < names.length-1)
            {
                header += ",";
            }
        }
        try 
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(Folder.getAbsolutePath() + File.separator + Folder.getName()+"summary.txt"));
            out.write(header + "\n");
            // go over all files in order and copy data
            Collections.sort(thresholds);
            for(int i:thresholds)
            {
                out.write(i+","+readAndParseSummaryFileContent(M.get(i).getAbsolutePath())+ "\n");
            }
            out.flush();
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(NetworkAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    /**
     * Copies all the summary files from the separate threshold folders to the base filter folder
     * assumes the following directory structure : base/filter/t1,t2,t3,etc
     * @param base 
     */
    public void copySummaries(String base){
        File F = new File(base);
        for(File filter : F.listFiles())
        {
            if(filter.isDirectory())
            {
                String filterName = filter.getName();
                System.out.println(filterName);
                for(File threshold : filter.listFiles())
                {
                    if(threshold.isDirectory())
                    {
                        String thresholdName = threshold.getName();
                        System.out.println("\t" + thresholdName);
                        for(File f : threshold.listFiles())
                        {
                            if(f.isFile() && f.getName().contains("summary"))
                            {
                                copyFile(f,new File(filter.getAbsolutePath() + File.separator + filterName+thresholdName + "summary.txt"));
                            }
                        }
                    }
                }
                combineFilterSummaries(filter.getAbsolutePath());
            }
        }
    }
    
    /**
     * Performs a complex network analysis based on the linkset in data. 
     * It creates a directory structure containing the results. The firstFile directory
     * is a subdir of the directory where the data is located and has the name resName
     * @param data streamed link set file location
     * @param resName name of the result file folder. A folder structure will be created to store the results
     * @param thresholds thresholds to be computed
     * @param pathMetrics true if path metrics should be computed, false otherwise
     */
    public void Analyse(String data, String resName, int[] thresholds, boolean pathMetrics){
        // find the data file
        File datFile = new File(data);
        // Get the base dir path for the results
        String resBaseDirPath = datFile.getAbsolutePath().substring(0,datFile.getAbsolutePath().lastIndexOf(datFile.separator)) + datFile.separator + resName;
        // Create the base dir
        File resBaseDir = new File(resBaseDirPath);
        if(!resBaseDir.exists())
        {
            resBaseDir.mkdir();
        }
        // Open the link set
        StreamedLinkSet sl = new StreamedLinkSet(data);
        // Perform analysis for each threshold
        for(int i : thresholds)
        {
            // Create the base dir
            File resDir = new File(resBaseDirPath + datFile.separator + "t" + i);
            if(!resDir.exists())
            {
                resDir.mkdir();
            }
            String baseResultPath = resDir+datFile.separator;
            if(includeDataInFileName)
            {
                baseResultPath += datFile.getName().substring(0,datFile.getName().length()-4);
            }
            String adjacencyPath = baseResultPath+"adjacencyList.txt";
            sl.writeAdjList(adjacencyPath, i);
            // create an utils object
            General u = new General();
            int[] NL = u.getNodeAndLinkCount(adjacencyPath);
            System.out.println("Network has " + NL[0] + " nodes and " + NL[1] + " links");
            // Create network
            Node[] network = u.readNetworkFromAdjacencyList(adjacencyPath);
            if(network.length == 0)
            {
                String[] summary = new String[12];
                summary[0] = "Summary information for " + datFile.getAbsolutePath() + " with threshold: " + i;
                summary[1] = "Total number of nodes: NaN" ;
                summary[2] = "Total number of links: NaN" ;
                summary[3] = "Number of nodes gc: NaN";
                summary[4] = "Number of links gc: NaN";
                if(pathMetrics)
                {
                    summary[5] = "Average distance: NaN";
                    summary[6] = "Diameter: NaN";
                    summary[7] = "Average CC: NaN";
                    summary[9] = "Max betweenness: NaN";
                }
                else
                {
                    summary[5] = "Average distance: NaN";
                    summary[6] = "Diameter: NaN";
                    summary[7] = "Average CC: NaN";
                    summary[9] = "Max betweenness: NaN";
                }

                summary[8] = "Assortativity: NaN";
                summary[10] = "Max coreness: NaN";
                summary[11] = "Average coreness: NaN";
                writeStringsToFile(summary, baseResultPath +"summary.txt");
                continue;
            }
            int N_total = network.length;
            int L_total = 0;
            for(Node nod : network)
            {
                L_total += nod.links.length;
            }
            L_total = L_total/2;
            // Extract largest component
            Node[] gc = u.extractLargestComponent(network);
            int N_gc = gc.length;
            int L_gc = 0;
            for(Node nod : gc)
            {
                L_gc += nod.links.length;
            }
            L_gc = L_gc/2;
            int[][] pathDist = null;
            double[] betw = null;
            double av_dist = -1;
            int[] ecc = null;
            if(pathMetrics)
            {
                // Calculate path matrics
                Object[] paths;
                if(numThreads == 1)
                {
                    paths = u.pathMetrics(gc);
                }
                else
                {
                    paths = u.pathMetricsMT(gc,numThreads);
                }
                ecc = (int[]) paths[0];
                av_dist = (Double) paths[1];
                betw = (double[]) paths[2];
                pathDist = (int[][])paths[3];
                
            }
            int[] coreness = u.coreness(gc);
            // calculate clustering
            double[] clust = u.clusteringCoefficient(gc);
            // Get components
            String[] comps = u.getLastDecompostion();
            double assort = u.assortativity(gc);
            
            // write results to files
            if(pathMetrics)
            {
                writeIntsToFile(ecc,gc, baseResultPath+"eccentricity.txt");
                writeDoublesToFile(betw,gc, baseResultPath+"betweenness.txt");
                writeIntColumnsToFile(pathDist, baseResultPath+"hopcounts.txt");
            }
            writeDoublesToFile(clust,gc, baseResultPath+"clustering.txt");
            writeStringsToFile(comps, baseResultPath+"components.txt");
            
            u.writeToSimpleTextList(gc, baseResultPath+"simpleList.txt");
            writeIntsToFile(coreness, gc,baseResultPath+"coreness.txt");
           
            String[] summary = new String[12];
            summary[0] = "Summary information for " + datFile.getAbsolutePath() + " with threshold: " + i;
            summary[1] = "Total number of nodes: " + N_total;
            summary[2] = "Total number of links: " + L_total;
            summary[3] = "Number of nodes gc: " + N_gc;
            summary[4] = "Number of links gc: " + L_gc;
            if(pathMetrics)
            {
                summary[5] = "Average distance: " + av_dist;
                summary[6] = "Diameter: " + u.max(ecc);
                summary[7] = "Average CC: " + u.average(clust);
                summary[9] = "Max betweenness: " + u.max(betw);
            }
            else
            {
                summary[5] = "Average distance: NaN";
                summary[6] = "Diameter: NaN";
                summary[7] = "Average CC: NaN";
                summary[9] = "Max betweenness: NaN";
            }
            
            summary[8] = "Assortativity: " + assort;
            summary[10] = "Max coreness: " + u.max(coreness);
            summary[11] = "Average coreness: " + u.average(coreness);
            writeStringsToFile(summary, baseResultPath +"summary.txt");

            Runtime.getRuntime().gc();
        }
        
    }
    int numThreads = 1;
    boolean includeDataInFileName = true;
}
