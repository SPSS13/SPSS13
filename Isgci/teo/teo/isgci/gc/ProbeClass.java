/*
 * A GraphClass that is the probe class of another GraphClass.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gc/ProbeClass.java,v 1.5 2011/05/29 16:50:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */


package teo.isgci.gc;


/**
 * A GraphClass that is the probe class of another class.
 */
public class ProbeClass extends DerivedClass {
    
    /** Creates a new graph class based on <tt>gc</tt>. */
    public ProbeClass(GraphClass gc){
        super();
        setBase(gc);
        if (gc.getHereditariness() == Hered.INDUCED)
            hereditariness = Hered.INDUCED;
    }

    
    /**
     * Constructs the name of this class by adding the prefix "probe "
     * to the name of the base class.
     */
    public void setName(){
        name = "probe "+ getBase().toString();
        nameExplicit = false;
    }
    
}
/* EOF */
