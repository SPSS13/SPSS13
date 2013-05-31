/*
 * A directed graph that caches nodes/edges.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/CacheGraph.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.HashMap;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphVertexChangeEvent;

import teo.isgci.util.Pair;


public class CacheGraph<V,E> extends ListenableDirectedGraph<V,E>
        implements GraphListener<V,E> {

    /** The graph we're caching */
    private DirectedGraph<V,E> graph;
    /** Cache for nodes */
    private HashMap<V,V> nodeCache;
    /** Cache for edges (for fast getEdge) */
    private HashMap<Pair,E> edgeCache;
    /** For looking up edges */
    private Pair reusablePair;
    /** Perform consistency checks? */
    private boolean checking;

    /**
     * Creates a new CacheGraph that caches base. The initial size of the
     * hashtable for nodes and edges are given by the parameters nodeSize,
     * edgeSize, respectively.
     */
    public CacheGraph(DirectedGraph<V,E> base, int nodeSize, int edgeSize) {
        super(base);
        graph = base;
        checking = false;
        reusablePair = new Pair();

        //---- Fill nodeCache
        nodeCache = new HashMap<V,V>(nodeSize);
        for (V v : base.vertexSet())
            if (nodeCache.put(v,v) != null)
                throw new IllegalArgumentException(v +
                    " already exists in graph!");

        //---- Fill edgeCache
        edgeCache = new HashMap<Pair,E>();
        for (E e : base.edgeSet())
            if (edgeCache.put( new Pair(graph.getEdgeSource(e),
                        graph.getEdgeTarget(e)), e) != null)
                throw new IllegalArgumentException(e +
                    " already exists in graph!");

        // Listen to events
        this.addGraphListener(this);
    }


    /**
     * Find the given vertex and return it or null if it doesn't exist.
     */
    public V findVertex(V v) {
        return nodeCache.get(v);
    }


    /**
     * Find the given edge and return it or null if it doesn't exist.
     */
    public E findEdge(V source, V target) {
        reusablePair.first = source;
        reusablePair.second = target;
        return edgeCache.get(reusablePair);
    }


    /**
     * Find the given edge and return it or null if it doesn't exist.
     */
    public E findEdge(E e) {
        return findEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
    }

    //------------------------- Graph methods --------------------------------
    public boolean containsVertex(V v) {
        return findVertex(v) != null;
    }

    public boolean containsEdge(E e) {
        return findEdge(e) != null;
    }

    public boolean containsEdge(V source, V target) {
        return findEdge(source, target) != null;
    }

    //------------------------- checking methods -----------------------------

    /**
     * Set the checking flag and return the old value.
     */
    public boolean setChecking(boolean c) {
        boolean old = checking;
        checking = c;
        return old;
    }


    /**
     * Check whether the caches have the same sizes as the graph.
     */
    private void checkSizes() {
        if (graph.vertexSet().size() != nodeCache.size())
            throw new RuntimeException("nodeCache has "+ nodeCache.size() +
                    "nodes and graph has "+ graph.vertexSet().size());
        if (graph.edgeSet().size() != edgeCache.size())
            throw new RuntimeException("nodeCache has "+ edgeCache.size() +
                    "nodes and graph has "+ graph.edgeSet().size());
    }


    /**
     * Check whether cache and underlying graph are consistent. Throw an
     * exception if not.
     */
    public void check() {
        checkSizes();
        for (V v : graph.vertexSet()) {
            if (findVertex(v) != v)
                throw new RuntimeException("Finding "+ v +" returns "+
                    findVertex(v));
        }
        for (E e : graph.edgeSet()) {
            if (findEdge(e) != e)
                throw new RuntimeException("Finding "+ e +" returns "+
                    findEdge(e));
        }
    }


    //---------------------- Graph Listener methods --------------------------
    //
    public void vertexAdded(GraphVertexChangeEvent<V> ev) {
        V v = ev.getVertex();

        if (nodeCache.put(v, v) != null)
            throw new IllegalArgumentException(v +" already exists in graph!");

        if (checking) {
            checkSizes();
            if (!graph.containsVertex(nodeCache.get(v)))
                throw new RuntimeException(
                        "Error caching addition of node "+ v);
        }
    }


    public void vertexRemoved(GraphVertexChangeEvent<V> ev) {
        V v = ev.getVertex();

        if (nodeCache.remove(v) == null)
            throw new IllegalArgumentException(v +" doesn't exist in graph!");

        if (checking) {
            checkSizes();
            if (graph.containsVertex(v)  ||  containsVertex(v))
                throw new RuntimeException(
                        "Error caching removal of node "+ v);
        }
    }


    public void edgeAdded(GraphEdgeChangeEvent<V,E> ev) {
        E e = ev.getEdge();

        if (null != edgeCache.put(
                new Pair(graph.getEdgeSource(e), graph.getEdgeTarget(e)), e))
            throw new IllegalArgumentException(ev.getEdge() +
                " already exists in graph!");

        if (checking) {
            checkSizes();
            if (!graph.containsEdge(findEdge(e)))
                throw new RuntimeException(
                        "Error caching addition of edge "+ e);
        }
    }


    public void edgeRemoved(GraphEdgeChangeEvent<V,E> ev) {
        E e = ev.getEdge();
        if (null == edgeCache.remove(
                new Pair(graph.getEdgeSource(e), graph.getEdgeTarget(e))))
            throw new IllegalArgumentException(ev.getEdge() +
                " doesn't exist in graph!");

        if (checking) {
            checkSizes();
            if (graph.containsEdge(e)  ||  containsEdge(e))
                throw new RuntimeException(
                        "Error caching removal of edge "+ e);
        }
    }
}

/* EOF */
