/*
 * An exception for problems that seem to be both P and NPC
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/ComplexityClashException.java,v 2.0 2011/09/25 12:33:16 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;

class ComplexityClashException extends RuntimeException {
    public ComplexityClashException(String s) {
        super(s);
    }

    public ComplexityClashException(Complexity a, Complexity b) {
        super(a.toString() +" /\\ "+ b.toString());
    }
}

/* EOF */
