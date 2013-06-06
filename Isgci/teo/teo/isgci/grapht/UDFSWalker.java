/*
 * Walks through the nodes of a graph, using undirected DFS.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/UDFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;

public class UDFSWalker<V,E> extends DFSWalker<V,E> {
    
    public UDFSWalker(
            DirectedGraph<V,E> graph,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        super(graph, mark, initCode);
    }


    public void visit(V v) {
        /* This does lead to parent edges being explored again */
        for (E e : graph.incomingEdgesOf(v))
            explore(e, graph.getEdgeTarget(e), graph.getEdgeSource(e));
        for (E e : graph.outgoingEdgesOf(v))
            explore(e, graph.getEdgeSource(e), graph.getEdgeTarget(e));
        finish(v);
    }
}

/* EOF */
