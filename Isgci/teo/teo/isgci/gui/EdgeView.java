/*
 * The view of an edge.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/EdgeView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
//import teo.isgci.gc.GraphClass;
//import teo.isgci.db.Algo;
//import teo.isgci.db.DataSet;
import teo.isgci.xml.GraphMLWriter;
import org.xml.sax.SAXException;

public class EdgeView<V,E> implements View {
    protected GraphView<V,E> parent;
    protected V from, to;
    protected boolean isProper;
    protected List<E> parts;
    protected boolean marked;

    public static final int ARROWLEN = 8;
    public static final double ARROWANGLE = 0.5;


    /**
     * Create a new edge view that can span virtual nodes. e.from must be
     * non-virtual. If e.to is virtual, it will be followed until a non-virtual
     * node is reached.
     */
    public EdgeView(GraphView<V,E> parent, E e) {
        this.parent = parent;
        from = parent.getGraph().getEdgeSource(e);
        parts = new ArrayList<E>();
        while (true) {
            parts.add(e);
            if (!parent.isVirtual(parent.getGraph().getEdgeTarget(e)))
                break;
            e = parent.getGraph().outgoingEdgesOf(
                    parent.getGraph().getEdgeTarget(e) ).iterator().next();
        }

        to = parent.getGraph().getEdgeTarget(e);
        marked = false;
        isProper = false;
    }


    public void setMark(boolean b) {
        marked = b;
    }

    public void setProper(boolean b) {
        isProper = b;
    }

    public V getFrom() {
        return from;
    }
    public V getTo() {
        return to;
    }

    /**
     * Return the upper left corner.
     */
    public Point getLocation() {
        Rectangle r = getBounds();
        return new Point(r.x, r.y);
    }

    public void setLocation(Point p) {}


    /**
     * Return the rectangle fitting around this edge.
     */
    public Rectangle getBounds() {
        Rectangle bounds = new Rectangle(0, 0, -1, -1);
        for (E e : parts)
            bounds = bounds.union(getBounds(e));
        
        return bounds;
    }


    /*
     * Return the bounding rectangle for a straight line edge.
     */
    protected Rectangle getBounds(E edge) {
        Point from = parent.getView(parent.getGraph().getEdgeSource(edge)).
                getCenter();
        Point to = parent.getView(parent.getGraph().getEdgeTarget(edge)).
                getCenter();
        Rectangle bounds = new Rectangle();

        if (from.x < to.x) {
            bounds.x = from.x;
            bounds.width = to.x - from.x;
        } else {
            bounds.x = to.x;
            bounds.width = from.x - to.x;
            if (bounds.width == 0)
                bounds.width = 1;
        }

        if (from.y < to.y) {
            bounds.y = from.y;
            bounds.height = to.y - from.y;
        } else {
            bounds.y = to.y;
            bounds.height = from.y - to.y;
            if (bounds.height == 0)
                bounds.height = 1;
        }

        return bounds;
    }


    /**
     * Does this edge contain p? This actually tests whether p is very close to
     * the line representing the edge.
     */
    public boolean contains(Point p) {
        for (E e : parts)
            if (edgeContains(e, p))
                return true;
        return false;
    }


    /**
     * Does this edge contain p? This actually tests whether p is very close to
     * the line representing the edge.
     */
    protected boolean edgeContains(E edge, Point p) {
        NodeView nodeFrom = parent.getView(
                parent.getGraph().getEdgeSource(edge));
        NodeView nodeTo = parent.getView(
                parent.getGraph().getEdgeTarget(edge));
        Point from = nodeFrom.getBorder(nodeTo.getCenter());
        Point to = nodeTo.getBorder(nodeFrom.getCenter());

        double ax, ay, bx, by, qx, qy, lambda;
        
        ax = to.x - from.x;                     // Vector from -> to
        ay = to.y - from.y;
        bx = p.x - from.x;                      // Vector from -> p
        by = p.y - from.y;
        lambda = (ax*bx + ay*by) / (ax*ax + ay*ay);     // Perpendicular

        if (lambda <= 0) {                      // Outside line, close to from
            qx = from.x;
            qy = from.y;
        } else if (lambda < 1) {                // Inside
            bx = lambda*ax;
            by = lambda*ay;
            qx = from.x + bx;
            qy = from.y + by;
        } else {                                // Outside, close to to
            qx = to.x;
            qy = to.y;
        }

        qx -= p.x;
        qy -= p.y;
        return (qx*qx + qy*qy) < 8.0;
    }


    /**
     * Paint this thing
     */
    public void paint(Graphics g, boolean drawUnproper) {
        if (g instanceof SmartGraphics) {
            if (getBounds().intersects(g.getClipBounds())) {
                Vector v = new Vector();
                for (E edge : parts) {
                    NodeView nodeFrom = parent.getView(
                            parent.getGraph().getEdgeSource(edge));
                    NodeView nodeTo = parent.getView(
                            parent.getGraph().getEdgeTarget(edge));
                    Point from = nodeFrom.getBorder(nodeTo.getCenter());
                    Point to = nodeTo.getBorder(nodeFrom.getCenter());

                    if (v.isEmpty())
                        v.addElement(from);
                    v.addElement(to);
                }
                ((SmartGraphics) g).drawArrow(v, !isProper && drawUnproper);
            }
        } else {
            for (E e : parts)
                edgePaint(g, e, drawUnproper);
        }
    }

    protected void edgePaint(Graphics g, E edge, boolean drawUnproper) {
        if (getBounds(edge).intersects(g.getClipBounds())) {
            NodeView nodeFrom = parent.getView(
                    parent.getGraph().getEdgeSource(edge));
            NodeView nodeTo = parent.getView(
                    parent.getGraph().getEdgeTarget(edge));
            Point from = nodeFrom.getBorder(nodeTo.getCenter());
            Point to = nodeTo.getBorder(nodeFrom.getCenter());
            
            g.drawLine(from.x, from.y, to.x, to.y);
            //System.out.println(from.x +","+ from.y +" -> "+ to.x +","+ to.y);
            if (!(nodeTo instanceof VirtualNodeView))
                drawArrowHead(g, from, to);
            if (drawUnproper  &&  !isProper  &&
                        !(nodeFrom instanceof VirtualNodeView)) {
                Graphics gg = g.create();
                gg.setColor(Color.lightGray);
                drawArrowHead(gg, to, from);
                gg.dispose();
            }

        }
        //System.out.println("view "+from+"->"+to);
    }

    public void setShadow(boolean b) {}
    public void setShadowAnchor(Point p) {}
    public void setShadowAnchorLocation(Point p) {}
    public Rectangle getShadowBounds() {return null;}
    public Point getShadowLocation() {return null;}
    public void paintShadow(Graphics g) {}

    /**
     * Draws an arrowhead at to, coming from from.
     */
    public static void drawArrowHead(Graphics g, Point from, Point to) {
        double theta = Math.atan2((double) from.y-to.y, (double) -from.x+to.x);
        int x[] = new int[3];
        int y[] = new int[3];
        
        x[0] = to.x;
        y[0] = to.y;
        x[1] = (int) Math.round(to.x - ARROWLEN * Math.cos(theta-ARROWANGLE));
        y[1] = (int) Math.round(to.y + ARROWLEN * Math.sin(theta-ARROWANGLE));
        x[2] = (int) Math.round(to.x - ARROWLEN * Math.cos(theta+ARROWANGLE));
        y[2] = (int) Math.round(to.y + ARROWLEN * Math.sin(theta+ARROWANGLE));
        /*System.err.print("("+x[0] +","+ y[0] +")");
        System.err.print("("+x[1] +","+ y[1] +")");
        System.err.println("("+x[2] +","+ y[2] +")");*/
        g.fillPolygon(x, y, 3);
    }

    /**
     * Writes this to w.
     */
    public void write(GraphMLWriter w)
            throws SAXException {
        w.writeEdge(
            Integer.toString(
                parent.getNodeViews().indexOf(parent.getView(this.from))),
            Integer.toString(
                parent.getNodeViews().indexOf(parent.getView(this.to))),
                isProper);
    }
}

/* EOF */
