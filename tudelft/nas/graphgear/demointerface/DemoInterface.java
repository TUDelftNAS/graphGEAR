/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JRootPane;


/**
 *
 * @author Ruud
 */
public class DemoInterface {

   
    /**
     * Creates a new gui with nodes at the specified coordinates
     * @param links
     * @param x x-coordinates
     * @param y y-coordinates
     */
    public void showGui(double[] x, double[] y){
        gui = new DemoGui(x,y);
        gui.setVisible(true);
    }

    /**
     * Creates an empty gui
     */
    public void showGui(){
        gui = new DemoGui();
        gui.setVisible(true);
    }

    /**
     * Adds a node info string to a node
     * @param node node id
     * @param info info string
     */
    public void addNodeInfo(int node, String info)
    {
        gui.gp.addNodeInfo(node, info);
    }
    
    /**
     * Adds a side panel to the side of the screen
     * @param sp side panel
     */
    public void addSidePanel(JPanel sp){
        gui.addSidePanel(sp);
    }
    
    /**
     * adds an info panel to the top of the screen
     * @param ip the info panel
     */
    public void addInfoPanel(JPanel ip){
        gui.addInfoPanel(ip);
    }
    
    /**
     * Adds a control panel to the bottom of the screen
     * @param cp 
     */
    public void addControlPanel(JPanel cp){
        gui.addControlPanel(cp);
    }
    
    /**
     * Returns a control panel
     * @param name name of the control panel
     * @return the control panel or null if not found
     */
    public JPanel getControlPanel(String name){
        JPanel res = null;
        Component[] C = gui.ControlPanel.getComponents();
        for(Component c : C)
        {
            if(c.getName() != null && c.getName().equals(name))
            {
                res = (JPanel)c;
            }
        }
        return res;
    }
    
    /**
     * Returns a side panel element
     * @param name name of the side panel element
     * @return the side panel element or null if not found
     */
    public JPanel getSidePanelElement(String name){
        JPanel res = null;
        int numComp = gui.SidePanelElementContainer.getComponentCount();
        Component c;
        for(int i=0;i<numComp;i++){
            c = gui.SidePanelElementContainer.getComponent(i);
            if(c.getName() != null && c.getName().equals(name))
            {
                res = (JPanel)c;
            }
        }
        return res;
    }
    
    /**
     * Returns a info panel element
     * @param name name of the info panel element
     * @return the info panel element or null if not found
     */
    public JPanel getInfoPanelElement(String name){
        JPanel res = null;
        JPanel ip = gui.InfoPanel;
        Component[] C = ip.getComponents();
        for(Component c : C)
        {
            if(c.getName() != null && (c instanceof LabelInfoPanel) && c.getName().equals(name))
            {
                res = (JPanel)c;
            }
        }
        return res;
    }
    
    /**
     * Returns a control panel
     * @param name name of the control panel
     * @return the control panel or null if not found
     */
    public JPanel getInfoPanel(String name){
        Component[] C = gui.InfoPanel.getComponents();
        JPanel res = null;
        for(Component c : C)
        {
            if(c.getName() != null && c.getName().equals(name))
            {
                res = (JPanel)c;
            }
                
        }
        return res;
    }

    /**
     * Shows the gui with the specified layout
     * @param layout 
     */
    public void showGui(String layout){
        gui = new DemoGui(layout);
        gui.setVisible(true);
    }
    public graphPanel getGraphPanel(){
        return gui == null ? null : gui.gp;
    }
    public DemoGui gui;
    
}
