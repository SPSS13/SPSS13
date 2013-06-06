/*
 * Bitbucket graphics for querying fontmetrics.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NulGraphics.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * A bitbucket Graphics, that creates no output, but can be used for obtaining
 * the size of painted strings.
 */
public class NulGraphics extends Graphics {
    protected Font font;
    protected Toolkit kit;

    public NulGraphics() {
        font = null;
        kit = Toolkit.getDefaultToolkit();
    }

    public Graphics create() {
        NulGraphics g = new NulGraphics();
        g.setFont(font);
        return g;
    }

    public Font getFont() {
        return font;
    }
    
    public void setFont(Font f) {
        font = f;
    }

    public FontMetrics getFontMetrics(Font f) {
        return kit.getFontMetrics(f);
    }

    public void translate(int x, int y) {}
    public Color getColor() {return null;}
    public void setColor(Color c) {}
    public void setPaintMode() {}
    public void setXORMode(Color c1) {}
    public Rectangle getClipBounds() {return null;}
    public void clipRect(int x, int y, int width, int height) {}
    public void setClip(int x, int y, int width, int height) {}
    public Shape getClip() {return null;}
    public void setClip(Shape clip) {}
    public void copyArea(int x,int y,int width,int height,int dx,int dy) {}
    public void drawLine(int x1, int y1, int x2, int y2) {}
    public void fillRect(int x, int y, int width, int height) {}
    public void clearRect(int x, int y, int width, int height) {}
    public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {}
    public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {}
    public void drawOval(int x, int y, int width, int height) {}
    public void fillOval(int x, int y, int width, int height) {}
    public void drawArc(int x, int y, int w, int h, int sa, int aa) {}
    public void fillArc(int x, int y, int w, int h, int sa, int aa) {}
    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {}
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {}
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {}
    public void drawString(String str, int x, int y) {}
    public boolean drawImage(Image i, int x, int y, ImageObserver o) {
        return false;
    }
    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {
        return false;
    }
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return false;
    }
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return false;
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return false;
    }
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return false;
    }
    public void drawString(java.text.AttributedCharacterIterator i,
            int x, int y) {
        throw new RuntimeException("Unsupported operation");
    }
    public void dispose() {}

}

/* EOF */
