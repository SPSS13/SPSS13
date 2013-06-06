/*
 * Walks through the nodes of a tree, using undirected DFS.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/LLWalker.java,v 2.0 2011/09/25 12:36:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import org.jgrapht.DirectedGraph;

import teo.isgci.grapht.TreeDFSWalker;
import teo.isgci.grapht.Annotation;
import teo.isgci.grapht.WalkerInfo;
import teo.isgci.grapht.GraphWalker;

public class LLWalker<V,E> extends TreeDFSWalker<V,E> {

    int offset;
    
    public LLWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode,
            int off) {
        super(graph, start, mark, initCode);
        offset = off;
    }
    
    public void finish(V v) {
        super.finish(v);
        GraphDrawInfo wi = (GraphDrawInfo) getDataNode(v);
        
        wi.low = wi.discover+offset;
        wi.lim = wi.finish+offset;
        wi.treeParent = wi.parent;
    }
}

/* EOF */
