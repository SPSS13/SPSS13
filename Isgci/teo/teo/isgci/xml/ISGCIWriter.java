/*
 * Write an ISGCIGraph using SAX events.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/ISGCIWriter.java,v 2.8 2012/04/09 14:06:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.xml;

import java.io.Writer;
import java.util.*;
import java.text.SimpleDateFormat;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.Graphs;
import teo.isgci.grapht.*;
import teo.isgci.db.*;
import teo.isgci.gc.*;
import teo.isgci.problem.*;
import teo.isgci.util.LessLatex;
import teo.sax.XMLWriter;

public class ISGCIWriter {
    /** Where to write to */
    private XMLWriter writer;
    /** What should be written? */
    private int mode;

    /** Write only online needed information */
    public static final int MODE_ONLINE = 0;
    /** Write information for sage. */
    public static final int MODE_SAGE = 1;
    /** Write all, for the web pages */
    public static final int MODE_FULL = 2;


    /**
     * Create a new ISGCIWriter
     * @param writer where to write to
     * @param mode what should be written
     */
    public ISGCIWriter(Writer writer, int mode) {
        this.writer = new XMLWriter(writer);
        this.mode = mode;
    }


    /**
     * Write a full ISGCI dataset as an XML document.
     * @param g the graph whose data to write
     * @param problems the problems to write
     * @param complementAnn a Set of complement nodes per node
     * @param xmldecl XML declaration (may be null)
     */
    public void writeISGCIDocument(DirectedGraph<GraphClass,Inclusion> g,
            Collection<Problem> problems,
            Collection<AbstractRelation> relations,
            Map<GraphClass,Set<GraphClass> > complementAnn,
            String xmldecl) throws SAXException {
        TreeMap<String,GraphClass> names = null;
        boolean sortbyname =  mode == MODE_FULL || mode == MODE_SAGE; 

         if (sortbyname) {
            names = new TreeMap<String,GraphClass>(new LessLatex());
            GraphClass w;
            for (GraphClass v : g.vertexSet()) {
                if ((w = names.put(v.toString(), v)) != null)
                    System.err.println("Duplicate classname! "+
                        v.getID() +" "+ w.getID() +" "+ v +" "+w);
            }
        }

        if (xmldecl == null)
            writer.startDocument();
        else
            writer.startDocument(xmldecl);
        writer.startElement(Tags.ROOT_ISGCI);
        writer.characters("\n");
            writeStatistics(g);
            writeProblemDefs(problems);
            writeNodes(sortbyname ?  names.values() : g.vertexSet(),
                    problems, complementAnn, g);

            writer.startElement(Tags.INCLUSIONS);
            writer.characters("\n");
                writeEdges(g);
                writeRelations(relations);
            writer.endElement(Tags.INCLUSIONS);
            writer.characters("\n");
        writer.endElement(Tags.ROOT_ISGCI);
        writer.endDocument();
    }


    private void writeStatistics(DirectedGraph<GraphClass,Inclusion> g)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        DirectedGraph<GraphClass,Inclusion> closedGraph =
                new SimpleDirectedGraph<GraphClass,Inclusion>(Inclusion.class);
        Graphs.addGraph(closedGraph, g);
        GAlg.transitiveClosure(closedGraph);

        atts.addAttribute(Tags.DATE,
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        atts.addAttribute(Tags.NODECOUNT,
                Integer.toString(closedGraph.vertexSet().size()));
        atts.addAttribute(Tags.EDGECOUNT,
                Integer.toString(closedGraph.edgeSet().size()));
        writer.emptyElement("", Tags.STATS, "", atts);
        writer.characters("\n");
    }


    /**
     * Write the GraphClasses.
     * @param nodes the nodes to write
     * @param problems the problems that can occur for nodes
     */
    private void writeNodes(Iterable<GraphClass> nodes,
            Collection<Problem> problems,
            Map<GraphClass,Set<GraphClass> > complementAnn,
            DirectedGraph<GraphClass,Inclusion> g) throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        Map<GraphClass,Set<GraphClass> > scc = GAlg.calcSCCMap(g);

        writer.startElement(Tags.GRAPHCLASSES);
        writer.characters("\n");

        for (GraphClass gc : nodes) {
            // Header
            atts.addAttribute(Tags.ID, gc.getID());
            atts.addAttribute(Tags.TYPE, Tags.graphClassType(gc));
            writer.startElement("", Tags.GRAPHCLASS, "", atts);
            writer.characters("\n");
                // Name
                if (mode == MODE_FULL  ||  mode == MODE_SAGE  ||
                            gc.namedExplicitly()) {
                    writer.dataElement(Tags.NAME, gc.toString());
                    writer.characters("\n");
                }
                // Set
                if (gc.getClass() != BaseClass.class) {
                    if (gc instanceof ForbiddenClass)
                        writeForbiddenSet(((ForbiddenClass) gc).getSet());
                    else if (gc instanceof IntersectClass)
                        writeClassesSet(((IntersectClass) gc).getSet());
                    else if (gc instanceof UnionClass)
                        writeClassesSet(((UnionClass) gc).getSet());
                    else if (gc instanceof ComplementClass)
                        writeClassesSet( ((ComplementClass) gc).getBase() );
                    else if (gc instanceof HereditaryClass)
                        writeClassesSet( ((HereditaryClass) gc).getBase() );
                    else if (gc instanceof DerivedClass)
                        writeClassesSet( ((DerivedClass) gc).getBase() );
                    else
                        throw new RuntimeException(
                                "Unknown class for node "+gc.getID());
                    writer.characters("\n");
                }
                // Hereditariness, Complements, references and notes
                if (mode == MODE_FULL) {
                    writeHereditariness(gc);
                    writeCliqueFixed(gc);
                    writeEquivs( scc.get(gc) );
                    writeComplements(complementAnn.get(gc));
                    writeRefs(gc.getRefs());
                }
                // Problems
                writeComplexities(gc, problems);
            writer.endElement(Tags.GRAPHCLASS);
            writer.characters("\n\n");
            atts.clear();
        }
        writer.endElement(Tags.GRAPHCLASSES);
        writer.characters("\n");
    }


    /**
     * Write the edges.
     */
    private void writeRelations(Collection<AbstractRelation> relations)
            throws SAXException {
        int confidence;
        SimpleAttributes atts = new SimpleAttributes();

        for (AbstractRelation r : relations) {
            String tag = r instanceof Disjointness ? Tags.DISJOINT :
                    Tags.INCOMPARABLE;

            atts.addAttribute(Tags.GC1, r.get1().getID());
            atts.addAttribute(Tags.GC2, r.get2().getID());
            confidence = r.getConfidence();
            if (confidence < Inclusion.CONFIDENCE_HIGHEST) {
                atts.addAttribute(Tags.CONFIDENCE,
                        Tags.confidence2string(confidence));
            }
            writer.startElement("", tag, "", atts);
            if (mode != MODE_SAGE)
                writeRefs(r.getRefs());
            writer.endElement(tag);
            writer.characters("\n");
            atts.clear();
        }
    }
    /**
     * Write the edges.
     */
    private void writeEdges(DirectedGraph<GraphClass,Inclusion> g)
            throws SAXException {
        int confidence;
        SimpleAttributes atts = new SimpleAttributes();

        for (Inclusion e : g.edgeSet()) {
            atts.addAttribute(Tags.SUPER, g.getEdgeSource(e).getID());
            atts.addAttribute(Tags.SUB, g.getEdgeTarget(e).getID());
            if (e.isProper()) {
                atts.addAttribute(Tags.PROPER, "y");
            }
            confidence = e.getConfidence();
            if (confidence < Inclusion.CONFIDENCE_HIGHEST) {
                atts.addAttribute(Tags.CONFIDENCE,
                        Tags.confidence2string(confidence));
            }
            writer.startElement("", Tags.INCLUSION, "", atts);
            if (mode != MODE_SAGE)
                writeRefs(e.getRefs());
            writer.endElement(Tags.INCLUSION);
            writer.characters("\n");
            atts.clear();
        }
    }


    /**
     * Write the forbidden subgraphs in set.
     */
    private void writeForbiddenSet(Iterable set) throws SAXException{
        for (Object elem : set)
            writer.dataElement(Tags.SMALLGRAPH, elem.toString());
    }


    /**
     * Write the graphclasses in set.
     * @param set the graphclasses to write
     */
    private void writeClassesSet(Iterable<GraphClass> set) throws SAXException{
        for (GraphClass gc : set)
            writer.dataElement(Tags.GCREF, gc.getID());
    }

    /**
     * Write the single graphclass gc as a set.
     * @param gc the graphclass to write
     */
    private void writeClassesSet(GraphClass gc) throws SAXException {
        writer.dataElement(Tags.GCREF, gc.getID());
    }


    /**
     * Write the hereditary element (if needed) for gc.
     */
    private void writeHereditariness(GraphClass gc) throws SAXException {
        if (!gc.hereditarinessExplicitly())
            return;

        SimpleAttributes atts = new SimpleAttributes();
        atts.addAttribute(Tags.TYPE,
            Tags.hereditariness2string(gc.getHereditariness()));
        writer.emptyElement("", Tags.HERED, "", atts);
    }


    /**
     * Write the clique-fixed element (if needed) for gc.
     */
    private void writeCliqueFixed(GraphClass gc) throws SAXException {
        if (!gc.isCliqueFixed())
            return;

        SimpleAttributes atts = new SimpleAttributes();
        writer.emptyElement("", Tags.CLIQUEFIXED, "", atts);
    }



    /**
     * Write a note containing the given equivalent classes.
     */
    private void writeEquivs(Set<GraphClass> eqs) throws SAXException {
        if (eqs == null)
            return;
        SimpleAttributes atts = new SimpleAttributes();
        atts.addAttribute(Tags.NAME, Tags.EQUIVALENTS);
        writer.startElement("", Tags.NOTE, "", atts);
            for (GraphClass eq : eqs) {
                writeClassesSet(eq);
            }
        writer.endElement(Tags.NOTE);
    }

    /**
     * Write a note containing the given complementclasses.
     */
    private void writeComplements(Set<GraphClass> cos)
            throws SAXException {
        if (cos == null)
            return;
        SimpleAttributes atts = new SimpleAttributes();
        atts.addAttribute(Tags.NAME, Tags.COMPLEMENTS);
        writer.startElement("", Tags.NOTE, "", atts);
            for (GraphClass co : cos) {
                writeClassesSet(co);
            }
        writer.endElement(Tags.NOTE);
    }


    /**
     * Write all Complexities for GraphClass n.
     */
    private void writeComplexities(GraphClass n, Collection<Problem> problems)
            throws SAXException {
        for (Problem p : problems) {
            writeComplexity(p, p.getDerivedComplexity(n),
                    p.getAlgos(n));
        }
    }


    /**
     * Write a Complexity for Problem problem.
     */
    private void writeComplexity(Problem problem, Complexity c,
            Iterator algos) throws SAXException {
        if (c == null)
            return;
        SimpleAttributes atts = new SimpleAttributes();
        atts.addAttribute(Tags.NAME, problem.getName());
        atts.addAttribute(Tags.COMPLEXITY, problem.getComplexityString(c));
        if (mode == MODE_ONLINE  ||  mode == MODE_SAGE) {
            writer.emptyElement("", Tags.PROBLEM, "", atts);
        } else {
            writer.startElement("", Tags.PROBLEM, "", atts);
                writeAlgorithms(problem, algos);
            writer.endElement(Tags.PROBLEM);
        }
    }

    /**
     * Write the algorithms for problem.
     */
    private void writeAlgorithms(Problem problem, Iterator algos)
            throws SAXException {
        if (mode == MODE_ONLINE  ||  mode == MODE_SAGE  ||  algos == null)
            return;
        SimpleAttributes atts = new SimpleAttributes();
        while (algos.hasNext()) {
            Algorithm a = (Algorithm) algos.next();
            atts.addAttribute(Tags.NAME, problem.getName());
            atts.addAttribute(Tags.COMPLEXITY,
                    problem.getComplexityString(a.getComplexity()));
            if (a.getTimeBounds() != null)
                atts.addAttribute(Tags.BOUNDS, a.getTimeBounds());
            writer.startElement("", Tags.ALGO, "", atts);
                if (a.getGraphClass() != null)
                    writer.dataElement(Tags.GCREF, a.getGraphClass().getID());
                writeRefs(a.getRefs());
            writer.endElement(Tags.ALGO);
            writer.characters("\n");
            atts.clear();
        }
    }


    /**
     * Write the references in refs.
     */
    private void writeRefs(Collection refs)
            throws SAXException {
        if (refs == null)
            return;

        SimpleAttributes atts = new SimpleAttributes();
        for (Object o : refs) {
            if (o instanceof Note) {
                Note n = (Note) o;
                if (n.getName() != null)
                    atts.addAttribute(Tags.NAME, n.getName());
                writer.startElement("", Tags.NOTE, "", atts);
                    writer.charactersRaw(n.toString());
                writer.endElement(Tags.NOTE);
                atts.clear();
            } else if (o instanceof Ref) {
                writer.dataElement(Tags.REF, ((Ref) o).getLabel());
            } else
                throw new RuntimeException("Not a note/ref"+ o);
        }
    }


    /**
     * Write Problem definitions.
     */
    private void writeProblemDefs(Collection<Problem> problems)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        for (Problem p : problems) {
            atts.addAttribute(Tags.NAME, p.getName());
            writer.startElement("", Tags.PROBLEM_DEF, "", atts);
            atts.clear();
            if (mode == MODE_FULL) {
                writeReductions(p.getReductions());
                writeRefs(p.getRefs());
            }
            writer.endElement(Tags.PROBLEM_DEF);
            writer.characters("\n");
        }
    }

    private void writeReductions(Iterator<Reduction> reds) throws SAXException{
        Reduction red;
        SimpleAttributes atts = new SimpleAttributes();

        while (reds.hasNext()) {
            red = reds.next();
            atts.addAttribute(Tags.NAME, red.getParent().getName());
            atts.addAttribute(Tags.COMPLEXITY,
                    red.getComplexity().getComplexityString());
            writer.emptyElement("", Tags.PROBLEM_FROM, "", atts);
            atts.clear();
        }
    }
}

/* EOF */
