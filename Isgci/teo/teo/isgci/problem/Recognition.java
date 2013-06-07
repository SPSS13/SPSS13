/*
 * The problem of recognizing a graphclass.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/Recognition.java,v 2.1 2011/09/29 15:08:59 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.jgrapht.DirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.gc.*;
import teo.isgci.db.Note;

/**
 * Stores information about recognizing a graph.
 */
public class Recognition extends Problem {
    /** Whether the first distribution of algorithms has been done */
    boolean firstDistributeDone;

    public Recognition(String name, DirectedGraph<GraphClass,Inclusion> g) {
        super(name, g);
        firstDistributeDone = false;
    }


    /**
     * Distribute the Algorithms for this problem over all nodes.
     * initAlgo/addAlgo must have been called for all problems.
     * We must redefine this function because Recognition distributes neither
     * upwards nor downwards.
     * Assumes the graph g is transitively closed!
     */
    protected void distributeAlgorithms() {
        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(graph);

        //---- Assert finite ForbiddenClass polynomial
        if (!firstDistributeDone) {
            for (GraphClass n : graph.vertexSet()) {
                if (n instanceof ForbiddenClass  &&
                        ((ForbiddenClass) n).isFinite()) {
                    createAlgo(n, Complexity.P,
                        "Finite forbidden subgraph characterization");
                }
            }
        }

        //---- Repeat twice in case of an intersection of unions or the other
        // way around
        /*for (int repeat = 0; repeat < 2; repeat++)*/ {
            //---- Add every set of algorithms to the equivalent nodes' set.
            for (GraphClass n : graph.vertexSet()) {
                HashSet h = getAlgoSet(n);
                if (h != null) {
                    distribute(h, scc.get(n));
                }
            }

            /*distributeUpUnion(gc2node);
            distributeDownIntersect(gc2node);*/
        }
        firstDistributeDone = true;
    }


    /**
     * Try moving complexity information UP to union nodes. We only change the
     * complexity class for Union nodes, and do not generate new references or
     * timebounds.
     * The reasoning is: If we can recognize every part of the union in
     * polytime/lin, then we can take the disjunction of their results in
     * polytime/lin as well. So the union is recognizable in polytime/lin.
     */
    protected void distributeUpUnion() {
        int i;
        boolean ok, linear;
        Complexity c;

        for (GraphClass n : graph.vertexSet()) {
            if ( !(n instanceof UnionClass) ||
                    getDerivedComplexity(n).betterOrEqual(Complexity.LINEAR) )
                continue;

            //---- Check whether all parts are in P ----
            ok = true;
            linear = true;
            Iterator<GraphClass> parts = ((UnionClass) n).getSet().iterator();
            while (ok  &&  parts.hasNext()) {
                GraphClass part = parts.next();
                c = getDerivedComplexity(part);
                ok = ok && c.betterOrEqual(Complexity.P);
                linear = linear && c.betterOrEqual(Complexity.LINEAR);
            }

            if (ok  &&  (linear ||
                    !getDerivedComplexity(n).betterOrEqual(Complexity.P))) {
                //System.err.println("NOTE: distributeUpUnion invoked on "+
                        //n.getName()+" "+toString());
                createAlgo(n,
                        linear ? Complexity.LINEAR : Complexity.P,
                        "From the constituent classes.");
            }
        }
    }


    /**
     * Try moving complexity information DOWN to intersection nodes. We only
     * change the complexity class for Intersect nodes, and do not generate new
     * references or timebounds.
     * The reasoning is: If we can recognize every part of the intersection in
     * polytime/lin, then we can take their conjunction in polytime/lin as
     * well.
     */
    protected void distributeDownIntersect() {
        int i;
        boolean ok, linear;
        Complexity c;

        for (GraphClass n : graph.vertexSet()) {
            if ( !(n instanceof IntersectClass) ||
                    getDerivedComplexity(n).betterOrEqual(Complexity.LINEAR) )
                continue;

            //---- Check whether all parts are in P ----
            ok = true;
            linear = true;
            Iterator<GraphClass> parts =
                    ((IntersectClass) n).getSet().iterator();
            while (ok  &&  parts.hasNext()) {
                GraphClass part = parts.next();
                c = getDerivedComplexity(part);
                ok = ok && c.betterOrEqual(Complexity.P);
                linear = linear && c.betterOrEqual(Complexity.LINEAR);
            }

            if (ok  &&  (linear ||
                    !getDerivedComplexity(n).betterOrEqual(Complexity.P))) {
                //System.err.println(
                        //"NOTE: distributeDownIntersect invoked on "+
                        //n.getName()+" "+toString());
                createAlgo(n,
                        linear ? Complexity.LINEAR : Complexity.P,
                        "From the constituent classes.");
            }
        }
    }
}

/* EOF */
