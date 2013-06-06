/*
 * Menu that selects the scale.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ScaleMenu.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gui;

import java.awt.event.*;
import javax.swing.*;

public class ScaleMenu extends JMenu implements ItemListener {
/*
    protected CheckboxMenuItem mi25, mi50, mi75, mi100, mi150;
    protected float scale;

    public ScaleMenu() {
        super("Scale");
        add(mi25 = new CheckboxMenuItem("25 %"));
        add(mi50 = new CheckboxMenuItem("50 %"));
        add(mi75 = new CheckboxMenuItem("75 %"));
        add(mi100 = new CheckboxMenuItem("100 %", true));
        add(mi150 = new CheckboxMenuItem("150 %"));
        scale = 1.0f;
        mi25.addItemListener(this);
        mi50.addItemListener(this);
        mi75.addItemListener(this);
        mi100.addItemListener(this);
        mi150.addItemListener(this);
    }

    public float getScale()
    {
        return scale;
    }
    
*/
    /** Listen for an item to be selected
     */
    public void itemStateChanged(ItemEvent event) {
/*        Object object = event.getSource();
        if (object == mi25) {
            scale = 0.25f;
            unselectAll();
            mi25.setState(true);
        } else if (object == mi50) {
            scale = 0.5f;
            unselectAll();
            mi50.setState(true);
        } else if (object == mi75) {
            scale = 0.75f;
            unselectAll();
            mi75.setState(true);
        } else if (object == mi100) {
            scale = 1.0f;
            unselectAll();
            mi100.setState(true);
        } else if (object == mi150) {
            scale = 1.5f;
            unselectAll();
            mi150.setState(true);
        }
*/
    }

    /** Unselects all items.
     */
/*    protected void unselectAll() {
        mi25.setState(false);
        mi50.setState(false);
        mi75.setState(false);
        mi100.setState(false);
        mi150.setState(false);
    }
*/
}

/* EOF */
