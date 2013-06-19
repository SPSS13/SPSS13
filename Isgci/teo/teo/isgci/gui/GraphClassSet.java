package teo.isgci.gui;

import java.io.Serializable;
import java.util.Set;

import teo.XsltUtil;
import teo.isgci.db.Algo;
import teo.isgci.gc.GraphClass;

/**
 * This class is for storing the gc information with a toString method, printing
 * html strings
 * 
 * @author leo
 * @date 12.06. 9:30
 * @param <V>
 */
public class GraphClassSet<V>{
	private ISGCIGraphCanvas parent;
	private Set<V> set;
	private String label;
	

	// shortcut method
	public GraphClassSet(Set<V> v, ISGCIGraphCanvas parent) {
		this(v, parent, null);
	}

	public GraphClassSet(Set<V> v, ISGCIGraphCanvas parent, String label) {
		this.set = v;
		this.parent = parent;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public Set<V> getSet() {
		return set;
	}

	/**
	 * @author leo
	 * @param label
	 * @annotation
	 */
	public void setLabel(String label) {
		if (label == null) {
			this.label = null;
		} else {
			this.label = XsltUtil.latex(label);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		if (label != null) {
			return label;
		} else {
			return XsltUtil.latex(Algo.getName((Set<GraphClass>) set,
					parent.namingPref));
		}
	}
}
