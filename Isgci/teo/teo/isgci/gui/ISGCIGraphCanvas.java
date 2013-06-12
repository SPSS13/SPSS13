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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.XsltUtil;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.ISGCIVertexFactory;
import teo.isgci.grapht.Inclusion;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.util.IntFunction;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.view.mxGraph;

/**
 * A canvas that can display an inclusion graph.
 */
public class ISGCIGraphCanvas extends
        GraphCanvas<Set<GraphClass>, DefaultEdge> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected NodePopup nodePopup;
    protected EdgePopup edgePopup;
    protected Problem problem;
    protected Algo.NamePref namingPref;
    private ISGCIMainFrame parent;
    private mxGraph graph;

    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;
    private HashMap<Set<GraphClass>, Object> map;
    private mxHierarchicalLayout layout;

    public ISGCIGraphCanvas(ISGCIMainFrame parent, mxGraph graph) {
        super(parent, ISGCIMainFrame.latex, new ISGCIVertexFactory(), null);
        this.parent = parent;
        problem = null;
        this.map = new HashMap<Set<GraphClass>, Object>();
        this.graph = graph;
        namingPref = Algo.NamePref.BASIC;
        setWidthFunc(new NodeWidthFunc());
        nodePopup = new NodePopup(parent);
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
     * Create a hierarchy subgraph of the given classes and draw it.
     * 
     * @author leo
     * @date 12.06 9:30
     * @annotation rewritten so that it uses GraphClassSet to print the label
     *             automatically and still be able to retrieve the set
     */
    public void drawHierarchy(Collection<GraphClass> nodes) {
        map.clear();
        graph.getModel().beginUpdate();
        ((mxGraphModel)graph.getModel()).clear();
        graph.getModel().endUpdate();

        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = Algo
                .createHierarchySubgraph(nodes);

        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            graph.setCellsResizable(true);
            // Add vertices
            for (Set<GraphClass> gc : edgegraph.vertexSet()) {
                GraphClassSet<GraphClass> graphClasses = new GraphClassSet<GraphClass>(
                        gc, this);
                Object vertex = graph.insertVertex(parent, gc.toString(),
                        graphClasses, 20, 20, 80, 30, "shape=roundtangle");
                map.put(gc, vertex);
                graph.updateCellSize(vertex);
                ((mxCell)vertex).setConnectable(false);
            }
            // add edges
            for (DefaultEdge edge : edgegraph.edgeSet()) {
                Set<GraphClass> source = edgegraph.getEdgeSource(edge);
                // System.out.println(source);
                Set<GraphClass> target = edgegraph.getEdgeTarget(edge);
                // System.out.println(target);
                graph.insertEdge(parent, null, null, map.get(source),
                        map.get(target));
            }
            layout.execute(parent);
            if(drawUnproper){
                setProperness();
            }
        } finally {
            graph.getModel().endUpdate();
            graph.setCellsResizable(false);
        }
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
        mxGraphComponent graphComponent = new mxGraphComponent(graph);

        this.parent.drawingPane = graphComponent;
        ((mxGraphComponent)this.parent.drawingPane).refresh();
    }

    private Object setUnproper(Object cell) {

        return cell;
    }

    /**
     * Returns all classes on the canvas (unsorted). * @author leo
     * 
     * @date 11.06.
     * @annitation debgging, first approach
     */
    public List<GraphClass> getClasses() {
        List<GraphClass> result = new ArrayList<GraphClass>();
        for (Collection<GraphClass> gc : map.keySet()) {
            for (GraphClass c : gc)
                result.add(c);
        }
        return result;
    }

    /**
     * Returns all names of all nodes.
     */
    public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs) {
            for (NodeView<Set<GraphClass>, DefaultEdge> nv : gv.getNodeViews())
                for (GraphClass gc : nv.getNode())
                    result.add(gc.toString());
        }
        return result;
    }

    /**
     * Set all nodes to their prefered names.
     * 
     * @author leo
     * @date 11.06., 14:00
     * @annotation Auf mxGraph angepasst
     */
    public void setPreferedNames() {
        // graph.setCellsResizable(true);
        graph.getModel().beginUpdate();
        try {
            for (Set<GraphClass> gc : map.keySet()) {
                // ((mxCell)map.get(gc)).setValue(XsltUtil.latex(Algo.getName(gc,
                // namingPref)));
                Object cell = map.get(gc);
                graph.getModel().setValue(cell,
                        XsltUtil.latex(Algo.getName(gc, namingPref)));
                graph.updateCellSize(cell);
            }
            layout.run(graph.getChildCells(graph.getDefaultParent()));
        } finally {

            graph.getModel().endUpdate();

            // graph.setCellsResizable(false);
        }
    }

    /**
     * Find the NodeView for the given graph class or null if not found
     */
    public NodeView<Set<GraphClass>, DefaultEdge> findNode(GraphClass gc) {
        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs) {
            for (NodeView<Set<GraphClass>, DefaultEdge> v : gv.getNodeViews())
                if (v.getNode().contains(gc))
                    return v;
        }
        return null;
    }

    /**
     * Bit of a hack to get all ISGCI stuff in one place: Set the appropriate
     * properness of the given edgeview.
     * @author leo
     * @date 12.06 10:45
     * @annotation edited completly
     */
    protected void setProperness() {
        for (Object cell : graph.getAllEdges(new Object[]{graph.getDefaultParent()})) {
            if (((mxCell) cell).isEdge()) {
                @SuppressWarnings("unchecked")
                GraphClassSet<GraphClass> source = (GraphClassSet<GraphClass>)((mxCell) cell).getSource().getValue();
                @SuppressWarnings("unchecked")
                GraphClassSet<GraphClass> target = (GraphClassSet<GraphClass>)((mxCell) cell).getTarget().getValue();
                List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, source.getSet().iterator()
                        .next(), target.getSet().iterator()
                        .next());
                if(Algo.isPathProper(path)
                        || Algo.isPathProper(Algo.makePathProper(path))){
                    ((mxCell)cell).setStyle("startArrow=open");
                }
            }
        }
//        List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, view.getFrom()
//                .iterator().next(), view.getTo().iterator().next());
//        view.setProper(Algo.isPathProper(path)
//                || Algo.isPathProper(Algo.makePathProper(path)));
    }

    /**
     * Set coloring for p and repaint. * @author leo
     * 
     * @date 10.06.
     * @annitation works fine
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
     * 
     * @author leo
     * @date 10.06.
     * @annitation works fine
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
     * @date 10.06.
     * @annitation works fine
     */
    public void setComplexityColors() {
        for (Set<GraphClass> gc : map.keySet()) {
            mxCell cell = (mxCell)map.get(gc);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                    mxHtmlColor.getHexColorString(complexityColor(gc)),
                    new Object[] { cell });
            ((mxGraphComponent)parent.drawingPane).refresh();
        }
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

        for (GraphView<Set<GraphClass>, DefaultEdge> gv : graphs) {
            if ((p = gv.getNodeCenter(v)) != null) {
                ((ISGCIMainFrame)parent).centerCanvas(p);
                return;
            }
        }
    }

    // ----------------------- MouseListener stuff --------------------------

    protected boolean mousePopup(MouseEvent event) {
        if (!event.isPopupTrigger())
            return false;

        event.consume();

        View v = getViewAt(event.getPoint());
        if (v == null)
            return true;
        if (v instanceof NodeView) {
            nodePopup.setNode((NodeView)v);
            nodePopup.show(this, event.getX(), event.getY());
        }
        if (v instanceof EdgeView) {
            edgePopup.setEdge((EdgeView)v);
            edgePopup.show(this, event.getX(), event.getY());
        }
        return true;
    }

    // ----------------------- Width calculator --------------------------
    class NodeWidthFunc implements IntFunction<Set<GraphClass>> {
        /** Return the width of node */
        public int execute(Set<GraphClass> node) {
            NodeView<Set<GraphClass>, DefaultEdge> view = getView(node);
            view.updateSize();
            return view.getSize().width;
        }
    }
}

/* EOF */
