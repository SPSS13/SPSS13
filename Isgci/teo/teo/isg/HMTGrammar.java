/*
 * Head-Mid-Tail families
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/HMTGrammar.java,v 1.3 2011/04/07 07:28:31 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class HMTGrammar{
    /** The constituent graphs and their attachment/extension */
    HMTGraph head, mid, tail;
    /** Type of the grammar */
    int type;
    /** Name of the grammar */
    String name;

    /**
     * Create a new HMT grammar of the given type without a name.
     */
    public HMTGrammar(int type){
        head = mid = tail = null;
        this.type = type;
        name = null;
    }
    
    /**
     * Create a new HMT grammar of the given type with a given name.
     */
    public HMTGrammar(int type, String name){
        this(type);
        this.name = name;
    }
    
    /** Returns type of this grammar. */
    public int getType(){
        return type;
    }

    /** Returns name of this grammar if this grammar exists outside family. */
    public String getName(){
        return name;
    }

    /**
     * Set head (with attachment).
     */
    public void setHead(Graph head, int[] atth){
        if (atth.length != type)
            throw new IllegalArgumentException("Type of head wrong");
        this.head = new HMTGraph(head, null, atth);
    }

    /**
     * Get head (with attachment).
     */
    public HMTGraph getHead(){
        return head;
    }

    /**
     * Set mid (with attachment/extension).
     */
    public void setMid(Graph mid, int[] extm, int[] attm){
        if (extm.length != type  ||  attm.length != type)
            throw new IllegalArgumentException("Type of mid wrong");
        this.mid = new HMTGraph(mid, extm, attm);
    }

    /**
     * Get mid (with attachment/extension).
     */
    public HMTGraph getMid(){
        return mid;
    }

    /**
     * Set tail (with extension).
     */
    public void setTail(Graph tail, int[] extt){
        if (extt.length != type)
            throw new IllegalArgumentException("Type of tail wrong");
        this.tail = new HMTGraph(tail, extt, null);
    }

    /**
     * Get tail (with extension).
     */
    public HMTGraph getTail(){
        return tail;
    }

    /**
     * Return the graph Head Mid^n Tail.
     */
    public Graph getElement(int n){
        HMTGraph left = head;
        for (int i = 0; i < n; i++)
            left = compose(left, mid);
        return new Graph(compose(left, tail));
    }


    /**
     * Return the graphs Head Mid^i Tail, for 0 <= i <= n.
     */
    public Vector getSmallElements(int n){
        Vector res = new Vector();
        HMTGraph left = head;
        res.addElement(new Graph(compose(left, tail)));
        for (int i = 1; i <= n; i++) {
            left = compose(left, mid);
            res.addElement(new Graph(compose(left, tail)));
        }
        return res;
    }

    /**
     * Returns the composition xy.
     */
    private HMTGraph compose(HMTGraph x, HMTGraph y){
        if (x.att.length != y.ext.length)
            throw new IllegalArgumentException("Types for compose mismatch");
        
        int i, p, q;
        int [] attz = null;

        Graph z = new Graph(x);                 // Z = XY
        p = z.countNodes();
        int[] f = new int[y.countNodes()];      // f: V(y) -> V(z)
        for (i = 0; i < f.length; i++) {
            if ((q = index(y.ext, i)) >= 0)
                f[i] = x.att[q];
            else {
                z.addNode();
                f[i] = p++;
            }
        }

        /* Add edges from y */
        for (p = 0; p < y.countNodes()-1; p++)
            for (q = p+1; q < y.countNodes(); q++)
                if (y.getEdge(p, q))
                    z.addEdge(f[p], f[q]);

        /* Make attachment */
        if (y.att != null) {
            attz = new int[y.att.length];
            for (i = 0; i < attz.length; i++)
                attz[i] = f[y.att[i]];
        }

        return new HMTGraph(z, x.ext, attz);
    }


    /**
     * Returns the index of x in a, or -1 if not found.
     */
    private int index(int[] a, int x){
        for (int i = 0; i < a.length; i++)
            if (a[i] == x)
                return i;
        return -1;
    }
    
    public String toString(){
        String s = "HMTGrammar:";
        if (name != null)
            s+="\n"+getName();
        s+="\nHead:\n"+getHead().toString()+"\nMid:\n"+
                getMid().toString()+"\nTail:\n"+getTail().toString()+"\n";
        return s;
    }

    /*====================================================================*/

    /**
     * A graph with attachment and/or extension.
     */
    public class HMTGraph extends Graph{
        int[] ext, att;

        /**
         * Create a new graph with the given attachment/extension.
         * Att/ext are arrays containing the nodes in att/ext, in the proper
         * order. That is, if att(0) = 0, att(5) = 1, att(2) = 2, then
         * att=[0,5,2].
         * If the graph has no att (ext), use null.
         */
        public HMTGraph(Graph graph, int[] ext, int[] att){
            super(graph);
            if (att != null  &&  att.length != type  ||
                ext != null  &&  ext.length != type)
                throw new IllegalArgumentException("Type of graph wrong");
            this.att = null;
            this.ext = null;
            if (att != null) {
                this.att = new int[att.length];
                System.arraycopy(att, 0, this.att, 0, type);
            }
            if (ext != null) {
                this.ext = new int[ext.length];
                System.arraycopy(ext, 0, this.ext, 0, type);
            }
        }
        
        public int[] getAtt(){
            return att;
        }
        
        public int[] getExt(){
            return ext;
        }
        
        public String toString(){
            String s = super.toString()+"\n";
            if (ext != null)
                s+="Extension: "+ext+"\n";
            if (att != null)
                s+="Attachment: "+att+"\n";
            return s;
        }
    }
}

/* EOF */
