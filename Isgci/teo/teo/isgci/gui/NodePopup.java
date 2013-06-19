/*
 * Popupmenu for NodeViews
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodePopup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import teo.XsltUtil;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Utility;

public class NodePopup extends JPopupMenu implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2260105093000618855L;
	ISGCIMainFrame parent;
	JMenuItem deleteItem, infoItem;
	JMenu nameItem;
	// rework
	mxCell cell;
	mxGraph graph;

	private static String CHANGENAME = "Name: ";

	public NodePopup(ISGCIMainFrame parent, mxGraph graph) {
		super();
		this.parent = parent;
		this.graph = graph;
		// deleteItem = new JMenuItem("Delete");
		add(infoItem = new JMenuItem("Information"));
		add(nameItem = new JMenu("Change name"));
		infoItem.addActionListener(this);
	}

	public void setNode(mxCell cell) {
		this.cell = cell;
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == infoItem) {
			System.out.println(searchName(cell));
			JDialog d = new GraphClassInformationDialog(parent,
					searchName(cell));
			d.setLocation(50, 50);
			d.pack();
			d.setSize(800, 600);
			d.setVisible(true);
		} else if (event.getActionCommand().startsWith(CHANGENAME)) {
			String fullname = event.getActionCommand().substring(
					CHANGENAME.length());
			graph.getModel().beginUpdate();
			try {
				@SuppressWarnings("unchecked")
				GraphClassSet<GraphClass> graphClassSet = (GraphClassSet<GraphClass>) cell
						.getValue();
				graphClassSet.setLabel(fullname);
				graph.updateCellSize(cell);
			} finally {
				graph.getModel().endUpdate();
			}
		}
	}

	public void show(Component orig, int x, int y) {
		// reworked
		LatexGraphics latex = ISGCIMainFrame.latex;
		Set<GraphClass> gcs = getAllClasses(cell);
		int i = 0;

		nameItem.removeAll();
		nameItem.setEnabled(gcs.size() != 1);
		JMenuItem[] mItem = new JMenuItem[gcs.size()];
		// FIXME sort and render latex properly
		for (GraphClass gc : gcs) {
			nameItem.add(mItem[i] = new JMenuItem((Utility
					.getShortName(XsltUtil.latex(gc.toString())))));
			mItem[i].setActionCommand(CHANGENAME + gc.toString());
			mItem[i].addActionListener(this);
			i++;
		}

		super.show(orig, x, y);
	}

	private Set<GraphClass> getAllClasses(mxCell c) {
		Set<GraphClass> result = new HashSet<GraphClass>();
		int start = 1;
		String id = c.getId();
		for (int i = 1; i < id.length(); i++) {
			if (id.charAt(i) == ',') {
				if (DataSet.getClass(id.substring(start, i)) != null) {
					result.add(DataSet.getClass(id.substring(start, i)));
					start = i + 2;
				}
			}
		}
		return result;
	}

	// Workaround... Hard to explain
	private GraphClass searchName(mxCell c) {
		String id = c.getId();
		if (XsltUtil.latex(id).contains(c.getValue().toString())) {
			int start = 1;
			for (int i = 1; i < id.length(); i++) {
				if (id.charAt(i) == ',') {
					if (XsltUtil.latex(id.substring(start, i)).equals(
							c.getValue().toString())) {
						return DataSet.getClass(id.substring(start, i));
					}
					start = i + 2;
				}
			}
			return DataSet.getClass(id.substring(1, id.length() - 1));
		}

		return null;
	}
}

/* EOF */
