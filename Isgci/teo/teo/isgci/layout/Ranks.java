/*
 * Holds the nodes of a graph in Ranks. The nodes are ordered within
 * its ranks to minimize edge-crossings.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/Ranks.java,v 2.1 2011/09/29 08:40:33 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import java.util.ArrayList;
import java.util.Arrays;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.util.TypeUtil;

import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Annotation;
import teo.isgci.grapht.WalkerInfo;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.BFSWalker;

/**
 * Holds the nodes of a graph in Ranks. The nodes are ordered within
 * its ranks to minimize edge-crossings.
 */
public class Ranks<V,E> {
    
    ArrayList<ArrayList<V> > ranks;
    boolean changed[];
    SimpleDirectedGraph<V,E> graph;
    HierarchyLayout layout;
    Annotation<V,E,GraphDrawInfo<V,E> > gdiAnn;
    
    public Ranks(HierarchyLayout l) {
        layout = l;
        graph = l.getGraph();
        gdiAnn = l.gdiAnn;
        
        // find maximum rank
        // (already done in balance, may be get it from there)
        int max = Integer.MIN_VALUE;
        for (V v : graph.vertexSet()) {
            int rank = gdiAnn.getNode(v).rank;
            if (rank > max)
                max = rank;
        }
        
        ranks = new ArrayList<ArrayList<V> >(max+1);
        changed = new boolean[max+1];
        for (int i = 0; i < max+1; i++)
            ranks.add(new ArrayList<V>());
    }


    /**
     * Copy the ranks from source to this.
     */
    public void copyRanks(Ranks<V,?> source) {
        copy(source.ranks, ranks);
    }
    

    /** Returns the rank specified by <tt>index</tt>. */
    public ArrayList<V> getRank(int index){
        return ranks.get(index);
    }
    
    public int cntRanks(){
        return ranks.size();
    }
    
    /* (parameters are only listed to tell apart methods with equal names)
     * minimizeCrossings()
     *     createVirtualNodes()
     *     initOrder()
     *     crossing()
     *         crossing(int)
     *     wMedian()
     *         medianValue()
     *         sort()
     *     transpose()
     *         locallayOptimal()
     */
    
    /**
     * Main Procedure.
     */
    public void minimizeCrossings(){
        final int rSize = ranks.size();
        ArrayList<ArrayList<V> > best = new ArrayList<ArrayList<V> >(rSize);
        int i, cnt,bestCnt;
        int best4, best8, best12;
        
        layout.createVirtualNodes();
        initOrder();
        bestCnt=crossing();
        if (bestCnt == 0)
            return;
        copy(ranks, best);
        
        for (i = 0; i < 4; i++) {
            wMedian(i);
            transpose(i);
            cnt = crossing();
            if (cnt == 0)
                return;
            if (cnt < bestCnt) {
                copy(ranks, best);
                bestCnt = cnt;
            }
            //System.out.println("iter "+i+" current "+cnt+" best "+bestCnt);
        }
        best4 = bestCnt;

        for (; i < 8; i++) {
            wMedian(i);
            transpose(i);
            cnt = crossing();
            if (cnt == 0)
                return;
            if (cnt < bestCnt) {
                copy(ranks, best);
                bestCnt = cnt;
            }
            //System.out.println("iter "+i+" current "+cnt+" best "+bestCnt);
        }
        best8 = bestCnt;
        if (best8 <= 20  ||  (float) best8/best4 <= 0.88) {
            copy(best, ranks);
            return;
        }

        for (; i < 12; i++) {
            wMedian(i);
            transpose(i);
            cnt = crossing();
            if (cnt == 0)
                return;
            if (cnt < bestCnt) {
                copy(ranks, best);
                bestCnt = cnt;
            }
            //System.out.println("iter "+i+" current "+cnt+" best "+bestCnt);
        }
        best12 = bestCnt;
        if (best12 < 200  &&
                ((float) best12/best8 < 0.99 || (float) best8/best4 > 0.9)) {
            copy(best, ranks);
            return;
        }

        for(; i < 19; i++){  
            wMedian(i);
            transpose(i);
            cnt=crossing();
            if(cnt<bestCnt){
                copy(ranks,best);
                bestCnt=cnt;
            }
            //System.out.println("iter "+i+" current "+cnt+" best "+bestCnt);
        }
        copy(best,ranks);
    }
    
    protected void copy(ArrayList< ArrayList<V> > source,
            ArrayList<ArrayList<V> > dest){
        dest.clear();
        for (ArrayList<V> s : source)
            dest.add((ArrayList<V>) s.clone());
    }
    
    /**
     * Initially orders the nodes in each rank.
     * This is done by a BFS starting with vertices of minimum rank.
     * Vertices are placed in their ranks when they are visited by BFS.
     * This ensures that the initial ordering of a tree has no crossings.
     */
    void initOrder(){
        for (WalkerInfo wi : gdiAnn.nodeValues())
            wi.reset();

        for (V node : GAlg.topologicalOrder(graph)) {
            if( gdiAnn.getNode(node).status != GraphWalker.Status.UNSEEN)
                continue; // already seen
            
            Annotation<V,E,WalkerInfo<V,E> > ann =
                    TypeUtil.uncheckedCast( gdiAnn, null );
            new BFSWalker<V,E>(graph, node, ann, GraphWalker.InitCode.NONE) {
                public void visit(V v) {
                    int rank = gdiAnn.getNode(v).rank;
                    gdiAnn.getNode(v).order = ranks.get(rank).size();
                    ranks.get(rank).add(v);
                    super.visit(v);
                }
            }.run();
        }
    }
    
    /**
     * Counts the number of crossings.
     */
    protected int crossing(){
        int i,count=0;
        
        for(i=ranks.size()-2;i>=0;i--)
            count+=crossing(i);
        
        return count;
    }
    
    /**
     * Counts the number of crossings between the given rank and its
     * successor.
     * 
     * @param nr index of a rank
     */
    protected int crossing(int nr){
        // count number of edges between the ranks
        int i,k,j,cnt,edgeCnt=0;
        final int size=ranks.get(nr).size();
        for(i=0;i<size;i++)
            edgeCnt += graph.outDegreeOf(ranks.get(nr).get(i));
        
        // meaning of from and to:
        // if there is an edge from node i in rank1 to node j in rank2
        // then from[cnt]=i; to[cnt]=j;
        int from[] = new int[edgeCnt],
            to[]   = new int[edgeCnt];
        cnt=0;
        for(i=0;i<size;i++){
            for (V w : GAlg.outNeighboursOf(graph, ranks.get(nr).get(i))) {
                j = gdiAnn.getNode(w).order;
                from[cnt]=i;
                to[cnt]=j;
                cnt++;
            }
        }
        
        // now count crossings:
        // edge x and edge y cross if from[x] < (>) from[y]
        // AND to[x] > (<) to[y]
        // Since from is sorted in ascending order (see how it was build)
        // we can skip the 2nd case from[i]>from[j] ...
        cnt=0;
        for(i=0;i<edgeCnt-1;i++){
            /*for(j=i+1;j<edgeCnt;j++)
                if(from[i]<from[j] && to[i]>to[j])
                    cnt++;*/
            
            for(j=i+1;j<edgeCnt && from[i]==from[j];j++) /* empty */ ;
            
            for(;j<edgeCnt;j++)
                if(to[i]>to[j])
                    cnt++;
        }
        
        return cnt;
    }
    
    /**
     * Reorders the nodes within each rank based on the
     * weighted median heuristic.
     * Depending on <tt>iter</tt> the ranks are traversed from
     * top to bottom (<tt>iter</tt>=even) or from bottom to top
     * (<tt>iter</tt>=odd).
     */
    protected void wMedian(int iter){
        final int size=ranks.size();
        int i,j;
        int from,to,inc;
        V node;
        boolean dir=((iter & 1) == 0);
        
        from= dir ? 1 : size-2;
        to= dir ? size : -1;
        inc= dir ? 1 : -1;
        
        for(i=from;i!=to;i+=inc){
            for(j=ranks.get(i).size()-1;j>=0;j--){
                node = ranks.get(i).get(j);
                gdiAnn.getNode(node).median = medianValue(node,dir);
            }
            sort(i);
        }
    }
    
    /**
     * Calculates the median value of a node.
     * 
     * @param v    the node whose median value is to be calculated
     * @param dir  determines direction (<tt>true</tt> -> forward)
     */
    protected double medianValue(V v,boolean dir){
        // no adjacent nodes in rank
        int cnt=dir ? graph.inDegreeOf(v) : graph.outDegreeOf(v);
        if(cnt==0) return -1.0;
        
        // build array pos which contains the present positions
        // of the nodes in rank adjacent to v
        int i=0, mid=cnt>>1;
        int pos[]=new int[cnt];
        for (V node : dir ? GAlg.inNeighboursOf(graph, v) :
                            GAlg.outNeighboursOf(graph, v))
            pos[i] = gdiAnn.getNode(node).order;
        
        Arrays.sort(pos);
        
        if((cnt&1)==1) return pos[mid];  // if cnt is odd
        
        if(cnt==2) return (pos[0]+pos[1])/2.0;
        
        // cnt is even and >=4
        int left  = pos[mid-1]-pos[0],
            right = pos[cnt-1]-pos[mid];
        return ((double)(pos[mid-1]*right+pos[mid]*left))/(left+right);
    }
    
    /**
     * Sorts the nodes in <tt>rank</tt> by their median value.
     * Nodes with median==-1 (no adjacent nodes) are left in their
     * current positions.
     * 
     * @param nr    index of rank to be sorted
     * @param iter  current iteration
     */
    protected void sort(int nr){
        final int size=ranks.get(nr).size();
        int i,j,min;
        double d;
        double medians[]=new double[size];
        
        for(i=0;i<size;i++){
            medians[i]=gdiAnn.getNode(ranks.get(nr).get(i)).median;
        }
        
        // SelectSort
        // QSort doesn't work because of (median==-1)-nodes
        for(i=0;i<size-1;i++){
            if(medians[i]<0) continue;  // skip
            min=i;
            
            /* It is possible to check medians[j]==medians[min] depending
             * on current iteration (just like in transpose()). But
             * here it seems to be worthless.
             */
            for(j=i+1;j<size;j++)
                if(medians[j]>=0 && (medians[j]<medians[min]))
                    min=j;
            
            // swap(i,min)
            if(i!=min){
                exchange(nr,i,min);
            
                d=medians[i];
                medians[i]=medians[min];
                medians[min]=d;
            }
        }
    }
    
    /**
     * Exchanges the nodes at position <tt>pos1</tt> and <tt>pos2</tt>
     * in rank <tt>nr</tt>. As a side-effect the GrapDrawInfo.order
     * field is updated.
     */
    private void exchange(int nr,int pos1,int pos2){
        V node1 = ranks.get(nr).get(pos1);
        V node2 = ranks.get(nr).get(pos2);
        ranks.get(nr).set(pos1,node2);
        ranks.get(nr).set(pos2,node1);
        gdiAnn.getNode(node1).order=pos2;
        gdiAnn.getNode(node2).order=pos1;
    }
    
    /**
     * Exchanges adjacent nodes on the same rank if this decreases
     * the number of crossings.
     */
    protected void transpose(int iter){
        final int size=ranks.size();
        int improved;
        int i,j;
        int delta,inChange,outChange;
        boolean chk=((iter&2)==2);
        boolean chk2=((iter&1)==1);
        int from,to,inc;
        V v,w;
        
        for(i=0;i<size;i++)
            changed[i]=true;
        
        do{
            improved=0;
            for(i=0;i<size;i++)
                if(changed[i]){
                    from=chk2?1:ranks.get(i).size()-1;
                    to=chk2?ranks.get(i).size():0;
                    inc=chk2?1:-1;
                    
                    for(j=from;j!=to;j+=inc){
                    //for(j=order[i].size()-1;j>0;j--){
                        changed[i]=false;
                        v = ranks.get(i).get(j-1);
                        w = ranks.get(i).get(j);
                        inChange=
                            (i==0) ? 0 : locallyOptimal(v,w,false);
                        outChange=
                            (i==size-1) ? 0 : locallyOptimal(v,w,true);
                        delta=inChange+outChange;
                        if(delta<0 || (chk && delta==0)){
                            exchange(i,j,j-1);
                            //order[i].setElementAt(v,j);
                            //order[i].setElementAt(w,j-1); // exchange v,w
                            improved+=delta;
                            changed[i]=true;
                            if(i<size-1)
                                changed[i+1]=true;
                            if(i>0)
                                changed[i-1]=true;
                        }
                    }
                }
        }while(improved<0);
    }
    
    /**
     * Is the ordering of v to the left of w locally optimal? That is, will the
     * number of edge crossings increase when w is placed to the left of v?
     * It is assumed that all out neighbours of v,w are on the rank following
     * v,w.
     * 
     * @param dir if <tt>true</tt> take forward edges (top to bottom)
     * @return value the number of crossings would change if v and w
     *         were exchanged. A value of -3 means the number of
     *         crossings would decreased by 3.
     */
    
    protected int locallyOptimal(V v, V w, boolean dir){
        int cross = 0;       // nr of crossings (order v-w)
        int revCross = 0;    // nr of crossings (order w-v)

        ArrayList<E> wVec = new ArrayList<E>(
                dir ? graph.outgoingEdgesOf(w) : graph.incomingEdgesOf(w));

        for (V vnode : dir ? GAlg.outNeighboursOf(graph, v) :
                             GAlg.inNeighboursOf(graph, v)) {
            int pos1 = gdiAnn.getNode(vnode).order;
            for (E e : wVec) {
                V wnode = dir? graph.getEdgeTarget(e) : graph.getEdgeSource(e);
                int pos2 = gdiAnn.getNode(wnode).order;
                if (pos1 > pos2)
                    cross++;
                else if (pos1 < pos2)  
                    revCross++;
            }
        }
        
        return revCross-cross;
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < ranks.size(); i++) {
            s.append(""+ i +": ");
            s.append(ranks.get(i));
        }
        return s.toString();
    }
}

/* EOF */
