/*
 * A JList filled with ISGCINodes
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodeList.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * A JList filled with ISGCINodes, that are displayed with formatted html.
 */
public class NodeList<V> extends JList<V> {
    /**
     * 
     */
    private static final long serialVersionUID = -8851203581820743547L;
    LatexGraphics latex;


    /**
     * Create a new list with the given nodes in it.
     */
    public NodeList(LatexGraphics latex, Vector<V> nodes) {
        super(nodes);
        init(latex);
    }


    /**
     * Create a new list.
     */
    public NodeList(LatexGraphics latex) {
        super();
        init(latex);
    }

    protected void init(LatexGraphics latex) {
        this.latex = latex;
        setCellRenderer(new NodeListCellRenderer());
        setFont(latex.getFont());
    }


    /**
     * Return the number of nodes in this list.
     */
    public int getElementCount() {
        return getModel().getSize();
    }


    /**
     * Replace all nodes in this list by the given data.
     */
    public void setListData(Iterator<?> data) {
        Vector<Object> v = new Vector<Object>();
        while (data.hasNext())
            v.add(data.next());
        setListData(v);
    }


    /**
     * Replace all nodes in this list by the given data.
     */
    public void setListData(Collection<?> data) {
        Vector v = new Vector(data);
        setListData(v);
    }


    /**
     * Return the smallest selected node.
     */
    public V getSelectedNode() {
        return  getSelectedValue();
    }


    /**
     * The renderer for nodes: We use a label with html code in it.
     */
    protected class NodeListCellRenderer implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            LatexLabel label =
                    latex.newLabel(value.toString());
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            label.setEnabled(list.isEnabled());
            label.setFont(list.getFont());
            //setOpaque(this);
            return label;
        }
    }
}

/* EOF */
