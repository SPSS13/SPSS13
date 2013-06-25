/*
 * Graphics context for SVG.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/SVGGraphics.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.util.*;


/**
 * A Graphics context for SVG. Drawing operations cause SVG
 * code to be appended to the variable 'content'. This variable can be
 * retrieved using getContent() and written to a file.<br><br>
 *
 * Currently only those operations that are required for ISGCI are
 * supported. Moreover, they are modified to make them more suitable for
 * the use ISGCI makes of them. For example, drawLine is only used for
 * \overline and \not, and it's thickness is chosen to match that use.<br>
 * The special function drawArrow() draws a (segmented) arrow with a head at
 * the end.<br>
 */
public class SVGGraphics extends SmartGraphics {

    protected static final String defaultprolog = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
        "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
        "<svg>\n" +
        "<defs>\n" +
        " <marker id=\"arrow\" orient=\"auto\" markerUnits=\"strokeWidth\"" +
        " markerWidth=\"8\" markerHeight=\"6\">\n" +
        "  <path fill=\"black\" d=\"M1 0 l -8 3 2 -3 -2 -3 z\"/>\n" +
        " </marker>\n" +
        " <marker id=\"unproper\" orient=\"auto\"" +
        " markerUnits=\"strokeWidth\" markerWidth=\"8\" markerHeight=\"6\">\n"+
        "  <path fill=\"silver\" d=\"M-1 0 l 8 3 -2 -3 2 -3 z\"/>\n" +
        " </marker>\n" +
        "</defs>\n";

    /** Parent Graphics if this one was created using create() */
    private SVGGraphics parent;

    /** dispose() already called? */
    private boolean disposed;

    /** Contents */
    private StringBuffer content;

    /** Current color */
    private Color color;
    /** Current font */
    private Font font;
    /** clip rectangle (AWT coords) (not used) */
    private Rectangle clip;
    /** Toolkit for calculating font metrics */
    protected Toolkit kit;

    /** x translation (AWT coords) */
    private int translatex;
    /** y translation (AWT coords) */
    private int translatey;

    public SVGGraphics() {
        parent = null;
        disposed = false;
        content = new StringBuffer(16*1024);
        content.append(defaultprolog);
        appendDesc();
        font = null;
        color = Color.black;
        translatex = 0;
        translatey = 0;
        clip = new Rectangle();
        kit = Toolkit.getDefaultToolkit();
    }

    /** Not all attributes make sense for derived graphics */
    private SVGGraphics(SVGGraphics g) {
        parent = g;
        disposed = false;
        content = g.content;
        color = g.color;
        font = g.font;
        translatex = g.translatex;
        translatey = g.translatey;
        clip = new Rectangle(g.clip);
        kit = g.kit;
    }


    /**
     * Derives a new, independent SVGGraphics object from this one.
     */
    public Graphics create() {
        return new SVGGraphics(this);
    }

    /**
     * Ends the drawing on this graphics context.
     */
    public void dispose() {
        if (disposed)
            return;
        content = null;
        parent = null;
        disposed = true;
    }


    /**
     * Return the created content. After this, the graphics is not usable
     * anymore.
     */
    public String getContent() {
        if (parent == null) {
            content.append("</svg>\n");
        }
        String result = content.toString();
        dispose();
        return result;
    }


    /**
     * Translate over (x,y)
     */
    public void translate(int x, int y) {
        translatex += x;
        translatey += y;
        clip.x -= x;
        clip.y -= y;
    }


    /**
     * Get current color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color.
     */
    public void setColor(Color c) {
        if (c==null || c.equals(color))
            return;
        color = c;
    }

    /**
     * Return the current font.
     */
    public Font getFont() {
        return font;
    }


    /**
     * Sets the font. Only 'SansSerif' supported.
     * @param font new font
     */
    public void setFont(Font font) {
        if (font==null || font.equals(this.font))
            return;
        if (!Font.SANS_SERIF.equals(font.getFamily()))
            throw new IllegalArgumentException(font.getFamily());
        this.font = font;
    }
    
    /**
     * Return the metrics for the given font.
     */
    public FontMetrics getFontMetrics(Font f) {
        return kit.getFontMetrics(f);
    }

    public Rectangle getClipBounds() {
        return new Rectangle(clip);
    }

    /**
     * Intersect the clipping area.
     */
    public void clipRect(int x, int y, int width, int height) {
        clip = clip.intersection(new Rectangle(x,y,width,height));
    }

    /**
     * Sets the clip.
     */
    public void setClip(int x, int y, int width, int height) {
        clip = new Rectangle(x,y,width,height);
    }

    public void setClip(Shape clip) {
        this.clip = clip.getBounds();
    }



    /**
     * Append a title and description element for the drawing.
     */
    private void appendDesc() {
        Calendar cal = Calendar.getInstance();

        content.append("<title>ISGCI graph class diagram</title>\n");
        content.append("<desc>Generated ");
        content.append(String.format("%1$tF %1$tR", cal));
        content.append(
                " by http://www.graphclasses.org</desc>\n");
    }


    /*
     * Appends an argument ' name="value"' to content.
     */
    private void arg(String name, String value) {
        content.append(" ");
        content.append(name);
        content.append("=\"");
        content.append(value);
        content.append("\"");
    }

    /*
     * Appends an argument ' name="value"' to content.
     */
    private void arg(String name, int value) {
        arg(name, Integer.toString(value));
    }

    /*
     * Appends an argument ' name="value"' to content.
     */
    private void arg(String name, float value) {
        arg(name, Float.toString(value));
    }


    /**
     * Draws a arrow (more or less) through the given points.
     * @param vec Points of the arrow
     */
    public void drawArrow(Vector vec, boolean unproper) {
        if (vec==null || vec.size()<2)
            return;

        content.append("<polyline");
        arg("stroke","black");
        arg("stroke-width", 1);
        content.append(" points=\"");
        for (int i=0; i<vec.size(); i++) {
            Point p = (Point) vec.elementAt(i);
            int x = translatex + p.x;
            int y = translatey + p.y;
            content.append(x).append(',').append(y).append(' ');
        }
        content.append("\"");
        arg("style", "fill: none;" +
            (unproper ?
                "marker-end:url(#arrow);marker-start:url(#unproper)" :
                "marker-end:url(#arrow)"));
        content.append("/>\n");

    }

    /**
     * Draw an node filled with the current color.
     */
    public void drawNode(int x, int y, int width, int height) {
        float rx = (float) width / 2;
        float ry = (float) height / 2;
        x += translatex;
        y += translatey;
        content.append("<ellipse");
        arg("cx", x + rx);
        arg("cy", y + ry);
        arg("rx", rx);
        arg("ry", ry);
        arg("fill", SColor.getHtml(color));
        arg("stroke", "black");
        arg("stroke-width", 1);
        content.append("/>\n");
    }

    /**
     * Draws a line.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        content.append("<line");
        arg("x1", translatex + x1);
        arg("y1", translatey + y1);
        arg("x2", translatex + x2);
        arg("y2", translatey + y2);
        arg("stroke", "black");
        arg("stroke-width", 1);
        content.append("/>\n");
    }


    /** 
     * Paints a string at the given coordinates.
     */
    public void drawString(String str, int x, int y) {
        if (font==null)
            return;

        content.append("<text");
        arg("x", translatex + x);
        arg("y", translatey + y);
        arg("style", "font-family:"+ font.getFamily() +
                ";font-size:"+ font.getSize() +
                ";fill:black");
        content.append(">");
        content.append(str);
        content.append("</text>\n");
        return;
    }


    public void setPaintMode() {
        throw new RuntimeException("Unsupported operation");
    }
    public void setXORMode(Color c1) {
        throw new RuntimeException("Unsupported operation");
    }
    public Shape getClip() {
        throw new RuntimeException("Unsupported operation");
    }
    public void copyArea(int x, int y, int w, int h, int dx, int dy) {
        throw new RuntimeException("Unsupported operation");
    }
    public void fillRect(int x, int y, int width, int height) {
        throw new RuntimeException("Unsupported operation");
    }
    public void clearRect(int x, int y, int width, int height) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
        throw new RuntimeException("Unsupported operation");
    }
    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawArc(int x, int y, int width, int height, int sA, int aA) {
        throw new RuntimeException("Unsupported operation");
    }
    public void fillArc(int x, int y, int w, int h, int start, int arc) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawOval(int x, int y, int width, int height) {
        throw new RuntimeException("Unsupported operation");
    }
    public void fillOval(int x, int y, int width, int height) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        throw new RuntimeException("Unsupported operation");
    }
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image i, int x, int y, ImageObserver o) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        throw new RuntimeException("Unsupported operation");
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        throw new RuntimeException("Unsupported operation");
    }
    public void drawString(java.text.AttributedCharacterIterator i,
            int x, int y) {
        throw new RuntimeException("Unsupported operation");
    }

}

/* EOF */
