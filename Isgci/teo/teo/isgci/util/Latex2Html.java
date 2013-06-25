/*
 * Convert latex to html.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Latex2Html.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

public class Latex2Html extends Latex {

    /**
     * Path to the location of the image files. Should end in /.
     */
    protected String imgpath;


    /**
     * Create a new latex->html converter with images at the given location.
     */
    public Latex2Html(String imgpath) {
        super();
        this.imgpath = imgpath;
    }


    /**
     * Return s as an html string.
     */
    public String html(String s) {
        HtmlState state = new HtmlState(s);
        drawLatexPart(state, true);
        return state.target.toString();
    }

    protected State startSuper(State s) {
        ((HtmlState) s).target.append("<sup>");
        return super.startSuper(s);
    }

    protected void endSuper(State s) {
        ((HtmlState) s).target.append("</sup>");
        super.endSuper(s);
    }

    protected State startSub(State s) {
        ((HtmlState) s).target.append("<sub>");
        return super.startSub(s);
    }

    protected void endSub(State s) {
        ((HtmlState) s).target.append("</sub>");
        super.endSub(s);
    }

    protected State startCo(State s) {
        ((HtmlState) s).target.append("<span class=\"complement\">");
        return super.startCo(s);
    }

    protected void endCo(State s) {
        ((HtmlState) s).target.append("</span>");
        super.endCo(s);
    }

    protected void drawPlainString(State state, String str) {
        ((HtmlState) state).target.append(str);
    }

    protected void drawGlyph(State state, LatexGlyph g) {
        StringBuffer t = ((HtmlState) state).target;
        if (!g.getHtml().equals("")) {
            t.append(g.getHtml());
        } else {
            t.append(
                "<img src=\"");
            t.append(imgpath);
            t.append(g.getImageName());
            t.append("\" alt=\"");
            t.append(g.getName());
            t.append("\"/>");
        }
    }



    protected class HtmlState extends Latex.State {
        protected StringBuffer target;

        public HtmlState(String s) {
            super(s);
            target = new StringBuffer();
        }

        public HtmlState(State parent) {
            super(parent);
            this.target = ((HtmlState) parent).target;
        }

        /**
         * Derive a new state and return it.
         */
        public State deriveStart() {
            return new HtmlState(this);
        }
    }
}


/* EOF */
