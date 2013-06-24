/*
 * Popupmenu for EdgeViews
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/EdgePopup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;

import teo.XsltUtil;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;


public class EdgePopup extends JPopupMenu implements ActionListener {
    ISGCIMainFrame parent;
    JMenuItem deleteItem, infoItem;
//    EdgeView<Set<GraphClass>,DefaultEdge> view;
    
    //rework
    mxCell cell;

    public EdgePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        //deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);
    }

    
    /**
     * reworked for accepting mxICell edges
     * @param n
     */
    public void setEdge(mxCell cell) {
        this.cell = cell;
    }

    /**
     * reworked for working with mxICells
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
            JDialog d = InclusionResultDialog.newInstance(parent,
                NodePopup.searchName((mxCell) cell.getSource()),
                NodePopup.searchName((mxCell) cell.getTarget()));
            d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
        } 
    }
}

/* EOF */
