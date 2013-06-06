/*
 * Select the naming preference for graphclasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NamingDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;
import javax.swing.*;
import teo.isgci.db.Algo;

public class NamingDialog extends JDialog implements ActionListener {
    protected ISGCIMainFrame parent;
    protected ButtonGroup group;
    protected JRadioButton basicBox, derivedBox, forbiddenBox;
    protected JButton okButton, cancelButton;


    public NamingDialog(ISGCIMainFrame parent) {
        super(parent, "Naming preference", true);
        this.parent = parent;
        group = new ButtonGroup();
        Algo.NamePref mode = parent.graphCanvas.getNamingPref();
        LatexGraphics latex = ISGCIMainFrame.latex;
        Container contents = getContentPane();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        contents.setLayout(gridbag);

        c.insets = new Insets(5, 5, 0, 5);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        c.gridwidth = 1;
        basicBox = new JRadioButton("Basic", mode == Algo.NamePref.BASIC);
        group.add(basicBox);
        gridbag.setConstraints(basicBox, c);
        contents.add(basicBox);
        c.gridwidth = GridBagConstraints.REMAINDER;
        LatexLabel label = latex.newLabel("e.g. threshold");
        gridbag.setConstraints(label, c);
        contents.add(label);

        c.insets = new Insets(0, 5, 0, 5);
        c.gridwidth = 1;
        forbiddenBox = new JRadioButton("Forbidden subgraphs",
                mode == Algo.NamePref.FORBIDDEN);
        group.add(forbiddenBox);
        gridbag.setConstraints(forbiddenBox, c);
        contents.add(forbiddenBox);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = latex.newLabel("e.g. (P_4,2K_2,C_4)-free");
        gridbag.setConstraints(label, c);
        contents.add(label);
        
        c.gridwidth = 1;
        derivedBox = new JRadioButton("Derived",
                mode == Algo.NamePref.DERIVED);
        group.add(derivedBox);
        gridbag.setConstraints(derivedBox, c);
        contents.add(derivedBox);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = latex.newLabel("e.g. cograph \\cap split");
        gridbag.setConstraints(label, c);
        contents.add(label);


        JPanel p = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        p.add(okButton);
        p.add(cancelButton);
        c.insets = new Insets(5, 0, 5, 0);
        gridbag.setConstraints(p, c);
        contents.add(p);

        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }


    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == cancelButton) {
            closeDialog();
        } else if (source == okButton) {
            Object c = group.getSelection();
            Algo.NamePref pref = Algo.NamePref.BASIC;
            if (c == basicBox.getModel())
                pref = Algo.NamePref.BASIC;
            else if (c == forbiddenBox.getModel())
                pref = Algo.NamePref.FORBIDDEN;
            else if (c == derivedBox.getModel())
                pref = Algo.NamePref.DERIVED;
            parent.graphCanvas.setNamingPref(pref);
            closeDialog();
        }
    }

}

/* EOF */
