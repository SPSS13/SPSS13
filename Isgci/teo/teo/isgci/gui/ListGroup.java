/*
 * A group of lists of which only one item can be selected.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ListGroup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import javax.swing.*;
import javax.swing.event.*;
import teo.isgci.gc.GraphClass;

/**
 * A group of lists in which only one item can be selected.
 */
class ListGroup implements ListSelectionListener {
    protected int used;
    protected JList[] lists;
    protected boolean busy;     // To prevent event handling causing new evs.

    /**
     * Make a new ListGroup of the specified number of Lists.
     */
    public ListGroup(int size) {
        lists = new JList[size];
        used = 0;
        busy = false;
    }

    /**
     * Add list to this ListGroup.
     */
    public void add(JList list) {
        lists[used++] = list;
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
    }

    /**
     * Return the selected item or null if no item is selected.
     */
    public Object getSelectedItem() {
        Object res = null;

        for (int i = 0; i < lists.length; i++) {
            res = lists[i].getSelectedValue();
            if (res != null)
                return res;
        }
        return null;
    }

    /**
     * Return the selected node or null if no node is selected.
     */
    public GraphClass getSelectedNode() {
        return (GraphClass) getSelectedItem();
    }


    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()  &&  !busy) {
            Object source = event.getSource();
            busy = true;
            for (int i = 0; i < lists.length; i++) {
                if (lists[i] != source  &&  !lists[i].isSelectionEmpty())
                    lists[i].clearSelection();
            }
            busy = false;
        }
    }

}

/* EOF */
