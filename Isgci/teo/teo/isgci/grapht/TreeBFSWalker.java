/*
 * Walks through the nodes of a graph, using undirected BFS.
 * It starts on a specified node and uses all edges
 * that start at or point to the current node and are marked as treeEdge. Note
 * that this class does not mark treeEdges itself.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/TreeBFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;

/**
 * Walk through the nodes of a graph, using only the edges marked tree edges.
 * For this GraphWalker InitCode.NONE can be used.
 */
public class TreeBFSWalker<V,E> extends UBFSWalker<V,E> {

    public TreeBFSWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        super(graph, start, mark, initCode);
    }
   
    
    /**
     * Explore the edge e in the direction from - to. Do NOT check whether to
     * is seen already, if so the tree edges would not be a tree.
     */
    public void explore(E e, V from, V to) {
        if (getDataEdge(e).tree  &&  getDataNode(from).parent != e) {
            getDataNode(to).status = Status.UNSEEN;  // Fake it, for init NONE
            super.explore(e, from, to);
        }
    }
}

/* EOF */
