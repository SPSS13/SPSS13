/*
 * Allows the user to select graphclasses for drawing.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassSelectionDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gui;

import teo.isgci.gc.GraphClass;
import teo.isgci.db.DataSet;
import teo.isgci.grapht.*;
import java.io.IOException;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashSet;
import java.util.Collection;


/**
 * Display a list of graphclasses and change the drawing according to the
 * selection.
 */
public class GraphClassSelectionDialog extends JDialog
        implements ActionListener {
    
    protected ISGCIMainFrame parent;
    protected NodeList classesList;
    protected JCheckBox superCheck, subCheck;
    protected JButton addButton, removeButton, newButton, cancelButton;
    protected WebSearch search;

    public GraphClassSelectionDialog(ISGCIMainFrame parent) {
        super(parent, "Select Graph Classes", false);
        this.parent = parent;

        Container contents = getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        contents.setLayout(gridbag);

        c.insets = new Insets(5, 5, 0, 0);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        JLabel label = new JLabel("Draw:      ", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        label = new JLabel("     Filter:", JLabel.RIGHT);
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        contents.add(label);

        search = new WebSearch();
        search.addActionListener(this);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5,5,0,5);
        gridbag.setConstraints(search, c);
        contents.add(search);

        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        classesList = new NodeList(parent.latex);
        JScrollPane scroller = new JScrollPane(classesList);
        gridbag.setConstraints(scroller, c);
        contents.add(scroller);

        c.insets = new Insets(0, 5, 0, 0);
        c.weighty = 0.0;
        label = new JLabel("and their", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        superCheck = new JCheckBox("superclasses");
        gridbag.setConstraints(superCheck, c);
        contents.add(superCheck);

        subCheck = new JCheckBox("subclasses");
        gridbag.setConstraints(subCheck, c);
        contents.add(subCheck);

        JPanel buttonPanel = new JPanel();
        newButton = new JButton("New drawing");
        addButton = new JButton("Add to drawing");
        removeButton = new JButton("Remove from drawing");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(newButton);
        //buttonPanel.add(addButton);
        //buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(buttonPanel, c);
        contents.add(buttonPanel);
        addListeners();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        classesList.setListData(DataSet.getClasses());
        pack();
        setSize(500, 400);
    }


    protected void addListeners() {
        newButton.addActionListener(this);
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        cancelButton.addActionListener(this);
    }

    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Select the given node.
     */
    public void select(GraphClass node) {
        classesList.setSelectedValue(node, true);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == cancelButton) {
            closeDialog();
        } else if (source == newButton) {
            Cursor oldcursor = parent.getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            parent.graphCanvas.drawHierarchy(getNodes());
            
            for (Object o : classesList.getSelectedValues()) {
                GraphClass gc = (GraphClass) o;
                NodeView v = parent.graphCanvas.findNode(gc);
                if (v != null)
                    v.setNameAndLabel(gc.toString());
            }
            parent.graphCanvas.updateBounds();
            
            setCursor(oldcursor);
            closeDialog();
        } else if (source == search) {
            search.setListData(parent, classesList);
        }

    }
    
    
    /**
     * Returns a Collection with the classes (in DataSet.inclGraph) that are
     * selected by the current settings.
     */
    protected Collection<GraphClass> getNodes() {
        final HashSet<GraphClass> result = new HashSet<GraphClass>();
        boolean doSuper = superCheck.isSelected(),
                doSub = subCheck.isSelected();
       
        for (Object o : classesList.getSelectedValues()) {
            GraphClass gc = (GraphClass) o;
            result.add(gc);
            if (doSuper) {
                new RevBFSWalker<GraphClass,Inclusion>( DataSet.inclGraph,
                        gc, null, GraphWalker.InitCode.DYNAMIC) {
                    public void visit(GraphClass v) {
                        result.add(v);
                        super.visit(v);
                    }
                }.run();
            }
            if (doSub) {
                new BFSWalker<GraphClass,Inclusion>(DataSet.inclGraph,
                        gc, null, GraphWalker.InitCode.DYNAMIC) {
                    public void visit(GraphClass v) {
                        result.add(v);
                        super.visit(v);
                    }
                }.run();
            }
        }

        return result;
    }    
}


/* EOF */

