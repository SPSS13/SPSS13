/*
 * Represents a simple family of graphs
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/SimpleFamily.java,v 1.8 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class SimpleFamily extends Family{
    
    /** Smallgraphs contained in Family */
    private Vector<SmallGraph> contains;
    
    /**
     * Induced subgraphs common for all members of Family except for those in
     * contains
     */
    private Vector<Vector<SmallGraph> > inducedRest;
    
    /** Creates a new SimpleFamily without Graphs */
    public SimpleFamily(){
        super();
        contains = null;
        induced = null;
        inducedRest = null;
    }
    

    public void copyFromComplement() {
        super.copyFromComplement();

        //---- First copy the complement
        SimpleFamily f = (SimpleFamily) complement;
        if (f.getContains() != null)
            contains = (Vector)f.getContains().clone();
        else
            contains = null;
        if (f.getInducedRest() != null) {
            inducedRest = new Vector<Vector<SmallGraph> >();
            for (Vector v : f.getInducedRest())
                inducedRest.addElement((Vector) v.clone());
        }
        else
            inducedRest = null;
        
        //---- Then complement
        int i, j;
        
        if (contains != null)
            for (i=0; i<contains.size(); i++)
                contains.setElementAt(contains.elementAt(i).getComplement(),i);
        
        if (inducedRest != null)
            for (i=0; i<inducedRest.size(); i++) {
                Vector<SmallGraph> v = inducedRest.elementAt(i);
                if (v != null)
                    for (j=0; j<v.size(); j++)
                        v.setElementAt(v.elementAt(j).getComplement(), j);
            }
    }
    
    /** Adds contains <tt>parsedContains</tt> to SimpleFamily */
    public void addContains(SmallGraph parsedContains){
        if (contains == null)
            contains = new Vector<SmallGraph>(2,2);
        contains.addElement(parsedContains);
    }
    
    /** Returns Vector contains */
    public Vector<SmallGraph> getContains(){
        return contains;
    }
    
    
    /** Adds inducedRest <tt>parsedInducedRest</tt> to SimpleFamily */
    public void addInducedRest(Vector<SmallGraph> parsedInducedRest){
        if (inducedRest == null)
            inducedRest = new Vector<Vector<SmallGraph> >(2,2);
        inducedRest.addElement(parsedInducedRest);
    }
    
    /** Returns Vector inducedRest */
    public Vector<Vector<SmallGraph> > getInducedRest(){
        return inducedRest;
    }
    
    
    public String toString(){
        int i, j;
	Vector v = null;
	String s = "Name: "+getName();
	if (contains != null) {
            s+="\nContains: ";
	    for (i=0; i<contains.size(); i++)
	        s+= contains.elementAt(i).getName()+"; ";
	}
        if (induced != null) {
            s+="\nInduced: ";
	    for (i=0; i<induced.size(); i++) {
	        v = (Vector) induced.elementAt(i);
	        for (j=0; j<v.size()-1; j++)
	            s+=((SmallGraph) v.elementAt(j)).getName()+"; ";
	    }
	}
	if (inducedRest != null) {
            s+="\nInducedRest: ";
	    for (i=0; i<inducedRest.size(); i++) {
	        v = inducedRest.elementAt(i);
	        for (j=0; j<v.size()-1; j++)
	            s+=((SmallGraph) v.elementAt(j)).getName()+"; ";
	    }
	}
        s+="\nLink: "+link+"\nComplement: "+complement.getName();
	return s;
    }
}
    
/* EOF */
