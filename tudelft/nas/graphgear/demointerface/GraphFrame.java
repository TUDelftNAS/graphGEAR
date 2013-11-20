/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author rvandebovenkamp
 */
public class GraphFrame extends JFrame{
    /**
     * creates a new graph frame
     */
    public GraphFrame()
    {
        this.setSize(600, 400);
        this.setVisible(true);
        content = new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
        this.add(content);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Adds a datapoint to a graph
     * @param graph name of the graph
     * @param p data point
     */
    public void addDataPoint(String graph, double p){
        if(graphs.containsKey(graph))
        {
            graphs.get(graph).addDataPoint(p);
        }
    }
    
    /**
     * Adds datapoints to a multi line graph
     * @param graph name of the graph
     * @param p points
     */
    public void addMultiDataPoint(String graph, double[] p){
        if(graphs.containsKey(graph))
        {
            graphs.get(graph).addMultiTraceDataPoint(p);
        }
    }
    
    /**
     * Adds a graph to the graph side panel
     * @param gp graph to be added
     * @param name name of the graph
     */
    public void addGraph(GraphSidePanel gp, String name){
        if(graphs.containsKey(name))
        {
            System.out.println("Graph " + name + " already present");
            return;
        }
        graphs.put(name,gp);
        content.add(gp);
    }
    JPanel content;
    Map<String, GraphSidePanel> graphs =  new HashMap<String,GraphSidePanel>();
}
