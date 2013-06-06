/*
 * A DFSWalker walks over nodes of a graph using DFS.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/DFSWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.*;

public class DFSWalker<V,E> extends GraphWalker<V,E> {
    protected int time;
    
    /**
     * @param graph the graph in which to walk
     * @param mark   where to store WalkerInfo
     * @param initCode determines whether to create a new WalkerInfo,
     *                 reset it or do nothing
     */
    public DFSWalker(
            DirectedGraph<V,E> graph,
            Annotation<V,E,WalkerInfo<V,E> > mark,
            GraphWalker.InitCode initCode) {
        this.graph = graph;
        dynamic = initCode == InitCode.DYNAMIC;
        this.dataAnn = mark != null ? mark :
                 WalkerInfo.createAnnotation(graph);
        time = 0;
        
        if (initCode == InitCode.CREATE) {
            for (V v : graph.vertexSet())
                dataAnn.getOrCreateNode(v);
        } else if (initCode == InitCode.RESET) {
            for (WalkerInfo wi : dataAnn.nodeValues())
                wi.reset();
        }
    }


    public void run() {
        WalkerInfo wi;

        for (V v : graph.vertexSet()) {
            wi = getDataNode(v);
            if (wi.status == Status.UNSEEN){
                wi.parent = null;
                discover(v);
            }
        }
    }


    public void visit(V v) {
        for (E e : graph.outgoingEdgesOf(v))
            explore(e, graph.getEdgeSource(e), graph.getEdgeTarget(e));
        finish(v);
    }
   

    public void explore(E e, V from, V to) {
        WalkerInfo<V,E> wi = getDataNode(to);
        
        if (wi.status == Status.UNSEEN) {
            wi.parent = e;
            discover(to);
        } else {
            see(to);
        }
    }
   

    public void discover(V v) {
        WalkerInfo wi = getDataNode(v);
        wi.status = Status.SEEN;
        wi.discover = ++time;
        visit(v);
    }


    public void see(V v) {}


    public void finish(V v) {
        WalkerInfo wi = getDataNode(v);
        wi.status = Status.FINISHED;
        wi.finish = ++time;
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
        g.addEdge("c", "k");
        g.addEdge("i", "q");
        g.addEdge("i", "u");
        g.addEdge("u", "i");
        g.addEdge("q", "u");
        g.addEdge("i", "c");
        g.addEdge("q", "i");

        new DFSWalker<String,DefaultEdge>(g,  null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(String v) {
                System.out.print(v);
                super.visit(v);
            }
        }.discover("q");
        System.out.println();
    }*/
}

/* EOF */
