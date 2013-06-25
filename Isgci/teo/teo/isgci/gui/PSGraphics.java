/*
 * Graphics context for Postscript.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/PSGraphics.java,v 2.1 2012/10/29 12:53:12 ux Exp $
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
 * A Graphics context for Postscript. Drawing operations cause Postscript
 * code to be appended to the variable 'content'. This variable can be
 * retrieved using getContent() and written to a file.<br><br>
 *
 * Currently only those operations that are required for ISGCI are
 * supported. Moreover, they are modified to make them more suitable for
 * the use ISGCI makes of them. For example, drawLine is only used for
 * \overline and \not, and it's thickness is chosen to match that use.<br>
 * The special function drawArrow() draws a (segmented) arrow with a head at
 * the end. Special characters are available as ISGCIFont,plain,12 as 'a' to
 * 'f':<br>
 * a - \cap<br>
 * b - \cup<br>
 * c - \tau<br>
 * d - \ge<br>
 * e - \cal C<br>
 * f - \cal P<br>
 * <br>
 * The external interface uses normal AWT coordinates, that is (0,0) is
 * topleft, with the positive axes going right and downwards. Internally the
 * Postscript system is used, with (0,0) topleft, and positive axes going
 * right and <bf>up</bf>ward.
 */
public class PSGraphics extends SmartGraphics {

    private static final String PROLOG = "data/psprolog.txt";
    protected static String defaultprolog = null;
    private static boolean initialized = false;

    /** Physical papersize in postscript units */
    public static final int PSWIDTH_A4       =  596;
    public static final int PSHEIGHT_A4      =  842;

    public static final int PSWIDTH_A3       =  842;
    public static final int PSHEIGHT_A3      = 1190;

    public static final int PSWIDTH_LETTER   =  612;
    public static final int PSHEIGHT_LETTER  =  792;

    public static final int PSWIDTH_LEGAL    =  612;
    public static final int PSHEIGHT_LEGAL   = 1008;

    public static final int PSWIDTH_TABLOID  =  792;
    public static final int PSHEIGHT_TABLOID = 1224;
    /** Margin of paper that cannot be used */
    public static final int MARGIN = 25;

    /** Parent Graphics if this one was created using create() */
    private PSGraphics parent;

    /** dispose() already called? */
    private boolean disposed;

    /** Contents */
    private StringBuffer content;

    /** Postscript prolog */
    private String prolog;

    /** Name and dimensions of paper */
    private String papersize;
    private int paperwidth;
    private int paperheight;

    /** Scale to fit? */
    private boolean doscale;
    /** Keep aspect ratio when scaling? */
    private boolean doratio;
    /** Rotate 90 degrees? */
    private boolean dorotate;
    /** Use color? */
    private boolean usecolor;

    /** Current color */
    private Color color;
    /** Current font */
    private Font font;
    /** clip rectangle (AWT coords) (not used) */
    private Rectangle clip;

    /** x translation (AWT coords) */
    private int translatex;
    /** y translation (AWT coords) */
    private int translatey;

    /** Area that is actually in use by the drawing (PS coords) */
    private Box bounds;


    public PSGraphics() {
        this("A4", true, true, false, false);
    }

    public PSGraphics(String paper, boolean fittopage, boolean keepsideratio,
            boolean rotate, boolean usecolor) {
        parent = null;
        disposed = false;
        content = new StringBuffer(defaultprolog.length()+2000);
        prolog = defaultprolog;
        color = Color.black;
        font = null;
        translatex = 0;
        translatey = 0;
        setPaperSize(paper);
        doscale = fittopage;
        doratio = keepsideratio;
        dorotate = rotate;
        this.usecolor = usecolor;
        bounds = new Box();
        clip = new Rectangle();
    }

    /** Not all attributes make sense for derived graphics */
    private PSGraphics(PSGraphics g) {
        parent = g;
        disposed = false;
        content = g.content;
        color = g.color;
        font = g.font;
        usecolor = g.usecolor;
        translatex = g.translatex;
        translatey = g.translatey;
        bounds = g.bounds;
        clip = new Rectangle(g.clip);
    }

    /**
     * Changes the papersize.
     * @param papersize one of A4,A3,Letter,Legal,Tabloid
     */
    public void setPaperSize(String papersize) {
        this.papersize = papersize;
        if (papersize.equals("A4")) {
            paperwidth = PSWIDTH_A4;
            paperheight = PSHEIGHT_A4;
        } else if (papersize.equals("A3")) {
            paperwidth = PSWIDTH_A3;
            paperheight = PSHEIGHT_A3;
        } else if (papersize.equals("Letter")) {
            paperwidth = PSWIDTH_LETTER;
            paperheight = PSHEIGHT_LETTER;
        } else if (papersize.equals("Legal")) {
            paperwidth = PSWIDTH_LEGAL;
            paperheight = PSHEIGHT_LEGAL;
        } else if (papersize.equals("Tabloid")) {
            paperwidth = PSWIDTH_TABLOID;
            paperheight = PSHEIGHT_TABLOID;
        }
    }


    /** Scale to fit? */
    public void setDoScale(boolean doscale) {
        this.doscale = doscale;
    }

    /** Keep aspect ratio? */
    public void setDoRatio(boolean doratio) {
        this.doratio = doratio;
    }

    /** Rotate 90 degrees? */
    public void setDoRotate(boolean dorotate) {
        this.dorotate = dorotate;
    }



    /**
     * Derives a new, independent PSGraphics object from this one.
     */
    public Graphics create() {
        return new PSGraphics(this);
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
            scaleAndRotate();
            content.insert(0, getPSHeader()+prolog+getPSStartPage());
            content.append(getPSEndPage()).append(getPSEnd());
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
        PSsetColor(c);
    }


    /**
     * Return the current font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font. Only 'Helvetica' and 'ISGCIFont' supported.
     * @param font new font
     */
    public void setFont(Font font) {
        if (font==null || font.equals(this.font))
            return;
        this.font = font;
    }
    
    /**
     * Return the metrics for the given font.
     */
    public FontMetrics getFontMetrics(Font f) {
        return new PSFontMetrics(f);
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
     * Generate output for the selected scale/rotate/fit options. The
     * boundingbox is changed to match the modified content.
     */
    private void scaleAndRotate() {
        int imgwidth, imgheight, imgbottom, imgleft;
        StringBuffer str = new StringBuffer();
        StringBuffer scalestr = null;

        if (dorotate) {
            imgwidth = bounds.top - bounds.bottom;
            imgheight = bounds.right-bounds.left;
        } else {
            imgwidth = bounds.right-bounds.left;
            imgheight = bounds.top - bounds.bottom;
        }

        // Calculate scaled image size
        if (doscale  &&  (imgwidth > paperwidth - 2*MARGIN ||
                          imgheight > paperheight - 2*MARGIN)) {
            float xscale = (float) (paperwidth-2*MARGIN) / imgwidth;
            float yscale = (float) (paperheight-2*MARGIN) / imgheight;
            if (doratio) {
                xscale = Math.min(xscale, yscale);
                yscale = xscale;
            }
            imgwidth = (int) (imgwidth * xscale);
            imgheight = (int) (imgheight * yscale);
            scalestr = new StringBuffer();
            scalestr.append(xscale).append(' ');
            scalestr.append(yscale).append(" scale\n");
        }

        // Calculate position to align at top left
        imgbottom = imgheight < paperheight - 2*MARGIN ?
                paperheight-imgheight-MARGIN : MARGIN;
        imgleft = MARGIN;
        
        // Translate to new origin
        str.append(imgleft).append(' ').
            append(imgbottom).append(" translate\n");
        // Rotate
        if (dorotate)
            str.append("90 rotate\n");
        // Scale
        if (scalestr != null)
            str.append(scalestr);
        // Move lower left to origin
        if (dorotate)
            str.append(-bounds.left).append(' ').
                append(-bounds.top).append(" translate\n");
        else
            str.append(-bounds.left).append(' ').
                append(-bounds.bottom).append(" translate\n");

        content.insert(0, str);
        
        bounds.left = imgleft;
        bounds.right = imgleft+imgwidth;
        bounds.top = imgbottom+imgheight;
        bounds.bottom = imgbottom;
    }


    /**
     * Return the postscript header. The content must be ready, including
     * scaling and rotating.
     */
    private String getPSHeader() {
        StringBuffer datestr = new StringBuffer();
        String str;
        Calendar cal = Calendar.getInstance();

        datestr.append(cal.get(Calendar.YEAR)).append('-');
        datestr.append(cal.get(Calendar.MONTH)+1).append('-');
        datestr.append(cal.get(Calendar.DATE)+1).append(' ');

        str = ""+cal.get(Calendar.HOUR_OF_DAY);
        if (str.length()<=1)
            datestr.append('0');
        datestr.append(str).append(':');

        str = ""+cal.get(Calendar.MINUTE);
        if (str.length()<=1)
            datestr.append('0');
        datestr.append(str).append(':');

        str = ""+cal.get(Calendar.SECOND);
        if (str.length()<=1)
            datestr.append('0');
        datestr.append(str);

        return
            // Prolog
            "%!PS-Adobe-3.0 EPSF-3.0\n"+
            "%%BoundingBox: "+bounds.left+" "+bounds.bottom+" "+
            bounds.right+" "+bounds.top+"\n"+
            "%%Creator: http://www.graphclasses.org\n"+
            "%%Title: ISGCI graph class diagram\n"+
            "%%CreationDate: "+datestr+"\n"+
            "%%Pages: 1\n"+
            "%%DocumentNeededResources: font Helvetica\n"+
            "%%DocumentSuppliedResources: procset ISGCI-Prolog 3.2 1\n"+
            "%%EndComments\n";
    }

    /**
     * Return start of page command.
     */
    private String getPSStartPage() {
        return
            "\n"+
            "%%Page:  1 1\n"+
            "ISGCI begin save\n"+
            "12 Setnormalsize\n"+
            "Usesplines\n"+
            "\n";
    }

    /**
     * Return end of page command.
     */
    private String getPSEndPage() {
        return "restore end showpage\n";
    }


    /**
     * Return end of document command.
     */
    private String getPSEnd() {
        return
            "%%Pages: 1\n"+
            "%%Trailer\n"+
            "%%EOF\n";
    }

    /**
     * Generate a line drawing command (PS coords).
     */
    private void PSdrawLine(int x1, int y1, int x2, int y2) {
        bounds.intersect(Math.min(x1,x2), Math.max(y1,y2),
                    Math.abs(x2-x1), Math.abs(y2-y1));

        content.append(x1).append(' ').append(y1).append(' ').
                append(x2).append(' ').append(y2).append(" Line\n");
    }

    /**
     * Generate a set color command.
     * Can handle only shades of black, shades of red and shades of green.
     */
    private void PSsetColor(Color col) {
        if (col == null)
            return;

        if (SColor.isGray(col))
            content.append(SColor.getGray(col)).append(" setgray ");
        else if (usecolor) {
            content.append(col.getRed()+" "+col.getGreen()+" "+col.getBlue()+
                    " Setcolor ");
        } else if (col.getRed() > 0)
            content.append("0.5 setgray ");
        else if (col.getGreen() > 0)
            content.append("0.9 setgray ");
    }


    /**
     * Generate a text writing command.
     */
    private void PSdrawString(String str, int x, int y) {
        if (str==null || font==null)
            return;

        content.append('(');

        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c=='('  ||  c==')'  ||  c=='\\')       // Quote if necessary
                content.append('\\');
            content.append(c);
        }

        content.append(") ").append(x).append(' ').append(y).append(' ');

        // Select font
        content.append('F');
        if (font.getName()=="ISGCIFont")
            content.append('S');
        else
            content.append('T');
        if (font.getSize() >= 12)
            content.append('N');
        else
            content.append('S');

        content.append(" Text\n");
    }

    

    /**
     * Draw an arrow (more or less) through the given points.
     * @param vec Points of the arrow
     */
    public void drawArrow(Vector vec, boolean unproper) {
        if (vec==null || vec.size()<2)
            return;

        for (int i=0; i<vec.size(); i++) {
            Point p = (Point) vec.elementAt(i);
            int x = translatex + p.x;
            int y = -(translatey + p.y);
            content.append(x).append(' ').append(y).append(' ');
            bounds.intersect(x-10, y+10, 20, 20);
        }
        content.append(vec.size()).append(unproper ? " DArrow\n" : " Arrow\n");
    }

    /**
     * Draw an node filled with the current color.
     */
    public void drawNode(int x, int y, int width, int height) {
        x += translatex;
        y = -(translatey+y);
        content.append(x).append(' ').append(y).append(' ');
        content.append(width).append(' ').append(height).append(" Node\n");
        bounds.intersect(x, y, width, height);
    }

    /**
     * Draws a line.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        PSdrawLine(translatex+x1, -(translatey+y1),
                translatex+x2, -(translatey+y2));
    }


    /** 
     * Paints a string at the given coordinates.
     */
    public void drawString(String str, int x, int y) {
        if (font==null)
            return;
        PSdrawString(str, x+translatex, -(translatey+y));
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

    /**
     * Loads the default Postscript prologue.
     */
    public static void init(teo.Loader loader) {
        if (initialized)
            return;

        StringBuffer b = new StringBuffer();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    loader.openStream(PROLOG)));
            String s;
            while ((s = r.readLine()) != null)
                b.append(s).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        defaultprolog = new String(b.toString());
    }

    protected class Box {
        int left, right, top, bottom;

        public Box() {
            left = bottom = Integer.MAX_VALUE;
            right = top = Integer.MIN_VALUE;
        }

        public void intersect(int left, int top, int width, int depth) {
            if (left < this.left)        this.left = left;
            if (left+width > this.right) this.right = left+width;
            if (top > this.top)          this.top = top;
            if (top-depth < this.bottom) this.bottom = top-depth;
        }
    }

}

/* EOF */
