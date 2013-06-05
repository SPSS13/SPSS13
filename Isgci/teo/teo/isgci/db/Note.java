/*
 * Annotations (inline references). These are copied as-is to the output file.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/Note.java,v 2.0 2011/09/25 12:35:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

/**
 * An annotation.
 */
public class Note {
    String text;
    String name;
    
    public Note(String text, String name) {
        this.text = text != null ? text : "";
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return text;
    }
}

/* EOF */
