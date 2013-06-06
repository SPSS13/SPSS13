/*
 * Standard data for relations.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/db/RelationData.java,v 2.1 2011/10/22 20:08:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.db;

import java.util.List;
import java.util.ArrayList;


public class RelationData implements Relation {

    private List refs;
    private int confidence;         // How reliable this relation is

    public RelationData() {
        refs = null;
        confidence = CONFIDENCE_REVIEWED;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int c) {
        confidence = c;
    }

    public void setRefs(List v) {
        refs = v;
    }

    public void addRef(Object ref) {
        List v = getRefs();
        if (v == null) {
            v = new ArrayList(2);
            v.add(ref);
            setRefs(v);
        } else {
            v.add(ref);
        }
    }

    public List getRefs() {
        return refs;
    }

}

/* EOF */
