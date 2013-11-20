/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.demointerface;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 *
 * @author rvandebovenkamp
 * Draws a rectangle with some text in
 */
public class CustomTextBox {
    
    /**
     * Draws a rectangle with some text in it
     * @param g2 the graphics2d object to write to
     * @param x x coordinate of box
     * @param y y coordinate of box
     * @param containerWidth width of the box
     * @param containerHeight height of the box
     * @param text text to be written in the box
     */
    public void draw(Graphics2D g2, int x, int y, int containerWidth, int containerHeight, String text){
        g2.setFont(textFont);
        String[] lines = text.split("\n");
        String longest = "";
        for(String line : lines)
        {
            if(line.length() > longest.length())
            {
                longest = line;
            }
        }
        // draw rectangle to contain info
        g2.setColor(Color.blue);
        int bw = g2.getFontMetrics().stringWidth(longest)+8;
        int bh = (g2.getFontMetrics().getHeight()*lines.length) + 2*g2.getFontMetrics().getDescent();
        if(x + bw > containerWidth)
        {
            x = x - bw - flipShift;
        }
        if(y + bh > containerHeight)
        {
            y = containerHeight- bh -1;
        }
        g2.fillRect(x, y, bw, bh);
        g2.setColor(Color.red);
        g2.drawRect(x, y, bw,bh);
        x += 4;

        g2.setColor(textColour);
        for (String line : lines)
        {
            g2.drawString(line, x, y += g2.getFontMetrics().getHeight());

        }
    }
    public void setFontSize(int _fontSize)
    {
        fontSize = _fontSize;
        textFont = new Font("Arial",Font.PLAIN,fontSize);
    }
    int flipShift = 0;
    Color textColour = Color.MAGENTA;
    int fontSize = 10;
    Font textFont = new Font("Arial",Font.PLAIN,fontSize);
}
