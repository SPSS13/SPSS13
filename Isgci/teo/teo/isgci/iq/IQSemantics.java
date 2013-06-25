/*
 * Semantics handler for IQNode parsers
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/iq/IQSemantics.java,v 1.1 2012/04/22 07:45:35 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.iq;

import teo.isgci.gc.GraphClass;
import teo.isgci.db.*;
import java.util.ArrayList;

class IQSemantics extends mouse.runtime.SemanticsBase {
    /** error or null */
    protected String errorString;
    /** Result tree */
    protected IQNode tree;

    public IQSemantics() {
        errorString = null;
        tree = null;
    }


    /**
     * Return an error string if parsing unsuccesful, or null otherwise.
     */
    public String getError() {
        return errorString;
    }


    /**
     * Return the result of the parse
     */
    public IQNode getIQ() {
        return tree;
    }


    /** Generate an error */
    void error(String err) {
        errorWhere(lhs().where(0) +": "+ err);
    }


    /** Generate an error that already includes the location */
    void errorWhere(String err) {
        errorString = err;
        lhs().errClear();
    }


    //--------------------------- Parser actions ----------------------------
    void top() {
        tree = (IQNode) rhs(1).get();
        //System.out.println("tree: "+tree);
    }

    void bad() {
        errorWhere(lhs().errMsg());
        //System.out.println("bad: "+errorString);
    }

    /** lhs = rhs */
    void dup() {
        lhs().put(rhs(0).get());
    }

    /** expr (and|or expr)* */
    void binop() {
        if (rhsSize() == 1) {
            lhs().put(rhs(0).get());
            return;
        }

        String op = rhs(1).text().trim();
        ArrayList<IQNode> gc = new ArrayList<IQNode>();
        for (int i = 0; i < rhsSize(); i+= 2)
            gc.add((IQNode) rhs(i).get());
        lhs().put(new IQbinop(op, gc));
    }

    /** ( expr )? */
    void paren() {
        if (rhsSize() < 3  ||  rhs(2).isEmpty()  ||  rhs(2).charAt(0) != ')') {
            error("No closing parenthesis");
        } else {
            lhs().put(new IQparen((IQNode) rhs(1).get()));
        }
    }

    /** not? expr */
    void not() {
        lhs().put(rhsSize() > 1 ? new IQnot((IQNode) rhs(1).get()) :
                rhs(0).get());
    }

    /** op? gc */
    void rel() {
        IQNode res = (IQNode) rhs(rhsSize()-1).get();
        if (rhsSize() > 1)
            res = new IQop(rhs(0).text().trim(), (IQgc) res);
        lhs().put(res);
    }

    /** id / name, possibly unterminated "name */
    void gc() {
        String name = rhs(0).text().trim();
        if (name.charAt(0) == '"') {
            if (!name.endsWith("\"")) {
                error("class name without closing quote (\")");
                return;
            }
            name = name.substring(1, name.length()-1);
        }

        GraphClass thegc = null;
        for (GraphClass g : DataSet.getClasses())
            if (name.equals(g.getID())  ||  name.equals(g.toString()))
                thegc = g;
        if (thegc == null) {
            error("Unknown class "+ name);
            return;
        }

        lhs().put(new IQgc(thegc, name));
    }

    /** whitespace */
    void space() {
        lhs().errClear();
    }
}

/* EOF */
