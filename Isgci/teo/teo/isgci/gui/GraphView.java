/*
 * The view of a connected graph.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.VertexFactory;
import teo.isgci.db.DataSet;
import teo.isgci.db.Algo;
import teo.isgci.layout.*;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.IntFunction;

/**
 * Display a graph in a GraphCanvas.
 */
public class GraphView<V,E> {
    /** The containing component (only GraphCanvas tested) */
    protected Component parent;
    protected SimpleDirectedGraph<V,E> graph;
    protected HierarchyLayout<V,E> layouter;
    protected LatexGraphics latexgraphics;
    protected boolean drawUnproper;
    /** NodeViews */
    protected List<NodeView<V,E> > views;
    /** 'Extra' views: edges, virtual nodes */
    protected List<View<V,E> > eViews;
    protected View markedView;
    /** Bounds of the drawing proper in graph coordinates */
    protected Rectangle bounds;
    /** Top left in canvas coordinates */
    protected Point location;


    /**
     * Create a new GraphView with nodes from DataSet.inclGraph.
     * @param parent the GraphCanvas that displays this view
     * @param nodes nodes the induce the graph to draw
     */
    public GraphView(Component parent,
            SimpleDirectedGraph<V,E> g,
            VertexFactory<V> vertexFactory,
            IntFunction<V> widthFunc) {
        this.parent = parent;
        bounds = null;
        location = new Point();
        markedView = null;

        graph = g;
        layouter = new HierarchyLayout<V,E>(g, vertexFactory, widthFunc);

        // Create the NodeViews; EdgeView are created when laying out.
        views = new ArrayList<NodeView<V,E> >();
        for (V v: g.vertexSet())
            views.add(new NodeView<V,E>(this, v));
    }


    /**
     * Return the graph this view shows.
     */
    public SimpleDirectedGraph<V,E> getGraph() {
        return graph;
    }


    /**
     * Set the LatexGraphics to be used for formatting latex strings.
     */
    public void setLatexGraphics(LatexGraphics g) {
        latexgraphics = g;
    }

    /**
     * Return the LatexGraphics to be used for formatting latex strings.
     */
    LatexGraphics getLatexGraphics() {
        return latexgraphics;
    }

    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
    }

    public boolean getDrawUnproper() {
        return drawUnproper;
    }

    /**
     * Return true if the given node is virtual.
     */
    public boolean isVirtual(V node) {
        return layouter.getGDI(node).virt;
    }


    /**
     * Return all nodeviews belonging to non-virtual nodes.
     */
    public List<NodeView<V,E> > getNodeViews() {
        return Collections.unmodifiableList(views);
    }


    /**
     * Return the view for the given node.
     */
    public NodeView<V,E> getView(V node) {
        for (NodeView v : views)
            if (v.getNode() == node)
                return v;
        for (View v : eViews)
            if (v instanceof NodeView  &&  ((NodeView) v).getNode() == node)
                return (NodeView<V,E>) v;
        return null;
    }


    public void layout() {
        layouter.layoutGraph();
        eViews = new ArrayList<View<V,E> >();

        // Create the EdgeViews
        for (E e : graph.edgeSet()) {
            if (!isVirtual(graph.getEdgeSource(e))) {
                EdgeView<V,E> v = new EdgeView<V,E>(this, e);
                if (parent instanceof GraphCanvas)
                    ((GraphCanvas) parent).setProperness(v);
                eViews.add(v);
            }
        }

        // Move the NodeViews to the proper position
        for (V n : graph.vertexSet()) {
            GraphDrawInfo gdi = layouter.getGDI(n);
            NodeView v = null;
            if (gdi.virt) {
                v = new VirtualNodeView<V,E>(this, n);
                eViews.add(v);
            } else
                v = getView(n);
            v.setCenter(gdi.xCoord, gdi.yCoord);
        }

        updateBounds();
    }


    public void paint(Graphics g) {
        //System.out.println("GraphView "+ getBounds()+" "+g.getClipBounds());
        if (views == null  ||  views.size() == 0  ||  bounds == null)
            return;

        Graphics gg = g.create();
        gg.translate(-bounds.x, -bounds.y);
        gg.setColor(Color.black);

        int i;
        for (View v : views)
            v.paint(gg, drawUnproper);

        if (eViews != null) {
            for (View v : eViews)
                v.paint(gg, drawUnproper);
        }

        gg.dispose();
    }


    /**
     * Recalculate the bounds of this view.
     */
    public void updateBounds() {
        if (views == null  ||  views.size() == 0) {
            bounds = new Rectangle();
            return;
        }
        
        bounds = new Rectangle(0, 0, -1, -1);
        for (View v : views)
            bounds = bounds.union(v.getBounds());
        for (View v : eViews)
            bounds = bounds.union(v.getBounds());

        //System.out.println("GraphView bounds: "+bounds);
    }


    public Dimension getPreferredSize() {
        return new Dimension(bounds.width, bounds.height);
    }


    /**
     * Return the bounds in canvas coordinates.
     */
    public Rectangle getBounds() {
        return new Rectangle(location.x, location.y,
                bounds.width, bounds.height);
    }

    
    /**
     * Set the top left corner in canvas coordinates.
     */
    public void setLocation(int x, int y) {
        location.x = x;
        location.y = y;
    }
   
    /**
     * Return the top left corner in canvas coordinates.
     */
    public Point getLocation() {
        return new Point(location);
    }

    /**
     * Return the view containing the given point (mouseclick).
     */
    public View getViewAt(Point p) {
        p.translate(bounds.x, bounds.y);
        //System.out.println(p);
        if (views == null  ||  views.size() == 0)
            return null;

        for (View v : views) {
            if (v.contains(p))
                return v;
        }

        for (View v : eViews) {
            if (v.contains(p))
                return v;
        }

        return null;
    }
    
    
    /**
     * Return the center of the given node or null if it isn't in this
     * GraphView.
     * @return the center of v or null
     */
    public Point getNodeCenter(NodeView v) {
        if (!views.contains(v))
            return null;
        Point p = v.getCenter();
        p.x += location.x - bounds.x;
        p.y += location.y - bounds.y;
        return p;
    }


    /**
     * Repaints the rectangle given in graph coordinates.
     */
    public void repaint(Rectangle r) {
        parent.repaint();
        /*r.x += location.x - bounds.x;
        r.y += location.y - bounds.y;
        canvas.repaint(r);*/
    }

    //------------------------- Marked items --------------------------------

    /**
     * Return true iff no objects are marked.
     */
    public boolean markIsEmpty() {
        return markedView == null;
    }

    /**
     * Unmark all objects and update the display.
     */
    public void unmarkAll() {
        if (markedView == null)
            return;

        markedView.setMark(false);
        markedView.setShadow(false);
        repaint(markedView.getBounds());
        markedView = null;
    }

    /**
     * Mark the given object and update the display.
     */
    public void mark(View v) {
        if (!views.contains(v) && !eViews.contains(v))
            return;

        v.setMark(true);
        markedView = v;
        repaint(v.getBounds());
    }

    /**
     * Make the given object the only selected one and update the display
     */
    public void markOnly(View v) {
        if (markedView == v)
            return;
        
        unmarkAll();
        mark(v);
    }

    /**
     * Gives all marked objects a shadow.
     */
    public void markSetShadow(boolean b) {
        if (markedView == null)
            return;
        
        markedView.setShadow(b);
    }

    /**
     * Return the boundingbox of the shadows in canvas coords or null.
     */
    public Rectangle getShadowBounds() {
        if (markedView == null)
            return null;
        Rectangle r = markedView.getShadowBounds();
        r.x += location.x - bounds.x;
        r.y += location.y - bounds.y;
        return r;
    }

    /**
     * Set the anchor for all marked object at the given point.
     */
    public void markSetShadowAnchor(Point p) {
        if (markedView == null)
            return;
        
        markedView.setShadowAnchor(p);
    }

    /**
     * Move the shadows of the marked objects to the given point
     */
    public void markSetShadowAnchorLocation(Point p) {
        if (markedView == null)
            return;
        
        markedView.setShadowAnchorLocation(p);
    }

    /**
     * Move the marked objects to the location of their shadow.
     */
    public void markMoveToShadow() {
        if (markedView == null)
            return;
        
        markedView.setLocation(markedView.getShadowLocation());
        markedView.setShadow(false);
        bounds = bounds.union(markedView.getBounds());
    }


    public void paintShadow(Graphics g) {
        if (markIsEmpty())
            return;

        Graphics gg = g.create();
        gg.translate(-bounds.x, -bounds.y);
        markedView.paintShadow(gg);
        gg.dispose();
    }

    /**
     * Writes this to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        int i;

        for (View v : views)
            v.write(w);
        for (View v : eViews)
            if (v instanceof EdgeView)
                v.write(w);
    }
}

/* EOF */
