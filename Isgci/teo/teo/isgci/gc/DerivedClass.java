/*
 * A GraphClass that is derived from a single other class.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/DerivedClass.java,v 1.3 2012/04/09 11:28:24 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;


public abstract class DerivedClass extends GraphClass {
    
    /** Contains the base class */
    private GraphClass base;
    

    /**
     * Sets base as given.
     */
    protected final void setBase(GraphClass g) {
        if (base != null)
            throw new UnsupportedOperationException("Attempt to modify base");

        base = g;
    }


    /**
     * Return the GraphClass this is based on.
     */
    public final GraphClass getBase() {
        return base;
    }

    
    /**
     * Returns <tt>true</tt> if <tt>obj</tt> is of the same class as this
     * and based on an equal graphclass.
     */
    public boolean equals(Object obj){
        if (obj == this)
            return true;
        if (obj != null  &&  obj.getClass() == getClass()) {
            return base.equals(((DerivedClass)obj).base);
        }
        return false;
    }

    public int calcHash() {
        return base.hashCode();
    }
}

/* EOF */
