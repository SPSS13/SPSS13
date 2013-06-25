/*
 * Simple color for brighter/darker (AWT version bugged).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/SColor.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Color;
import javax.swing.*;

public class SColor {
    private static final double FACTOR = 0.7;

    private SColor() {}

    /**
     * Return a brighter version of c.
     */
    public static Color brighter(Color c) {
        return new Color(brighter(c.getRed()), brighter(c.getGreen()),
                brighter(c.getBlue()));
    }

    /*
     * Is c a shade of black?
     */
    public static boolean isGray(Color c) {
        return c.getRed() == c.getGreen()  &&  c.getRed() == c.getBlue();
    }

    public static float getGray(Color c) {
        return (float) c.getRed()/255;
    }

    /**
     * Return an html colour string #rrggbb for the given colour.
     */
    public static String getHtml(Color color) {
        return String.format("#%1$02X%2$02X%3$02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Brighten colour component c.
     */
    private static final int brighter(int c) {
        return Math.min(c + (int)((255-c) * FACTOR), 255);
    }
}

/* EOF */
