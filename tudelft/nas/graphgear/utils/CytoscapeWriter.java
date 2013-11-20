/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author rvandebovenkamp
 * Writes a network and properties to a cytoscape readable XGMML file
 */
public class CytoscapeWriter {
    /**
     * Creates a new instance of CytoscapeWriter. The network has to be specified
     * in the constructor
     * @param network the network
     * @param name the name of the network
     */
    public CytoscapeWriter(Node[] network, String name){
        // Create the graph
        graph = new Element("graph","","http://www.cs.rpi.edu/XGMML");
        // Write header
        createHeader(graph, name);
        // init node map
        nodes = new HashMap<>();
        links = new HashSet<>();
        // Add all nodes the map and links to the graph
        Set<String> L = new HashSet<>();
        int s = 0;
        int d = 0;
        for(Node N : network)
        {
            Element node_E = new Element("node");
            // ID and label of the node (as attributes)
            node_E.setAttribute("label", Integer.toString(N.name));
            node_E.setAttribute("id",Integer.toString(N.name));
            nodes.put(N.name,node_E);
            // Add links
            s = N.name;
            for(Node D : N.links)
            {
                d = D.name;
                // Add every link only once
                if(!L.contains((s + " " + d)))
                {
                    Element link_E = new Element("edge");
                    link_E.setAttribute("label","edge");
                    link_E.setAttribute("source", Integer.toString(s));
                    link_E.setAttribute("target", Integer.toString(d));
                    L.add((d + " " + s));
                    links.add(link_E);
                }
            }
        }
    }
    
    /**
     * Adds properties to the network. Each property in the properties array is
     * supposed to belong to the corresponding node in the network array
     * @param nw the network
     * @param properties properties array
     * @param key name of the property
     */
    public void addProperties(Node[] nw, int[] properties, String key){
        for(int i=0;i<nw.length;i++)
        {
            Element E = nodes.get(nw[i].name);
            Element P = new Element("att");
            P.setAttribute("type","integer");
            P.setAttribute("name",key);
            P.setAttribute("value",Integer.toString(properties[i]));
            E.addContent(P);
        }
    }
    
    /**
     * Sets the coordinates of the nodes in case of a visualisation. The x and y coordinates
     * correspond to the node in the network array
     * @param nw the network
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addCoordinate(Node[] nw, double[] x, double[] y){
        for(int i=0;i<nw.length;i++)
        {
            Element E = nodes.get(nw[i].name);
            Element P = new Element("graphics");
            P.setAttribute("x",Double.toString(x[i]));
            P.setAttribute("y",Double.toString(y[i]));
            E.addContent(P);
        }
        
    }
    
    /**
     * Adds properties to the network. Each property in the properties array is
     * supposed to belong to the corresponding node in the network array
     * @param nw the network
     * @param properties properties array
     * @param key name of the property
     */
    public void addProperties(Node[] nw, double[] properties, String key){
        for(int i=0;i<nw.length;i++)
        {
            Element E = nodes.get(nw[i].name);
            Element P = new Element("att");
            P.setAttribute("type","real");
            P.setAttribute("name",key);
            P.setAttribute("value",Double.toString(properties[i]));
            E.addContent(P);
        }
        
    }
    
    /**
     * writes the network to the file.
     * @param file file location
     */
    public void writeToFile(String file){
        // Add all the nodes from the map to the graph
        for(Element e:nodes.values())
        {
            graph.addContent(e);
        }
        // Add the links
        for(Element e : links)
        {
            graph.addContent(e);
        }
         // Now we write the element @graph_E to a file.
        Document d = new Document(graph); // Root element sent to the constructor
        FileOutputStream output;
        try 
        {
            output = new FileOutputStream(file);
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(d,output);
            output.flush();
            output.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(CytoscapeWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
    * XML header-fuzz that must be added in order for Cytoscape to work.
    * 
    * @param graph_E
    */
    private void createHeader(Element graph_E, String name){
            graph_E.setAttribute("label",name);
            graph_E.addNamespaceDeclaration(Namespace.getNamespace(
                            "dc", "http://purl.org/dc/elements/1.1/"));
            graph_E.addNamespaceDeclaration(Namespace.getNamespace(
                            "xlink", "http://www.w3.org/1999/xlink"));
            graph_E.addNamespaceDeclaration(Namespace.getNamespace(
                            "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
            graph_E.addNamespaceDeclaration(Namespace.getNamespace(
                            "cy", "http://www.cytoscape.org"));
            graph_E.addNamespaceDeclaration(Namespace.getNamespace(
                            "dc", "http://purl.org/dc/elements/1.1/"));
            graph_E.setAttribute("directed","1");
    } 
    Element graph;
    Map<Integer, Element> nodes;
    Set<Element> links;
}
