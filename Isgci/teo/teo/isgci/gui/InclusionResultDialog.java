/*
 * Displays the relation between two graphclasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/InclusionResultDialog.java,v 2.4 2011/12/22 13:13:54 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import teo.isgci.gui.*;
import teo.isgci.gc.ForbiddenClass;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.GAlg;
import teo.isgci.db.*;
import teo.isgci.util.LessLatex;


public class InclusionResultDialog extends JDialog implements ActionListener {
    protected ISGCIMainFrame parent;
    protected JButton okButton;
    protected JButton drawButton;
    protected JButton refButton;
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected Container content;
    protected Collection<GraphClass> upper, lower;

    protected static String nodeName1, nodeName2;


    /** Creates the dialog
     * @param parent parent of this dialog
     */
    protected InclusionResultDialog(ISGCIMainFrame parent) {
        super(parent, "Relation", true);
        this.parent = parent;
        content = getContentPane();
        gridbag = new GridBagLayout();
        constraints = new GridBagConstraints();
        content.setLayout(gridbag);

        constraints.insets = new Insets(5, 5, 0, 5);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
    }


    /**
     * Displays the relation "no relation" between node1 and node2. All nodes
     * in inclGraph.
     * @param parent parent of this dialog
     * @param node1 first node
     * @param node2 second node
     * @param minsuper minimal common supernodes
     * @param maxsub maximal common subnodes
     */
    public InclusionResultDialog(ISGCIMainFrame parent,
            List<GraphClass> minsuper, List<GraphClass> maxsub,
            GraphClass node1, GraphClass node2) {
        this(parent);
        if (minsuper.isEmpty() && maxsub.isEmpty())
            upper = lower = null;
        else if (minsuper.isEmpty()) {
            upper = new ArrayList<GraphClass>();
            upper.add(node1);
            upper.add(node2);
            lower = maxsub;
        } else if (maxsub.isEmpty()) {
            lower = new ArrayList<GraphClass>();
            lower.add(node1);
            lower.add(node2);
            upper = minsuper;
        } else {
            upper = minsuper;
            lower = maxsub;
        }

        JLabel line1 = new JLabel("There is no inclusion relation between",
                JLabel.LEFT);
        //line1.setFont(new Font("TimesRoman",Font.BOLD,14));
        gridbag.setConstraints(line1, constraints);
        content.add(line1);

        LatexLabel line2 = new LatexLabel(parent.latex,
                node1.toString() +" and "+ node2.toString()+".");
        //line2.setFont(new Font("TimesRoman",Font.PLAIN,14));
        gridbag.setConstraints(line2, constraints);
        content.add(line2);

        JComponent rel = makeNoRelationPanel(node1, node2);
        if (rel != null) {
            gridbag.setConstraints(rel, constraints);
            content.add(rel);
        }

        line1 = new JLabel("Minimal common superclass(es):", JLabel.LEFT);
        //line1.setFont(new Font("TimesRoman",Font.BOLD,14));
        constraints.gridwidth = 1;
        gridbag.setConstraints(line1, constraints);
        content.add(line1);

        line1 = new JLabel("Maximal common subclass(es):", JLabel.LEFT);
        //line1.setFont(new Font("TimesRoman",Font.BOLD,14));
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(line1, constraints);
        content.add(line1);

        constraints.insets = new Insets(0, 10, 5, 10);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        JComponent list = makeListPanel(minsuper);
        constraints.gridwidth = 1;
        gridbag.setConstraints(list, constraints);
        content.add(list);

        list = makeListPanel(maxsub);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(list, constraints);
        content.add(list);

        constraints.insets = new Insets(0, 5, 0, 0);
        finalizeConstructor();
    }


    /**
     * Displays the relation "equivalent" between node1 and node2.
     * @param parent parent of this dialog
     * @param node1 first node
     * @param node2 second node
     */
    public InclusionResultDialog(ISGCIMainFrame parent,
            GraphClass node1, GraphClass node2) {
        this(parent);
        upper = lower = null;

        LatexLabel l = new LatexLabel(parent.latex, node1.toString());
        //l.setFont(new Font("TimesRoman",Font.PLAIN,14));
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l, constraints);
        content.add(l);

        LatexLabel equiv = new LatexLabel(parent.latex, "$\\equiv$");
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(equiv,constraints);
        content.add(equiv);

        l = new LatexLabel(parent.latex, node2.toString());
        //l.setFont(new Font("TimesRoman",Font.PLAIN,14));
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l, constraints);
        content.add(l);

        finalizeConstructor();
    }


    /**
     * Displays a path from node1 to node2.
     * @param parent parent of this dialog
     * @param path the path
     */
    public InclusionResultDialog(ISGCIMainFrame parent, List<Inclusion> path) {
        this(parent);

        path = Algo.makePathProper(path);
        upper = Collections.singleton(path.get(0).getSuper());
        lower = Collections.singleton(path.get(path.size()-1).getSub());
        constraints.insets = new Insets(5, 5, 5, 5);

        JComponent w = makeStrictInclusionPanel();
        if (w != null) {
            gridbag.setConstraints(w, constraints);
            content.add(w);
        }
        
        //System.out.println(path);
        JPanel p = makePathPanel(path, true);
        gridbag.setConstraints(p, constraints);
        content.add(p);

        finalizeConstructor();
    }
    
    
    
    /**
     * Displays two paths between two (equivalent) nodes.
     * @param parent parent of this dialog
     * @param pathab one path
     * @param pathba the other path
     */
    public InclusionResultDialog(ISGCIMainFrame parent,
            List<Inclusion> pathab, List<Inclusion> pathba) {
        this(parent);
        upper = lower = null;
        int col = 0;

        JPanel ab = makePathPanel(pathab, false); 
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 5, 5, 10);
        constraints.anchor = GridBagConstraints.NORTH;
        gridbag.setConstraints(ab, constraints);
        content.add(ab);

        JPanel ba = makePathPanel(pathba, false);
        constraints.insets = new Insets(5, 10, 5, 5);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(ba, constraints);
        content.add(ba);        

        finalizeConstructor();
    }
    
        

    protected void finalizeConstructor() {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        
        JPanel p = new JPanel();
        refButton = new JButton("View references");
        drawButton = new JButton("Draw");
        okButton = new JButton("OK");
        p.add(refButton);
        p.add(drawButton);
        p.add(okButton);
        constraints.weightx = 0;
        constraints.weighty = 0;
        gridbag.setConstraints(p, constraints);
        content.add(p);
        if (upper == null  ||  lower == null)
            drawButton.setEnabled(false);

        // Add listeners
        okButton.addActionListener(this);
        drawButton.addActionListener(this);
        refButton.addActionListener(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    
    /**
     * Create a component describing why the given classes are incomparable or
     * disjoint and return it.
     * If neither can be determined, null is returned.
     */
    private JComponent makeNoRelationPanel(GraphClass node1, GraphClass node2){
        //---- First check the stored non-inclusion relations
        for (AbstractRelation r : DataSet.relations) {
            if (r instanceof Disjointness) {
                if (    GAlg.getPath(DataSet.inclGraph,r.get1(),node1)!=null &&
                        GAlg.getPath(DataSet.inclGraph,r.get2(),node2)!=null ||
                        GAlg.getPath(DataSet.inclGraph,r.get1(),node2)!=null &&
                        GAlg.getPath(DataSet.inclGraph,r.get2(),node1)!=null )
                    return makeNoRelationPanel(node1, node2, r);
            } else if (r instanceof Incomparability) {
                if (r.get1() == node1  &&  r.get2() == node2  ||
                        r.get1() == node2  &&  r.get2() == node1)
                    return makeNoRelationPanel(node1, node2, r);
            }
        }

        //---- Find equiv. forbiddenclass for node1, node2
        ForbiddenClass g1 = null, g2 = null;

        if (node1 instanceof ForbiddenClass) {
            g1 = (ForbiddenClass) node1;
        } else {
            for (GraphClass g : DataSet.getEquivalentClasses(node1))
                if (g instanceof ForbiddenClass) {
                    g1 = (ForbiddenClass) g;
                    break;
                }
        }

        if (node2 instanceof ForbiddenClass) {
            g2 = (ForbiddenClass) node2;
        } else {
            for (GraphClass g : DataSet.getEquivalentClasses(node2))
                if (g instanceof ForbiddenClass) {
                    g2 = (ForbiddenClass) g;
                    break;
                }
        }

        if (g1 != null  &&  g2 != null)
            return makeIncomparablePanel(g1, g2) ;

        return null;
    }


    /**
     * Create a component how the given classes are unrelated according to rel
     * and return it.
     */
    private JComponent makeNoRelationPanel(GraphClass node1, GraphClass node2,
            AbstractRelation rel) {
        JLabel l = null;
        if (rel instanceof Disjointness)
            l = new JLabel("Classes are disjoint");
        else if (rel instanceof Incomparability)
            l = new JLabel("Classes are incomparable");
        else
            return null;

        JComponent res = Box.createVerticalBox();
        JComponent refs = res;
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        res.add(l);

        if (rel instanceof Disjointness   &&  !(
                node1 == rel.get1()  &&  node2 == rel.get2() ||
                node1 == rel.get2()  &&  node2 == rel.get1()) ) {
            JComponent p = new JPanel();
            p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(new JLabel("by disjointness of"));
            p.add(new LatexLabel(parent.latex, rel.get1()+" and "+rel.get2()));
            res.add(p);
            refs = p;
        }

        StringBuilder s = new StringBuilder();
        for (Object o : rel.getRefs())
            s.append(o.toString());
        refs.add(new JLabel(s.toString()));

        return res;
    }


    /**
     * Create a component describing the forbidden subgraphs showing why the
     * given classes are incomparable and return it.
     * If neither can be determined, null is returned.
     */
    private JComponent makeIncomparablePanel(ForbiddenClass node1,
            ForbiddenClass node2) {
        StringBuilder why1 = new StringBuilder();
        StringBuilder why2 = new StringBuilder();
        Boolean not1 = ((ForbiddenClass) node2).notSubClassOf(
                (ForbiddenClass) node1, why1);
        Boolean not2 = ((ForbiddenClass) node1).notSubClassOf(
                (ForbiddenClass) node2, why2);

        if (!not1  ||  !not2)
            return null;

        JComponent res = Box.createVerticalBox();
        JLabel l = new JLabel("Classes are incomparable");
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        res.add(l);

        if (why1.length() > 0   &&  why2.length() > 0) {
            JComponent p = new JPanel();
            p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(new JLabel("witnesses:"));
            p.add(new LatexLabel(parent.latex, why1.toString()));
            p.add(new LatexLabel(parent.latex, why2.toString()));
            res.add(p);
        } else {
            JPanel p = new JPanel();
            p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(new JLabel("[forbidden subgraphs]"));
            res.add(p);
        }
        return res;
    }
    

    /**
     * Create a component describing the forbidden subgraphs showing why lower
     * is a _proper_ subclass of upper and return it.
     * If none can be determined, null is returned.
     */
    private JComponent makeStrictInclusionPanel() {
        //---- Find forbidden equivalent to lower
        ForbiddenClass forbLower = null;
        for (GraphClass gc :
                DataSet.getEquivalentClasses(lower.iterator().next())) {
            if (gc instanceof ForbiddenClass) {
                forbLower = (ForbiddenClass) gc;
                break;
            }
        }
        if (forbLower == null)
            return null;

        //---- Try to find a forbidden proper superclass
        for (GraphClass gc : Algo.nodesBetween(upper, lower)) {
            if (!(gc instanceof ForbiddenClass))
                continue;
            StringBuilder why = new StringBuilder();
            if ( ((ForbiddenClass) gc).notSubClassOf(forbLower, why)  &&
                    why.length() > 0) {
                JComponent res = Box.createVerticalBox();
                JLabel l = new JLabel("Inclusion is proper");
                l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                res.add(l);

                JComponent p = new JPanel();
                p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                p.add(new JLabel("witness:"));
                p.add(new LatexLabel(parent.latex, why.toString()));
                res.add(p);
                return res;
            }
        }
        return null;
    }
   

    /**
     * Create a component listing the given classes, order alphabetically, and
     * return it. If necessary, the component contains scrollbars.
     */
    private JComponent makeListPanel(List<GraphClass> classes) {
        if (classes == null  ||  classes.isEmpty())
            return new JLabel("None found", JLabel.LEFT);

        Collections.sort(classes, new LessLatex());

        JComponent label = null;
        JPanel panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;

        for (GraphClass gc : classes) {
            label = new LatexLabel(parent.latex, gc.toString());
            gridbag.setConstraints(label, c);
            panel.add(label);
        }
        c.weightx = 1;
        c.weighty = 1;
        gridbag.setConstraints(label, c);
        JComponent res = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        res.setBorder(new EmptyBorder(0,0,0,0));
        return res;
    }


    /**
     * Create a panel that shows the given inclusion path.
     * If details = true, distinguish between =, < and <=, otherwise use <=.
     */
    private JPanel makePathPanel(List<Inclusion> path, boolean details) {
        JPanel compo = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        compo.setLayout(gridbag); 
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        
        for (int i = path.size()-1; i >= 0; i--) {
            LatexLabel l = new LatexLabel(parent.latex,
                path.get(i).getSub().toString());
            constraints.gridwidth = 1;
            gridbag.setConstraints(l, constraints);
            compo.add(l);
            JLabel label2=new JLabel("", JLabel.CENTER);
                constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(label2, constraints);
            compo.add(label2);
            
            Inclusion e =  path.get(i);
            GraphClass sup = e.getSuper();
            GraphClass sub = e.getSub();

            LatexLabel subset;
            if (details  &&
                        DataSet.getEquivalentClasses(sup).contains(sub))
                subset = new LatexLabel(parent.latex, "   $\\equiv$");
            else if (details  &&  e.isProper())
                subset = new LatexLabel(parent.latex, "   $\\subset$");
            else
                subset = new LatexLabel(parent.latex, "   $\\subseteq$");
            constraints.gridwidth = 1;
            gridbag.setConstraints(subset,constraints);
            compo.add(subset);
                
            StringBuffer s = new StringBuffer();
            for (Object o : e.getRefs())
                s.append(o);
            JLabel label1=new JLabel(s.toString(), JLabel.CENTER);
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(label1, constraints);
            compo.add(label1);
        }

        LatexLabel l = new LatexLabel(parent.latex,
            path.get(0).getSuper().toString());
        constraints.gridwidth = 1;
        gridbag.setConstraints(l, constraints);
        compo.add(l);
        JLabel label2=new JLabel("", JLabel.CENTER);
            constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label2, constraints);
        compo.add(label2);

        return compo;
        
    }
    

    protected void closeDialog() {
        setVisible(false);
        dispose();
    }


    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == okButton) {
            closeDialog();
            return;
        } else if (source == drawButton) {
            Cursor oldcursor = parent.getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            parent.graphCanvas.drawHierarchy(Algo.nodesBetween(upper, lower));

            NodeView node1 = parent.graphCanvas.findNode(
                    DataSet.getClass(nodeName1));
            NodeView node2 = parent.graphCanvas.findNode(
                    DataSet.getClass(nodeName2));
            if (node1 != null  && node2 != null) {
                node1.setNameAndLabel(nodeName1);
                node2.setNameAndLabel(nodeName2);
            }
            
            setCursor(oldcursor);
            closeDialog();
            parent.graphCanvas.repaint();
        } else if (source == refButton) {
            parent.loader.showDocument("classes/refs00.html");
        }
    }


    /**
     * Creates a window displaying the relation between the given classnames.
     * This window is not made visible!
     */
    public static InclusionResultDialog newInstance(ISGCIMainFrame parent,
            GraphClass v1, GraphClass v2) {
        List<Inclusion> v12 = GAlg.getPath(DataSet.inclGraph, v1, v2),
                        v21 = GAlg.getPath(DataSet.inclGraph, v2, v1);
        InclusionResultDialog dialog = null;
        
        nodeName1 = v1.toString();
        nodeName2 = v2.toString();

        if (v1 == v2) { // equal
            dialog = new InclusionResultDialog(parent, v1, v2);
        } else if (v12 == null  &&  v21 == null) { // no relation
            dialog = new InclusionResultDialog(parent, 
                    Algo.findMinimalSuper(v1, v2),
                    Algo.findMaximalSub(v1, v2),
                    v1, v2);
        } else if (v12 != null  &&  v21 != null) { // equivalent
            dialog = new InclusionResultDialog(parent, v12, v21);
        } else  {       // subclass
            dialog = new InclusionResultDialog(parent,
                    v12 != null ? v12 : v21);
        }

        dialog.pack();
        Dimension size = dialog.getPreferredSize();
        if (size.width > 600  ||  size.height > 600)
            dialog.setSize(Math.min(size.width, 600),
                    Math.min(size.height, 600));
        
        return dialog;
    }
}

/* EOF */
