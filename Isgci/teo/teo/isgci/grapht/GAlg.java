/*
 * General algorithms and tools for graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/GAlg.java,v 2.2 2013/04/07 10:48:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import org.jgrapht.traverse.TopologicalOrderIterator;
import teo.isgci.util.Itera;

public class GAlg {

    /**
     * Copy the subgraph (vertices and edges) induced by vertices in g to
     * target.
     */
    public static <V,E> void copyInduced(DirectedGraph<V,E> g,
            Iterable<V> vertices, DirectedGraph<V,E> target) {
        for (V v : vertices)
            target.addVertex(v);
        for (E e : g.edgeSet())
            if (target.containsVertex(g.getEdgeSource(e))  &&
                    target.containsVertex(g.getEdgeTarget(e)))
                target.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e), e);
    }


    /**
     * Split a graph into its connected components.
     */
    public static <V,E> List<SimpleDirectedGraph<V,E> > split(
            SimpleDirectedGraph<V,E> g, java.lang.Class<E> edgeClass) {
        List<SimpleDirectedGraph<V,E> > res =
                new ArrayList<SimpleDirectedGraph<V,E> >();
        for (Set<V> compo : new ConnectivityInspector<V,E>(g).connectedSets()){
            SimpleDirectedGraph<V,E> compog =
                    new SimpleDirectedGraph<V,E>(edgeClass);
            copyInduced(g, compo, compog);
            res.add(compog);
        }
        return res;
    }


    /**
     * Return the in-neighbours of v in g.
     */
    public static <V,E>
            Itera<V> inNeighboursOf(final DirectedGraph<V,E> g, final V v) {
        return new Itera<V>() {
            final Iterator<E> edges = g.incomingEdgesOf(v).iterator();

            public V next() {
                return g.getEdgeSource(edges.next());
            }

            public boolean hasNext() {
                return edges.hasNext();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }


    /**
     * Return the out-neighbours of v in g.
     */
    public static <V,E>
            Itera<V> outNeighboursOf(final DirectedGraph<V,E> g, final V v) {
        return new Itera<V>() {
            final Iterator<E> edges = g.outgoingEdgesOf(v).iterator();

            public V next() {
                return g.getEdgeTarget(edges.next());
            }

            public boolean hasNext() {
                return edges.hasNext();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    
    /**
     * Return a path (list of edges) between src and dest in g or null if no
     * path exist. If src==dest an empty list is returned.
     */
    public static <V,E> List<E> getPath(DirectedGraph<V,E> g, V src, V dest) {
        return org.jgrapht.alg.DijkstraShortestPath.findPathBetween(
                g, src, dest);
    }


    /**
     * Return a map from vertices to their SCCs.
     * In the values of this map, every SCC exists precisely once as an
     * unmodifiable set.
     */
    public static <V,E> Map<V,Set<V> > calcSCCMap(DirectedGraph<V,E> dg) {
        Map<V, Set<V> > sccs = new HashMap<V, Set<V> >();
        for (Set<V> scc : calcSCCList(dg)) {
            Set<V> s = Collections.unmodifiableSet(scc);
            for (V g : scc)
                sccs.put(g, s);
        }

        return sccs;
    }


    /*
     * Return the SCCs of a graph as a list of vertex sets.
     */
    public static <V,E> List<Set<V> > calcSCCList(DirectedGraph<V,E> g) {
        return new StrongConnectivityInspector(g).stronglyConnectedSets();
    }


    /**
     * Return the topological order of g.
     * Undefined if this graph is not acyclic!
     */
    public static <V,E> Itera<V> topologicalOrder(DirectedGraph<V,E> g) {
        return new Itera<V>(new TopologicalOrderIterator<V,E>(g));
    }


    /**
     * Transitively close g.
     */
    public static <V,E> void transitiveClosure(DirectedGraph<V,E> g) {
        new ClosingDFS(g).run();
    }


    /**
     * Transitively reduce g.
     */
    public static <V,E> void transitiveReduction(DirectedGraph<V,E> g) {
        if (new CycleDetector<V,E>(g).detectCycles())
            throw new IllegalArgumentException("Graph not acyclic");

        ArrayDeque<V> topo = new ArrayDeque<V>();
        for (V n : new Itera<V>(new TopologicalOrderIterator<V,E>(g)))
            topo.add(n);

        HashMap<V, HashSet<V> > sub = new HashMap<V, HashSet<V> >();

        // Process nodes in reverse topo order
        for (V n : new Itera<V>(topo.descendingIterator())) {
            HashSet<V> nsub = new HashSet<V>(); // All subnodes of n.
            HashSet<V> nout = new HashSet<V>(); // Reduced outnodes of n.
            sub.put(n, nsub);

            for (V v : outNeighboursOf(g, n)) {
                if (!nsub.contains(v)) {
                    nsub.add(v);
                    nout.add(v);

                    for (V w : sub.get(v)) {
                        if (!nsub.contains(w))
                            nsub.add(w);
                        else
                            nout.remove(w);
                    }
                }
            }

            // Remove transitive edges
            HashSet<E> remove = new HashSet<E>();
            for (E e : g.outgoingEdgesOf(n)) {
                if (!nout.contains(g.getEdgeTarget(e)))
                    remove.add(e);
            }
            g.removeAllEdges(remove);

        }
    }


    /**
     * Transitively reduce a possibly cyclic graph using brute force:
     * Every edges is removed and re-added only if it is necessary to maintain
     * reachability.
     */
    public static <V,E> void transitiveReductionBruteForce(
            DirectedGraph<V,E> g) {
        List<E> edges = new ArrayList<E>(g.edgeSet());
        for (E edge : edges) {
            V src = g.getEdgeSource(edge);
            V dest = g.getEdgeTarget(edge);
            g.removeEdge(edge);
            if (getPath(g, src, dest) == null)
                g.addEdge(src, dest, edge);
        }
    }


    public static void main(String[] args) {
        SimpleDirectedGraph<String,DefaultEdge> g =
                new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);

        g.addVertex("q");
        g.addVertex("u");
        g.addVertex("i");
        g.addVertex("c");
        g.addVertex("k");
        g.addEdge("c", "k");
        g.addEdge("i", "c");
        g.addEdge("u", "i");
        g.addEdge("q", "u");
        GAlg.transitiveClosure(g);
        System.out.println(g);
        GAlg.transitiveReduction(g);
        System.out.println(g);

        System.out.println(org.jgrapht.alg.DijkstraShortestPath.
                findPathBetween(g, "q", "q"));
        System.out.println(org.jgrapht.alg.DijkstraShortestPath.
                findPathBetween(g, "q", "k"));
        System.out.println(org.jgrapht.alg.DijkstraShortestPath.
                findPathBetween(g, "k", "q"));
    }
}

/* EOF */
