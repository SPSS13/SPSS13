/*
 * Write an ISGCIGraph as GraphML using SAX events.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/GraphMLWriter.java,v 2.1 2011/09/29 08:38:52 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.xml;

import java.io.Writer;
import java.util.Date;
import java.awt.Color;
import teo.isgci.util.Latex2JHtml;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import teo.sax.XMLWriter;

public class GraphMLWriter {
    /** Where to write to */
    protected XMLWriter writer;
    /** What should be written? */
    protected int mode;
    /** Should the labels be in html? */
    protected boolean html;
    /** Should unproper inclusions be marked? */
    protected boolean writeUnproper;
    /** The html converter */
    protected Latex2JHtml converter;

    /** Write only nodes and edges (plain GraphML) */
    public static final int MODE_PLAIN = 0;
    /** Write also node/edge styling info in yEd format */
    public static final int MODE_YED = 2;

    /** XML tags */
    private static final String XMLDECL =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    private static final String NS_XSI =
            "http://www.w3.org/2001/XMLSchema-instance";
    private static final String NS_SCHEMA =
            "http://graphml.graphdrawing.org/xmlns";
    private static final String NS_Y =
            "http://www.yworks.com/xml/graphml";
    private static final String NS_YSCHEMA =
            "http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd";
    private static final String GRAPHCLASS = "node";
    private static final String ID = "id";
    private static final String INCLUSION = "edge";
    private static final String SUPER = "source";
    private static final String SUB = "target";
    private static final String PROPER = "directed";
    private static final String ROOT = "graphml";
    private static final String GRAPH = "graph";


    /**
     * Create a new GraphMLWriter
     * @param writer where to write to
     * @param mode what should be written
     * @param unproper mark unproper inclusions
     * @param html output labels not in latex but in Java html
     */
    public GraphMLWriter(Writer writer, int mode, boolean unproper,
            boolean html) {
        this.writer = new XMLWriter(writer);
        this.converter = new Latex2JHtml();
        this.mode = mode;
        this.html = html;
        this.writeUnproper = unproper;
    }


    public void startDocument() throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        writer.startDocument(XMLDECL);
        writer.forceNSDecl(NS_XSI, "xsi");
        if (mode == MODE_YED) {
            writer.forceNSDecl(NS_Y, "y");
            writer.forceNSDecl(NS_SCHEMA +" "+ NS_YSCHEMA, "schemaLocation");
        } else
            writer.forceNSDecl(NS_SCHEMA, "schemaLocation");
        writer.startElement(ROOT);
        writer.characters("\n");

        if (mode == MODE_YED) {
            atts.clear();
            atts.addAttribute("id", "d0");
            atts.addAttribute("for", "node");
            atts.addAttribute("yfiles.type", "nodegraphics");
            writer.emptyElement("", "key", "", atts);
            writer.characters("\n");

            atts.clear();
            atts.addAttribute("id", "e0");
            atts.addAttribute("for", "edge");
            atts.addAttribute("yfiles.type", "edgegraphics");
            writer.emptyElement("", "key", "", atts);
            writer.characters("\n");
        }
        atts.clear();
        atts.addAttribute("id", "isgci");
        atts.addAttribute("edgedefault", "directed");
        writer.startElement("", GRAPH, "", atts);
        writer.characters("\n");

        writer.dataElement("desc",
                "ISGCI graph class diagram, generated "+
                String.format("%1$tF %1$tR", java.util.Calendar.getInstance())+
                " by http://www.graphclasses.org");
        writer.characters("\n");
    }


    public void endDocument() throws SAXException {
        writer.endElement(GRAPH);
        writer.characters("\n");
        writer.endElement(ROOT);
        writer.endDocument();
    }


    /**
     * Write a node.
     * @param id id of the node to write
     * @param label the label of the node
     * @param color its color
     */
    public void writeNode(String id, String label, Color color)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        atts.addAttribute(ID, id);
        writer.startElement("", GRAPHCLASS, "", atts);
        if (mode == MODE_YED) {
            writer.characters("\n");
            atts.clear();
            atts.addAttribute("key", "d0");
            writer.startElement("", "data", "", atts);
            writer.startElement(NS_Y, "ShapeNode");
 
            atts.clear();
            atts.addAttribute("color", String.format("#%1$02X%2$02X%3$02X",
                    color.getRed(), color.getGreen(), color.getBlue()));
            writer.emptyElement(NS_Y, "Fill", "", atts);

            atts.clear();
            atts.addAttribute("type", "ellipse");
            writer.emptyElement(NS_Y, "Shape", "", atts);

            writer.startElement(NS_Y, "NodeLabel");
            if (html) {
                writer.characters("<html>"+ converter.html(label) +"</html>");
            } else
                writer.characters(label);
            writer.endElement(NS_Y, "NodeLabel");

            writer.endElement(NS_Y, "ShapeNode");
            writer.endElement("data");
        } else
            writer.dataElement("desc", label);
        writer.endElement(GRAPHCLASS);
        writer.characters("\n");
    }

    /**
     * Write an edge
     * @param from source of the edge (superclass)
     * @param to destination of the edge (subclass)
     * @param proper whether the inclusion is proper
     */
    public void writeEdge(String from, String to, boolean proper)
            throws SAXException {
        SimpleAttributes atts = new SimpleAttributes();

        atts.addAttribute(SUPER, from);
        atts.addAttribute(SUB, to);
        if (!proper)
            atts.addAttribute(PROPER, "false");
        writer.startElement("", INCLUSION, "", atts);
        if (mode == MODE_YED) {
            atts.clear();
            atts.addAttribute("key", "e0");
            writer.startElement("", "data", "", atts);
            writer.startElement(NS_Y, "PolyLineEdge");

            atts.clear();
            atts.addAttribute(SUB, "standard");
            atts.addAttribute(SUPER,
                    writeUnproper && !proper ? "short" : "none");
            writer.emptyElement(NS_Y, "Arrows", "", atts);

            writer.endElement(NS_Y, "PolyLineEdge");
            writer.endElement("data");
        }
        writer.endElement(INCLUSION);
        writer.characters("\n");
    }
}

/* EOF */
