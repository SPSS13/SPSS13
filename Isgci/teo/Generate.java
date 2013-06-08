/*
 * Find trivial inclusions, generate node info.
 *
 * $Header: /home/ux/CVSROOT/teo/Generate.java,v 2.9 2012/10/28 16:00:37 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

import teo.isgci.grapht.*;
import teo.isgci.xml.*;
import teo.isgci.gc.*;
import teo.isgci.db.*;
import teo.isgci.problem.*;
import teo.Loader;

import gnu.getopt.Getopt;
import java.io.*;
import java.util.*;
import java.net.URL;
import org.xml.sax.InputSource;
import org.jgrapht.Graph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

public class Generate {

    static final String XMLDECL =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
        "<!DOCTYPE ISGCI SYSTEM \"isgci.dtd\">\n";


    /**
     * Print node/edge count statistics of the given graph.
     */
    public static void show(Graph dg){
        System.err.print("Nodes: "+ dg.vertexSet().size());
	System.err.println("     Edges: "+ dg.edgeSet().size());
    }


    /**
     * Print statistics on the given problem.
     */
    public static void showProblemStats(
            DirectedGraph<GraphClass,Inclusion> dg, Problem p) {
        int i;
        String cs;
        int[] count = new int[Complexity.values().length];

        Arrays.fill(count, 0);

        for (GraphClass gc : dg.vertexSet())
            count[p.getDerivedComplexity(gc).ordinal()]++;

        System.out.print(String.format("%1$-15.15s", p.getName()));
        System.out.print("\t");
        for (i = 0; i < count.length; i++) {
            System.out.print(count[i]);
            System.out.print("\t");
        }
        System.out.println();
    }


    /**
     * Print statistics on the given problems.
     */
    public static void showProblemStats(
            DirectedGraph<GraphClass,Inclusion> dg, List<Problem> problems) {
        System.out.print("Problem:        ");
        for (Complexity c : Complexity.values()) {
            System.out.print("\t");
            System.out.print(c.getShortString());
        }
        System.out.println();
        for (Problem p : problems)
            showProblemStats(dg, p);
    }


    /**
     * Write a list of gc-numbers and names to file.
     */
    public static void showNames(
            DirectedGraph<GraphClass,Inclusion> dg, String file) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            System.out.println(e);
        }

        showNames(dg, out);
        out.close();
    }


    /**
     * Write a list of gc-numbers and names to out.
     */
    public static void showNames(
            DirectedGraph<GraphClass,Inclusion> dg, PrintWriter out) {
        for (GraphClass v : dg.vertexSet())
            out.println(v.getID() +"\t"+ v);
    }


    /**
     * Write the data in xml format in the given ISGCIWriter.MODE_* mode.
     */
    private static void writeISGCI(
            DirectedGraph<GraphClass,Inclusion> g,
            List<Problem> problems,
            List<AbstractRelation> relations,
            Map<GraphClass,Set<GraphClass> > complements,
            String file,
            int format) {
        try {
            FileWriter out = new FileWriter(file);
            ISGCIWriter w = new ISGCIWriter(out, format);
            w.writeISGCIDocument(g, problems, relations, complements, XMLDECL);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Write a MODE_SAGE document of g to file.
     */
    public static void showSage(
            DirectedGraph<GraphClass,Inclusion> g,
            List<Problem> problems,
            List<AbstractRelation> relations,
            Map<GraphClass,Set<GraphClass> > complements,
            String file) {
        writeISGCI(g, problems, relations, complements, file,
                ISGCIWriter.MODE_SAGE);
    }


    /**
     * Write a MODE_FULL document of g to file.
     */
    public static void showFull(
            DirectedGraph<GraphClass,Inclusion> g,
            List<Problem> problems,
            List<AbstractRelation> relations,
            Map<GraphClass,Set<GraphClass> > complements,
            String file) {
        writeISGCI(g, problems, relations, complements, file,
                ISGCIWriter.MODE_FULL);
    }


    /**
     * Write a MODE_ONLINE document of g to file.
     */
    public static void showShort(
            DirectedGraph<GraphClass,Inclusion> g,
            List<Problem> problems,
            List<AbstractRelation> relations,
            Map<GraphClass,Set<GraphClass> > complements,
            String file){
        writeISGCI(g, problems, relations, complements, file,
                ISGCIWriter.MODE_ONLINE);
    }


    /**
     * Load the ISGCI database.
     */
    public static void load(String file,
            DirectedGraph<GraphClass,Inclusion> graph,
            List<Problem> problems,
            List<AbstractRelation> relations)
            throws java.net.MalformedURLException {
        Loader loader = new Loader("file:"+System.getProperty("user.dir")+"/",
                true);
        ISGCIReader gcr = new ISGCIReader(graph, problems);
        XMLParser xml = new XMLParser(loader.openInputSource(file),
                gcr, loader.new Resolver(), new NoteFilter());
        xml.parse();
        relations.addAll(gcr.getRelations());
    }


    /**
     * Prints e to the given writer as gc_ -> gc_\t name -> name
     */
    private static void println(PrintWriter w, Inclusion e) {
        println(w, e.getSuper(), e.getSub());
    }


    /**
     * Prints from->to to the given writer as gc_ -> gc_\t name -> name
     */
    private static void println(PrintWriter w, GraphClass from, GraphClass to){
        w.print(from.getID());
        w.print(" -> ");
        w.print(to.getID());
        w.print("\t");
        w.print(from);
        w.print(" -> ");
        w.println(to);
        w.flush();
    }


    /**
     * Check whether for every pair (from, to) in the list there is a path in
     * g. Failures are printed.
     */
    public static void checkPaths(DirectedGraph<GraphClass,Inclusion> g,
            Collection<Inclusion> list) {
        int i;
        GraphClass from, to;
        PrintWriter w = new PrintWriter(System.out);

        for (Inclusion e :  list) {
            from = e.getSuper();
            to = e.getSub();
            if (GAlg.getPath(g, from, to) == null)
                println(w, from, to);
        }
    }


    /**
     * Print debug info gathered in the deducer d to the given writer.
     */
    private static void printDebug(Deducer d, PrintWriter w) {
        w.println("\n==== Start debug ==================================");
        for (Inclusion e : d.getGraph().edgeSet())
            d.printTrace(w, e);
        w.println("==== End debug =====================================");
        w.flush();
    }


    /**
     * Print relation debug info gathered in the deducer d to the given writer.
     */
    private static void printRelDebug(Deducer d, Iterable<Inclusion> originals,
            PrintWriter w) {
        w.println("\n==== Start relations ==============================");
        for (Inclusion e : d.getGraph().edgeSet())
            d.printRelationTrace(w, e);
        w.println("==== End relations ==================================");
        w.flush();
        w.println("==== Proper or equal ================================");
        for (Inclusion e : originals) {
            if (!e.isProper()  &&  d.getEdge(e.getSub(),e.getSuper()) == null){
                w.print("! ");
                println(w, e);
            }
        }
        w.println("==== End proper or equal ============================");
        w.flush();
    }


    /**
     * Give every class a list of its complements and return it.
     */
    private static Map<GraphClass,Set<GraphClass> >
            gatherComplements(DirectedGraph<GraphClass,Inclusion> dg) {
        GraphClass w;

        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(dg);
        Map<GraphClass,Set<GraphClass> > compls =
            new HashMap<GraphClass,Set<GraphClass> >();

        for (GraphClass v : dg.vertexSet()) {
            if (!(v instanceof ComplementClass))
                continue;
            if (compls.containsKey(v))          // Already handled.
                continue;

            w = ((ComplementClass) v).getBase();
            Set<GraphClass> compo = scc.get(v);
            Set<GraphClass> cocompo = scc.get(w);
            for (GraphClass x : compo)
                compls.put(x, cocompo);
            for (GraphClass x : cocompo)
                compls.put(x, compo);
        }
        return compls;
    }


    /**
     * Main
     */
    public static void main(String args[]) throws Exception {
        int i;

        Deducer deducer;
        DirectedGraph<GraphClass,Inclusion> graph;
        List<Problem> problems;

        boolean notrivial = false;
        boolean extrachecks = false;
        String debugout = null;
        String debugrelout = null;
        String autocache = null;
        String sageout = null;
        Collection<Inclusion> deleted;
        PrintWriter writer;
        Map<GraphClass,Set<GraphClass> > compls;
        List<AbstractRelation> relations = new ArrayList<AbstractRelation>();

        Getopt opts = new Getopt("Generate", args, "Cxa:l:r:s:h");
        opts.setOpterr(false);
        while ((i = opts.getopt()) != -1) {
            switch (i) {
                case 'C':
                    extrachecks = true;
                    break;
                case 'x':
                    notrivial = true;
                    break;
                case 'a':
                    autocache = opts.getOptarg();
                    break;
                case 'l':
                    debugout = opts.getOptarg();
                    break;
                case 'r':
                    debugrelout = opts.getOptarg();
                    break;
                case 's':
                    sageout = opts.getOptarg();
                    break;
                case '?':
                case 'h':
                    usage();
                    System.exit(1);
            }
        }
        if (args.length - opts.getOptind() < 5) {
            usage();
            System.exit(1);
        }

        //---- Load everything
        // Performance optimization: We only add edges that do not exist yet,
        // so the underlying graph does not need to check this.
        graph = new DirectedMultigraph<GraphClass,Inclusion>(Inclusion.class);
        problems = new ArrayList<Problem>();

        Problem.setDeducing();
        ForbiddenClass.initRules(null, args[opts.getOptind()+1]);
        load(args[opts.getOptind()], graph, problems, relations);
        deducer = new Deducer(graph,true, extrachecks);
        deducer.setGeneratorCache(autocache);
        show(graph);

        ArrayList<Inclusion> originals =
                new ArrayList<Inclusion>(graph.edgeSet());

        //---- Deductions
        if (notrivial)
            GAlg.transitiveClosure(graph);
        else {
            deducer.findTrivialInclusions();
            deducer.findTrivialPropers();
            deducer.sanityCheckAbstractRelations(relations);
            deducer.printStatistics();
        }

        //---- Print debug info
        if (debugout != null) {
            writer = new PrintWriter(
                    new BufferedWriter(new FileWriter(debugout), 64*1024));
            showNames(graph, writer);
            printDebug(deducer, writer);
        }
            
        if (debugrelout != null) {
            writer = new PrintWriter(
                    new BufferedWriter(new FileWriter(debugrelout), 64*1024));
            printRelDebug(deducer, originals, writer);
        }

        System.out.println("Distributing complexities");
        Problem.distributeComplexities();
        showProblemStats(graph, problems);

        System.out.println("Gathering complements");
        compls = gatherComplements(graph);

        // Remove temporaries
        System.out.println("Cleaning up");
        deducer.removeTemp();
        show(graph);
        showProblemStats(graph, problems);

        
        int nc = graph.vertexSet().size();               // For Safety check
        int ec = graph.edgeSet().size();

        deleted = deducer.deleteSuperfluousEdges();
        show(graph);

        if (extrachecks) {
            System.out.println("Verify deleteSuperFluousEdges");
            checkPaths(graph, deleted);
        }

        // Output
        deducer.addRefs();
        showShort(graph, problems, relations, compls,args[opts.getOptind()+3]);
        showSage(graph, problems, relations, compls, sageout);
        deleted = deducer.deleteSuperfluousEdgesFull();

        if (extrachecks) {
            System.out.println("Verify deleteSuperFluousEdgesFull");
            checkPaths(graph, deleted);
        }

        showFull(graph, problems, relations, compls, args[opts.getOptind()+2]);
        showNames(graph, args[opts.getOptind()+4]);

        // Safety check
        GAlg.transitiveClosure(graph);
        if (graph.vertexSet().size() != nc  ||  graph.edgeSet().size() != ec)
            System.err.println("Error in deleteSuperfluousEdges?!");
    }


    private static void usage() {
        System.out.println("Usage: java Generate [options] "+
                "input.xml smallgraphsin.xml "+
                "fullout.xml shortout.xml outnames.txt\n"+
                " -x : Only generate XML, no deductions done\n"+
                " -C : Perform extra checks on code (not data) correctness\n"+
                " -s filename: write out for sage to filename\n" +
                " -a filename: AUTO_* cache filename\n" +
                " -l filename: Log debug output to filename\n" +
                " -r filename: Log relations debug output to filename");
    }
}
