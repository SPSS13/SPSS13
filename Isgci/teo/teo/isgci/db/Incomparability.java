/*
 * Two classes being incomparable.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/Incomparability.java,v 2.1 2011/10/22 20:08:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

import teo.isgci.gc.GraphClass;

/**
 * Records incomparability of two classes.
 */
public class Incomparability extends AbstractRelation {

    public Incomparability(GraphClass gc1, GraphClass gc2) {
        super(gc1, gc2);
    }

    public String toString() {
        return gc1.getID() +" ~ "+ gc2.getID();
    }
}

/* EOF */
