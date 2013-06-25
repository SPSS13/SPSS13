/*
 * XML Tags for ISGCI
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/Tags.java,v 2.3 2012/04/09 14:06:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.xml;

import teo.isgci.grapht.Inclusion;
import teo.isgci.gc.*;

class Tags {
    /* Root types */
    static final String ROOT_ISGCI = "ISGCI";

    static final String STATS = "stats";
    static final String DATE = "date";
    static final String NODECOUNT = "nodes";
    static final String EDGECOUNT = "edges";
    
    /* GraphClasses */
    static final String GRAPHCLASSES = "GraphClasses";
    static final String GRAPHCLASS = "GraphClass";
    static final String ID = "id";
    static final String TYPE = "type";
    static final String SELFCO = "self-complementary";
    static final String CLIQUEFIXED = "clique-fixed";

    /* GraphClass types */
    static final String INTER = "intersect";
    static final String UNION = "union";
    static final String FORBID = "forbidden";
    static final String COMPL = "complement";
    static final String HERED = "hereditary";
    static final String ISOHERED = "isometric-hereditary";
    static final String CONHERED = "connected-hereditary";
    static final String INDHERED = "induced-hereditary";
    static final String PROBE = "probe";
    static final String CLIQUE = "clique";
    static final String BASE = "base";

    /* attributes/child elements */
    static final String NAME = "name";
    static final String SMALLGRAPH = "smallgraph";
    public static final String GCREF = "graphclass";
    static final String COMPLEMENTS = "complements";
    static final String EQUIVALENTS = "equivalents";

    /* Problems */
    static final String ALGO = "algo";
    static final String PROBLEM = "problem";
    static final String COMPLEXITY = "complexity";
    static final String BOUNDS = "bounds";
    static final String PROBLEM_DEF = "Problem";
    static final String PROBLEM_FROM = "from";
    static final String PROBLEM_COMPLEMENT = "complement";

    /* Inclusions */
    static final String INCLUSIONS = "Inclusions";
    static final String INCLUSION = "incl";
    static final String SUPER = "super";
    static final String SUB = "sub";
    static final String EQU = "equ";
    static final String DISJOINT = "disjoint";
    static final String INCOMPARABLE = "incmp";
    static final String GC1 = "gc1";
    static final String GC2 = "gc2";
    static final String PROPER = "proper";
    static final String CONFIDENCE = "confidence";
    static final String UNREVIEWED = "unreviewed";
    static final String UNPUBLISHED = "unpublished";

    /* References */
    static final String REF = "ref";
    static final String NOTE = "note";

    private Tags() {}

    public static String graphClassType(GraphClass gc) {
        Class c = gc.getClass();
        if (c == BaseClass.class)
            return BASE;
        else if (c == ComplementClass.class)
            return COMPL;
        else if (c == ForbiddenClass.class)
            return FORBID;
        else if (c == IsometricHereditaryClass.class)
            return ISOHERED;
        else if (c == ConnectedHereditaryClass.class)
            return CONHERED;
        else if (c == InducedHereditaryClass.class)
            return INDHERED;
        else if (c == IntersectClass.class)
            return INTER;
        else if (c == UnionClass.class)
            return UNION;
        else if (c == ProbeClass.class)
            return PROBE;
        else if (c == CliqueClass.class)
            return CLIQUE;
        else
            throw new RuntimeException("Unknown graphclass "+c);
    }

    /* Should go in GraphClass? Special hereditary class? */
    public static GraphClass.Hered string2hereditary(String s) {
        if (ISOHERED.equals(s))
            return GraphClass.Hered.ISOMETRIC;
        else if (CONHERED.equals(s))
            return GraphClass.Hered.CONNECTED;
        else if (INDHERED.equals(s))
            return GraphClass.Hered.INDUCED;
        else
            throw new RuntimeException("Unknown hereditariness "+s);
    }

    public static String hereditariness2string(GraphClass.Hered h) {
        switch (h) {
            case ISOMETRIC: return ISOHERED;
            case CONNECTED: return CONHERED;
            case INDUCED: return INDHERED;
            default:
                throw new RuntimeException("No string for hereditariness "+h);
        }
    }

    public static int string2confidence(String s) {
        if (s == null)
            return Inclusion.CONFIDENCE_HIGHEST;
        if (UNPUBLISHED.equals(s))
            return Inclusion.CONFIDENCE_UNPUBLISHED;
        else
            throw new RuntimeException("Unknown confidence "+s);
    }


    public static String confidence2string(int c) {
        switch (c) {
            case Inclusion.CONFIDENCE_UNPUBLISHED: return UNPUBLISHED;
            default:
                throw new RuntimeException("No string for confidence "+c);
        }
    }
}

/* EOF */
