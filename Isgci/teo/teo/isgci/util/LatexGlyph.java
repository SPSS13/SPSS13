/*
 * Definitions of known latex glyphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/LatexGlyph.java,v 2.2 2012/05/28 07:38:10 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.awt.Image;

/**
 * A special character in latex.
 */
public class LatexGlyph {
    /**
    * The known glyphs.
    */
    static protected LatexGlyph[] glyphs = {
        new LatexGlyph("cap",  "\u2229",   "",      "a", "cap.gif"),
        new LatexGlyph("cup",  "\u222a",   "",      "b", "cup.gif"),
        new LatexGlyph("tau",  "\u03c4",   "",      "c", "tau.gif"),
        new LatexGlyph("le",   "\u2264",   "&lt;=", "<=","le.gif"),
        new LatexGlyph("ge",   "\u2265",   "&gt;=", "d", "ge.gif"),
        new LatexGlyph("cal C","",         "",      "e", "calC.gif"), //u0187
        new LatexGlyph("cal P","",         "",      "f", "calP.gif"), //u01a7
        new LatexGlyph("beta", "\u03b2",   "",      "g", ""),
        new LatexGlyph("equiv","=",        "",      "=", ""),
        new LatexGlyph("subseteq","\u2286","",      "<=","subseteq.gif"),
        new LatexGlyph("subset","\u2282",  "",      "<", "subset.gif"),
        new LatexGlyph("supseteq","\u2287","",      ">=","supseteq.gif"),
        new LatexGlyph("supset","\u2283",  "",      ">", "supset.gif"),
        new LatexGlyph("not",  "\u0338",   "",      "/", ""),
    };


    public static LatexGlyph[] getGlyphs() {
        return glyphs;
    }


    /**
     * See if the given string starts with a known latex command and if so,
     * return the glyph. Otherwise return null.
     * @param s the string to check
     * @param ix at which index to check
     */
    public static LatexGlyph parseGlyph(String s, int ix) {
        for (int i = 0; i < glyphs.length; i++)
            if (s.startsWith(glyphs[i].name, ix))
                return glyphs[i];
        return null;
    }


    /**
     * Return the glyph with the given latex commandname or null.
     */
    public static LatexGlyph getGlyph(String name) {
        for (int i = 0; i < glyphs.length; i++)
            if (glyphs[i].name == name)
                return glyphs[i];
        return null;
    }


    /**
     * The latex command name for this glyph.
     */
    protected String name;             

    /**
     * Its unicode.
     */
    protected String unicode;

    /**
     * Its html.
     */
    protected String html;

    /**
     * The character in ISGCIFont for this glyph.
     */
    protected String isgcichar;

    /**
     * File with a pic of the glyph.
     */
    protected String imagename;

    /**
     * (Loaded) pic of the glyph.
     */
    protected Image image;


    /**
     * Creates a glyph that can be rendered with the given unicode, html or
     * image.
     */
    protected LatexGlyph(String name, String unicode, String html, String c,
            String imagename) {
        this.name = name;
        this.unicode = unicode;
        this.html = !html.equals("") ? html : unicode;
        this.isgcichar = c;
        this.imagename = imagename;
    }


    /**
     * Return the name of this glyph.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the html for this glyph.
     */
    public String getHtml() {
        return this.html;
    }

    /**
     * Return the unicode for this glyph.
     */
    public String getUnicode() {
        return this.unicode;
    }

    /**
     * Return the character code in ISGCIFont for this glyph.
     */
    public String getIsgcichar() {
        return this.isgcichar;
    }

    /**
     * Return the image name of this glyph.
     */
    public String getImageName() {
        return this.imagename;
    }

    /**
     * Return the image of this glyph.
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * Set the image of this glyph.
     */
    public void setImage(Image image) {
        this.image = image;
    }

}

/* EOF */
