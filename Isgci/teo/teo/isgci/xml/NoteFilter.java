/*
 * A filter that converts the contents of a <note> element into a characters()
 * call.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/NoteFilter.java,v 2.1 2011/10/11 07:12:01 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.xml;

import java.io.StringWriter;
import teo.sax.XMLWriter;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A NoteFilter passes the contents of a <note> element as a string to its
 * parent: startElement("note"); character(..); endElement(note);
 * note elements cannot be nested.
 */
public class NoteFilter extends XMLFilterImpl {
    StringWriter content;
    XMLWriter writer;
    String tag;

    public NoteFilter(String tag) {
        content = null;
        writer = new XMLWriter();
        this.tag = tag;
    }

    public NoteFilter() {
        this(Tags.NOTE);
        content = null;
        writer = new XMLWriter();
    }

    /** ContentHandler Interface */
    public void startElement(String uri, String locName, String qName, 
             Attributes atts) throws SAXException {
        if (tag.equals(qName)) {
            if (content != null)
                throw new SAXException(tag +" can't be nested");
            content = new StringWriter();
            writer.setOutput(content);
            writer.reset();
            writer.startDocument("");     // Prevent XML decl
            super.startElement(uri, locName, qName, atts);
        } else {
            if (content != null)
                writer.startElement(uri, locName, qName, atts);
            else
                super.startElement(uri, locName, qName, atts);
        }
    }

    /** ContentHandler Interface */
    public void endElement(String uri, String locName, String qName)
            throws SAXException {
        if (tag.equals(qName)) {
            writer.endDocument();
            char[] buf = new char[content.getBuffer().length()];
            content.getBuffer().getChars(0, buf.length, buf, 0);
            content = null;
            super.characters(buf, 0, buf.length);
            super.endElement(uri, locName, qName);
        } else {
            if (content != null)
                writer.endElement(uri, locName, qName);
            else
                super.endElement(uri, locName, qName);
        }
    }

    /** ContentHandler Interface */
    public void characters(char[] ch, int start, int len) throws SAXException {
        if (content != null)
            writer.characters(ch, start, len);
        else
            super.characters(ch, start, len);
    }
}

/* EOF */
