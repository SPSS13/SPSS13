/*
 * Walks through the nodes of a graph, using BFS.
 * It starts on a specified node and uses only edges
 * that point to the current node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/RevBFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;

public class RevBFSWalker<V,E> extends BFSWalker<V,E> {

    public RevBFSWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        super(graph, start, mark, initCode);
    }

    
    public void visit(V v) {
        for (E e : graph.incomingEdgesOf(v))
            explore(e, graph.getEdgeTarget(e), graph.getEdgeSource(e));
        finish(v);
    }
}

/* EOF */
