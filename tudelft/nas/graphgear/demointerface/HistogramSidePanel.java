/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import tudelft.nas.graphgear.utils.Analyser;


/**
 *
 * @author rvandebovenkamp
 */
public class HistogramSidePanel extends GraphSidePanel{
    /**
     * Creates a new HistogramSidePanel
     * @param name name of the side panel
     */
    public HistogramSidePanel(String name){
        super();
        this.setName(name);
        this.addMouseMotionListener(new MouseMotionListener(){

            @Override
            public void mouseDragged(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                barInfo = -1;
                if(barBacks != null)
                {
                    for(int i=0;i<barBacks.length;i++)
                    {
                        if(barBacks[i].contains(e.getPoint()))
                        {
                            if(barInfo != i)
                            {
                                barInfo = i;
                                repaint();
                            }
                        }
                    }
                }
            }
            
        
        });
        this.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mousePressed(MouseEvent e) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                barInfo = -1;
                repaint();
            }
            
        });
        setFontSize(16);
    }
    
    /**
     * Updates the data in the histogram
     * @param data raw data that will be processed to create draw a histogram
     */
    public void updateData(int[] data){
        A.clearBin(0);
        for(int i : data)
        {
            A.addToBin(0, i, 1);
        }
        histData = A.getHistogramFromBin(0);
        maxScore = 0;
        for(int[] i : histData)
        {
            maxScore = maxScore > i[1] ? maxScore : i[1];
        }
        barBacks = null;
        repaint();
    }
    public void setFontSize(int fontSize){
        CTB.setFontSize(fontSize);
    }
    public void paintComponent(Graphics g){
        if(histData == null || histData.length == 0)
        {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        g2.setFont(textFont);
        int h = this.getHeight();
        int w = this.getWidth();
        g2.clearRect(0, 0, w, h);
        int b = 4;
        int yoff = 10;
        int bottomLine = h - b - g2.getFontMetrics().getHeight();
        int tackHeight = 5;
        int ib = 1;
        int binWidth = (w - (2*b) - ((histData.length-1)*ib)) / histData.length;
        int binHeight = bottomLine - yoff;
        double pixPerUnit = (double)(binHeight)/(double)maxScore;
        int x = b;
        g2.setColor(barColour);
        barBacks = new Rectangle2D[histData.length];
        x = b;
        for(int i=0;i<histData.length;i++)
        {
            barBacks[i] = new Rectangle2D.Double(x,yoff,binWidth,binHeight);
            x += binWidth + ib;
        }
        g2.setColor(barBackColour);
        for(Rectangle2D r : barBacks)
        {
            g2.fill(r);
        }
        x = b+ib;
        
        int bh =0;
        for(int[] i : histData)
        {
            bh = (int)(i[1] * pixPerUnit);
            g2.setColor(barColour);
            g2.fillRect(x, yoff + (binHeight - bh), binWidth, bh);
            g2.setColor(Color.black);
            g2.drawLine(x-ib, bottomLine-tackHeight, x-ib, bottomLine);
            x += binWidth + ib;
        }
        g2.drawLine(x-ib, bottomLine-tackHeight, x-ib, bottomLine);
        g2.setColor(Color.BLACK);
        {
            g2.drawLine(b, bottomLine, x-ib, bottomLine);
        }
        if(barInfo != -1 && barBacks != null && barInfo < barBacks.length)
        {
            Rectangle2D bar = barBacks[barInfo];
            int xt = (int)bar.getCenterX();
            int yt = (int)bar.getCenterY();
            String text = "bin: " + histData[barInfo][0] + "\ncount: " + histData[barInfo][1];
            CTB.draw(g2, xt, yt, this.getWidth(), this.getHeight(), text);
        }
    }
    Analyser A = new Analyser();
    int[][] histData = null;
    int maxScore;
    Color barColour = Color.BLUE;
    Color barBackColour = Color.YELLOW;
    Rectangle2D[] barBacks;
    Font textFont = new Font("Arial",Font.PLAIN,10);
    int barInfo = 1;
    CustomTextBox CTB = new CustomTextBox();
}
