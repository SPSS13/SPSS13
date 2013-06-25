/*
 * Represents a family of graphs given by HMT-grammar
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/HMTFamily.java,v 1.10 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class HMTFamily extends Family{
    
    /** Head-Mid-Tail grammar defining Family */
    private HMTGrammar hmtg;
    /** Index of the Head-Mid-Tail grammar defining Family */
    private String index;
    /** Smallmembers of this Family */
    private Vector<SmallGraph> smallmembers;
    
    /** Creates a new HMTFamily without Graphs */
    public HMTFamily(){
        super();
	hmtg = null;
        index = null;
        induced = null;
	smallmembers = null;
    }
    
    
    /**
     * Copies the smallmembers from the complement. Grammar and index is not
     * updated (i.e. usually remains null).
     */
    public void copyFromComplement() {
        super.copyFromComplement();

        HMTFamily f = (HMTFamily) complement;
	if (f.smallmembers != null) {
            //---- First copy the complement
	    smallmembers = (Vector) f.smallmembers.clone();

            //---- Then complement
            for (int i=0; i<smallmembers.size(); i++) {
                smallmembers.setElementAt(
                        smallmembers.elementAt(i).getComplement(), i);
            }
        }
    }


    /** Returns Head-Mid-Tail grammar defining Family. */
    public HMTGrammar getGrammar(){
        return hmtg;
    }
    
    /** Sets Head-Mid-Tail grammar defining Family. */
    public void setGrammar(HMTGrammar hmtg){
        this.hmtg = hmtg;
    }
    
    /** Initializes array smallmembers of size <tt>smSize</tt> */
    public void initFromGrammar(int smSize){
        if (index == null)
            smallmembers = hmtg.getSmallElements(smSize);
        else {
            smallmembers = new Vector();
            IndexExpr ie = new IndexExpr(index);
            for (int i=0; i<=smSize; i++)
                smallmembers.addElement(hmtg.getElement(ie.eval(i)));
        }
    }
    
    /** Returns index of the Head-Mid-Tail grammar defining Family. */
    public String getIndex(){
        return index;
    }
    
    /** Sets index of the Head-Mid-Tail grammar defining Family. */
    public void setIndex(String index){
        this.index = index;
    }
    
    /** Return array smallmembers */    
    public Vector<SmallGraph> getSmallmembers(){
        return smallmembers;
    }
    
    /** Set array <tt>smMem</tt> as smallmembers in this HMTFamily */
    public void setSmallmembers(Vector<SmallGraph> smMem){
        smallmembers = (Vector) smMem.clone();
    }

    /** Add a smallmember */
    public void addSmallmember(SmallGraph sm) {
        if (smallmembers == null)
            smallmembers = new Vector<SmallGraph>();
        smallmembers.addElement(sm);
    }

    
    public String toString(){
        int i, j;
	Vector v = null;
	String s = "Name: "+getName();
        if (hmtg != null) {
            if (index != null)
                if (hmtg.getName() != null)
                    s+="\n"+index+"\n"+hmtg.getName();
                else
                    System.err.println("\nIndex for a grammar without name!");
            else
                s+="\nHMTGrammar:\n"+hmtg.toString();
        }
        if (smallmembers != null) {
            s+="\nSmallmembers: ";
            for (i=0; i<smallmembers.size(); i++) {
                s+=((Graph)smallmembers.elementAt(i)).getName()+", ";
            }
	}
        if (induced != null) {
            s+="\nInduced: ";
	    for (i=0; i<induced.size(); i++) {
	        v = (Vector) induced.elementAt(i);
	        for (j=0; j<v.size()-1; j++)
	            s+=((SmallGraph) v.elementAt(j)).getName()+"; ";
	    }
	}
        s+="\nLink: "+link+"\nComplement: "+complement.getName();
	return s;
    }
}
    
/* EOF */
