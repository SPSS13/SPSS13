/*
 * Problems on graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/Problem.java,v 2.1 2011/09/29 15:08:59 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.jgrapht.DirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.gc.*;
import teo.isgci.db.Note;


/**
 * Stores the information about a graph problem.
 */
public class Problem {

    protected String name;
    /** Inclusion graph */
    protected DirectedGraph<GraphClass,Inclusion> graph;
    /** Stores complexity information on a graph class */
    protected Annotation<GraphClass,Inclusion,Complexity> complexAnn;
    /** Stores algorithms on a graph class */
    protected Annotation<GraphClass,Inclusion,ProblemOnNode> algoAnn;
    /** More/less general problems */
    protected List<Reduction> parents;
    protected List<Reduction> children;
    /** Solving this on G is polytime equivalent to solving complement on co-G
     */
    protected Problem complement;
    /** The algorithms (node independent) that solve depending on co-G. */
    protected List<Algorithm> coAlgos;
    /** References for this problem */
    List refs;

    protected Problem(String name, DirectedGraph<GraphClass,Inclusion> g) {
        this(name, g, null);
    }

    /**
     * Create a problem.
     * @param name the name of the problem ("Independent set")
     * @param g the graph of classes for which the problem exists
     * @param complement the complement of the problem (Clique)
     */
    protected Problem(String name, DirectedGraph<GraphClass,Inclusion> g,
            Problem complement){
        this.name = name;
        this.graph = g;
        setComplement(complement);
        this.parents = new ArrayList<Reduction>();
        this.children = new ArrayList<Reduction>();
        this.complexAnn = new Annotation<GraphClass,Inclusion,Complexity>(g);
        this.algoAnn = deducing ?
                new Annotation<GraphClass,Inclusion,ProblemOnNode>(g) : null;
        this.coAlgos = null;
    }

    public String getName() {
        return name;
    }

    /**
     * Adds a new reduction parent->this with cost c.
     */
    public void addReduction(Problem parent, Complexity c) {
        Reduction r = new Reduction(this, parent, c);
        this.parents.add(r);
        parent.children.add(r);
    }

    /**
     * Return the reductions from other problems to this.
     */
    public Iterator<Reduction> getReductions() {
        return parents.iterator();
    }


    public void setComplement(Problem thecomplement) {
        this.complement = thecomplement;
        if (thecomplement != null)
            thecomplement.complement = this;
    }

    public Problem getComplement() {
        return complement;
    }

    public void setRefs(List refs) {
        this.refs = refs;
    }

    public List getRefs() {
        return refs;
    }

    public String toString() {
        return getName();
    }

    /**
     * Create a new problem with the given name and graph;
     */
    public static Problem createProblem(
            String name, DirectedGraph<GraphClass,Inclusion> g) {
        Problem p = null;
        if (name.equals("Cliquewidth"))
            p = new Cliquewidth(name, g);
        else if (name.equals("Recognition"))
            p = new Recognition(name, g);
        else
            p = new Problem(name, g);
        if (deducing)
            problems.add(p);
        return p;
    }

    //====================== Complexity on a node ===========================

    /**
     * Return the stored complexity of this problem on n. Return UNKNOWN if
     * nothing is stored.
     */
    public Complexity getComplexity(GraphClass n) {
        Complexity c = complexAnn.getNode(n);
        return c == null ? Complexity.UNKNOWN : c;
    }


    /**
     * Return a string representation of the given complexity.  The
     * string chosen depends on whether this problem is a tree/cliquewidth
     * parameter or not.
     */
    public String getComplexityString(Complexity c) {
        return c.getComplexityString();
    }


    /**
     * Set the complexity of this on n to c.
     */
    public void setComplexity(GraphClass n, Complexity c) {
        complexAnn.setNode(n, c);
    }


    /**
     * Return the complexity of this problem on n, as derived in the last
     * completed step.
     * Meant to be used internally and for XML writing.
     */
    public Complexity getDerivedComplexity(GraphClass n) {
        ProblemOnNode pon = algoAnn.getNode(n);

        return pon == null ? Complexity.UNKNOWN :
                pon.getComplexity(
                    Problem.currentStep > 0 ? Problem.currentStep-1 : 0);
    }


    /**
     * Get the complexity of n, consulting the parent problem, too.
     */
    protected Complexity getParentallyDerivedComplexity(GraphClass n) {
        Complexity c = getDerivedComplexity(n);
        if (parents.isEmpty())
            return c;

        Complexity pc = Complexity.UNKNOWN;
        for (Reduction r : parents) {
            pc = r.fromParent(r.getParent().getParentallyDerivedComplexity(n));
            if (!c.isCompatible(pc))
                throw new Error("Inconsistent data for "+n+" "+name);
            if (pc.betterThan(c))
                c = pc;
        }
        return c;
    }


    /**
     * Get the complexity of n, consulting the child problem, too.
     */
    protected Complexity getProgeniallyDerivedComplexity(GraphClass n) {
        Complexity c = getDerivedComplexity(n);
        if (children.isEmpty())
            return c;

        Complexity cc = Complexity.UNKNOWN;
        for (Reduction r : children) {
            cc = r.fromChild(r.getChild().getProgeniallyDerivedComplexity(n));
            if (!cc.isCompatible(c))
                throw new Error("Inconsistent data for "+n+" "+name);
            if (cc.betterThan(c))
                c = cc;
        }
        return c;
    }

    /**
     * If complement can be solved on co-G in time c, return the time in which
     * this can be solved on G.
     */
    public Complexity complementComplexity(Complexity c) {
        return c.betterThan(Complexity.P) ? Complexity.P : c;
    }


    //============================ Algorithms =============================


    /**
     * Add an algorithm for this problem on graphclass n and update the
     * complexity on n.
     */
    protected void addAlgo(GraphClass n, Algorithm a) {
        if (!graph.containsVertex(n))
            throw new IllegalArgumentException("Invalid node");
        if (a.getProblem() != this  &&  a.getProblem() != complement)
            throw new IllegalArgumentException("Invalid algorithm "+ a);

        ProblemOnNode pon = algoAnn.getNode(n);
        if (pon == null) {
            pon = new ProblemOnNode(this, n);
            algoAnn.setNode(n, pon);
        }

        pon.addAlgo(a, Problem.currentStep);
    }

    /**
     * Add all algorithms in iter to n.
     */
    protected void addAlgos(GraphClass n, Iterator iter) {
        while (iter.hasNext())
            addAlgo(n, (Algorithm) iter.next());
    }


    /**
     * Create a new algorithm for this problem on a node n, add it to node n
     * and return it.
     * n may be null.
     */
    public Algorithm createAlgo(GraphClass n, Complexity complexity,
            String bounds, List refs) {
        Algorithm res = new SimpleAlgorithm(this, n, complexity, bounds, refs);
        if (n != null)
            addAlgo(n, res);
        return res;
    }

    /**
     * Create a new algorithm for this problem on a node n with a simple
     * explanation (Note text), add it to node n and return it.
     * n may be null.
     */
    public Algorithm createAlgo(GraphClass n, Complexity complexity,
            String why) {
        List refs = new ArrayList();
        refs.add(new Note(why, null));
        return createAlgo(n, complexity, null, refs);
    }


    /**
     * Get the algorithms for this problem that work on node n or null if there
     * are none.
     */
    protected HashSet<Algorithm> getAlgoSet(GraphClass n) {
        if (algoAnn == null)
            return null;

        ProblemOnNode pon = algoAnn.getNode(n);
        return pon == null ? null : pon.getAlgoSet();
    }


    /**
     * Return an iterator over the algorithms for this problem on the given
     * node. Never returns null.
     */
    public Iterator<Algorithm> getAlgos(GraphClass n) {
        HashSet<Algorithm> hash = getAlgoSet(n);
        if (hash == null)
            hash = new HashSet<Algorithm>();
        return hash.iterator();
    } 


    //====================== Distribution of algorithms =====================


    /**
     * Distribute the Algorithms for this problem over all nodes.
     * initAlgo/addAlgo must have been called for all problems.
     * Assumes the graph is transitively closed!
     * @param gc2node maps GraphClass to Node in g
     */
    protected void distributeAlgorithms() {
        Complexity c;
        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(graph);

        //---- Add every set of algorithms to the super/subnodes' set. ----
        for (GraphClass n : graph.vertexSet()) {
            HashSet algos = getAlgoSet(n);
            if (algos != null) {
                c = getDerivedComplexity(n);
                if (c.distributesUp())
                    distribute(algos, GAlg.inNeighboursOf(graph, n));
                else if (c.distributesDown())
                    distribute(algos, GAlg.outNeighboursOf(graph, n));
                else if (c.distributesEqual())
                    distribute(algos, scc.get(n));
            }
        }
    }


    /**
     * Distribute the algorithms for the parents to this problem.
     */
    protected void distributeParents() {
        Complexity c;

        for (GraphClass n: graph.vertexSet()) {
            for (Reduction r : parents) {
                c = r.fromParent(
                        r.getParent().getParentallyDerivedComplexity(n) );
                if (!c.isUnknown())
                    addAlgo(n, r.getChildAlgo(c));
            }
        }
    }


    /**
     * Distribute the algorithms for the children to this problem.
     */
    protected void distributeChildren() {
        Complexity c;

        for (GraphClass n : graph.vertexSet()) {
            for (Reduction r : children) {
                c = r.fromChild(
                        r.getChild().getProgeniallyDerivedComplexity(n) );
                if (!c.isUnknown())
                    addAlgo(n, r.getParentAlgo(c));
            }
        }
    }


    /**
     * Distribute the Algorithms for this problem over all nodes via the
     * complement.
     * initAlgo/addAlgo must have been called for all problems.
     * distributeAlgorithms must have been called for this problem, and all
     * parent/child problems.
     * Assumes the graph is transitively closed and the complement index is
     * set!
     */
    public void distributeComplement() {
        if (complement == null)
            return;

        GraphClass con;
        Complexity nc, conc;

        for (GraphClass n : graph.vertexSet()) {
            if (!(n instanceof ComplementClass))
                continue;
            con = ((ComplementClass) n).getBase();
            nc = getDerivedComplexity(n);
            conc = complement.getDerivedComplexity(con);
            if (!nc.isCompatible(complementComplexity(conc))) {
                System.err.println("ComplexityClash: "+
                        n +" "+ this.name +"="+ nc +" but "+
                        con +" "+ this.complement.name +"="+ conc);
            } else if (nc.isUnknown() && !conc.isUnknown()) {
                addAlgo(n, getComplementAlgo(complementComplexity(conc)));
            } else if (conc.isUnknown() && !nc.isUnknown()) {
                complement.addAlgo(con, complement.getComplementAlgo(
                        complement.complementComplexity(nc)) );
            }
        }
    }


    /**
     * Try moving complexity information UP to union nodes. We only change the
     * complexity class for Union nodes, and do not generate new references or
     * timebounds.
     * The reasoning is: If we can solve the problem for every part of the
     * union in polytime, then we can apply all part algorithms in polytime,
     * and check their solutions in polytime. So the problem is solvable in
     * polytime on the union.
     */
    protected void distributeUpUnion() {
        int i;
        boolean ok;
        Complexity c;

        for (GraphClass n: graph.vertexSet()) {
            if ( !(n instanceof UnionClass) ||
                    getDerivedComplexity(n).betterOrEqual(Complexity.P) )
                continue;

            //---- Check whether all parts are in P ----
            ok = true;
            for (GraphClass part : ((UnionClass) n).getSet()) {
                if (!getDerivedComplexity(part).betterOrEqual(Complexity.P)) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                //System.err.println("NOTE: distributeUpUnion invoked on "+
                        //n.getName()+" "+toString());
                createAlgo(n, Complexity.P, "From the constituent classes.");
            }
        }
    }


    /**
     * Try moving complexity information DOWN to intersection nodes.
     * Example: If we can recognize every part of the intersection in
     * polytime/lin, then we can take their conjunction in polytime/lin as
     * well.
     * @param gc2node translates a GraphClass into the corresponding ISGCINode
     */
    protected void distributeDownIntersect() {}

    /**
     * Do special deductions for a particular problem.
     * Default implementation does nothing.
     */
    protected void distributeSpecial() {}


    /**
     * Adds the algorithms in algos to the classes in nodes.
     */
    protected void distribute(HashSet algos, Iterable<GraphClass> nodes) {
        for (GraphClass n : nodes) {
            addAlgos(n, algos.iterator());
        }
    }

    //--------------------- Derived algorithms ---------------------------

    /**
     * Find in the given list an algorithm of the requested complexity. If it
     * doesn't exist yet, create it node-independently with the given
     * complexity and text.
     */
    protected Algorithm getDerivedAlgo(List<Algorithm> l,
            Complexity c, String why) {
        for (Algorithm a : l)
            if (a.getComplexity().equals(c))
                return a;

        Algorithm a = createAlgo(null, c, why);
        l.add(a);
        return a;
    }
    

    /**
     * Return an algorithm that solves this on a class in time c, assuming the
     * complement can be solved on co-G.
     */
    protected Algorithm getComplementAlgo(Complexity c) {
        final String why = "from "+ complement +" on the complement";

        if (coAlgos ==  null)
            coAlgos = new ArrayList<Algorithm>();
        return getDerivedAlgo(coAlgos, c, why);
    }

    //================= Controlling the deduction process ====================
    /**
    * Complexities are deduced in multiple steps, as follows:
    * Repeat twice:
    * - Algorithms for problem on node or on a super/subclass of node.
    * - Derived from previous step by parent/child problems
    * - Derived from previous step by union/intersect/special
    * Derived from previous step by complement problems.
    * Repeat twice:
    * - Algorithms for problem on node or on a super/subclass of node.
    * - Derived from previous step by parent/child problems
    * - Derived from previous step by union/intersect/special
    */
    static final int STEPS = 4*3 + 1;
    /** The current step */
    private static int currentStep;
    /** Whether we are doing deductions */
    private static boolean deducing;
    /** The problems */
    private static List<Problem> problems;


    /**
     * Call this before reading the graph when you're going to deduce.
     */
    public static void setDeducing() {
        deducing = true;
        currentStep = 0;
        problems = new ArrayList<Problem>();
    }


    /**
     * Perform a single sequence of complexity deductions (3 steps), without
     * complement.
     */
    private static void distributeComplexitiesBasic() {
        for (Problem p : problems)
            p.distributeAlgorithms();
        currentStep++;

        for (Problem p : problems) {
            p.distributeParents();
            p.distributeChildren();
        }
        currentStep++;

        for (Problem p : problems) {
            p.distributeUpUnion();
            p.distributeDownIntersect();
            p.distributeSpecial();
        }
        currentStep++;
    }


    /**
     * Distribute/deduce the algorithms and complexities.
     * @param gc2node maps GraphClass to Node in g
     */
    public static void distributeComplexities() {
        distributeComplexitiesBasic();
        distributeComplexitiesBasic();

        for (Problem p : problems)
            p.distributeComplement();
        currentStep++;

        distributeComplexitiesBasic();
        distributeComplexitiesBasic();
    }
}


/* EOF */
