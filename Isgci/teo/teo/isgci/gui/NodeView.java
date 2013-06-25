/*
 * The view of a node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodeView.java,v 2.1 2011/09/29 08:52:48 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.problem.Problem;
import teo.isgci.problem.Complexity;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.Utility;
import teo.isgci.gc.GraphClass;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.*;
import java.util.Set;
import org.xml.sax.SAXException;

/**
 * Displays a node.
 */
public class NodeView<V,E> implements View {
    protected GraphView<V,E> parent;
    protected V node;
    protected Point center;
    protected Dimension size;
    /** The label that is displayed in the node, shortened automatically from
     * the fullname */
    protected String label;
    /** The fullname of the node (e.g. for identification) */
    protected String fullName;
    protected Color color;
    protected boolean marked;
    // For dragging
    protected boolean shadowed;
    protected Point anchor;
    protected Point shadowLocation;

    /** Margins between label and border */
    protected static final int HORMARGIN = 7;
    protected static final int VERTMARGIN = 7;


    public NodeView(GraphView<V,E> parent, V node) {
        this.parent = parent;
        this.node = node;
        this.marked = false;
        this.center = new Point();
        this.color = Color.white;
        size = new Dimension();
        label = "";
        fullName = "";
        shadowed = false;
        shadowLocation = new Point();
    }


    /**
     * Get the node.
     */
    public V getNode() {
        return node;
    }


    /** Get the full name */
    public String getFullName() {
        return fullName;
    }

    /** Set the full name */
    protected void setFullName(String s) {
        fullName = s;
    }

    /** Get the depicted label */
    public String getLabel() {
        return label;
    }

    /** Set the depicted label */
    protected void setLabel(String s) {
        label = teo.isgci.util.Utility.getShortName(s);
        updateSize();
    }

    /** Set the fullname to s and the label to the shortened form of s. */
    public void setNameAndLabel(String s) {
        setFullName(s);
        setLabel(s);
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    protected LatexGraphics getLatexGraphics() {
        return parent.getLatexGraphics();
    }

    public void setMark(boolean b) {
        marked = b;
    }

    public void updateSize() {
        Graphics g = new NulGraphics();
        g.setFont(getLatexGraphics().getFont());
        FontMetrics m = g.getFontMetrics();
        
        size = new Dimension(
                getLatexGraphics().getLatexWidth(g, label) + 2*HORMARGIN,
                m.getHeight() + 2*VERTMARGIN);
    }

    public Dimension getSize() {
        return new Dimension(size);
    }


    public Point getCenter() {
        return new Point(center);
    }


    public void setCenter(Point p) {
        setCenter(p.x, p.y);
    }

    public void setCenter(int x, int y) {
        center.x = x;
        center.y = y;
    }

    /**
     * Return the top left corner of the bounding box.
     */
    public Point getLocation() {
        return new Point(center.x - size.width/2, center.y - size.height/2);
    }


    /**
     * Move the node sucht that the top left corner is at the given point.
     */
    public void setLocation(Point p) {
        setCenter(p.x + size.width/2, p.y + size.height/2);
    }


    /**
     * Return the bounding box.
     */
    public Rectangle getBounds() {
        return new Rectangle(center.x - size.width/2,
                center.y - size.height/2, size.width, size.height);
    }

    
    public boolean contains(Point p) {
        return getBounds().contains(p);
    }


    /**
     * Return the intersection of the border of this node and a straight line
     * going from the center to p. Meant for drawing edges.
     * @param p the other endpoint of the line.
     */
    public Point getBorder(Point p) {
        /* Calculate the intersection as if the center of the ellips is at
         * (0,0) and p is in the first quadrant
         */
        double w = (double) size.width/2;
        double h = (double) size.height/2;
        double px = Math.abs(p.x - center.x);
        double py = Math.abs(p.y - center.y);
        int interx, intery;

        if (px < 0.5) {                         // Vertical line
            interx = 0;
            intery = (int) h;
        } else {
            double a = (double) py/px;
            double dinterx = h / Math.sqrt(a*a + (h*h)/(w*w));
            interx = (int) dinterx;
            intery = (int) (a*dinterx);
        }
        intery++;                               // Allow for line thickness

        // Now move the intersection to the proper quadrant 
        Point result = new Point(center);
        if (p.x > result.x)
            result.x += interx;
        else
            result.x -= interx;
        if (p.y > result.y)
            result.y += intery;
        else
            result.y -= intery;
        //System.out.println("rect: "+r+"\tcenter ("+(r.x+w)+","+(r.y+h)+")");
        //System.out.println(p+" ("+px+","+py+")" + "a="+a);
        //System.out.println(" inter= ("+interx+","+intery+") point="+result);
        //if (interx > w) System.out.println("X TOO LARGE!");
        //if (intery > h) System.out.println("Y TOO LARGE!");

        return result;
    }

    public void setShadow(boolean b) {
        shadowed = b;
    }

    public void setShadowAnchor(Point p) {
        Point location = getLocation();
        anchor = new Point(p.x - location.x, p.y - location.y);
    }

    public void setShadowAnchorLocation(Point p) {
        shadowLocation.x = p.x - anchor.x;
        shadowLocation.y = p.y - anchor.y;
    }

    public Rectangle getShadowBounds() {
        return new Rectangle(shadowLocation.x, shadowLocation.y,
                size.width, size.height);
    }

    public Point getShadowLocation() {
        return new Point(shadowLocation);
    }

    /**
     * Paints the node.
     */
    public void paint(Graphics g, boolean drawUnproper) {
        //g.setFont(new Font(FONTNAME, FONTSTYLE, FONTSIZE));
        FontMetrics m = g.getFontMetrics();

        Rectangle r = getBounds();
        if (r.intersects(g.getClipBounds())) {
            if (g instanceof SmartGraphics) {
                g.setColor(color);
                ((SmartGraphics) g).drawNode(r.x, r.y, r.width, r.height);
                g.setColor(Color.black);
                int w = getLatexGraphics().getLatexWidth(g, label);
                getLatexGraphics().drawLatexString(g, label, center.x - w/2,
                        r.y+r.height/2+ (m.getAscent()-m.getDescent())/2);
            } else {
                Color c = g.getColor();
                g.fillOval(r.x, r.y, r.width, r.height);
                g.setColor(color);
                if (marked) 
                    g.fillOval(r.x+3, r.y+3, r.width-6, r.height-6);
                else
                    g.fillOval(r.x+1, r.y+1, r.width-2, r.height-2);
                g.setColor(c);
                getLatexGraphics().drawLatexString(g, label, r.x+HORMARGIN,
                        r.y+r.height/2+ (m.getAscent()-m.getDescent())/2);
            }
        }

        //System.out.println("node ("+center.x+","+center.y+") "+marked+" "+
                //label +" "+ r.x +","+ r.y +" "+ r.width +"x"+ r.height);
    }

    public void paintShadow(Graphics g) {
        if (shadowed) {
            Rectangle s = getShadowBounds();
            if (s.intersects(g.getClipBounds())) {
                FontMetrics m = g.getFontMetrics();
                g.drawOval(s.x, s.y, s.width, s.height);
                getLatexGraphics().drawLatexString(g, label, s.x+HORMARGIN,
                        s.y+s.height/2+ (m.getAscent()-m.getDescent())/2);
            }
        }
    }
    
    
    /**
     * Writes this nodeview to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        w.writeNode(Integer.toString(parent.getNodeViews().indexOf(this)),
                getLabel(), getColor());
    }

    public String toString() {
        return "node ("+center.x+","+center.y+") "+ label;
    }
}

/* EOF */
