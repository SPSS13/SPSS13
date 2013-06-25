/*
 * SAX parser event handler for all ISGCI data.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/ISGCIReader.java,v 2.5 2012/04/09 14:06:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.xml;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.jgrapht.DirectedGraph;

import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;

import teo.isgci.gc.*;
import teo.isgci.grapht.*;
import teo.isgci.problem.*;
import teo.isgci.db.*;

public class ISGCIReader extends DefaultHandler{
    
    private StringBuffer chunks;
    private Locator locator;
    
    /* ISGCI */
    DirectedGraph<GraphClass,Inclusion> graph;

    /* Graphclasses */
    private HashMap<String,GraphClass> classes;         // key = id, obj = gc
    private List<GraphClassWrapper> todo;// Save Wrappers that are not yet done
    private GraphClassWrapper curClass;

    /* Inclusions */
    private Inclusion curIncl;
    private AbstractRelation curRel;
    private List<AbstractRelation> relations;

    /* Problems */
    private Hashtable problemNames;
    private List<Problem> problems;
    private List<AlgoWrapper> algos;               // All read algo elements
    private AlgoWrapper curAlgo;
    private Problem curProblem;
    private List<ReductionWrapper> reductionsTodo;

    /* References */
    private List refs;                // Refs for the current element
    private String noteName;
    
    /* Statistics */
    private String date;
    private String nodecount;
    private String edgecount;

    private boolean parsingDone;
    

    /**
     * Creates a reader that uses g for storing data. Data indices will be set
     * by the reader.
     */
    public ISGCIReader(DirectedGraph<GraphClass,Inclusion> g,
            List<Problem> problems) {
        parsingDone = false;
        chunks = new StringBuffer();
        this.graph = g;
        this.problems = problems;
        problemNames = new Hashtable();
        classes = new HashMap<String,GraphClass>();
        todo = new ArrayList<GraphClassWrapper>();
        algos = new ArrayList<AlgoWrapper>();
        reductionsTodo = new ArrayList<ReductionWrapper>();
        relations = new ArrayList<AbstractRelation>();
    }
    
    public DirectedGraph<GraphClass,Inclusion> getGraph() {
        return graph;
    }

    public List<AbstractRelation> getRelations() {
        return relations;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    /** ContentHandler interface */
    public void setDocumentLocator(Locator l) {
        locator = l;
    }

    /** ContentHandler Interface */
    public void startDocument() {
    }
    
    /** ContentHandler Interface */
    public void endDocument() {
    }
    
    public String getDate() {
        return date;
    }    
    
    public String getNodeCount() {
        return nodecount;
    }    
    
    public String getEdgeCount() {
        return edgecount;
    }    
    
    /** ContentHandler Interface */
    public void startElement(String uri, String locName, String qName, 
             Attributes atts) throws SAXException {
        try {
        
            
        //---- Statistics ----
        if (Tags.STATS.equals(qName)) {
            date = atts.getValue(Tags.DATE);
            nodecount = atts.getValue(Tags.NODECOUNT);
            edgecount = atts.getValue(Tags.EDGECOUNT);
        } else

        //---- GraphClasses ----
        if (Tags.GRAPHCLASSES.equals(qName)) {
            // Add the problem reductions
            int i, size, oldsize;
            oldsize = reductionsTodo.size();
            while ((size = reductionsTodo.size()) != 0) {
                for (i = size-1; i >= 0; i--) {
                    if (reductionsTodo.get(i).generate())
                        reductionsTodo.remove(i);
                }
                /*Iterator<ReductionWrapper> iter = reductionsTodo.iterator();
                while (iter.hasNext())
                    if (iter.next().generate())
                        iter.remove();*/
                if (reductionsTodo.size() == oldsize) {
                    System.err.println(size+" problems not resolved");
                    System.err.println(reductionsTodo);
                    return;
                }
                oldsize = size;
            }
        } else
        
        //---- GraphClass ----
        if (Tags.GRAPHCLASS.equals(qName)) {
            curClass = new GraphClassWrapper(atts.getValue(Tags.ID),
                    atts.getValue(Tags.TYPE));
        } else if (Tags.HERED.equals(qName)) {
            curClass.hered = atts.getValue(Tags.TYPE);
        } else if (Tags.SELFCO.equals(qName)) {
            curClass.selfComplementary = true;
        } else if (Tags.CLIQUEFIXED.equals(qName)) {
            curClass.cliqueFixed = true;
        } else

        //---- Inclusion/relation ----
        if (Tags.INCLUSION.equals(qName)  || Tags.EQU.equals(qName)) {
            String gcsuper = Tags.INCLUSION.equals(qName) ?
                    Tags.SUPER : Tags.GC1;
            String gcsub = Tags.INCLUSION.equals(qName) ?
                    Tags.SUB : Tags.GC2;
            if (atts.getValue(gcsuper) == atts.getValue(gcsub))
                throw new SAXException("super = sub = "+
                        atts.getValue(gcsuper));
            /*System.out.println(
                    atts.getValue(gcsuper) +" -> "+ atts.getValue(gcsub) +" "+
                    classes.get(atts.getValue(gcsuper)) +" ->"+
                    classes.get(atts.getValue(gcsub)) );*/
            if (graph.containsEdge(
                    classes.get(atts.getValue(gcsuper)),
                    classes.get(atts.getValue(gcsub)) ))
                throw new SAXException("Edge "+ atts.getValue(gcsuper) +" -> "+
                        atts.getValue(gcsub) +" already exists");
            curIncl = graph.addEdge(
                    classes.get(atts.getValue(gcsuper)),
                    classes.get(atts.getValue(gcsub)) );
            curIncl.setProper(atts.getValue(Tags.PROPER) != null);
            curIncl.setConfidence(Tags.string2confidence(
                    atts.getValue(Tags.CONFIDENCE)));
            refs = new ArrayList();

        } else if (Tags.DISJOINT.equals(qName)  ||
                Tags.INCOMPARABLE.equals(qName)) {
            if (atts.getValue(Tags.GC1) == atts.getValue(Tags.GC2))
                throw new SAXException("gc1 = gc2 = "+
                        atts.getValue(Tags.GC1));
            if (Tags.DISJOINT.equals(qName))
                curRel = new Disjointness(
                        classes.get(atts.getValue(Tags.GC1)),
                        classes.get(atts.getValue(Tags.GC2)));
            else
                curRel = new Incomparability(
                        classes.get(atts.getValue(Tags.GC1)),
                        classes.get(atts.getValue(Tags.GC2)));
            curRel.setConfidence(Tags.string2confidence(
                    atts.getValue(Tags.CONFIDENCE)));
            for (AbstractRelation r : relations)
                if (r.get1() == curRel.get1()  &&  r.get2() == curRel.get2())
                    throw new SAXException(
                        "An incomparability or disjointness between "+
                        curRel.get1().getID() +" and "+ curRel.get2().getID() +
                        " already exists.");
            relations.add(curRel);
            refs = new ArrayList();
        } else

        //---- Problem stuff ----
        if (Tags.PROBLEM_DEF.equals(qName)) {
            Problem p;
            String compl = atts.getValue(Tags.PROBLEM_COMPLEMENT);
            p = Problem.createProblem(atts.getValue(Tags.NAME), graph);
            problems.add(p);
            problemNames.put(p.getName(), p);
            if (compl != null) {
                Problem c = (Problem) problemNames.get(compl);
                if (c == null)
                    throw new SAXException("Complement problem "+ compl +
                            "not found.");
                p.setComplement((Problem) c);
            }
            curProblem = p;
            refs = new ArrayList();

        } else if (Tags.PROBLEM_FROM.equals(qName)) {
            String from = atts.getValue(Tags.NAME);
            Complexity c = Complexity.getComplexity(
                    atts.getValue(Tags.COMPLEXITY));
            reductionsTodo.add(new ReductionWrapper(curProblem, from, c));

        } else if (Tags.ALGO.equals(qName)) {
            curAlgo = new AlgoWrapper(curClass.id, atts.getValue(Tags.NAME),
                atts.getValue(Tags.COMPLEXITY), atts.getValue(Tags.BOUNDS));

        } else if (Tags.PROBLEM.equals(qName)) {
            curClass.complexities.add(new ProblemWrapper(
                (Problem) problemNames.get(atts.getValue(Tags.NAME)),
                atts.getValue(Tags.COMPLEXITY) != null ?
                    Complexity.getComplexity(atts.getValue(Tags.COMPLEXITY)) :
                    Complexity.UNKNOWN));
        } else

        //---- References ----
        if (Tags.NOTE.equals(qName)) {
            chunks.setLength(0);
            noteName = atts.getValue(Tags.NAME);
        } else if (Tags.REF.equals(qName) ||  Tags.SMALLGRAPH.equals(qName) ||
                Tags.GCREF.equals(qName)  ||  Tags.NAME.equals(qName)) {
            chunks.setLength(0);
        }
        
        } catch (Exception e) {
            String s = "Line "+ Integer.toString(locator.getLineNumber()) +
                "\nColumn "+ Integer.toString(locator.getColumnNumber()) +
                "\nId "+ qName +
                e.toString();
            throw new SAXException(s);
        }
    }
    
    /** ContentHandler Interface */
    public void endElement(String uri, String locName, String qName)
            throws SAXException {
        try {

        //---- ISGCI ----
        if (Tags.ROOT_ISGCI.equals(qName)) {
            parsingDone = true;
        }

        //---- GraphClasses ----
        if (Tags.GRAPHCLASSES.equals(qName)) {
            // First generate the outstanding graphclasses.
            int i, size, oldsize = todo.size();
            while ((size = todo.size()) != 0) {
                for (i = size-1; i >= 0; i--) {
                    if (todo.get(i).generate())
                        todo.remove(i);
                }
                if (todo.size() == oldsize) {
                    System.err.println(size+" classes not resolved");
                    System.err.println(todo);
                    return;
                }
                oldsize = size;
            }
            //System.out.println(classes.size()+" classes successfully read");

            // Then create the Complexities.
            for (AlgoWrapper aw : algos)
                aw.generate();

        } else if (Tags.GRAPHCLASS.equals(qName)) {
            curClass.end();
            if (!curClass.generate())
                todo.add(curClass);
        } else if (Tags.NAME.equals(qName)) {
            curClass.name = new String(chunks.toString());
        } else if (Tags.SMALLGRAPH.equals(qName) || Tags.GCREF.equals(qName)) {
            if (Tags.INTER.equals(curClass.type) ||
                    Tags.FORBID.equals(curClass.type) ||
                    Tags.UNION.equals(curClass.type)) {
                curClass.set.add(new String(chunks.toString()));
            } else if (Tags.INDHERED.equals(curClass.type)  ||
                    Tags.CONHERED.equals(curClass.type) ||
                    Tags.ISOHERED.equals(curClass.type)  ||
                    Tags.PROBE.equals(curClass.type)  ||
                    Tags.CLIQUE.equals(curClass.type)  ||
                    Tags.COMPL.equals(curClass.type)) {
                if (curClass.base == null)
                    curClass.base = new String(chunks.toString());
                else
                    throw new SAXException("More than one "+qName+" in "+
                        curClass.type);
            } else
                throw new SAXException("Unexpected "+qName);
        } else

        //---- Inclusions ----
        if (Tags.INCLUSION.equals(qName)) {
            curIncl.setRefs(refs);
        } else if (Tags.DISJOINT.equals(qName)  ||
                Tags.INCOMPARABLE.equals(qName)) {
            curRel.setRefs(refs);
        } else

        if (Tags.EQU.equals(qName)) {
            curIncl.setRefs(refs);
            Inclusion revIncl = graph.addEdge(
                    graph.getEdgeTarget(curIncl),
                    graph.getEdgeSource(curIncl) );
            revIncl.setProper(false);
            revIncl.setConfidence(curIncl.getConfidence());
            revIncl.setRefs(new ArrayList(refs));
        } else

        //---- Problems ----
        if (Tags.ALGO.equals(qName)) {
            curAlgo.end();
            algos.add(curAlgo);
        } else if (Tags.PROBLEM_DEF.equals(qName)) {
            curProblem.setRefs(new ArrayList(refs));
        } else

        //---- References ----
        if (Tags.NOTE.equals(qName)) {
            refs.add(new Note(new String(chunks.toString()), noteName));
        } else if (Tags.REF.equals(qName)) {
            refs.add(new Ref(new String(chunks.toString())));
        }

        } catch (Exception e) {
            e.printStackTrace();
            throw new SAXException(e.toString());
        }
    }
    
    /** ContentHandler Interface */
    public void characters(char[] ch, int start, int len) {
        chunks.append(ch, start, len);
    }

    
    //-------------------------- GraphClassWrapper -------------------------
    private class GraphClassWrapper {
        String type, name, id;
        String base;            // base class id for complement/hereditary
        HashSet<String> set;    // set for union/intersect/forbidden
        String hered;
        boolean selfComplementary;
        boolean cliqueFixed;
        List<ProblemWrapper> complexities;
        List refs, prevrefs;
        
        public GraphClassWrapper(String id, String type) {
            name = null;
            base = null;
            set = null;
            hered = null;
            selfComplementary = false;
            cliqueFixed = false;
            complexities = new ArrayList<ProblemWrapper>();
            this.id = id;
            this.type = type;
            if (Tags.INTER.equals(type) ||
                    Tags.FORBID.equals(type) ||
                    Tags.UNION.equals(type)) {
                set = new HashSet<String>();
            }
            prevrefs = ISGCIReader.this.refs;
            ISGCIReader.this.refs = refs = new ArrayList();
        }

        public void end() {
            ISGCIReader.this.refs = prevrefs;
        }

        public boolean generate() throws SAXException { 
            GraphClass gc = null, base2;
            HashSet<GraphClass> set2;

            if (Tags.BASE.equals(type)) {
                gc = new BaseClass(name);
            } else if (Tags.FORBID.equals(type)) {
                gc = new ForbiddenClass(set); // still uses strings
                gc.setName(name);
            } else if (Tags.COMPL.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for complement class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new ComplementClass(base2);
                gc.setName(name);
            } else if (Tags.ISOHERED.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for hereditary class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new IsometricHereditaryClass(base2);
                gc.setName(name);
            } else if (Tags.CONHERED.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for hereditary class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new ConnectedHereditaryClass(base2);
                gc.setName(name);
            } else if (Tags.INDHERED.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for hereditary class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new InducedHereditaryClass(base2);
                gc.setName(name);
            } else if (Tags.INTER.equals(type)) {
                set2 = new HashSet<GraphClass>();
                for (String s : set) {
                    base2 = classes.get(s);
                    if (base2 == null) {
                        return false;
                    }
                    set2.add(base2);
                }
                gc = new IntersectClass(set2);
                gc.setName(name);
            } else if (Tags.UNION.equals(type)) {
                set2 = new HashSet<GraphClass>();
                for (String s : set) {
                    base2 = classes.get(s);
                    if (base2 == null) {
                        return false;
                    }
                    set2.add(base2);
                }
                gc = new UnionClass(set2);
                gc.setName(name);
            } else if (Tags.PROBE.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for probe class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new ProbeClass(base2);
                gc.setName(name);
            } else if (Tags.CLIQUE.equals(type)) {
                if (base == null)
                    throw new SAXException(
                        "base class required for clique class "+id);
                base2 = classes.get(base);
                if (base2 == null) {
                    return false;
                }
                gc = new CliqueClass(base2);
                gc.setName(name);
            }

            if (hered != null) {
                if (gc.getHereditariness() != GraphClass.Hered.UNKNOWN)
                    System.out.println(
                        "Warning: Changing hereditariness for " + id +
                        ": was "+gc.getHereditariness());
                gc.setHereditariness(Tags.string2hereditary(hered));
            }

            gc.setSelfComplementary(selfComplementary);
            gc.setCliqueFixed(cliqueFixed);
            gc.setID(id);
            gc.setRefs(refs);
            graph.addVertex(gc);
            for (ProblemWrapper w : complexities) {
                w.problem.setComplexity(gc, w.complexity);
            }
            classes.put(id, gc);
            return true;
        }

        public String toString() {
            return "<GraphClass: "+id+" "+name+">";
        }
    }


    //-------------------------- AlgoWrapper -------------------------
    private class AlgoWrapper {
        String id;                      // graphclass id
        String bounds;
        Problem problem;
        Complexity complexity;
        List refs, prevrefs;

        public AlgoWrapper(String id, String name, String complexity,
                String bounds) throws SAXException {
            this.id = id;
            this.bounds = bounds;
            this.problem = (Problem) problemNames.get(name);
            if (this.problem == null)
                throw new SAXException("problem not found: "+name);
            this.complexity = Complexity.getComplexity(complexity);
            prevrefs = ISGCIReader.this.refs;
            ISGCIReader.this.refs = refs = new ArrayList();
        }

        public void end() {
            ISGCIReader.this.refs = prevrefs;
        }

        public boolean generate() {
            problem.createAlgo(classes.get(id), complexity, bounds, refs);
            return true;
        }
    }

    //---------------------- ProblemWrapper -----------------------
    private class ProblemWrapper {
        Problem problem;
        Complexity complexity;

        public ProblemWrapper(Problem p, Complexity c) {
            problem = p;
            complexity = c;
        }
    }


    //---------------------- ReductionWrapper -------------------------
    private class ReductionWrapper {
        Problem child;
        String parent;
        Complexity complexity;

        public ReductionWrapper(Problem p, String from, Complexity c) {
            child = p;
            parent = from;
            complexity = c;
        }

        public boolean generate() {
            Problem f = (Problem) problemNames.get(parent);
            if (f == null)
                return false;
            child.addReduction(f, complexity);
            return true;
        }
    }
}

/* EOF */
