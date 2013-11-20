/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JPanel;

/**
 *
 * @author Ruud
 * The graph panel 
 */
public class graphPanel extends JPanel{
    public graphPanel(){
        this.setLayout(null);
        this.setToolTipText(null);
        this.setVisible(true);
        this.setName("GraphPanel");
        this.addMouseMotionListener(new MouseMotionListener(){

            @Override
            public void mouseDragged(MouseEvent e) {
                if(dragNode != -1)
                {
                    double xs = (double)(gw()-b);
                    double ys = (double)(gh()-b);
                    nodes.get(dragNode).x = (double)e.getX()/xs;
                    nodes.get(dragNode).y = (double)e.getY()/ys;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int n;
                
                if((n = findNodeClicked(e.getPoint()))!= -1)
                {
                    showInfoForNode = n;
                    repaint();
                }
                else
                {
                    if(showInfoForNode != -1)
                    {
                        showInfoForNode = -1;
                        repaint();
                    }
                    showInfoForNode = -1;
                }
                
                
            }
        
        });
        this.addMouseListener(new MouseListener(){
            
            @Override
            public void mouseClicked(MouseEvent e) {
                int n = findNodeClicked(e.getPoint());
                
                if(n != -1)
                {
                    fireNodeClickedEvent(n,e.isShiftDown() ? 1 : 0);
                }
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int n = findNodeClicked(e.getPoint());
                if(n != -1)
                {
                    dragNode = n;
                    x_b = nodes.get(n).x;
                    y_b = nodes.get(n).y;
                            
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragNode = -1;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                
            }
        });
    }
    
    /**
     * Creates a new graph panel containing the specified nodes and link
     * @param _nodes list of draw nodes
     * @param _links list of draw links
     */
    public graphPanel(ArrayList<drawNode> _nodes, ArrayList<drawLink> _links){
        this();
        if(_nodes.size() > MAX_NETWORK_SIZE)
        {
            System.err.println("Network too big. Maximum size for DemoInterface is currently 10k nodes");
            System.exit(3);
        }
        for(drawNode N : _nodes)
        {
            nodes.put(N.id, N);
        }
        for(drawLink L : _links)
        {
            links.put((L.sid*MAX_NETWORK_SIZE)+L.eid,L);
        }
        
    }
    

    /**
     * Removes a node from the graph
     * @param n node to be removed
     */
    public void removeNode(int n){
        nodes.remove(n);
    }
    /**
     * Removes a node from the graph
     * @param N node to be removed
     */
    public void removeNode(drawNode N){
        nodes.remove(N.id);
    }
    
    /**
     * Removes a link from the graph
     * @param L link to be removed
     */
    public void removeLink(drawLink L){
        links.remove((L.sid*MAX_NETWORK_SIZE)+L.eid);
    }
    
    /**
     * Remove a link between two node ids
     * @param s source id
     * @param d destination id
     */
    public void removeLink(int s, int d){
        boolean res = false;
        if(s < d)
        {
            res = (links.remove((s*MAX_NETWORK_SIZE)+d) != null);
        }
        else
        {
            res = (links.remove((d*MAX_NETWORK_SIZE)+s) != null);
        }
        System.out.println("Remove link between " + s + " and " + d + (res ? " successfully" : " without success"));
    }
    
    /**
     * Adds a link between two nodes
     * @param s source node
     * @param d destination node
     */
    public void addLink(int s, int d){
        drawNode S,D;
        if(s < d)
        {
            S = nodes.get(s);
            D = nodes.get(d);
        }
        else
        {
            D = nodes.get(s);
            S = nodes.get(d);
        }
        if(S == null || D == null)
        {
            System.err.println("Cannot add link between " + s + " and "+ d);
        }
        else
        {
            links.put((S.id*MAX_NETWORK_SIZE)+D.id, new drawLink(S.x,S.y,D.x,D.y,S.id,D.id));
        }
    }
    
    /** 
     * Adds a link to the graph
     * @param L the link to be added
     */
    public void addLink(drawLink L){
        links.put((L.sid*MAX_NETWORK_SIZE)*L.eid, L);
    }
    
    /**
     * Adds a node to the graph
     * @param N the node to be added
     */
    public void addNode(drawNode N){
        nodes.put(N.id, N);
    }
    
    /**
     * store BufferedImage to file
     * @param image BufferedImage
     * @param outputFile output image file
     * @param quality quality of output image
     * @return true success, else fail
     */
    public static boolean storeImage(BufferedImage image,File outputFile,float quality){
        try {
            //reconstruct folder structure for image file output
            if(outputFile.getParentFile() != null && !outputFile.getParentFile().exists()){
              outputFile.getParentFile().mkdirs();
            }
                  if(outputFile.exists()){
                      outputFile.delete();
                  }
            //get image file suffix
            String extName = "gif";
            //get registry ImageWriter for specified image suffix
            Iterator writers = ImageIO.getImageWritersByFormatName(extName);
            ImageWriter imageWriter = (ImageWriter) writers.next();
            //set image output params
            ImageWriteParam params = new JPEGImageWriteParam(null);
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            params
                .setProgressiveMode(javax.imageio.ImageWriteParam.MODE_DISABLED);
            params.setDestinationType(new ImageTypeSpecifier(IndexColorModel
                .getRGBdefault(), IndexColorModel.getRGBdefault()
                .createCompatibleSampleModel(16, 16)));
            //writer image to file
            ImageOutputStream imageOutputStream = ImageIO
                .createImageOutputStream(outputFile);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(null, new IIOImage(image, null, null), params);
            imageOutputStream.close();
            imageWriter.dispose();
            return true;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Grabs a frame and stores it to a file
     * @param path path of the location where to safe the file
     * @param name name of the image file
     * @param quality quality of gif
     * @param frame frame number. If frame number equals 1 the content of the path folder is emptied
     */
    public void grabAndStoreFrame(String path, String name, float quality, int frame){
        if(frame == 1)
        {
            File f = new File(path);
            if(f.isDirectory())
            {
                String[] content = f.list();
                for(String con : content)
                {
                    File c = new File(f,con);
                    c.delete();
                }
            }
        }
        int w = this.getWidth();
        int h = this.getHeight();
        BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        this.paint(g);
        String fn = Integer.toString(frame);
        while(fn.length() < 3)
        {
            fn = "0" + fn;
        }
        storeImage(bi, new File(path + "/" + name + fn + ".gif"),quality);
    }
    
    /**
     * Gets the height of this panel
     * @return height of panel
     */
    public int gh(){
        return this.getHeight();
    }
    
    /**
     * Gets the width of this panel
     * @return width of panel
     */
    public int gw(){
        return this.getWidth();
    }
    
    /**
     * Set the node colour, but don't repaint
     * @param node node id 
     * @param colour node colour
     */
    public void setNodeColorNoRepaint(int node, Color colour){
        if(nodes.containsKey(node))
        {
            nodes.get(node).color = colour;
        }
    }
    
    /**
     * Set the node colour and repaint
     * @param node node id
     * @param colour node colour
     */
    public void setNodeColor(int node, Color colour){
        if(nodes.containsKey(node))
        {
            nodes.get(node).color = colour;
        }
        this.repaint();
    }
    
    /**
     * Sets the colour of all links, but don't repaint
     * @param colour link colour
     */
    public void setAllLinkColorNoRepaint(Color colour){
        for(drawLink L : links.values())
        {
            L.color = colour;
        }
    }
    
    /**
     * Sets the colour of all links and repaints
     * @param colour link colour
     */
    public void setAllLinkColor(Color colour){
        for(drawLink L : links.values())
        {
            L.color = colour;
        }
        this.repaint();
    }
    
    /**
     * Set the colour of a particular link, both don't repaint
     * @param s source of link
     * @param d destination of link
     * @param colour link colour
     */
    public void setLinkColorNoRepaint(int s, int d, Color colour){
        int index = s > d ? s*nodes.size() + d : d*nodes.size() + s;
        if(links.containsKey(index))
        {
            links.get(index).color = colour;
        }
    }
    
    /**
     * Set the colour of a particular link and repaint
     * @param s source of the link
     * @param d destination of the link
     * @param colour link colour
     */
    public void setLinkColor(int s, int d, Color colour){
        int index = s > d ? s*nodes.size() + d : d*nodes.size() + s;
        if(links.containsKey(index))
        {
            links.get(index).color = colour;
        }
        this.repaint();
    }

    /**
     * Finds whether a point is within a node
     * @param p point
     * @return node id that the point is in, or -1 if no node contains the point
     */
    public int findNodeClicked(Point p){
        double xs = (double)(this.getWidth()-b-(nodes.get(0).r*nodeScaling));
        double ys = (double)(this.getHeight()-b-(nodes.get(0).r*nodeScaling)-noticeBorder);
        for(drawNode N : nodes.values())
        {
            Shape S = new Ellipse2D.Double(N.x*xs,N.y*ys, N.r*nodeScaling, N.r*nodeScaling);
            if(S.contains(p))
            {
                return N.id;
            }
        }
        return -1;
    }
    
    /**
     * Sets all nodes to offColour.
     */
    public void reset(){
        for(drawNode N : nodes.values())
        {
            N.color = offColour;
        }
        this.repaint();
    }

    /**
     * Adds node info to the node info map. Node info will be shown next to a node
     * when the mouse is placed over it
     * @param node id of the node
     * @param info the info to be shown
     */
    public void addNodeInfo(int node, String info){
        nodeInfo.put(node, info);
    }
    
    /**
     * Set node scaling
     * @param s node scaling
     */
    public void setNodeScaling(double s){
        nodeScaling = s;
        this.repaint();
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(nodes.get(0) == null)
        {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.blue);
        int x = 0;
        int y = 0;
        double xs = (double)(this.getWidth()-b-(nodes.get(0).r*nodeScaling));
        double ys = (double)(this.getHeight()-b-(nodes.get(0).r*nodeScaling)-noticeBorder);
        g2.setColor(Color.black);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.setColor(Color.blue);
        drawNode s;
        drawNode d;
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        g2.setColor(Color.white);
        if(!ignoreLinks)
        {
            for(drawLink L : links.values())
            {
                g2.setColor(L.color);
                s = nodes.get(L.sid);
                d = nodes.get(L.eid);
                if(s != null && d != null)
                {
                    g2.drawLine((int)((s.x*xs)+(s.r*nodeScaling)/2d), (int)((s.y*ys)+(s.r*nodeScaling)/2d),(int)((d.x*xs)+(d.r*nodeScaling)/2d),(int)((d.y*ys)+(d.r*nodeScaling)/2d));
                }
            }
        }
        for(drawNode N : nodes.values())
        {
            g2.setColor(N.color);
            g2.fill(new Ellipse2D.Double(N.x*xs,N.y*ys, N.r*nodeScaling, N.r*nodeScaling));
            g2.setColor(Color.black);
            g2.draw(new Ellipse2D.Double(N.x*xs,N.y*ys, N.r*nodeScaling, N.r*nodeScaling));
        }
        if(showInfoForNode != -1 && nodeInfo.containsKey(showInfoForNode))
        {
            drawNode N = nodes.get(showInfoForNode);

            x = (int)(N.x*xs + N.r*nodeScaling) + 3;
            y = (int)(N.y*ys);
            CTB.flipShift = (int)(N.r*nodeScaling)+5;
            CTB.draw(g2, x, y, this.getWidth(), this.getHeight(), nodeInfo.get(showInfoForNode));
        }
        g2.setColor(Color.white);
        g2.setFont(new Font("Arial",Font.PLAIN,10));
        g2.drawString("Ruud van de Bovenkamp - NAS", b, this.getHeight()-b);
        doneDrawing = true;
    }
    
    /**
     * Fires a node clicked event
     * @param node the node that was clicked
     */
    private synchronized void fireNodeClickedEvent(int node, int modifier){
        NodeClickedEvent evt = new NodeClickedEvent(this, node, modifier);
        for(NodeClickedListener l : nodeClickedListeners)
        {
            l.nodeClicked(evt);
        }
    }
    
    /**
     * Add a nodeclickedlistener to the panel
     * @param l node clicked listener
     */
    public synchronized void addNodeClickedListener(NodeClickedListener l){
        nodeClickedListeners.add(l);
    }
    
    private ArrayList<NodeClickedListener> nodeClickedListeners = new ArrayList<>();
    Map<Integer,String> nodeInfo = new HashMap<>();
    Map<Integer,drawNode> nodes = new HashMap<>();
    Map<Integer,drawLink> links = new HashMap<>();
    //ArrayList<drawLink> links;
    Color onColour = Color.pink;
    Color offColour = Color.blue;
    
    private double nodeScaling = 1.8d;
    int noticeBorder = 10;
    int dragNode = -1;
    int b = 3;
    double x_b = 0;
    double y_b = 0;
    BufferedImage image = null;
    Image dimg = null;
    int showInfoForNode = -1;
    static final int MAX_NETWORK_SIZE = 10000;
    CustomTextBox CTB = new CustomTextBox();
    public volatile boolean doneDrawing = true;
    boolean ignoreLinks = false;
}
