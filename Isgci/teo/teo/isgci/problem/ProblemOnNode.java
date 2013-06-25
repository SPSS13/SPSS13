/*
 * Stores the algorithms of a problem on a node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/ProblemOnNode.java,v 2.0 2011/09/25 12:33:16 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import java.util.HashSet;
import teo.isgci.gc.GraphClass;

/**
 * Stores the algorithms and deduced complexities on a node. Complexities are
 * deduced in multiple steps, see the class Problem.
 */
public class ProblemOnNode {
    protected static final int STEPS = Problem.STEPS;
    /** The problem */
    protected Problem problem;
    /** The node */
    protected GraphClass node;
    /** The algorithms */
    protected HashSet<Algorithm> algos;
    /** The complexities, as deduced in the different steps. */
    protected Complexity[] complexity;

    ProblemOnNode(Problem p, GraphClass n) {
        problem = p;
        node = n;
        algos = new HashSet<Algorithm>();
        complexity = new Complexity[STEPS];
        for (int i = 0; i < complexity.length; i++)
            complexity[i] = Complexity.UNKNOWN;
    }


    /**
     * Update the complexity of this at the given step by distilling it with
     * c.
     */
    protected void updateComplexity(Complexity c, int step)
            throws ComplexityClashException {
        c = c.distil(complexity[step]);
        for (; step < complexity.length; step++)
            complexity[step] = c;
    }


    /**
     * Return the complexity at the given step.
     */
    Complexity getComplexity(int step) {
        return complexity[step];
    }


    /**
     * Add an algorithm at the given deduction step.
     */
    void addAlgo(Algorithm a, int step) {
        algos.add(a);
        try {
            updateComplexity(a.getComplexity(), step);
        } catch (ComplexityClashException e) {
            System.err.println("Complexity clash for "+ problem.getName() +
                    " on "+ node + " "+ a +" and "+ algos);
        }

    }


    /**
     * Return the algorithms defined on this node.
     */
    HashSet<Algorithm> getAlgoSet() {
        return algos;
    }

}

/* EOF */
