/*
 * Recursively calculates the transitive closure of a graph.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/ClosingDFS.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;
import java.util.HashSet;
import java.util.ArrayList;
import teo.isgci.util.UnaryFunction;

/**
 * A DFSWalker that transitively closes the graph.
 */
class ClosingDFS<V,E> extends DFSWalker<V,E> {

    /**
     * Create a closingDFS for graph g.
     */
    public ClosingDFS(DirectedGraph<V,E> g) {
        super(g,
                new Annotation<V,E,WalkerInfo<V,E> >(g,
                    new UnaryFunction<V,WalkerInfo<V,E> >() {
                        public ClosingInfo<V,E> execute(V n) {
                            return new ClosingInfo<V,E>();
                        }
                    },
                    null
                ),
                InitCode.CREATE);
    }


    /**
     * Transform the graph into its transitive closure.
     */
    public void run() {
        super.run();
        
        for (V v : graph.vertexSet()) {
            for (V w : ((ClosingInfo<V,E>) getDataNode(v)).sub) {
                if (v != w  &&  !graph.containsEdge(v, w))
                    graph.addEdge(v, w);
            }
        }
    }

    public void explore(E e, V from, V to) {
        ClosingInfo<V,E> ci = (ClosingInfo<V,E>) getDataNode(to);

        if (ci.status == Status.UNSEEN) {
            ci.parent = e;
            discover(to);
        } else {
            if (ci.status == Status.SEEN)
                ci.back.add(from);
            see(to);
        }
    }

    public void finish(V v) {
        super.finish(v);

        int i, j;
        ClosingInfo<V,E> ci = (ClosingInfo<V,E>) getDataNode(v);
        HashSet<V> sub = ci.sub;

        // Gather subnodes in sub
        sub.add(v);
        for (V w : GAlg.outNeighboursOf(graph, v))
            sub.addAll(((ClosingInfo<V,E>) getDataNode(w)).sub);

        // Distribute subnodes over backedges
        for (V w : ci.back)
            distribute(w, sub, ci);
    }

    /**
     * Give all supernodes of v that are in the subtree of info sub-nodes-hash
     * sub.
     */
    private void distribute(V v, HashSet<V> sub, ClosingInfo<V,E> info) {
        ClosingInfo<V,E> ci = (ClosingInfo<V,E>) getDataNode(v);
        if (ci.status != Status.FINISHED  ||  ci.sub == sub  ||
                !(info.discover < ci.discover && ci.finish < info.finish))
            return;
        
        ci.sub = sub;
        for (V w : GAlg.inNeighboursOf(graph, v))
            distribute(w, sub, info);
    }
}


class ClosingInfo<V,E> extends WalkerInfo<V,E> {
    HashSet<V> sub;
    ArrayList<V> back;

    ClosingInfo() {
        super();
        back = new ArrayList<V>();
        sub = new HashSet<V>();
    }
}

/* EOF */
