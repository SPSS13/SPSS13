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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.model.mxCell;

import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.shape.mxIMarker;
import com.mxgraph.shape.mxMarkerRegistry;

import teo.isgci.grapht.*;
import teo.isgci.xml.GraphMLWriter;
import com.javaswingcomponents.accordion.JSCAccordion;
/*import teo.isgci.gc.GraphClass;
import java.util.ArrayList;*/


/** The main frame of the application.
 */
public class ISGCIMainFrame extends JFrame
        implements WindowListener, ActionListener, ItemListener, MenuListener {

    public static final String APPLICATIONNAME = "ISGCI";

    public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
    public static LatexGraphics latex;
    public static Font font;

    protected teo.Loader loader;

    // The menu
    protected JMenuItem miNew, miExport, miExit;
    protected JMenuItem miSidebar, miNaming, miSearching, miDrawUnproper;
    protected JMenuItem miSelectGraphClasses, miCheckInclusion;
    protected JMenuItem miGraphClassInformation;
    protected JMenuItem miCut, miCopy, miPaste, miDelete, miSelectAll;
    protected JMenu miOpenProblem, miColourProblem;
    protected JMenuItem miSmallgraphs, miHelp, miAbout;
  //Selection MenuItems
    protected JMenuItem miShowInformation;
    protected JMenuItem miShowDetails;
    protected JMenuItem miShowNeighbours;
    protected JMenuItem miHideNeighbours;
    protected JMenuItem miShowSuperclasses;
    protected JMenuItem miHideSuperclasses;
    protected JMenuItem miShowSubclasses;
    protected JMenuItem miHideSubclasses;
    protected JMenu selectionMenu;
    

    // This is where the drawing goes.
    //rework
    protected JScrollPane drawingPane;
    public ISGCIGraphCanvas graphCanvas;
    protected final Accordion sidebar;

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

        // set Color for improper Inclusions
        addImproperInclColor();
        
        
        sidebar = new Accordion();
        sidebar.setVisible(false);
        setJMenuBar(createMenus());
        getContentPane().add("Center", createCanvasPanel());
        getContentPane().add("West", sidebar);
        registerListeners();
        setLocation(100, 20);
        this.setSize(500, 400);
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
        miSidebar.addActionListener(this);
        miSearching.addActionListener(this);
        miDrawUnproper.addItemListener(this);
        miSelectGraphClasses.addActionListener(this);
        miCheckInclusion.addActionListener(this);
        miGraphClassInformation.addActionListener(this);
        miShowSuperclasses.addActionListener(this);
        miHideSuperclasses.addActionListener(this);
        miHideSubclasses.addActionListener(this);
        miShowSubclasses.addActionListener(this);
        miHideSubclasses.addActionListener(this);
        miShowNeighbours.addActionListener(this);
       // miHideNeighbours.addActionListener(this);
        //miDelete.addActionListener(this);
        //miSelectAll.addActionListener(this);
        //miOpenProblem.addActionListener(this);
        miSmallgraphs.addActionListener(this);
        miHelp.addActionListener(this);
        miAbout.addActionListener(this);
        //Bind MenuListener for disabling
        selectionMenu.addMenuListener(this);
        
        
        //
        
        miShowInformation.addActionListener(this);
        miShowDetails.addActionListener(this);
        //
        graphCanvas.registerMouseListener((mxGraphComponent) drawingPane);
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
        viewMenu.add(miSidebar = new JMenuItem("Details visible"));
        viewMenu.add(miSearching = new JMenuItem("Search in drawing..."));
        viewMenu.add(miNaming = new JMenuItem("Naming preference..."));
        viewMenu.add(miDrawUnproper =
                new JCheckBoxMenuItem("Mark unproper inclusions", true));
        /* menu = new ScaleMenu();
        menu.setEnabled(false);
        viewMenu.add(menu); */
        mainMenuBar.add(viewMenu);
        
        //Create new Menu to add to the TopMenuBar named Selected
        selectionMenu = new JMenu("Selection");
        // Add MenuItems
        selectionMenu.add(miShowInformation = new JMenuItem("Show Information"));
        selectionMenu.add(miShowDetails = new JMenuItem("Show details"));
        selectionMenu.addSeparator();
        selectionMenu.add(miShowNeighbours = new JMenuItem("Show neighbours"));
        selectionMenu.add(miHideNeighbours = new JMenuItem("Hide neighbours"));
        selectionMenu.addSeparator();
        selectionMenu.add(miShowSuperclasses = new JMenuItem("Show superclasses"));
        selectionMenu.add(miHideSuperclasses = new JMenuItem("Hide superclasses"));
        selectionMenu.addSeparator();
        selectionMenu.add(miShowSubclasses = new JMenuItem("Show subclasses"));
        selectionMenu.add(miHideSubclasses = new JMenuItem("Hide sublcasses"));
        mainMenuBar.add(selectionMenu);

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
    
    
    // Menu handling to disable some menu points if more than one or zero cells
    // are selected.

    @Override
    public void menuSelected(MenuEvent e) {
     if(e.getSource()==selectionMenu){
      //System.out.println(((mxGraphComponent)drawingPane).getGraph().getSelectionCount());
      if(((mxGraphComponent)drawingPane).getGraph().getSelectionCount()!=1){
       //disable all features
       System.out.println("in");
       miHideNeighbours.setEnabled(false);
       miShowInformation.setEnabled(false);
       miShowDetails.setEnabled(false);
       miShowNeighbours.setEnabled(false);
       miHideNeighbours.setEnabled(false);
       miShowSuperclasses.setEnabled(false);
       miHideSuperclasses.setEnabled(false);
       miShowSubclasses.setEnabled(false);
       miHideSubclasses.setEnabled(false);
      }else{
       //Test if the selection is a vertex or an edge
    	if(((mxCell)((mxGraphComponent)drawingPane).getGraph().getSelectionCell()).isEdge()){
        //Cell is edge, show only miShowDetails
        miHideNeighbours.setEnabled(false);
        miShowInformation.setEnabled(true);
        miShowDetails.setEnabled(false);
        miShowNeighbours.setEnabled(false);
        miHideNeighbours.setEnabled(false);
        miShowSuperclasses.setEnabled(false);
        miHideSuperclasses.setEnabled(false);
        miShowSubclasses.setEnabled(false);
        miHideSubclasses.setEnabled(false);
       }else{
        //Cell is vertex, show all items
        miHideNeighbours.setEnabled(true);
        miShowInformation.setEnabled(true);
        miShowDetails.setEnabled(true);
        miShowNeighbours.setEnabled(true);
        miHideNeighbours.setEnabled(true);
        miShowSuperclasses.setEnabled(true);
        miHideSuperclasses.setEnabled(true);
        miShowSubclasses.setEnabled(true);
        miHideSubclasses.setEnabled(true);
       }
      }
     }

    }

    @Override
    public void menuDeselected(MenuEvent e) {
     // TODO Auto-generated method stub

    }

    @Override
    public void menuCanceled(MenuEvent e) {
     // TODO Auto-generated method stub

    }


    /**
     * Creates the drawing canvas with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JComponent createCanvasPanel() {
    	mxGraph graph = new mxGraph();
    	graph = setGraphSwitches(graph);
    	graph.setAllowDanglingEdges(false);
    	mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphCanvas = new ISGCIGraphCanvas(this,graph);
        drawingPane = graphComponent;
        drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
        drawingPane.getVerticalScrollBar().setUnitIncrement(100);
        drawingPane.setBounds(20, 20, 500, 400);
        drawingPane.getViewport().setOpaque(false);
        drawingPane.setOpaque(true);
        drawingPane.setBackground(Color.white);
        ((mxGraphComponent) drawingPane).setPanning(true);
        ((mxGraphComponent) drawingPane).setDragEnabled(false);
        return drawingPane;
    }
    
//    protected JComponent createSidebar() {
//                final Accordion obj = new Accordion();
//                obj.setVisible(false);
//               // obj.setContent("http://www.graphclasses.org/classes/gc_230.html");
//                return obj;
//    }


    private mxGraph setGraphSwitches(mxGraph graph) {
        graph.setCellsEditable(false);
        graph.setCellsDisconnectable(false);
        graph.setAutoSizeCells(true);
        graph.setBorder(10);
        graph.setEdgeLabelsMovable(false);
        graph.setVertexLabelsMovable(false);
        graph.setSplitEnabled(false);
        graph.setResetEdgesOnMove(true);
        graph.setHtmlLabels(true);
        graph.setAllowDanglingEdges(false);
        graph.setConnectableEdges(false);
        graph.setDisconnectOnMove(false);
        //((mxGraphComponent)drawingPane).setConnectable(false);
        return graph;
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
        	System.out.println("naming");
            JDialog d = new NamingDialog(this);
            d.setLocation(50,50);
            d.pack();
            d.setVisible(true);
        } else if (object == miSearching) {
        	System.out.println("test");
            JDialog search = new SearchDialog(this);
            search.setLocation(50,50);
            search.setVisible(true);
        } else if (object == miSidebar) {
            sidebar.toggleVisibility();
            graphCanvas.setSidebarConent();
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
        } else if (object == miShowInformation){
        	JDialog d = new GraphClassInformationDialog(graphCanvas.getParent(), 
        			graphCanvas.nodePopup.searchName(graphCanvas.getSelectedCell()));
			d.setLocation(50, 50);
			d.pack();
			d.setSize(800, 600);
			d.setVisible(true);
        } else if (object == miShowDetails){
            sidebar.setVisible(true);
            graphCanvas.setSidebarConent();
        } else if (object == miShowNeighbours){
        	graphCanvas.drawNeighbours();
        } else if (object == miHideNeighbours){
        	
        	//new
        } else if (object == miShowSuperclasses){
            graphCanvas.drawSuperSub(graphCanvas.getSuperNodes());            
        } else if (object == miHideSuperclasses){
        	graphCanvas.deleteSuperSub(graphCanvas.getSuperNodes());
        } else if (object == miShowSubclasses){
        	graphCanvas.drawSuperSub(graphCanvas.getSubNodes());
        } else if (object == miHideSubclasses){
        	graphCanvas.deleteSuperSub(graphCanvas.getSubNodes());
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
    
    public void clear(){
        System.out.println("#####################################removing");
        //drawingPane.removeAll();
    }

    /**
    * @author Matthias Miller
    * @date 25.06.2013
    * @annotation sets the color (grey) for improper inclusion while using mxIMarker of the Library JGraphX
    */

public void addImproperInclColor(){
    mxIMarker tmp = new mxIMarker()
 {
  public mxPoint paintMarker(mxGraphics2DCanvas canvas,
    mxCellState state, String type, mxPoint pe, double nx,
    double ny, double size, boolean source)
  {
   Polygon poly = new Polygon();
   poly.addPoint((int) Math.round(pe.getX()),
     (int) Math.round(pe.getY()));
   poly.addPoint((int) Math.round(pe.getX() - nx - ny / 2),
     (int) Math.round(pe.getY() - ny + nx / 2));

   if (type.equals("improper"))
   {
    poly.addPoint((int) Math.round(pe.getX() - nx * 3 / 4),
      (int) Math.round(pe.getY() - ny * 3 / 4));
   }

   poly.addPoint((int) Math.round(pe.getX() + ny / 2 - nx),
     (int) Math.round(pe.getY() - ny - nx / 2));

   if (mxUtils.isTrue(state.getStyle(), (source) ? "startFill" : "endFill", true))
   {
    canvas.fillShape(poly);
   }
   
   canvas.getGraphics().setPaint(Color.LIGHT_GRAY);
   canvas.fillShape(poly);
   canvas.getGraphics().draw(poly);

   return new mxPoint(-nx, -ny);
  }
 };
       mxMarkerRegistry.registerMarker("improper", tmp);
   }
}

/* EOF */
