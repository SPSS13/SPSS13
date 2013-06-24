/*
 * Unary function object that returns an int.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/IntFunction.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;


/**
 * A unary int-function.
 */
public interface IntFunction<I> {
    public abstract int execute(I o);
}

/* EOF */
