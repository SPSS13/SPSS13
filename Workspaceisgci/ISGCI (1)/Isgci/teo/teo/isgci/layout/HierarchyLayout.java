/*
 * Layout a Digraph hierachically.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/layout/HierarchyLayout.java,v 2.1 2011/09/29 08:52:51 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;

import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Annotation;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.TreeDFSWalker;
import teo.isgci.grapht.WalkerInfo;
import teo.isgci.util.IntFunction;

/**
 * Calculates a hierarchy layout for a DAG. The graph should be transitively
 * reduced. If the graph is not acyclic or not transitively reduced, behaviour
 * of this class is undefined.
 */
public class HierarchyLayout<V,E> {
    /** The graph we're laying out */
    protected SimpleDirectedGraph<V,E> graph;
    /** Contains the index of the GraphDrawInfo-Object */
    Annotation<V,E,GraphDrawInfo<V,E> > gdiAnn;
    /** The ranks (layers) of vertices */
    protected Ranks<V,E> ranks;
    /** Used to generate virtual and temporary nodes */
    protected VertexFactory<V> vertexFactory;
    /** Used to calculate the width of a vertex */
    protected IntFunction<V> widthFunc;
    

    /**
     * Create a new layout for g.
     * @param g the graph that will be layed out by calling layoutGraph()
     * @param vertexFactory is used for generating virtual nodes, the vertices
     * it generates should all be not equal().
     * @param widthFunc should return the width for a vertex in g. It is called
     * at the beginning of the layout algorithm. If widthFunc is null, a
     * default width is used.
     */
    public HierarchyLayout(
            SimpleDirectedGraph<V,E> g,
            VertexFactory<V> vertexFactory,
            IntFunction<V> widthFunc) {
        graph = g;
        this.vertexFactory = vertexFactory;
        this.widthFunc = widthFunc;
        gdiAnn = null;
        ranks = null;
    }

    public SimpleDirectedGraph<V,E> getGraph() {
        return graph;
    }

    public GraphDrawInfo<V,E> getGDI(V node) {
        return gdiAnn.getNode(node);
    }

    /**
     * Calculate the layout.
     */
    public void layoutGraph(){
        long t1,t2;
        
        //System.out.println("Nodes: "+ graph.vertexSet().size());
        //System.out.println("Edges: "+ graph.edgeSet().size());
        
        t1=System.currentTimeMillis();
        init();
        feasibleTree();
        t2=System.currentTimeMillis();
        //System.out.println("init: "+(t2-t1)+" ms");

        t1=System.currentTimeMillis();
        rank(0);
        t2=System.currentTimeMillis();
        //System.out.println("rank: "+(t2-t1)+" ms");
        
        t1=System.currentTimeMillis();
        vertexOrdering();
        t2=System.currentTimeMillis();
        //System.out.println("vertex-order: "+(t2-t1)+" ms");

        t1=System.currentTimeMillis();
        xCoords();
        yCoords();
        t2=System.currentTimeMillis();
        //System.out.println("coords: "+(t2-t1)+" ms");
    }
    

    /**
     * Initializes the GraphDrawInfo-Objects.
     */
    protected void init(){
        gdiAnn = new Annotation<V,E,GraphDrawInfo<V,E> >(graph);
        
        if (widthFunc == null)
            for (V v : graph.vertexSet())
                gdiAnn.setNode(v, new GraphDrawInfo());
        else
            for (V v : graph.vertexSet())
                gdiAnn.setNode(v, new GraphDrawInfo(widthFunc.execute(v)));

        for (E e : graph.edgeSet())
            gdiAnn.setEdge(e, new GraphDrawInfo());
    }
    

    /*
     * ==============================================================
     * optimal rank assignment (network simplex)
     * ==============================================================
     * structure of function calls:
     * feasibleTree()
     *     initRank()
     *     tightTree()
     *         findDeltaRank()
     *     initCutValues()
     *         getCutValue()
     * rank()
     *     enterEdge()
     *     exchange()
     *         getCutValue() (see initCutValues)
     *     normalize()
     *     balance()
     */

    /**
     * Main procedure of the network simplex algorithm.
     * This is the first pass of laying out the graph for drawing.
     * It's purpose is to assigns each node of the graph to an integer rank.
     * 
     * param pass determines whether this is called for the ranking process
     *            (pass=0) or to calculate the x-coords (pass=1)
     */
    //long enteredged, exchanged, updated;
    protected void rank(int pass){
        E e,f;
        
        long t1, t2;
        int iter = 0;

        //enteredged = exchanged = updated = 0;

        t1 = System.currentTimeMillis();
        // create a vector with all tree-edges with a negative cutvalue
        ArrayList<E> negCut = new ArrayList<E>();
        ArrayList<E> nonTree = new ArrayList<E>();
        GraphDrawInfo<V,E> gdi;
        for (E edge : graph.edgeSet()) {
            gdi = gdiAnn.getEdge(edge);
            if (gdi.tree) {
                if (gdi.cutValue<0)
                    negCut.add(edge);
            } else {
                nonTree.add(edge);
            }
        }
        
        
        t2 = System.currentTimeMillis();
        //System.out.println("init: "+(t2-t1));
        
        t1 = System.currentTimeMillis();
        
        
        int pos=-1,num,min,mpos=0,size;
        // mpos must be initialzed
        // pos=-1 causes pos=0 in 1st loop
        while((size=negCut.size())!=0){
            //System.out.println(size);
            // find edge with negative cutvalue
            min=0; // all edges in negCut have negative cutvalues (<0)
            for(num=Math.min(20,size);num>=0;num--){
                pos=(pos+1)%size;
                e=negCut.get(pos);
                gdi = gdiAnn.getEdge(e);
                if(gdi.cutValue<min){
                    mpos=pos;
                    min=gdi.cutValue;
                }
            }
            e=negCut.remove(mpos);
            //System.err.println(nonTree);
            f=enterEdge(e,nonTree);       // find non-tree edge to replace e
            exchange(e,f,negCut,nonTree); // + updating tree, vectors, ...
            iter++;
        }

        t2 = System.currentTimeMillis();
        //System.out.println("iters: "+iter+" time: "+(t2-t1));
        /*System.out.println("enterEdge: "+enteredged+" exchange: "+exchanged+
                " treeUpdate: "+updated);
        */
        
        normalize();              // set least rank to zero
        balance();

        /*{    // find maximum rank
            //GraphDrawInfo gdi;
            int max = 0;
            Enumeration eenum = graph.getNodes();
            Node node;
            while(eenum.hasMoreElements()){
                node=(Node)eenum.nextElement();
                gdi = gdiAnn.get(node);
                if(gdi.rank>max) max=gdi.rank;
            }
            System.out.println("ranks: "+max);
        }*/
    }
    
    /**
     * Constructs an initial feasible tree.
     * A tree is feasible if it induces a feasible ranking.
     * A feasible ranking is one satisfying the length constraints
     * e.len>=e.minlen for all edges e (e.len = e.to.rank - e.from.rank).
     */
    protected void feasibleTree(){
        initRank();      // initial feasible ranking
        tightTree();     // spanning tree (only tight edges)
        new LLWalker(graph, graph.vertexSet().iterator().next(),
                gdiAnn, GraphWalker.InitCode.NONE, 0).run();
        initCutValues(); // nomen est omen
    }
    
    
    /**
     * Computes an initial feasible ranking.
     */
    protected void initRank(){
        /* What is done here:
         * To find the rank of a node, compute the ranks of its in-nodes,
         * add the minimum length of the corresponding edge and take
         * the maximum of these values.
         */
        V node2;
        int max,len,rank;

        for (V node : GAlg.topologicalOrder(graph)) {
            max=0;  // max(edge.minlen+edge.from.rank)
            for (E edge : graph.incomingEdgesOf(node)) {
                // minlen of incoming edge
                len = gdiAnn.getEdge(edge).minlen;
                
                node2 = graph.getEdgeSource(edge);
                // rank of predecessor-node
                rank = gdiAnn.getNode(node2).rank;
                if(rank+len>max)
                    max=rank+len;
            }
            // assign node to rank
            gdiAnn.getNode(node).rank = max;
        }
    }
   

    /**
     * Finds maximal tree of tight edges (Spanning tree)
     */
    protected void tightTree(){
        V v = graph.vertexSet().iterator().next();
        V v2;
        final int size = graph.vertexSet().size();
        int delta;
        
        TightTreeWalker ttw = new TightTreeWalker(graph, v, gdiAnn,
                GraphWalker.InitCode.RESET);
        ttw.run();
        
        while(ttw.getCount() != size){
            /* 1. find non-edge incident to a tree node with minimal slack
             * 2. adjust ranks of tree nodes
             */
            
            delta=findDeltaRank();
            new TreeReranker(graph, v, gdiAnn, GraphWalker.InitCode.NONE,
                    delta).run();
            
            // set edge.tree to false -> reset tree to be rebuild
            for (E edge : graph.edgeSet()) {
                gdiAnn.getEdge(edge).tree = false;
                // node.tree needs not to be reset because all nodes
                // that were reached last time must be reached this time, too
                // but it is not sure which edges will be used
            }
            ttw=new TightTreeWalker(graph,v,gdiAnn,GraphWalker.InitCode.RESET);
            ttw.run();
        }
    }
    

    /**
     * Finds a non-tree-edge incident on the tree with a minimal amount
     * of slack.
     * This slack is the value, the ranks of the treenodes have to be
     * increased or decreased (depending on the direction of the edge), to
     * make this edge tight.
     */
    protected int findDeltaRank(){
        E min = null;
        int sl, slack = Integer.MAX_VALUE;
        GraphDrawInfo<V,E> gdi1, gdi2, gdi3;
        
        for (E edge : graph.edgeSet()) {
            // we can break this loop if we find an edge with slack==1
            // because all edges with slack==0 should already be tree-edges
            // and there can't be edges with negative slack
            if (slack == 1)
                break;
            if (slack < 0)
                throw new RuntimeException("Negative slack");

            gdi1 = gdiAnn.getNode(graph.getEdgeSource(edge));
            gdi2 = gdiAnn.getNode(graph.getEdgeTarget(edge));
            gdi3 = gdiAnn.getEdge(edge);
            if (gdi1.tree != gdi2.tree) {
                sl = gdi2.rank - gdi1.rank - gdi3.minlen;
                /*if(sl==0){
                    System.out.print(gdi2.rank+" ");
                    System.out.print(gdi1.rank+" ");
                    System.out.println(gdi3.minlen);
                }*/
                if(sl<slack){
                    min=edge;
                    slack=sl;
                }
            }
        }
        // edge with minimal slack is min, slack is delta
        // now determine whether the ranks have to be in- or decreased:
        // if (from in tree) increase;
        // else (if (to in tree)) decrease;
        
        gdi1=gdiAnn.getNode(graph.getEdgeSource(min));
        return gdi1.tree ? slack : -slack;
    }
    

    /**
     * Calculates the cut values of the tree edges
     */
    protected void initCutValues(){
        /* how:
         * find a node with exact one tree-edge without cutvalue
         * get cutvalue for this edge
         * loop until all tree-edges have a valid cutvalue
         * 
         * meaning of variables:
         * node.mark: number of tree-edges without cutvalue (cut==INT_MIN)
         * next: vector with nodes where node.mark==1
         * todo: vector with nodes where node.mark>1
         */
        ArrayList<V> next = new ArrayList<V>();
        ArrayList<V> todo = new ArrayList<V>();
        GraphDrawInfo<V,E> gdi,gdi2;
        
        //System.err.println("---- Start initCutValues: ----");
        //printDebug();
        //System.err.println("---- end start initCutValues ----");
        
        // init next and todo
        for (V v : graph.vertexSet()) {
            gdi=gdiAnn.getNode(v);
            gdi.mark=0;

            for (E e : graph.incomingEdgesOf(v)) {
                gdi2=gdiAnn.getEdge(e);
                // initially all tree-edges should have
                // the non-valid cutvalue INT_MIN
                if(gdi2.tree)
                    gdi.mark++;
            }
            for (E e : graph.outgoingEdgesOf(v)) {
                gdi2=gdiAnn.getEdge(e);
                if(gdi2.tree)
                    gdi.mark++;
            }

            if(gdi.mark>1)
                todo.add(v);
            else if(gdi.mark==1)
                next.add(v);
            // mark == 0 can only happen for isolated vertices
        }
        
        // start loop to calc cutvalues
        while (!next.isEmpty()) {
            V v = next.remove(next.size()-1);
            E e = getCutValue(v); // calc new cutvalue (and get edge)
            
            // get reached node (and its gdi)
            V v2 = Graphs.getOppositeVertex(graph, e, v);
            gdi2=gdiAnn.getNode(v2);
            
            gdi2.mark--;  // reduce number of uncomputed edges
            if(gdi2.mark==1){  // one edge without cutvalue left
                todo.remove(v2);
                next.add(v2);
            }else if(gdi2.mark==0){
                // maybe the node is downto zero edges
                // without processing the node itself -> remove it from next
                next.remove(v2);
                // does that happen only once per component ?
            }
        }
    }


    /**
     * Given a node with exactly one tree-edge without cutvalue, this method
     * returns this edge with its valid cutvalue.
     */
    protected E getCutValue(V v){
        // at first this code was part of initCutValues()
        // it was extracted to a separate method
        // to re-use it with exchange(e,f)
        
        /* 1. E=tree-edge without cutvalue
         *    cut=E.weight
         * (following steps take only edges adjacent to v)
         * 2. forall (non-tree-edges e)
         *        same direction as E: cut+=e.weight
         *        opposite direction : cut-=e.weight
         * 3. forall (tree-edges e with known cutvalue)
         *        same direction as E: cut-=(e.cut-e.weight)
         *        opposite direction : cut+=(e.cut-e.weight)
         * 4. store cut to E.cut
         */
        int ntOut,    // weight of non-tree-edges, starting at v
            ntIn,     //           -- "" --      , ending at v
            treeIn,   // (cut-weight) of outgoing treeedges
            treeOut;  // (cut-weight) of incoming edges
        E newEdge; // edge without cutvalue
        GraphDrawInfo<V,E> gdi,gdi2;
        
        ntIn = ntOut = treeIn = treeOut = 0;
        newEdge = null;
        gdi = null;
        
        // incoming edges
        for (E e : graph.incomingEdgesOf(v)) {
            gdi2=gdiAnn.getEdge(e);
            if(gdi2.tree){
                if (gdi2.cutValue==Integer.MIN_VALUE) {
                    newEdge=e;
                    gdi=gdi2;
                } else
                    treeIn+=(gdi2.cutValue-gdi2.weight);
            } else
                ntIn+=gdi2.weight;
        }
        // outgoing edges
        for (E e : graph.outgoingEdgesOf(v)) {
            gdi2=gdiAnn.getEdge(e);
            if(gdi2.tree){
                if(gdi2.cutValue==Integer.MIN_VALUE){
                    newEdge=e;
                    gdi=gdi2;
                }else
                    treeOut+=(gdi2.cutValue-gdi2.weight);
            }else
                ntOut+=gdi2.weight;
        }
        
        // store cutvalue
        int extern=ntOut-ntIn+treeIn-treeOut;
        gdi.cutValue = gdi.weight +
                (v == graph.getEdgeSource(newEdge) ? extern : -extern);
        
        return newEdge;
    }
   

    /**
     * Returns a non-tree edge to replace <tt>edge</tt>.
     * All edges going from the head component to the tail component of
     * <tt>edge</tt> are considered, with an edge of minimum slack
     * being chosen to maintain feasibility.
     */
    protected E enterEdge(E edge, ArrayList<E> nonTree){
        GraphDrawInfo<V,E> gdi,gdiFrom,gdiTo;
        boolean ftail,ttail,rev;
        int low,lim; // low,lim of tail of edge, low=discover, lim=finish
        int sl, minSlack = Integer.MAX_VALUE;
        E result = null;
        int cost = Integer.MAX_VALUE;

        //long t = System.currentTimeMillis();

        // head-component means the part of the tree
        // that includes the root-node
        gdiFrom=gdiAnn.getNode(graph.getEdgeSource(edge));
        gdiTo=gdiAnn.getNode(graph.getEdgeTarget(edge));
        if(gdiFrom.lim < gdiTo.lim){
            low=gdiFrom.low;
            lim=gdiFrom.lim;
            rev=false;         // head of edge is really in head-component
        }else{
            low=gdiTo.low;
            lim=gdiTo.lim;
            rev=true;        // head of edge is in tail-component
        }

        //System.err.println("entered: low="+ low +" lim="+ lim +" "+ edge);
        
        // search non-tree-edge between head- and tail-component of edge
        // that is headed in the opposite direction
        // (if edge is head->tail then search for tail->head)
        // 
        // a node v is in tail if and only if: low <= v.finish <= lim
        // (low and lim are from the node of edge that lies in head)
        // select an edge with minimal slack -> break if minSlack==0
        // because it's the minimal possible value
        for (E e : nonTree) {
            if (minSlack == 0)
                break;
            
            //if(gdi.tree) continue; // not necessary since we use nonTree
            
            // e is a non-tree edge
            // now get info for nodes
            gdiFrom=gdiAnn.getNode(graph.getEdgeSource(e));
            gdiTo=gdiAnn.getNode(graph.getEdgeTarget(e));
            // is from/to in tail component of edge
            ftail=(low<=gdiFrom.lim && gdiFrom.lim<=lim);
            ttail=(low<=gdiTo.lim && gdiTo.lim<=lim);

            //System.err.print("enter? "+ gdiFrom.lim +"->"+ gdiTo.lim);
            //System.err.println(" "+ rev +" "+ ftail +" "+ttail +" "+ e);

            if((!rev && !ftail &&  ttail) ||
               ( rev &&  ftail && !ttail)){ // e is headed from head to tail
                gdi=gdiAnn.getEdge(e);
                sl=gdiTo.rank-gdiFrom.rank-gdi.minlen;
                if(sl<minSlack){
                    result=e;    // save edge
                    minSlack=sl; // minimal slack
                }/* else if (sl==minSlack) {
                    int nc = gdiFrom.lim > gdiTo.lim ? gdiFrom.lim
                                                     : gdiTo.lim;
                    nc -= gdiFrom.low < gdiTo.low ? gdiFrom.low : gdiTo.low;
                    if (sl < minSlack || nc < cost) {
                        result = e;
                        //minSlack = sl;
                        cost = nc;
                    }
                }*/
            }
        }
        //enteredged += System.currentTimeMillis()-t;
        return result;
    }
   

    /**
     * Returns true if from-component has more nodes.
     * <tt>edge</tt> is a tree-edge.
     * If it is removed the tree is split into two parts - a head and
     * a tail component. Assuming that the low/lim values of the nodes
     * are set properly this method finds out which part of the tree
     * contains more nodes.
     */
    protected boolean cmp(E edge){
        GraphDrawInfo<V,E> gdiFrom,gdiTo;
        gdiFrom = gdiAnn.getNode(graph.getEdgeSource(edge));
        gdiTo = gdiAnn.getNode(graph.getEdgeTarget(edge));
        int n, half = graph.vertexSet().size() >> 1;

        if(gdiTo.low<gdiFrom.low){
            n=(gdiFrom.lim-gdiFrom.low+1)>>1;
            return (n>half);
        }else{
            n=(gdiTo.lim-gdiTo.low+1)>>1;
            return (n<half);
        }
    }
    

    /**
     * <tt>e2</tt> becomes a tree-edge instead of <tt>e1</tt>.
     * Then the tree and its cutvalues are updated, as well as the vector
     * negCut which contains all tree-edges with negative cutvalue.
     */
    protected void exchange(E e1, E e2,
                            ArrayList<E> negCut, ArrayList<E> nonTree){
        //long t = System.currentTimeMillis();
        GraphDrawInfo<V,E> gdi1,gdi2,gdiFrom,gdiTo;
        //System.err.println("exch tree: "+ e1 +" for "+ e2);
        
        // some gdis we'll need
        gdi1=gdiAnn.getEdge(e1);
        gdi2=gdiAnn.getEdge(e2);
        gdiFrom=gdiAnn.getNode(graph.getEdgeSource(e2));
        gdiTo=gdiAnn.getNode(graph.getEdgeTarget(e2));
        
        nonTree.remove(e2);
        nonTree.add(e1);
        
        gdi1.tree=false; // remove e1 from tree (split tree)
        
        // adjust ranks of the component with root=e1.from
        // (make e2 tight)
        int slack=gdiTo.rank-gdiFrom.rank-gdi2.minlen; // slack of e2
        if(slack!=0){              // if slack==0 -> nothing to do
            
            V v;
            // find out which part has fewer nodes
            if(cmp(e1)){
                v = graph.getEdgeTarget(e1);
                slack = -slack;
            }else{
                v = graph.getEdgeSource(e1);
            }
            
            new TreeReranker(graph, v, gdiAnn, GraphWalker.InitCode.NONE,
                    -slack).run();
        }
        
        gdi2.cutValue = -gdi1.cutValue; // by definition
        gdi2.tree=true;                 // add e2 to tree
        treeUpdate(e2,negCut);
        //exchanged += System.currentTimeMillis()-t;
    }


    /**
     * Updates critical values of the tree:
     * <ol>
     * <li> cutValues of edges along the path that connects the nodes
     *      of the old edge in the new tree
     * <li> low and lim values of all nodes below the least common
     *      ancestor of the two nodes of the new edge
     * </ol>
     */
    protected void treeUpdate(E newEdge, ArrayList<E> negCut){
        //long t = System.currentTimeMillis();
        GraphDrawInfo<V,E> gdiFrom,gdiTo,gdi1,gdi2,gdi;
        int low,lim,i,cut;
        V v1,v2;
        E e;
        
        v1 = graph.getEdgeSource(newEdge);
        v2 = graph.getEdgeTarget(newEdge);
        gdiFrom = gdiAnn.getNode(v1);
        gdiTo = gdiAnn.getNode(v2);
        cut = gdiAnn.getEdge(newEdge).cutValue;
        
        // find least common ancestor of e2.from and e2.to
        // that is a node which was discovered before and finished
        // after from and to (lower low and greater lim)
        low=Math.min(gdiFrom.low,gdiTo.low);
        lim=Math.max(gdiFrom.lim,gdiTo.lim);
        // start search
        // and update cutValues
        
        gdi1=gdiFrom;
        while(gdi1.low>low || gdi1.lim<lim){
            //System.err.println("tU "+ v1 +" "+ gdi1.treeParent);
            e=gdi1.treeParent;
            gdi=gdiAnn.getEdge(e);
            if(gdi.tree){ // no update for old edge (now nonTree)
                // remove edge in the first place,
                // position in negCut changes anyway
                i=gdi.cutValue;
                //if(gdi.cutValue<0) negCut.removeElement(e);
                gdi.cutValue += (v1 == graph.getEdgeSource(e) ? -cut : cut);
                if(i<0 && gdi.cutValue>=0)
                    negCut.remove(e);
                if(i>=0 && gdi.cutValue<0)
                    negCut.add(e);
            }
            v1 = Graphs.getOppositeVertex(graph, e, v1);
            gdi1=gdiAnn.getNode(v1);
        }
        // and the same for the other node of e2
        gdi2=gdiTo;
        while(gdi2.low>low || gdi2.lim<lim){
            e=gdi2.treeParent;
            gdi=gdiAnn.getEdge(e);
            if(gdi.tree){ // no update for old edge
                i=gdi.cutValue;
                //if(gdi.cutValue<0) negCut.remove(negCut.indexOf(e));
                gdi.cutValue += (v2 == graph.getEdgeTarget(e) ? -cut : cut);
                if(i<0 && gdi.cutValue>=0)
                    negCut.remove(e);
                if(i>=0 && gdi.cutValue<0)
                    negCut.add(e); // (re)insert
            }
            v2 = Graphs.getOppositeVertex(graph, e, v2);
            gdi2=gdiAnn.getNode(v2);
        }
        // Am I paranoid?
        if(v1!=v2)
            System.err.println("error in treeUpdate");
        
        // update low/lim values below v/v2 (least common ancestor)

        i=gdi1.low-1; // offset for low/lim values below v
        // the TreeDFSWalker sets the discover/finish-times starting at 1
        // (v.discover will be 1)
        // to get the correct low-value: v.low=v.discover+i;
        
        // split tree so that we only update nodes below v1
        e=gdi1.treeParent;
        if(e!=null){
            gdi=gdiAnn.getEdge(e);
            gdi.tree=false;
        } else
            gdi=null;

        new LLWalker(graph,v1,gdiAnn,GraphWalker.InitCode.NONE, i).run();
        gdi1.treeParent=e;  // reset correct parent of v1
        
        // reinsert edge to complete tree
        if(gdi!=null)
            gdi.tree=true;
        
        //updated += System.currentTimeMillis()-t;
    }
   

    /**
     * Normalizes the ranking by setting the least rank to zero.
     */
    protected void normalize(){
        GraphDrawInfo<V,E> gdi;
        
        int min=Integer.MAX_VALUE;
        // find minimum rank
        for (V node : graph.vertexSet()) {
            gdi=gdiAnn.getNode(node);
            if(gdi.rank<min) min=gdi.rank;
        }
        
        if(min==0) return; // nothing to do
        
        // adjust ranks so that minimum is zero
        for (V node : graph.vertexSet()) {
            gdi=gdiAnn.getNode(node);
            gdi.rank-=min;
        }
    }
  

    /**
     * Nodes having equal in- and out-edge weights and multiple feasible
     * ranks are moved to a fesible rank with the fewest nodes.
     */
    protected void balance(){
        GraphDrawInfo<V,E> gdi,gdi2,gdi3;
        int max=Integer.MIN_VALUE;  // min=0 since ranks were normalized
        int rankSize[];             // number of nodes in each rank
        
        // find maximum rank
        for (V node : graph.vertexSet()) {
            gdi=gdiAnn.getNode(node);
            if(gdi.rank>max) max=gdi.rank;
        }
        
        rankSize=new int[max+1];
        Arrays.fill(rankSize, 0);
        
        // count nodes per rank
        for (V node : graph.vertexSet()) {
            gdi=gdiAnn.getNode(node);
            rankSize[gdi.rank]++;
        }
        
        // balance nodes
        int lo,hi,    // min, max feasible rank
            minSlack, // minimum slack
            slack,
            weightin,weightout; // weights of in-/out-edges
        for (V node : graph.vertexSet()) {
            gdi=gdiAnn.getNode(node);
            
            weightin=weightout=0;
            minSlack=Integer.MAX_VALUE;
            // find lowest feasible rank
            for (E edge : graph.incomingEdgesOf(node)) {
                gdi2=gdiAnn.getNode(graph.getEdgeSource(edge));
                gdi3=gdiAnn.getEdge(edge);
                slack=gdi.rank-gdi2.rank-gdi3.minlen;
                if(slack<minSlack)
                    minSlack=slack;
                weightin+=gdi3.weight;
            }
            lo=(minSlack<Integer.MAX_VALUE) ? gdi.rank-minSlack : gdi.rank;
            
            minSlack=Integer.MAX_VALUE;
            // find highest feasible rank
            for (E edge : graph.outgoingEdgesOf(node)) {
                gdi2=gdiAnn.getNode(graph.getEdgeTarget(edge));
                gdi3=gdiAnn.getEdge(edge);
                slack=gdi2.rank-gdi.rank-gdi3.minlen;
                if(slack<minSlack)
                    minSlack=slack;
                weightout+=gdi3.weight;
            }
            hi=(minSlack<Integer.MAX_VALUE) ? gdi.rank+minSlack : gdi.rank;
            
            // move node to rank with minimum number of nodes
            int minRank;
            if(weightin==weightout && lo<hi){
                minRank=lo;  // minRank=rank with minimum number of nodes
                for(int i=lo+1;i<=hi;i++)
                    if(rankSize[i]<rankSize[minRank]) minRank=i;
                // move node to new rank
                rankSize[gdi.rank]--;
                rankSize[minRank]++;
                gdi.rank=minRank;
            }
        }
    }
    
    /* ==============================================================
     * vertex ordering within ranks
     * ==============================================================
     */
    
    protected void vertexOrdering(){
        ranks=new Ranks<V,E>(this);
        ranks.minimizeCrossings();
    }
    
    /**
     * Converts the graph into one whose edges connect only nodes
     * on adjacent ranks.
     * Edges between nodes more than one rank apart are replaced by
     * chains of edges between virtual nodes. These nodes are placed
     * on the intermediate ranks.
     */
    void createVirtualNodes(){
        ArrayList<E> todo = new ArrayList<E>();
        GraphDrawInfo<V,E> gdi,edgeGdi;
        int i,j,rankFrom,rankTo;
        E newEdge;
        
        for (E edge : graph.edgeSet()) {
            rankFrom = gdiAnn.getNode(graph.getEdgeSource(edge)).rank;
            rankTo = gdiAnn.getNode(graph.getEdgeTarget(edge)).rank;
            if(rankTo-rankFrom >= 2)
                todo.add(edge);
        }
        /* It is necessary to collect edges first. If they are replaced
         * immediately the enumeration would produce strange results.
         */
        
        for (E edge : todo) {
            rankFrom = gdiAnn.getNode(graph.getEdgeSource(edge)).rank;
            rankTo = gdiAnn.getNode(graph.getEdgeTarget(edge)).rank;
            edgeGdi = gdiAnn.getEdge(edge);

            V node1 = graph.getEdgeSource(edge);
            graph.removeEdge(edge);

            for(j=rankFrom+1;j<rankTo;j++){
                V node2 = vertexFactory.createVertex();
                graph.addVertex(node2);
                
                gdi=new GraphDrawInfo<V,E>();
                gdi.virt=true;
                gdi.rank=j;
                gdiAnn.setNode(node2,gdi);
                
                newEdge = graph.addEdge(node1,node2);
                gdiAnn.setEdge(newEdge,edgeGdi);
                
                node1=node2;
            }
            newEdge = graph.addEdge(node1, graph.getEdgeTarget(edge));
            gdiAnn.setEdge(newEdge, edgeGdi);
        }
    }
    
    /* ==============================================================
     * node coordinates
     * ==============================================================
     */
    
    /* structure:
     * xCoord()
     *     buildGraph()
     *         getOmega()
     *     restoreGraph()
     * yCoord()
     */
    
    int nodeSep=50;
    int rankSep=100;
    
    /**
     * Calculates the x-coordinates for each node of the graph.
     * To do this, the graph is converted into an auxiliary graph.
     * This graph is ranked with the network-simplex algorthm.
     * The ranks of the nodes of this graph are the x-coordinates
     * of the original graph.
     */
    protected void xCoords(){
        HierarchyLayout<V,DefaultEdge> xlayout = buildGraph();
        xlayout.feasibleTree2(this.ranks);
        xlayout.rank(1);
        //System.err.println("----xCoord result: ----");
        //xlayout.printDebug();
        //System.err.println("----end xCoord result ----");
        restoreGraph(xlayout);
    }
    
    /**
     * Builds the auxiliary graph.<br>
     * Every edge e=(u,v) of the original graph is replaced by a new
     * node (n) and two edges e1=(n,u) and e2=(n,v). These edges have a
     * minlen of 0 and a weight of e.weight*e.Omega.<br>
     * <br>
     * Another class of edges is added to separate nodes on the same rank.
     * If v is the left neighbor of, the the auxiliary graph gets an edge
     * f=(v,w) with a weight of 0 and minlen that results from the width
     * of both nodes and the node-separation value.
     */
    protected HierarchyLayout<V,DefaultEdge> buildGraph() {
        SimpleDirectedGraph<V,DefaultEdge> g =
                new SimpleDirectedGraph<V,DefaultEdge>(DefaultEdge.class);
        Annotation<V,DefaultEdge,GraphDrawInfo<V,DefaultEdge> > xgdiAnn = 
                new Annotation<V,DefaultEdge,GraphDrawInfo<V,DefaultEdge> >(g);

        GraphDrawInfo<V,E> gdi, gdi1;
        V node, node1;
        DefaultEdge e1, e2;
        GraphDrawInfo<V,DefaultEdge> gdiNew1, gdiNew2;

        //---- Copy the original nodes
        for (V v : graph.vertexSet()) {
            g.addVertex(v);
            gdiNew1 = new GraphDrawInfo<V,DefaultEdge>();
            gdiNew1.width = gdiAnn.getNode(v).width;
            xgdiAnn.setNode(v, gdiNew1);
        }
        
        //---- first class: replace orig edges by a P_3
        for (E edge : graph.edgeSet()) {
            gdi=gdiAnn.getEdge(edge);

            // create node and edges
            node = vertexFactory.createVertex();
            g.addVertex(node);
            e1 = g.addEdge(node, graph.getEdgeSource(edge));
            e2 = g.addEdge(node, graph.getEdgeTarget(edge));

            // add GraphDrawInfo
            gdiNew1 = new GraphDrawInfo<V,DefaultEdge>();
            xgdiAnn.setNode(node, gdiNew1);
            gdi1 = gdiAnn.getNode(graph.getEdgeSource(edge));
            gdiNew1.rank = gdi1.rank; // store the rank where node lies
            gdiNew1.aux = true;
            
            gdiNew1 = new GraphDrawInfo<V,DefaultEdge>();
            gdiNew2 = new GraphDrawInfo<V,DefaultEdge>();
            gdiNew1.weight = gdiNew2.weight = gdi.weight * getOmega(edge);
            gdiNew1.minlen = gdiNew2.minlen = 0;
            xgdiAnn.setEdge(e1, gdiNew1);
            xgdiAnn.setEdge(e2, gdiNew2);
        }
        
        //---- second class: connect neighbors
        ArrayList<V> vec;
        for (int i = ranks.cntRanks()-1; i>=0; i--) {
            vec = ranks.getRank(i);
            for (int j = vec.size()-1; j>0; j--) {
                node = vec.get(j-1);
                node1 = vec.get(j);
                gdi = gdiAnn.getNode(node);
                gdi1 = gdiAnn.getNode(node1);

                e1 = g.addEdge(node, node1);
                gdiNew1 = new GraphDrawInfo<V,DefaultEdge>();
                gdiNew1.weight = 0;
                gdiNew1.minlen = ((gdi.width+gdi1.width)>>1)+nodeSep;
                xgdiAnn.setEdge(e1, gdiNew1);
            }
        }

        //---- Create layouter
        HierarchyLayout<V,DefaultEdge> xlayout =
                new HierarchyLayout<V,DefaultEdge>(g, vertexFactory, null);
        xlayout.gdiAnn = xgdiAnn;
        
        //System.err.println("----buildGraph result: ----");
        //xlayout.printDebug();
        //System.err.println("----end buildGraph result ----");
        return xlayout;
    }
   

    /**
     * Omega is a internal value to favor straightening long edges. Therefore
     * edges are divided into three types depending on their end vertices:
     * <ol>
     * <li>both real nodes
     * <li>one real and one virtual node
     * <li>both virtual nodes
     * </ol>
     * where omega(1) <= omega(2) <= omega(3). (We use 1,2,8).<br>
     */
    protected int getOmega(E edge){
        GraphDrawInfo<V,E> gdi1,gdi2;
        gdi1=gdiAnn.getNode(graph.getEdgeSource(edge));
        gdi2=gdiAnn.getNode(graph.getEdgeTarget(edge));
        
        if(gdi1.virt && gdi2.virt) return 8;
        if(!gdi1.virt && !gdi2.virt) return 1;
        return 2;
    }
    
    /**
     * Restores the original graph.
     */
    protected void restoreGraph(HierarchyLayout<V,DefaultEdge> xlayout) {
        int i,j;
        GraphDrawInfo<V,E> gdi;
        GraphDrawInfo<V,DefaultEdge> gdix;
        
        // restore nodes and their values
        ArrayList<V> vec;
        for (i = ranks.cntRanks()-1; i>=0; i--) {
            vec = ranks.getRank(i);
            for (V node : vec) {
                gdi = gdiAnn.getNode(node);
                gdix = xlayout.gdiAnn.getNode(node);
                gdi.xCoord = gdix.rank;
                gdi.rank = i;
            }
        }
    }
   

    protected void yCoords(){
        GraphDrawInfo<V,E> gdi;
        for (V v : graph.vertexSet()) {
            gdi=gdiAnn.getNode(v);
            gdi.yCoord=(gdi.rank+1)*rankSep;
        }
    }
    
    /**
     * Constructs an initial feasible tree.
     * This method replaces the original feasibleTree() when the
     * ranking-algorithmus for the auxiliary graph is invoked. It uses
     * a more efficient way to construct the initial feasible tree by taking
     * advantage of its structure:
     * <ol>
     * <li> use all edges connecting nodes in the same rank
     * <li> for each pair of adjacent ranks, pick an edge f=(u,v) between
     * the ranks and add both f(u) and f(v) to the tree
     * <li> for every edge e=(w,x)!=f between two ranks, add either e(w) or
     * e(x) to the tree depending on whether w or x is placed leftmost
     * </ol>
     * @param levels the ranks to use (obtained from 1st pass, node placement)
     * @param gdiAnn1 the GraphDrawInfo annotation from the 1st pass, to obtain
     * node positions
     */
    protected void feasibleTree2(Ranks<V,?> levels) {
        boolean leveldone[] = new boolean[levels.cntRanks()];
        
        int i,j;
        ArrayList<V> vec;
        GraphDrawInfo<V,E> gdi,gdi1,gdi2;
        V v1,v2;
       
        //---- 1st pass: nodes in the same rank
        for (i = levels.cntRanks()-1; i >= 0; i--) {
            leveldone[i] = false; // needed for 2nd+3rd pass
            vec = levels.getRank(i);
            //System.err.print(""+ i +": ");
            //System.err.println(vec);

            // Leftmost vertex gets rank 0
            v2 = vec.get(0);
            gdi2 = gdiAnn.getNode(v2);
            gdi2.rank = 0;

            // Go left-right through the level, assigning ranks and creating
            // a tree path
            for (j = 1; j < vec.size(); j++) {
                v1 = vec.get(j);
                gdi1 = gdiAnn.getNode(v1);
                gdi1.rank = gdi2.rank + ((gdi1.width+gdi2.width)>>1) + nodeSep;
                gdiAnn.getEdge(graph.getEdge(v2,v1)).tree = true;
                gdi2 = gdi1;
                v2 = v1;
            }
        }
        
        // build list with auxiliary nodes
        ArrayList<V> auxNodes = new ArrayList<V>();
        for (V v : graph.vertexSet()) {
            gdi1 = gdiAnn.getNode(v);
            if (gdi1.aux) {
                //System.out.println("AUX"+ v);
                auxNodes.add(v);
            }
        }
        
        //---- 2nd pass: connect adjacent levels
        // add both edges of an auxiliary node to tree
        for (i = auxNodes.size()-1; i >= 0; i--) {
            V node = auxNodes.get(i);
            gdi = gdiAnn.getNode(node);
            if (leveldone[gdi.rank])
                continue; // ranks already connected
            
            leveldone[gdi.rank] = true;

            // node must have exactly two outgoing edges (by construction)
            Iterator<E> iter = graph.outgoingEdgesOf(node).iterator();
            E e1 = iter.next();
            E e2 = iter.next();
            v1 = graph.getEdgeTarget(e1);
            v2 = graph.getEdgeTarget(e2);
            gdi1 = gdiAnn.getNode(v1);
            gdi2 = gdiAnn.getNode(v2);
            
            // move adjacent piece of tree so that v1 and v2 are on the
            // same rank
            if (gdi1.rank != gdi2.rank) {
                int k = gdi2.rank-gdi1.rank;
                new TreeReranker(graph, v1, gdiAnn, GraphWalker.InitCode.NONE,
                        k).run();
            }
            
            // add edges to tree
            gdi.rank = gdi1.rank;
            gdiAnn.getEdge(e1).tree = true;
            gdiAnn.getEdge(e2).tree = true;
            
            auxNodes.remove(i);
        }
        
        //---- 3rd pass: nodes in adjacent ranks, only one edge
        for (i = auxNodes.size()-1; i>= 0; i--) {
            V node = auxNodes.get(i);
            gdi=gdiAnn.getNode(node);
            
            // node must have exactly two outgoing edges (by construction)
            Iterator<E> iter = graph.outgoingEdgesOf(node).iterator();
            E e1 = iter.next();
            E e2 = iter.next();
            v1 = graph.getEdgeTarget(e1);
            v2 = graph.getEdgeTarget(e2);
            gdi1=gdiAnn.getNode(v1);
            gdi2=gdiAnn.getNode(v2);
            
            // ranks already connected -> use only one edge
            if(gdi1.rank<gdi2.rank){
                gdi.rank=gdi1.rank;
                gdiAnn.getEdge(e1).tree=true;
            }else{
                gdi.rank=gdi2.rank;
                gdiAnn.getEdge(e2).tree=true;
            }   
        }
        
        //---- Recalculate the low/lim values
        new LLWalker(graph, levels.getRank(0).get(0), gdiAnn,
                GraphWalker.InitCode.NONE, 0).run();

        //System.err.println("----- after feasibleTree2 -----");
        //printDebug();
        //System.err.println("----- end after feasibleTree2 -----");

        initCutValues(); // nomen est omen
    }


    protected void printDebugNodes() {
        for (V v : graph.vertexSet()) {
            System.err.print(v);
            System.err.println(gdiAnn.getNode(v));
        }
    }


    protected void printDebug() {
        System.err.println(graph);
        printDebugNodes();

        for (E e : graph.edgeSet()) {
            System.err.println(e);
            System.err.println(gdiAnn.getEdge(e));
        }
    }

    public static void main(String[] args) {
        SimpleDirectedGraph<String,DefaultEdge> g =
                new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);

        g.addVertex("q");
        g.addVertex("u");
        g.addVertex("i");
        g.addVertex("c");
        g.addVertex("k");
        g.addVertex("f");
        g.addEdge("k", "f");
        g.addEdge("c", "f");
        g.addEdge("i", "k");
        g.addEdge("u", "k");
        g.addEdge("u", "c");
        g.addEdge("q", "u");
        g.addEdge("q", "i");

        HierarchyLayout<String,DefaultEdge> layout =
                new HierarchyLayout<String,DefaultEdge>(g,
                    new VertexFactory<String>() {
                        int running = 0;
                        public String createVertex() {
                            return("virtual-"+ (++running));
                        }
                    },
                    null);
        layout.layoutGraph();
        System.err.println("---- end result: ----");
        layout.printDebug();
    }
}
