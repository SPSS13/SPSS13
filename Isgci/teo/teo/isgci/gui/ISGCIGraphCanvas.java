/*
 * Displays ISGCI graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIGraphCanvas.java,v 2.1 2012/10/28 16:00:51 ux Exp $
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
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import teo.isgci.db.*;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.IntFunction;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.ISGCIVertexFactory;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Inclusion;

/**
 * A canvas that can display an inclusion graph.
 */
public class ISGCIGraphCanvas extends
        GraphCanvas<Set<GraphClass>, DefaultEdge> {
    protected NodePopup nodePopup;
    protected EdgePopup edgePopup;
    protected Problem problem;
    protected Algo.NamePref namingPref;

    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;


    public ISGCIGraphCanvas(ISGCIMainFrame parent) {
        super(parent, parent.latex, new ISGCIVertexFactory(), null);
        problem = null;
        namingPref = Algo.NamePref.BASIC;
        setWidthFunc(new NodeWidthFunc());
        nodePopup = new NodePopup(parent);
        edgePopup = new EdgePopup(parent);
        add(nodePopup);
        add(edgePopup);
    }


    /**
     * Add the given graph to this canvas.
     */
    protected GraphView<Set<GraphClass>,DefaultEdge> addGraph(
            SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> g) {
        GraphView<Set<GraphClass>,DefaultEdge> gv = super.addGraph(g);
        for (NodeView<Set<GraphClass>,DefaultEdge> nv : gv.getNodeViews()) {
            nv.setColor(complexityColor(nv.getNode()));
            nv.setNameAndLabel(Algo.getName(nv.getNode(), namingPref)); 
        }
        return gv;
    }
    

    /**
     * Create a hierarchy subgraph of the given classes and draw it.
     */
    public void drawHierarchy(Collection<GraphClass> nodes) {
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph =
            Algo.createHierarchySubgraph(nodes);
        //System.err.println(graph);
        List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
                GAlg.split(graph, DefaultEdge.class);
        //System.err.println(list);
        drawGraphs(list);
    }
    
    
    /**
     * Returns all classes on the canvas (unsorted).
     */
    public List<GraphClass> getClasses() {
        List<GraphClass> result = new ArrayList<GraphClass>();
        for (GraphView<Set<GraphClass>,DefaultEdge> gv : graphs) {
            for (NodeView<Set<GraphClass>,DefaultEdge> nv : gv.getNodeViews())
                for (GraphClass gc : nv.getNode())
                    result.add(gc);
        }
        return result;
    }
    
    
    /**
     * Returns all names of all nodes.
     */
    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (GraphView<Set<GraphClass>,DefaultEdge> gv : graphs) {
            for (NodeView<Set<GraphClass>,DefaultEdge> nv : gv.getNodeViews())
                for (GraphClass gc : nv.getNode())
                    result.add(gc.toString());
        }
        return result;
    }


    /**
     * Set all nodes to their prefered names.
     */
    public void setPreferedNames() {
        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs)
            for (NodeView<Set<GraphClass>, DefaultEdge> v : gv.getNodeViews())
               v.setNameAndLabel(Algo.getName(v.getNode(), namingPref)); 
    }


    /**
     * Find the NodeView for the given graph class or null if not found
     */
    public NodeView<Set<GraphClass>, DefaultEdge> findNode(GraphClass gc) {
        for (GraphView<Set<GraphClass>,DefaultEdge> gv : graphs) {
            for (NodeView<Set<GraphClass>, DefaultEdge> v : gv.getNodeViews())
                if (v.getNode().contains(gc))
                    return v;
        }
        return null;
    }
     

    /**
     * Bit of a hack to get all ISGCI stuff in one place:
     * Set the appropriate properness of the given edgeview.
     */
    protected void setProperness(EdgeView<Set<GraphClass>,DefaultEdge> view) {
        List<Inclusion> path = GAlg.getPath(DataSet.inclGraph,
                view.getFrom().iterator().next(),
                view.getTo().iterator().next());
        view.setProper(Algo.isPathProper(path)  ||
                Algo.isPathProper(Algo.makePathProper(path)));
    }


    /**
     * Set coloring for p and repaint.
     */
    public void setProblem(Problem p) {
        if (problem != p) {
            problem = p;
            setComplexityColors();
            repaint();
        }
    }


    public Problem getProblem() {
        return problem;
    }


    /**
     * Return the color for node considering its complexity for the active
     * problem.
     */
    protected Color complexityColor(Set<GraphClass> node) {
        if (problem == null)
            return COLOR_UNKNOWN;
        Complexity complexity= problem.getComplexity(node.iterator().next());
        if (/*complexity == null  ||*/  complexity.isUnknown())
            return COLOR_UNKNOWN;
        if (complexity.betterOrEqual(Complexity.LINEAR))
            return COLOR_LIN;
        if (complexity.betterOrEqual(Complexity.P))
            return COLOR_P;
        if (complexity.equals(Complexity.GIC))
            return COLOR_INTERMEDIATE;
        if (complexity.likelyNotP())
            return COLOR_NPC;
        return COLOR_UNKNOWN;
    }


    /**
     * Set all nodes to the proper complexity color.
     */
    public void setComplexityColors() {
        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs)
            for (NodeView<Set<GraphClass>, DefaultEdge> v : gv.getNodeViews())
               v.setColor(complexityColor(v.getNode()));
    }


    public void setNamingPref(Algo.NamePref pref) {
        namingPref = pref;
        setPreferedNames();
        updateBounds();
        repaint();
    }

    public Algo.NamePref getNamingPref() {
        return namingPref;
    }



    /**
     * Center the canvas on the given NodeView.
     */
    public void centerNode(NodeView<Set<GraphClass>, DefaultEdge> v) {
        Point p = null;

        for (GraphView<Set<GraphClass>,DefaultEdge> gv : graphs) {
            if ((p = gv.getNodeCenter(v)) != null) {
                ((ISGCIMainFrame) parent).centerCanvas(p);
                return;
            }
        }
    }


    //----------------------- MouseListener stuff --------------------------

    protected boolean mousePopup(MouseEvent event) {
        if (!event.isPopupTrigger())
            return false;

        event.consume();
        
        View v = getViewAt(event.getPoint());
        if (v == null)
            return true;
        if (v instanceof NodeView) {
            nodePopup.setNode((NodeView) v);
            nodePopup.show(this, event.getX(), event.getY());
        }
        if (v instanceof EdgeView) {
            edgePopup.setEdge((EdgeView) v);
            edgePopup.show(this, event.getX(), event.getY());
        }
        return true;
    }


    //----------------------- Width calculator --------------------------
    class NodeWidthFunc implements IntFunction<Set<GraphClass> > {
        /** Return the width of node */
        public int execute(Set<GraphClass> node) {
            NodeView<Set<GraphClass>, DefaultEdge> view = getView(node);
            view.updateSize();
            return view.getSize().width;
        }
    }
}

/* EOF */
