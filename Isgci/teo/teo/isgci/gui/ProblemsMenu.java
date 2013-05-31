/*
 * Menu to select a preferred problem (for colouring).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ProblemsMenu.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

//import.java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import teo.isgci.db.*;
import teo.isgci.problem.*;

public class ProblemsMenu extends JMenu implements ActionListener {
    protected Vector items;
    protected ISGCIMainFrame parent;
    protected ButtonGroup group;

    public ProblemsMenu(ISGCIMainFrame parent, String label) {
        super(label);
        this.parent = parent;
        items = new Vector();
        group = new ButtonGroup();

        addRadio("None", true);

        for (int i = 0; i < DataSet.problems.size(); i++)
            addRadio(((Problem)DataSet.problems.elementAt(i)).getName(),false);
    }

    /**
     * Add a radiobutton to this menu.
     */
    private void addRadio(String s, boolean def) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(s, def);
        item.setActionCommand(s);
        item.addActionListener(this);
        add(item);
        group.add(item);
        items.addElement(item);
        
    }

    public void actionPerformed(ActionEvent event) {
        parent.graphCanvas.setProblem(
                DataSet.getProblem(event.getActionCommand()));
    }
}

/* EOF */

