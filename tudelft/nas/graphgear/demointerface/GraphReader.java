/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruud
 */
public class GraphReader {
    
    /**
     * Reads an svg file to get a layout
     * @param SVGPath file location
     */
    public GraphReader(String SVGPath){
        try 
        {
            File fin = new File(SVGPath);
            BufferedReader in = new BufferedReader(new FileReader(fin));
            String line;
            boolean inCircle = false;
            boolean inLine = false;
            int s;
            int e;
            int node = -1;
            String r  = "r=\"";
            String cx = "cx=\"";
            String cy = "cy=\"";
            String ids = "class=\"";
            String vb = "viewBox=\"";
            String cl = "fill=\"";
            String ls = "d=\"";
            String lcs = "class=\"";
            double radius = 0d;
            double xcor = 0d;
            double ycor = 0d;
            double xoff = 0d;
            double yoff = 0d;
            double xs = 1d;
            double ys = 1d;
            double lxs = 0d;
            double lys = 0d;
            double lxe = 0d;
            double lye = 0d;
            String color = "";
            int id = 0;
            int sid = 0;
            int eid = 0;
            while((line = in.readLine()) != null)
            {
                
                if(line.contains("<circle"))
                {
                    inCircle = true;
                }
                if(line.contains("<path"))
                {
                    inLine = true;
                }
                if(!inCircle && !inLine)
                {
                    s = line.indexOf(vb);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+vb.length());
                        String[] box = line.substring(s+vb.length(), e).split(" ");
                        xoff = Double.parseDouble(box[0]);
                        yoff = Double.parseDouble(box[1]);
                        xs = Double.parseDouble(box[2]) - xoff;
                        ys = Double.parseDouble(box[3]) - yoff;
                    }
                }
                if(inCircle)
                {
                    s = line.indexOf(r);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+r.length());
                        radius = Double.parseDouble(line.substring(s+r.length(), e));
                    }
                    s = line.indexOf(cx);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+cx.length());
                        xcor = Double.parseDouble(line.substring(s+cx.length(), e));
                    }
                    s = line.indexOf(cy);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+cy.length());
                        ycor = Double.parseDouble(line.substring(s+cy.length(), e));
                    }
                    s = line.indexOf(ids);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+ids.length());
                        id = Integer.parseInt(line.substring(s+ids.length(), e));
                    }
                    s = line.indexOf(cl);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+cl.length());
                        color = line.substring(s+cl.length()+1, e);
                    }
                }
                if(inLine)
                {
                    s = line.indexOf(ls);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+ls.length());
                        String[] part = line.substring(s+ls.length()+1, e).split(" ");
                        String[] lb = part[1].split(",");
                        String[] le = part[3].split(",");
                        //System.out.println(part[1] + " : "+ part[3]);
                        lxs = Double.parseDouble(lb[0]);
                        lys = Double.parseDouble(lb[1]);
                        lxe = Double.parseDouble(le[0]);
                        lye = Double.parseDouble(le[1]);
                    }
                    s = line.indexOf(lcs);
                    if(s != -1)
                    {
                        e = line.indexOf("\"", s+lcs.length());
                        String[] part = line.substring(s+lcs.length(), e).split(" ");
                        sid = Integer.parseInt(part[0]);
                        eid = Integer.parseInt(part[1]);
                    }
                }
                if(inCircle && line.contains("/>"))
                {
                    inCircle = false;
                    nodes.add(new drawNode(xcor,ycor,radius,id,color));
                }
                if(inLine && line.contains("/>"))
                {
                    inLine = false;
                    if(sid > eid) // we want ids to be sorted
                    {
                        int temp = sid;
                        sid = eid;
                        eid = temp;
                        double tempd = lxs;
                        lxs = lxe;
                        lxe = tempd;
                        
                        tempd = lys;
                        lys = lye;
                        lye = tempd;
                    }
                    links.add(new drawLink(lxs,lys,lxe,lye,sid,eid));
                }
            }
            
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(GraphReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        rescale();
    } 
    
    /**
     * Rescales all coordinates to be between 0 and 1
     */
    public void rescale(){
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        for(drawNode N : nodes)
        {
            xmin = xmin < N.x ? xmin : N.x;
            xmax = xmax > N.x ? xmax : N.x;
            ymin = ymin < N.y ? ymin : N.y;
            ymax = ymax > N.y ? ymax : N.y;
        }
        for(drawNode N : nodes)
        {
            N.x = (N.x-xmin)/(xmax-xmin);
            N.y = (N.y-ymin)/(ymax-ymin);
        }
        for(drawLink L : links)
        {
            L.xs = (L.xs-xmin)/(xmax-xmin);
            L.ys = (L.ys-ymin)/(ymax-ymin);
            L.xe = (L.xe-xmin)/(xmax-xmin);
            L.ye = (L.ye-ymin)/(ymax-ymin);
        }
    }
    
    /**
     * Gets all the links (draw links)
     * @return list containing all links
     */
    public ArrayList<drawLink> getLinks(){
        return links;
    }
    
    /**
     * Gets all the nodes (draw nodes)
     * @return list containing all nodes
     */
    public ArrayList<drawNode> getNodes(){
        
        return nodes;
    }
    ArrayList<drawNode> nodes = new ArrayList<>();
    ArrayList<drawLink> links = new ArrayList<>();
}
