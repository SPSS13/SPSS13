/*
 * Walks through the nodes of a tree, using undirected DFS.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/TreeDFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
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
public class TreeDFSWalker<V,E> extends UDFSWalker<V,E> {
    protected V start;
    
    /** A TreeDFSWalker starts on a certain node. */
    public TreeDFSWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        super(graph, mark, initCode);
        this.start = start;
    }
    

    public void run() {
        getDataNode(start).parent = null;
        discover(start);
    }

   
    /**
     * Explore the edge e in the direction from - to. Note that to must be
     * UNSEEN, otherwise the tree edges would not be a tree.
     */
    public void explore(E e, V from, V to) {
        if (getDataEdge(e).tree  &&  getDataNode(from).parent != e) {
            getDataNode(to).status = Status.UNSEEN;   // Fake it, for init NONE
            super.explore(e, from, to);
        }
    }

    
}

/* EOF */
