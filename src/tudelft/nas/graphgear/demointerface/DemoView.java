/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;
import javax.swing.JFrame;

/**
 *
 * @author Ruud
 * The DemoView is the frame showing the demo interface
 */
public class DemoView extends JFrame{

    /**
     * Creates a new frame and adds a graphPanel
     */
    public DemoView(){
        this.setSize(600, 400);
        this.setVisible(false);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        GraphReader G = new GraphReader("d:/PA300m1.svg");
        p = new graphPanel(G.getNodes(),G.getLinks());
        this.add(p);
        this.setVisible(true);
    }

    graphPanel p;
}
