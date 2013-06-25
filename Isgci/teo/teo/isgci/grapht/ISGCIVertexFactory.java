/*
 * Creates sets of virtual graphclasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/ISGCIVertexFactory.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.Collections;
import java.util.Set;
import org.jgrapht.VertexFactory;
import teo.isgci.gc.GraphClass;
import teo.isgci.gc.BaseClass;

public class ISGCIVertexFactory implements VertexFactory<Set<GraphClass> > {
    int running;

    public ISGCIVertexFactory() {
        running = 0;
    }

    public Set<GraphClass> createVertex() {
        return Collections.singleton((GraphClass)
                new VirtualClass("virtual-"+ (++running)) );
    }


    class VirtualClass extends BaseClass {
        public VirtualClass(String s) {
            super(s);
        }
    }
}

/* EOF */
