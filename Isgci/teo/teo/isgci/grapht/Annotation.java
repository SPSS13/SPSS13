/*
 * Data that is annotated to a node or an edge.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/Annotation.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.Collection;
import java.util.WeakHashMap;
import org.jgrapht.Graph;
import teo.isgci.util.UnaryFunction;

/**
 * Data (D) that is annotated to a node (V) or an edge (E).
 */
public class Annotation<V, E, D> {
    /** The graph this annotation belongs to */
    private Graph<V,E> graph;
    /** Annotations for the nodes */
    private WeakHashMap<V,D> nodeData;
    /** Annotations for the edges */
    private WeakHashMap<E,D> edgeData;
    /** Creates annotation for vertices */
    private UnaryFunction<V,D> nodeCreator;
    /** Creates annotation for edges */
    private UnaryFunction<E,D> edgeCreator;


    /**
     * Create a new annotation for the given graph.
     */
    public Annotation(Graph<V,E> g) {
        this(g, null, null);
    }


    /**
     * Create a new annotation for the given graph.
     */
    public Annotation(Graph<V,E> g, UnaryFunction<V,D> nodeCreator,
            UnaryFunction<E,D> edgeCreator) {
        graph = g;
        nodeData = new WeakHashMap<V,D>();
        edgeData = new WeakHashMap<E,D>();
        this.nodeCreator = nodeCreator;
        this.edgeCreator = edgeCreator;
    }

    /**
     * Set the node creator.
     */
    public void setNodeCreator(UnaryFunction<V,D> creator) {
        nodeCreator = creator;
    }


    /**
     * Set the edge creator.
     */
    public void setEdgeCreator(UnaryFunction<E,D> creator) {
        edgeCreator = creator;
    }


    /**
     * Return the data attached to node n or null.
     */
    public D getNode(V n) {
        if (!graph.containsVertex(n))
            throw new IllegalArgumentException();
        return nodeData.get(n);
    }


    /**
     * Return the data attached to node n. If there is no data for n, it is
     * created using the nodecreator and then returned.
     */
    public D getOrCreateNode(V n) {
        D res = getNode(n);
        if (res == null) {
            res = nodeCreator.execute(n);
            setNode(n, res);
        }
        return res;
    }


    /**
     * Set the data attached to node n.
     */
    public void setNode(V n, D data) {
        if (!graph.containsVertex(n))
            throw new IllegalArgumentException();
        nodeData.put(n, data);
    }


    /**
     * Return the data attached to edge e or null.
     */
    public D getEdge(E e) {
        if (!graph.containsEdge(e))
            throw new IllegalArgumentException();
        return edgeData.get(e);
    }


    /**
     * Return the data attached to edge e. If there is no data for e, it is
     * created using the edgecreator and then returned.
     */
    public D getOrCreateEdge(E e) {
        D res = getEdge(e);
        if (res == null) {
            res = edgeCreator.execute(e);
            setEdge(e, res);
        }
        return res;
    }


    /**
     * Set the data attached to edge e.
     */
    public void setEdge(E e, D data) {
        if (!graph.containsEdge(e))
            throw new IllegalArgumentException();
        edgeData.put(e, data);
    }

    /**
     * Return the data stored for nodes.
     */
    public Collection<D> nodeValues() {
        return nodeData.values();
    }

    /**
     * Return the data stored for edges.
     */
    public Collection<D> edgeValues() {
        return edgeData.values();
    }
}

/* EOF */
