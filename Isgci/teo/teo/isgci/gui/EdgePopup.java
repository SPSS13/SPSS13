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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mxgraph.model.mxCell;

public class EdgePopup extends JPopupMenu implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -1056253319637812792L;
    ISGCIMainFrame parent;
    JMenuItem deleteItem, infoItem;
    // EdgeView<Set<GraphClass>,DefaultEdge> view;

    // rework
    mxCell cell;

    public EdgePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        // deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);
    }

    /**
     * reworked for accepting mxICell edges
     * 
     * @param n
     */
    public void setEdge(mxCell cell) {
        this.cell = cell;
    }

    /**
     * reworked for working with mxICells FIXME geht nicht auf allen klassen
     * bsp: 0,3 colorable und claw-free \cup odd anti-hole-free \cup tripartite
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
            GraphClassSet edgesource = ((GraphClassSet)((mxCell)cell
                    .getTarget()).getValue());
            GraphClassSet edgetarget = ((GraphClassSet)((mxCell)cell
                    .getSource()).getValue());
            JDialog d = InclusionResultDialog.newInstance(parent,
                    edgesource.getLabel(), edgetarget.getLabel());

            d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
        }
    }
}

/* EOF */
