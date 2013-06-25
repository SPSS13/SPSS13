/*
 * The abstract GraphClass.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/GraphClass.java,v 1.10 2012/04/09 14:06:25 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

public abstract class GraphClass {

    public enum Hered {
        /* We have to check consistency among equivalent classes with this one
        public static final int OPEN_HERED = 0; */
        UNKNOWN,
        /** Property holds for every isometric connected induced subgraph */
        ISOMETRIC,
        /** Property holds for every connected induced subgraph */
        CONNECTED,
        /** Property holds for every induced subgraph */
        INDUCED,
        /** For having a max. */
        STRICTEST
    };

    /** Is this class hereditary in some sense? */
    protected Hered hereditariness;
    /** Is the hereditariness set explicitly? */
    protected boolean hereditarinessExplicit;
    /** Is this class marked in the input as self-complementary? */
    protected boolean selfComplementary;
    /** Is this class marked in the input as clique-fixed (K(C)=C)? */
    protected boolean cliqueFixed;
    /** ID ("gc_*") of this class */
    protected String id;
    /** The references for this class */
    protected Collection refs;
    
    /** Name of this GraphClass. */
    protected String name;
    /** Is the name set explicitly? */
    protected boolean nameExplicit;

    /** hashcode of this graphclass */
    protected int hashcode;
    /** Did we calculate the hashcode already? */
    protected boolean havehash;


    public GraphClass() {
        name = null;
        nameExplicit = false;
        hereditariness = Hered.UNKNOWN;
        hereditarinessExplicit = false;
        /*id = null; keep undefined to provoke errors */
        refs = null;
        havehash = false;
    }


    /** Get the hereditariness */
    public Hered getHereditariness() {
        return hereditariness;
    }


    /** Set the hereditariness */
    public void setHereditariness(Hered h) {
        hereditariness = h;
        hereditarinessExplicit = true;
    }


    /** Is the hereditariness set explicitly? */
    public boolean hereditarinessExplicitly() {
        return hereditarinessExplicit;
    }


    /**
     * Get/Set self-complementary property.
     */
    public boolean isSelfComplementary() {
        return selfComplementary;
    }

    public void setSelfComplementary(boolean b) {
        selfComplementary = b;
    }


    /**
     * Get/Set cliquedfixed property.
     */
    public boolean isCliqueFixed() {
        return cliqueFixed;
    }

    public void setCliqueFixed(boolean b) {
        cliqueFixed = b;
    }
    

    /**
     * Get/Set ID
     */
    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }


    /**
     * Sets v as the references for this class.
     * Note that v is used as-is and not duplicated!
     */
    public void setRefs(Collection v) {
        refs = v;
    }


    /**
     * Returns the references (original, not copied!) for this class.
     */
    public Collection getRefs() {
        return refs;
    }


    /**
     * Sets the name of the GraphClass.
     * This method can be used to construct the name of the GraphClass
     * automatically, based on their definition.
     * If a derived class overrides this method, it should set
     * nameExplicit=false!
     */
    public abstract void setName();
   

    /**
     * Sets the name of this class to <tt>s</tt>.
     * Used, if automatic construction is not possible or not wanted.
     */
    public void setName(String s) {
        name = s;
        nameExplicit = true;
    }


    /**
     * Is the name set explicitly or constructed automatically from its
     * definition?
     */
    public boolean namedExplicitly() {
        return nameExplicit;
    }


    /**
     * Checks if the set of graphs described by this graphclass is a subset
     * of the set described by <tt>gc</tt>.<br>
     * This method does only find trivial inclusions. That means inclusions
     * that follow from characterizations of the classes using rules like
     * relations between sets and subsets.
     * It is not the purpose of this method to provide a complete
     * inclusion-graph. This task is left for other classes.
     */
    public boolean subClassOf(GraphClass gc){
        if (this == gc)
            return true;
        // A << A+B+...
        if(gc instanceof UnionClass)
            return ((UnionClass)gc).getSet().contains(this);
        // A << probe A
        if (gc instanceof ProbeClass)
            return ((ProbeClass) gc).getBase() == this;
        return false;
    }


    /**
     * Return a reference string describing why subClassOf returned true.
     */
    public String whySubClassOf() {
        return "trivial";
    }
   

    /**
     * Creates an intersection of this and the specified GraphClass.
     * The exact type of the result depends on the types of the both
     * classes that are intersected.
     * This default implementation returns an IntersectClass.
     */
    public GraphClass intersect(GraphClass gc) {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>();
        set.add(this);
        if (gc instanceof IntersectClass) {
            // If this already in gc results in a duplicate of gc.
            set.addAll(((IntersectClass) gc).getSet());
        } else {
            // if this==gc -> error when constructing a new IntersectClass
            // with only one class in Set
            set.add(gc);
        }
        
        return new IntersectClass(set);
    }
   

    /**
     * Creates a union of this and the specified GraphClass.
     */
    public GraphClass unite(GraphClass gc) {
        ArrayList<GraphClass> set = new ArrayList<GraphClass>();
        set.add(this);
        if(gc instanceof UnionClass) {
            // If this already in gc results in a duplicate of gc.
            set.addAll(((UnionClass)gc).getSet());
        } else {
            // if this==gc -> error when constructing a new UnionClass
            // with only one class in Set
            set.add(gc);
        }
        
        return new UnionClass(set);
    }


    /**
     * Creates a complement of this class.
     */
    public GraphClass complement() {
        return new ComplementClass(this);
    }


    /**
     * equals and calcHash should be implemented consistently by inheriting
     * classes.
     */
    public abstract boolean equals(Object obj);

    public final int hashCode() {
        if (!havehash) {
            hashcode = calcHash();
            havehash = true;
        }
        return hashcode;
    }

    protected abstract int calcHash();
   

    /**
     * Returns a String representation of this GraphClass.
     * If <tt>name</tt> is still unset it is first tried to construct the
     * name automatically.
     */
    public String toString() {
        if (name == null)
            setName();
        return name;
    }
}

/* EOF */
