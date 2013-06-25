/*
 * Index Expression (for creating HMT-Families)
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/IndexExpr.java,v 1.2 2011/04/07 07:28:31 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;
import java.util.StringTokenizer;

/**
 * An IndexExpr is a unary total function natural -> int. It is used to
 * create (sub)families using hmt-grammars.
 */
public class IndexExpr{

    protected Token[] tokens;            // These make up the expression in RPN

    /**
     * Initialize <tt>this</tt> from blank-separated expression s, e.g.
     * "2 x * 5 +" for the index set 5,7,9,...
     */
    public IndexExpr(String s) {
        Vector v = new Vector();
        String p;

        StringTokenizer tok = new StringTokenizer(s);
        while (tok.hasMoreElements()) {
            p = (String)tok.nextElement();
            if (p.length() == 1  &&  "+-*/x".indexOf(p.charAt(0)) >= 0)
                v.addElement(new Token(p.charAt(0), 0));
            else
                v.addElement(new Token('i', Integer.parseInt(p)));
        }

        tokens = new Token[v.size()];
        v.copyInto(tokens);
    }

    /**
     * Evaluate this IndexExpr for input value x.
     */
    public int eval(int x) {
        int i;

        for (i = 0; i < tokens.length; i++)
            switch (tokens[i].op) {
                case 'i':                      // Integer constant
                    break;
                case 'x':
                    tokens[i].value = x;
                    break;
                case '+':
                    tokens[i].value = tokens[i-2].value + tokens[i-1].value;
                    break;
                case '-':
                    tokens[i].value = tokens[i-2].value - tokens[i-1].value;
                    break;
                case '*':
                    tokens[i].value = tokens[i-2].value * tokens[i-1].value;
                    break;
                case '/':
                    tokens[i].value = tokens[i-2].value / tokens[i-1].value;
                    break;
                default:
                    throw new RuntimeException("Unknown token "+tokens[i].op+
                            " for IndexExpr");
            }
        return tokens[tokens.length-1].value;
    }

    /*====================================================================*/

    /**
     * Token in IndexExpr.
     */
    protected class Token{
        char op;
        int value;

        public Token(char op, int value) {
            this.op = op;
            this.value = value;
        }
    }
}

/* EOF */
