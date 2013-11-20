/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.gossip;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import tudelft.nas.graphgear.demointerface.ButtonControl;
import tudelft.nas.graphgear.demointerface.DemoInterface;
import tudelft.nas.graphgear.demointerface.NodeClickedEvent;
import tudelft.nas.graphgear.demointerface.NodeClickedListener;
import tudelft.nas.graphgear.demointerface.switchControl;
import tudelft.nas.graphgear.utils.General;




/**
 *
 * @author Ruud van de Bovenkamp
 */
public class gossipicoExperiment extends experiment{

    /**
     * Removes a node 
     * @param id node to be removed
     */
    @Override
    public void removeNode(int id){
        // remove node from network
        super.removeNode(id);
        // remove node from visualisation
        D.getGraphPanel().removeNode(id);
        D.getGraphPanel().repaint();
    }
    
    /**
     * Adds a node to the graph and to the visulisation
     * @param x x-coordinate of new node
     * @param y y-coordinate of new node
     */
    public void addNode(float x, float y){
        Node N = new Node(idCounter++);
        N.addFAB(fabs.get(0).make());
        N.initFAB(N.getNumFab()-1,0);
        network.add(N);
    }
    
    /**
     * Removes a link from the network and from the visualisation
     * @param s source node
     * @param d destination node
     */
    public void removeLink(int s, int d){
        Node S = null;
        Node P = null;
        for(Node N : network)
        {
            if(N.id == s)
            {
                S = N;
            }
            if(N.id == d)
            {
                P = N;
            }
        }
        if(S != null && P != null)
        {
            System.out.println("Removing link from network and vis");
            S.removeLink(S.linkIndexForNode(P.id));
            P.removeLink(P.linkIndexForNode(S.id));
            D.getGraphPanel().removeLink(s, d);
            D.getGraphPanel().repaint();
            // trigger recount in gossipico
            GossipicoFAB gf;
            gf = (GossipicoFAB)S.getFAB(0);
            gf.count.resetCount();
            gf.beacon.reviveFraction();
            gf.recountTriggered();
            gf = (GossipicoFAB)P.getFAB(0);
            gf.count.resetCount();
            gf.beacon.reviveFraction();
            gf.recountTriggered();
        }
        else
        {
            System.err.println("Cannot find link between " + s + " and " + d);
        }
    }
    
    /**
     * Creates a link between two nodes
     * @param s source id
     * @param d destination id;
     */
    public void addLink(int s, int d){
        Node S = null;
        Node P = null;
        for(Node N : network)
        {
            if(N.id == s)
            {
                S = N; 
            }
            if(N.id == d)
            {
                P = N;
            }
        }
        if(S != null && P != null)
        {
            S.addLink(new Link(P));
            P.addLink(new Link(S));
            D.getGraphPanel().addLink(S.id, P.id);
            D.getGraphPanel().repaint();
        }
        else
        {
            System.err.println("Cannot find both nodes: " + s + " and " + d);
        }
    } 
    
    /**
     * Sets the node colour depending on the node count or army info. The colour ranges from
     * red for 100% wrong to green for correct when based on the count info or
     * with an army dependent colour. The global boolean armyColours specifies
     * the type of colouring
     */
    private void updateNodeColours(){
        if(armyColours)
        {
            for(Node N : network)
            {
                if(N.id != clicked)
                {
                    D.getGraphPanel().setNodeColorNoRepaint(N.id, Color.getHSBColor(colourMap.get(((GossipicoFAB)N.getFAB(0)).beacon.A), 1f, 1f));
                }
                else
                {
                    D.getGraphPanel().setNodeColorNoRepaint(N.id, Color.MAGENTA);
                }
            }
            D.getGraphPanel().repaint();
        }
        else
        {
            float h=0;
            for(Node N : network)
            {
                if(N.id == clicked)
                {
                    h = 5f/6f;
                }
                else
                {
                    h = 0.33f*(float)((GossipicoFAB)N.getFAB(0)).count.SC/(float)network.size();
                }
                D.getGraphPanel().setNodeColorNoRepaint(N.id, Color.getHSBColor(h, 1, 1));
            }
        }
        D.getGraphPanel().repaint();
        updateNodeInfo();
    }
    
    /**
     * Checks whether a link exists between source and destination
     * @param s source node
     * @param d destination node
     * @return true if there is a link between s and d, false otherwise
     */
    private boolean linkExists(int s, int d){
        for(Node N : network)
        {
            if(N.id == s)
            {
                if(N.linkIndexForNode(d) == -1)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }
  
    /**
     * Sets the node info for each node equal to its count value. Hovering
     * over the node will show the count value next to the cursor.
     */
    private void updateNodeInfo(){
        for(Node N : network)
        {
            D.getGraphPanel().addNodeInfo(N.id, "Count value: " + Integer.toString(((GossipicoFAB)N.getFAB(0)).count.SC) );
        }
    }
    
    /**
     * Creates a colour map by assigning as many colours as there are node
     * ids a map. The index in the map maps to an army colour
     */
    private void createColourMap(){
        colourMap.clear();
        float c = 0;
        float step = 1f/(float)network.size();
        for(Node N :network)
        {
            colourMap.put(N.id, c);
            c += step;
        }
    }
    
    /**
     * Runs an experiment where we load a PA graph and run Gossipico.
     * From the demo interface we catch click events and add / delete nodes
     * and links
     */
    public void run(){
        //TODO gossip runs and interface events need to be synchronised
        // Create demo interface
        D = new DemoInterface();
        D.showGui("PA300m1.svg");
        
        // minimise side panel
        D.gui.toggleSidePanelSize();
        
        // add node click listener
        D.getGraphPanel().addNodeClickedListener(new NodeClickedListener(){

            @Override
            public void nodeClicked(NodeClickedEvent evt) {
                if(evt.getModifier() == 1)
                {
                    System.out.println("Want to remove " + evt.getNode());
                    removeNode(evt.getNode());
                    clicked = -1;
                }
                else
                {
                    if(clicked == -1)
                    {
                        System.out.println("Clicked node: " + evt.getNode());
                        clicked = evt.getNode();
                    }
                    else if(evt.getNode() != clicked)
                    {
                        System.out.println("Want to add/remove link between " + clicked + " and " + evt.getNode());
                        if(linkExists(clicked,evt.getNode()))
                        {
                            removeLink(clicked,evt.getNode());
                        }
                        else
                        {
                            addLink(clicked, evt.getNode());
                        }
                        clicked = -1;
                    }
                    else
                    {
                        clicked = -1;
                    }
                }
                updateNodeColours();
            }
            
        });
        
        // add colour control box and listener
        switchControl colourControl = new switchControl("ccontrol","army colours",armyColours);
        colourControl.CheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                armyColours = !armyColours;
                updateNodeColours();
            }
            
        });
        D.addControlPanel(colourControl);
        
        // add reset button
        ButtonControl resetButton = new ButtonControl("resetControl","reset");
        resetButton.Button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                reset = true;
            }
            
        });
        D.addControlPanel(resetButton);
        
        // create a general utility object to read the network
        General G = new General();
        
        // read network from file
        network = netGen.fromNodeArray(G.readNetworkFromSimpleListPreserveIDs("PA300m1AL.csv"));
        idCounter = network.size();
        
        // add gossipicoFAB
        addFAB(new GossipicoFAB(gen),true);
        
        // create colour map
        createColourMap();
        
        // create order array
        int[] order = createOrder();
        
        // run simulation
        while(true)
        {
            // shuffle order
            shuffleOrder(order);
            // activate all nodes
            for(int o:order)
            {
                network.get(o).getFAB(0).interact();
            }
            if(reset)
            {
                reset = false;
                addFAB(new GossipicoFAB(gen),true);
            }
            updateNodeColours();
            pause(250);
        }
    }  
    DemoInterface D;
    private int clicked = -1; // used to keep track of a clicked node.
    private int idCounter;
    private boolean armyColours = true;
    boolean reset = false;
    private Map<Integer,Float> colourMap = new HashMap<>();
}
