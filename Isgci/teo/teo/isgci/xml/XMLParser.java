/*
 * Convenience wrapper for XML parsers.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/XMLParser.java,v 2.0 2011/09/25 12:36:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.xml;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.net.URL;
import java.io.*;

/**
 * This class wraps some of the stuff necessary to parse an XML file.
 * All the user has to care about is the file to be parsed, and
 * a ContentHandler, a class that knows how to handle the information
 * that were extracted from the file.
 */
public class XMLParser{
    
    protected InputSource input;
    protected XMLReader xmlReader;
    
    /**
     * Create a new parser with only the file specified. If no ContentHandler
     * is specified explicitly, the parser will only report errors.
     */
    public XMLParser(InputSource input) {
        this(input, new DefaultHandler(), null);
    }
    
    public XMLParser(InputSource input, ContentHandler cont) {
        this(input, cont, null);
    }

    /**
     * Create a new parser.
     */
    public XMLParser(InputSource input, ContentHandler cont, EntityResolver r){
        this(input, cont, r, null);
    }

    /**
     * Create a new parser.
     */
    public XMLParser(InputSource input, ContentHandler cont, EntityResolver r,
            XMLFilter filter){
        // SAXParserFactory.newInstance() might fail because it consults
        // system properties, which is not allowed for applets. Moreover it
        // doesn't work for https connections in java 1.2+. So create an
        // instance directly.
        SAXParserFactory spf = SAXParserFactory.newInstance();
                //new org.apache.crimson.jaxp.SAXParserFactoryImpl();
        spf.setValidating(true);

        xmlReader = null;
        try {
            // Create a JAXP SAXParser
            SAXParser saxParser = spf.newSAXParser();
            // Get the encapsulated SAX XMLReader
            xmlReader = saxParser.getXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        if (filter != null) {
            filter.setParent(xmlReader);
            xmlReader = filter;
        }

        // set a default ContentHandler of the XMLReader
        xmlReader.setContentHandler(cont);
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));
        if (r != null)
            xmlReader.setEntityResolver(r);

        this.input = input;
    }
    
    /** Set a new file to parse. */
    public void setInput(InputSource input) {
        this.input = input;
    }
    
    /** Set a new ContentHandler that will handle the information. */
    public void setContentHandler(ContentHandler ch){
        xmlReader.setContentHandler(ch);
    }
    
    /** Parse the file. */
    public void parse(){
        try {
            // Tell the XMLReader to parse the XML document
            xmlReader.parse(input);
        } catch (SAXException se) {
            se.printStackTrace();
            System.exit(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
    }
    
    
    /** Error handler to report errors and warnings */
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /** Returns a string describing parse exception details. */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                "\nLine=" + spe.getLineNumber() +
                "\ncol="+spe.getColumnNumber()+
                "\nid="+spe.getPublicId()+
                "\nmsg:" + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            out.println("Error: " + getParseExceptionInfo(spe));
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}

/* EOF */
