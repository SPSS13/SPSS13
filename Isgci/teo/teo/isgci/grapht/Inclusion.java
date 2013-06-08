/*
 * Stores the information on inclusions.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/Inclusion.java,v 2.1 2011/10/22 15:21:08 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import java.util.List;
import java.util.ArrayList;
import teo.isgci.gc.GraphClass;
import teo.isgci.db.Relation;
import teo.isgci.db.RelationData;

/**
 * Represents an inclusion in the system. For efficiency of the graph
 * structure, equals and hashCode should NOT be overridden!
 */
public class Inclusion extends org.jgrapht.graph.DefaultEdge
        implements Relation {

    private boolean isProper;       // True if this incl is proper
    private RelationData rel;

    public Inclusion() {
        isProper = false;
        rel = new RelationData();
    }

    /*public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof Inclusion))
            return false;

        Inclusion o = (Inclusion) other;
        return superClass.equals(o.superClass) && subClass.equals(o.subClass);
    }

    public int hashCode() {
        return superClass.hashCode() + subClass.hashCode();
    }*/

    public String toString() {
        if (getSource() instanceof GraphClass)
            return getSuper().getID() +" -> "+ getSub().getID();
        return super.toString();
    }

    public GraphClass getSuper() {
        return (GraphClass) getSource();
    }

    public GraphClass getSub() {
        return (GraphClass) getTarget();
    }

    public boolean isProper() {
        return isProper;
    }
    
    public void setProper(boolean b) {
        isProper = b;
    }

    public int getConfidence() {
        return rel.getConfidence();
    }

    public void setConfidence(int c) {
        rel.setConfidence(c);
    }

    public void setRefs(List v) {
        rel.setRefs(v);
    }

    public void addRef(Object ref) {
        rel.addRef(ref);
    }

    public List getRefs() {
        return rel.getRefs();
    }

}

/* EOF */
