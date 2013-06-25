/*
 * A GraphClass defined by intersection of two or more other GraphClasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/IntersectClass.java,v 1.14 2011/05/29 16:50:54 ux Exp $
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
 * A GraphClass defined by intersection of two or more other GraphClasses.
 */
public class IntersectClass extends SetClass {

    /**
     * Creates a new graph class based on the GraphClasses in the
     * given set (doesn't need to be mathematical set).<br>
     * Note that this constructor may result in a class different to one
     * that was created with gc1.intersect(gc2). This constructor will
     * always create an IntersectClass while the result of intersect()
     * depends on the type of the classes to be intersected.
     */
    public IntersectClass(Collection<GraphClass> set) {
        super();
        if (set == null  ||  set.size() < 2)
            throw new IllegalArgumentException("missing GraphClasses" + set);
        
        Set<GraphClass> gcSet = new HashSet<GraphClass>();
        Hered hered = Hered.STRICTEST;

        for (GraphClass gc : set) {
            /* avoid classes like (a+b)+c
             * instead make a      a+b+c
             */
            if (gc instanceof IntersectClass)
                gcSet.addAll(((IntersectClass)gc).getSet());
            else
                gcSet.add(gc);
            
            if (gc.getHereditariness().compareTo(hered) < 0)
                hered = gc.getHereditariness();
        }

        setSet(gcSet);
        hereditariness = hered;
    }

    
    /**
     * Constructs the name of this graphclass by connecting the names of
     * its base-classes with " \cap " (TeX-symbol for a union).
     */
    public void setName() {
        buildName(" $\\cap$ ");
    }
        

    public boolean subClassOf(GraphClass gc) {
        if (super.subClassOf(gc))
            return true;
        if (gc instanceof IntersectClass) {
            // A*B -> A*B*C
            return getSet().containsAll( ((IntersectClass)gc).getSet() );
        } else {
            // A -> A*B
            return getSet().contains(gc);
        }
    }
   

    public GraphClass intersect(GraphClass gc) {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>(getSet());
        // result may be equal to this, e.g. if gc already elem in gcSet
        if (gc instanceof IntersectClass) {
            set.addAll(((IntersectClass)gc).getSet());
        } else {
            set.add(gc);
        }
        return new IntersectClass(set);
    }

    
    /**
     * Create a complement of this by complementing the member classes.
     */
    public GraphClass complement() {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>();
        for (GraphClass gc : getSet())
            set.add(gc.complement());
        return new IntersectClass(set);
    }
    
}
/* EOF */
