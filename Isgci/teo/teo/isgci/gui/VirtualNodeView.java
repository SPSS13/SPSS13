/*
 * View of a virtual node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/VirtualNodeView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Graphics;

/**
 * Displays a virtual node: it has no dimension and is not painted.
 */
public class VirtualNodeView<V,E> extends NodeView<V,E> {

    public VirtualNodeView(GraphView<V,E> parent, V node) {
        super(parent, node);
        setNameAndLabel("(virtual)");
        this.size = new Dimension(0,0);
        this.center = new Point();
    }

    public void updateSize() {}

    //----------------------- Component like stuff --------------------------


    /**
     * Return the bounding box.
     */
    public Rectangle getBounds() {
        return new Rectangle(center.x, center.y, 0, 0);
    }


    /**
     * Return the intersection of the border of this node and a straight line
     * going from the center to p. Meant for drawing edges.
     * @param p the other endpoint of the line.
     */
    public Point getBorder(Point p) {
        return getCenter();
     }

    
    public void paint(Graphics g) {}

}

/* EOF */
