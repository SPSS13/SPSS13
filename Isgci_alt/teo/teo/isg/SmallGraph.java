/*
 * A common definition of Graph and Family
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/SmallGraph.java,v 1.1 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class SmallGraph{
    
    /** List of names, the first is the prime, the others are aliases */
    protected List<String> names;
    protected String link;
    protected SmallGraph complement;
    /**
     * Of a graph and its complement, one (the primary) is specified in the
     * xml-file, the other one (secondary) is created by complementing the
     * primary one.
     */
    protected boolean primary;
    /** Induced subgraphs common for all members of the SmallGraph */
    protected Vector< Vector<SmallGraph> > induced;
    
    /** Creates a new empty SmallGraph */
    public SmallGraph(){
        names = null;
        link = null;
        complement = null;
        primary = true;
        induced = null;
    }
    

    /** Adds a new name */
    public void addName(String parsedName){
        if (names == null)
            names = new ArrayList<String>(2);
        names.add(parsedName);
    }
    
    /** Returns the first name. */
    public String getName(){
        return names == null ? null : names.get(0);
    }
    
    /** Returns the list with all names. */
    public List<String> getNames(){
        return Collections.unmodifiableList(names);
    }

    /** Returns a string between [] with all the names of this. */
    protected String namesToString() {
        int i, j;
        StringBuffer s = new StringBuffer();
        if (names != null) {
            s.append("[");
            j = names.size();
            for (i=0; i<j; i++) {
                s.append( names.get(i) );
                if (i < j-1)
                    s.append("=");
            }
            s.append("]");
        }
        return s.toString();
    }
    
    /** Adds a link */
    public void addLink(String parsedLink){
        link = parsedLink;
    }
    
    /** Returns a link to the drawing */
    public String getLink(){
        return link;
    }
    
    /** Sets complement */
    public void setComplement(SmallGraph comp){
        complement = comp;
    }
    
    /** Returns complement */
    public SmallGraph getComplement(){
        return complement;
    }
    
    public void setPrimary(boolean prim){
        primary = prim;
    }
    
    public boolean isPrimary(){
        return primary;
    }

    /** Adds induced <tt>parsedInduced</tt> to induced */
    public void addInduced(Vector<SmallGraph> parsedInduced){
        if (induced == null)
            induced = new Vector<Vector<SmallGraph> >();
        induced.add(parsedInduced);
    }
    
    /** Returns Collection induced */
    public Vector< Vector<SmallGraph> > getInduced(){
        return induced;
    }

    
    /**
     * Return an incomplete complement of this.
     * this and the returned graph are marked as each others complement,
     * otherwise the new graph has no contents yet. It can be completed by
     * calling copyFromComplement.
     */
    public SmallGraph halfComplement() {
        SmallGraph c = null;
        try {
            c = (SmallGraph) getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        c.setPrimary(false);
        c.setComplement(this);
        setComplement(c);
        
        return c;
    }


    /**
     * Copy data from the complement to this. Ignores names and links.
     */
    public void copyFromComplement() {
        Vector<SmallGraph> v;
        int i, j;

        if (complement.getInduced() != null) {
            //---- First copy induced from complement
            induced = new Vector<Vector<SmallGraph> >();
            for (Vector<SmallGraph> is : complement.getInduced())
                induced.add(new Vector<SmallGraph>(is));

            //---- Then complement it.
            for (i = 0; i < induced.size(); i++) {
                v = induced.elementAt(i);
                if (v != null)
                    for (j=0; j < v.size(); j++) {
                        /*if (!(v.elementAt(j) instanceof SmallGraph)) {
                            System.err.println("Induced "+
                                v.elementAt(j) +" of "+ getName() +
                                " not a SmallGraph");
                        }*/
                        v.setElementAt(v.elementAt(j).getComplement(), j);
                    }
            }
        }
    }
    
    
    /** Create and return the complement of this SmallGraph */
    public SmallGraph makeComplement() {
        SmallGraph c = halfComplement();
        c.copyFromComplement();
        
        return c;
    }
    
    /* Ugly hack */
    public void addEdge(int a, int b){}
}
    
/* EOF */
