/*
 * GraphWalkers can travel over a graph. Useful to implement DFS/BFS and
 * friends.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/GraphWalker.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;

/**
 * <p>A GraphWalker is an object that moves over a graph in a particular
 * fashion. After the walker is created, it can be started by calling run().
 * It is not specified whether walkers can be reused by calling run() a
 * second time.</p>
 *
 * <p>A vertex can be in one of three (implicit) states:
 * <dl>
 * <dt>UNSEEN</dt><dd>  The vertex has not yet been seen by the walker</dd>
 * <dt>SEEN</dt><dd>    The vertex has been seen by the walker</dd>
 * <dt>FINISHED</dt><dd>The walker is finished visiting the vertex.</dd>
 * </dl></p>
 *
 * <p>Initially all vertices are UNSEEN. To change a vertex v from UNSEEN to
 * SEEN, discover(v) is called.  A walker can be moved onto a vertex v by
 * calling visit(v). Only SEEN vertices can be visited. If a walker moves
 * off a vertex v and will not visit it again, finish(v) is called; this
 * changes the state of v to FINISHED.</p>
 * 
 * <p>A walker will normally move over edges (direction unspecified) to travel
 * from vertex to vertex.  explore(edge) is called to examine an edge and
 * a vertex v at the other end, or possibly both ends. If v is SEEN or
 * FINISHED, see(v) is called, if v is UNSEEN, discover(v) is called.</p>
 */
public abstract class GraphWalker<V,E> implements Runnable {

    /** How far we are in handling a vertex */
    public enum Status {UNSEEN, SEEN, FINISHED};
    /** How to initialize the nodes */
    public enum InitCode {
        NONE,   /** Do not initialize nodes */
        RESET,  /** Reset the nodes */
        CREATE, /** Start by creating the info for every node */
        DYNAMIC /** Create the nodes as needed */
    };

    /** The graph we're walking in. */
    protected DirectedGraph<V,E> graph;
    /** WalkerInfo annotation */
    protected Annotation<V, E, WalkerInfo<V,E> > dataAnn;
    /** Create WalkerInfos when they're first fetched? */
    protected boolean dynamic;

    public abstract void visit(V v);
    public abstract void explore(E e, V from, V to);
    public abstract void discover(V v);
    public abstract void see(V v);
    public abstract void finish(V v);

    protected WalkerInfo<V,E> getDataNode(V v) {
        return dynamic ? dataAnn.getOrCreateNode(v) : dataAnn.getNode(v);
    }

    protected WalkerInfo<V,E> getDataEdge(E e) {
        return dynamic ? dataAnn.getOrCreateEdge(e) : dataAnn.getEdge(e);
    }
}

/* EOF */
