/*
 * Binary predicate object (for compatibility with JGL).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/BinaryPredicate.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;


/**
 * A Binary predicate.
 */
public interface BinaryPredicate {
    public abstract boolean execute(Object a, Object b);
}

/* EOF */
