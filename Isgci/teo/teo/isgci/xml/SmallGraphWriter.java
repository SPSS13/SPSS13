/*
 * Writes small graphs to an XML file
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/SmallGraphWriter.java,v 2.6 2012/04/08 06:02:14 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */
 
package teo.isgci.xml;
 
import org.xml.sax.*;
import teo.sax.XMLWriter;
import java.util.*;
import java.io.*;
import teo.isg.SmallGraph;
import teo.isg.Graph;
import teo.isg.Family;
import teo.isg.SimpleFamily;
import teo.isg.HMTFamily;
import teo.isg.UnionFamily;
import teo.isg.Configuration;
import teo.isg.HMTGrammar;
import teo.isg.HMTGrammar.HMTGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class SmallGraphWriter {
    /* Where to write to */
    private XMLWriter writer;
    private Vector graphs;
    
    final static int EDGE = 1;
    final static int NONEDGE = -1;
    final static int OPTEDGE = 0;

    public SmallGraphWriter(Writer writer) {
        this.writer = new XMLWriter(writer);
    }

    /** Writes small graphs to an XML file */
    public void writeSmallGraphs(String xmldecl, Vector smallGraphs,
            Vector gram, Vector famil, Vector config,
            DirectedGraph<Graph,DefaultEdge> incls)
            throws SAXException{
        if (xmldecl == null)
            writer.startDocument();
        else
        writer.startDocument(xmldecl);
        writer.startElement(SmallGraphTags.ROOT_SMALLGRAPHS);
        writer.characters("\n");
        
        writeGraphs(smallGraphs);
        writeGrammars(gram);
        writeFamilies(famil);
        writeConfigurations(config);
        
        writer.characters("\n");
        if (incls != null)
            writeEdges(incls);
        writer.endElement(SmallGraphTags.ROOT_SMALLGRAPHS);
        writer.endDocument();
    }
    
    /** Writes simple small graphs */
    private void writeGraphs(Collection<Graph> graphs) throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        
        for (Graph g : graphs) {
            if (!g.isPrimary())
                continue;
            
            if (g.getName().startsWith("USG"))
                continue;

            writer.characters("\n");
            atts.addAttribute(SmallGraphTags.NAME, g.getName());
            writer.startElement("", SmallGraphTags.SIMPLE, "", atts);
            atts.clear();
            writer.characters("\n   ");
                writeNodes(g.countNodes());

                writeEdges(g, "");
                
                writeAliases(g.getNames());
                writeLink(g.getLink());
                writeGraphComplement(g);
            writer.endElement(SmallGraphTags.SIMPLE);
            writer.characters("\n");
        }
    }

    protected void writeNodes(int count) throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        
        atts.addAttribute(SmallGraphTags.COUNT,
                (new Integer(count)).toString());
        writer.emptyElement("", SmallGraphTags.NODES, "", atts);
        atts.clear();
        writer.characters("\n   ");
    }
    
    protected void writeEdges(Graph gr, String indent) throws SAXException {
        writer.characters(indent);
        writer.startElement(SmallGraphTags.EDGES);
        writer.characters("\n");
        String edge = new String();
        for (int i=0; i<gr.countNodes(); i++)
            for (int j=i+1; j<gr.countNodes(); j++)
                if (gr.getEdge(i,j))
                    edge+=(indent+"      "+i+" - "+j+";\n");
        writer.characters(edge+"   "+indent);
        writer.endElement(SmallGraphTags.EDGES);
        writer.characters("\n");
    }
    
    /** Writes aliases of a SmallGraph */
    protected void writeAliases(List<String> names) throws SAXException {
        if (names.size() <= 1)
            return;

        SimpleAttributes atts = new SimpleAttributes();

        for (int i=1; i<names.size(); i++) {
            writer.characters("   ");
            atts.addAttribute(SmallGraphTags.NAME, names.get(i));
            writer.emptyElement("", SmallGraphTags.ALIAS, "",atts);
            atts.clear();
            writer.characters("\n");
        }
    }
    
    /** Writes a link of a SmallGraph */
    protected void writeLink(String link) throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        
        writer.characters("   ");
        atts.addAttribute(SmallGraphTags.ADDRESS, link == null ? "" : link);
        writer.emptyElement("", SmallGraphTags.LINK, "", atts);
        atts.clear();
        writer.characters("\n");
    }

    /** writes complement of a simple small graph */
    protected void writeGraphComplement(Graph g) throws SAXException {
        if (g.getComplement() == null)
            return;
            
        SimpleAttributes atts = new SimpleAttributes();
        Graph co = (Graph)g.getComplement();
        List<String> names = co.getNames();
        
        if ((co.getLink() == null  ||  co.getLink().equals(g.getLink()))  &&
                (names.size() == 1  ||  co.getName() == g.getName())) {
            writer.characters("   ");
            atts.addAttribute(SmallGraphTags.NAME, co.getName());
            writer.emptyElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
            writer.characters("\n");
        } else {
            writer.characters("   ");
            atts.addAttribute(SmallGraphTags.NAME, co.getName());
            writer.startElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
            writer.characters("\n");

            if (names.size()>1) {
                writer.characters("   ");
                writeAliases(names);
            }
            if (co.getLink() != null)
                writeLink(co.getLink());

            writer.characters("   ");
            writer.endElement(SmallGraphTags.COMPLEMENT);
            writer.characters("\n");
        }
    }
    
    /** Writes HMT-grammars */
    private void writeGrammars(Collection<HMTGrammar> grammars)
            throws SAXException {
        for (HMTGrammar hmtg : grammars) {
            writer.characters("\n");
            writeGrammar(hmtg, "");
        }
    }

    /** Writes families of graphs */
    private void writeFamilies(Collection<Family> families)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();
        
        for (Family f : families) {
            if (!f.isPrimary())
                continue;
            
            writer.characters("\n");
            if (f instanceof SimpleFamily)
                atts.addAttribute(SmallGraphTags.TYPE, "simple");
            else if (f instanceof UnionFamily)
                atts.addAttribute(SmallGraphTags.TYPE, "union");
            else if (f instanceof HMTFamily)
                atts.addAttribute(SmallGraphTags.TYPE, "hmt");
            atts.addAttribute(SmallGraphTags.NAME, f.getName());
            writer.startElement("", SmallGraphTags.FAMILY, "", atts);
            atts.clear();
            writer.characters("\n");
            writeAliases(f.getNames());
            if (f instanceof SimpleFamily) {
                SimpleFamily fs = (SimpleFamily)f;
                writeContains(fs.getContains());
                writeInducedRest(fs.getInducedRest());
            }
            else if (f instanceof HMTFamily) {
                HMTFamily fhmt = (HMTFamily)f;
                if (fhmt.getSmallmembers() != null)
                    writeSmallmembers(fhmt.getSmallmembers());
                if (fhmt.getGrammar() != null)
                    if (fhmt.getGrammar().getName() != null) {
                        atts.addAttribute(SmallGraphTags.NAME,
                                fhmt.getGrammar().getName());
                        if (fhmt.getIndex() == null)
                            System.err.println("HMTFamily "+fhmt.getName()+
                                    " without index");
                        else
                            atts.addAttribute(SmallGraphTags.INDEX,
                                    fhmt.getIndex());
                        writer.characters("   ");
                        writer.emptyElement("", SmallGraphTags.USEGRAMMAR, "",
                                atts);
                        writer.characters("\n");
                        atts.clear();
                    }
                    else
                        writeGrammar(fhmt.getGrammar(), "   ");
            }
            else if (f instanceof UnionFamily) {
                UnionFamily fu = (UnionFamily)f;
                writeSubfamilies(fu.getSubfamilies());
            }
            writeInduced(f.getInduced());
            writeLink(f.getLink());
            writeFamilyComplement(f);
            writer.endElement(SmallGraphTags.FAMILY);
            writer.characters("\n");
        }
    }
    
    /** Writes contains of a Family */
    protected void writeContains(Collection<SmallGraph> list)
            throws SAXException {
        if (list == null)
            return;
        
        for (SmallGraph g : list) {
            writer.characters("   ");
            writer.startElement(SmallGraphTags.CONTAINS);
            writer.characters(g.getName());
            writer.endElement(SmallGraphTags.CONTAINS);
            writer.characters("\n");
        }
    }
    
    /** Writes induced subgraphs of a Family or Configuration */
    protected void writeInduced(Vector<Vector<SmallGraph> > list)
            throws SAXException {
        if (list == null)
            return;
        
        for (Vector<SmallGraph> vecInd : list) {
            writer.characters("   ");
            if (vecInd.size() == 1) {
                writer.dataElement(SmallGraphTags.INDUCED1,
                        vecInd.firstElement().getName());
            } else {
                writer.startElement(SmallGraphTags.INDUCED);
                    for (SmallGraph g : vecInd)
                        writer.dataElement(SmallGraphTags.SMALLGRAPH,
                                g.getName());
                writer.endElement(SmallGraphTags.INDUCED);
            }
            writer.characters("\n");
        }
    }

    /** Writes inducedRest subgraphs of a Family */
    protected void writeInducedRest(Vector<Vector<SmallGraph> > list)
            throws SAXException {
        if (list == null)
            return;
        
        for (Vector<SmallGraph> vecInd : list) {
            writer.characters("   ");
            if (vecInd.size() == 1) {
                writer.dataElement(SmallGraphTags.INDUCEDREST1,
                        vecInd.firstElement().getName());
            } else {
                writer.startElement(SmallGraphTags.INDUCEDREST);
                    for (SmallGraph g : vecInd)
                        writer.dataElement(SmallGraphTags.SMALLGRAPH,
                                g.getName());
                writer.endElement(SmallGraphTags.INDUCEDREST);
            }
            writer.characters("\n");
        }
    }

    protected void writeSmallmembers(Collection<SmallGraph> small)
            throws SAXException {
        for (SmallGraph g : small) {
            writer.characters("   ");
            writer.startElement(SmallGraphTags.SMALLMEMBER);
                writer.characters(g.getName());
            writer.endElement(SmallGraphTags.SMALLMEMBER);
            writer.characters("\n");
        }
    }
    
    protected void writeGrammar(HMTGrammar gram, String indent)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        writer.characters(indent);
        atts.addAttribute(SmallGraphTags.TYPE, (new Integer(gram.getType())).
                toString());
        if (gram.getName() != null)
            atts.addAttribute(SmallGraphTags.NAME, gram.getName());
        writer.startElement("", SmallGraphTags.HMTGRAMMAR, "", atts);
        atts.clear();
        writer.characters("\n   "+indent);
            writer.startElement(SmallGraphTags.HEAD);
            writer.characters("\n      "+indent);
                writeNodes(gram.getHead().countNodes());
                writeEdges(gram.getHead(), "   "+indent);
                writeAttachment(gram.getHead(), indent);
            writer.characters("   "+indent);
            writer.endElement(SmallGraphTags.HEAD);
            writer.characters("\n   "+indent);
            
            writer.startElement(SmallGraphTags.MID);
            writer.characters("\n      "+indent);
                writeNodes(gram.getMid().countNodes());
                writeEdges(gram.getMid(), "   "+indent);
                writeExtension(gram.getMid(), indent);
                writeAttachment(gram.getMid(), indent);
            writer.characters("   "+indent);
            writer.endElement(SmallGraphTags.MID);
            writer.characters("\n   "+indent);
            
            writer.startElement(SmallGraphTags.TAIL);
            writer.characters("\n");
                writer.characters("      "+indent);
                writeNodes(gram.getTail().countNodes());
                writeEdges(gram.getTail(), "   "+indent);
                writeExtension(gram.getTail(), indent);
            writer.characters("   "+indent);
            writer.endElement(SmallGraphTags.TAIL);
            writer.characters("\n"+indent);
        writer.endElement(SmallGraphTags.HMTGRAMMAR);
        writer.characters("\n");
    }
    
    /** Writes attachment of head/mid/tail */
    protected void writeAttachment(HMTGraph hmtgr, String indent)
            throws SAXException {
        writer.characters("      "+indent);
        writer.startElement(SmallGraphTags.ATTACHMENT);
            String att = new String();
            for (int i=0; i<hmtgr.getAtt().length; i++)
                att += (hmtgr.getAtt()[i]+",");
            writer.characters(att);
        writer.endElement(SmallGraphTags.ATTACHMENT);
        writer.characters("\n");
    }
    
    /** Writes extension of head/mid/tail */
    protected void writeExtension(HMTGraph hmtgr, String indent)
            throws SAXException {
        writer.characters("      "+indent);
        writer.startElement(SmallGraphTags.EXTENSION);
            String ext = new String();
            for (int i=0; i<hmtgr.getExt().length; i++)
                ext += (hmtgr.getExt()[i]+",");
            writer.characters(ext);
        writer.endElement(SmallGraphTags.EXTENSION);
        writer.characters("\n");
    }
    
    /** Writes subfamilies of a UnionFamily */
    protected void writeSubfamilies(Collection<SmallGraph> list)
            throws SAXException {
        if (list == null)
            return;
        
        for (SmallGraph g : list) {
            writer.characters("   ");
            writer.startElement(SmallGraphTags.SUBFAMILY);
            writer.characters(g.getName());
            writer.endElement(SmallGraphTags.SUBFAMILY);
            writer.characters("\n");
        }
    }
    
    /** Writes complement of a Family */
    protected void writeFamilyComplement(Family f) throws SAXException {
        if (f.getComplement() == null)
            return;
        
        SimpleAttributes atts = new SimpleAttributes();
        Family co = (Family)f.getComplement();
        
        writer.characters("   ");
        atts.addAttribute(SmallGraphTags.NAME, co.getName());
        
        if (co.getLink() == null  ||  co.getLink().equals(f.getLink())) {
            writer.emptyElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
        } else {
            writer.startElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
            writer.characters("\n   ");
            writeLink(co.getLink());
            writer.characters("   ");
            writer.endElement(SmallGraphTags.COMPLEMENT);
        }
        writer.characters("\n");
    }
    
    /** Writes configurations */
    private void writeConfigurations(Collection<Configuration> configs)
            throws SAXException {
        int i,j;
        String edge, nonedge, optedge;
        SimpleAttributes atts = new SimpleAttributes();
        
        for (Configuration c : configs) {
            if (!c.isPrimary())
                continue;
            
            writer.characters("\n");
            atts.addAttribute(SmallGraphTags.NAME, c.getName());
            writer.startElement("", SmallGraphTags.CONFIGURATION, "", atts);
            atts.clear();
            writer.characters("\n   ");
            writeAliases(c.getNames());
            writer.characters("\n   ");
                atts.addAttribute(SmallGraphTags.COUNT,
                    (new Integer(c.countNodes())).toString());
                writer.emptyElement("", SmallGraphTags.NODES, "", atts);
                atts.clear();
                writer.characters("\n   ");

                if (c.getName().startsWith("XC"))
                    writeConfEdges(c, SmallGraphTags.NONEDGES, NONEDGE);
                else if (c.getName().startsWith("XZ"))
                    writeConfEdges(c, SmallGraphTags.OPTEDGES, OPTEDGE);
                else {
                    writeConfEdges(c, SmallGraphTags.OPTEDGES, OPTEDGE);
                    System.out.println("Strange name of configuration: "+
                                       c.getName());
                }
                
                writeContains(c.getContains());
                writeInduced(c.getInduced());
                writeLink(c.getLink());
                writeConfigurationComplement(c);
            writer.endElement(SmallGraphTags.CONFIGURATION);
            writer.characters("\n");
            
        }
    }
    
    /** Writes edges and nonedges/optedges of a Configuration */
    protected void writeConfEdges(Configuration conf, String tag, int edgeType)
                            throws SAXException {
        String edge = new String(),
               otherEdge = new String();
        int i, j;
        
        writer.startElement(SmallGraphTags.EDGES);
        writer.characters("\n");
        for (i=0; i<conf.countNodes(); i++)
            for (j=i+1; j<conf.countNodes(); j++)
                if (conf.getEdge(i,j) == EDGE)
                    edge+=("      "+i+" - "+j+";\n");
                else if (conf.getEdge(i,j) == edgeType)
                    otherEdge+=("      "+i+" - "+j+";\n");
        writer.characters(edge+"   ");
        writer.endElement(SmallGraphTags.EDGES);
        writer.characters("\n   ");
        
        writer.startElement(tag);
        writer.characters("\n");
        writer.characters(otherEdge+"   ");
        writer.endElement(tag);
        writer.characters("\n");
    }

    /** Writes complement of a Configuration */
    protected void writeConfigurationComplement(Configuration c)
                        throws SAXException {
        if (c.getComplement() == null)
            return;
        
        SimpleAttributes atts = new SimpleAttributes();
        Configuration co = (Configuration)c.getComplement();
        
        writer.characters("   ");
        atts.addAttribute(SmallGraphTags.NAME, co.getName());
        if (co.getLink() == null  ||  co.getLink().equals(c.getLink())) {
            writer.emptyElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
            writer.characters("\n");
        }else {
            writer.startElement("", SmallGraphTags.COMPLEMENT, "", atts);
            atts.clear();
            writer.characters("\n   ");
            writeLink(co.getLink());
            writer.characters("   ");
            writer.endElement(SmallGraphTags.COMPLEMENT);
            writer.characters("\n");
        }
    }

    /** Write the inclusions between smallgraphs */
    protected void writeEdges(DirectedGraph<Graph,DefaultEdge> incls)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        for (DefaultEdge e : incls.edgeSet()) {
            atts.addAttribute(SmallGraphTags.SUPER,
                    incls.getEdgeSource(e).getName());
            atts.addAttribute(SmallGraphTags.SUB,
                    incls.getEdgeTarget(e).getName());
            writer.emptyElement("", SmallGraphTags.INCL, "", atts);
            atts.clear();
            writer.characters("\n");
        }
    }
}

/* EOF */
