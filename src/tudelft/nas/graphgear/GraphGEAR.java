/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear;

import java.io.File;
import tudelft.nas.graphgear.extract.Filter;
import tudelft.nas.graphgear.extract.NetworkAnalyser;
import tudelft.nas.graphgear.gossip.gossipicoExperiment;

/**
 *
 * @author rvandebovenkamp
 */
public class GraphGEAR {

    /**
     * @param args the command line arguments
     * Runs two examples. One graph extraction example and one gossip simulation
     * example. Change the example string to choose between examples.
     */
    public static void main(String[] args) {
        String example = "gossip";
        switch(example)
        {
            case "extraction":
                Filter F = new Filter();
                // Extract links between players that played on the same side
                F.SimpleRunSameSideLinks("DotaLi.dat", "DotaLiSameSide.res");
                // Create network analyser
                NetworkAnalyser nwa = new NetworkAnalyser();
                // create result folder
                new File("res").mkdir();
                // analyse networks for three threshold values
                nwa.Analyse("DotaLiSameSide.res", "res/DotaliSS", new int[]{0,2,4,8}, true);
                // copy summary files
                nwa.copySummaries("res");
                // Combine summaries
                nwa.combineFilterSummaries("res/DotaliSS");
                // The file res/DotaliSS/DotaliSSsummary.txt now contains the comma separated network metrics
                // of the network extracted from the sample data
                break;
            case "gossip":
                // create gossipico experiment and run it.
                gossipicoExperiment e = new gossipicoExperiment();
                e.run();
                break;
                    
        }
    }
    
}
