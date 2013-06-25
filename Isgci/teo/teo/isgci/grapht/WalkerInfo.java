/*
 * A WalkerInfo contains basic fields for graph operations like
 * traversing a graph with DFS or BFS.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/grapht/WalkerInfo.java,v 2.0 2011/09/25 12:41:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.grapht;

import org.jgrapht.DirectedGraph;
import teo.isgci.util.UnaryFunction;

public class WalkerInfo<V,E> {
    public GraphWalker.Status status;
    /** How this node was reached in e.g. BFS-tree */
    public E parent;
    
    /**
     * Determines whether or not a node or an edge belongs to a tree.
     * This attribute is essential for edges but optional for nodes.
     * By default a node belongs to a tree if at least one of its adjacent
     * edges is marked as a tree-edge. GraphWalkers normally use only
     * the edge.tree attribute. But there may be some special Walkers
     * (like TightTreeWalker) that use the node.tree as a redundant
     * shortcut. In any other case the validity of this value
     * is not guaranteed.<br>
     * However, if you don't plan to use the graph as a tree (e.g.
     * using TreeWalkers) you don't need to care about this field.
     */
    public boolean tree;
    public int distance; // distance to start node in BFS-tree
    public int discover; // discover-time in DFS
    public int finish;   // finish-time in DFS
    public int mark;     // free integer field for various purposes
    
    /** Initializes the fields. */
    public WalkerInfo(){
        status   = GraphWalker.Status.UNSEEN;
        parent   = null;
        tree     = false;
        distance = -1;
        discover = 0;
        finish   = 0;
        mark     = 0;
    }
    
    public void reset(){
        status=GraphWalker.Status.UNSEEN;
        parent=null;
    }
    

    /**
     * Returns a new Annotation for a graphwalker with node creator that
     * creates a new walkerinfo.
     */
    public static <VV,EE> Annotation<VV,EE,WalkerInfo<VV,EE> >
            createAnnotation(DirectedGraph<VV,EE> g){
        return
            new Annotation<VV,EE,WalkerInfo<VV,EE> >(g,
                new UnaryFunction<VV,WalkerInfo<VV,EE> >() {
                    public WalkerInfo<VV,EE> execute(VV n) {
                        return new WalkerInfo<VV,EE>();
                    }
                },
                null
            );
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("status="); s.append(status);
        s.append(", parent="); s.append(parent);
        s.append(", tree="); s.append(tree);
        s.append(", distance="); s.append(distance);
        s.append(", d="); s.append(discover);
        s.append(", f="); s.append(finish);
        s.append(", mark="); s.append(mark);

        return s.toString();
    }
}
