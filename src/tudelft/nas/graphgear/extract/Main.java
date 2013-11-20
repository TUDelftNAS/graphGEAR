/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.extract;


import java.io.*;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.linkset.LinkSetNode;
import tudelft.nas.graphgear.linkset.StreamedLinkSet;
import tudelft.nas.graphgear.utils.ArgumentParser;
import tudelft.nas.graphgear.utils.General;


/**
 *
 * @author rvandebovenkamp
 */
public class Main {

    

    public static void createThresholdFiles(String folder){
        File F = new File(folder);
        for(File f : F.listFiles())
        {
            if(f.getAbsolutePath().contains(".res"))
            {
                int[] t = getThresholds(f.getAbsolutePath());
                try 
                {
                    BufferedWriter out = new BufferedWriter(new FileWriter(f.getAbsolutePath()+"-T"));
                    for(int i : t)
                    {
                        out.write(i + "\n");
                    }
                    out.flush();
                    out.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Creates an array with all different threshold values in the linkset
     * @param linkset location of the linkset
     * @return array containing all different threshold values in the link set
     */
    public static int[] getThresholds(String linkset){
        StreamedLinkSet sls = new StreamedLinkSet(linkset);
        Set<Integer> W = new HashSet<Integer>();
        sls.initTreeTraversal();
        LinkSetNode L;
        while((L = sls.getNextInOrder()) != null )
        {
            W.add(L.w);
        }
        General u = new General();
        List<Integer> w  = u.asSortedList(W);
        int[] r = new int[w.size()];
        for(int i =0;i<r.length;i++)
        {
            r[i] = w.get(r.length-i-1);
        }
        return r;
    }
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException, InterruptedException {
        System.out.println("Number of input arguments: " + args.length);
        for(String a : args)
        {
            System.out.println(a);
        }
        if(args.length > 0)
        {
            Filter F = new Filter();
            ArgumentParser ap = new ArgumentParser(args[1]);
            NetworkAnalyser nwa = new NetworkAnalyser();
            switch(ap.getIntArgument("type", true))
            {
                case 0: // same game
                    System.out.println("Creating same match (SM) links");
                    F.SimpleRunSameGameLinks(ap.getStringArgument("input", true), ap.getStringArgument("output", true));
                    break;
                case 1: // same side
                    System.out.println("Creating same side (SS) links");
                    F.SimpleRunSameSideLinks(ap.getStringArgument("input", true), ap.getStringArgument("output", true));
                    break;
                case 2: // opposing side
                    System.out.println("Creating opposing side (OS) links");
                    F.SimpleRunOpposingSideLinks(ap.getStringArgument("input", true), ap.getStringArgument("output", true));
                    break;
                case 3: // same side won
                    System.out.println("Creating same side match won (MW) links");
                    F.SimpleRunSameSideWon(ap.getStringArgument("input", true), ap.getStringArgument("output", true));
                    break;
                case 4: // same side lost
                    System.out.println("Creating same side match lost (ML) links");
                    F.SimpleRunSameSideLost(ap.getStringArgument("input", true), ap.getStringArgument("output", true));
                    break;
                case 5: // analyse res file
                    nwa.Analyse(ap.getStringArgument("input", true), ap.getStringArgument("output", true), new int[]{ap.getIntArgument("T", true)}, true);
                    break;
                case 6: // same game interval
                    F.SimpleRunSameGameInterval(ap.getStringArgument("input", true), ap.getStringArgument("output", true),ap.getIntArgument("interval", true));
                    break;
                case 7: // print matches to console
                    F.printMatches(ap.getStringArgument("input",true));
                    break;
                case 8: // create threshold files
                    createThresholdFiles(ap.getStringArgument("folder", true));
                    break;
            }
        }
    }

}
