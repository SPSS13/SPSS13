/*
 * Allows the user to select two graphclasses and check their relation.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/CheckInclusionDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import teo.isgci.db.DataSet;
import teo.isgci.db.Algo;
import java.io.IOException;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.Vector;

/**
 * The dialog the checks for an inclusion between two graphclasses.
 * It contains two lists in single selection mode.
 */
public class CheckInclusionDialog extends JDialog
        implements ActionListener, ListSelectionListener {
    
    protected ISGCIMainFrame parent;
    protected NodeList firstList, secondList;
    protected JButton cancelButton;
    protected JButton inclusionCheckButton;
    protected WebSearch firstSearch, secondSearch;
    
    /** Create and display the dialog
     * @param parent the parent of the dialog
     */
    public CheckInclusionDialog(ISGCIMainFrame parent) {
        super(parent, "Find Relation", false);
        this.parent = parent;

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        Container content = getContentPane();
        content.setLayout(gridbag);

        c.gridwidth = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 0, 0);
        JLabel label = new JLabel("First class:", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        content.add(label);

        label = new JLabel("     Filter:", JLabel.RIGHT);
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        content.add(label);

        firstSearch = new WebSearch();
        firstSearch.addActionListener(this);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5,5,0,5);
        gridbag.setConstraints(firstSearch, c);
        content.add(firstSearch);

        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.weighty = 1.0;
        firstList = new NodeList(parent.latex);
        firstList.setListData(DataSet.getClasses());
        firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(firstList);
        gridbag.setConstraints(scroller, c);
        content.add(scroller);

        label = new JLabel("Second class:");
        c.insets = new Insets(5, 5, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        content.add(label);

        label = new JLabel("     Filter:", JLabel.RIGHT);
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        content.add(label);

        secondSearch = new WebSearch();
        secondSearch.addActionListener(this);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5,5,0,5);
        gridbag.setConstraints(secondSearch, c);
        content.add(secondSearch);

        secondList = new NodeList(parent.latex);
        secondList.setListData(DataSet.getClasses());
        secondList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroller = new JScrollPane(secondList);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        gridbag.setConstraints(scroller, c);
        content.add(scroller);
        JPanel drawPanel = new JPanel();
        cancelButton = new JButton("Close");
        inclusionCheckButton = new JButton("Find relation");
        drawPanel.add(inclusionCheckButton);
        drawPanel.add(cancelButton);
        c.weighty = 0.0;
        gridbag.setConstraints(drawPanel, c);
        content.add(drawPanel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
        handleButtons();

    }

    /** Sets the event listeners for this dialog */
    protected void addListeners() {
        cancelButton.addActionListener(this);
        inclusionCheckButton.addActionListener(this);
        firstList.addListSelectionListener(this);
        secondList.addListSelectionListener(this);
    }


    /** Enables/disables the buttons depending on whether any items are
     * selected
     */
    public void handleButtons() {
        if (firstList.getSelectedValue() == null ||
                secondList.getSelectedValue() == null) {
            inclusionCheckButton.setEnabled(false);
        } else {
            inclusionCheckButton.setEnabled(true);
        }
    }

    
    /** Eventhandlers for the buttonclicks.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        if (e.getSource() instanceof NodeList)
            handleButtons();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == inclusionCheckButton) {
            inclusionCheck();
        } else if (source == cancelButton) {
            closeDialog();
        } else if (source == firstSearch) {
            firstSearch.setListData(parent, firstList);
        } else if (source == secondSearch) {
            secondSearch.setListData(parent, secondList);
        }
    }


    /** Close the dialog and release resources */
    public void closeDialog() {
        setVisible(false);
        dispose();
    }

    
    /** Checks whether the selected classes are related and displays the
     * result of this check in a new dialog.
     */
    public void inclusionCheck() {
        InclusionResultDialog.newInstance(parent,
                firstList.getSelectedNode(), secondList.getSelectedNode()
        ).setVisible(true);
    }

}

/* EOF */
