/*
 * A weighted view of a directed graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/AsWeightedDirectedGraph.java,v 2.2 2012/04/11 10:32:55 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */
package teo.isgci.grapht;

import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * <p>A weighted view of the backing graph specified in the constructor. This
 * graph allows modules to apply algorithms designed for weighted graphs to an
 * unweighted graph by providing an explicit edge weight mapping. The
 * implementation also allows for "masking" weights for a subset of the edges
 * in an existing weighted graph.</p>
 */
public class AsWeightedDirectedGraph<V, E>
        extends AsWeightedGraph<V,E>
        implements DirectedGraph<V, E>
{
    public AsWeightedDirectedGraph(DirectedGraph<V, E> g,
            Map<E, Double> weightMap)
    {
        super(g, weightMap);
    }
}

// EOF
