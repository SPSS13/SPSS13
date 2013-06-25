/*
 * A node in the syntax tree of an ISGCI Query.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/iq/IQNode.java,v 1.1 2012/04/22 07:45:35 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.iq;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;

/**
 * IQNode is a node in the parse tree of an ISGCI Query.
 */
class IQNode {
    /**
     * Evaluate the expression and return the resulting set.
     * This method is intended to be called for the root node, and maybe
     * investigated subnodes directly (i.e. without calling their eval
     * methods).
     */
    public Set<GraphClass> eval() {
        return new HashSet<GraphClass>();
    }
}

/** A single graph class. */
class IQgc extends IQNode {
    GraphClass gc;
    String name;

    IQgc(GraphClass gc, String s) {
        this.gc = gc;
        name = s;
    }

    public Set<GraphClass> eval() {
        return java.util.Collections.singleton(gc);
    }

    public String toString() {
        return "\""+ name +"\"";
    }
}

/** A operator-preceded (=,<=,<,>,>=) graphclass */
class IQop extends IQNode {
    String op;
    IQgc gc;

    IQop(String op, IQgc gc) {
        this.op = op;
        this.gc = gc;
    }

    public Set<GraphClass> eval() {
        Set<GraphClass> set = null;

        if ("=".equals(op)) {
            set = new HashSet<GraphClass>(DataSet.getEquivalentClasses(gc.gc));
        } else if (op.charAt(0) == '<') {
            set = new HashSet<GraphClass>(Algo.subNodes(gc.gc));
            if (!"<=".equals(op))
                set.removeAll(DataSet.getEquivalentClasses(gc.gc));
        } else if (op.charAt(0) == '>') {
            set = new HashSet<GraphClass>(Algo.superNodes(gc.gc));
            if (!">=".equals(op))
                set.removeAll(DataSet.getEquivalentClasses(gc.gc));
        } else
            throw new RuntimeException("Unknown operator "+ op);
        return set;
    }

    public String toString() {
        return op + gc;
    }
}

/** A negated IQNode */
class IQnot extends IQNode {
    IQNode gc;

    IQnot(IQNode gc) {
        this.gc = gc;
    }

    public Set<GraphClass> eval() {
        Set<GraphClass> set = new HashSet<GraphClass>();
        set.addAll(DataSet.getClasses());
        set.removeAll(gc.eval());
        return set;
    }

    public String toString() {
        return "not "+ gc;
    }
}

/** A IQNode between () */
class IQparen extends IQNode {
    IQNode gc;

    IQparen(IQNode gc) {
        this.gc = gc;
    }

    public Set<GraphClass> eval() {
        return gc.eval();
    }

    public String toString() {
        return "("+ gc +")";
    }
}



/** A binary operator IQNode */
class IQbinop extends IQNode {
    String op;
    List<IQNode> gc;

    IQbinop(String op, List<IQNode> gc) {
        this.op = op;
        this.gc = gc;
    }


    public Set<GraphClass> eval() {
        Set<GraphClass> set = null;

        if ("or".equals(op)) {
            set = new HashSet<GraphClass>();
            for (IQNode g : gc)
                set.addAll(g.eval());
        } else if ("and".equals(op)) {
            // First intersect all positive expressions
            for (IQNode g : gc)
                if (!(g instanceof IQnot)) {
                    if (set == null)
                        set = g.eval();
                    else
                        set.retainAll(g.eval());
                }
            // Then remove the negative ones
            for (IQNode g : gc)
                if (g instanceof IQnot) {
                    if (set == null)
                        set = g.eval();
                    else
                        set.removeAll(((IQnot) g).gc.eval());
                }
        } else
            throw new RuntimeException("Unknown operator "+ op);

        return set;
    }


    public String toString() {
        StringBuffer b = new StringBuffer();
        //b.append("(");
        Iterator<IQNode> iter = gc.iterator();
        while (iter.hasNext()) {
            b.append(iter.next());
            if (iter.hasNext())
                b.append(" "+op+" ");
        }
        //b.append(")");
        return b.toString();
    }
}

/* EOF */
