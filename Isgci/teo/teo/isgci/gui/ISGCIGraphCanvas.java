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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import teo.isgci.grapht.ISGCIVertexFactory;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.RevBFSWalker;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * A canvas that can display an inclusion graph.
 */
public class ISGCIGraphCanvas extends GraphCanvas<Set<GraphClass>, DefaultEdge>
        implements MouseWheelListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8803653758744069010L;
    protected NodePopup nodePopup;
    protected EdgePopup edgePopup;
    protected Problem problem;
    protected Algo.NamePref namingPref;
    private ISGCIMainFrame parent;
    private mxGraph graph;
    private mxCell lastSelected;

    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;
    private HashMap<Set<GraphClass>, Object> map;
    private mxHierarchicalLayout layout;
    private Point start;

    private String vertexStyle = "shape=rectangle;fontColor=black;strokeColor=black";
    private String edgeStyle = "strokeColor=black";

    public ISGCIGraphCanvas(ISGCIMainFrame parent, mxGraph graph) {
        super(parent, ISGCIMainFrame.latex, new ISGCIVertexFactory(), null);
        this.parent = parent;
        problem = null;
        this.map = new HashMap<Set<GraphClass>, Object>();
        this.graph = graph;
        namingPref = Algo.NamePref.BASIC;
        // setWidthFunc(new NodeWidthFunc());
        nodePopup = new NodePopup(parent, graph);
        edgePopup = new EdgePopup(parent);
        add(nodePopup);
        add(edgePopup);
        layout = new mxHierarchicalLayout(graph);
        // make default cells white

    }

    // /**
    // * Add the given graph to this canvas.
    // */
    // protected GraphView<Set<GraphClass>, DefaultEdge> addGraph(
    // SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g) {
    // GraphView<Set<GraphClass>, DefaultEdge> gv = super.addGraph(g);
    // for (NodeView<Set<GraphClass>, DefaultEdge> nv : gv.getNodeViews()) {
    // nv.setColor(complexityColor(nv.getNode()));
    // nv.setNameAndLabel(Algo.getName(nv.getNode(), namingPref));
    // }
    // return gv;
    // }

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
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            graph.setCellsResizable(true);
            // Add vertices
            for (Set<GraphClass> gc : edgegraph.vertexSet()) {
                // check if node is already present
                if (map.get(gc) == null) {
                    // add the node
                    GraphClassSet graphClasses = new GraphClassSet(gc, this);
                    Object vertex = graph.insertVertex(parent, gc.toString(),
                            graphClasses, 20, 20, 80, 30, vertexStyle);
                    // add the node to the map
                    map.put(gc, vertex);
                    // update the size of the node to match the text
                    graph.updateCellSize(vertex);
                    ((mxCell)vertex).setConnectable(false);
                }
            }

            // add edges
            for (DefaultEdge edge : edgegraph.edgeSet()) {
                Set<GraphClass> source = edgegraph.getEdgeSource(edge);
                Set<GraphClass> target = edgegraph.getEdgeTarget(edge);
                // check if the edge is already present
                if (graph.getEdgesBetween(map.get(source), map.get(target)).length == 0)
                    graph.insertEdge(parent, null, null, map.get(source),
                            map.get(target), edgeStyle);

            }
            ((mxGraphComponent)this.parent.drawingPane).validate();
            // make Layout
            layout.execute(parent);

            // make edges look nice
            if (drawUnproper) {
                setProperness();
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
                        graph.getChildCells(parent));
            }

        } finally {
            graph.getModel().endUpdate();
            graph.setCellsResizable(false);

        }
    }

    /**
     * Create the neighbors of the given classes and draw it. and add direct Sub
     * and superclasses of selected node.
     * 
     * @author leo
     * @date 26.06 3:00
     * @annotation2 reworked to make it work with makeGraph()
     * 
     * 
     */
    public void drawNeighbours() {
        Set<GraphClass> selected = ((GraphClassSet)getSelectedCell().getValue())
                .getSet();
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
        // add the new subgraph to the mxGraph
        makeGraph(result);
        centerNode((mxCell)map.get(selected));
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

    public void drawSuperSub(Collection<GraphClass> nodes) {
        // add all present nodes to the collection
        nodes.addAll(getClasses());
        // reduce the graph
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);
        makeGraph(edgegraph);
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
     * @annotation2 now deletes from the map too, cleaned up a bit
     * 
     */

    public void deleteSuperSub(Collection<GraphClass> nodes) {
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);
        graph.getModel().beginUpdate();
        try {
            graph.setCellsResizable(true);

            ArrayList<Object> toDelete = new ArrayList<Object>();
            // add nodes to an array to delete them
            for (Set<GraphClass> gc : edgegraph.vertexSet()) {
                if (((mxGraphModel)graph.getModel()).getCell(gc.toString()) != null
                        && getSelectedCell() != ((mxGraphModel)graph.getModel())
                                .getCell(gc.toString())) {
                    Object cell = ((mxGraphModel)graph.getModel()).getCell(gc
                            .toString());
                    toDelete.add(cell);
                    // delete from map
                    map.remove(gc);
                }
            }
            // add Edges to an array to delete them
            for (DefaultEdge edge : edgegraph.edgeSet()) {
                if (((mxGraphModel)graph.getModel()).getCell(edge.toString()) != null
                        && getSelectedCell() != ((mxGraphModel)graph.getModel())
                                .getCell(edge.toString())) {
                    Object cell = ((mxGraphModel)graph.getModel()).getCell(edge
                            .toString());
                    toDelete.add(cell);
                }
            }
            // remove the gathered cells
            Object[] cells = toDelete.toArray(new Object[0]);
            graph.removeCells(cells);
            // make Layout
            // layout.execute(parent);
            /*
             * not needed // make edges look nice // if (drawUnproper) { //
             * setProperness(); // } // // make nodes colorful // if (problem !=
             * null) { // setComplexityColors(); // // } else { // // make all
             * cells white // graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
             * // // the color white understandable for mxGraph //
             * mxHtmlColor.getHexColorString(COLOR_UNKNOWN), // // get all cells
             * // graph.getChildCells(parent)); // }
             */
        } finally {
            graph.getModel().endUpdate();
            graph.setCellsResizable(false);
        }
    }

    /**
     * Get the nodes for deleting and adding SuperNodes
     * 
     * @author philipp
     * @date 22.06 9:30
     * @annotation includes parts of getNodes from
     *             GraphClassSelectionDialog.java (bottom)
     * 
     */

    protected Collection<GraphClass> getSuperNodes() {
        final HashSet<GraphClass> result = new HashSet<GraphClass>();
        GraphClass gc = NodePopup.searchName(getSelectedCell());
        new RevBFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                super.visit(v);
            }
        }.run();
        return result;
    }

    /**
     * Get the nodes for adding and Deleting SubNodes
     * 
     * @author philipp
     * @date 22.06 9:30
     * @annotation includes parts of getNodes from
     *             GraphClassSelectionDialog.java (bottom)
     * 
     */

    protected Collection<GraphClass> getSubNodes() {
        final HashSet<GraphClass> result = new HashSet<GraphClass>();
        GraphClass gc = NodePopup.searchName(getSelectedCell());
        new BFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                super.visit(v);
            }
        }.run();
        return result;
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
        for (Object cell : graph.getChildVertices(graph.getDefaultParent())) {
            GraphClassSet graphClassSet = (GraphClassSet)((mxCell)cell)
                    .getValue();
            if (graphClassSet != null) {
                for (GraphClass gc : graphClassSet.getSet()) {
                    result.add(gc);
                }
            }
        }
        return result;
    }

    /**
     * Returns all Names classes on the canvas (unsorted).
     * 
     * @author leo
     * @date 14.04
     * @annotation reworked, never used though
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
     * Set all nodes to their prefered names.
     * 
     * @author leo
     * @date 11.06., 14:00
     * @annotation Auf mxGraph angepasst, reworked 14.06.
     */
    public void setPreferedNames() {
        graph.setCellsResizable(true);
        graph.getModel().beginUpdate();
        try {
            for (Object cell : graph.getChildVertices(graph.getDefaultParent())) {
                // reset all labels, because the cell does the naming alone
                GraphClassSet gcs = ((GraphClassSet)((mxCell)cell).getValue());
                gcs.setLabel(null);
                graph.updateCellSize(cell, true);
            }

        } finally {
            // graph.refresh();

            layout.run(graph.getChildCells(graph.getDefaultParent()));
            graph.getModel().endUpdate();
            graph.setCellsResizable(false);
        }
    }

    /**
     * Find the NodeView for the given graph class or null if not found shortcut
     * method to work with NodeViews, should be replaced i dont know what this
     * is needed for but the lanfmatk wants it
     */
    @SuppressWarnings("unchecked")
    // the cast is save
    public NodeView<Set<GraphClass>, DefaultEdge> findNode(GraphClass gc) {
        return (NodeView<Set<GraphClass>, DefaultEdge>)findNode(gc, false);
    }

    /**
     * Find the cell for the given graph class or null if not found assumes that
     * you search with an HTML label
     * 
     * @author leo
     * @date 14.06; 25.06.13
     * @annotation is a shortcut now
     */
    public Object findNode(GraphClass gc, boolean mxCellOrNodeView) {
        return findNode(gc, mxCellOrNodeView, false);
    }

    /**
     * Find the cell for the given graph class or null if not found, true ==
     * latex label, false == HTML label for comparison
     * 
     * @author leo
     * @date 25.06.13
     * @annotation should work now
     */
    public Object findNode(GraphClass gc, boolean mxCellOrNodeView,
            boolean latexOrHTML) {
        if (mxCellOrNodeView) {
            if (latexOrHTML) {
                for (Object cell : graph.getChildVertices(graph
                        .getDefaultParent())) {
                    Set<GraphClass> set = ((GraphClassSet)((mxCell)cell)
                            .getValue()).getSet();
                    if (set.contains(gc)) {
                        System.out
                                .println(((mxCell)cell).getValue().toString());
                        return (mxCell)cell;

                    }
                }
            } else {
                for (Object cell : graph.getChildVertices(graph
                        .getDefaultParent())) {
                    Set<GraphClass> set = ((GraphClassSet)((mxCell)cell)
                            .getValue()).getSet();
                    if (set.contains(gc)) {
                        System.out
                                .println(((mxCell)cell).getValue().toString());
                        return (mxCell)cell;

                    }
                }
            }
        } else {
            for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs) {
                for (NodeView<Set<GraphClass>, DefaultEdge> v : gv
                        .getNodeViews())
                    if (v.getNode().contains(gc))
                        return v;
            }
        }
        return null;
    }

    @Override
    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
        graph.getModel().beginUpdate();
        try {
            setProperness();
        } finally {
            ((mxGraphComponent)parent.drawingPane).refresh();
            graph.getModel().endUpdate();
        }
    }

    /**
     * 
     * properness of the given edge-cell
     * 
     * @author leo
     * @date 12.06 10:45
     * @annotation edited completely, works just fine, you need to call
     *             model.beginupdate and model.end update in calling methode
     */
    protected void setProperness() {
        for (Object cell : graph.getAllEdges(new Object[] { graph
                .getDefaultParent() })) {
            // safety check
            if (((mxCell)cell).isEdge()) {
                if (drawUnproper) {
                    GraphClassSet source = (GraphClassSet)((mxCell)cell)
                            .getSource().getValue();

                    GraphClassSet target = (GraphClassSet)((mxCell)cell)
                            .getTarget().getValue();
                    List<Inclusion> path = GAlg.getPath(DataSet.inclGraph,
                            source.getSet().iterator().next(), target.getSet()
                                    .iterator().next());
                    if (!(Algo.isPathProper(path) || Algo.isPathProper(Algo
                            .makePathProper(path)))) {
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

    /**
     * Set coloring for p and repaint.
     */
    public void setProblem(Problem p) {
        if (problem != p) {
            problem = p;
            setComplexityColors();
            // repaint();
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
     * Set all nodes to the proper complexity color.
     * 
     * @author leo
     * @date 8.06
     * @annotation reworked
     */
    public void setComplexityColors() {
        System.out
                .println("+#################################################Coloring");
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
     * Center the canvas on the given NodeView.
     */
    public void centerNode(NodeView<Set<GraphClass>, DefaultEdge> v) {
        Point p = null;

        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs) {
            if ((p = gv.getNodeCenter(v)) != null) {
                ((ISGCIMainFrame)parent).centerCanvas(p);
                return;
            }
        }
    }

    /**
     * @author leo
     * @param classesList
     * @date 14.06
     * @annotation sets the name of the node to the name that was selected for
     *             drawing it, despite the naming preference
     */
    public void setNodeName(NodeList classesList) {
        // made getSelectedValues() to getSelectedValuesList()
        graph.getModel().beginUpdate();
        try {
            for (Object o : classesList.getSelectedValuesList()) {
                GraphClass gc = (GraphClass)o;
                mxCell cell = (mxCell)findNode(gc, true);
                if (cell != null) {
                    GraphClassSet graphClassSet = (GraphClassSet)cell
                            .getValue();
                    graphClassSet.setLabel(gc);
                    graph.updateCellSize(cell);
                }
            }
        } finally {
            graph.getModel().endUpdate();
            ((mxGraphComponent)parent.drawingPane).refresh();
        }

    }

    /**
     * reworked
     * 
     * @author leo
     * @param cell
     */
    public void markOnly(mxCell cell) {
        graph.getSelectionModel().setCell(cell);
    }

    /**
     * like in the mxGraphComponent does not work :/ ==> Update: WORKS
     * 
     * @param cell
     * @author leo
     * @date 14.06
     * 
     * @author Matthias
     * @date 22.06
     * 
     *       Customized for centering the selected Cell, by centering resp. to
     *       the center of the selected node by calling the centerCanvas()
     *       Method of the ISGCIMainFrame ==> WORKS, true centering only
     *       possible, if the Cells are approxmiated to the size of the Window
     *       in the middle of the Window ==> not really centering if the cells
     *       are next to the border of the window, but after calling this
     *       Method, the cell is definitly visible in the window as far as
     *       possible in the middle of the window
     */

    public void centerNode(mxCell cell) {
        mxCellState state = graph.getView().getState(cell);

        if (state != null) {
            mxRectangle bounds = state;

            // bounds = (mxRectangle) bounds.clone();

            Point p = new Point((int)bounds.getCenterX(),
                    (int)bounds.getCenterY());
            ((ISGCIMainFrame)parent).centerCanvas(p);
        }
        setSelectedCell(cell);
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

    public void mouseMoved(MouseEvent e) {
    }

    /**
     * few Methods needed for "Show Information" and other Functionality
     * 
     * @param cell
     *            and parent
     * @author philipp
     * @date 21.06
     */

    public void setSelectedCell(mxCell cell) {
        if (lastSelected != null && cell != lastSelected) {
            graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1",
                    new Object[] { lastSelected });
        }
        graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "3",
                new Object[] { cell });
        this.lastSelected = cell;
        setSidebarConent();
    }

    public mxCell getSelectedCell() {
        return lastSelected;

    }

    public void setSidebarConent() {
        if (parent.sidebar.isVisible()) {
            if (getSelectedCell() != null) {
                if (parent.sidebar.getContent() != NodePopup.searchName(
                        getSelectedCell()).getID()) {
                    parent.sidebar.setContent(NodePopup.searchName(
                            getSelectedCell()).getID());
                }
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

    public void mouseDragged(MouseEvent event) {

        if (!event.isConsumed() && start != null) {
            int dx = event.getX() - start.x;
            int dy = event.getY() - start.y;

            Rectangle r = parent.drawingPane.getViewport().getViewRect();

            int right = r.x + ((dx > 0) ? 0 : r.width) - dx;
            int bottom = r.y + ((dy > 0) ? 0 : r.height) - dy;

            ((mxGraphComponent)parent.drawingPane).getGraphControl()
                    .scrollRectToVisible(new Rectangle(right, bottom, 0, 0));

            event.consume();
        }

        // if (!dragInProcess) {
        // markSetShadow(true);
        // dragInProcess = true;
        // }
        // markSetShadowAnchorLocation(event.getPoint());
        super.repaint();
    }

    /**
     * Method for showing a popup menu if a node or an edge is rightclicked
     */
    protected boolean mousePopup(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            event.consume();
            Object cell = ((mxGraphComponent)parent.drawingPane).getCellAt(
                    event.getX(), event.getY());
            if (cell != null) {
                mxCell c = (mxCell)cell;
                if (!c.isEdge()) {
                    setSelectedCell(c);
                    setParent(parent);
                    System.out
                            .println("Save parent and cell somewhere useful for GraphClassInformation");
                }
            }
            return false;

            /**
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
                    nodePopup.setNode(c);
                    nodePopup.show(parent,
                            event.getXOnScreen() - parent.getX(),
                            event.getYOnScreen() - parent.getY());
                }
            }
            return true;
        }

        //
        // event.consume();
        //
        // View v = getViewAt(event.getPoint());
        // if (v == null)
        // return true;
        // if (v instanceof NodeView) {
        // nodePopup.setNode((NodeView)v);
        // nodePopup.show(this, event.getX(), event.getY());
        // }
        // if (v instanceof EdgeView) {
        // edgePopup.setEdge((EdgeView)v);
        // edgePopup.show(this, event.getX(), event.getY());
        // }
        // return true;
    }

    // ----------------------- Width calculator --------------------------
    // class NodeWidthFunc implements IntFunction<Set<GraphClass>> {
    // /** Return the width of node */
    // public int execute(Set<GraphClass> node) {
    // NodeView<Set<GraphClass>, DefaultEdge> view = getView(node);
    // view.updateSize();
    // return view.getSize().width;
    // }
    // }

    /**
     * @date 21.06.2013
     * @author Matthias
     * @annotation Zooming Function by setting values after computing relative
     *             to the mouse position for the Scrollbars ==> WORKS!!!
     */

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double zoom = ((mxGraphComponent)parent.getContentPane()
                .getComponent(0)).getZoomFactor();
        int x = e.getX();
        int y = e.getY();

        Point view = ((mxGraphComponent)parent.getContentPane().getComponent(0))
                .getViewport().getViewPosition();
        int width = parent.drawingPane.getViewport().getWidth();
        int height = parent.drawingPane.getViewport().getHeight();
        view = new Point(view.x + width / 2, view.y + height / 2);
        int dx = view.x - x;
        int dy = view.y - y;
        graph.getModel().beginUpdate();
        try {
            if (e.getWheelRotation() < 0) {
                ((mxGraphComponent)parent.getContentPane().getComponent(0))
                        .zoomIn();
                x = Math.round((float)(zoom * x + dx));
                y = Math.round((float)(zoom * y + dy));
                Point p = new Point(x, y);
                parent.centerCanvas(p);
            } else {
                ((mxGraphComponent)parent.getContentPane().getComponent(0))
                        .zoomOut();
                x = Math.round((float)(x / zoom + dx));
                y = Math.round((float)(y / zoom + dy));
                Point p = new Point(x, y);
                parent.centerCanvas(p);
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }

}

/* EOF */
