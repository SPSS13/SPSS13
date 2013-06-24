/*
 * Algorithm for a problem on a particular graphclass
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/SimpleAlgorithm.java,v 2.1 2012/10/28 15:59:06 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import java.util.List;
import teo.isgci.gc.GraphClass;


/**
 * Stores the complexity of some algorithm.
 */
public class SimpleAlgorithm extends Algorithm {
    /** The problem that this algorithm solves */
    protected Problem problem;
    /** Complexity class */
    protected Complexity complexity;
    /** Time bounds, e.g. O(VE) */
    protected String timeBounds;
    /** References */
    protected List refs;
    /** On which graphclass was this algorithm defined? */
    protected GraphClass gc;


    SimpleAlgorithm(Problem problem, GraphClass gc, Complexity complexity,
            String bounds, List refs) {
        this.problem = problem;
        this.complexity = complexity;
        timeBounds = bounds;
        this.refs = refs;
        this.gc = gc;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    public void setComplexity(String s) {
        complexity = Complexity.getComplexity(s);
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setTimeBounds(String bounds) {
        timeBounds = bounds;
    }

    public String getTimeBounds() {
        return timeBounds;
    }


    public List getRefs() {
        return refs;
    }

    public void setRefs(List v) {
        refs = v;
    }

    public GraphClass getGraphClass() {
        return gc;
    }

    public void setGraphClass(GraphClass gc) {
        this.gc = gc;
    }

    public String toString() {
        return "{"+
                (problem != null ? problem.getName() : "(null)") +" "+
                (complexity != null ? complexity.toString() : "(null)") +
                (timeBounds != null ?  "["+timeBounds+"]" : "") +
                " on "+
                (gc != null ? gc.getID() : "(null)") +"}";
    }
}

/* EOF */
