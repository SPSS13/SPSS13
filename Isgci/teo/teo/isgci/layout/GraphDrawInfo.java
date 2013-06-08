/*
 * An extended WalkerInfo with additional fields for the ranking-process.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/GraphDrawInfo.java,v 2.0 2011/09/25 12:36:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import teo.isgci.grapht.WalkerInfo;

/**
 * This extended WalkerInfo contains some additional fields
 * to store values used in the ranking process
 */
public class GraphDrawInfo<V,E> extends WalkerInfo<V,E> {

    /* WalkerInfo:
     * status, parent, distance, discover, finish, mark
     */
    
    // values for edges
    public int cutValue;
    public int minlen;
    public int weight;
    
    // values for nodes
    public int rank;
    public boolean virt;    // virtual nodes are flagged with true
    public boolean aux;     // is this an auxiliary node for xcoords?
    
    // the next 3 values are needed for ranking, when edges with negative
    // cutvalues are replaced to find an optimal feasible tree
    public int low;         // dfs-discover-time
    public int lim;         // dfs-finish-time
    public E treeParent; // edge over which a node was reached
    
    public double median;   // temporary value to sort nodes within ranks
    public int order;       // order in Rank (faster than using indexOf())
    
    public int xCoord;      // coordinates on canvas
    public int yCoord;
    
    public int width;       // dimension of a node
    public int height;

    public GraphDrawInfo(int width) {
        this();
        this.width = width;
    }

    public GraphDrawInfo(){
        //super(); // called by default
        cutValue=Integer.MIN_VALUE;
        minlen=1;
        weight=1;
        
        aux = false;
        virt=false;
        rank=-1;
        low=-1;
        lim=-1;
        treeParent=null;
        median=-1.0;
        order=-1;
        xCoord=-1;
        yCoord=-1;
        width=50;
        height=30;
    }

    public String toString() {
        StringBuffer s = new StringBuffer(super.toString());
        s.append("; low= ");   s.append(low);
        s.append(", lim=");  s.append(lim);
        s.append(", tp=");   s.append(treeParent);
        s.append(", rank=");   s.append(rank);
        s.append(", order=");   s.append(order);
        s.append(", width=");   s.append(width);
        s.append(", height=");   s.append(height);
        s.append(", minlen=");   s.append(minlen);
        return s.toString();
    }
}
