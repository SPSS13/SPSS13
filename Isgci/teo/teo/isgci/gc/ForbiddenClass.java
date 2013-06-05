/*
 * A GraphClass that is defined by forbidden induced subgraphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/ForbiddenClass.java,v 1.51 2012/04/09 11:28:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.ArrayUnenforcedSet;

import teo.isgci.grapht.GAlg;
import teo.isgci.util.Utility;
import teo.isgci.util.LessLatex;
import teo.isgci.util.Pair;
import teo.isgci.xml.*;
import teo.isg.*;

/**
 * A GraphClass defined by forbidding induced subgraphs.
 */
public class ForbiddenClass extends GraphClass {
    
    /** Contains the forbidden graphs as Strings. */
    Set<String> isgSet;
    /** Score for how "good" the forbidden set is */
    int theNiceness;


    /**
     * Creates a new graph class based on the GraphClasses in the
     * given set.
     */
    public ForbiddenClass(Collection<String> set){
        super();
        if (set==null || set.isEmpty())
            throw new IllegalArgumentException("missing graphs");

        if (isgGraph != null) {
            Set<String> setnorm = new HashSet<String>();

            for (String gc : set) {
                SmallGraph gr = names.get(gc);
                if (gr == null)
                    throw new IllegalArgumentException(
                        "Unknown smallgraph "+gc);
                setnorm.add(gr.getName());
            }

            isgSet = cancel(setnorm);
        } else {
            isgSet = new ArrayUnenforcedSet<String>(set);
        }

        isgSet = java.util.Collections.unmodifiableSet(isgSet);
        hereditariness = Hered.INDUCED;
        theNiceness = Integer.MIN_VALUE;
    }


    /**
     * Return the set of forbidden smallgraphs.
     * No need to copy as isgSet is unmodifiable.
     */
    public Set<String> getSet() {
        return isgSet;
    }


    public void setHereditariness(Hered h) {
        throw new RuntimeException(
            "Hereditariness cannot be set for ForbiddenClass "+ this);
    }


    /**
     * Returns the names of all smallgraphs contained in the configurations in
     * this ForbiddenClass.
     */
    public Set<String> getConfigContains() {
        HashSet<String> contains = new HashSet<String>();
        for (String s : isgSet) {
            SmallGraph o = names.get(s);
            if (o instanceof Configuration) {
                for (SmallGraph g : ((Configuration) o).getContains())
                    contains.add(g.getName());
            }
        }
        return contains;
    }


    /**
     * Returns <tt>true</tt> if <tt>obj</tt> and <tt>this</tt> are equal.
     * That is, if <tt>obj</tt> is a ForbiddenClass and defined by an equal
     * HashSet of forbidden induced subgraphs.
     */
    public boolean equals(Object obj){
        if (obj == this)
            return true;
        if(obj instanceof ForbiddenClass){
            ForbiddenClass fc = (ForbiddenClass)obj;
            return hashCode() == fc.hashCode()  &&  isgSet.equals(fc.isgSet);
        }
        return false;
    }


    public int calcHash() {
        return isgSet.hashCode();
    }
   

    public void setName() {
        StringBuilder nm = new StringBuilder();
        boolean useBrackets = (isgSet.size() >= 2);
        String[] isgNames = isgSet.toArray(new String[0]);

        Arrays.sort(isgNames, new LessLatex());
        
        if (useBrackets)
            nm.append("(");
        
        for (int i = 0; i < isgNames.length; i++) {
            nm.append(isgNames[i]);
            if (i < isgNames.length-1)
                nm.append(",");
        }

        if (useBrackets)
            nm.append(")");
        nm.append("--free");

        name = nm.toString();
        nameExplicit = false;
    }


    /**
     * Returns ForbiddenClass with a set consisting of complements to the set
     * of this ForbiddenClass
     */
    public GraphClass complement() {
        ArrayList<String> hsNew = new ArrayList<String>();

        for (String forb : getSet()) {
            try {
                hsNew.add(names.get(forb).getComplement().getName());
            } catch(RuntimeException e) {
                System.err.println("Can't find a complement for "+forb);
                e.printStackTrace();
            }
        }
        return new ForbiddenClass(hsNew);
    }


    public boolean subClassOf(GraphClass gc) {
        return subClassOf(gc, new ArrayList<SmallGraph>());
    }


    /**
     * Return true iff we can say by the forbidden subgraphs for certain that
     * this is not a subclass of gc.
     * If possible the name of a smallgraph that witnesses this is written to
     * witness.
     */
    public boolean notSubClassOf(ForbiddenClass gc, StringBuilder witness) {
        List<SmallGraph> witnesses = new ArrayList<SmallGraph>();
        if (subClassOf(gc, witnesses))
            return false;
        if (witnesses.isEmpty())
            return false;

        //---- Find the best (graph > configuration > family) witness
        SmallGraph wit = null, witConf = null, witFam = null;
        for (SmallGraph w : witnesses) {
            if (!w.getName().startsWith("USG"))
                if (w instanceof Family)
                    witFam = w;
                else if (w instanceof Configuration)
                    witConf = w;
                else
                    wit = w;
        }
        if (wit != null)
            witness.append(wit.getName());
        else if (witConf != null)
            witness.append(witConf.getName());
        else if (witFam != null)
            witness.append(witFam.getName());

        return true;
    }
   
    
    /**
     * Return true iff this is a subclass of gc.
     * If false is returned, witnesses may be stored in witnesses.
     */
    private boolean subClassOf(GraphClass gc, List<SmallGraph> witnesses) {
        if (super.subClassOf(gc))
            return true;
        if (gc instanceof ForbiddenClass) {
            HashSet<String> superLeft = new HashSet<String>(
                    ((ForbiddenClass)gc).isgSet);
            superLeft.removeAll(isgSet);

            Set<SmallGraph> subSetVec = new ArrayUnenforcedSet<SmallGraph>();
            for (String s : isgSet)
                subSetVec.add(names.get(s));
            
            for (String g : superLeft) {
                if (!forbids(subSetVec, names.get(g), witnesses))
                    return false;
            }
            return true;
        }
            
        return false;
    }


    /** Return a reference string describing why subClassOf returned true. */
    public String whySubClassOf() {
        return "forbidden";
    }
    

    public GraphClass intersect(GraphClass gc){
        if (gc instanceof ForbiddenClass) {
            ArrayList<String> set = new ArrayList<String>(isgSet);
            isgSet.addAll(((ForbiddenClass)gc).isgSet);
            return new ForbiddenClass(set);
        }

        return super.intersect(gc);
    }
    

    /**
     * Returns true iff this graphclass is characterized by a finite set of
     * finite forbidden subgraphs.
     */
    public boolean isFinite() {
        for (String s : isgSet) {
            if ( names.get(s) instanceof Family )
                return false;
        }
        return true;
    }


    /**
     * Returns the number of infinite families in this characterization.
     */
    protected int countInfinite() {
        int i = 0;
        for (String s : isgSet) {
            if ( names.get(s) instanceof Family )
                i++;
        }
        return i;
    }


    /**
     * Returns the number of configurations in this characterization.
     */
    protected int countConfigurations() {
        int i = 0;
        for (String s : isgSet) {
            if ( names.get(s) instanceof Configuration )
                i++;
        }
        return i;
    }


    /**
     * Return a score that prioritizes equivalent forbidden classes. The higher
     * the score, the more preferable the definition is.
     */
    public int niceness() {
        if (theNiceness == Integer.MIN_VALUE)
            theNiceness = -((countInfinite() << 16) + countConfigurations());
        return theNiceness;
    }

    //----------------------- static deduction stuff -----------------------
    
    /**
     * isgGraph has a node for every SmallGraph and an edge a->b iff every
     * graph that contains a as induced subgraph, has b as induced subgraph(s).
     * In other words, b-free implies a-free.
     */
    static SimpleDirectedGraph<SmallGraph,DefaultEdge> isgGraph;
    static HashMap<String,SmallGraph> names;  // Maps name/alias to SmallGraph

    
    /**
     * Initialize the rule system for deriving relations between
     * ForbiddenClasses. When this function isn't called, NONE of the static
     * class variables are initialized.
     * @param xmlfile .xml file containing the definitions of smallgraphs
     */
    public static void initRules(teo.Loader loader, String xmlfile) {

        SmallGraphReader handler = new SmallGraphReader();

        try{
            XMLParser xr = null;
            if (loader != null) {
                xr = new XMLParser(loader.openInputSource(xmlfile),
                        handler, loader.new Resolver());
            } else {
                String path=(new File("")).getAbsolutePath();
                path = (path.startsWith("/") ? "file:" : "file:/") + path +"/";
                URL url=new URL(new URL(path), xmlfile);
                InputSource input = new InputSource(url.openStream());
                input.setSystemId(url.toString());
                xr = new XMLParser(input, handler);
            }
            xr.parse();
        }catch(Exception exml){
            System.err.println("could not read XML file:");
            exml.printStackTrace();
        }

        Collection<SmallGraph> readGraphs = handler.getGraphs();

        //---- Gather all the names and fill isgGraph and names
        names = new HashMap<String,SmallGraph>();
        isgGraph = new SimpleDirectedGraph<SmallGraph,DefaultEdge>(
                DefaultEdge.class);

        for (SmallGraph gr : readGraphs) {
            for (String s : gr.getNames())
                names.put(s, gr);
            isgGraph.addVertex(gr);
        }

        //---- Cycle through all the smallgraphs and add edges in isgGraph
        for (SmallGraph gr : readGraphs) {
            if (gr instanceof Family) {
                Vector<SmallGraph> supers = null;
                Family f = (Family) gr;

                Vector<Vector<SmallGraph> > subs = f.getInduced();
                if (subs != null)
                    for (Vector<SmallGraph> innerVec : subs) {
                        if (innerVec.size() == 1)
                            isgGraph.addEdge(gr, innerVec.firstElement());
                    }

                if (f instanceof SimpleFamily)
                    supers = ((SimpleFamily)f).getContains();
                else if (f instanceof UnionFamily)
                    supers = ((UnionFamily)f).getSubfamilies();
                else if (f instanceof HMTFamily)
                    supers = ((HMTFamily)f).getSmallmembers();

                if (supers != null)
                    try {
                        for (SmallGraph g : supers)
                            isgGraph.addEdge(g, gr);
                    } catch (Exception ex) {
                        System.err.println(gr);
                        ex.printStackTrace();
                    }
            } else if (gr instanceof Configuration) {
                Configuration c = (Configuration) gr;
                Vector<SmallGraph> supers = c.getContains();
                if (supers != null)
                    for (SmallGraph g : supers)
                        isgGraph.addEdge(g, gr);
                else
                    System.out.println("Mistake!Empty configuration "+
                            c.getName());
            }
        }

        for (Pair<String,String> e : handler.getInclusions()) {
            SmallGraph from = names.get(e.first);
            SmallGraph to = names.get(e.second);
            if (from == null) {
                System.err.println("Cannot find incl.super"+ e.first);
                continue;
            }
            if (to == null) {
                System.err.println("Cannot find incl.sub"+ e.second);
                continue;
            }
            if (!isgGraph.containsEdge(from, to))
                isgGraph.addEdge(from, to);
        }
        
        //System.out.println(isgGraph);
        GAlg.transitiveClosure(isgGraph);

        //System.out.println(isgGraph);
    }


    /**
     * Deletes superfluous elements from <tt>s</tt> and returned the cleaned
     * set.
     * In the set [2K_2,C_4,C_5,P_4], for example, the element C_5 is
     * superfluous because of P_4. If a graph has no induced P_4, it can never
     * contain a C_5 as an induced subgraph, because P_4 is an induced
     * subgraph in C_5.
     */
    private Set<String> cancel(Set<String> s){
        int i;
        Set<String> result = new HashSet<String>();
        ArrayList<SmallGraph> vec = new ArrayList<SmallGraph>();
        ArrayList<SmallGraph> fam = new ArrayList<SmallGraph>();

        //---- Put Families at the end: Try to cancel them first ----
        for (String o : s) {
            SmallGraph x = names.get(o);
            if ( x instanceof Family)
                fam.add(x);
            else
                vec.add(x);
        }
        vec.addAll(fam);

        for (i = vec.size()-1; i >= 0; i--) {
            SmallGraph cur = vec.get(i);
            vec.set(i, vec.get(vec.size()-1));
            vec.remove(vec.size()-1);
            if (!forbids(vec, cur)) {
                vec.add(cur);
                result.add(cur.getName());
            }
        }
        return result;
    }


    /**
     * Does forbidding the set of graphs (Strings) forbid the target?
     * If the answer is no and we can actually find a witness for that, it will
     * be added to witnesses.
     */
    private static boolean forbids(Collection<SmallGraph> graphs,
            SmallGraph target, List<SmallGraph> witnesses) {
        int i, j;

        if (!isgGraph.containsVertex(target))
            System.err.println("Vertex "+ target.getName() +" doesn't exist!");

        for (SmallGraph n : graphs) {
            if (!isgGraph.containsVertex(n))
                System.err.println("Vertex "+ n.getName() +" doesn't exist!");
            if (target == n || isgGraph.containsEdge(target, n))
                return true;
        }
        
        if (target instanceof Configuration) {
            Configuration targetConf = (Configuration) target;
            for (SmallGraph g : targetConf.getContains())
                if (!forbids(graphs, g, witnesses)) {
                    witnesses.add(g);
                    witnesses.add(target);
                    return false;
                }
            return true;
        }
        
        if (target instanceof Graph) {
            witnesses.add(target);
            return false;
        }

        if (!(target instanceof Family))
            return false;
        
        Family targetFamily = (Family) target;
        if (targetFamily.getInduced() != null) {
            for (Vector<SmallGraph> ind : targetFamily.getInduced()) {
                boolean isForbidden = true;
                for (SmallGraph gs : ind)
                    if (!forbids(graphs, gs, witnesses)) {
                        isForbidden = false;
                        break;
                    }
                if (isForbidden)
                    return true;
            }
        }
        
        if (targetFamily instanceof SimpleFamily) {
            SimpleFamily targetSF = (SimpleFamily)targetFamily;
        
            if (targetSF.getInducedRest() != null) {
                for (SmallGraph g : targetSF.getContains())
                    if (!forbids(graphs, g, witnesses)) {
                        witnesses.add(g);
                        witnesses.add(targetSF);
                        return false;
                    }

                for (Vector<SmallGraph> indRest : targetSF.getInducedRest()) {
                    boolean isForbidden = true;
                    for (SmallGraph g : indRest)
                        if (!forbids(graphs, g, witnesses)) {
                            isForbidden = false;
                            break;
                        }
                    if (isForbidden)
                        return true;
                }
            }
        }
        else if (targetFamily instanceof UnionFamily) {
            UnionFamily targetUF = (UnionFamily)targetFamily;
            if (targetUF.getSubfamilies() != null) {
                for (SmallGraph g : targetUF.getSubfamilies())
                    if (!forbids(graphs, g, witnesses)) {
                        /*witnesses.add(g);   not necessarily concrete
                        witnesses.add(targetUF);  counter example */
                        return false;
                    }
                return true;
            } else
                System.err.println("UnionFamily "+ targetUF.getName() +
                    " without subfamilies!");
        }
        else if (targetFamily instanceof HMTFamily) {
            HMTFamily targetHF = (HMTFamily)targetFamily;
            if (targetHF.getSmallmembers() != null) {
                for (SmallGraph g : targetHF.getSmallmembers())
                    if (!forbids(graphs, g, witnesses)) {
                        witnesses.add(g);
                        witnesses.add(targetHF);
                        return false;
                    }
                return true;
            }
        }
        
        return false;
    }

    /** Forbids that isn't interested in witnesses */
    private static boolean forbids(Collection<SmallGraph> graphs,
            SmallGraph target) {
        return forbids(graphs, target, new ArrayList<SmallGraph>());
    }


    public static void main(String args[]) throws IOException {
        initRules(null, args[0]);
        boolean again = true;
        while (again) {
            System.out.println(
                    "Insert ;-separated forbidden HashSet of supposed SUB");
            String a = (new LineNumberReader(
                new InputStreamReader(System.in))).readLine();
            StringTokenizer strTok1 = new StringTokenizer(a, ";");
            System.out.println(
                    "Insert ;-separated forbidden HashSet of supposed SUPER");
            a = (new LineNumberReader(new InputStreamReader(System.in))).
                readLine();
            StringTokenizer strTok2 = new StringTokenizer(a, ";");
            HashSet set1 = new HashSet(), set2 = new HashSet();
            while (strTok1.hasMoreElements())
                set1.add(strTok1.nextToken().trim());
            while (strTok2.hasMoreElements())
                set2.add(strTok2.nextToken().trim());

            ForbiddenClass gc1 = new ForbiddenClass(set1);
            ForbiddenClass gc2 = new ForbiddenClass(set2);

            System.out.print("sub: "+ gc1.subClassOf(gc2));

            StringBuilder witness = new StringBuilder();
            boolean res = gc1.notSubClassOf(gc2, witness);
            System.out.println(" not sub: "+ res +" "+ witness);
            /*System.out.print("Continue? (y/n): ");
            System.out.flush();
            a = (new LineNumberReader(new InputStreamReader(System.in))).
                readLine();
            if (!a.equals("y"))
                again = false;*/
        }
    }
}

/* EOF */
