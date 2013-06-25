/*
 * Common system for formatting latex.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Latex.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;


/**
 * Contains information necessary for displaying latex text.
 * To draw a string, use the following in a derived class:
 * state = new State(s);
 * drawLatexPart(state, true);
 */
public abstract class Latex {

    /**
     * Start a superscript by performing the necessary drawing actions and
     * returning a derived state for it.
     */
    protected State startSuper(State s) {
        return s.deriveStart();
    }


    /**
     * End a superscript by performing the necessary drawing actions and
     * finishing the derived state for it.
     */
    protected void endSuper(State s) {
        s.deriveEnd();
    }


    /**
     * Start a subscript by performing the necessary drawing actions and
     * returning a derived state for it.
     */
    protected State startSub(State s) {
        return s.deriveStart();
    }


    /**
     * End a subscript by performing the necessary drawing actions and
     * finishing the derived state for it.
     */
    protected void endSub(State s) {
        s.deriveEnd();
    }


    /**
     * Start a complement by performing the necessary drawing actions and
     * returning a derived state for it.
     */
    protected State startCo(State s) {
        return s.deriveStart();
    }


    /**
     * End a complement by performing the necessary drawing actions and
     * finishing the derived state for it.
     */
    protected void endCo(State s) {
        s.deriveEnd();
    }


    /**
     * Draws a string (without latex codes) and updates the painting
     * information in state
     * @param state the parsing/painting state
     * @param str the string to draw
     */
    protected abstract void drawPlainString(State state, String str);

    /**
     * Draws a single Glyph and updates the painting information in state
     * @param state the parsing/painting state
     * @param g the Glyph to draw
     */
    protected abstract void drawGlyph(State state, LatexGlyph g);

    /**
     * Paints the next part of a latex string. The next part is the entire
     * string if fullString is true, the first character of s if not a brace
     * open and the part of s up to the matching closing brace otherwise.
     */
    protected final void drawLatexPart(State state, boolean fullString) {
        State substate;
        char c;
        StringBuffer b = new StringBuffer();    // To gather plain chars
        LatexGlyph glyph;

        int bracelevel = 0;
        boolean justone = false;

        if (!fullString)
            justone = state.s.charAt(state.i) != '{';
        
        while (state.i < state.s.length()) {
            c = state.s.charAt(state.i++);
            switch (c) {
                case '$' :              // Math mode.
                    state.toggleMathmode();
                    break;
                case '{' :              // Start grouping
                    bracelevel++;
                    break;
                case '}' :              // End grouping
                    bracelevel--;
                    break;
                case '_' :              // Subscript
                    if (b.length() > 0) {
                        drawPlainString(state, b.toString());
                        b.setLength(0);
                    }
                    substate = startSub(state);
                    drawLatexPart(substate, false);
                    endSub(substate);
                    break;
                case '^' :              // Superscript
                    if (b.length() > 0) {
                        drawPlainString(state, b.toString());
                        b.setLength(0);
                    }
                    substate = startSuper(state);
                    drawLatexPart(substate, false);
                    endSuper(substate);
                    break;
                case '\\':              // Latex special
                    if (b.length() > 0) {
                        drawPlainString(state, b.toString());
                        b.setLength(0);
                    }
                    if (state.s.startsWith("co", state.i)) {  // \overline
                        state.i += "co".length();
                        substate = startCo(state);
                        drawLatexPart(substate, false);
                        endCo(substate);
                        break;
                    } else if ( (glyph =
                            LatexGlyph.parseGlyph(state.s, state.i)) != null ){
                        state.i += glyph.getName().length();
                        drawGlyph(state, glyph);
                    } else
                        System.err.println("Unknown latex code at "+
                                state.s.substring(state.i, 10));
                    break;
                case '-' :              // Maybe --
                    b.append('-');
                    if (state.s.charAt(state.i) == '-')
                        state.i++;
                    break;
                default :               // Plain char
                    b.append(c);
                    
            }
            if (!fullString  &&  (justone || bracelevel==0))
                break;
        }

        if (b.length() > 0) {
            drawPlainString(state, b.toString());
            b.setLength(0);
        }
    }


    //=====================================================================
    /**
     * Parse state.
     */
    protected class State {
        public String s;                // Source string
        public int i;                   // Next index in s
        public boolean mathmode;        // Mathmode active?
        public State parent;         // parent for derived states


        /**
         * Create a new State for drawing the given string.
         */
        public State(String s) {
            this.s = s;
            this.i = 0;
            this.mathmode = false;
            this.parent = null;
        }


        /**
         * Create a new State with the given one as its parent and with the
         * same settings.
         */
        public State(State parent) {
            this.s = parent.s;
            this.i = parent.i;
            this.mathmode = parent.mathmode;
            this.parent = parent;
        }


        /**
         * Toggle mathmode.
         */
        public void toggleMathmode() {
            this.mathmode = !this.mathmode;
        }


        /**
         * Derive a new state and return it.
         */
        public State deriveStart() {
            return new State(this);
        }


        /**
         * End this state and update the parent.
         */
        public void deriveEnd() {
            parent.i = i;
        }
    }
}

/* EOF */
