/*
 * The Cliquewidth parameter on graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/Cliquewidth.java,v 2.0 2011/09/25 12:33:16 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import org.jgrapht.DirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.gc.*;
import teo.isgci.db.Note;

/**
 * Stores information about the cliquewidth of a graph.
 */
public class Cliquewidth extends Problem {
    public Cliquewidth(String name, DirectedGraph<GraphClass,Inclusion> g) {
        super(name, g);
    }

    public String getComplexityString(Complexity c) {
        return c.getWidthString();
    }

    public Complexity complementComplexity(Complexity c) {
        return c;
    }


    /**
     * Distribute the algorithms for the children to this problem.
     * Overloaded because cliquewidth NPC distributes DOWNWARD to cliquewidth
     * expression. It would come back to cwd again, causing a loop, which looks
     * suspicious to the user.
     */
    protected void distributeChildren() {
        Complexity c;

        for (GraphClass n : graph.vertexSet()) {
            for (Reduction r : children) {
                c = r.fromChild(
                        r.getChild().getProgeniallyDerivedComplexity(n) );
                if (!c.isUnknown()  &&  (
                        !getDerivedComplexity(n).isNPC() ||
                        !"Cliquewidth expression".equals(
                            r.getChild().getName())  ||
                        !c.isNPC() ) )
                    addAlgo(n, r.getParentAlgo(c));
            }
        }
    }


    /**
     * Do special deductions for a particular problem.
     * Deduce probe X from X.
     */
    protected void distributeSpecial() {
        int i;
        boolean ok;
        Complexity c;

        for (GraphClass n : graph.vertexSet()) {
            if ( !(n instanceof ProbeClass) ||
                    getDerivedComplexity(n).betterOrEqual(Complexity.P) )
                continue;

            GraphClass base = ((ProbeClass) n).getBase();
            if (getDerivedComplexity(base).betterOrEqual(Complexity.P))
                createAlgo(n, Complexity.P, "From the base class.");
        }
    }
}

/* EOF */
