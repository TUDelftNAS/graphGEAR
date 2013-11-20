/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GraphSidePanel.java
 *
 * Created on 29-aug-2012, 18:07:45
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author Ruud
 */
public class GraphSidePanel extends javax.swing.JPanel {
    ArrayList<Double> columnData;
    Object[] multiColumnData;
    int dataPointLimit;
    boolean minmaxFixed = false;
    public boolean drawAverage = false;
    boolean multiTrace = false;
    public boolean printLastValue = false;
    double fixedMin;
    double fixedMax;
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
    Font F = new Font("Arial",Font.BOLD,14);
        
        NumberFormat formatter;
    /** Creates new form GraphSidePanel */
    public GraphSidePanel() {
        initComponents();
        this.setSize(this.getPreferredSize());
        this.setVisible(true);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(','); 
        
        formatter = new DecimalFormat("0.00", otherSymbols);
    }
    
    /**
     * Creates a new graph side panel with a fixed min and max value
     * @param _dataPointLimit number of datapoints to store
     * @param name name of the graph
     * @param min fixed minimum
     * @param max fixed maximum
     * @param av true if average should be drawn, false otherwise
     */
    public GraphSidePanel(int _dataPointLimit, String name, double min, double max, boolean av){
        this();
        this.setName(name);
        dataPointLimit = _dataPointLimit;
        columnData = new ArrayList<>();
        fixedMin = min;
        fixedMax = max;
        minmaxFixed = true;
        drawAverage = av;
    }
    
    /**
     * Creates a new multi graph with a fixed minimum and maximum
     * @param _dataPointLimit number of points to be remembered
     * @param name name of the graph
     * @param traces number of traces
     * @param min fixed minimum
     * @param max fixed maximum
     */
    public GraphSidePanel(int _dataPointLimit, String name, int traces, double min, double max){
        this();
        this.setName(name);
        dataPointLimit = _dataPointLimit;
        fixedMin = min;
        fixedMax = max;
        minmaxFixed = true;
        if(traces < 2)
        {
            System.out.println("Don't use multi trace method for single trade graph!");
        }
        multiTrace = true;
        multiColumnData = new Object[traces];
        for(int i=0;i<traces;i++)
        {
            multiColumnData[i] = new ArrayList<>();
        }
        Color[] cs = new Color[5];
        cs[0] = Color.BLUE;
        cs[1] = Color.GREEN;
        cs[2] = Color.MAGENTA;
        cs[3] = Color.PINK;
        cs[4] = Color.RED;
        lineColour = new Color[traces];
        int ci = 0;
        for(int i=0;i<lineColour.length;i++)
        {
            lineColour[i] = cs[ci++];
            if(ci > 4)
            {
                ci = 0;
            }
        }
    }
    
    /**
     * Creates a new multi graph with minimum and maximum taken from the data points
     * @param _dataPointLimit number of data points to draw
     * @param name name of the graph
     * @param traces number of traces to be used
     */
    public GraphSidePanel(int _dataPointLimit, String name, int traces){
        this();
        this.setName(name);
        dataPointLimit = _dataPointLimit;
        if(traces < 2)
        {
            System.out.println("Don't use multi trace method for single trade graph!");
        }
        multiTrace = true;
        multiColumnData = new Object[traces];
        for(int i=0;i<traces;i++)
        {
            multiColumnData[i] = new ArrayList<>();
        }
        Color[] cs = new Color[5];
        cs[0] = Color.BLUE;
        cs[1] = Color.GREEN;
        cs[2] = Color.MAGENTA;
        cs[3] = Color.PINK;
        cs[4] = Color.RED;
        lineColour = new Color[traces];
        int ci = 0;
        for(int i=0;i<lineColour.length;i++)
        {
            lineColour[i] = cs[ci++];
            if(ci > 4)
            {
                ci = 0;
            }
        }
    }
    
    /**
     * Creates a new graph panel with variable minimum and maximum
     * @param _dataPointLimit number of data points to draw
     * @param name name of the graph
     * @param av draw average or not
     */
    public GraphSidePanel(int _dataPointLimit, String name, boolean av){
        this();
        this.setName(name);
        dataPointLimit = _dataPointLimit;
        columnData = new ArrayList<>();
        drawAverage = av;
    }
    
    /**
     * Computes the average of the trace (NOT MULTITRACE)
     * @return 
     */
    public double getAverage(){
        if(multiTrace)
        {
            return -1d;
        }
        double sum = 0d;
        for(double d: columnData)
        {
            sum += d;
        }
        return sum/(double)columnData.size();
    }
    
    /**
     * Clears all column data
     */
    public void clear(){
        if(multiTrace)
        {
            for(int i = 0;i<multiColumnData.length;i++)
            {
                ArrayList<Double> l = (ArrayList<Double>)multiColumnData[i];
                l.clear();
            }
        }
        else
        {
            columnData.clear();
        }
        
    }
    
    /**
     * Adds a multipoint to the graph
     * @param in array of points
     */
    public synchronized void addMultiTraceDataPoint(double[] in){
        if(in.length != multiColumnData.length)
        {
            System.err.println("Not enough data points. Length in: " + in.length + " num traces: " + multiColumnData.length);
        }
        else
        {
            for(int i = 0;i<multiColumnData.length;i++)
            {
                ArrayList<Double> l = (ArrayList<Double>)multiColumnData[i];
                if(dataPointLimit != -1 && l.size() > dataPointLimit)
                {
                    l.remove(0);
                    l.add(in[i]);
                }
                else
                {
                    l.add(in[i]);
                }
            }
            this.repaint();
        }
    }
    
    /**
     * Adds a data point to the graph
     * @param in data point
     */
    public synchronized void addDataPoint(double in){
        
        if(dataPointLimit != -1 && columnData.size() > dataPointLimit)
        {
            columnData.remove(0);
            columnData.add(in);
        }
        else
        {
            columnData.add(in);
        }
        this.repaint();
    }
    
    /**
     * Returns the minimum and maximum of the trace data
     * @return the minimum and maximum of the trace data
     */
    public double[] getMinMax(){
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        if(multiTrace)
        {
            for(int i = 0;i<multiColumnData.length;i++)
            {
                ArrayList<Double> l = (ArrayList<Double>)multiColumnData[i];
                for(double d : l)
                {
                    min = min < d ? min : d;
                    max = max > d ? max : d;
                }
            }
        }   
        else
        {
            for(double d : columnData)
            {
                min = min < d ? min : d;
                max = max > d ? max : d;
            }
        }
        return new double[]{min,max};
    }
    
    /**
     * Sets the colour of the line
     * @param in trace number
     * @param trace line colour
     */
    public void setLineColour(Color in, int trace){
        lineColour[trace] = in;
    }
    
    /**
     * Gets the average of the offered data list
     * @param data data to compute the average of
     * @return the average of the data points
     */
    private double getAv(ArrayList<Double> data){
        double res = 0d;
        for(double d : data)
        {
            res += d;
        }
        return res/(double)data.size();
    }
    
    /**
     * Paints the last value as a string next to the trace
     * @param g2 graphics object to paint to
     * @param value last value
     * @param max maximum value
     * @param b border
     * @param pixPerUnit current pixelsPerUnit in the graph
     */
    private void paintLastValue(Graphics2D g2, double value, double max, int b, double pixPerUnit){
        String val = formatter.format(value);
        int y1 = (int)(b+(max-value)*pixPerUnit);
        g2.setFont(F);
        g2.drawString(val, this.getWidth()-b-g2.getFontMetrics().stringWidth(val)-10, y1);
    }
    
    /**
     * Draws a line showing the average value of the graph and a string with the average
     * @param g2 graphics object to draw to
     * @param data data points
     * @param max maximum value
     * @param b border
     * @param pixPerUnit  the current pixelPerUnit ratio
     */
    private void paintAverage(Graphics2D g2, ArrayList<Double> data, double max, int b, double pixPerUnit){
        double av = getAv(data);
        int y1 = (int)(b+(max-av)*pixPerUnit);
        Color c = g2.getColor();
        g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),150));
        g2.drawLine(b, y1, this.getWidth()-b, y1);
        String avs = formatter.format(av);
        int delta = 0;
        if(y1 < this.getHeight()/2)
        {
            delta = g2.getFontMetrics().getHeight();
        }
        g2.setFont(F);
        g2.setColor(Color.red);
        g2.drawString(avs, this.getWidth()-b-g2.getFontMetrics().stringWidth(avs)-10, y1+delta);
    }
    
    /**
     * Paints a trace
     * @param g2 graphics object to paint to
     * @param columnData data points
     * @param max maximum value
     * @param b border
     * @param tickSpacing spacing of the ticks
     * @param pixPerUnit current pixelPerUnit ratio
     */
    private void paintTrace(Graphics2D g2,ArrayList<Double> columnData,double max, int b, float tickSpacing, double pixPerUnit){
        int x1,x2,y1,y2;
        
        for(int i =0; i < columnData.size()-1;i++)
        {
                x1 = (int)(b + i*tickSpacing);
                x2 = (int)(b + (i+1)*tickSpacing);
                y1 = (int)(b+(max - columnData.get(i))*pixPerUnit);
                y2 = (int)(b+(max - columnData.get(i+1))*pixPerUnit);
                g2.drawLine(x1, y1, x2, y2);
        }
    }
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());
        int b = 4;
        int tl = 3;
        int w = this.getWidth();
        int h = this.getHeight();
        float tickSpacing = -1;
        if(multiTrace)
        {
            ArrayList<Double> dummy = (ArrayList<Double>)multiColumnData[0];
            tickSpacing = (float)(w - 2*b)/(float)(dummy.size()-1);
        }
        else
        {
            tickSpacing = (float)(w - 2*b)/(float)(columnData.size()-1);
        }
        g2.drawRect(b, b, w-2*b, h-2*b);
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        double[] minmax;
        if(minmaxFixed)
        {
            minmax = new double[]{fixedMin,fixedMax};
        }
        else
        {
            minmax = getMinMax();
        }
        double range = minmax[1] - minmax[0];
        boolean zeroRange = (range == 0);
        if(zeroRange)
        {
            range = minmax[1]*2;
        }
        double pixPerUnit = (double)((h-(2*b)))/range;
        if(minmax[0] < 0 && minmax[1] > 0 )
        {
            g2.setColor(Color.gray);
            y1 = (int)(b+(minmax[1])*pixPerUnit);
            g2.drawLine(b, y1, w-b, y1);
        }
        
        // Draw traces
        if(multiTrace)
        {
            for(int i =0;i<multiColumnData.length;i++)
            {
                ArrayList<Double> data = (ArrayList<Double>)multiColumnData[i];
                g2.setColor(lineColour[i]);
                if(drawAverage)
                {
                    paintAverage(g2,data,minmax[1],b,pixPerUnit);
                }
                g2.setColor(lineColour[i]);
                paintTrace(g2,data,minmax[1],b,tickSpacing,pixPerUnit);
                if(printLastValue && data.size() > 1)
                {
                    paintLastValue(g2,(double)data.get((data.size()-1)),minmax[1],b,pixPerUnit );
                }
            }
        }
        else
        {
            g2.setColor(lineColour[0]);
            if(!zeroRange)
            {
                paintTrace(g2,columnData,minmax[1],b,tickSpacing,pixPerUnit);
            }
            else
            {
                paintTrace(g2,columnData,2*minmax[1],b,tickSpacing,pixPerUnit);
            }
            if(drawAverage && !zeroRange)
            {
               paintAverage(g2,columnData,minmax[1],b,pixPerUnit);
            }
        }
        
        g2.setFont(F);
        g2.setColor(Color.red);
        String mins = formatter.format(minmax[0]);
        String maxs = formatter.format(minmax[1]);
        String avs = formatter.format(getAverage());
        String upper = maxs; 
        String lower = mins ;
        g2.drawString(upper, b+4, b+16);
        g2.drawString(lower, b+4, h-b-8);
    }
    private Color lineColour[] = new Color[]{Color.BLUE};
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(240, 240, 140));
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setPreferredSize(new java.awt.Dimension(290, 225));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 221, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
