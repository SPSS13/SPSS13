/*
 * Popupmenu for NodeViews
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodePopup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.XsltUtil;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.Inclusion;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class NodePopup extends JPopupMenu implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2260105093000618855L;
    ISGCIMainFrame parent;
    private JMenuItem infoItem;
    private JMenuItem miShowSuperclasses;
    private JMenuItem miHideSuperclasses;
    private JMenuItem miShowSubclasses;
    private JMenuItem miHideSubclasses;
    private JMenuItem miShowDetails;
    private JMenuItem miAddSuperclasses;
    private JMenuItem miAddSubclasses;
    private JMenuItem miAddNeighbours;

    JMenu nameItem;
    // rework
    mxCell cell;
    mxGraph graph;

    private static String CHANGENAME = "Name: ";
    private HashMap<Set<GraphClass>, Object> map;

    public NodePopup(ISGCIMainFrame parent, mxGraph graph,
            HashMap<Set<GraphClass>, Object> map) {
        super();
        this.parent = parent;
        this.graph = graph;
        this.map = map;
        // deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        add(miShowDetails = new JMenuItem("Show sidebar"));
        addSeparator();
        add(nameItem = new JMenu("Change name"));
        addSeparator();
        add(miHideSuperclasses = new JMenuItem("superclasses - hide"));
        add(miShowSuperclasses = new JMenuItem("superclasses - show"));
        add(miAddSuperclasses = new JMenuItem("superclasses - add"));
        addSeparator();
        add(miHideSubclasses = new JMenuItem("subclasses - hide"));
        add(miShowSubclasses = new JMenuItem("subclasses - show"));
        add(miAddSubclasses = new JMenuItem("subclasses - add"));
        addSeparator();
        add(miAddNeighbours = new JMenuItem("neighbours - add"));

        // register Listeners
        miHideSubclasses.addActionListener(this);
        miHideSuperclasses.addActionListener(this);
        miShowSubclasses.addActionListener(this);
        miShowSuperclasses.addActionListener(this);
        miAddNeighbours.addActionListener(this);
        miShowDetails.addActionListener(this);
        miAddSubclasses.addActionListener(this);
        miAddSuperclasses.addActionListener(this);
        infoItem.addActionListener(this);
    }

    /**
     * returns true if any superclass of the cell is visible; returns false if
     * all superclass of the cell are invisible; returns false if there are no
     * superclasses
     * 
     * @return
     * @author leo
     */
    private boolean hideSuperclassesSwitch() {
        return checkVisibility(true, true);
    }

    /**
     * returns true if any superclass of the cell is invisible; returns false if
     * all superclass of the cell are visible; returns false if there are no
     * superclasses
     * 
     * @return
     * @author leo
     */
    private boolean showSuperclassesSwitch() {
        return checkVisibility(true, false);
    }

    /**
     * returns true if any subclass of the cell is visible; returns false if all
     * subclass of the cell is invisible; returns false if there are no
     * subclasses
     * 
     * @return
     * @author leo
     */
    private boolean hideSubclassesSwitch() {
        return checkVisibility(false, true);
    }

    /**
     * returns true if any subclass of the cell is invisible; returns false if
     * all subclass of the cell are visible; returns false if there are no
     * subclasses
     * 
     * @return
     * @author leo
     */
    private boolean showSubclassesSwitch() {
        return checkVisibility(false, false);
    }

    /**
     * Utility method to check weather super or subclasses are hidden or
     * visible, use someSuperclassesVisible(), someSuperclassesInvisible(),
     * someSubclassesVisible() and someSubclassesInvisible() for easy access.
     * 
     * (superclassesORsubclasses ? superclasses : subclasses) (visibility ?
     * visible : invisible)
     * 
     * @param superclassesORsubclasses
     * @param visibility
     * @return
     */
    private boolean checkVisibility(boolean superclassesORsubclasses,
            boolean visibility) {
        // if the cell has associated edges in the given direction
        Object[] edges = new Object[]{};
//                (superclassesORsubclasses ? graph
//                .getIncomingEdges(cell) : graph.getOutgoingEdges(cell));
//        // there is no sub- or supernode
//        if (edges == null || edges.length == 0) {
//            System.out.println("no super or subclasses");
//            return false;
//        }
        LinkedList<Object> queue = new LinkedList<Object>();
        // add root cell
        queue.add(cell);
        // find all subclasses with a BFS Search
        while (!queue.isEmpty()) {
            edges = (superclassesORsubclasses ? graph.getIncomingEdges(queue
                    .pollFirst()) : graph.getOutgoingEdges(queue.pollFirst()));
            for (Object edge : edges) {
                mxCell temp = (mxCell)(superclassesORsubclasses ? ((mxCell)edge)
                        .getSource() : ((mxCell)edge).getTarget());
                /*
                 * debug Code System.out.println((superclassesORsubclasses ?
                 * "Super" : "Sub") + "Node: " + (visibility ?
                 * ((GraphClassSet)temp.getValue()) .getLabel().toString() :
                 * ((GraphClassSet)temp .getValue()).getLabel().toString()) +
                 * (visibility ? "; is visible " : "; is invisible ") +
                 * (visibility ? temp.isVisible() : temp.isVisible()));
                 */
                // check if the cell is visible
                if ((visibility ? temp.isVisible() : !temp.isVisible()))
                    return true;
                // otherwise add it to the queue
                queue.add((superclassesORsubclasses ? ((mxCell)edge)
                        .getSource() : ((mxCell)edge).getTarget()));
            }
        }
        return false;
    }

    /**
     * checks if all direct superclasses of the associated cell are present in
     * the graph and visible
     * 
     * @return
     * @author leo
     */
    private boolean superclassesAddable() {
        Set<GraphClass> classes = ((GraphClassSet)cell.getValue()).getSet();
        // get all neighbours
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = getNeighbours(classes);
        // check for all edges if they are already drawn
        for (DefaultEdge edge : edgegraph.incomingEdgesOf(classes)) {
            mxCell source = (mxCell)map.get(edgegraph.getEdgeSource(edge));
            // the superclass is not present
            if (source == null)
                return true;
            // the superclass is invisible
            if (!source.isVisible())
                return true;
        }
        // all superclasses are present and visible
        return false;
    }

    /**
     * checks if all direct sub- and superclasses of the associated cell are
     * present in the graph and visible
     * 
     * @return
     * @author leo
     */
    @SuppressWarnings("unused")
    private boolean neighboursAddable() {
        Set<GraphClass> classes = ((GraphClassSet)cell.getValue()).getSet();
        // get all neighbours
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = getNeighbours(classes);
        // check for all edges if they are already drawn
        for (DefaultEdge edge : edgegraph.outgoingEdgesOf(classes)) {
            mxCell target = (mxCell)map.get(edgegraph.getEdgeTarget(edge));
            // the subclass is not present
            if (target == null)
                return true;
            // the subclass is invisible
            if (!target.isVisible())
                return true;
        }
        // -> all subclasses are present and visible
        // check for all incoming edges if they are already drawn
        for (DefaultEdge edge : edgegraph.incomingEdgesOf(classes)) {
            mxCell source = (mxCell)map.get(edgegraph.getEdgeSource(edge));
            // the superclass is not present
            if (source == null)
                return true;
            // the superclass is invisible
            if (!source.isVisible())
                return true;
        }
        // -> all superclasses are present and visible
        return false;
    }

    /**
     * checks if all direct subclasses of the associated cell are present in the
     * graph and visible
     * 
     * @return
     * @author leo
     */
    private boolean subclassesAddable() {
        Set<GraphClass> classes = ((GraphClassSet)cell.getValue()).getSet();
        // get all neighbours
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> edgegraph = getNeighbours(classes);
        // check for all outgoing edges if they are already drawn
        for (DefaultEdge edge : edgegraph.outgoingEdgesOf(classes)) {
            mxCell target = (mxCell)map.get(edgegraph.getEdgeTarget(edge));
            // the subclass is not present
            if (target == null)
                return true;
            // the subclass is invisible
            if (!target.isVisible())
                return true;
        }
        // all subclasses are present and visible
        return false;
    }

    /**
     * Utility method. Returns a SimpleDirectedGraph including all direct
     * neighbours of the given node
     * 
     * @param classes
     * @return
     */
    private SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> getNeighbours(
            Set<GraphClass> classes) {
        HashSet<GraphClass> nodes = new HashSet<GraphClass>();
        for (GraphClass gc : classes) {
            if (!DataSet.inclGraph.edgesOf(gc).isEmpty()) {
                for (Inclusion inc : DataSet.inclGraph.edgesOf(gc)) {
                    nodes.add(inc.getSuper());
                    nodes.add(inc.getSub());
                }
            }
        }
        return Algo.createHierarchySubgraph(nodes);
    }

    public void setMenuItemSwitches(JMenuItem miShowSuperclasses,
            JMenuItem miHideSuperclasses, JMenuItem miShowSubclasses,
            JMenuItem miHideSubclasses, JMenuItem miShowDetails,
            JMenuItem miAddSuperclasses, JMenuItem miAddSubclasses,
            JMenuItem miAddNeighbours) {
        cell = parent.graphCanvas.getSelectedCell();

        // toggle menus
        //sidebar
        miShowDetails.setEnabled(true);
        // superclasses
        boolean superclassesAddable = superclassesAddable();
        boolean subclassesAddable = subclassesAddable();
        miHideSuperclasses.setEnabled(hideSuperclassesSwitch());      
        miShowSuperclasses.setEnabled(showSuperclassesSwitch());
        miAddSuperclasses.setEnabled(superclassesAddable);
        // subclasses
        miHideSubclasses.setEnabled(hideSubclassesSwitch());
        miShowSubclasses.setEnabled(showSubclassesSwitch());
        miAddSubclasses.setEnabled(subclassesAddable);
        // neighbours
        miAddNeighbours.setEnabled(superclassesAddable || subclassesAddable);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
            JDialog d = new GraphClassInformationDialog(parent,
                    ((GraphClassSet)cell.getValue()).getLabel());
            d.setLocation(50, 50);
            d.pack();
            d.setSize(800, 600);
            d.setVisible(true);
        } else if (event.getActionCommand().startsWith(CHANGENAME)) {
            String fullname = event.getActionCommand().substring(
                    CHANGENAME.length());
            graph.getModel().beginUpdate();
            try {
                GraphClassSet graphClassSet = (GraphClassSet)cell.getValue();
                // search the label matching the selection
                GraphClass label = null;
                for (GraphClass gc : ((GraphClassSet)cell.getValue()).getSet()) {
                    if (gc.toString().equals(fullname)) {
                        label = gc;
                    }
                }
                // set the label
                graphClassSet.setLabel(label);
                graph.updateCellSize(cell);
                parent.graphCanvas.animateGraph();
            } finally {
                graph.getModel().endUpdate();
            }
        } else if (source == miShowDetails) {
            parent.visibleHaken.setState(true);
            parent.getContentPane().add("West", parent.sidebar);
            parent.button2.setVisible(false);
            parent.sidebar.setVisible(true);
            parent.graphCanvas.setSidebarConent();
        } else if (source == miShowSuperclasses) {
            parent.graphCanvas.drawSuperSub(parent.graphCanvas
                    .getSuperNodes(cell));
        } else if (source == miHideSuperclasses) {
            parent.graphCanvas.deleteSuperSub(parent.graphCanvas
                    .getSuperNodes(cell));
        } else if (source == miShowSubclasses) {
            parent.graphCanvas.drawSuperSub(parent.graphCanvas
                    .getSubNodes(cell));
        } else if (source == miHideSubclasses) {
            parent.graphCanvas.deleteSuperSub(parent.graphCanvas
                    .getSubNodes(cell));
        } else if (source == miAddNeighbours) {
            parent.graphCanvas.drawNeighbours(cell);
        } else if (source == miAddSubclasses) {
            parent.graphCanvas.addSubclasses(cell);
        } else if (source == miAddSuperclasses) {
            parent.graphCanvas.addSuperclasses(cell);
        }
    }

    /**
     * updates the visibility of the menuitems and the content of the nameItem
     * 
     * @author leo
     */
    public void show(Component orig, int x, int y) {
        setMenuItemSwitches(miShowSuperclasses, miHideSuperclasses,
                miShowSubclasses, miHideSubclasses, miShowDetails,
                miAddSuperclasses, miAddSubclasses, miAddNeighbours);
        Set<GraphClass> gcs = ((GraphClassSet)cell.getValue()).getSet();
        int i = 0;
        nameItem.removeAll();
        nameItem.setEnabled(gcs.size() != 1);
        JMenuItem[] mItem = new JMenuItem[gcs.size()];
        for (GraphClass gc : gcs) {
            nameItem.add(mItem[i] = new JMenuItem(XsltUtil.latexTruncated(gc
                    .toString())));
            mItem[i].setActionCommand(CHANGENAME + gc.toString());
            mItem[i].addActionListener(this);
            i++;
        }

        super.show(orig, x, y);
    }

}

/* EOF */
