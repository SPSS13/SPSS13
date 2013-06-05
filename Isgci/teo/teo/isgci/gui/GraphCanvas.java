/*
 * Displays graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphCanvas.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.VertexFactory;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.IntFunction;

/**
 * A canvas that can display a graph.
 */
public class GraphCanvas<V,E> extends JPanel
        implements MouseListener, MouseMotionListener {

    public static final int CANVASWIDTH = 400,          // Initial canvas size
                            CANVASHEIGHT = 300;

    protected Component parent;
    protected List<GraphView<V,E> > graphs;
    protected Rectangle bounds;
    protected View markedView;
    protected boolean dragInProcess;
    protected boolean drawUnproper;
    protected LatexGraphics latexgraphics;
    protected VertexFactory<V> vertexFactory;
    protected IntFunction<V> widthFunc;
    
    /** Margins around drawing */
    protected static final int LEFTMARGIN = 20;
    protected static final int RIGHTMARGIN = 20;
    protected static final int TOPMARGIN = 20;
    protected static final int BOTTOMMARGIN = 20;
    protected static final int INTMARGIN = 40;          // Internal


    public GraphCanvas(Component parent,
            LatexGraphics latexgraphics,
            VertexFactory<V> vertexFactory,
            IntFunction<V> widthFunc) {
        super();
        this.parent = parent;
        this.latexgraphics = latexgraphics;
        this.vertexFactory = vertexFactory;
        this.widthFunc = widthFunc;
        graphs = new ArrayList<GraphView<V,E> >();
        bounds = new Rectangle();
        markedView = null;
        dragInProcess = false;
        drawUnproper = true;
        setBackground(Color.white);
        setForeground(Color.black);
        setOpaque(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }


    /**
     * Remove all graphs from the canvas.
     */
    public void clearGraphs() {
        graphs.clear();
    }


    /**
     * Add the given graph to this canvas.
     */
    protected GraphView<V,E> addGraph(SimpleDirectedGraph<V,E> g) {
        GraphView<V,E> gv = new GraphView<V,E>(this, g,
                vertexFactory, widthFunc);
        gv.setLatexGraphics(latexgraphics);
        gv.setDrawUnproper(drawUnproper);
        graphs.add(gv);
        return gv;
    }
    
    
    /**
     * Return the NodeView for the given node.
     */
    public NodeView<V,E> getView(V node) {
        for (GraphView<V,E> gv : graphs) {
            NodeView<V,E> view = gv.getView(node);
            if (view != null)
                return view;
        }
        return null;
    }


    /**
     * Clear the canvas and draw the given graphs.
     */
    public void drawGraphs(Collection<SimpleDirectedGraph<V,E> > graphs) {
        try {
            /*long t, s = System.currentTimeMillis();*/
            clearGraphs();
            /*s = t;
            t = System.currentTimeMillis();
            System.out.println("split "+(t-s));*/
            for (SimpleDirectedGraph<V,E> g : graphs)
                addGraph(g);
            /*s = t;
            t = System.currentTimeMillis();
            System.out.println("add "+(t-s));*/
            calcLayout();
            /*s = t;
            t = System.currentTimeMillis();
            System.out.println("layout "+(t-s));*/
            repaint();
        } catch (Error e) {
            //e.printStackTrace();
            if (e instanceof OutOfMemoryError ||
                    e instanceof StackOverflowError) {
                clearGraphs();
                MessageDialog.error(parent,
                        "Not enough memory to draw this many graph classes");
            } else
                throw(e);
        }
    }


    /**
     * Layout the graphs vertically.
     */
    public void calcLayout() {
        int x = LEFTMARGIN, y = TOPMARGIN;
        for (GraphView<V,E> gv : graphs) {
            gv.layout();
            gv.setLocation(x, y);
            y += gv.getPreferredSize().height + INTMARGIN;
        }
        updateBounds();
    }


    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
        for (GraphView<V,E> gv : graphs)
            gv.setDrawUnproper(b);
        repaint();
    }

    public boolean getDrawUnproper() {
        return drawUnproper;
    }

    /**
     * Bit of a hack to get all ISGCI stuff in one place:
     * Set the appropriate properness of the given edgeview.
     */
    protected void setProperness(EdgeView<V,E> view) {
    }


    public LatexGraphics getLatexGraphics() {
        return latexgraphics;
    }

    public void setWidthFunc(IntFunction<V> widthFunc) {
        this.widthFunc = widthFunc;
    }


    /**
     * Write this to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        for (GraphView gv : graphs)
            gv.write(w);
    }


    /**
     * Update the bounds of the canvas.
     */
    public void updateBounds() {
        if (graphs.isEmpty()) {
            bounds.x = bounds.y = bounds.width = bounds.height = 0;
            return;
        }

        bounds = new Rectangle(0, 0, -1, -1);
        for (GraphView gv : graphs) {
            gv.updateBounds();
            bounds = bounds.union(gv.getBounds());
        }
        invalidate();
        getParent().validate();
    }


    /**
     * Return the preferred size. For sensible results, call layout() first.
     */
    public Dimension getPreferredSize() {
        Dimension d =  new Dimension(bounds.width + LEFTMARGIN + RIGHTMARGIN,
                bounds.height + TOPMARGIN + BOTTOMMARGIN);
        if (d.width < CANVASWIDTH)
            d.width = CANVASWIDTH;
        if (d.height < CANVASHEIGHT)
            d.height = CANVASHEIGHT;

        return d;
    }

    
    /**
     * Return the view containing the given point (mouseclick).
     */
    public View getViewAt(Point p) {
        for (GraphView<V,E> gv : graphs) {
            if (gv.getBounds().contains(p)) {
                Point t = gv.getLocation();
                t.x = p.x - t.x;
                t.y = p.y - t.y;
                View v = gv.getViewAt(t);
                if (v != null)
                    return v;
            }
        }
        return null;
    }
    
    

    //--------------------------- Paint stuff ----------------------------


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*System.err.println("paint: bounds() "+getBounds());
        System.err.println("paint: bounds' "+bounds);
        System.err.println("paint: clip "+g.getClipBounds());
        parent.printPort();
        System.err.println("");*/

        Rectangle clip = g.getClipBounds();
        /*g.setColor(Color.RED);
        g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
        g.setColor(Color.BLACK);*/

        dopaint(g);
        paintShadow(g);
    }


    /**
     * Paint this entire drawing to g.
     */
    public void forcePaint(Graphics g) {
        g.setClip(bounds);
        //g.setColor(getForeground());          // Done by ...View.paint
        g.setFont(latexgraphics.getFont());
        dopaint(g);
    }


    /**
     * Do the actual painting in a prepared graphics context.
     */
    protected void dopaint(Graphics g) {
        for (GraphView<V,E> gv : graphs) {
            Graphics gg = g.create();
            /*System.err.print("dopaint translate: ");
            System.err.print(i);
            System.err.println(graphs[i].getLocation());*/
            gg.translate(gv.getLocation().x, gv.getLocation().y);
            gv.paint(gg);
            gg.dispose();
        }
    }


    /**
     * Paint the shadows.
     */
    public void paintShadow(Graphics g) {
        g.setColor(teo.isgci.gui.SColor.brighter(Color.black));
        for (GraphView<V,E> gv : graphs) {
            Graphics gg = g.create();
            gg.translate(gv.getLocation().x, gv.getLocation().y);
            gv.paintShadow(gg);
            gg.dispose();
        }
    }



    //------------------------- Marked items --------------------------------


    /**
     * Return true iff no objects are marked.
     */
    public boolean markIsEmpty() {
        for (GraphView<V,E> gv : graphs)
            if (!gv.markIsEmpty())
                return false;
        return true;
    }


    /**
     * Unmark all objects and update the display.
     */
    public void unmarkAll() {
        for (GraphView<V,E> gv : graphs)
            gv.unmarkAll();
    }


    /**
     * Mark the given object and update the display.
     */
    public void mark(View v) {
        for (GraphView<V,E> gv : graphs)
            gv.mark(v);
    }


    /**
     * Make the given object the only selected one and update the display
     */
    public void markOnly(View v) {
        unmarkAll();
        mark(v);
    }


    /**
     * Gives all marked objects a shadow.
     */
    public void markSetShadow(boolean b) {
        for (GraphView<V,E> gv : graphs)
            gv.markSetShadow(b);
    }


    /**
     * Set the anchor for all marked object at the given point.
     */
    public void markSetShadowAnchor(Point p) {
        for (GraphView<V,E> gv : graphs) {
            Point t = gv.getLocation();
            t.x = p.x - t.x;
            t.y = p.y - t.y;
            gv.markSetShadowAnchor(t);
        }
    }


    /**
     * Move the shadows of the marked objects to the given point
     */
    public void markSetShadowAnchorLocation(Point p) {
        for (GraphView<V,E> gv : graphs) {
            Point t = gv.getLocation();
            t.x = p.x - t.x;
            t.y = p.y - t.y;
            gv.markSetShadowAnchorLocation(t);
        }
    }


    /**
     * Move the marked objects to the location of their shadow.
     */
    public void markMoveToShadow() {
        for (GraphView<V,E> gv : graphs)
            gv.markMoveToShadow();
    }


    public Rectangle getShadowBounds() {
        if (graphs.isEmpty())
            return null;

        Rectangle bounds = new Rectangle(0, 0, -1, -1);
        for (GraphView<V,E> gv : graphs)
            bounds = bounds.union(gv.getBounds());

        return bounds;
    }

    //----------------------- MouseListener stuff --------------------------

    /**
     * Overload this to react to popups. Return true iff the event is handled.
     */
    protected boolean mousePopup(MouseEvent event) {
        return false;
    }

    public void mouseClicked(MouseEvent event) {
        mousePopup(event);
    }

    public void mousePressed(MouseEvent event) {
        if (mousePopup(event))
            return;

        View v = getViewAt(event.getPoint());
        if (v != null  &&  v instanceof NodeView) {
            markOnly(v);
            markSetShadowAnchor(event.getPoint());
        } else {
            unmarkAll();
        }
    }

    public void mouseReleased(MouseEvent event) {
        if (mousePopup(event))
            return;
        if (dragInProcess) {
            dragInProcess = false;
            markMoveToShadow();
            updateBounds();
            repaint();
        }
    }

    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    //-------------------- MouseMotionListener stuff ------------------------

    public void mouseDragged(MouseEvent event) {
        if (markIsEmpty())
            return;

        if (!dragInProcess) {
            markSetShadow(true);
            dragInProcess = true;
        }
        markSetShadowAnchorLocation(event.getPoint());
        super.repaint();
    }

    public void mouseMoved(MouseEvent e) {}

}

/* EOF */
