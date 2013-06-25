/*
 * A "smart" Graphics with special methods for drawing graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/SmartGraphics.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Graphics;
import java.util.Vector;

/**
 * A "smart" Graphics with special methods for drawing graphs.
 */
public abstract class SmartGraphics extends Graphics {
    /**
     * Draw a node filled with the current color.
     */
    public abstract void drawNode(int x, int y, int width, int height);

    /**
     * Draw an arrow (more or less) through the given points.
     * @param vec Points of the arrow
     * @param unproper mark the edge as unproper?
     */
    public abstract void drawArrow(Vector vec, boolean unproper);

}

/* EOF */
