/*
 * A GraphClass defined by union of two or more other GraphClasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/UnionClass.java,v 1.14 2011/05/29 16:50:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * A GraphClass defined by union of two or more other GraphClasses.
 */
public class UnionClass extends SetClass {

    /**
     * Creates a new graph class based on the GraphClasses in the
     * given set (doesn't need to be mathematical set).
     */
    public UnionClass(Collection<GraphClass> set){
        super();
        if (set == null  ||  set.size() < 2)
            throw new IllegalArgumentException("missing GraphClasses" + set);
        
        Set<GraphClass> gcSet = new HashSet<GraphClass>();
        Hered hered = Hered.STRICTEST;

        for (GraphClass gc : set) {
            /* avoid classes like (a+b)+c
             * instead make a      a+b+c
             */
            if (gc instanceof UnionClass) {
                gcSet.addAll(((UnionClass)gc).getSet());
            } else {
                gcSet.add(gc);
            }
            if (gc.getHereditariness().compareTo(hered) < 0)
                hered = gc.getHereditariness();
        }
        setSet(gcSet);
        hereditariness = hered;
    }

    
    /**
     * Constructs the name of this graphclass by connecting the names of
     * its base-classes with " \cup " (TeX-symbol for a union).
     */
    public void setName() {
        buildName(" $\\cup$ ");
    }

        
    public boolean subClassOf(GraphClass gc){
        if (super.subClassOf(gc))
            return true;
        if (gc instanceof UnionClass) {
            // A+B+C -> A+B (includes gc==this)
            return ((UnionClass)gc).getSet().containsAll(getSet());
        }
        return false;
    }
    
    
    public GraphClass unite(GraphClass gc) {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>(getSet());
        // result may be equal to this, e.g. if gc already elem in gcSet
        if (gc instanceof UnionClass) {
            set.addAll(((UnionClass)gc).getSet());
        } else {
            set.add(gc);
        }
        return new UnionClass(set);
    }


    /**
     * Create a complement of this by complementing the member classes.
     */
    public GraphClass complement() {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>();
        for (GraphClass gc : getSet())
            set.add(gc.complement());
        return new UnionClass(set);
    }
    
}
/* EOF */
