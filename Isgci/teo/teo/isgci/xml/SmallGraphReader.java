/*
 * Reads XML file containing small graphs and families and parses it
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/SmallGraphReader.java,v 2.4 2011/10/27 15:53:30 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.xml;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

import org.xml.sax.XMLReader;
import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import teo.isg.SmallGraph;
import teo.isg.Graph;
import teo.isg.Family;
import teo.isg.SimpleFamily;
import teo.isg.HMTFamily;
import teo.isg.UnionFamily;
import teo.isg.HMTGrammar;
import teo.isg.Configuration;
import teo.isgci.util.Pair;

public class SmallGraphReader extends DefaultHandler {

    private StringBuilder contents;
    private Locator locator;

    /** What we're parsing now as a stack */
    private Deque<Wrapper> current;
    /** The grammar we're parsing now */
    private HMTGrammar curgrammar;
    /** Attachment of the current H/M/T */
    private int[] attachment;
    /** Extension of the current H/M/T */
    private int[] extension;
    /** Wrappers to complete */
    private ArrayList<Wrapper> todo;
    /** Maps name/alias to SmallGraph */
    private HashMap<String,SmallGraph> graphs;
    /** Maps names to grammars */
    private HashMap<String,HMTGrammar> grammars;
    /** Contains the inclusion pairs (super, sub) */
    private ArrayList<Pair<String,String> > inclusions;
    /** Here smallgraphs are added (e.g. for induced) */
    private ArrayList<String> smallgraphs;


    
    public SmallGraphReader() {
        super();
        graphs = new HashMap<String,SmallGraph>();
        grammars = new HashMap<String,HMTGrammar>();
        current = new ArrayDeque<Wrapper>();
        todo = new ArrayList<Wrapper>();
        inclusions = new ArrayList<Pair<String,String> >();
        curgrammar = null;
        smallgraphs = null;
    }
        

    /** Return the parsed graphsets */
    public Collection<SmallGraph> getGraphs() {
        return Collections.unmodifiableSet(
                new HashSet<SmallGraph>(graphs.values()));
    }

    
    /** Return the parsed grammars */
    public Collection<HMTGrammar> getGrammars() {
        return grammars.values();
    }

    
    /** Return the parse inclusion (super,sub) */
    public Collection<Pair<String,String> > getInclusions() {
        return inclusions;
    }

    
    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////
      
    public void setDocumentLocator(Locator l) {
        locator = l;
    }

    public void startDocument() {
        contents = new StringBuilder();
    }


    public void endDocument() {
    }

    /** ContentHandler Interface */
    public void startElement(String uri, String locName, String qName, 
             Attributes atts) throws SAXException {        
        try {
        
        if (qName.equals(SmallGraphTags.SIMPLE)) {
            SmallGraph g = new Graph();
            if (!addName(g, atts.getValue(SmallGraphTags.NAME)))
                throw new SAXException("Bad name");
            current.addFirst(new Wrapper(g));
        }
            
        else if (qName.equals(SmallGraphTags.CONFIGURATION)) {
            SmallGraph g = new Configuration();
            if (!addName(g, atts.getValue(SmallGraphTags.NAME)))
                throw new SAXException("Bad name");
            current.addFirst(new Wrapper(g));
        }

        else if (qName.equals(SmallGraphTags.FAMILY)) {
            SmallGraph g;
            String ftype = atts.getValue(SmallGraphTags.TYPE);
            if (ftype.equals("simple"))
                g = new SimpleFamily();
            else if (ftype.equals("union"))
                g = new UnionFamily();
            else if (ftype.equals("hmt"))
                g = new HMTFamily();
            else
                throw new SAXException("Family "+
                        atts.getValue(SmallGraphTags.NAME) +
                        " with unknown type "+ ftype);
            if (!addName(g, atts.getValue(SmallGraphTags.NAME)))
                throw new SAXException("Bad name");
            current.addFirst(new Wrapper(g));
        }

        else if (qName.equals(SmallGraphTags.NODES)) {
            Wrapper w = current.peekFirst();
            int nodeCount =
                    Integer.parseInt(atts.getValue(SmallGraphTags.COUNT));
            if (w.graph instanceof Graph)
                ((Graph) w.graph).addNodesCount(nodeCount);
            else if (w.graph instanceof Configuration)
                ((Configuration) w.graph).addNodesCount(nodeCount);
            else
                throw new SAXException("Unexpected nodes");
        }
        
        else if (qName.equals(SmallGraphTags.EDGES) ||
                qName.equals(SmallGraphTags.CONTAINS) ||
                qName.equals(SmallGraphTags.INDUCEDREST1) ||
                qName.equals(SmallGraphTags.INDUCED1) ||
                qName.equals(SmallGraphTags.SMALLGRAPH) ||
                qName.equals(SmallGraphTags.NONEDGES) ||
                qName.equals(SmallGraphTags.OPTEDGES) ||
                qName.equals(SmallGraphTags.EXTENSION) ||
                qName.equals(SmallGraphTags.ATTACHMENT) ||
                qName.equals(SmallGraphTags.SMALLMEMBER) ||
                qName.equals(SmallGraphTags.SUBFAMILY))
            contents.setLength(0);

        else if (qName.equals(SmallGraphTags.INDUCED)  ||
                qName.equals(SmallGraphTags.INDUCEDREST)) {
            smallgraphs = new ArrayList<String>();
        }
        
        else if (qName.equals(SmallGraphTags.ALIAS)) {
            if (current.peekFirst() != null) // Ignore alias in e.g. fakefamily
                addName(current.peekFirst().graph,
                        atts.getValue(SmallGraphTags.NAME));
        }
        
        else if (qName.equals(SmallGraphTags.LINK)) {
            if (current.peekFirst() != null)   // Ignore links outside graphs
                current.peekFirst().graph.addLink(
                        atts.getValue(SmallGraphTags.ADDRESS));

        } else if (qName.equals(SmallGraphTags.COMPLEMENT)) {
            String name = atts.getValue(SmallGraphTags.NAME);
            SmallGraph g = current.peekFirst().graph;
            if (name.equals(g.getName())) { // Self-complementary graph/config?
                g.setComplement(g);
                current.addFirst(current.peekFirst()); // for easy stacking
            } else {
                g = g.halfComplement();
                if (!addName(g, name))
                    throw new SAXException("Bad name "+ name);
                current.addFirst(new Wrapper(g));
            }
        }
        
        else if (qName.equals(SmallGraphTags.HMTGRAMMAR)) {
            if (current.isEmpty()) {
                String name = atts.getValue(SmallGraphTags.NAME);
                if (name == null  ||  "".equals(name))
                    throw new SAXException("HMT-grammar outside family and"+
                            " without a name!");

                if (grammars.get(name) != null)
                    System.err.println("Grammar "+ name +" already exists.");

                curgrammar = new HMTGrammar(
                        Integer.parseInt(atts.getValue(SmallGraphTags.TYPE)),
                        name);
                grammars.put(name, curgrammar);
            } else {
                curgrammar = new HMTGrammar(
                        Integer.parseInt(atts.getValue(SmallGraphTags.TYPE)));
                ((HMTFamily) current.peekFirst().graph).setGrammar(curgrammar);
            }
        }

        else if (qName.equals(SmallGraphTags.USEGRAMMAR)) {
            if (current.peekFirst().graph instanceof HMTFamily) {
                HMTFamily fam = (HMTFamily) current.peekFirst().graph;
                HMTGrammar g =
                        grammars.get(atts.getValue(SmallGraphTags.NAME));
                if (g == null)
                    throw new SAXException("Grammar "+
                            atts.getValue(SmallGraphTags.NAME) +" not found");
                fam.setGrammar(g);
                fam.setIndex(atts.getValue("index"));
            } else
                throw new SAXException("use-grammar only valid for hmt-"+
                        "families");
        }

        else if (qName.equals(SmallGraphTags.HEAD) ||
                qName.equals(SmallGraphTags.MID) ||
                qName.equals(SmallGraphTags.TAIL)) {
            String name;
            if (curgrammar != null)
                name = curgrammar.getName() +"."+ qName;
            else
                name = current.peekFirst().graph.getName() +"."+ qName;
            SmallGraph g = new Graph();
            g.addName(name);
            current.addFirst(new Wrapper(g));
        }

        else if (qName.equals(SmallGraphTags.INCL)) {
            inclusions.add( new Pair(
                    atts.getValue(SmallGraphTags.SUPER),
                    atts.getValue(SmallGraphTags.SUB) ));
        }
       
        
        } catch (Exception e) {
            String s = "Line "+ Integer.toString(locator.getLineNumber()) +
                "\nColumn "+ Integer.toString(locator.getColumnNumber()) +
                "\nId "+ qName +
                "\n" + e.toString();
            throw new SAXException(s);
        }
    }
        

    /** ContentHandler Interface */
    public void endElement(String uri, String locName, String qName)
            throws SAXException {
        try {
        if (qName.equals(SmallGraphTags.ROOT_SMALLGRAPHS)) {
            for (Wrapper w : todo)
                w.complete();
            fixComplements();
        }
        
        else if (qName.equals(SmallGraphTags.EDGES)) {
            if (current.peekFirst().graph instanceof Graph) {
                Graph g = (Graph) current.peekFirst().graph;
                for (IntEdge edge : new EdgeTokenizer(contents.toString(),
                        g.countNodes()))
                    if (edge != null)
                        g.addEdge(edge.first, edge.second);
            } else if (current.peekFirst().graph instanceof Configuration) {
                Configuration g = (Configuration) current.peekFirst().graph;
                for (IntEdge edge : new EdgeTokenizer(contents.toString(),
                        g.countNodes()))
                    if (edge != null)
                        g.addEdge(edge.first, edge.second);
            } else
                throw new SAXException("Unexpected edges");
        }

        else if (qName.equals(SmallGraphTags.NONEDGES)) {
            Configuration g = (Configuration) current.peekFirst().graph;
            int nodeCount = g.countNodes();
            for (IntEdge edge : new EdgeTokenizer(contents.toString(),
                    nodeCount))
                if (edge != null)
                    g.addNonedge(edge.first, edge.second);
            //XXX
            for (int i=0; i<nodeCount; i++)
                for (int j=i+1; j<nodeCount; j++)
                    if (g.getEdge(i, j) == 2)
                        g.addOptedge(i, j);
        }

        else if (qName.equals(SmallGraphTags.OPTEDGES)) {
            Configuration g = (Configuration) current.peekFirst().graph;
            int nodeCount = g.countNodes();
            for (IntEdge edge : new EdgeTokenizer(contents.toString(),
                    nodeCount))
                if (edge != null)
                    g.addOptedge(edge.first, edge.second);
            //XXX
            for (int i=0; i<nodeCount; i++)
                for (int j=i+1; j<nodeCount; j++)
                    if (g.getEdge(i, j) == 2)
                        g.addNonedge(i, j);
        }
        
        else if (qName.equals(SmallGraphTags.SIMPLE)  ||
                qName.equals(SmallGraphTags.CONFIGURATION)  ||
                qName.equals(SmallGraphTags.FAMILY)  ||
                qName.equals(SmallGraphTags.COMPLEMENT)) {
            if (!current.peekFirst().done())
                todo.add(current.peekFirst());
            current.removeFirst();
        }

        else if (qName.equals(SmallGraphTags.CONTAINS)) {
            current.peekFirst().addContains(contents.toString());
        }
        
        else if (qName.equals(SmallGraphTags.INDUCED)) {
            current.peekFirst().addInduced(smallgraphs);
            smallgraphs = null;
        }
        
        else if (qName.equals(SmallGraphTags.INDUCED1)) {
            current.peekFirst().addInduced(contents.toString());
        }
        
        else if (qName.equals(SmallGraphTags.INDUCEDREST)) {
            current.peekFirst().addInducedRest(smallgraphs);
            smallgraphs = null;
        }
        
        else if (qName.equals(SmallGraphTags.INDUCEDREST1)) {
            current.peekFirst().addInducedRest(contents.toString());
        }
        
        else if (qName.equals(SmallGraphTags.SMALLGRAPH)) {
            smallgraphs.add(contents.toString());
        }
        
        else if (qName.equals(SmallGraphTags.SUBFAMILY)) {
            current.peekFirst().addSubfamily(contents.toString());
        }

        else if (qName.equals(SmallGraphTags.HMTGRAMMAR)) {
            curgrammar = null;
        }

        else if (qName.equals(SmallGraphTags.SMALLMEMBER)) {
            if (!(current.peekFirst().graph instanceof HMTFamily))
                throw new SAXException("smallmember only allowed for "+
                        "hmt-families");
            current.peekFirst().addSmallmember(contents.toString());
        }

        else if (qName.equals(SmallGraphTags.HEAD)) {
            curgrammar.setHead((Graph) current.peekFirst().graph, attachment);
            attachment = null;
            current.removeFirst();
        }

        else if (qName.equals(SmallGraphTags.MID)) {
            curgrammar.setMid((Graph) current.peekFirst().graph,
                    extension, attachment);
            extension = null;
            attachment = null;
            current.removeFirst();
        }

        else if (qName.equals(SmallGraphTags.TAIL)) {
            curgrammar.setTail((Graph) current.peekFirst().graph, extension);
            extension = null;
            current.removeFirst();
        }

        else if (qName.equals(SmallGraphTags.ATTACHMENT)) {
            attachment = parseInts(contents.toString());
            if (attachment.length != curgrammar.getType())
                throw new SAXException("Attachment of wrong length");
            int nodeCount = ((Graph) current.peekFirst().graph).countNodes();
            for (int i = 0; i < attachment.length; i++)
                if (attachment[i] < 0  ||  attachment[i] >= nodeCount)
                    throw new SAXException("Invalid node"+ attachment[i]);
        }
        
        else if (qName.equals(SmallGraphTags.EXTENSION)) {
            extension = parseInts(contents.toString());
            if (extension.length != curgrammar.getType())
                throw new SAXException("Extension of wrong length");
            int nodeCount = ((Graph) current.peekFirst().graph).countNodes();
            for (int i = 0; i < extension.length; i++)
                if (extension[i] < 0  ||  extension[i] >= nodeCount)
                    throw new SAXException("Invalid node"+ extension[i]);
        }
        
        } catch (Exception e) {
            String s = "Line "+ Integer.toString(locator.getLineNumber()) +
                "\nColumn "+ Integer.toString(locator.getColumnNumber()) +
                "\nId "+ qName +
                "\n" + e.toString();
            throw new SAXException(s, e);
        }
    }

    /** ContentHandler Interface */    
    public void characters(char[] ch, int start, int len) {
        contents.append(ch, start, len);
    }
  
    //------------------------------ Helpers --------------------------------
    
    /**
     * Adds the given name to g, if it is not used already, and the name -
     * graph combination to the hashmap.
     * Returns true iff succesful.
     */
    private boolean addName(SmallGraph g, String name) {
        if (name == null  ||  "".equals(name)) {
            System.err.println("No name given");
            return false;
        }
        if (name.contains(";")) {
            System.err.println("Name cannot contain ';'");
            return false;
        }

        if (graphs.get(name) != null) {
            System.err.println("Name "+ name +" already exists. Ignored");
            return false;
        }

        g.addName(name);
        graphs.put(name, g);
        return true;
    }


    /**
     * Complete the half complements.
     */
    private void fixComplements() {
        for (SmallGraph g : graphs.values()) {
            if (!g.isPrimary())
                g.copyFromComplement();
        }
    }


    /**
     * Parse a comma separated list of integers and return it as an array.
     */
    private int[] parseInts(String s) {
        String[] snum = s.split(",");
        int[] num = new int[snum.length];

        for (int i = 0; i < num.length; i++) {
            num[i] = Integer.parseInt(snum[i].trim());

        }

        return num;
    }


    public static void main(String args[]) throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        //XMLReader xr = new XMLReaderImpl();
        SmallGraphReader handler = new SmallGraphReader();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        
        //FileReader r = new FileReader("xmlstart.xml");
        String path = (new File("")).getAbsolutePath();
        if (path.startsWith("/"))
            path = "file:" + path;
        else
            path = "file:/" + path;
        URL url = new URL(new URL(path), args[0]);
        InputSource input = new InputSource(url.openStream());
        input.setSystemId(url.toString());
        xr.parse(input);


        Vector graphs = new Vector();
        Vector grams = new Vector();
        Vector fams = new Vector();
        Vector confs = new Vector();

        for (SmallGraph g : handler.getGraphs()) {
            if (g instanceof Graph)
                graphs.addElement(g);
            else if (g instanceof Configuration)
                confs.addElement(g);
            else if (g instanceof Family)
                fams.addElement(g);
        }

        for (HMTGrammar g : handler.getGrammars())
            grams.addElement(g);

        try {
            SmallGraphWriter writer = new SmallGraphWriter(
                    new java.io.OutputStreamWriter(System.out));
            writer.writeSmallGraphs(null, graphs, grams, fams, confs, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------- Wrapper class ---------------------------
    /**
     * Contains a graphset and graphsets that it stores, e.g. as induced.
     */
    class Wrapper {
        SmallGraph graph;
        ArrayList<String> contains;
        ArrayList<ArrayList<String> > induced;
        ArrayList<ArrayList<String> > inducedRest;
        ArrayList<String> subfamily;
        ArrayList<String> smallmembers;

        public Wrapper(SmallGraph g) {
            graph = g;
            contains = new ArrayList<String>();
            induced = new ArrayList<ArrayList<String> >();
            inducedRest = new ArrayList<ArrayList<String> >();
            subfamily = new ArrayList<String>();
            smallmembers = new ArrayList<String>();
        }

        public void addContains(String g) {
            contains.add(g);
        }

        public void addInduced(String graph) {
            ArrayList<String> graphs = new ArrayList<String>();
            graphs.add(graph);
            addInduced(graphs);
        }

        public void addInduced(ArrayList<String> graphs) {
            induced.add(graphs);
        }

        public void addInducedRest(String graph) {
            ArrayList<String> graphs = new ArrayList<String>();
            graphs.add(graph);
            addInducedRest(graphs);
        }

        public void addInducedRest(ArrayList<String> graphs) {
            inducedRest.add(graphs);
        }

        public void addSubfamily(String g) {
            subfamily.add(g);
        }

        public void addSmallmember(String m) {
            smallmembers.add(m);
        }

        public boolean done() {
            return contains.isEmpty()  &&  induced.isEmpty()  &&
                    inducedRest.isEmpty()  &&  subfamily.isEmpty()  &&
                    smallmembers.isEmpty();
        }

        public void complete() {
            for (String s : contains) {
                SmallGraph g = graphs.get(s);
                if (g == null)
                    System.err.println("Can't find contains "+ s +" for "+
                            graph.getName());
                else if (graph instanceof Configuration) {
                    if (!(g instanceof Graph)) {
                        System.err.println("Contains for Configuration can "+
                                "only be simple graph: "+s);
                        continue;
                    }
                    ((Configuration) graph).addContains((Graph) g);
                } else if (graph instanceof SimpleFamily)
                    ((SimpleFamily) graph).addContains(g);
                else
                    System.err.println("Contains not allowed for "+
                            graph.getClass() +" "+ graph.getName() +"?");
            }

            for (ArrayList<String> ss : induced) {
                Vector<SmallGraph> v = new Vector<SmallGraph>();
                for (String s : ss) {
                    if (s == null)
                        continue;
                    SmallGraph g = graphs.get(s);
                    if (g == null)
                        System.err.println("Can't find induced "+ s +
                                " for "+ graph.getName());
                    v.add(g);
                }
                graph.addInduced(v);
            }

            for (ArrayList<String>  ss : inducedRest) {
                Vector<SmallGraph> v = new Vector<SmallGraph>();
                for (String s : ss) {
                    if (s == null)
                        continue;
                    SmallGraph g = graphs.get(s);
                    if (g == null)
                        System.err.println("Can't find induced-rest "+ s +
                                " for "+ graph.getName());
                    v.addElement(g);
                }
                if (graph instanceof SimpleFamily)
                    ((SimpleFamily) graph).addInducedRest(v);
                else
                    System.err.println("induced-rest for "+ graph.getClass() +
                        " "+ graph.getName() +"?");
            }

            for (String s : subfamily) {
                SmallGraph g = graphs.get(s);
                if (g == null)
                    System.err.println("Can't find subfamily "+ s +" for "+
                            graph.getName());
                else if (graph instanceof UnionFamily)
                    ((UnionFamily) graph).addSubfamily(g);
                else
                    System.err.println("subfamily for "+ graph.getClass() +
                        " "+ graph.getName() +"?");
            }

            for (String s : smallmembers) {
                SmallGraph g = graphs.get(s);
                if (g == null)
                    System.err.println("Can't find smallmember "+ s +
                            " for "+ graph.getName());
                else if (graph instanceof HMTFamily)
                    ((HMTFamily) graph).addSmallmember(g);
                else
                    System.err.println("smallmember for "+ graph.getClass() +
                        " "+ graph.getName() +"?");
            }

            contains.clear();
            induced.clear();
            inducedRest.clear();
            subfamily.clear();
            smallmembers.clear();
        }
    }
}

//================================ Auxiliary classes ========================

/**
 * A pair of integers for storing an edge.
 */
class IntEdge {
    int first, second;
}


/**
 * Parses edges and returns them one by one.
 * Used format is (int - int;)*
 */
class EdgeTokenizer implements Iterator<IntEdge>, Iterable<IntEdge> {
    String[] edges;
    int nodes;
    int i;
    IntEdge cur;

    public EdgeTokenizer(String contents, int nodeCount) {
        edges = contents.split("\\s*;\\s*");
        nodes = nodeCount;
        i = 0;
        cur = new IntEdge();
    }


    public Iterator<IntEdge> iterator() {
        return this;
    }


    public boolean hasNext() {
        return i < edges.length;
    }


    /** Can return null! */
    public IntEdge next() {
        String[] s = edges[i++].split("\\s*-\\s*");
        if (s.length == 0  ||  s.length == 1  &&  s[0].matches("\\s*"))
            return null;
        if (s.length != 2)
            throw new NoSuchElementException("Edge with != 2 nodes");
        cur.first = Integer.parseInt(s[0].trim());
        cur.second = Integer.parseInt(s[1].trim());
        if (cur.first < 0  ||  cur.first >= nodes)
            throw new NoSuchElementException("Bad node "+ s[0] +" in edge");
        if (cur.second < 0  ||  cur.second >= nodes)
            throw new NoSuchElementException("Bad node "+ s[1] +" in edge");
        return cur;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

/* EOF */
