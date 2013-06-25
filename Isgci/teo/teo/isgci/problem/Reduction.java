/*
 * Reduction from one problem to another.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/Reduction.java,v 2.0 2011/09/25 12:33:16 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a reduction from a parent to a child.
 * If parent can be solved with complexity x on a class, then child can be
 * solved with complexity complexity + x on the same class.
 * Conversely, if child is (co)NPC on a class, then so is the parent.
 */
public class Reduction {
    protected Problem parent, child;
    protected Complexity complexity;
    /** The algorithms solving the parent/child problems */
    protected List<Algorithm> parentAlgos, childAlgos;


    /**
     * Create a new linear/constant time reduction from parent to child.
     */
    public Reduction(Problem child, Problem parent) {
        this(child, parent, Complexity.LINEAR);
    }


    /**
     * Create a new poly/linear/constant time reduction from parent to child.
     * factor must be either LIN or P.
     */
    public Reduction(Problem child, Problem parent, Complexity complexity) {
        this.child = child;
        this.parent = parent;
        this.complexity = complexity;
        this.parentAlgos = new ArrayList<Algorithm>();
        this.childAlgos = new ArrayList<Algorithm>();
    }


    public Problem getParent() {
        return parent;
    }

    public Problem getChild() {
        return child;
    }

    public Complexity getComplexity() {
        return complexity;
    }


    /**
     * If parent can be solved on a graph class in time c, return the
     * complexity of child for this class.
     */
    public Complexity fromParent(Complexity c) {
        if (c.betterOrEqual(Complexity.P))
            return c.betterThan(complexity) ? complexity : c;
        // Cliquewidth unbounded distributes DOWNWARD!
        if (c.equals(Complexity.NPC)  &&
                "Cliquewidth".equals(parent.getName())  &&
                "Cliquewidth expression".equals(child.getName()))
            return c;
        return Complexity.UNKNOWN;
    }

    /**
     * If child can be solved on a graph class in time c, return the
     * complexity of parent for this class.
     */
    public Complexity fromChild(Complexity c) {
        if (c.likelyNotP())
            return c;
        return Complexity.UNKNOWN;
    }

    /**
     * Find an algorithm of the requested complexity for p (either parent or
     * child). If it doesn't exist yet, create it node-independently with the
     * given complexity and text.
     */
    protected Algorithm getReductionAlgo(Problem p, Complexity c, String why) {
        List<Algorithm> l = p == parent ? parentAlgos : childAlgos;
        for (Algorithm a : l) {
            if (a.getComplexity().equals(c))
                return a;
        }

        Algorithm a = p.createAlgo(null, c, why);
        l.add(a);
        return a;
    }
    

    /**
     * Return an algorithm that solves parent on a class in time c, assuming
     * the child can be solved.
     */
    public Algorithm getParentAlgo(Complexity c) {
        final String why = "from "+ child.getName();
        return getReductionAlgo(parent, c, why);
    }


    /**
     * Return an algorithm that solves child on a class in time c, assuming
     * the parent can be solved.
     */
    public Algorithm getChildAlgo(Complexity c) {
        final String why = "from "+ parent.getName();
        return getReductionAlgo(child, c, why);
    }
}

/* EOF */
