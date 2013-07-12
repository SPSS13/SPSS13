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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.BFSWalker;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.RevBFSWalker;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * A canvas that can display an inclusion graph.
 */
public class ISGCIGraphCanvas extends CustomGraphComponent implements
        MouseListener, MouseMotionListener, MouseWheelListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8803653758744069010L;
    protected NodePopup nodePopup;
    protected EdgePopup edgePopup;
    protected Problem problem;
    protected Algo.NamePref namingPref;
    private ISGCIMainFrame parent;
    private final CustomGraph graph;
    private mxCell lastSelected;
    private boolean neighbours = false;
    private boolean animationActivated = false;
    private Object[] highlitedEdges;
    private Object[] highlitedCells;

    public static final int CANVASWIDTH = 400, // Initial canvas size
            CANVASHEIGHT = 300;

    protected Rectangle bounds;
    protected boolean dragInProcess;
    protected boolean drawUnproper;
    protected LatexGraphics latexgraphics;

    /** Margins around drawing */
    protected static final int LEFTMARGIN = 20;
    protected static final int TOPMARGIN = 20;

    /** Colors for different complexities */
    public static final Color COLOR_LIN = new Color(80, 235, 65);
    public static final Color COLOR_P = new Color(0, 155, 0);
    public static final Color COLOR_NPC = new Color(255, 20, 20);
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;
    private final HashMap<Set<GraphClass>, Object> map;
    private mxHierarchicalLayout hierachicalLayout;
    private Point start;

    private final String EDGE_COLOR = "black";
    private final String CELL_COLOR = "black";
    // light blue
    private final String ALTERNATE_EDGE_COLOR = "#3d86b8";
    // light blue
    private final String ALTERNATE_CELL_COLOR = "#3d86b8";

    private final String vertexStyle = "shape=rectangle;perimeter=rectanglePerimeter;rounded=true;fontColor="
            + CELL_COLOR
            + ";strokeColor="
            + CELL_COLOR
            + ";spacingLeft=4;spacingRight=4;spacingTop=2;spacingBottom=2";
    private final String edgeStyle = "strokeColor=" + EDGE_COLOR
            + ";rounded=true" + ";selectable=false";

    // Implementation of custom cursors for panning events and clicking on nodes
            Toolkit.getDefaultToolkit().createImage(
                            this.getClass().getResource("/images/grab.png")),
                    new Point(0, 0), "grab");

    private Cursor grabbingcursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(
                    Toolkit.getDefaultToolkit().createImage(
                            this.getClass()
                                    .getResource("/images/grabbing.png")),
                    new Point(0, 0), "grabbing");

    private Cursor pointcursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(
                    Toolkit.getDefaultToolkit().createImage(
                            this.getClass().getResource("/images/point.png")),
                    new Point(0, 0), "point");

    public ISGCIGraphCanvas(ISGCIMainFrame parent, CustomGraph graph) {
        super(graph);
        this.parent = parent;
        this.latexgraphics = ISGCIMainFrame.latex;
        this.parent = parent;
        drawUnproper = true;
        problem = null;
        this.map = new HashMap<Set<GraphClass>, Object>();
        this.graph = graph;
        namingPref = Algo.NamePref.BASIC;
        // setWidthFunc(new NodeWidthFunc());
        nodePopup = new NodePopup(parent, graph, map);
        edgePopup = new EdgePopup(parent);
        add(nodePopup);
        add(edgePopup);
        hierachicalLayout = new mxHierarchicalLayout(graph);
        hierachicalLayout.setParentBorder(0);
        hierachicalLayout.setResizeParent(true);
        hierachicalLayout.setInterHierarchySpacing(100);
        this.graphControl = ((CustomGraphComponent)parent.drawingPane)
                .getGraphControl();
    }

    /**
     * Create a new hierarchy subgraph of the given classes and draw it.
     * 
     * @author leo
     * @date 12.06 9:30; 26.06 00:10
     * @annotation rewritten so that it uses GraphClassSet to print the label
     *             automatically and still be able to retrieve the set
     * @annotation2 altered to use makeGraph
     */
    public void drawHierarchy(Collection<GraphClass> nodes) {

        map.clear();
        graph.getModel().beginUpdate();
        ((mxGraphModel)graph.getModel()).clear();
        graph.getModel().endUpdate();
        // reset selection to prevent side effects
        lastSelected = null;
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);

        makeGraph(edgegraph);
    }

    /**
     * Create a hierarchy graph of the given classes and draw it. Takes cate of
     * using the map correctly, adds all nodes given to the graph and the map,
     * if nor already present
     * 
     * @author leo
     * @date 26.06 00:10
     * @annotation written for more modularity
     */
    private void makeGraph(
            SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph) {
        graph.getModel().beginUpdate();
        Object defaultParent = graph.getDefaultParent();
        try {
            graph.setCellsResizable(true);
            // Add vertices
            for (Set<GraphClass> gc : edgegraph.vertexSet()) {
                mxCell cell = (mxCell)map.get(gc);

                // check if node is already present, or invisible
                if (cell == null) {
                    // add the node
                    GraphClassSet graphClasses = new GraphClassSet(gc, this);
                    int x = (int)((lastSelected != null) ? lastSelected
                            .getGeometry().getCenterX() : 30);
                    int y = (int)((lastSelected != null) ? lastSelected
                            .getGeometry().getCenterY() : 30);
                    Object vertex = graph.insertVertex(defaultParent,
                            gc.toString(), graphClasses, x, y, 30, 80,
                            vertexStyle);
                    // add the node to the map
                    map.put(gc, vertex);
                    // update the size of the node to match the text
                    graph.cellSizeUpdated(vertex, false);
                    ((mxCell)vertex).setConnectable(false);
                } else {
                    // check if node is invisible
                    if (!cell.isVisible()) {
                        graph.toggleCells(true,
                                graph.addAllEdges(new Object[] { cell }));
                    }
                }
            }
            // add edges
            for (DefaultEdge edge : edgegraph.edgeSet()) {
                Set<GraphClass> source = edgegraph.getEdgeSource(edge);
                Set<GraphClass> target = edgegraph.getEdgeTarget(edge);
                // check if the edge is already present
                if (graph.getEdgesBetween(map.get(source), map.get(target)).length == 0) {
                    graph.insertEdge(defaultParent, null, null,
                            map.get(source), map.get(target), edgeStyle);
                }
            }
            parent.drawingPane.getComponent(0).validate();
            // make Layout, if no animation
            if (!neighbours || !animationActivated) {
                hierachicalLayout.execute(graph.getDefaultParent());
                // timer.schedule(new Task(), 0);
            }
            // make edges look nice
            if (drawUnproper) {
                // set properness for all possible cells
                setProperness(graph.getChildCells(graph.getDefaultParent(),
                        false, true));
            }

            // make nodes colorful
            if (problem != null) {
                setComplexityColors();

            } else {
                // make all cells white
                graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                // the color white understandable for mxGraph
                        mxHtmlColor.getHexColorString(COLOR_UNKNOWN),
                        // get all cells
                        graph.getChildCells(defaultParent));
            }

            graph.refresh();
        } finally {
            try {
                // animation?
                if (neighbours && animationActivated) {
                    neighbours = false;
                    animateGraph();
                } else {
                    graph.refresh();
                    parent.drawingPane.getComponent(0).validate();
                    centerNode(getSelectedCell());
                }
                graph.setCellsResizable(false);

            } finally {
                graph.getModel().endUpdate();
            }
        }
        // translate the Graph a bit
        this.getGraph().getView()
                .setTranslate(new mxPoint(LEFTMARGIN, TOPMARGIN));
    }

    /**
     * Add direct sub and superclasses of the selected node to the Graph and
     * draw them
     * 
     * @author leo
     * @date 26.06 3:00
     * @annotation
     * 
     * 
     */
    public void drawNeighbours(mxCell cell) {
        neighbours = true;
        Set<GraphClass> selected = ((GraphClassSet)cell.getValue()).getSet();
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> result = new SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>(
                DefaultEdge.class);
        Collection<GraphClass> nodes = new HashSet<GraphClass>();
        // get all possible inclusions
        for (GraphClass gc : selected) {
            if (DataSet.inclGraph.edgesOf(gc) != null) {
                for (Inclusion inc : DataSet.inclGraph.edgesOf(gc)) {
                    nodes.add(inc.getSuper());
                    nodes.add(inc.getSub());
                }
            }
        }
        // reduce the graph
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);
        // add selected node to the result graph
        result.addVertex(selected);
        // move all edges directly connected to the selected node to a result
        // graph
        if (edgegraph.outgoingEdgesOf(selected) != null) {
            for (DefaultEdge edge : edgegraph.outgoingEdgesOf(selected)) {
                result.addVertex(edgegraph.getEdgeTarget(edge));
                result.addEdge(selected, edgegraph.getEdgeTarget(edge));
            }
        }
        if (edgegraph.incomingEdgesOf(selected) != null) {
            for (DefaultEdge edge : edgegraph.incomingEdgesOf(selected)) {
                result.addVertex(edgegraph.getEdgeSource(edge));
                result.addEdge(edgegraph.getEdgeSource(edge), selected);

            }
        }
        result = findMissingEdges(result);
        // add the new subgraph to the mxGraph
        makeGraph(result);
        parent.drawingPane.getComponent(0).validate();
        centerNode(getSelectedCell());
    }

    /**
     * this method searches the superclasses of the selected node and adds them
     * to the Graph
     */
    public void addSuperclasses(mxCell cell) {
        neighbours = true;
        Set<GraphClass> selected = ((GraphClassSet)cell.getValue()).getSet();
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> result = new SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>(
                DefaultEdge.class);
        Collection<GraphClass> nodes = new HashSet<GraphClass>();
        // get all possible inclusions
        for (GraphClass gc : selected) {
            if (!DataSet.inclGraph.edgesOf(gc).isEmpty()) {
                for (Inclusion inc : DataSet.inclGraph.edgesOf(gc)) {
                    nodes.add(inc.getSuper());
                    nodes.add(inc.getSub());
                }
            }
        }
        // reduce the graph
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);
        // add selected node to the result graph
        result.addVertex(selected);
        // move all vertexes with edges directly connected to the selected node
        // to a result graph
        if (edgegraph.incomingEdgesOf(selected) != null) {
            for (DefaultEdge edge : edgegraph.incomingEdgesOf(selected)) {
                result.addVertex(edgegraph.getEdgeSource(edge));
            }
        }
        result = findMissingEdges(result);
        // add the new subgraph to the mxGraph
        makeGraph(result);
        // center the graph
        centerNode(getSelectedCell());
    }

    /**
     * this method searches the subclasses of the selected node and adds them to
     * the Graph
     */
    public void addSubclasses(mxCell cell) {
        neighbours = true;
        Set<GraphClass> selected = ((GraphClassSet)cell.getValue()).getSet();
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> result = new SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>(
                DefaultEdge.class);
        final Collection<GraphClass> nodes = new HashSet<GraphClass>();
        // get all possible inclusions
        for (GraphClass gc : selected) {
            if (!DataSet.inclGraph.edgesOf(gc).isEmpty()) {
                for (Inclusion inc : DataSet.inclGraph.edgesOf(gc)) {
                    nodes.add(inc.getSuper());
                    nodes.add(inc.getSub());
                }
            }
        }
        // reduce the graph,
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);
        // add selected node to the result graph
        result.addVertex(selected);
        // move all vertexes with edges directly connected to the selected node
        // to a result graph
        if (edgegraph.outgoingEdgesOf(selected) != null) {
            for (DefaultEdge edge : edgegraph.outgoingEdgesOf(selected)) {
                result.addVertex(edgegraph.getEdgeTarget(edge));
            }
        }
        result = findMissingEdges(result);
        // add the new subgraph to the mxGraph
        makeGraph(result);
        centerNode(getSelectedCell());
    }

    /**
     * this method adds all inclusions to the present graph
     * 
     * @param result
     *            , the graphclasses which should be added to the Graph
     * @author leo
     * @date 27.06.2013
     * @annotation works
     */
    private SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> findMissingEdges(
            SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> result) {
        // add all graphclasses that should be present in the new graph
        Collection<GraphClass> allNeededClasses = new HashSet<GraphClass>();
        for (Set<GraphClass> gc : result.vertexSet()) {
            allNeededClasses.addAll(gc);
        }
        Object[] allVertexes = graph
                .getChildVertices(graph.getDefaultParent());
        for (Object gc : allVertexes) {
            Set<GraphClass> value = ((GraphClassSet)((mxCell)gc).getValue())
                    .getSet();
            allNeededClasses.addAll(value);
        }
        return Algo.createHierarchySubgraph(allNeededClasses);
    }

    /**
     * Create a hierarchy subgraph of the given classes and draw it. and Add Sub
     * / Superclasses of selected node.
     * 
     * @author philipp
     * @date 22.06 9:30
     * @annotation redraw the graph and add subgraph/superclass of the given
     *             class
     * 
     */

    public void drawSuperSub(Object[] cells) {
        // reduce the graph the Sets of equivalent Graphclasses
        graph.toggleCells(true, cells, true);
        hierachicalLayout.execute(graph.getDefaultParent());
        centerNode(getSelectedCell());
    }

    /**
     * Create a hierarchy subgraph of the given classes and draw it. and remove
     * Sub / Superclasses of selected node.
     * 
     * @author philipp, leo
     * @date 22.06 9:30
     * @annotation redraw the graph and remove subgraph/superclass of the given
     *             class
     * @annotation2 cleaned up a bit
     * 
     */

    public void deleteSuperSub(Object[] cells) {
        graph.toggleCells(false, cells, true);
    }

    /**
     * Get the nodes for deleting and adding SuperNodes
     * 
     * @author philipp
     * @date 22.06 9:30
     * @annotation
     * 
     */

    public Object[] getSuperNodes(mxCell cell) {
        // if the cell has associated edges in the given direction
        Object[] edges = new Object[] {};
        LinkedList<Object> queue = new LinkedList<Object>();
        Set<Object> result = new HashSet<Object>();
        // add root cell
        queue.add(cell);
        // find all superclasses with a BFS Search
        while (!queue.isEmpty()) {
            Object currentCell = queue.pollFirst();
            edges = graph.addAllEdges(new Object[] { currentCell });
            for (Object edge : edges) {
                if ((((mxCell)edge).isEdge())) {
                    Object temp = (((mxCell)edge).getSource());
                    if (temp != currentCell) {
                        // add it to the queue
                        queue.add(temp);
                        // add it to the result
                        result.add(temp);
                    }
                }
            }
        }
        return result.toArray();
    }

    /**
     * Get the subnodes for adding and deleting subnodes
     * 
     * @author philipp
     * @date 22.06 9:30
     * @annotation
     * 
     */

    public Object[] getSubNodes(mxCell cell) {
        // if the cell has associated edges in the given direction
        Object[] edges = new Object[] {};
        LinkedList<Object> queue = new LinkedList<Object>();
        Set<Object> result = new HashSet<Object>();
        // add root cell
        queue.add(cell);
        // find all subclasses with a BFS Search
        while (!queue.isEmpty()) {
            Object currentCell = queue.pollFirst();
            edges = graph.addAllEdges(new Object[] { currentCell });
            for (Object edge : edges) {
                if ((((mxCell)edge).isEdge())) {
                    Object temp = (((mxCell)edge).getTarget());
                    if (temp != currentCell) {
                        // add it to the queue
                        queue.add(temp);
                        // add it to the result
                        result.add(temp);
                    }
                }
            }
        }
        return result.toArray();
    }

    /**
     * Returns all classes on the canvas (unsorted).
     * 
     * @author leo
     * @date 14.04
     * @annotation reworked
     */
    public List<GraphClass> getClasses() {
        List<GraphClass> result = new ArrayList<GraphClass>();
        for (Set<GraphClass> gcs : map.keySet()) {
            for (GraphClass gc : gcs) {
                result.add(gc);
            }
        }
        return result;
    }

    /**
     * Returns all Names classes on the canvas (unsorted).
     * 
     * @author leo
     * @date 14.04
     * @annotation reworked, never used though, never tested
     */
    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (Object cell : graph.getChildVertices(graph.getDefaultParent())) {
            GraphClassSet graphClassSet = (GraphClassSet)((mxCell)cell)
                    .getValue();
            if (graphClassSet != null) {
                for (GraphClass gc : graphClassSet.getSet()) {
                    result.add(gc.toString());
                }
            }
        }
        return result;
    }

    /**
     * Set all nodes to their preferred names, according to the namingPreference
     * 
     * @author leo
     * @date 11.06., 14:00
     * @annotation Auf mxGraph angepasst, reworked 14.06.
     */
    public void setPreferedNames() {
        graph.setCellsResizable(true);
        parent.undoManager.setSignificant(false);
        graph.getModel().beginUpdate();
        try {
            for (Object cell : graph
                    .getChildVertices(graph.getDefaultParent())) {
                // reset all labels, because the cell does the naming alone
                GraphClassSet gcs = ((GraphClassSet) ((mxCell) cell)
                        .getValue());
                gcs.setLabel(null);
                graph.updateCellSize(cell);
            }
        } finally {
            hierachicalLayout
                    .run(graph.getChildCells(graph.getDefaultParent()));
            graph.getModel().endUpdate();
            parent.undoManager.setSignificant(true);
            graph.setCellsResizable(false);
        }
    }

    /**
     * Find the cell for the given graph class or null
     * 
     * @author leo
     * @date 14.06; 25.06.13
     * @annotation is a shortcut now
     */

    public Object findNode(GraphClass gc) {
        if (gc != null) {
            // search for the cell
            for (Set<GraphClass> set : map.keySet()) {
                if (set.contains(gc)) {
                    // found
                    return map.get(set);
                }
            }
        }
        // not found
        return null;
    }

    /**
     * set the switch drawUnproper and update the graph
     * 
     * @param b
     */
    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
        graph.getModel().beginUpdate();
        try {
            setProperness(graph.getAllEdges(new Object[] { graph
                    .getDefaultParent() }));
        } finally {
            ((mxGraphComponent)parent.drawingPane).refresh();
            graph.getModel().endUpdate();
        }
    }

    /**
     * check the properness of the given edges and draw a startArrow if the
     * inclusion is not proper
     * 
     * should be used during between model.beginUpdate() and model.endUpdate()
     * 
     * @author leo
     * @date 12.06 10:45
     * @annotation edited completely, works fine, you need to call
     *             model.beginupdate and model.end update in calling methode
     */
    protected void setProperness(Object[] edges) {
        if (edges != null && edges.length > 0) {
            for (Object cell : edges) {
                // safety check
                if (cell != null && ((mxCell)cell).isEdge()) {
                    if (drawUnproper) {
                        GraphClassSet source = (GraphClassSet)((mxCell)cell)
                                .getSource().getValue();

                        GraphClassSet target = (GraphClassSet)((mxCell)cell)
                                .getTarget().getValue();
                        List<Inclusion> path = GAlg.getPath(DataSet.inclGraph,
                                source.getSet().iterator().next(), target
                                        .getSet().iterator().next());
                        if (!(Algo.isPathProper(path) || Algo
                                .isPathProper(Algo.makePathProper(path)))) {
                            // uses color gray for drawing improper inclusions
                            graph.setCellStyles("startArrow", "improper",
                                    new Object[] { cell });
                        }
                    } else {
                        graph.setCellStyles("startArrow", "none",
                                new Object[] { cell });
                    }
                }
            }
        }

    }

    /**
     * Set coloring for p and repaint.
     */
    public void setProblem(Problem p) {
        // repaint only on a change
        if (problem != p) {
            problem = p;
            setComplexityColors();
        }
    }

    /**
     * returns the currently for coloring used problem
     * 
     * @return
     */
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
        Complexity complexity = problem.getComplexity(node.iterator().next());
        if (/* complexity == null || */complexity.isUnknown())
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
     * Set the background of all nodes to the proper complexity color.
     * 
     * @author leo
     * @date 8.06
     * @annotation reworked
     */
    public void setComplexityColors() {
        graph.getModel().beginUpdate();
        try {
            for (Set<GraphClass> gc : map.keySet()) {
                mxCell cell = (mxCell)map.get(gc);
                graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                        mxHtmlColor.getHexColorString(complexityColor(gc)),
                        new Object[] { cell });
            }
        } finally {
            ((mxGraphComponent)parent.drawingPane).refresh();
        }
        graph.getModel().endUpdate();
    }

    public void setNamingPref(Algo.NamePref pref) {
        namingPref = pref;
        setPreferedNames();
        // updateBounds();
        // repaint();
    }

    public Algo.NamePref getNamingPref() {
        return namingPref;
    }

    /**
     * Sets the label of the nodes in the graph to the names of the list. If a
     * node is present multiple times the last one is shown
     * 
     * @author leo
     * @param classesList
     * @date 14.06, 28.06. reworked to work with lists
     * @annotation sets the name of the node to the name that was selected for
     *             drawing it, despite the naming preference
     */
    public void setNodeName(List<GraphClass> classesList) {
        graph.setCellsResizable(true);
        graph.getModel().beginUpdate();
        parent.undoManager.setSignificant(false);
        try {
            for (GraphClass gc : classesList) {
                mxCell cell = (mxCell)findNode(gc);
                if (cell != null) {
                    GraphClassSet graphClassSet = (GraphClassSet)cell
                            .getValue();
                    graphClassSet.setLabel(gc);

                    graph.updateCellSize(cell);
                }
            }
            hierachicalLayout.execute(graph.getDefaultParent());
        } finally {
            ((mxGraphComponent)parent.drawingPane).refresh();
            hierachicalLayout.run(graph.getDefaultParent());
            graph.getModel().endUpdate();
            graph.setCellsResizable(false);
            parent.undoManager.setSignificant(false);
        }

    }

    /**
     * Customized for centering the selected Cell, by centering resp. to the
     * center of the selected node by calling the centerCanvas() Method of the
     * ISGCIMainFrame.
     * 
     * Actually, centering only possible, if the Cells are approxmiated to the
     * size of the Window in the middle of the Window ==> not really centering
     * if the cells are next to the border of the window, but after calling this
     * Method, the cell is definitly visible in the window as far as possible in
     * the middle of the window
     * 
     * @param cell
     * @author Matthias
     * @date 22.06
     * @annotation WORKS
     * 
     * 
     */

    public void centerNode(mxCell cell) {
        mxCellState state = graph.getView().getState(cell);
        if (state != null) {
            mxRectangle bounds = state;
            // get the middle of the selected cell
            Point p = new Point((int)bounds.getCenterX(),
                    (int)bounds.getCenterY());
            // center the canvas
            ((ISGCIMainFrame)parent).centerCanvas(p);
        }
        setSelectedCell(cell);
        this.getGraph().getView()
                .setTranslate(new mxPoint(LEFTMARGIN, TOPMARGIN));
    }

    // ----------------------- MouseListener stuff --------------------------

    public void registerMouseListener(mxGraphComponent pane) {
        pane.getGraphControl().addMouseListener(this);
        pane.getGraphControl().addMouseWheelListener(this);
        pane.getGraphControl().addMouseMotionListener(this);
    }

    public void mouseReleased(MouseEvent event) {
        mousePopup(event);
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    /**
     * Modified for changing cursor while on Canvas for different cursors on
     * Cells and Plain
     * 
     * @author Fabian Vollmer
     * @date 01.07
     */
    public void mouseMoved(MouseEvent e) {
        mxCell cell = (mxCell) ((mxGraphComponent) parent.drawingPane)
                .getCellAt(e.getX(), e.getY());
        if (cell != null) {
            if (cell.isVertex())
                ((mxGraphComponent) parent.drawingPane).getGraphControl()
                        .setCursor(pointcursor);
        } else {
            // ((mxGraphComponent) parent.drawingPane).getGraphControl()
            // .setCursor(grabcursor);
        }
    }

    /**
     * few Methods needed for "Show Information" and other Functionality
     * 
     * @param cell
     * @author philipp, leo
     * @date 21.06, 30.06.
     * @annotation now changes teh e
     */

    public void setSelectedCell(mxCell cell) {
        graph.setSelectionCell(null);
        parent.getUndoM().setSignificant(false);
        graph.getModel().beginUpdate();
        try {
            if (lastSelected != null && cell != lastSelected) {
                graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1",
                        new Object[] { lastSelected });
            }
            this.lastSelected = cell;
            // update the sidebar
            setSidebarConent();
            // reset the old selection of edges
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, EDGE_COLOR,
                    highlitedEdges);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1",
                    highlitedEdges);
            setProperness(highlitedEdges);

            // switch the selected edges
            highlitedEdges = graph.getEdges(cell);

            // color the new edges
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                    ALTERNATE_EDGE_COLOR, highlitedEdges);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "3",
                    highlitedEdges);
            setProperness(highlitedEdges);
            // reset the style of the old highlighted nodes
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, CELL_COLOR,
                    highlitedCells);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1",
                    highlitedCells);

            // get and switch the associated cells
            Set<Object> temp = new HashSet<Object>();
            for (Object edge : graph.getIncomingEdges(cell)) {
                temp.add(((mxCell)edge).getSource());
            }
            for (Object edge : graph.getOutgoingEdges(cell)) {
                temp.add(((mxCell)edge).getTarget());
            }
            highlitedCells = temp.toArray();
            // highlight the cells of the new highlighted nodes
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                    ALTERNATE_CELL_COLOR, highlitedCells);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "3",
                    highlitedCells);

            // make selected cell back
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "3",
                    new Object[] { cell });
        } finally {
            graph.getModel().endUpdate();
            parent.getUndoM().setSignificant(true);
        }
    }

    public mxCell getSelectedCell() {
        return lastSelected;

    }

    public void setSidebarConent() {
        if (parent.sidebar.isVisible()) {
            if (getSelectedCell() != null) {
                if (parent.sidebar.getContent() != ((GraphClassSet)getSelectedCell()
                        .getValue()).getLabel().getID()) {
                    parent.sidebar
                            .changeContent(((GraphClassSet)getSelectedCell()
                                    .getValue()).getLabel().getID());
                }
            } else {
                parent.sidebar.setDefaultText();
            }
        }
    }

    public void setParent(ISGCIMainFrame parent) {
        this.parent = parent;
    }

    public ISGCIMainFrame getParent() {
        return parent;
    }

    public void mousePressed(MouseEvent event) {
        mousePopup(event);
        start = event.getPoint();
    }

    public void mouseClicked(MouseEvent event) {
        mousePopup(event);
    }

    /**
     * Method to enable panning if the mouse is dragged with a pressed
     * mousebutton.
     */
    public void mouseDragged(MouseEvent event) {
        graph.getModel().beginUpdate();
        try {
            if (!event.isConsumed() && start != null) {

                // Cursor handling for dragging
                ((mxGraphComponent)parent.drawingPane).getGraphControl()
                        .setCursor(grabbingcursor);

                int dx = event.getX() - start.x;
                int dy = event.getY() - start.y;

                Rectangle r = parent.drawingPane.getViewport().getViewRect();

                int right = r.x + ((dx > 0) ? 0 : r.width) - dx;
                int bottom = r.y + ((dy > 0) ? 0 : r.height) - dy;

                ((mxGraphComponent)parent.drawingPane)
                        .getGraphControl()
                        .scrollRectToVisible(new Rectangle(right, bottom, 0, 0));

                event.consume();
            }
            super.repaint();
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Method for showing a popup menu if a node or an edge is rightclicked
     */
    protected boolean mousePopup(MouseEvent event) {
        // select the node, if the event is not a popup trigger
        if (!event.isPopupTrigger()) {
            event.consume();
            Object cell = ((mxGraphComponent)parent.drawingPane).getCellAt(
                    event.getX(), event.getY());
            if (cell != null) {
                mxCell c = (mxCell)cell;
                if (!c.isEdge()) {
                    setSelectedCell(c);
                    setParent(parent);
                }
            }
            return false;

            /*
             * Method for showing a popup menu if a node or an edge is
             * rightclicked
             */
        } else {
            event.consume();
            Object cell = ((mxGraphComponent)parent.drawingPane).getCellAt(
                    event.getX(), event.getY());
            if (cell != null) {
                mxCell c = (mxCell)cell;
                if (c.isEdge()) {
                    edgePopup.setEdge(c);
                    edgePopup.show(parent,
                            event.getXOnScreen() - parent.getX(),
                            event.getYOnScreen() - parent.getY());
                } else {
                    nodePopup.show(parent,
                            event.getXOnScreen() - parent.getX(),
                            event.getYOnScreen() - parent.getY());
                }
            }
            // parent.getUndoM().
            return true;
        }
    }

    /**
     * Zooming Function by setting values after computing relative to the mouse
     * position for the Scrollbars.
     * 
     * @date 21.06.2013
     * @author Matthias
     */

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double zoom = ((mxGraphComponent)parent.getContentPane()
                .getComponent(0)).getZoomFactor();
        int x = e.getX();
        int y = e.getY();

        Point view = ((mxGraphComponent) parent.getContentPane().getComponent(
                0)).getViewport().getViewPosition();
        int width = parent.drawingPane.getViewport().getWidth();
        int height = parent.drawingPane.getViewport().getHeight();
        view = new Point(view.x + width / 2, view.y + height / 2);
        int dx = view.x - x;
        int dy = view.y - y;
        graph.getModel().beginUpdate();
        try {
            if (e.getWheelRotation() < 0) {
                // Boundary for zooming in;
                if (graph.getView().getScale() <= 50) {
                    ((mxGraphComponent)parent.getContentPane().getComponent(0))
                            .zoomIn();
                    x = Math.round((float)(zoom * x + dx));
                    y = Math.round((float)(zoom * y + dy));
                    Point p = new Point(x, y);
                    parent.centerCanvas(p);
                }
            } else {
                ((mxGraphComponent)parent.getContentPane().getComponent(0))
                        .zoomOut();
                x = Math.round((float)(x / zoom + dx));
                y = Math.round((float)(y / zoom + dy));
                Point p = new Point(x, y);
                parent.centerCanvas(p);
            }

            this.getGraph()
                    .getView()
                    .setTranslate(
                            new mxPoint(LEFTMARGIN * zoom, TOPMARGIN * zoom));

        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Zoom to Fit in Window: when calling this method and the graphbounds are
     * smaller than 3/4 of the windowsize, then will the graph zoomed to nearly
     * completely fit in the window if the graph is bigger then the window,
     * respectively to windowsize zoomOut, so that the whole graph will be
     * visible in the Window
     * 
     * @version 28.06.2013
     * @author Matthias Miller
     * 
     */
    public void fitInWindow() {

        graph.getModel().beginUpdate();
        try {
            double newScale = 1;
            Dimension graphSize, viewPortSize;
            mxRectangle bounds = ((mxGraphComponent)parent.getContentPane()
                    .getComponent(0)).getGraph().getGraphBounds();

            graphSize = ((mxGraphComponent)parent.getContentPane()
                    .getComponent(0)).getGraphControl().getSize();
            viewPortSize = ((mxGraphComponent)parent.getContentPane()
                    .getComponent(0)).getViewport().getSize();

            double gw = graphSize.getWidth();
            double gh = graphSize.getHeight();
            double gbw = bounds.getWidth() + bounds.getX();
            double gbh = bounds.getHeight() + bounds.getY();
            double w = viewPortSize.getWidth();
            double h = viewPortSize.getHeight();

            if (gbw < 0.75 * w && gbh < 0.75 * h) {
                newScale = 0.95 * Math.min(w / gbw, h / gbh);
            } else {
                newScale = Math.min(w / gw, h / gh);
            }
            if (newScale < 0.05) {
                newScale = 0.05;
            }
            zoom(newScale);
            this.getGraph()
                    .getView()
                    .setTranslate(
                            new mxPoint(LEFTMARGIN * newScale, TOPMARGIN
                                    * newScale));
        } finally {
            graph.getModel().endUpdate();
        }

    }

    /**
     * Executes/Calculates Morphing/Animation for a layout
     * 
     * @return Action, which executes Animation
     * @author Matthias Miller
     * @date 30.06.2013
     */
    public void animateGraph() {
        final mxHierarchicalLayout layout = new mxHierarchicalLayout(
                ((mxGraphComponent)parent.getContentPane().getComponent(0))
                        .getGraph());

        if (layout != null) {

            final mxGraph graph = ((mxGraphComponent)parent.getContentPane()
                    .getComponent(0)).getGraph();
            Object cell = graph.getDefaultParent();

            graph.getModel().beginUpdate();
            try {
                layout.execute(cell);
            } finally {
                mxMorphing morph = new mxMorphing(((mxGraphComponent)parent
                        .getContentPane().getComponent(0)), 20, 1.5, 10);
                // add a listener, to revalidte the graph after the morphing is
                // done
                morph.addListener(mxEvent.DONE, new mxIEventListener() {

                    public void invoke(Object sender, mxEventObject evt) {
                        graph.refresh();
                        graph.getModel().endUpdate();
                        parent.drawingPane.getComponent(0).validate();
                        centerNode(getSelectedCell());
                    }

                });
                morph.startAnimation();
            }
        }
    }

    /**
     * Set boolean value, to activate morphing
     * 
     * @param active
     */
    public void setAnimation(boolean active) {
        animationActivated = active;
    }

    /**
     * Return whether Animation is activated
     * 
     * @return boolean value whether animation is activated or not
     */
    public boolean getAnimation() {
        return animationActivated;
    }

    public NodePopup getNodePopup() {
        return nodePopup;
    }

    /*
     * shortened index method
     * 
     * @author Matthias Miller
     */
    public int ind() {
        return parent.getUndoM().getIndexNext();
    }

    /**
     * resets the graph to default color and no selected cell, also updates the
     * map for fast access, always use this method with every undo/redo, never
     * use it in other cases
     * 
     * @author leo
     * @date 11.07.
     */
    public void resetGraph() {
        if (lastSelected != null)
            setSelectedCell(null);
        map.clear();
        for (Object cell : graph.getChildCells(graph.getDefaultParent(), true,
                false)) {
            map.put(((GraphClassSet)((mxCell)cell).getValue()).getSet(), cell);
        }
        setSidebarConent();
        parent.getUndoM().setSignificant(false);
        graph.getModel().beginUpdate();
        try {
            Object[] edges = graph.getChildCells(graph.getDefaultParent(),
                    false, true);
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, EDGE_COLOR,
                    edges);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1", edges);
            Object[] vertexes = graph.getChildCells(graph.getDefaultParent(),
                    true, false);
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, CELL_COLOR,
                    vertexes);
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1", vertexes);
            // necessary because of possible label changes
            for (Object cell : map.values()) {
                graph.updateCellSize(cell);
            }
            // do a layout due to possible label changes
            hierachicalLayout.execute(graph.getDefaultParent());
        } finally {
            graph.getModel().endUpdate();
            parent.getUndoM().setSignificant(true);
        }
    }
}

/* EOF */
