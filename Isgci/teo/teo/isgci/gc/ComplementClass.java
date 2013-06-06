/*
 * A GraphClass that is the complement of another GraphClass.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/ComplementClass.java,v 1.8 2011/05/29 16:50:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;


/**
 * A GraphClass that is a complement of another class.
 */
public class ComplementClass extends DerivedClass {
    
    /**
     * Creates a new graph class based on <tt>gc</tt>.
     */
    public ComplementClass(GraphClass gc) {
        super();
        // avoid recursion
        if (gc instanceof ComplementClass)
            throw new IllegalArgumentException("complement of a complement");
        setBase(gc);
        if (gc.getHereditariness() == Hered.INDUCED)
            hereditariness = Hered.INDUCED;
    }

    
    /**
     * Constructs the name of this class by adding the prefix "co--"
     * to the name of the base class.
     */
    public void setName() {
        if (getBase() instanceof SetClass)
            name = "co-("+ getBase().toString() +")";
        else
            name = "co--"+ getBase().toString();
        nameExplicit = false;
    }
    

    /**
     * Create a complement of this.
     */
    public GraphClass complement() {
        return getBase();
    }
    
}

/* EOF */
