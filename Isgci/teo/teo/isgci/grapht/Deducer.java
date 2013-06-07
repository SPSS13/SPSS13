/*
 * Deduces trivial inclusions.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/Deducer.java,v 2.10 2012/07/27 07:10:22 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.grapht;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import teo.isgci.gc.*;
import teo.isg.Graph;
import teo.isgci.db.*;
import teo.isgci.util.IDGenerator;
import teo.isgci.util.Itera;
import teo.isgci.util.Iterators;

public class Deducer {
    
    /** Where we're deducing */
    CacheGraph<GraphClass,Inclusion> graph;
    /** To keep track of derived inclusions */
    /*Object original;
    int orgIx;*/
    /** Contains the temporary nodes */
    HashSet<GraphClass> temporaries;
    /** Trace where the deductions come from? */
    boolean trace;
    /** Perform consistency checks? */
    boolean checking;
    /** Tracedata why the inclusion holds */
    Annotation<GraphClass,Inclusion,TraceData> traceAnn;
    /** Tracedata why the inclusion is of this type (properness) */
    Annotation<GraphClass,Inclusion,TraceData> traceRelAnn;
    /** Classes added in the last run of findTrivialInclusions */
    private ArrayList<GraphClass> newclasses;
    /** The edges that were deduced with subClassOf() */
    private Set<Inclusion> directs;
    /** Confidence level at which we're currently deducing */
    private int confidence;
    /** Input edges with less than certain confidence levels */
    private HashSet<Inclusion> uncertains;
    /** Generates the ids for automatically deduces classes (AUTO_*) */
    private IDGenerator idgenerator;
    /** Prefix when printing newly created nodes */
    private String nodedebugprefix;
    /** Print new classes as they are created? */
    private boolean printnewclasses;
    /** Iteration number */
    private int iteration;
    
    
    public Deducer(DirectedGraph<GraphClass,Inclusion> g, boolean trace,
            boolean checking) {
        graph = new CacheGraph<GraphClass,Inclusion>(g,
                1*1000*1000, 5*1000*1000);
        graph.setChecking(checking);
        temporaries = new HashSet<GraphClass>(g.vertexSet().size());
        directs = new HashSet<Inclusion>(1*1000*1000);
        idgenerator = null;
        this.trace = trace;
        this.checking = checking;
        printnewclasses = true;
        if (trace) {
            traceAnn = new Annotation<GraphClass,Inclusion,TraceData>(graph);
            traceRelAnn= new Annotation<GraphClass,Inclusion,TraceData>(graph);
        }
        newclasses = null;
    }


    /**
     * Return the graph on which we're deducing.
     */
    public DirectedGraph<GraphClass,Inclusion> getGraph() {
        return graph;
    }


    /**
     * Print statistics on the deduced relations.
     */
    public void printStatistics() {
        int nodecount = graph.vertexSet().size();
        int edgecount = graph.edgeSet().size();
        int total = nodecount * (nodecount-1) / 2;
        int numproper = 0, numequal = 0;
        int numpropereq = 0;

        for (Inclusion e : graph.edgeSet()) {
            if (e.isProper())
                numproper++;
            else if (containsEdge(graph.getEdgeTarget(e),
                    graph.getEdgeSource(e)))
                numequal++;
        }
        numpropereq = edgecount - numproper - numequal;

        System.out.print("Total relations: ");
        System.out.println(total);

        System.out.print("Inclusions: ");
        System.out.print(edgecount);
        System.out.print(" (");
        System.out.print((100.0 * edgecount)/total);
        System.out.println("%)");

        System.out.println("of these");
        
        System.out.print("   Proper inclusions: ");
        System.out.print(numproper);
        System.out.print(" (");
        System.out.print((100.0 * numproper)/edgecount);
        System.out.println("%)");

        System.out.print("   Equalities: ");
        System.out.print(numequal);
        System.out.print(" (");
        System.out.print((100.0 * numequal)/edgecount);
        System.out.println("%)");

        System.out.print("   Equal or proper: ");
        System.out.print(numpropereq);
        System.out.print(" (");
        System.out.print((100.0 * numpropereq)/edgecount);
        System.out.println("%)");

        System.out.println("with confidence");

        for (int level = Inclusion.CONFIDENCE_HIGHEST;
                level >= Inclusion.CONFIDENCE_LOWEST;
                level--) {
            int count = countEdges(level);

            System.out.print("   ");
            System.out.print(level);
            System.out.print(": ");
            System.out.print(count);
            System.out.print(" (");
            System.out.print((100.0 * count)/edgecount);
            System.out.println("%)");
        }
    }


    /**
     * Create a new IDGenerator using the given cachefile.
     * Must be called before start of deductions.
     */
    public void setGeneratorCache(String filename) {
        idgenerator = new IDGenerator("AUTO_", filename);
    }


    /**
     * Return the number of edges with the given confidence level.
     */
    private int countEdges(int level) {
        int res = 0;

        for (Inclusion e : graph.edgeSet()) {
            if (e.getConfidence() == level)
                res++;
        }
        return res;
    }


    /**
     * Transitive reduction that prefers deleting edges with low keeperPrio().
     * Returns the deleted edges as list of Pairs(from, to).
     */
    public Collection<Inclusion> deleteSuperfluousEdges() {
        Collection<Inclusion> deleted = new ArrayList<Inclusion>();

        // First delete the bulk using weighted shortest paths
        WeightedGraph<GraphClass,Inclusion> wg = keeperWeightedGraph();
        FloydWarshallShortestPaths<GraphClass,Inclusion> fw =
            new FloydWarshallShortestPaths<GraphClass,Inclusion>(wg);
        for (Inclusion e : graph.edgeSet())
            if (keeperWeight(e) > fw.shortestDistance(
                    graph.getEdgeSource(e), graph.getEdgeTarget(e)))
                deleted.add(e);
        for (Inclusion e : deleted)
            removeEdge(e);

        // Then go through the remaining edges one by one
        deleted.addAll(deleteSuperfluousEdges(0));
        deleted.addAll(deleteSuperfluousEdges(1));
        return deleted;
    }


    /**
     * Return a weighted version of the deductions graph, designed for easily
     * recognizing edges that can deleted (see keeperWeight).
     */
    protected WeightedGraph<GraphClass,Inclusion> keeperWeightedGraph() {
        Map<Inclusion,Double> map = new HashMap<Inclusion,Double>();
        for (Inclusion e : graph.edgeSet())
            map.put(e, new Double(keeperWeight(e)));
        return new AsWeightedDirectedGraph(graph, map);
    }


    /**
     * Removes edges with the given prio, provided a path from src to dest
     * remains in the graph. Like a restricted transitive reduction.
     * Returns the deleted edges as list of Inclusions.
     */
    public Collection<Inclusion> deleteSuperfluousEdges(int prio){
        final HashSet<Inclusion> deleted = new HashSet<Inclusion>();

        for (Inclusion edge : graph.edgeSet()) {
            if (keeperPrio(edge) != prio)
                continue;

            final GraphClass src = graph.getEdgeSource(edge);
            final GraphClass dest = graph.getEdgeTarget(edge);
            final Inclusion theedge = edge;
            final ArrayList reached = new ArrayList();

            new BFSWalker<GraphClass,Inclusion>(graph, src, null,
                    GraphWalker.InitCode.DYNAMIC) {
                public void explore(Inclusion e, GraphClass from,
                        GraphClass to) {
                    if (e != theedge  &&  !deleted.contains(e))
                        super.explore(e, graph.getEdgeSource(e),
                                graph.getEdgeTarget(e));
                }
                public void visit(GraphClass v) {
                    if (v == dest) {
                        reached.add(v);
                        q.clear();
                    }
                    else
                        super.visit(v);
                }

            }.run();
            if (!reached.isEmpty())
                deleted.add(edge);
        }

        for (Inclusion e : deleted)
            removeEdge(e);
        return deleted;
    }


    /**
     * Return the priority for keeping e when deleting superfluous edges.
     * 0 = no tracedata or transitive
     * 1 = other cases
     * 2 = with refs
     */
    private int keeperPrio(Inclusion e) {
        if (e.getRefs() != null  &&  !e.getRefs().isEmpty())
            return 2;

        TraceData td = traceAnn.getEdge(e);

        if (td == null  ||  "Transitivity".equals(td.desc))
            return 0;

        return 1;
    }


    /*
     * Return weights for the edges of the deductions graph, designed for
     * easily recognizing edges that can deleted. Every edge has weight
     * depending on keeperPrio: 2->0.0001, 1->1, 0->10000. If the shortest path
     * a->b is shorter than the edge weight, this means the edge can surely be
     * deleted.
     */
    private double keeperWeight(Inclusion e) {
        final double[] weights = {10000.0, 1.0, 0.0001};
        return weights[keeperPrio(e)];
    }


    /**
     * Reduction that removes edges between SCCs for which a path exists.
     * Sort of a transitive reduction except that edges inside an SCC are
     * never removed.
     * This ensures that the web pages contain only minimal/maximal
     * super/subclasses.
     * Returns the deleted edges.
     */
    public Collection<Inclusion> deleteSuperfluousEdgesFull(){
        int i;
        final List<Inclusion> deleted = new ArrayList<Inclusion>();
        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(graph);

        for (Inclusion edge : graph.edgeSet()) {
            final GraphClass src = graph.getEdgeSource(edge);
            final GraphClass dest = graph.getEdgeTarget(edge);

            if (scc.get(src) == scc.get(dest))
                continue;

            final Inclusion theedge = edge;
            final ArrayList reached = new ArrayList();

            new BFSWalker<GraphClass,Inclusion>(graph, src, null,
                    GraphWalker.InitCode.DYNAMIC) {
                public void explore(Inclusion e, GraphClass from,
                        GraphClass to) {
                    if (e != theedge  &&  !deleted.contains(e))
                        super.explore(e, from, to);
                }
                public void visit(GraphClass v) {
                    if (v == dest) {
                        reached.add(v);
                        q.clear();
                    }
                    else
                        super.visit(v);
                }

            }.run();
            if (!reached.isEmpty())
                deleted.add(edge);
        }

        for (Inclusion e : deleted)
            removeEdge(e);
        return deleted;
    }


    /**
     * Adds reference strings for trivially deduced inclusions.
     */
    public void addRefs() {
        TraceData tr;

        for (Inclusion e : graph.edgeSet()) {
            tr = traceAnn.getEdge(e);
            if (tr == null)
                continue;

            if ("direct".equals(tr.desc)) {
                e.addRef(new Ref( graph.getEdgeTarget(e).whySubClassOf() ));
            } else if ("addForbiddenSuper".equals(tr.desc)) {
                e.addRef(new Ref("forbidden"));
            } else if ("addForbiddenSuperConfig".equals(tr.desc)) {
                e.addRef(new Ref("forbidden"));
            } else if ("extendForbidden".equals(tr.desc)) {
                e.addRef(new Ref("forbidden"));
            } else if ("complement".equals(tr.desc)) {
                e.addRef(new Ref("complement"));
            } else if ("probeclass".equals(tr.desc)) {
                e.addRef(new Ref("basederived"));
            } else if ("cliqueclass".equals(tr.desc)) {
                e.addRef(new Ref("basederived"));
            }
        }
    }
  

    /**
     * Remove the edges with confidence levels below the highest one and store
     * them in uncertains.
     */
    private void separateUncertains() {
        int i;

        uncertains = new HashSet<Inclusion>();
        for (Inclusion e : graph.edgeSet()) {
            if (e.getConfidence() < Inclusion.CONFIDENCE_HIGHEST)
                uncertains.add(e);
        }
        for (Inclusion e : uncertains)
            graph.removeEdge(e);
        /*System.out.print("unreliable: ");
        System.out.println(uncertains.size());*/
    }


    /**
     * Add the uncertains of the current confidence level back into the graph.
     * They are removed from uncertains.
     * Returns the number of edges added.
     */
    private int addUncertains() {
        Inclusion e;
        int i = 0;

        Iterator<Inclusion> iter = uncertains.iterator();
        while (iter.hasNext()) {
            e =  iter.next();
            if (e.getConfidence() == confidence) {
                if (!containsEdge(e.getSuper(), e.getSub())) {
                    Inclusion e2 = addTrivialEdge(e.getSuper(), e.getSub());
                    e2.setConfidence(e.getConfidence());
                    e2.setProper(e.isProper());
                    e2.setRefs(e.getRefs());
                }
                iter.remove();
                i++;
            }
        }
        return i;
    }


    /**
     * Deduce all trivial inclusions. The resulting graph is transitively
     * closed.
     */
    public void findTrivialInclusions() {
        Map<GraphClass,Set<GraphClass> > sccBefore, sccAfter;

        /*markOriginals();*/
        //buildHash();
        sccBefore = GAlg.calcSCCMap(graph);
        separateUncertains();
        if (checking)
            graph.check();
        GAlg.transitiveClosure(graph);
        //buildEdgeCache();

        confidence = Inclusion.CONFIDENCE_HIGHEST;
        iteration = 0;
        do {
            System.out.print("confidence level: ");
            System.out.println(confidence);

            iteration++;
            findTrivialInclusionsOneLevel();

            if (checking)
                graph.check();

            if (uncertains.isEmpty())
                break;

            do {
                confidence--;
            } while (confidence >= Inclusion.CONFIDENCE_LOWEST  &&
                    addUncertains() == 0);
        } while (confidence >= Inclusion.CONFIDENCE_LOWEST);

        /*System.out.println("Adding complements");
        Vector classes = (Vector) nodeList.clone();
        correspondCompAndForbidden2(classes);
        System.out.print("Nodes: "+countNodes());
        System.out.println("     Edges: "+countEdges());*/

        tempify();
        sccAfter = GAlg.calcSCCMap(graph);
        sanityCheckSCC(sccBefore, sccAfter);
        sanityCheckForbidden();
        sanityCheckForbiddenNonEdge();
    }


    /**
     * Deduce all trivial inclusions on the current confidence level.
     * The resulting graph is transitively closed.
     */
    public void findTrivialInclusionsOneLevel() {
        int oldnodes, oldedges;
        ArrayList<GraphClass> lastnewclasses = new ArrayList<GraphClass>();

        newclasses = new ArrayList<GraphClass>(graph.vertexSet());
 
        do {
            oldnodes = graph.vertexSet().size();
            oldedges = graph.edgeSet().size();
            System.out.print("Nodes: "+oldnodes);
            System.out.println("     Edges: "+oldedges);
            lastnewclasses.clear();

            do {
                //Vector curnodes = (Vector) nodeList.clone();
                ArrayList<GraphClass> added = newclasses;
                newclasses = new ArrayList<GraphClass>();
                // Sort added classes by id
                sortByID(added);
                // (A,B)-free ==> A-free \cap B-free
                nodedebugprefix = "#forbidden2intersect";
                forbidden2Intersect(added);
                // A-free \cap B-free ==> (A,B)-free
                nodedebugprefix = "#extendforbidden";
                extendForbidden();              // Uses inclusions!
                // X ==> clique X for clique fixed
                nodedebugprefix = "#addcliquefixed";
                addCliqueFixed(added);
                // X ==> co-X for forbidden and self-compl
                nodedebugprefix = "#addcomplements1";
                addComplements1(added);
                // co-(A-free) ==> (co-A)-free
                nodedebugprefix = "#correspond";
                correspondCompAndForbidden(added);
                // For A\c.p B add co-(A\c.p B) <==> co-A \c.p co-B
                nodedebugprefix = "#addcomplements2";
                addComplements2();
                lastnewclasses.addAll(added);
            } while (newclasses.size() > 0); 
            findTrivialOnce(lastnewclasses);
        } while (graph.edgeSet().size() > oldedges  ||
                graph.vertexSet().size() > oldnodes);
        System.out.print("Nodes: "+ graph.vertexSet().size());
        System.out.println("     Edges: "+ graph.edgeSet().size());
    }


    public void findTrivialOnce(ArrayList<GraphClass> classes) {
        TraceData tr;
        Inclusion e;
        int i, j;

        ArrayList<ComplementClass> complements =
                new ArrayList<ComplementClass>();
        ArrayList<UnionClass> unions = new ArrayList<UnionClass>();
        ArrayList<IntersectClass> intersects = new ArrayList<IntersectClass>();
        ArrayList<HereditaryClass> hereditaries =
                new ArrayList<HereditaryClass>();
        ArrayList<ProbeClass> probes = new ArrayList<ProbeClass>();
        ArrayList<CliqueClass> cliques = new ArrayList<CliqueClass>();

        for (GraphClass g : graph.vertexSet()) {
            if (g instanceof ComplementClass)
                complements.add((ComplementClass) g);
            else if (g instanceof UnionClass)
                unions.add((UnionClass) g);
            else if (g instanceof IntersectClass)
                intersects.add((IntersectClass) g);
            else if (g instanceof HereditaryClass)
                hereditaries.add((HereditaryClass) g);
            else if (g instanceof ProbeClass)
                probes.add((ProbeClass) g);
            else if (g instanceof CliqueClass)
                cliques.add((CliqueClass) g);
        }

        // find direct trivial inclusions and insert corresponding edges
        System.out.println("direct");
        tr = trace ? new TraceData("direct") : null;
        if (graph.vertexSet().size() > classes.size()) {
            // Some classes pre-existing, some new: Check all-new and new-all
            for (GraphClass gi : classes) {
                for (GraphClass gj : graph.vertexSet())  {
                    if (gi == gj)
                        continue;
                    if (!containsEdge(gj, gi)  &&  gi.subClassOf(gj)) {
                        if ((e = addTrivialEdge(gj, gi, tr)) != null)
                            directs.add(e);
                    }
                    if (!containsEdge(gi, gj)  &&  gj.subClassOf(gi)) {
                        if ((e = addTrivialEdge(gi, gj, tr)) != null)
                            directs.add(e);
                    }
                }
            }
        } else {   // All classes are new
            for (GraphClass gi : graph.vertexSet())  {
                //System.out.println(gi.getID());
                for (GraphClass gj : graph.vertexSet()) {
                    if (gi == gj)
                        continue;
                    if (!containsEdge(gj, gi)  &&  gi.subClassOf(gj)) {
                        //System.out.println("   "+ gj.getID() +" -> "+
                                //gi.getID());
                        if ((e = addTrivialEdge(gj, gi, tr)) != null)
                            directs.add(e);
                    }
                }
            }
        }

        // Complement must be handled specially (works in all ways)
        System.out.println("complement");
        for (i = complements.size()-1; i >= 0; i--) {
            for (j = 0; j <= i; j++)    // i=j important for self-compls
                complement(complements.get(i), complements.get(j));
        }

        // Everything works against unions
        System.out.println("union");
        for (UnionClass gi : unions) {
            for (GraphClass gj : graph.vertexSet()) {
                if (gi == gj)
                    continue;
                if (!containsEdge(gj, gi))
                    union(gj, gi);
            }
        }

        // Intersects work against everything
        System.out.println("intersect");
        for (IntersectClass gi : intersects) {
            for (GraphClass gj : graph.vertexSet()) {
                if (gi == gj)
                    continue;
                if (!containsEdge(gi, gj))
                    intersect(gi, gj);
            }
        }

        // Hereditaries work against everything
        System.out.println("hereditary");
        for (HereditaryClass gi : hereditaries) {
            for (GraphClass gj : graph.vertexSet()) {
                if (gi == gj)
                    continue;
                if (!containsEdge(gi, gj))
                    hereditary(gi, gj);
            }
        }

        // Probes work against probes
        System.out.println("probe");
        for (ProbeClass gi : probes) {
            for (ProbeClass gj : probes) {
                if (gi == gj)
                    continue;
                if (!containsEdge(gi, gj))
                    probe(gi, gj);
            }
        }

        // Cliques work against cliques
        System.out.println("clique");
        for (CliqueClass gi : cliques) {
            for (CliqueClass gj : cliques) {
                if (gi == gj)
                    continue;
                if (!containsEdge(gi, gj))
                    clique(gi, gj);
            }
        }
    }


    public void complement(ComplementClass gc1, ComplementClass gc2){
        GraphClass gc3 = gc1.getBase();
        GraphClass gc4 = gc2.getBase();

        if (containsEdge(gc1, gc2))
            addTrivialEdge(gc3, gc4,
                newTraceData("complement", getEdge(gc1, gc2)));
        if (containsEdge(gc3, gc4))
            addTrivialEdge(gc1, gc2,
                newTraceData("complement", getEdge(gc3, gc4)));

        if (containsEdge(gc2, gc1))
            addTrivialEdge(gc4, gc3,
                newTraceData("complement", getEdge(gc2, gc1)));
        if (containsEdge(gc4, gc3))
            addTrivialEdge(gc2, gc1,
                newTraceData("complement", getEdge(gc4, gc3)));

        if (containsEdge(gc1, gc4))
            addTrivialEdge(gc3, gc2,
                newTraceData("complement", getEdge(gc1, gc4)));
        if (containsEdge(gc3, gc2))
            addTrivialEdge(gc1, gc4,
                newTraceData("complement", getEdge(gc3, gc2)));

        if (containsEdge(gc4, gc1))
            addTrivialEdge(gc2, gc3,
                newTraceData("complement", getEdge(gc4, gc1)));
        if (containsEdge(gc2, gc3))
            addTrivialEdge(gc4, gc1,
                newTraceData("complement", getEdge(gc2, gc3)));
    }
   

    /**
     * Can hereditariness properties be used to deduce that gc1 >> gc2?
     * Add an edge accordingly.
     */
    public boolean hereditary(HereditaryClass gc1, GraphClass gc2) {
        TraceData tr = null;
        GraphClass gc3 = gc1.getBase();
        
        if (containsEdge(gc3,gc2)  &&  gc2.getHereditariness().compareTo(
                gc1.getHereditariness()) >= 0) {
            if (trace)
                tr = new TraceData("hereditariness", getEdge(gc3,gc2));
            addTrivialEdge(gc1, gc2, tr);
            return true;
        }
        return false;
    }
    
    
    /**
     * Can probe properties be used to deduce that v1 >> v2?
     * Add an edge accordingly.
     */
    public boolean probe(ProbeClass gc1, ProbeClass gc2) {
        TraceData tr = null;
        GraphClass gc3 = gc1.getBase();
        GraphClass gc4 = gc2.getBase();
        
        if (containsEdge(gc3,gc4)) {
            if (trace)
                tr = new TraceData("probeclass", getEdge(gc3,gc4));
            addTrivialEdge(gc1, gc2, tr);
            return true;
        }
        return false;
    }


    /**
     * Can clique properties be used to deduce that v1 >> v2?
     * Add an edge accordingly.
     */
    public boolean clique(CliqueClass gc1, CliqueClass gc2) {
        TraceData tr = null;
        GraphClass gc3 = gc1.getBase();
        GraphClass gc4 = gc2.getBase();
        
        if (containsEdge(gc3,gc4)) {
            if (trace)
                tr = new TraceData("cliqueclass", getEdge(gc3,gc4));
            addTrivialEdge(gc1, gc2, tr);
            return true;
        }
        return false;
    }


    /**
     * Can intersection rules be used to deduce gc1 >> gc2?
     * Add an edge accordingly.
     */
    public boolean intersect(IntersectClass gc1, GraphClass gc2) {
        Set<GraphClass> hs1;
        ArrayList<Inclusion> traces = new ArrayList<Inclusion>();
        TraceData tr = null;

        hs1 = new HashSet<GraphClass>(gc1.getSet());
        if (gc2 instanceof IntersectClass){
            hs1.removeAll(((IntersectClass)gc2).getSet());
        }else{
            hs1.remove(gc2);
        }
        
        //---- Check that each gc in hs1 is a superclass of v2 ----
        
        // for this part it's important that direct trivial inlcusions
        // have already been found
        for (GraphClass gc : hs1) {
            if(!containsEdge(gc,gc2)) {
                return false;
            } else {
                if (trace)
                    traces.add(getEdge(gc,gc2));
            }
        }
        if (trace)
            tr = new TraceData("intersect", traces);
        addTrivialEdge(gc1, gc2, tr);
        return true;
    }


    /**
     * Can union rules be used to deduce v1 >> v2?
     * Add an edge accordingly.
     */
    public boolean union(GraphClass gc1, UnionClass gc2) {
        Set<GraphClass> hs2;
        ArrayList<Inclusion> traces = new ArrayList<Inclusion>();
        TraceData tr = null;

        hs2 = new HashSet<GraphClass>(((UnionClass)gc2).getSet());
        if (gc1 instanceof UnionClass){
            hs2.removeAll(((UnionClass)gc1).getSet());
        }else{
            hs2.remove(gc1);
        }
        
        //---- Chech that each gc in hs2 is a subclass of v1 ----
        
        // for this part it's important that direct trivial inlcusions
        // have already been found
        for (GraphClass gc : hs2) {
            if (!containsEdge(gc1,gc))
                return false;
            else {
                if (trace)
                    traces.add(getEdge(gc1,gc));
            }
        }
        if (trace)
            tr = new TraceData("union", traces);
        addTrivialEdge(gc1, gc2, tr);
        return true;
    }
    

    public void extendForbidden() {
        int i, k;
        Set<GraphClass> sccVec;
        boolean temp;
        Set hs1, hs2;               // candidates for replaces
        /* These two vectors contain all IntersectClasses and the set of
         * classses whose intersection makes up this IntersectClass. interHS[i]
         * starts from the definition of interGC[i], but as many elements as
         * possible will be replaced by a ForbiddenClass.
         */
        ArrayList<IntersectClass> interGC = new ArrayList<IntersectClass>();
        ArrayList<Set<GraphClass> > interHS =
                new ArrayList<Set<GraphClass> >();
        /* Contains the ForbiddenClass that we will consider replacing into
         * interHS. */
        ArrayList<ForbiddenClass> forbid = new ArrayList<ForbiddenClass>();
        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(graph);
        HashSet hasEqForb = new HashSet();
        
        // Collect interGC, interHS and forbid
        ArrayList<GraphClass> nodes =
                new ArrayList<GraphClass>(graph.vertexSet());
        sortByID(nodes);
allvertices:
        for (GraphClass gc : nodes) {
            if (gc instanceof IntersectClass) {
                interGC.add((IntersectClass) gc);
                interHS.add(((IntersectClass)gc).getSet());
            } else if (gc instanceof ForbiddenClass) {
                if (hasEqForb.contains(gc))
                    continue;           // Nodes in this SCC already added
                sccVec = scc.get(gc);
                if (sccVec.size() <= 1)
                    continue;
                // Only store the nicest definitions for handling
                // Since we go through the nodes by ID and every SCC is added
                // only once, this implies that we add the nicest class, with
                // the lowest id.
                for (GraphClass gc1 : sccVec) {
                    if ( gc1 instanceof ForbiddenClass  &&
                            ((ForbiddenClass) gc1).niceness() >
                            ((ForbiddenClass) gc).niceness() )
                        continue allvertices;
                }

                forbid.add((ForbiddenClass) gc);
                hasEqForb.addAll(sccVec);
            }
        }
        
        /* Replace classes with their forbidden-equivs
         * If there are several forbidden-equivs we use only one!
         */
        for (ForbiddenClass gc : forbid) {
            sccVec = scc.get(gc);
            for (GraphClass gc1 : sccVec) {
                if (gc1 == gc)
                    continue;
                if (gc1 instanceof IntersectClass) {
                    hs1 = ((IntersectClass)gc1).getSet();
                } else {
                    hs1 = new HashSet();
                    hs1.add(gc1);
                }
                for (k = 0; k < interHS.size(); k++) {
                    hs2 = interHS.get(k);
                    if (hs2.containsAll(hs1)) {
                        hs2 = new HashSet(hs2);
                        hs2.removeAll(hs1);
                        hs2.add(gc);
                        interHS.set(k,hs2);
                    }
                }
            }
        }

        //System.out.println("extend and equivs");
        // melt forbidden
        for(i = 0; i < interHS.size(); i++) {
            GraphClass gc = interGC.get(i);
            temp = temporaries.contains(gc)  ||
                    hasEqForb.contains(gc);
            eF(interHS.get(i), gc, temp);
        }
    }

    
    /** Extend the graph DAG by the equivalence between the class ic and the
     * intersection class of icSet. In particular are those members of icSet
     * that are characterized by forbidden subgraphs merged to produce a
     * single, new list of forbidden subgraphs for ic.
     * @param ic IntersectClass
     * @param icSet the GraphClasses that insersect to make up ic
     * @param temp should a possible new GraphClass be created temporary (true)
     * or trivial (false)?
     * @see IntersectClass GraphClass
     */
    private void eF(Set<GraphClass> icSet,GraphClass ic, boolean temp){
        //System.out.println("hash: "+icSet);
        //System.out.println("class: "+ic);
        GraphClass v;
        int cntF = 0;
        HashSet fb = new HashSet();       // Set of forbidden subgraphs
        // Non-forbidden GraphClasses
        HashSet<GraphClass> gcs = new HashSet<GraphClass>();

        // Divide icSet into forbiddens (fb) and others (gcs)
        for (GraphClass gc : icSet) {
            if (gc instanceof ForbiddenClass) {
                fb.addAll(((ForbiddenClass)gc).getSet());
                cntF++;
            } else
                gcs.add(gc);
        }
        
        if (cntF<2)                      // nothing to melt with up to 1 class
            return;
        // found >=2 forbidden classes that are melted, so create new node
        if (temp)
            v = ensureTempNode(new ForbiddenClass(fb));
        else
            v = ensureTrivialNode(new ForbiddenClass(fb));
        
        if (!gcs.isEmpty()) {            // if other classes were found
            gcs.add(v);
            if (temp)
                v = ensureTempNode(new IntersectClass(gcs));
            else
                v = ensureTrivialNode(new IntersectClass(gcs));
        }
        addTrivialEdge(ic, v,
            trace ? new TraceData("extendForbidden") : null);
        addTrivialEdge(v, ic,
                trace ? new TraceData("extendForbidden") : null);
    }
   

    //----------- Creating intersections for forbidden classes ----------

    /**
     * Create an intersection of forbidden subgraph-classes for every
     * forbidden subgraph-class that allows so.
     */
    private void forbidden2Intersect(ArrayList<GraphClass> classes) {
        for (GraphClass gc : classes) {
            if (/*!temporaries.contains(gc) &&*/
                    (gc instanceof ForbiddenClass)) {
                if (((ForbiddenClass) gc).getSet().size() >= 2)
                    addForbiddenSuper((ForbiddenClass) gc);
                else
                    addForbiddenSuperConfig((ForbiddenClass) gc);
            }
        }
    }

    /**
     * For a node (A,B,..)-free add nodes A-free, B-free, ... and A-free \cap
     * B-free \cap ...
     * @param node ForbiddenClass node
     */
    private void addForbiddenSuper(ForbiddenClass node) {
        HashSet one;
        GraphClass sup;
        HashSet supers = new HashSet();
        TraceData tr = trace ? new TraceData("addForbiddenSuper") : null;

        for (Object forb : node.getSet()) {
            one = new HashSet();
            one.add(forb);
            sup = ensureTempNode(new ForbiddenClass(one));
            addForbiddenSuperConfig((ForbiddenClass) sup);
            supers.add(sup);
            addTrivialEdge(sup, node, tr);
        }

        sup = ensureTempNode(new IntersectClass(supers));
        addTrivialEdge(sup, node, tr);
        addTrivialEdge(node, sup, tr);
    }


    /**
     * For a node config-free add nodes config-1-free, config-2-free, ...
     * and config-1-free \cap config-2-free \cap ...
     * @param node ForbiddenClass node
     */
    private void addForbiddenSuperConfig(ForbiddenClass node) {
        HashSet<String> one;
        GraphClass sup;
        HashSet<GraphClass> supers = new HashSet<GraphClass>();
        TraceData tr = trace ? new TraceData("addForbiddenSuperConfig") : null;
        Set<String> isgSet = node.getConfigContains();

        if (isgSet.size() == 0) 
            return;

        if (isgSet.size() == 1) {
            one = new HashSet<String>();
            one.add(isgSet.iterator().next());
            sup = ensureTempNode(new ForbiddenClass(one));
        } else {
            for (String s : isgSet) {
                one = new HashSet<String>();
                one.add(s);
                sup = ensureTempNode(new ForbiddenClass(one));
                supers.add(sup);
                addTrivialEdge(sup, node, tr);
            }
            sup = ensureTempNode(new IntersectClass(supers));
        }
        addTrivialEdge(sup, node, tr);
        addTrivialEdge(node, sup, tr);
    }


    //--------------Establishing relations between complement and -----------
    //-------------------forbidden/union/intersect --------------------------


    /**
     * For every clique-fixed node X add temporary node clique X
     */
    private void addCliqueFixed(ArrayList<GraphClass> classes) {
        GraphClass con;

        for (GraphClass gc : classes) {
            if (gc.isCliqueFixed()) {
                con = ensureTempNode(new CliqueClass(gc));
                addTrivialEdge(gc,con, new TraceData("clique-fixed"));
                addTrivialEdge(con,gc, new TraceData("clique-fixed"));
            }
        }
    }


    /**
     * For every node (A,B,..)-free add temporary node co-((A,B,..)-free)
     * For every self-complementary node X add temporary node co-X
     */
    private void addComplements1(ArrayList<GraphClass> classes) {
        GraphClass con;

        for (GraphClass gc : classes) {
            if (gc instanceof ForbiddenClass) {
                ensureTempNode(new ComplementClass(gc));
            } else if (gc.isSelfComplementary()) {
                con = ensureTempNode(new ComplementClass(gc));
                addTrivialEdge(gc,con, new TraceData("self-complementary"));
                addTrivialEdge(con,gc, new TraceData("self-complementary"));
            }
        }
    }


    /**
     * For every node A\c.p B such that co-A and co-B exists, add temporary
     * node co-(A\c.p B) and co-A\c.p co-B.
     */
    private void addComplements2() {
        ArrayList<GraphClass> classes =
                new ArrayList<GraphClass>(graph.vertexSet());
        sortByID(classes);
        for (GraphClass gc : classes)
            if (gc instanceof UnionClass  ||  gc instanceof IntersectClass)
                doAddComplements2(gc);
    }


    /**
     * For a node A\c.p B check whether co-A and co-B exists, and, if so,
     * add temporary nodes co-(A\c.p B) and co-A\c.p co-B.
     * Return true if the graph could be handled successfully.
     */
    private boolean doAddComplements2(GraphClass gc) {
        GraphClass gcco, cogc;
        Set<GraphClass> parts = null;
        TraceData tr = trace ? new TraceData("addComplements2") : null;

        //---- Try to create complement
        cogc = gc.complement();
        if (cogc instanceof SetClass)
            parts = ((SetClass) cogc).getSet();
        else
            throw new IllegalArgumentException("Wrong classtype "+ gc);
        for (GraphClass p : parts) {
            if (!graph.containsVertex(p))
                return false;
        }

        gcco = ensureTempNode(new ComplementClass(gc));
        cogc = ensureTempNode(cogc);
        addTrivialEdge(cogc, gcco, tr);
        addTrivialEdge(gcco, cogc, tr);
        return true;
    }

    
    /**
     * For every node co-((A,B,..)-free) add trivial equal node
     * (co-A,co-B,..)-free
     */
    private void correspondCompAndForbidden(ArrayList<GraphClass> classes) {
        TraceData tr = trace ?
                new TraceData("correspondCompAndForbidden") : null;
        GraphClass gc2, gc3;
        for (GraphClass gc1 : classes) {
            if (gc1 instanceof ComplementClass) {
                gc2 = ((ComplementClass) gc1).getBase();
                if (gc2 instanceof ForbiddenClass) {
                    if ( temporaries.contains(gc2) )
                        gc3 = ensureTempNode(
                                ((ForbiddenClass)gc2).complement());
                    else
                        gc3 = ensureTrivialNode(
                                ((ForbiddenClass)gc2).complement());
                    addTrivialEdge(gc1, gc3, tr);
                    addTrivialEdge(gc3, gc1, tr);
                }
            }
        }
    }

    
    /**
     * For all non-temporary nodes (co-A,...)-free create an equivalent class
     * co-X, if (A,...)-free has a non temporary basic equivalent class X.
     * 
     * We actually start at co-((A,..)-free) and from there find (co-A,..)-free
     * and X.
     *
     * N.B. This method is currently not used.
     * N.B.2. It also creates complements for self-complementary classes.
     */
    /*private void correspondCompAndForbidden2(Vector classes) {
        TraceData tr = trace ?
                new TraceData("correspondCompAndForbidden2") : null;
        ISGCINode node1, node;
        GraphClass gc1, gc2, basic;
        Vector equ1, equ2;
        boolean ok1, ok2;
        int i;
        int scc = findSCC();

        Enumeration eenum = classes.elements();
nextnode:
        while (eenum.hasMoreElements()) {

            //---- Find gc1 = co-(A-free), gc2 = A-free
            node1 = (ISGCINode) eenum.nextElement();
            gc1 = node1.getGraphClass();
            if (!(gc1 instanceof ComplementClass))
                continue;
            gc2 = ((ComplementClass) gc1).getBase();
            if (!(gc2 instanceof ForbiddenClass))
                continue;

            //System.out.println("found "+ gc1 +" co of: "+ gc2);
            
            //---- Check that gc1 has non-temp equivs
            equ1 = (Vector) node1.getData(scc);
            for (i = equ1.size()-1; i >= 0; i--) {
                if ( ((Node) equ1.elementAt(i)).getData(tempIx) != temporary)
                    break;
                if (i == 0)
                    continue nextnode;
            }

            //System.out.println("gc1 ok");
            //---- Go through basic equivs of gc2 (basic implies non-temp)
            equ2 = (Vector) ((Node) hash.get(gc2)).getData(scc);
            for (i = equ2.size()-1; i >= 0; i--) {
                basic = ((ISGCINode) equ2.elementAt(i)).getGraphClass();
                if ( basic.getClass() != BaseClass.class)
                    continue;
                //System.out.println("gc2 ok: "+ basic);
                node = ensureTrivialNode(basic.complement());
                addTrivialEdge(node1, node, tr);
                addTrivialEdge(node, node1, tr);
            }
        }
        releaseData(scc);
    }*/

    //------------------------ Proper inclusions ---------------------------

    /**
     * Mark (already found) inclusions as proper.
     */
    public void findTrivialPropers() {
        int i, j;
        boolean newproper;
        ArrayList<Inclusion> propers = new ArrayList<Inclusion>();
        ArrayList<ComplementClass> compls = new ArrayList<ComplementClass>();
        ArrayList<ProbeClass> probes = new ArrayList<ProbeClass>();
        ArrayList<CliqueClass> cliques = new ArrayList<CliqueClass>();
        ArrayList<HereditaryClass> hereds = new ArrayList<HereditaryClass>();

        System.out.println("findTrivialPropers");

        //---- Gather classes according to type
        for (GraphClass gi : graph.vertexSet()) {
            if (gi instanceof ComplementClass)
                compls.add((ComplementClass) gi);
            else if (gi instanceof ProbeClass)
                probes.add((ProbeClass) gi);
            else if (gi instanceof CliqueClass)
                cliques.add((CliqueClass) gi);
            else if (gi instanceof HereditaryClass)
                hereds.add((HereditaryClass) gi);

        }

        //---- Ensure transitivity for masterdata propers
        for (Inclusion e : graph.edgeSet()) {
            if (e.isProper())
                propers.add(e);
        }
        for (Inclusion e : propers) {
            e.setProper(false);
            setProper(e, null);
        }
        propers = null;         // Free memory

        //---- Direct propers
        properForbiddenDirect();
        properCliqueDirect(cliques);
        properProbeDirect(probes);
        properHereditaryDirect(hereds);

        //---- Repeatedly deduce properness of inclusions
        do {
            newproper = false;
            System.out.println("complement");
            for (i = compls.size()-1; i >= 0; i--) {
                for (j = 0; j <= i; j++)    // i=j important for self-compls
                    newproper = newproper |
                            properFromComplement(compls.get(i), compls.get(j));
            }

            System.out.println("probes");
            newproper = newproper | properProbes(probes);
            
            System.out.println("forbidden");
            newproper = newproper | properForbiddenSub();
        } while (newproper);

        sanityCheckProper();
    }


    /**
     * Mark inclusions between forbidden classes as proper if their definition
     * warrants so.
     */
    private void properForbiddenDirect() {
        GraphClass from, to;

        for (Inclusion e : graph.edgeSet()) {
            from = graph.getEdgeSource(e);
            to = graph.getEdgeTarget(e);
            if ( ! (from instanceof ForbiddenClass)  ||
                    ! (to instanceof ForbiddenClass) )
                continue;

            // If the classes are equal, the reverse relation should be found
            // from the forbidden graphs. If it doesn't exist -> proper
            if (!containsEdge(to, from))
                setProper(e, new TraceData("properForbiddenDirect"));
        }
    }


    /**
     * Mark A > B proper if A has a forbidden subclass that is not a subclass
     * of B, which is itself a forbidden class.
     * Return true iff something new was deduced.
     */
    private boolean properForbiddenSub() {
        GraphClass from, to;
        boolean res = false;

        for (Inclusion e : graph.edgeSet()) {
            from = graph.getEdgeSource(e);
            to = graph.getEdgeTarget(e);

            if (e.isProper()  ||  containsEdge(to, from)  ||
                    from instanceof ForbiddenClass ||
                    !(to instanceof ForbiddenClass))
                continue;

            //---- Test all forbidden subs of from
            for (GraphClass v : GAlg.outNeighboursOf(graph, from)) {
                if (v == to || !(v instanceof ForbiddenClass))
                    continue;

                if (!containsEdge(v, to)  &&  !containsEdge(to, v)) {
                    setProper(e, newTraceData("properForbiddenSub "+
                        from +" -> "+ to, null));
                    res = true;
                }
            }
        }
        return res;
    }


    /**
     * Mark clique X < clique unless we already know otherwise.
     */
    private void properCliqueDirect(List<CliqueClass> cliques) {
        GraphClass clique = null;
        for (GraphClass gc : graph.vertexSet()) {
            if ("gc_141".equals(gc.getID())) {
                if (!"clique".equals(gc.toString())) // Safety check
                    throw new RuntimeException(
                            "gc_141 expected to be clique graphs");
                clique = gc;
                break;
            }
        }

        for (CliqueClass vi : cliques) {
            if (!containsEdge(vi, clique))
                setProper(getEdge(clique, vi),
                        newTraceData("properCliqueDirect", null));
        }
    }


    /**
     * Mark X < probe X unless we already know otherwise.
     */
    private void properProbeDirect(List<ProbeClass> probes) {
        GraphClass basei;

        for (ProbeClass vi : probes) {
            basei = vi.getBase();
            if (!containsEdge(basei, vi))
                setProper(getEdge(vi, basei),
                        newTraceData("properProbeDirect", null));
        }
    }


    /**
     * Mark hereditary X < X unless we already know otherwise.
     */
    private void properHereditaryDirect(List<HereditaryClass> hereds) {
        GraphClass basei;

        for (HereditaryClass vi : hereds) {
            basei = vi.getBase();
            if (!containsEdge(vi, basei))
                setProper(getEdge(basei, vi),
                        newTraceData("properHereditaryDirect", null));
        }
    }


    /**
     * Mark probe X < probe Y if X < Y, unless we already know otherwise.
     * Return true iff something new was deduced.
     */
    private boolean properProbes(List<ProbeClass> probes) {
        ProbeClass vi, vj;
        int i, j;
        boolean res = false;

        for (i = 0; i < probes.size()-1; i++) {
            vi = probes.get(i);
            for (j = i+1; j <  probes.size(); j++) {
                vj =  probes.get(j);
                if (containsEdge(vi, vj))
                    res = res | properFromProbe(vi, vj);
                if (containsEdge(vj, vi))
                    res = res | properFromProbe(vj, vi);
            }
        }
        return res;
    }


    /**
     * Mark probe X < probe Y if X < Y, unless we already know otherwise.
     * Return true iff something new was deduced.
     */
    private boolean properFromProbe(ProbeClass x, ProbeClass y) {
        GraphClass basex, basey;
        Inclusion e, basee;
        int i, j;

        basex = x.getBase();
        basey = y.getBase();
        e = getEdge(x, y);
        basee = getEdge(basex, basey);
        if (!e.isProper()  &&  !containsEdge(y, x)  &&
                basee != null  &&  basee.isProper()) {
            setProper(e, newTraceData("ProperFromProbe", basee));
            return true;
        }

        return false;
    }


    /**
     * Given two complement classes v1, v2, try to deduce properness of
     * inclusions in all possible directions.
     * Return true iff something new was deduced.
     */
    public boolean properFromComplement(ComplementClass v1,
            ComplementClass v2) {
        GraphClass v3 = v1.getBase();
        GraphClass v4 = v2.getBase();
        Inclusion e, f;
        boolean res = false;

        if (containsEdge(v1, v2)  &&  (e = getEdge(v1, v2)) != null  &&
                e.isProper()  &&  !(f = getEdge(v3, v4)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }
        if (containsEdge(v3, v4)  &&  (e = getEdge(v3, v4)) != null  &&
                e.isProper()  &&  !(f = getEdge(v1, v2)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }

        if (containsEdge(v2, v1)  &&  (e = getEdge(v2, v1)) != null  &&
                e.isProper()  &&  !(f = getEdge(v4, v3)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }
        if (containsEdge(v4,v3)  &&  (e = getEdge(v4, v3)) != null  &&
                e.isProper()  &&  !(f = getEdge(v2, v1)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }

        if (containsEdge(v1, v4)  &&  (e = getEdge(v1, v4)) != null  &&
                e.isProper()  &&  !(f = getEdge(v3, v2)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }
        if (containsEdge(v3, v2)  &&  (e = getEdge(v3, v2)) != null  &&
                e.isProper()  &&  !(f = getEdge(v1, v4)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }

        if (containsEdge(v4, v1)  &&  (e = getEdge(v4, v1)) != null  &&
                e.isProper()  &&  !(f = getEdge(v2, v3)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }
        if (containsEdge(v2, v3)  &&  (e = getEdge(v2, v3)) != null  &&
                e.isProper()  &&  !(f = getEdge(v4, v1)).isProper()) {
            setProper(f, newTraceData("properFromComplement", e));
            res = true;
        }

        return res;
    }

    //------------------------ Sanity checks ---------------------------
    
    /**
     * Print SCC that have merged as a result of deducing inclusions.
     */
    private void sanityCheckSCC(
            Map<GraphClass,Set<GraphClass> > before,
            Map<GraphClass,Set<GraphClass> > after) {
        Set<GraphClass> vecBefore1, vecAfter1, vecBefore2, vecAfter2;
        // Maps after-SCC to beforeSCCs
        HashMap<Set<GraphClass>, Set<Set<GraphClass> > > scc =
                new HashMap<Set<GraphClass>, Set<Set<GraphClass> > >();
        HashSet<Set<GraphClass> > hs;

        for (GraphClass node1 : graph.vertexSet()) {
            if (before.get(node1) == null)
                continue;
            vecBefore1 = before.get(node1);
            vecAfter1 = after.get(node1);

            for (GraphClass node2 : graph.vertexSet()) {
                if (before.get(node2) == null)
                    continue;
                vecBefore2 = before.get(node2);
                vecAfter2 = after.get(node2);

                if (vecBefore1 != vecBefore2  &&  vecAfter1 == vecAfter2) {
                    if (scc.containsKey(vecAfter1))
                        scc.get(vecAfter1).add(vecBefore1);
                    else {
                        hs = new HashSet<Set<GraphClass> >();
                        hs.add(vecBefore1);
                        scc.put(vecAfter1, hs);
                    }
                    
                    if (scc.containsKey(vecAfter2))
                        scc.get(vecAfter2).add(vecBefore1);
                    else {
                        hs = new HashSet<Set<GraphClass> >();
                        hs.add(vecBefore1);
                        scc.put(vecAfter2, hs);
                    }
                }
            }
        }

        System.out.println("sanityCheckSCC");
        for (Map.Entry<Set<GraphClass>, Set<Set<GraphClass> > > entry :
                scc.entrySet()) {
            vecAfter1 = entry.getKey();
            System.out.println("sccBefore: ");
            for (Set<GraphClass> bfor : entry.getValue()) {
                System.out.print("[");
                for (GraphClass gc : bfor)
                    System.out.print( gc.getID() +" ("+ gc.toString() +"), ");
                System.out.println("]");
            }
            System.out.print("sccAfter:\n[");
            for (GraphClass gc : vecAfter1)
                System.out.print( gc.getID() +" ("+ gc.toString() +"), ");
            System.out.println("]");
        }
        System.out.println("end sanityCheckSCC");
    }


    /**
     * Prints inclusions between ForbiddenClasses that were derived, but can't
     * be confirmed by ForbiddenClass.subClassOf.
     */
    private void sanityCheckForbidden() {
        GraphClass from, to;
        HashSet<GraphClass> hs = new HashSet<GraphClass>();
        DirectedGraph<GraphClass,Inclusion> inducedSub;
        
        for (Inclusion e : graph.edgeSet()) {
            from = graph.getEdgeSource(e);
            to = graph.getEdgeTarget(e);
            if (from instanceof ForbiddenClass && to instanceof ForbiddenClass)
                if (!to.subClassOf(from)) {
                    hs.add(from);
                    hs.add(to);
                }
        }
        if (hs.isEmpty())
            return;
            
        //TODO
        /*inducedSub = (ISGCIGraph) createSubgraph(hs.elements());
        inducedSub.contractSCC();
        inducedSub.transitiveReduction();*/
        inducedSub = new SimpleDirectedGraph<GraphClass,Inclusion>(
                Inclusion.class);
        GAlg.copyInduced(graph, hs, inducedSub);
        
        System.out.println("sanityCheckForbidden");
        for (Inclusion e : inducedSub.edgeSet()) {
            from = inducedSub.getEdgeSource(e);
            to = inducedSub.getEdgeTarget(e);
            if (!to.subClassOf(from))
                System.out.println(from +" ("+ from.getID()+ ") -> "+
                    to +" ("+ to.getID() +") ");
        }
        System.out.println("end sanityCheckForbidden");
    }


    /**
     * Prints pairs of ForbiddenClasses that have no inclusion, but no witness
     * for this is found.
     */
    private void sanityCheckForbiddenNonEdge() {
        System.out.println("begin sanityCheckForbiddenNonEdge");
        for (GraphClass gc1 : graph.vertexSet()) {
            if (!(gc1 instanceof ForbiddenClass))
                continue;
            for (GraphClass gc2 : graph.vertexSet()) {
                if (gc2 == gc1  ||  !(gc2 instanceof ForbiddenClass)  ||
                        containsEdge(gc1, gc2))
                    continue;

                StringBuilder s = new StringBuilder();
                boolean b = ((ForbiddenClass) gc2).notSubClassOf(
                        (ForbiddenClass) gc1, s);
                if (!b)
                    System.out.println("Unconfirmed non-inclusion "+
                            gc1 + " ("+ gc1.getID()+ ") -> "+
                            gc2 +" ("+ gc2.getID() +") ");
            }
        }
        System.out.println("end sanityCheckForbiddenNonEdge");
    }

    
    /**
     * Check consistency for disjoint/incomparable relations.
     */
    public void sanityCheckAbstractRelations(
            Collection<AbstractRelation> relations) {
        System.out.println("begin sanityCheckAbstractRelations");
        for (AbstractRelation r : relations) {
            if (r instanceof Incomparability) {
                if (containsEdge(r.get1(), r.get2())  ||
                        containsEdge(r.get2(), r.get1()))
                    System.out.println("Inclusion exists for "+ r);
                continue;
            }

            if (!(r instanceof Disjointness))
                throw new RuntimeException("Unknown relation"+ r);

            for (GraphClass gc1 : new Itera<GraphClass>(Iterators.union(
                    Iterators.singleton(r.get1()),
                    GAlg.outNeighboursOf(graph, r.get1()))))
                for (GraphClass gc2 : new Itera<GraphClass>(Iterators.union(
                        Iterators.singleton(r.get2()),
                        GAlg.outNeighboursOf(graph, r.get2())))) {
                    if (containsEdge(gc1, gc2))
                        System.out.println("Inclusion "+ getEdge(gc1, gc2) +
                                " exists for "+ r);
                    if (containsEdge(gc2, gc1))
                        System.out.println("Inclusion "+ getEdge(gc2, gc1) +
                                " exists for "+ r);
                    if (gc1.getHereditariness() == GraphClass.Hered.INDUCED &&
                           gc2.getHereditariness() == GraphClass.Hered.INDUCED)
                        System.out.println("Induced-hereditary subclasses "+
                                gc1 +" "+ gc2 +" exists for "+ r);
                }
        }
        System.out.println("end sanityCheckAbstractRelations");
    }


    /**
     * Proper subclasses cannot be equivalent.
     */
    private void sanityCheckProper() {
        GraphClass from, to;

        System.out.println("sanityCheckProper");
        for (Inclusion e : graph.edgeSet()) {
            if (!e.isProper())
                continue;
            from = graph.getEdgeSource(e);
            to = graph.getEdgeTarget(e);
            if (containsEdge(to, from))
                System.out.println(e);
        }
        System.out.println("end sanityCheckProper");
    }


    //---------------------- Various supporting functions --------------------



    /**
     * Sort the given list of nodes by id.
     */
    private void sortByID(List<GraphClass> list) {
        Collections.sort(list, new Comparator<GraphClass>() {
                public int compare(GraphClass o1, GraphClass o2) {
                    String s1 = ( o1).getID();
                    String s2 = ( o2).getID();
                    boolean orig1 = s1.startsWith("gc");
                    boolean orig2 = s2.startsWith("gc");
                    if (orig1  &&  !orig2)
                        return -1;
                    if (orig2  &&  !orig1)
                        return 1;
                    return s1.compareTo(s2);
                }
        });
    }


    /**
     * Go through all nodes and if several Forbidden classes are equivalent,
     * mark as many of them as possible as temporary.
     * After this, go through all SetClasses and mark classes that contain
     * temporary classes as temporary themselves.
     */
    public void tempify() {
        ArrayList<ForbiddenClass> forb = new ArrayList<ForbiddenClass>();
        List<Set<GraphClass> > sccs = GAlg.calcSCCList(graph);

        for (Set<GraphClass> eqs : sccs) {
            if (eqs.size() <= 1)
                continue;
            forb.clear();
            for (GraphClass nj : eqs) {
                if (nj instanceof ForbiddenClass)
                    forb.add((ForbiddenClass) nj);
            }
            if (forb.size() <= 1)
                continue;
            tempEqForbidden(forb);
        }

        for (GraphClass gc : graph.vertexSet()) {
            if (!(gc instanceof SetClass)  ||  temporaries.contains(gc))
                continue;
            for (GraphClass gc2 : ((SetClass) gc).getSet()) {
                if (temporaries.contains(gc2)) {
                    temporaries.add(gc);
                    System.out.println("Tempified "+ gc.getID() +" "+ gc);
                    break;
                }
            }
        }

    }


    /**
     * Given a set of equivalent Forbidden classes, mark as many of them as
     * possible as temporary.
     */
    public void tempEqForbidden(Collection<ForbiddenClass> v) {
        if (v.size() < 2)
            return;

        // original classes
        ArrayList<ForbiddenClass> orig = new ArrayList<ForbiddenClass>();
        // new, not temporary classes
        ArrayList<ForbiddenClass> auto = new ArrayList<ForbiddenClass>();
        // new, temporary classes;
        ArrayList<ForbiddenClass> temp = new ArrayList<ForbiddenClass>();

        //---- Categorize the classes
        for (ForbiddenClass ni : v) {
            if (temporaries.contains(ni))
                temp.add(ni);
            else if (ni.getID().startsWith("AUTO"))
                auto.add(ni);
            else
                orig.add(ni);
        }

        if (auto.size() == 0  ||  auto.size() + orig.size() <= 1)
            return;

        //---- Have orig -> temporize all new
        if (orig.size() > 0) {
            temporaries.addAll(auto);
            return;
        }

        //---- Keep only classes with minimal number of families,configurations
        int best = Integer.MIN_VALUE;
        for (ForbiddenClass ni : auto) {
            if (ni.niceness() > best)
                best = ni.niceness();
        }

        for (ForbiddenClass ni : auto) {
            if (ni.niceness() < best)
                temporaries.add(ni);
        }
    }


    /**
     * Remove nodes marked as temporary.
     */
    public void removeTemp() {
        for (GraphClass temp : temporaries)
            graph.removeVertex(temp);
    }


    /**
     * Add a trivially deduced inclusion.
     */
    private Inclusion addTrivialEdge(GraphClass from, GraphClass to,
            TraceData tr) {
        if (from == to  ||  containsEdge(from, to))
            return null;
        //System.out.println(from.getGraphClass() +" -> "+ to.getGraphClass());
        Inclusion e = addEdge(from, to);
        if (trace)
            traceAnn.setEdge(e, tr);
        /*e.setData(orgIx, null);*/

        //---- Maintain transitivity
        Inclusion trans;
        // Collect out-neighbours of to in tos
        GraphClass[] tos = new GraphClass[graph.outDegreeOf(to)];
        int i = 0;
        for (GraphClass g : GAlg.outNeighboursOf(graph, to))
            tos[i++] = g;
        
        // Add from -> tos
        for (i = 0; i < tos.length; i++)
            if (from != tos[i]  &&  !containsEdge(from, tos[i])) {
                if (from.equals(tos[i]))
                    System.err.println("Equals: "+ from.getID() +" "+ from +
                    " = "+ tos[i].getID() +" "+ tos[i]);
                trans = addEdge(from, tos[i])/*.setData(orgIx, null)*/;
                if (trace) {
                    traceAnn.setEdge(trans, new TraceData("Transitivity", e,
                            getEdge(to, tos[i])));
                }
            }

        for (GraphClass v : GAlg.inNeighboursOf(graph, from)) {
            // add super(from) -> to
            if (v != to  &&  !containsEdge(v, to)) {
                if (v.equals(to))
                    System.err.println("Equals: "+
                        v.getID() +" "+ v +" = "+ to.getID() +" "+ to);
                trans = addEdge(v, to);
                if (trace)
                    traceAnn.setEdge(trans,
                           new TraceData("Transitivity", getEdge(v, from), e));
            }
            // add super(from) -> tos
            for (i = 0; i < tos.length; i++)
                if (v != tos[i]  &&  !containsEdge(v, tos[i])) {
                    if (v.equals(tos[i]))
                        System.err.println("Equals: "+ v.getID() +" "+ v +
                        " = "+ tos[i].getID() +" "+ tos[i]);
                    trans = addEdge(v, tos[i])/*.setData(orgIx, null)*/;
                    if (trace) {
                        traceAnn.setEdge(trans,
                               new TraceData("Transitivity", getEdge(v, from),
                                       e, getEdge(to, tos[i])));
                    }
                }
        }

        return e;
    }


    private Inclusion addTrivialEdge(GraphClass from, GraphClass to) {
        return addTrivialEdge(from, to, null);
    }


    /**
     * If a node for graphclass gc exists, return it, otherwise add it as a
     * trivial node.
     * @param gc GraphClass to ensure a node for
     */
    private GraphClass ensureTrivialNode(GraphClass gc) {
        GraphClass n = graph.findVertex(gc);
        if (n == null)
            return addTrivialNode(gc);

        temporaries.remove(n);
        return n;
    }


    /**
     * If a node for graphclass gc exists, return it, otherwise add it as a
     * temporary node.
     * @param gc GraphClass to ensure a node for
     */
    private GraphClass ensureTempNode(GraphClass gc) {
        GraphClass n = graph.findVertex(gc);
        return n == null ? addTempNode(gc) : n;
    }


    /**
     * Add a new trivially deduced node.
     * The new node is printed in XML to stderr.
     */
    private GraphClass addTrivialNode(GraphClass gc) {

        // Intersections of temp classes are temp themselves
        if (gc instanceof IntersectClass) {
            for (Object o : ((IntersectClass) gc).getSet())
                if (temporaries.contains((GraphClass) o))
                    return addTempNode(gc);

        }

        GraphClass v = doAddTrivialNode(gc);
        //System.out.println("new: "+gc);
        return v;
    }


    /**
     * Add a temporary trivially deduced node.
     */
    private GraphClass addTempNode(GraphClass gc) {
        GraphClass v = doAddTrivialNode(gc);
        temporaries.add(v);
        //System.out.println("new (temp): "+gc);
        return v;
    }


    private GraphClass doAddTrivialNode(GraphClass gc) {
        graph.addVertex(gc);
        gc.setID(idgenerator.getID(gc.toString()));
        temporaries.remove(gc);
        newclasses.add(gc);
        /*System.out.print(iteration);
        System.out.print("\t");
        System.out.print(nodedebugprefix);
        System.out.print("\t");
        System.out.print(gc.getID());
        System.out.print("\t");
        System.out.println(gc);*/
        return gc;
    }


    /**
     * Add an edge.
     */
    public Inclusion addEdge(GraphClass src, GraphClass dest) {
        Inclusion e = graph.addEdge(src, dest);
        e.setConfidence(confidence);
        return e;
    }


    /**
     * Return true iff graph contains an edge between from and to.
     */
    public boolean containsEdge(GraphClass from, GraphClass to) {
        return graph.containsEdge(from, to);
    }


    /**
     * Look for an edge that starts at node <code>from</code> and ends at node
     * <code>to</code>.
     *
     * @param from the start node
     * @param to   the target node
     * return an appropriate edge or <tt>null</tt> if no such edge was found
     */
    public Inclusion getEdge(GraphClass from, GraphClass to) {
        if (from==null || to==null) {
            throw new NullPointerException("node can't be null");
        }
        return graph.findEdge(from, to);
    }


    /**
     * Remove an edge.
     */
    public void removeEdge(Inclusion e) {
        graph.removeEdge(e);
    }


    /**
     * Set the properflag for an edge to true and do this for the transitive
     * inclusions as well.
     */
    public void setProper(Inclusion e, TraceData t) {
        if (e == null  ||  e.isProper())
            return;

        e.setProper(true);
        if (trace)
            traceRelAnn.setEdge(e, t);

        GraphClass from = graph.getEdgeSource(e);
        GraphClass to = graph.getEdgeTarget(e);
 
        // Maintain transitivity
        Inclusion trans;
        // Collect out-neighbours of to in tos
        GraphClass[] tos = new GraphClass[graph.outDegreeOf(to)];
        int i = 0;
        for (GraphClass g : GAlg.outNeighboursOf(graph, to))
            tos[i++] = g;
        
        for (i = 0; i < tos.length; i++)
            if (from != tos[i]  &&  containsEdge(from, tos[i])  &&
                    (trans = getEdge(from, tos[i])) != null &&
                    !trans.isProper()) {
                trans.setProper(true);
                if (trace)
                    traceRelAnn.setEdge(trans, newTraceData("Transitivity",e));
            }

        for (GraphClass v : GAlg.inNeighboursOf(graph, from)) {
            // add super(from) -> to
            if (v != to  &&  containsEdge(v, to)  &&
                    (trans = getEdge(v, to)) != null &&
                    !trans.isProper()) {
                trans.setProper(true);
                if (trace)
                    traceRelAnn.setEdge(trans, newTraceData("Transitivity",e));
            }
            // add super(from) -> tos
            for (i = 0; i < tos.length; i++)
                if (v != tos[i]  &&  containsEdge(v, tos[i])  &&
                        (trans = getEdge(v, tos[i])) != null &&
                        !trans.isProper()) {
                    trans.setProper(true);
                    if (trace)
                        traceRelAnn.setEdge(trans,
                                newTraceData("Transitivity",e));
                }
        }
    }


    /**
     * Print a trace for the given edge to writer.
     */
    public void printTrace(PrintWriter writer, Inclusion e) {
        if (trace)
            TraceData.print(writer, e, traceAnn);
    }


    /**
     * Print a trace for the given edge to writer.
     */
    public void printRelationTrace(PrintWriter writer, Inclusion e) {
        if (trace  &&  e.isProper())
            TraceData.print(writer, e, traceRelAnn);
    }


    /**
     * Create new tracedata, if the trace flag is set.
     */
    public TraceData newTraceData(String desc, Inclusion a) {
        if (trace)
            return new TraceData(desc, a);
        else
            return null;
    }


    /**
     * Create new tracedata, if the trace flag is set.
     */
    public TraceData newTraceData(String desc, Inclusion a, Inclusion b) {
        if (trace)
            return new TraceData(desc, a, b);
        else
            return null;
    }
}

//============================== TraceData ==================================

//TODO Use varargs
class TraceData {
    String desc;                        // Description.
    ArrayList<Inclusion> prereqs;       // Prerequisite edges.

    public TraceData() {
        prereqs = new ArrayList<Inclusion>();
    }

    public TraceData(TraceData t) {
        this.desc = t.desc;
        this.prereqs = (ArrayList<Inclusion>) t.prereqs.clone();
    }

    public TraceData(String desc) {
        this();
        set(desc);
    }

    public TraceData(String desc, Inclusion a) {
        this();
        set(desc, a);
    }

    public TraceData(String desc, Inclusion a, Inclusion b) {
        this();
        set(desc, a, b);
    }

    public TraceData(String desc, Inclusion a, Inclusion b, Inclusion c) {
        this();
        set(desc, a, b, c);
    }

    public TraceData(String desc, Inclusion a, Inclusion b, Inclusion c,
            Inclusion d) {
        this();
        set(desc, a,b,c,d);
    }

    public TraceData(String desc, ArrayList<Inclusion> v) {
        this();
        set(desc, v);
    }

    public TraceData set(String desc) {
        this.desc = desc;
        return this;
    }

    public TraceData set(String desc, Inclusion a) {
        this.desc = desc;
        prereqs.clear();
        prereqs.add(a);
        return this;
    }

    public TraceData set(String desc, Inclusion a, Inclusion b) {
        this.desc = desc;
        prereqs.clear();
        prereqs.add(a);
        prereqs.add(b);
        return this;
    }

    public TraceData set(String desc, Inclusion a, Inclusion b, Inclusion c) {
        this.desc = desc;
        prereqs.clear();
        prereqs.add(a);
        prereqs.add(b);
        prereqs.add(c);
        return this;
    }

    public TraceData set(String desc, Inclusion a, Inclusion b, Inclusion c,
            Inclusion d) {
        this.desc = desc;
        prereqs.clear();
        prereqs.add(a);
        prereqs.add(b);
        prereqs.add(c);
        prereqs.add(d);
        return this;
    }

    public TraceData set(String desc, ArrayList<Inclusion> v) {
        this.desc = desc;
        prereqs = (ArrayList<Inclusion>) v.clone();
        return this;
    }

    public static void print(PrintWriter writer, Inclusion e,
            Annotation<GraphClass,Inclusion,TraceData> traceAnn) {
        print(writer, e, traceAnn, "");
    }

    private static void print(PrintWriter writer, Inclusion edge,
            Annotation<GraphClass,Inclusion,TraceData> traceAnn,
            String prefix) {
        writer.print(prefix);
        writer.print(edge);
        writer.print("  ");
        TraceData td = traceAnn.getEdge(edge);
        if (td != null) {
            writer.print(td.desc);
            writer.println();
            for (Inclusion e : td.prereqs) {
                writer.print(prefix+" ");
                writer.println(e);
            }
        } else {
            writer.print("(no tracedata)");
            writer.println();
        }
    }
}

/* EOF */
