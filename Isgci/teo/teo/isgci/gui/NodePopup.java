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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import teo.XsltUtil;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Utility;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class NodePopup extends JPopupMenu implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2260105093000618855L;
    ISGCIMainFrame parent;
    private JMenuItem deleteItem, infoItem;
    private JMenuItem miShowSuperclasses;
    private JMenuItem miHideSuperclasses;
    private JMenuItem miShowSubclasses;
    private JMenuItem miHideSubclasses;
    JMenu nameItem;
    // rework
    mxCell cell;
    mxGraph graph;

    private static String CHANGENAME = "Name: ";

    public NodePopup(ISGCIMainFrame parent, mxGraph graph) {
        super();
        this.parent = parent;
        this.graph = graph;
        // deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        add(nameItem = new JMenu("Change name"));
        addSeparator();
        add(miShowSuperclasses = new JMenuItem("Show superclasses"));
        add(miHideSuperclasses = new JMenuItem("Hide superclasses"));
        addSeparator();
        add(miShowSubclasses = new JMenuItem("Show subclasses"));
        add(miHideSubclasses = new JMenuItem("Hide sublcasses"));
        miHideSubclasses.addActionListener(this);
        miHideSuperclasses.addActionListener(this);
        miShowSubclasses.addActionListener(this);
        miShowSuperclasses.addActionListener(this);
        infoItem.addActionListener(this);
    }

    public void setNode(mxCell cell) {
        this.cell = cell;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
            JDialog d = new GraphClassInformationDialog(parent,
                    searchName(cell));
            d.setLocation(50, 50);
            d.pack();
            d.setSize(800, 600);
            d.setVisible(true);
        } else if (event.getActionCommand().startsWith(CHANGENAME)) {
            String fullname = event.getActionCommand().substring(
                    CHANGENAME.length());
            graph.getModel().beginUpdate();
            try {
                GraphClassSet graphClassSet = (GraphClassSet)cell
                        .getValue();
                //search the label matching the selection
                GraphClass label = null;
                for(GraphClass gc : getAllClasses(cell)){

                    if(gc.toString().equals(fullname)){
                        label = gc;
                    }
                }
                //set the label
                graphClassSet.setLabel(label);
                //FIXME Layout machen
                graph.updateCellSize(cell);
            } finally {
                graph.getModel().endUpdate();
            }
        } else if (source == miShowSuperclasses) {
            parent.graphCanvas.drawSuperSub(parent.graphCanvas.getSuperNodes());
        } else if (source == miHideSuperclasses) {
            parent.graphCanvas.deleteSuperSub(parent.graphCanvas
                    .getSuperNodes());
        } else if (source == miShowSubclasses) {
            parent.graphCanvas.drawSuperSub(parent.graphCanvas.getSubNodes());
        } else if (source == miHideSubclasses) {
            parent.graphCanvas.deleteSuperSub(parent.graphCanvas.getSubNodes());
        }
    }

    public void show(Component orig, int x, int y) {
        // reworked
        LatexGraphics latex = ISGCIMainFrame.latex;
        Set<GraphClass> gcs = getAllClasses(cell);
        int i = 0;

        nameItem.removeAll();
        nameItem.setEnabled(gcs.size() != 1);
        JMenuItem[] mItem = new JMenuItem[gcs.size()];
        // FIXME sort and render latex properly
        for (GraphClass gc : gcs) {
            nameItem.add(mItem[i] = new JMenuItem((Utility
                    .getShortName(XsltUtil.latex(gc.toString())))));
            mItem[i].setActionCommand(CHANGENAME + gc.toString());
            mItem[i].addActionListener(this);
            i++;
        }

        super.show(orig, x, y);
    }

    /**
     * A method for getting all GraphClasses a mxCell contains of.
     * 
     * @param c
     *            the mxCell whose GraphClasses should be returned
     * @return A Set of all GraphClasses contained in a mxCell
     * @author Fabian Vollmer
     * @date 24.06.2013
     */
    private Set<GraphClass> getAllClasses(mxCell c) {
        Set<GraphClass> result = new HashSet<GraphClass>();
        result = ((GraphClassSet)c.getValue()).getSet();
        return result;
    }

    /**
     * Searches for the GraphClass represented by the name of a mxCell which is
     * a Latex String
     * 
     * @param c
     *            the mxCell that includes the name of the GraphClass that
     *            should be searched
     * @return the GraphClass represented by the Name
     * 
     * @author Fabian Vollmer
     * @date 24.06.2013
     */
    public static GraphClass searchName(mxCell c) {
       return ((GraphClassSet)c.getValue()).getLabel();
    }
}

/* EOF */
