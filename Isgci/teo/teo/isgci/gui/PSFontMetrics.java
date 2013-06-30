/*
 * Fontmetrics of Postscript fonts.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/PSFontMetrics.java,v 2.1 2011/10/18 15:11:57 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.*;
import javax.swing.*;


/**
 * FontMetrics of the Postscript fonts Helvetica and ISGCIFont.
 */
public class PSFontMetrics extends FontMetrics {

    /**
     * Helvetica characterwidth. Actual width is calculated by
     * cw[charcode]*font.getSize()/1000
     */
    static int[] cw = {
    0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   //   0
    0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   //  16
    278,278,355,556, 556,889,667,221, 333,333,389,584, 278,333,278,278, //  32
    556,555,556,556, 556,556,556,556, 556,556,278,278, 584,584,584,556, //  48
    1015,667,667,722, 722,667,611,778, 722,278,500,667, 556,833,722,778,//  64
    667,778,722,667, 611,722,667,944, 667,667,611,278, 278,278,278,469, //  80
    222,556,556,500, 556,556,278,556, 556,222,222,500, 222,833,556,556, //  96
    556,556,333,500, 278,556,500,722, 500,500,500,334, 260,334,584,  0, // 112
    0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   // 128
    0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   // 144
    0,333,556,556, 167,556,556,556, 556,191,333,556, 333,333,500,500,   // 160
    0,556,556,556, 278,  0,537,350, 222,333,333,556,1000,1000, 0,611,   // 176
    0,333,333,333, 333,333,333,333, 333,  0,333,333,   0,333,333,333,   // 192
    1000,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,   0,  0,  0,  0,// 208
    0,1000, 0,370,   0,  0,  0,  0, 556,778,1000,365,  0,  0,  0,  0,   // 224
    0,889,  0,  0,   0,278,  0,  0, 222,611,944,611,   0,  0,  0,  0,   // 240
    };

    /**
     * Characterwidth of ISGCIFont 'a' to 'g'
     */
    int[] specialcw = {600,600,500,500,600,600,400};


    /**
     * Creates the fontmetrics for a given font.
     */
    public PSFontMetrics(Font font) {
        super(font);
    }

    /**
     * Gets the font ascent. The font ascent is the distance from the
     * base line to the top of most Alphanumeric characters.  Note,
     * however, that some characters in the font may extend above
     * this height.
     * @see #getMaxAscent
     */
    public int getAscent() {
        return font.getSize();
    }

    /**
     * Gets the font descent. The font descent is the distance from the
     * base line to the bottom of most Alphanumeric characters.  Note,
     * however, that some characters in the font may extend below this
     * height.
     * @see #getMaxDescent
     */
    public int getDescent() {
        return 219*font.getSize()/1000;
    }

    /**
     * Returns the printed of the characters data[off] to data[off+len-1]
     * @param data the array of characters to be measured
     * @param off the start offset of the characters in the array
     * @param len the number of characters to be measured from the array
     */
    public int charsWidth(char data[], int off, int len) {
        double w = 0;
        double fs = font.getSize();
        boolean specialchars = (font.getName()=="ISGCIFont");
        for (int i = off; i<off+len; i++) {
            char ch = data[i];
            if (specialchars) {
                if (ch>='a' && ch<='g')
                    w += ((double)specialcw[ch-'a'])*fs/1000;
                else
                    throw new RuntimeException("Bad character");
            } else if (ch<256)
                w += ((double)cw[ch])*fs/1000;
        }
        return (int)Math.round(w);
    }


    /**
     * Returns the printed width of the given string
     * @param s the string to be measured.
     */
    public int stringWidth(String s) {
        char data[] = s.toCharArray();
        return charsWidth(data, 0, data.length);
    }

    /**
     * Returns the advance width of the specified character in this Font.
     * The advance width is the amount by which the current point is
     * moved from one character to the next in a line of text.
     * @param ch the character to be measured
     * @see #stringWidth
     */
    public int charWidth(char ch) {
        char[] ca = {ch};
        return charsWidth(ca,0,1);
    }
}

/* EOF */
