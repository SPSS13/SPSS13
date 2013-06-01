/*
 * The database of ISGCI.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/DataSet.java,v 2.2 2011/10/22 15:21:08 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.xml.*;
import teo.isgci.gc.GraphClass;
import teo.isgci.problem.Problem;
import teo.isgci.util.LessLatex;

/**
 * The Database of the information system.
 * inclGraph is the inclusion graph proper.
 * getClassNames() returns a sorted list of all class names.
 */
public final class DataSet {

    private static boolean initialized;
    
    private static String date;
    private static String nodecount, edgecount;

    /** The inclusion graph */
    public static SimpleDirectedGraph<GraphClass,Inclusion> inclGraph;
    /** Maps classnames to nodes */
    protected static TreeMap<String,GraphClass> names;
    /** Maps graphclasses to their SCCs */
    protected static Map<GraphClass, Set<GraphClass> > sccs;

    /** Problems */
    public static Vector<Problem> problems;

    /** Relations not in inclGraph */
    public static List<AbstractRelation> relations;

    static {
        initialized = false;
    }

    /** Load all the data.
     */
    public static void init(teo.Loader loader, String file) {
        if (initialized)
            return;

        inclGraph = new SimpleDirectedGraph<GraphClass,Inclusion>(
                Inclusion.class);
        problems = new Vector<Problem>();
        load(loader, file, inclGraph, problems);

        // Gather the classnames
        names = new TreeMap<String,GraphClass>(new LessLatex());
        for (GraphClass gclass : inclGraph.vertexSet())
            names.put(gclass.toString(), gclass);

        // Gather the SCCs
        sccs = GAlg.calcSCCMap(inclGraph);

        initialized = true;
    }


    public static void load(teo.Loader loader, String file,
            SimpleDirectedGraph<GraphClass,Inclusion> graph,
            Vector problems) {
        ISGCIReader gcr = new ISGCIReader(graph, problems);
        XMLParser xml=new XMLParser(loader.openInputSource(file),
                gcr, loader.new Resolver());
        xml.parse();
        date = gcr.getDate();
        nodecount = gcr.getNodeCount();
        edgecount = gcr.getEdgeCount();
        relations = gcr.getRelations();
    }

    
    /**
     * Returns the names of the available graphclasses ordered alphabetically.
     */
    public static Set<String> getClassNames() {
        return Collections.unmodifiableSet(names.keySet());
    }


    /**
     * Returns the nodes of the available graphclasses ordered alphabetically.
     */
    public static Collection<GraphClass> getClasses() {
        return Collections.unmodifiableCollection(names.values());
    }


    /**
     * Return the node in inclGraph belonging to the given classname.
     */
    public static GraphClass getClass(String name) {
        return names.get(name);
    }


    /**
     * Return the set of classes equivalent to the given one.
     */
    public static Set<GraphClass> getEquivalentClasses(GraphClass gc) {
        return sccs.get(gc);
    }


    /**
     * Return the problem with the given name.
     */
    public static Problem getProblem(String name) {
        for (int i = 0; i < problems.size(); i++)
            if (name.equals( ((Problem) problems.elementAt(i)).getName() )) {
                return (Problem) problems.elementAt(i);
            }
        return null;
    }
    
    public static String getDate() {
        return date;
    }        
    
    public static String getNodeCount() {
        return nodecount;
    }    
    
    public static String getEdgeCount() {
        return edgecount;
    }    
    

}

/* EOF */
