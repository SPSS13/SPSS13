/*
 * Represents a family of graphs given by a union of SmallGraphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/UnionFamily.java,v 1.8 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class UnionFamily extends Family{
    
    /** Subfamilies of UnionFamily */
    Vector<SmallGraph> subfamilies;
    
    /** Creates a new UnionFamily without Graphs */
    public UnionFamily(){
        super();
        subfamilies = null;
    }
    

    public void copyFromComplement(){
        super.copyFromComplement();
        UnionFamily f = (UnionFamily) complement;
        if (f.getSubfamilies() != null) {
            subfamilies = (Vector<SmallGraph>) f.getSubfamilies().clone();
            for (int i=0; i<subfamilies.size(); i++)
                subfamilies.setElementAt(
                        subfamilies.elementAt(i).getComplement(), i);
        }
    }

    /** Adds subfamily <tt>subf</tt> to UnionFamily */
    public void addSubfamily(SmallGraph subf){
        if (subfamilies == null)
            subfamilies = new Vector<SmallGraph>(2,2);
        subfamilies.addElement(subf);
    }
    
    /** Returns Vector subfamilies */
    public Vector<SmallGraph> getSubfamilies(){
        return subfamilies;
    }
    
    
    public String toString(){
        int i;
	String s = "Name: "+getName()+"\nSubfamilies: ";
	for (i=0; i<subfamilies.size(); i++)
	    s+=((SmallGraph) subfamilies.elementAt(i)).getName()+"; ";
	s+="\nLink: "+link+"\nComplement: "+complement.getName();
	return s;
    }
}
    
/* EOF */
