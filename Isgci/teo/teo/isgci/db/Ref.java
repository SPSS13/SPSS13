/*
 * References.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/Ref.java,v 2.1 2012/04/06 17:57:41 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

/**
 * An indirect annotation.
 */
public class Ref {
    /** Id of this reference */
    String id;

    /** Create a reference with the given id */
    public Ref(String id) {
        this.id = id;
    }

    /** Return a formatted reference: [123] or "trivial" */
    public String toString() {
        if (id.startsWith("ref_")) 
            return new String("["+id.substring("ref_".length())+"]");
        else if (id.equals("def"))
            return "by definition";
        else if (id.equals("trivial"))
            return "trivial";
        else if (id.equals("forbidden"))
            return "forbidden subgraphs";
        else if (id.equals("complement"))
            return "from the complements";
        else if (id.equals("hereditary"))
            return "by hereditariness";
        else if (id.equals("basederived"))
            return "from the baseclasses";
        else
            System.out.println("Reference not understood "+id);

        return "";
    }

    /** Return the id of this reference */
    public String getLabel() {
        return id;
    }

    /** Return true iff this reference is trivial, instead of literature. */
    public boolean isTrivial() {
        return !id.startsWith("ref_");
    }
}

/* EOF */
