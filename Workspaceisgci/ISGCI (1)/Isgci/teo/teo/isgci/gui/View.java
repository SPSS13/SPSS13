/*
 * Something that can be displayed on a GraphCanvas.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/View.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.*;
import teo.isgci.xml.GraphMLWriter;

/*
 * An element that can be displayed on a GraphCanvas.
 */
public interface View<V,E> {
    /** Upper left corner */
    public Point getLocation();
    public void setLocation(Point p);
    /** upper left corner and size */
    public Rectangle getBounds();
    /** Is the given point inside? Meant for mouse clicks */
    public boolean contains(Point p);
    /** Paint the thing */
    public void paint(Graphics g, boolean drawUnproper);
    /** Mark/unmark it */
    public void setMark(boolean b);
    /** Set/unset shadow item (for dragging) */
    public void setShadow(boolean b);
    /** Set anchoring position for shadow */
    public void setShadowAnchor(Point p);
    /** Move the shadow so that the anchor is at the given Point */
    public void setShadowAnchorLocation(Point p);
    /** Get the bounds of the shadow */
    public Rectangle getShadowBounds();
    /** Get the top left corner of the shadow */
    public Point getShadowLocation();
    /** Paint the shadow */
    public void paintShadow(Graphics g);
    /** Write this to w */
    public void write(GraphMLWriter w) throws org.xml.sax.SAXException;
}

/* EOF */
