/*
 * A GraphClass based on another class and adding the hereditary property.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/HereditaryClass.java,v 1.10 2012/04/09 11:28:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;

/**
 * A GraphClass based on another class and adding the hereditary property.
 */
public abstract class HereditaryClass extends DerivedClass {
    
    /** Creates a new graph class based on <tt>gc</tt>. */
    public HereditaryClass(GraphClass gc){
        super();
        if (gc instanceof HereditaryClass)
            throw new IllegalArgumentException("double hereditary");
        setBase(gc);
    }

    
    /**
     * Constructs the name of this class by adding the prefix "hereditary "
     * to the name of the base class.
     */
    public void setName(){
        name = "hereditary "+ getBase().toString();
        nameExplicit = false;
    }


    public void setHereditariness(int h) {
        throw new UnsupportedOperationException(
            "Hereditariness cannot be set for HereditaryClass "+this);
    }
 

    /** Is this a subclass of gc? **/
    public boolean subClassOf(GraphClass gc) {
        if (super.subClassOf(gc))
            return true;
        if (getBase().equals(gc))
            return true;
        if (gc instanceof HereditaryClass  &&
            getBase() == ((HereditaryClass) gc).getBase()  &&
            gc.getClass().isInstance(this))
            return true;
        return false;
    }


    /** Return a reference string describing why subClassOf returned true. */
    public String whySubClassOf() {
        return "hereditary";
    }


    /**
     * Returns <tt>true</tt> if <tt>obj</tt> is a HereditaryClass of the same
     * type as this and is based on an equal graphclass.
     */
    public boolean equals(Object obj){
        if (obj == this)
            return true;
        return obj != null  &&  obj.getClass() == getClass()  &&
            getBase().equals(((HereditaryClass)obj).getBase());
    }

    public int calcHash() {
        return getBase().hashCode();
    }
    
}

/* EOF */
