/*
 * Walks through the nodes of a graph, using BFS.
 * It starts on a specified node and uses only edges
 * that start at the current node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/BFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.Queue;
import java.util.ArrayDeque;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.*;

public class BFSWalker<V,E> extends GraphWalker<V,E> {
    protected Queue<V> q;
    protected V start;


    /**
     * @param graph the graph in which to walk
     * @param start  node to start from
     * @param mark   where to store WalkerInfo
     * @param initCode determines whether to create a new WalkerInfo,
     *                 reset it or do nothing
     */
    public BFSWalker(
            DirectedGraph<V,E> graph,
            V start,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        this.start = start;
        this.graph = graph;
        dynamic = initCode == InitCode.DYNAMIC;
        this.dataAnn = mark != null ? mark :
                 WalkerInfo.createAnnotation(graph);
        q = new ArrayDeque<V>();
       
        if (initCode == InitCode.CREATE) {
            for (V v : graph.vertexSet())
                dataAnn.getOrCreateNode(v);
        } else if (initCode == InitCode.RESET) {
            for (WalkerInfo wi : dataAnn.nodeValues())
                wi.reset();
        }
        
        WalkerInfo<V,E> wi = getDataNode(start);
        wi.distance=0;
        wi.parent=null;
    }


    public void run() {
        discover(start);
        while (!q.isEmpty())
            visit(q.remove());
    }
    

    public void visit(V v) {
        for (E e : graph.outgoingEdgesOf(v))
            explore(e, graph.getEdgeSource(e), graph.getEdgeTarget(e));
        finish(v);
    }
    

    public void explore(E e, V from, V to) {
        WalkerInfo<V,E> wiTo, wiFrom;
        wiTo = getDataNode(to);
        
        if (wiTo.status == Status.UNSEEN) {
            wiFrom = getDataNode(from);
            wiTo.parent = e;
            wiTo.distance = wiFrom.distance+1;
            discover(to);
        } else {
            see(to);
        }
    }


    public void discover(V v) {
        WalkerInfo wi = getDataNode(v);
        
        wi.status = Status.SEEN;
        q.add(v);
    }


    public void see(V v) {}


    public void finish(V v) {
        WalkerInfo wi = getDataNode(v);
        wi.status = Status.FINISHED;
    }


    /*public static void main(String[] args) {
        DirectedGraph<String,DefaultEdge> g =
                new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);
        g.addVertex("q");
        g.addVertex("u");
        g.addVertex("i");
        g.addVertex("c");
        g.addVertex("k");
        g.addEdge("c", "q");
        g.addEdge("c", "i");
        g.addEdge("c", "u");
        g.addEdge("i", "k");
        g.addEdge("i", "q");
        g.addEdge("i", "u");
        g.addEdge("u", "i");
        g.addEdge("u", "c");
        g.addEdge("q", "u");
        g.addEdge("q", "i");

        new BFSWalker<String,DefaultEdge>(g, "q", null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(String v) {
                System.out.print(v);
                super.visit(v);
            }
        }.run();
        System.out.println();
    }*/
}

/* EOF */
