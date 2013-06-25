/*
 * Convert latex to html understandable by Java Swing.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Latex2JHtml.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

public class Latex2JHtml extends Latex {

    /**
     * Create a new latex->html converter.
     */
    public Latex2JHtml() {
        super();
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
        ((HtmlState) s).target.append("co-(");
        return super.startCo(s);
    }

    protected void endCo(State s) {
        ((HtmlState) s).target.append(")");
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
            t.append(g.getName());
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
