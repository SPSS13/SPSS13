/*
 * Display information about ISGCI.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/AboutDialog.java,v 2.1 2011/09/29 08:38:57 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.event.*;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;
import javax.swing.*;
import teo.isgci.db.*;

public class AboutDialog extends JDialog implements ActionListener {
    protected ISGCIMainFrame parent;
    protected JButton okButton;


    public AboutDialog(ISGCIMainFrame parent) {
        super(parent, "About ISGCI", true);
        this.parent = parent;

        Insets insetsZero = new Insets(0,0,0,0);
        Insets insetsTopMargin = new Insets(10,0,0,0);
        Font big = new Font("serif", Font.BOLD, 18);
        Container content = getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        content.setLayout(gridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(10,10,0,10); 
        
        JLabel label1=new JLabel(
                "Information System on Graph Classes and their Inclusions",
                JLabel.CENTER);
        label1.setFont(big);
        gridbag.setConstraints(label1, c);
        content.add(label1);
                
        c.insets = insetsTopMargin;
        JLabel label2 = new JLabel("Version 3.1", JLabel.CENTER);
        label2.setFont(big);
        gridbag.setConstraints(label2, c);
        content.add(label2);
        
        JLabel label2a = new JLabel("by H.N. de Ridder et al.",JLabel.CENTER);
        gridbag.setConstraints(label2a, c);
        content.add(label2a);
        
        c.insets = insetsZero;
        JLabel label2b = new JLabel("uses the JGraphT library",JLabel.CENTER);
        gridbag.setConstraints(label2b, c);
        content.add(label2b);
        
        c.insets = insetsTopMargin;
        JLabel label7 = new JLabel(DataSet.getNodeCount()+" classes, "+
                DataSet.getEdgeCount()+ " inclusions", JLabel.CENTER);
        gridbag.setConstraints(label7, c);
        content.add(label7);
        
        c.insets = insetsZero;
        JLabel label5 = new JLabel("Database generated : "+
                DataSet.getDate(), JLabel.CENTER);
        gridbag.setConstraints(label5, c);
        content.add(label5);
        
        c.insets = insetsTopMargin;
        JLabel label3=new JLabel("http://www.graphclasses.org", JLabel.CENTER);
        gridbag.setConstraints(label3, c);
        content.add(label3);
        
        c.insets = new Insets(10, 0, 5, 0);
        okButton = new JButton(" OK ");
        gridbag.setConstraints(okButton, c);
        content.add(okButton);


        okButton.addActionListener(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }


    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == okButton) {
            closeDialog();
        }
    }
}

/* EOF */
