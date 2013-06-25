/*
 * A GraphClass that is self-contained.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/BaseClass.java,v 1.12 2012/04/09 11:28:24 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;


/**
 * A GraphClass that is defined witout refering to other GraphClasses
 * or by forbidden induced subgraphs. Its only property is its name.
 */
public class BaseClass extends GraphClass {
    
    /** Creates a new graph class with a name. */
    public BaseClass(String s){
        super();
        if (s == null)
            throw new IllegalArgumentException("No name given for BaseClass");
        setName(s);
    }


    /**
     * No need to do anything, since the name is set by the constructor.
     */
    public void setName() {}

    public void setName(String s) {
        if (name != null)
            throw new UnsupportedOperationException(
                "Attempt to change name of BaseClass");
        super.setName(s);
    }


    /**
     * Returns true if obj is a BaseClass with the same name.
     */
    public boolean equals(Object obj){
        if (obj == this)
            return true;
        if(obj instanceof BaseClass){
            return name.equals(((BaseClass)obj).name);
        }
        return false;
    }

    public int calcHash() {
        return name.hashCode();
    }
    
}
/* EOF */
