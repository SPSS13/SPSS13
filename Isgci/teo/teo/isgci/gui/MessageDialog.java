/*
 * Class for message dialogs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/MessageDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import javax.swing.JOptionPane;

public class MessageDialog {
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "ISGCI Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message,
            String yes, String no) {
        String[] options = {yes, no};
        return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
                parent, message, "ISGCI Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);

    }

}

/* EOF */
