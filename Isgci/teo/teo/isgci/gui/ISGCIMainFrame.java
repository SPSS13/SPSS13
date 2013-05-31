/*
 * The main window of ISGCI. Also the class to start the program.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIMainFrame.java,v 2.4 2013/04/07 10:51:04 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import java.io.*;
import teo.isgci.db.DataSet;
import teo.isgci.db.Ref;
import teo.isgci.problem.*;
import teo.isgci.gc.ForbiddenClass;
import teo.isgci.gc.GraphClass;

import java.awt.Color;
import org.jgrapht.*;
import org.jgrapht.graph.SimpleDirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.xml.GraphMLWriter;

/*import teo.isgci.gc.GraphClass;
import java.util.ArrayList;*/


/** The main frame of the application.
 */
public class ISGCIMainFrame extends JFrame
        implements WindowListener, ActionListener, ItemListener {

    public static final String APPLICATIONNAME = "ISGCI";

    public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
    public static LatexGraphics latex;
    public static Font font;

    protected teo.Loader loader;

    // The menu
    protected JMenuItem miNew, miExport, miExit;
    protected JMenuItem miNaming, miSearching, miDrawUnproper;
    protected JMenuItem miSelectGraphClasses, miCheckInclusion;
    protected JMenuItem miGraphClassInformation;
    protected JMenuItem miCut, miCopy, miPaste, miDelete, miSelectAll;
    protected JMenu miOpenProblem, miColourProblem;
    protected JMenuItem miSmallgraphs, miHelp, miAbout;

    // This is where the drawing goes.
    protected JScrollPane drawingPane;
    public ISGCIGraphCanvas graphCanvas;


    /** Creates the frame.
     * @param locationURL The path/URL to the applet/application.
     * @param isApplet true iff the program runs as an applet.
     */
    public ISGCIMainFrame(teo.Loader loader) {
        super(APPLICATIONNAME);

        loader.register();
        this.loader = loader;
        tracker = this;

        DataSet.init(loader, "data/isgci.xml");
        ForbiddenClass.initRules(loader, "data/smallgraphs.xml");
        PSGraphics.init(loader);
        if (latex == null) {
            latex = new LatexGraphics();
            latex.init(loader);
        }

        boolean createMaps = false;
        try {
            createMaps = System.getProperty("org.isgci.mappath") != null;
        } catch (Exception e) {}

        if (createMaps) {       // Create maps and terminate
            createCanvasPanel();
            new teo.isgci.util.LandMark(this).createMaps();
            closeWindow();
        }

        /*{
            int sub = 0, equ = 0, incomp = 0, incompWit = 0, incompWitFin = 0;

            ArrayList<ForbiddenClass> fcs = new ArrayList<ForbiddenClass>();
            for (GraphClass gc : DataSet.getClasses())
                if (gc instanceof ForbiddenClass)
                    fcs.add((ForbiddenClass) gc);

            for (int i = 0; i < fcs.size()-1; i++) {
                for (int j = i+1; j < fcs.size(); j++) {
                    boolean sub1 = fcs.get(i).subClassOf(fcs.get(j));
                    boolean sub2 = fcs.get(j).subClassOf(fcs.get(i));
                    if (sub1  &&  sub2)
                        equ++;
                    else if (!sub1  &&  !sub2) {
                        StringBuilder why1 = new StringBuilder();
                        StringBuilder why2 = new StringBuilder();
                        Boolean not1 = fcs.get(j).notSubClassOf(
                                fcs.get(i), why1);
                        Boolean not2 = fcs.get(i).notSubClassOf(
                                fcs.get(j), why2);
                        if (not1  &&  not2) {
                            if (why1.length() > 0  &&  why2.length() > 0) {
                                if (fcs.get(i).isFinite()  &&
                                        fcs.get(j).isFinite())
                                    incompWitFin++;
                                else
                                    incompWit++;
                            } else
                                incomp++;
                        }
                    } else
                        sub++;
                }
            }
            System.out.println("Total: "+ fcs.size() +
                    " sub: "+ sub +
                    " equ: "+ equ +
                    " incomparable: "+ incomp +
                    " incomparable with finite witness: "+ incompWitFin +
                    " incomparable with witness: "+ incompWit);
        }*/

        /*
        writeGraphML();
        closeWindow();
        */

        setJMenuBar(createMenus());
        getContentPane().add("Center", createCanvasPanel());
        registerListeners();
        setLocation(20, 20);
        pack();
        setVisible(true);
    }


    /**
     * Write the entire database in GraphML to isgcifull.graphml.
     */
    private void writeGraphML() {
        OutputStreamWriter out = null;

        SimpleDirectedGraph<GraphClass, Inclusion> g =
            new SimpleDirectedGraph<GraphClass, Inclusion>(Inclusion.class);
        Graphs.addGraph(g, DataSet.inclGraph);
        GAlg.transitiveReductionBruteForce(g);

        try {
            out = new OutputStreamWriter(
                    new FileOutputStream("isgcifull.graphml"), "UTF-8");
            GraphMLWriter w = new GraphMLWriter(out,
                        GraphMLWriter.MODE_PLAIN,
                        true,
                        false);
            w.startDocument();
            for (GraphClass gc : g.vertexSet()) {
                w.writeNode(gc.getID(), gc.toString(), Color.WHITE);
            }
            for (Inclusion e : g.edgeSet()) {
                w.writeEdge(e.getSuper().getID(), e.getSub().getID(),
                        e.isProper());
            }
            w.endDocument();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates and attaches the necessary eventlisteners.
     */
    protected void registerListeners() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        miNew.addActionListener(this);
        miExport.addActionListener(this);
        miExit.addActionListener(this);
        miNaming.addActionListener(this);
        miSearching.addActionListener(this);
        miDrawUnproper.addItemListener(this);
        miSelectGraphClasses.addActionListener(this);
        miCheckInclusion.addActionListener(this);
        miGraphClassInformation.addActionListener(this);
        //miDelete.addActionListener(this);
        //miSelectAll.addActionListener(this);
        //miOpenProblem.addActionListener(this);
        miSmallgraphs.addActionListener(this);
        miHelp.addActionListener(this);
        miAbout.addActionListener(this);
    }


    /**
     * Creates the menu system.
     * @return The created JMenuBar
     * @see JMenuBar
     */
    protected JMenuBar createMenus() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu fileMenu, editMenu, viewMenu,  graphMenu, helpMenu, problemsMenu;
        JMenuItem menu;

        fileMenu = new JMenu("File");
        fileMenu.add(miNew = new JMenuItem("New window"));
        fileMenu.add(miExport = new JMenuItem("Export drawing..."));
        fileMenu.add(miExit = new JMenuItem("Exit"));
        mainMenuBar.add(fileMenu);

        /*editMenu = new Menu("Edit");
        editMenu.add(miDelete = new MenuItem("Delete"));
        editMenu.add(miSelectAll = new MenuItem("Select all"));
        mainMenuBar.add(editMenu);*/

        viewMenu = new JMenu("View");
        viewMenu.add(miSearching = new JMenuItem("Search in drawing..."));
        viewMenu.add(miNaming = new JMenuItem("Naming preference..."));
        viewMenu.add(miDrawUnproper =
                new JCheckBoxMenuItem("Mark unproper inclusions", true));
        /* menu = new ScaleMenu();
        menu.setEnabled(false);
        viewMenu.add(menu); */
        mainMenuBar.add(viewMenu);

        graphMenu = new JMenu("Graph classes");
        miGraphClassInformation = new JMenuItem("Browse Database");
        graphMenu.add(miGraphClassInformation);
        miCheckInclusion = new JMenuItem("Find Relation...");
        graphMenu.add(miCheckInclusion);
        miSelectGraphClasses = new JMenuItem("Draw...");
        graphMenu.add(miSelectGraphClasses);
        mainMenuBar.add(graphMenu);

        problemsMenu = new JMenu("Problems");
        miOpenProblem = new JMenu("Boundary/Open classes");
        problemsMenu.add(miOpenProblem);
        for (int i = 0; i < DataSet.problems.size(); i++) {
            menu = new JMenuItem(
                    ((Problem) DataSet.problems.elementAt(i)).getName());
            miOpenProblem.add(menu);
            menu.addActionListener(this);
            menu.setActionCommand("miOpenProblem");
        }
        miColourProblem = new ProblemsMenu(this, "Colour for problem");
        problemsMenu.add(miColourProblem);
        mainMenuBar.add(problemsMenu);


        helpMenu = new JMenu("Help");
        miSmallgraphs = new JMenuItem("Small graphs");
        helpMenu.add(miSmallgraphs);
        miHelp = new JMenuItem("Help");
        helpMenu.add(miHelp);
        miAbout = new JMenuItem("About");
        helpMenu.add(miAbout);
        //mainMenuBar.add(Box.createHorizontalGlue());
        mainMenuBar.add(helpMenu);

        return mainMenuBar;
    }


    /**
     * Creates the drawing canvas with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JComponent createCanvasPanel() {
        graphCanvas = new ISGCIGraphCanvas(this);
        drawingPane = new JScrollPane(graphCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
        drawingPane.getVerticalScrollBar().setUnitIncrement(100);
        
        return drawingPane;
    }


    /**
     * Center the canvas on the given point.
     */
    public void centerCanvas(Point p) {
        JViewport viewport = drawingPane.getViewport();
        Dimension port = viewport.getExtentSize();
        Dimension view = viewport.getViewSize();

        p.x -= port.width/2;
        if (p.x + port.width > view.width)
            p.x = view.width - port.width;
        if (p.x < 0)
            p.x = 0;
        p.y -= port.height/2;
        if (p.y + port.height > view.height)
            p.y = view.height - port.height;
        if (p.y < 0)
            p.y = 0;
        viewport.setViewPosition(p);
    }

    public void printPort() {
        Rectangle view = getViewport();
        System.err.println("port: "+view);
    }

    public Rectangle getViewport() {
        return drawingPane.getViewport().getViewRect();
    }

    
    /** Closes the window and possibly terminates the program. */
    public void closeWindow() {
        setVisible(false);
        dispose();
        loader.unregister();
    }

    /**
     * Eventhandler for window events
     */
    public void windowClosing(WindowEvent e) {
        closeWindow();
    }

    /**
     * Required to overload (abstract)
     */
    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}

    /**
     * Eventhandler for menu selections
     */
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();

        if (object == miExit) {
            closeWindow();
        } else if (object == miNew) {
            new ISGCIMainFrame(loader);
        } else if (object == miExport) {
            JDialog export = new ExportDialog(this);
            export.setLocation(50, 50);
            export.pack();
            export.setVisible(true);
        } else if (object == miNaming) {
            JDialog d = new NamingDialog(this);
            d.setLocation(50,50);
            d.pack();
            d.setVisible(true);
        } else if (object == miSearching) {
            JDialog search = new SearchDialog(this);
            search.setLocation(50,50);
            search.setVisible(true);
        } else if (object == miGraphClassInformation) {
            JDialog info = new GraphClassInformationDialog(this);
            info.setLocation(50, 50);
            info.pack();
            info.setSize(800, 600);
            info.setVisible(true);
        } else if (object == miCheckInclusion) {
            JDialog check = new CheckInclusionDialog(this);
            check.setLocation(50, 50);
            check.pack();
            check.setSize(700, 400);
            check.setVisible(true);
        } else if (object == miSelectGraphClasses) {
            JDialog select = new GraphClassSelectionDialog(this);
            select.setLocation(50, 50);
            select.pack();
            select.setSize(500, 400);
            select.setVisible(true);
        } else if (object == miAbout) {
            JDialog select = new AboutDialog(this);
            select.setLocation(50, 50);
            select.setVisible(true);
        } else if (object == miHelp) {
            loader.showDocument("help.html");
        } else if (object == miSmallgraphs) {
            loader.showDocument("smallgraphs.html");
        } else if (event.getActionCommand() == "miOpenProblem") {
            JDialog open=new OpenProblemDialog(this,
                    ((JMenuItem) event.getSource()).getText());
            open.setLocation(50, 50);
            open.setVisible(true);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object object = event.getSource();

        if (object == miDrawUnproper) {
            graphCanvas.setDrawUnproper(
                    ((JCheckBoxMenuItem) object).getState());
        }
    }
}


/* EOF */
