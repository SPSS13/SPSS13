/*
 * Reranks nodes in a layout tree.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/TreeReranker.java,v 2.0 2011/09/25 12:36:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import org.jgrapht.DirectedGraph;

import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.TreeBFSWalker;
import teo.isgci.grapht.WalkerInfo;
import teo.isgci.grapht.Annotation;

public class TreeReranker<V,E> extends TreeBFSWalker<V,E> {
    protected int delta;

    public TreeReranker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode,
            int delta) {
        super(graph, start, mark, initCode);
        this.delta = delta;
    }


    public void visit(V v) {
        ((GraphDrawInfo)getDataNode(v)).rank += delta;
        super.visit(v);
    }
}

/* EOF */
