/*
 * Unary function object (for compatibility with JGL).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/UnaryFunction.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;


/**
 * A unary function.
 */
public interface UnaryFunction<I,O> {
    public abstract O execute(I o);
}

/* EOF */
