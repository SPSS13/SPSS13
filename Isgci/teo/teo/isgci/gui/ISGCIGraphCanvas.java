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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
	private static final long serialVersionUID = 5317514150138896189L;
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
		super(parent, parent.latex, new ISGCIVertexFactory(), null);
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
		((mxGraphModel) graph.getModel()).clear();
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
						graphClasses, 20, 20, 80, 30,
						"shape=rectangle;fontColor=black");
				map.put(gc, vertex);
				graph.updateCellSize(vertex);
				((mxCell) vertex).setConnectable(false);
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
			if (drawUnproper) {
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
		((mxGraphComponent) this.parent.drawingPane).refresh();
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
			@SuppressWarnings("unchecked")
			GraphClassSet<GraphClass> graphClassSet = (GraphClassSet<GraphClass>) ((mxCell) cell)
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
			@SuppressWarnings("unchecked")
			GraphClassSet<GraphClass> graphClassSet = (GraphClassSet<GraphClass>) ((mxCell) cell)
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
				@SuppressWarnings("unchecked")
				// cast is safe
				GraphClassSet<GraphClass> gcs = ((GraphClassSet<GraphClass>) ((mxCell) cell)
						.getValue());
				if (gcs.getLabel() != null) {
					gcs.setLabel(null);
					//graph.getModel().setValue(cell, gcs);

				}
				graph.updateCellSize(cell, true);
			}

		} finally {
			//graph.refresh();
			
			layout.run(graph.getChildCells(graph.getDefaultParent()));
			graph.getModel().endUpdate();
			graph.setCellsResizable(false);
		}
	}

	/**
	 * Find the NodeView for the given graph class or null if not found shortcut
	 * method to work with NodeViews, should be replaced
	 */
	@SuppressWarnings("unchecked")
	// the cast is save
	public NodeView<Set<GraphClass>, DefaultEdge> findNode(GraphClass gc) {
		return (NodeView<Set<GraphClass>, DefaultEdge>) findNode(gc, false);
	}

	/**
	 * Find the cell for the given graph class or null if not found
	 * 
	 * @author leo
	 * @date 14.06
	 * @annotation should work now
	 */
	public Object findNode(GraphClass gc, boolean mxCellOrNodeView) {
		if (mxCellOrNodeView) {
			for (Object cell : graph.getChildVertices(graph.getDefaultParent())) {
				@SuppressWarnings("unchecked")
				// the cast should be safe
				Set<GraphClass> set = ((GraphClassSet<GraphClass>) ((mxCell) cell)
						.getValue()).getSet();
				if (set.contains(gc)) {
					return (mxCell) cell;
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
			graph.getModel().endUpdate();
			((mxGraphComponent) parent.drawingPane).refresh();
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
			if (((mxCell) cell).isEdge()) {
				if (drawUnproper) {
					@SuppressWarnings("unchecked")
					GraphClassSet<GraphClass> source = (GraphClassSet<GraphClass>) ((mxCell) cell)
							.getSource().getValue();
					@SuppressWarnings("unchecked")
					GraphClassSet<GraphClass> target = (GraphClassSet<GraphClass>) ((mxCell) cell)
							.getTarget().getValue();
					List<Inclusion> path = GAlg.getPath(DataSet.inclGraph,
							source.getSet().iterator().next(), target.getSet()
									.iterator().next());
					if (!(Algo.isPathProper(path) || Algo.isPathProper(Algo
							.makePathProper(path)))) {
						((mxCell) cell).setStyle("startArrow=open");
					}
				} else {
					((mxCell) cell).setStyle("startArrow=none");
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
				mxCell cell = (mxCell) map.get(gc);
				graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
						mxHtmlColor.getHexColorString(complexityColor(gc)),
						new Object[] { cell });

			}
		} finally {
			((mxGraphComponent) parent.drawingPane).refresh();
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
				((ISGCIMainFrame) parent).centerCanvas(p);
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
				GraphClass gc = (GraphClass) o;
				mxCell cell = (mxCell) findNode(gc, true);
				if (cell != null) {
					@SuppressWarnings("unchecked")
					GraphClassSet<GraphClass> graphClassSet = (GraphClassSet<GraphClass>) cell
							.getValue();
					graphClassSet.setLabel(gc.toString());
					graph.updateCellSize(cell);
				}
			}
		} finally {
			graph.getModel().endUpdate();
			((mxGraphComponent) parent.drawingPane).refresh();
		}

	}

	public void markOnly(mxCell cell) {
		graph.getSelectionModel().setCell(cell);
	}

	/**
	 * like in the mxGraphComponent
	 * 
	 * @param cell
	 * @author leo
	 * @date 14.06
	 */
	public void centerNode(mxCell cell) {

		mxCellState state = graph.getView().getState(cell);

		if (state != null) {
			mxRectangle bounds = state;

			bounds = (mxRectangle) bounds.clone();

			bounds.setX(bounds.getCenterX() - getWidth() / 2);
			bounds.setWidth(getWidth());
			bounds.setY(bounds.getCenterY() - getHeight() / 2);
			bounds.setHeight(getHeight());

			scrollRectToVisible(bounds.getRectangle());
			System.out.println(bounds.getRectangle().toString());
		}
	}

	// ----------------------- MouseListener stuff --------------------------

	public void registerMouseListener(mxGraphComponent pane) {
		pane.getGraphControl().addMouseListener(this);
		pane.getGraphControl().addMouseWheelListener(this);
	}

	/**
	 * Method for showing a popup menu if a node or an edge is rightclicked
	 */
	protected boolean mousePopup(MouseEvent event) {
		if (!event.isPopupTrigger()) {
			return false;
		} else {
			event.consume();
			Object cell = ((mxGraphComponent) parent.drawingPane).getCellAt(
					event.getX(), event.getY());
			if (cell != null) {
				mxCell c = (mxCell) cell;
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		e.consume();
		Point p = e.getPoint();
		Point current = graph.getView().getTranslate().getPoint();
		System.out.println(current);
		graph.getView().revalidate();
		if (e.getWheelRotation() < 0) {
			graph.getView().scaleAndTranslate(graph.getView().getScale() * 1.1,
					current.x - Math.abs(p.x) / 10,
					-Math.abs(current.y + Math.abs(p.y) / 10));
			System.out.println(graph.getView().getTranslate());
		} else {
			// ((mxGraphComponent) parent.drawingPane).zoom(0.9);
			// graph.getView().setTranslate(new mxPoint((-p.x)/10, (-p.y)/10));
			graph.getView().scaleAndTranslate(graph.getView().getScale() * 0.9,
					current.x + Math.abs(p.x) / 10,
					-Math.abs(current.y + Math.abs(p.y) / 10));
			System.out.println(graph.getView().getTranslate());
		}

	}

}

/* EOF */
