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
import java.util.Set;
import javax.swing.*;
import org.jgrapht.graph.DefaultEdge;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;


public class EdgePopup extends JPopupMenu implements ActionListener {
    ISGCIMainFrame parent;
    JMenuItem deleteItem, infoItem;
    EdgeView<Set<GraphClass>,DefaultEdge> view;

    public EdgePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        //deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);
    }

    public void setEdge(EdgeView n) {
        view = n;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
            JDialog d = InclusionResultDialog.newInstance(parent,
                DataSet.getClass(
                    parent.graphCanvas.getView(view.getFrom()).getFullName()),
                DataSet.getClass(
                    parent.graphCanvas.getView(view.getTo()).getFullName()));
            d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
        } 
    }
}

/* EOF */
