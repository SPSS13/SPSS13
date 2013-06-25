package teo.isgci.gui;

import java.io.Serializable;
import java.util.Set;

import teo.XsltUtil;
import teo.isgci.db.Algo;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Utility;

/**
 * This class is for storing the gc information with a toString method, printing
 * html strings
 * 
 * @author leo
 * @date 12.06. 9:30
 * @param <V>
 */
public class GraphClassSet {
    private ISGCIGraphCanvas parent;
    // all graphclasses represented by this node
    private Set<GraphClass> set;
    // the graphclass matching the label
    private GraphClass label;

    // shortcut method
    public GraphClassSet(Set<GraphClass> v, ISGCIGraphCanvas parent) {
        this(v, parent, null);
    }

    public GraphClassSet(Set<GraphClass> v, ISGCIGraphCanvas parent,
            GraphClass label) {
        this.set = v;
        this.parent = parent;
        if (label == null) {
            for (GraphClass gc : set) {
                if (gc.toString().equals(Algo.getName(set, parent.namingPref))) {
                    label = gc;
                }
            }
        }
        this.label = label;
    }

    public GraphClass getLabel() {
        return label;
    }

    public Set<GraphClass> getSet() {
        return set;
    }

    /**
     * @author leo
     * @param label
     * @annotation
     */
    public void setLabel(GraphClass label) {
        if (label == null) {
            for (GraphClass gc : set) {
                System.out.println(gc.toString());
                System.out.println(Algo.getName(set, parent.namingPref));
                if (gc.toString().equals(Algo.getName(set, parent.namingPref))) {
                    label = gc;
                }
            }
        }
        this.label = label;
    }

    @Override
    public String toString() {

        return XsltUtil.latex(label.toString());

    }
}
