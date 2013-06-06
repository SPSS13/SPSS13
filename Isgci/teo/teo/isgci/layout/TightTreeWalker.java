/*
 * Walks through the nodes of a graph, using undirected BFS.
 * It starts on a specified node and uses all tight edges
 * that start at or point to the current node.
 * It is supposed to be used for ranking-algorithm (HierarchyLayout).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/TightTreeWalker.java,v 2.0 2011/09/25 12:36:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import org.jgrapht.DirectedGraph;

import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.UBFSWalker;
import teo.isgci.grapht.WalkerInfo;
import teo.isgci.grapht.Annotation;

/**
 * This class realizes a undirected walk over tight edges
 * (slack(=len-minlen)=0). The edges used by this Walker are marked as
 * tree-edges (GraphDrawingInfo). The result is a spanning tree for the
 * graph.<br>
 * This class is supposed to be used for the network simplex algorithm
 * in HierarchyLayout
 */
public class TightTreeWalker<V,E> extends UBFSWalker<V,E> {
// since this class should only be used by HierarchyLayout
// this class and its members must not be public (only package)

    /** counts the nodes visited */
    protected int count;
    
    public TightTreeWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        super(graph, start, mark, initCode);
        count = 0;
    }
  

    public int getCount(){
        return count;
    }
    

    public void discover(V v) {
        count++;
        getDataNode(v).tree = true; // mark as treeNode
        super.discover(v);
    }
    

    /** New node will be discovered if edge is tight */
    public void explore(E e, V from, V to) {
        GraphDrawInfo<V,E> gdiFrom,gdiTo,gdiEdge;
        
        gdiFrom = (GraphDrawInfo<V,E>) getDataNode(from);
        gdiTo = (GraphDrawInfo<V,E>) getDataNode(to);
        gdiEdge = (GraphDrawInfo<V,E>) getDataEdge(e);
        gdiEdge.status = Status.SEEN;
        
        boolean tight = (Math.abs(gdiTo.rank-gdiFrom.rank) == gdiEdge.minlen);
        
        //Node v = revEdge ? from : to;
        //gdiTo=(GraphDrawInfo)v.getData(mark);
        if (tight  &&  gdiTo.status == Status.UNSEEN) {
            gdiEdge.tree = true;
            //Node v2=revEdge ? to : from;
            //gdiFrom=(GraphDrawInfo)v2.getData(mark);
            gdiTo.parent = e;
            gdiTo.distance = gdiFrom.distance+1;
            discover(to);
        } else {
            see(to);
        }
    }
}

/* EOF */
