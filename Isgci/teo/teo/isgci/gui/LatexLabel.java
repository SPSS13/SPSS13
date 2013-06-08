/*
 * A Label that can contain latex.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/LatexLabel.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.*;
import java.net.*;
import java.io.*;

/**
 * A Label that can depict a subset of latex. drawLatexString can also
 * be called from other classes.
 */
public class LatexLabel extends JComponent {

    /** Where the glyphs come from. */
    private LatexGraphics latex;
    
    /** Contents of this label */
    private String text;

    private int align;
    //private int border;


    /**
     * Creates a new label without text.
     */
    public LatexLabel(LatexGraphics l) {
        this(l, "", JLabel.LEFT);
    }

    /**
     * Creates a new label
     *
     * @param text the contents of the label
     */
    public LatexLabel(LatexGraphics l, String text) {
        this(l, text, JLabel.LEFT);
    }

    /**
     * Creates a new label
     *
     * @param text contents of the label
     * @param alignment alignment of the label (only LEFT supported )
     */
    public LatexLabel(LatexGraphics l, String text, int alignment) {
        this.latex = l;
        this.text = text;
        align = alignment;
        setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        //border = 5;
    }

    /**
     * Paints the label
     *
     * @param g the graphics context
     */
    protected void paintComponent(Graphics graphics) {
        Graphics g = graphics.create();
        // Get component width and height
        int width = getSize().width;
        int height = getSize().height;

        // Clear screen
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        // Set current component color and font
        g.setColor(getForeground());
        g.setFont(getFont());

        // Get the metrics of the current font
        FontMetrics fm = g.getFontMetrics();
        Insets insets = getInsets();

        drawLatexString(g, text, insets.left, //border,
                insets.top /*border*/ + fm.getLeading() + fm.getMaxAscent());
    }

    /**
     * Changes the text of the label
     *
     * @param text the new text
     */
    public void setText(String text) {
        this.text=text;
        repaint();
    }

    /**
     * Returns the contents of the label.
     */
    public String getText() {
        return text;
    }

    /**
     * Changes the alignment of the label (only LEFT supported).
     *
     * @param align the new alignment
     */
    public void setAlignment(int align) {
        this.align=align;
    }

    /**
     * Returns the alignment of the label.
     */
    public int getAlignment() {
        return align;
    }

    /**
     * Returns the preferred size of the label.
     */
    public Dimension getPreferredSize() {
        if (getFont() == null)
            return getSize();

        Insets insets = getInsets();
        Graphics g;
        if (getGraphics() == null)
            g = new NulGraphics();
        else
            g = getGraphics().create();
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        Dimension d = new Dimension(
                insets.left + insets.right + latex.getLatexWidth(g, text),
                insets.top + insets.bottom + fm.getHeight());
        g.dispose();
        return d;
    }

    /**
     * Returns the minimum size of the label.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns the maximal size of the label.
     */
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
        //return getPreferredSize();
    }


    /**
     * Paints a string that contains latexcodes and returns its width.
     * @param g the graphics context
     * @param f the font metrics
     * @param str the string to draw
     * @param x where to paint
     * @param y where to paint
     */
    public int drawLatexString(Graphics g, String str, int x, int y) {
        return latex.drawLatexString(g, str, x, y);
    }


    /**
     * Returns the width of a latexstring.
     * @param str the string
     * @param f the fontmetrics
     */
    public int getLatexWidth(Graphics g, String str) {
        return latex.getLatexWidth(g, str);
    }


}

/* EOF */
