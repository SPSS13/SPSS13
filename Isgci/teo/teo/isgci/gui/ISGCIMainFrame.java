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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicArrowButton;

import teo.isgci.db.DataSet;
import teo.isgci.gc.ForbiddenClass;
import teo.isgci.problem.Problem;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxIMarker;
import com.mxgraph.shape.mxMarkerRegistry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxGraphControl;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/*import teo.isgci.gc.GraphClass;
 import java.util.ArrayList;*/

/**
 * The main frame of the application.
 */
public class ISGCIMainFrame extends JFrame implements WindowListener,
        ActionListener, ItemListener, MenuListener {

    /**
     * 
     */
    private static final long serialVersionUID = -6245759418166375376L;

    public static final String APPLICATIONNAME = "ISGCI";

    public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
    public static LatexGraphics latex;
    public static Font font;

    protected teo.Loader loader;

    // The menu
    protected JMenuItem miNew, miExport, miExit;
    protected JMenuItem miSidebar, miNaming, miSearching, miFitInWindow,
            miDrawUnproper, miAnimation, miUndo, miRedo, miLayout;
    protected JMenuItem miSelectGraphClasses, miCheckInclusion;
    protected JMenuItem miGraphClassInformation;
    protected JMenuItem miCut, miCopy, miPaste, miDelete, miSelectAll;
    protected JMenu miOpenProblem, miColourProblem;
    protected JMenuItem miSmallgraphs, miHelp, miAbout;
    // Selection MenuItems
    protected JMenuItem miShowInformation, miShowDetails, miAddSuperclasses, miAddSubclasses, miAddNeighbours;
    protected JMenuItem miShowSuperclasses;
    protected JMenuItem miHideSuperclasses;
    protected JMenuItem miShowSubclasses;
    protected JMenuItem miHideSubclasses;
    protected JMenu selectionMenu;
    protected JMenu editMenu;
    protected mxUndoManager undoManager;

    protected BasicArrowButton button2;

    /**
     * Needed to add undoManager to the Mainframe
     */
    protected mxIEventListener undoHandler = new mxIEventListener() {
        public void invoke(Object source, mxEventObject evt) {
            undoManager.undoableEditHappened((mxUndoableEdit)evt
                    .getProperty("edit"));
        }
    };

    // This is where the drawing goes.
    // rework
    protected JScrollPane drawingPane;
    public ISGCIGraphCanvas graphCanvas;
    protected final Accordion sidebar;
    protected final Thread sidebarThread;
    protected JCheckBoxMenuItem visibleHaken = new JCheckBoxMenuItem(
            "Details visible", false);

    /**
     * Creates the frame.
     * 
     * @param locationURL
     *            The path/URL to the applet/application.
     * @param isApplet
     *            true iff the program runs as an applet.
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
        } catch (Exception e) {
        }

        if (createMaps) { // Create maps and terminate
            createCanvasPanel();
            new teo.isgci.util.LandMark(this).createMaps();
            closeWindow();
        }

        // set Color for improper Inclusions
        addImproperInclColor();

        undoManager = new mxUndoManager();

        sidebar = new Accordion(this);
        sidebarThread = new Thread(sidebar);
        sidebarThread.start();
        sidebarThread.suspend();
        sidebar.setVisible(false);
        setJMenuBar(createMenus());
        getContentPane().add("Center", createCanvasPanel());
        getContentPane().add("West", sidebar);
        registerListeners();

        button2 = new BasicArrowButton(SwingConstants.EAST);// The fade-in
                                                            // Button
        button2.setBorder(new LineBorder(Color.BLACK, 2));
//        button2.setBackground(new Color(255, 255, 255));
        getContentPane().add("West", button2);

        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sidebar.visibilityChanged();
                button2.setVisible(false);
                getContentPane().add("West", sidebar);
                // sidebar.setVisible(true);
                visibleHaken.setState(true);
            }
        });

        /*
         * Add undomanager and undohandler to the mainframe by registrating to
         * the graph in the mxGraphComponent
         */
        ((mxGraphComponent)drawingPane).getGraph().getModel()
                .addListener(mxEvent.UNDO, undoHandler);
        ((mxGraphComponent)drawingPane).getGraph().getView()
                .addListener(mxEvent.UNDO, undoHandler);
        mxIEventListener undoHandler = new mxIEventListener() {
            public void invoke(Object source, mxEventObject evt) {
                List<mxUndoableChange> changes = ((mxUndoableEdit)evt
                        .getProperty("edit")).getChanges();
                ((mxGraphComponent)drawingPane).getGraph().setSelectionCells(
                        ((mxGraphComponent)drawingPane).getGraph()
                                .getSelectionCellsForChanges(changes));
            }
        };
        // add Listeners to the undomanager
        undoManager.addListener(mxEvent.UNDO, undoHandler);
        undoManager.addListener(mxEvent.REDO, undoHandler);

        setLocation(100, 20);
        this.setSize(500, 400);
        setVisible(true);
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
        miFitInWindow.addActionListener(this);
        miDrawUnproper.addItemListener(this);
        miAnimation.addActionListener(this);
        miUndo.addActionListener(this);
        miRedo.addActionListener(this);
        miLayout.addActionListener(this);
        miSelectGraphClasses.addActionListener(this);
        miCheckInclusion.addActionListener(this);
        miGraphClassInformation.addActionListener(this);
        miAddSuperclasses.addActionListener(this);
        miAddSubclasses.addActionListener(this);
        miShowSuperclasses.addActionListener(this);
        miHideSuperclasses.addActionListener(this);
        miHideSubclasses.addActionListener(this);
        miShowSubclasses.addActionListener(this);
        miHideSubclasses.addActionListener(this);
        miAddNeighbours.addActionListener(this);
        // miHideNeighbours.addActionListener(this);
        // miDelete.addActionListener(this);
        // miSelectAll.addActionListener(this);
        // miOpenProblem.addActionListener(this);
        miSmallgraphs.addActionListener(this);
        miHelp.addActionListener(this);
        miAbout.addActionListener(this);
        // Bind MenuListener for disabling
        selectionMenu.addMenuListener(this);
        editMenu.addMenuListener(this);

        //

        miShowInformation.addActionListener(this);
        miShowDetails.addActionListener(this);
        //
        graphCanvas.registerMouseListener((mxGraphComponent)drawingPane);
    }

    /**
     * Creates the menu system.
     * 
     * @return The created JMenuBar
     * @see JMenuBar
     */
    protected JMenuBar createMenus() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu fileMenu, viewMenu, graphMenu, helpMenu, problemsMenu;
        JMenuItem menu;

        fileMenu = new JMenu("File");
        fileMenu.add(miNew = new JMenuItem("New window"));
        fileMenu.add(miExport = new JMenuItem("Export drawing..."));
        fileMenu.add(miExit = new JMenuItem("Exit"));
        mainMenuBar.add(fileMenu);

        /*
         * editMenu = new Menu("Edit"); editMenu.add(miDelete = new
         * MenuItem("Delete")); editMenu.add(miSelectAll = new
         * MenuItem("Select all")); mainMenuBar.add(editMenu);
         */

        viewMenu = new JMenu("View");
        // viewMenu.add(bind(mxResources.get("undo"), new HistoryAction(true)));
        // viewMenu.add(bind(mxResources.get("redo"), new
        // HistoryAction(false)));

        viewMenu.add(miFitInWindow = new JMenuItem("Fit in window"));
        viewMenu.add(miSearching = new JMenuItem("Search in drawing..."));
        viewMenu.add(miNaming = new JMenuItem("Naming preference..."));
        viewMenu.add(miSidebar = visibleHaken);
        viewMenu.add(miDrawUnproper = new JCheckBoxMenuItem(
                "Mark improper inclusions", true));
        /*
         * menu = new ScaleMenu(); menu.setEnabled(false); viewMenu.add(menu);
         */
        mainMenuBar.add(viewMenu);

        // Create new Menu to add Editing Options -> Redo, Undo
        editMenu = new JMenu("Editing");

        editMenu.add(miAnimation = new JCheckBoxMenuItem("Animation", false));
        editMenu.add(miUndo = new JMenuItem("Undo..."));
        editMenu.add(miRedo = new JMenuItem("Redo..."));
        editMenu.add(miLayout = new JMenuItem("Relayout"));

        mainMenuBar.add(editMenu);

        // Create new Menu to add to the TopMenuBar named Selected
        selectionMenu = new JMenu("Selection");
        // Add MenuItems
        selectionMenu
                .add(miShowInformation = new JMenuItem("Show information"));
        selectionMenu.add(miShowDetails = new JMenuItem("Show sidebar"));
        selectionMenu.addSeparator();
        selectionMenu.add(miShowSuperclasses = new JMenuItem(
                "Show superclasses"));
        selectionMenu.add(miHideSuperclasses = new JMenuItem(
                "Hide superclasses"));
        selectionMenu.addSeparator();
        selectionMenu.add(miShowSubclasses = new JMenuItem("Show subclasses"));
        selectionMenu.add(miHideSubclasses = new JMenuItem("Hide sublcasses"));
        selectionMenu.addSeparator();
        selectionMenu.add(miAddSuperclasses = new JMenuItem("Add superclasses"));
        selectionMenu.add(miAddSubclasses = new JMenuItem("Add subclasses"));
        selectionMenu.add(miAddNeighbours = new JMenuItem("Add neighbours"));
        
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
                    ((Problem)DataSet.problems.elementAt(i)).getName());
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
        // mainMenuBar.add(Box.createHorizontalGlue());
        mainMenuBar.add(helpMenu);

        return mainMenuBar;
    }

    // Menu handling to disable some menu points if more than one or zero cells
    // are selected.

    @Override
    public void menuSelected(MenuEvent e) {
        if (e.getSource() == selectionMenu) {
            // System.out.println(((mxGraphComponent)drawingPane).getGraph().getSelectionCount());
            if (((mxGraphComponent)drawingPane).getGraph().getSelectionCount() != 1) {
                // disable all features
                System.out.println("in");
                miShowInformation.setEnabled(false);
                miShowDetails.setEnabled(false);
                miAddNeighbours.setEnabled(false);
                miShowSuperclasses.setEnabled(false);
                miHideSuperclasses.setEnabled(false);
                miAddSubclasses.setEnabled(false);
                miAddSuperclasses.setEnabled(false);
                miShowSubclasses.setEnabled(false);
                miHideSubclasses.setEnabled(false);
            } else {
                // Test if the selection is a vertex or an edge
                if (((mxCell)((mxGraphComponent)drawingPane).getGraph()
                        .getSelectionCell()).isEdge()) {
                    // Cell is edge, show only miShowDetails
                    miShowInformation.setEnabled(true);
                    miShowDetails.setEnabled(false);
                    miAddNeighbours.setEnabled(false);
                    miShowSuperclasses.setEnabled(false);
                    miHideSuperclasses.setEnabled(false);
                    miAddSubclasses.setEnabled(false);
                    miAddSuperclasses.setEnabled(false);
                    miShowSubclasses.setEnabled(false);
                    miHideSubclasses.setEnabled(false);
                } else {
                    // Cell is vertex, show all items
                    miShowInformation.setEnabled(true);
                    miShowDetails.setEnabled(true);
                    miAddNeighbours.setEnabled(true);
                    miShowSuperclasses.setEnabled(true);
                    miHideSuperclasses.setEnabled(true);
                    miAddSubclasses.setEnabled(true);
                    miAddSuperclasses.setEnabled(true);
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
     * 
     * @return the panel
     */
    protected JComponent createCanvasPanel() {
        mxGraph graph = new mxGraph();
        setGraphSwitches(graph);
        graph.setAllowDanglingEdges(false);
        CustomGraphComponent graphComponent = new CustomGraphComponent(graph);
        graphCanvas = new ISGCIGraphCanvas(this, graph);
        drawingPane = graphComponent;
        drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
        drawingPane.getVerticalScrollBar().setUnitIncrement(100);
        drawingPane.setLocation(100, 100);
        drawingPane.getViewport().setOpaque(false);
        drawingPane.setOpaque(true);
        drawingPane.setBackground(Color.white);
        ((mxGraphComponent)drawingPane).setPanning(true);
        ((mxGraphComponent)drawingPane).setDragEnabled(false);
        ((mxGraphComponent)drawingPane).setToolTips(true);
        return drawingPane;
    }

    private void setGraphSwitches(mxGraph graph) {
        graph.setCellsEditable(false);
        graph.setCellsDisconnectable(false);
        graph.setCellsDeletable(false);
        graph.setCellsCloneable(false);
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
        // does not seem to have any effect
        graph.setMultigraph(false);

    }

    /**
     * Center the canvas on the given point.
     */
    public void centerCanvas(Point p) {
        JViewport viewport = drawingPane.getViewport();
        Dimension port = viewport.getExtentSize();
        Dimension view = viewport.getViewSize();

        p.x -= port.width / 2;
        if (p.x + port.width > view.width)
            p.x = view.width - port.width;
        if (p.x < 0)
            p.x = 0;
        p.y -= port.height / 2;
        if (p.y + port.height > view.height)
            p.y = view.height - port.height;
        if (p.y < 0)
            p.y = 0;
        viewport.setViewPosition(p);
    }

    public void printPort() {
        Rectangle view = getViewport();
        System.err.println("port: " + view);
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
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

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
            // JDialog export = new ExportDialog(this);
            // export.setLocation(50, 50);
            // export.pack();
            // export.setVisible(true);
        } else if (object == miNaming) {
            System.out.println("naming");
            JDialog d = new NamingDialog(this);
            d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
        } else if (object == miSearching) {
            System.out.println("test");
            JDialog search = new SearchDialog(this);
            search.setLocation(50, 50);
            search.setVisible(true);
        } else if (object == miFitInWindow) {
            // fits current shown graph into the window (nearly complete)
            graphCanvas.fitInWindow();
        } else if (object == miAnimation) {
            // sets animation activated or not
            if (graphCanvas.getAnimation()) {
                graphCanvas.setAnimation(false);
            } else {
                graphCanvas.setAnimation(true);
            }

        } else if (object == miUndo) {
            // undo last change in the drawingPane
            undoManager.undo();
            ((mxGraphComponent)drawingPane).getGraph().setSelectionCell(null);
        } else if (object == miRedo) {
            // redo change in the drawingPane
            undoManager.redo();
            ((mxGraphComponent)drawingPane).getGraph().setSelectionCell(null);
        } else if (object == miLayout) {

            graphCanvas.animateGraph();
            ;
            if (!graphCanvas.getAnimation()) {
                ((mxGraphComponent)drawingPane).refresh();
            }
        } else if (object == miSidebar) {
            sidebar.visibilityChanged();
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
            JDialog open = new OpenProblemDialog(this,
                    ((JMenuItem)event.getSource()).getText());
            open.setLocation(50, 50);
            open.setVisible(true);
        } else if (object == miShowInformation) {

            if (((mxCell)((mxGraphComponent)drawingPane).getGraph()
                    .getSelectionCell()).isEdge()) {
                GraphClassSet edgesource = (GraphClassSet)((mxCell)((mxCell)((mxGraphComponent)graphCanvas)
                        .getGraph().getSelectionCell()).getSource()).getValue();
                GraphClassSet edgetarget = (GraphClassSet)((mxCell)((mxCell)((mxGraphComponent)graphCanvas)
                        .getGraph().getSelectionCell()).getTarget()).getValue();
                JDialog d = InclusionResultDialog.newInstance(
                        graphCanvas.getParent(), edgesource.getLabel(),
                        edgetarget.getLabel());
                d.setLocation(50, 50);
                d.pack();
                d.setVisible(true);
            } else {
                JDialog d = new GraphClassInformationDialog(
                        graphCanvas.getParent(), ((GraphClassSet)graphCanvas
                                .getSelectedCell().getValue()).getLabel());
                d.setLocation(50, 50);
                d.pack();
                d.setSize(800, 600);
                d.setVisible(true);
            }
        } else if (object == miShowDetails) {
            sidebar.visibilityChanged();
            graphCanvas.setSidebarConent();
        } else if (object == miAddNeighbours) {
            graphCanvas.drawNeighbours(graphCanvas.getSelectedCell());
        }  else if (object == miShowSuperclasses) {
            graphCanvas.drawSuperSub(graphCanvas.getSuperNodes(graphCanvas
                    .getSelectedCell()));
        } else if (object == miHideSuperclasses) {
            graphCanvas.deleteSuperSub(graphCanvas.getSuperNodes(graphCanvas
                    .getSelectedCell()));
        } else if (object == miShowSubclasses) {
            graphCanvas.drawSuperSub(graphCanvas.getSubNodes(graphCanvas
                    .getSelectedCell()));
        } else if (object == miHideSubclasses) {
            graphCanvas.deleteSuperSub(graphCanvas.getSubNodes(graphCanvas
                    .getSelectedCell()));
        } else if (object == miAddSubclasses){
        	graphCanvas.addSubclasses(graphCanvas.getSelectedCell());
        } else if (object == miAddSuperclasses){
        	graphCanvas.addSuperclasses(graphCanvas.getSelectedCell());
        } else if (object == miAbout) {
            JDialog select = new AboutDialog(this);
            select.setLocation(50, 50);
            select.setVisible(true);
        } else if (object == miHelp) {
            loader.showDocument("help.html");
        } else if (object == miSmallgraphs) {
            loader.showDocument("smallgraphs.html");
        } else if (event.getActionCommand() == "miOpenProblem") {
            JDialog open = new OpenProblemDialog(this,
                    ((JMenuItem)event.getSource()).getText());
            open.setLocation(50, 50);
            open.setVisible(true);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object object = event.getSource();

        if (object == miDrawUnproper) {
            graphCanvas.setDrawUnproper(((JCheckBoxMenuItem)object).getState());
        }
    }

    public void clear() {
        System.out.println("#####################################removing");
        // drawingPane.removeAll();
    }

    /**
     * sets the color (grey) for improper inclusions while using mxIMarker of
     * the Library JGraphX
     * 
     * @author Matthias Miller
     * @date 25.06.2013
     * @annotation sets the color (grey) for improper inclusion while using
     *             mxIMarker of the Library JGraphX
     */

    public void addImproperInclColor() {
        mxIMarker tmp = new mxIMarker() {
            public mxPoint paintMarker(mxGraphics2DCanvas canvas,
                    mxCellState state, String type, mxPoint pe, double nx,
                    double ny, double size, boolean source) {
                Polygon poly = new Polygon();
                poly.addPoint((int)Math.round(pe.getX()),
                        (int)Math.round(pe.getY()));
                poly.addPoint((int)Math.round(pe.getX() - nx - ny / 2),
                        (int)Math.round(pe.getY() - ny + nx / 2));

                if (type.equals("improper")) {
                    poly.addPoint((int)Math.round(pe.getX() - nx * 3 / 4),
                            (int)Math.round(pe.getY() - ny * 3 / 4));
                }

                poly.addPoint((int)Math.round(pe.getX() + ny / 2 - nx),
                        (int)Math.round(pe.getY() - ny - nx / 2));

                if (mxUtils.isTrue(state.getStyle(), (source) ? "startFill"
                        : "endFill", true)) {
                    canvas.fillShape(poly);
                }

                Color gray = new Color(180,180,180);
                canvas.getGraphics().setPaint(gray);
                canvas.fillShape(poly);
                canvas.getGraphics().draw(poly);

                return new mxPoint(-nx, -ny);
            }
        };
        mxMarkerRegistry.registerMarker("improper", tmp);
    }

    public mxUndoManager getUndoM() {
        return undoManager;
    }
}

/* EOF */
