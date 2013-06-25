/*
 * XML Tags for small graphs
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/xml/SmallGraphTags.java,v 2.2 2011/10/11 07:12:01 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */
 
package teo.isgci.xml;

public class SmallGraphTags {
    /* Root types */
    static final String ROOT_SMALLGRAPHS = "SMALLGRAPHS";
    
    /* Graph declarations types */
    static final String SIMPLE = "simple";
    static final String FAMILY = "family";
    static final String CONFIGURATION = "configuration";
    
    /* Child elements */
    static final String COMPLEMENT = "complement";
    static final String NODES = "nodes";
    static final String EDGES = "edges";
    static final String NONEDGES = "nonedges";
    static final String OPTEDGES = "optedges";
    static final String ALIAS = "alias";
    static final String LINK = "link";
    static final String CONTAINS = "contains";
    static final String INDUCED = "induced";
    static final String INDUCED1 = "induced1";
    static final String INDUCEDREST = "induced-rest";
    static final String INDUCEDREST1 = "induced-rest1";
    static final String SMALLGRAPH = "smallgraph";
    static final String SMALLMEMBER = "smallmember";
    static final String HMTGRAMMAR = "hmt-grammar";
    static final String HEAD = "head";
    static final String MID = "mid";
    static final String TAIL = "tail";
    static final String EXTENSION = "extension";
    static final String ATTACHMENT = "attachment";
    static final String USEGRAMMAR = "use-grammar";
    static final String SUBFAMILY = "subfamily";
    public static final String EXPL = "expl";
    
    /* Attributes */
    static final String NAME = "name";
    static final String COUNT = "count";
    static final String ADDRESS = "address";
    static final String TYPE = "type";
    static final String INDEX = "index";

    /* Relations */
    static final String INCL = "incl";
    static final String SUPER = "super";
    static final String SUB = "sub";
    
    private SmallGraphTags() {
    }
}
    
/* EOF */
