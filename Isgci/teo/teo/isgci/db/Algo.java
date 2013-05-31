/*
 * Some general algorithms for graph class hierarchies.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/Algo.java,v 2.3 2013/03/12 18:53:42 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

import teo.isgci.grapht.*;
import teo.isgci.gc.*;
import teo.isgci.util.LessLatex;
import java.util.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.util.ArrayUnenforcedSet;

/**
 * Some general algorithms for graph class hierarchies.
 */
public final class Algo {
    /** Disallow instantiation of this class */
    private Algo() {}

    /** Preferences for classnames */
    public enum NamePref {
        BASIC,
        FORBIDDEN,
        DERIVED
    };

    
    /**
     * Return the name to be used for the given set of equivalent classes,
     * according to the naming preference.
     */
    public static String getName(Set<GraphClass> equivs, NamePref pref) {
        GraphClass namer = null;

        for (GraphClass c : equivs) {
            if (pref == NamePref.FORBIDDEN  &&  c instanceof ForbiddenClass)
                return c.toString();
            if (pref == NamePref.DERIVED) {
                if (c instanceof UnionClass  ||  c instanceof IntersectClass)
                    return c.toString();
                if (c instanceof HereditaryClass ||
                        c instanceof ComplementClass)
                    namer = c;
            }
            if (pref == NamePref.BASIC   &&  !(
                    c instanceof ForbiddenClass ||
                    c instanceof UnionClass ||
                    c instanceof IntersectClass ||
                    c instanceof HereditaryClass ||
                    c instanceof ComplementClass))
                return c.toString();
            if (namer == null  ||  namer instanceof ForbiddenClass)
                namer = c;
        }
        return namer.toString();
    }


    /**
     * Find the subclasses of node and return them ordered by name.
     */
    public static ArrayList<GraphClass> subNodes(GraphClass node) {
        final ArrayList<GraphClass> res = new ArrayList<GraphClass>();
        new BFSWalker<GraphClass,Inclusion>(
                DataSet.inclGraph, node, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                res.add(v);
                super.visit(v);
            }
        }.run();
        Collections.sort(res, new LessLatex());
        return res;
    }


    /**
     * Find the superclasses of node and return them ordered by name.
     */
    public static ArrayList<GraphClass> superNodes(GraphClass node) {
        final ArrayList<GraphClass> res = new ArrayList<GraphClass>();
        new RevBFSWalker<GraphClass,Inclusion>(
                DataSet.inclGraph, node, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                res.add(v);
                super.visit(v);
            }
        }.run();
        Collections.sort(res, new LessLatex());
        return res;
    }


    /**
     * Find the equivalent classes of node and return them ordered by name.
     */
    public static ArrayList<GraphClass> equNodes(GraphClass node) {
        ArrayList<GraphClass> res =
                new ArrayList<GraphClass>(DataSet.getEquivalentClasses(node));
        Collections.sort(res, new LessLatex());
        return res;
    }


    /**
     * Return the minimal common super classes of a and b. a and b are
     * assumed to be unrelated (i.e. none is a superclass of the other).
     * @param a node
     * @param b node
     * @returns ArrayList of the classes
     * @see DataSet
     */
    public static List<GraphClass> findMinimalSuper(
            SimpleDirectedGraph<GraphClass,Inclusion> dg,
            GraphClass a, GraphClass b) {
        final HashSet<GraphClass> as = new HashSet<GraphClass>();
        final HashSet<GraphClass> bs = new HashSet<GraphClass>();
        ArrayList<GraphClass> res =  new ArrayList<GraphClass>();

        // Gather supernodes of a
        new RevBFSWalker<GraphClass,Inclusion>(
                dg, a, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                as.add(v);
                super.visit(v);
            }
        }.run();

        // Gather supernodes of b
        new RevBFSWalker<GraphClass,Inclusion>(
                dg, b, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                bs.add(v);
                super.visit(v);
            }
        }.run();

        as.retainAll(bs);
        SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> common =
                createHierarchySubgraph(as);
        for (Set<GraphClass> scc : common.vertexSet())
            if (common.outDegreeOf(scc) == 0)
                res.addAll(scc);

        return res;
    }


    public static List<GraphClass> findMinimalSuper(
            GraphClass a, GraphClass b) {
        return findMinimalSuper(DataSet.inclGraph, a, b);
    }


    /**
     * Return the maximal common sub classes of a and b. a and b are
     * assumed to be unrelated (i.e. none is a superclass of the other).
     * @param a node
     * @param b node
     * @returns Vector of Nodes
     * @see DataSet
     */
    public static List<GraphClass> findMaximalSub(
            SimpleDirectedGraph<GraphClass,Inclusion> dg,
            GraphClass a, GraphClass b) {
        final HashSet<GraphClass> as = new HashSet<GraphClass>();
        final HashSet<GraphClass> bs = new HashSet<GraphClass>();
        ArrayList<GraphClass> res =  new ArrayList<GraphClass>();

        // Gather subnodes of a
        new BFSWalker<GraphClass,Inclusion>(
                dg, a, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                as.add(v);
                super.visit(v);
            }
        }.run();

        // Gather subnodes of b
        new BFSWalker<GraphClass,Inclusion>(
                dg, b, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                bs.add(v);
                super.visit(v);
            }
        }.run();

        as.retainAll(bs);
        SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> common =
                createHierarchySubgraph(as);
        for (Set<GraphClass> scc : common.vertexSet())
            if (common.inDegreeOf(scc) == 0)
                res.addAll(scc);

        return res;
    }


    public static List<GraphClass> findMaximalSub(
            GraphClass a, GraphClass b) {
        return findMaximalSub(DataSet.inclGraph, a, b);
    }


    /**
     * Return the nodes between upper and lower.
     */
    public static Collection<GraphClass> nodesBetween(
            Collection<GraphClass> upper, Collection<GraphClass> lower) {
        SimpleDirectedGraph<GraphClass,Inclusion> dg = DataSet.inclGraph;

        final HashSet<GraphClass> as = new HashSet<GraphClass>();
        final HashSet<GraphClass> bs = new HashSet<GraphClass>();
        ArrayList<GraphClass> res =  new ArrayList<GraphClass>();

        // Gather subclasses of upper 
        for (GraphClass a : upper) {
            new BFSWalker<GraphClass,Inclusion>(
                    dg, a, null, GraphWalker.InitCode.DYNAMIC){
                public void visit(GraphClass v) {
                    as.add(v);
                    super.visit(v);
                }
            }.run();
        }

        // Gather superclasses of lower 
        for (GraphClass b : lower) {
            new RevBFSWalker<GraphClass,Inclusion>(dg,b,null,
                    GraphWalker.InitCode.DYNAMIC){
                public void visit(GraphClass v) {
                    bs.add(v);
                    super.visit(v);
                }
            }.run();
        }

        as.retainAll(bs);
        return as;
    }


    /**
     * Return true if the given path contains a proper edge.
     */
    public static boolean isPathProper(List<Inclusion> path) {
        for (Inclusion e : path)
            if (e.isProper())
                return true;
        return false;
    }


    /**
     * Given a path between two classes, try to find another path, indicating
     * that the inclusion is proper, and return it.
     */
    public static List<Inclusion> makePathProper(List<Inclusion> path) {
        GraphClass from = path.get(0).getSuper();
        GraphClass to = path.get(path.size()-1).getSub();

        // Check if this path indicates it is a proper subclass
        if (isPathProper(path))
            return path;

        // If not, try to find another "proper" path
        //System.err.println("try other proper");
        Set<GraphClass> nodes =
                new ArrayUnenforcedSet( nodesBetween(
                Collections.singleton(from), Collections.singleton(to) ));
        DirectedSubgraph<GraphClass,Inclusion> g =
            new DirectedSubgraph(DataSet.inclGraph, nodes, null);
        for (Inclusion e : g.edgeSet()) {
            if (!e.isProper())
                continue;
            
            //System.err.println(e);
            List<Inclusion> p1 =
                    GAlg.getPath(DataSet.inclGraph, from, e.getSuper());
            List<Inclusion> p2 =
                    GAlg.getPath(DataSet.inclGraph, e.getSub(), to);
            //System.err.println(p1.size());
            //System.err.println(p2.size());
            p1.add(e);
            p1.addAll(p2);
            //System.err.println(p1.size());
            return p1;
        }

        return path;
    }


    /**
     * Create a hierarchy subgraph for drawing the given nodes.
     * The graph is created with as vertices the SCCs of the input nodes. There
     * is an edge if there is a path in inclGraph. This result is then
     * transitively reduced.
     * @param subnodes nodes that make up the new graph.
     */
    public static SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>
            createHierarchySubgraph(Collection<GraphClass> subnodes) {
        final SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> res = new
            SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>(
                DefaultEdge.class);

        //---- Create vertices
        for (GraphClass gc : subnodes)
            res.addVertex(DataSet.getEquivalentClasses(gc));

        //---- Create edges between SCC that exist in inclGraph
        for (Set<GraphClass> scc : res.vertexSet()) {
            for (GraphClass gc : scc)
                for (Inclusion e  : DataSet.inclGraph.outgoingEdgesOf(gc)) {
                    Set<GraphClass> sccTo = DataSet.getEquivalentClasses(
                        DataSet.inclGraph.getEdgeTarget(e));
                    if (sccTo != scc  &&  res.containsVertex(sccTo)  &&
                            !res.containsEdge(scc, sccTo))
                        res.addEdge(scc, sccTo);
                }
        }

        //---- If only 1 component, we're done.
        final ConnectivityInspector<Set<GraphClass>, DefaultEdge> conn =
            new ConnectivityInspector<Set<GraphClass>, DefaultEdge>(res);
        if (conn.isGraphConnected()) {
            GAlg.transitiveReduction(res);
            return res;
        }

        //---- Add edges from one component to another.
        for (final Set<GraphClass> scc : res.vertexSet()) {
            final Set compo = conn.connectedSetOf(scc);
            GraphClass orig = scc.iterator().next();
            new BFSWalker<GraphClass,Inclusion>(DataSet.inclGraph, orig, null,
                    GraphWalker.InitCode.DYNAMIC) {
                public void explore(Inclusion e,GraphClass from,GraphClass to){
                    Set<GraphClass> sccTo = DataSet.getEquivalentClasses(to);
                    Set compoTo = res.containsVertex(sccTo) ?
                            conn.connectedSetOf(sccTo) : null;
                    /* Another node in the same connected component?
                       We will check nodeTo later (or earlier) anyhow. */
                    if (compoTo == compo  &&  sccTo != scc)
                        return;

                    // Edge enters another component?
                    if (compoTo != null  &&  compoTo != compo) {
                        if (res.getEdge(scc, sccTo) == null)
                            res.addEdge(scc, sccTo);
                    } else
                        super.explore(e, from, to);
                }
            }.run();
        }

        //System.err.println(res.edgeSet().size());
        GAlg.transitiveReduction(res);
        //System.err.println(res.edgeSet().size());
        return res;
    }
}

/* EOF */
