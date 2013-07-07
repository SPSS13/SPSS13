/*
 * A convenience form of ..sax.Attributes with just qname and value
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/SimpleAttributes.java,v 2.0 2011/09/25 12:36:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.xml;

import org.xml.sax.helpers.AttributesImpl;

public class SimpleAttributes extends AttributesImpl {
    public void addAttribute(String locName, String value) {
        addAttribute("", locName, "", "", value);
    }
}

/* EOF */
