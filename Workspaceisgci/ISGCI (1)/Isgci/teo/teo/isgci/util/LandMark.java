/*
 * Find trivial inclusions, generate node info.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/LandMark.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import javax.imageio.ImageIO;
import org.xml.sax.InputSource;
import gnu.getopt.Getopt;
import org.jgrapht.graph.SimpleDirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.gui.*;
import teo.isgci.xml.*;
import teo.isgci.gc.*;
import teo.isgci.db.*;
import teo.isgci.problem.*;
import teo.Loader;

public class LandMark {
    /** Where we check for relations */
    static SimpleDirectedGraph<GraphClass,Inclusion> thegraph;

    /** Creating the map images is done as part of the application */
    ISGCIMainFrame parent;
    /** The landmark classes. The first element is to be replaced by the
     * class to be landmarked */
    List<GraphClass> landmarks;
    /** Where to store the maps */
    String path;

    public LandMark(ISGCIMainFrame parent) {
        this.parent = parent;
        landmarks = findLandmarks();
        path = System.getProperty("org.isgci.mappath");
    }

    /**
     * Fill the landmarks array with the proper nodes.
     */
    protected List<GraphClass> findLandmarks() {
        List<GraphClass> landmarks = new ArrayList<GraphClass>();
        landmarks.add(null);
        landmarks.add(DataSet.getClass("perfect"));
        landmarks.add(DataSet.getClass("Meyniel"));
        landmarks.add(DataSet.getClass("threshold"));
        landmarks.add(DataSet.getClass("tree"));
        landmarks.add(DataSet.getClass("cograph"));
        landmarks.add(DataSet.getClass("clique"));
        landmarks.add(DataSet.getClass("proper interval"));
        landmarks.add(DataSet.getClass("even-hole--free"));
        return landmarks;
    }


    /**
     * Export the map for gc_x as gc_x.png.
     */
    public void createMap(GraphClass n) {
        Dimension dim;
        BufferedImage image;

        landmarks.set(0, n);

        //---- Create the map
        parent.graphCanvas.drawHierarchy(landmarks);
        // reverse order so n is last in case it is one of the landmarks
        for (int i = landmarks.size()-1; i >= 0; i--) {
            GraphClass gc = landmarks.get(i);
            NodeView v = parent.graphCanvas.findNode(gc);
            v.setNameAndLabel(gc.toString());
            v.setMark(i == 0);
        }
        parent.graphCanvas.updateBounds();


        //---- Export it as png
        dim = parent.graphCanvas.getPreferredSize();
        image = new BufferedImage(dim.width, dim.height,
                BufferedImage.TYPE_INT_ARGB);
        parent.graphCanvas.forcePaint(image.getGraphics());
        try {
            ImageIO.write(image, "png",
                    new File(path +"/"+ n.getID() +".png"));
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    public void createMaps() {
        //createMap(DataSet.getNode("(2K_2,A,H)--free"));

        for (GraphClass n : DataSet.getClasses()) {
            try {
                createMap(n);
            } catch (Exception e) {
                System.err.println(n.toString());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    

    /**
     * Print node/edge count statistics of the given graph.
     */
    public static void show(SimpleDirectedGraph dg){
        System.err.print("Nodes: "+ dg.vertexSet().size());
        System.err.println("     Edges: "+ dg.vertexSet().size());
    }


    /**
     * Load the ISGCI database.
     */
    public static void load(String file,
            SimpleDirectedGraph<GraphClass,Inclusion> graph, Vector problems)
            throws java.net.MalformedURLException {
        Loader loader = new Loader("file:"+System.getProperty("user.dir")+"/",
                true);
        ISGCIReader gcr = new ISGCIReader(graph, problems);
        XMLParser xml=new XMLParser(loader.openInputSource(file),
                gcr, loader.new Resolver(), new NoteFilter());
        xml.parse();
    }


    /**
     * Split the given graph on the given graphclass and return the results
     * [g \cap supers, g \cap subs, g \cap others].
     */
    /*FIXME port to jgrapht
    private static ArrayList<ISGCIGraph> split(ISGCIGraph g,
            Vector supers, Vector subs) {
        ISGCIGraph.ISGCINode n;
        Enumeration eenum;
        ArrayList<ISGCIGraph> res = new ArrayList<ISGCIGraph>(3);

        Vector gsupers = new Vector();
        Vector gsubs = new Vector();
        Vector grest = new Vector();

        eenum = g.getNodes();
        while (eenum.hasMoreElements()) {
            n = (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (supers.contains(n.getUltimateParent()))
                gsupers.addElement(n);
            else if (subs.contains(n.getUltimateParent()))
                gsubs.addElement(n);
            else
                grest.addElement(n);
        }

        res.add(null);
        res.add(null);
        res.add(null);
        res.set(0, (ISGCIGraph) g.createSubgraph(gsupers.elements()));
        res.set(1, (ISGCIGraph) g.createSubgraph(gsubs.elements()));
        res.set(2, (ISGCIGraph) g.createSubgraph(grest.elements()));
        return res;
    }*/


    /**
     * Split the given graphs on the given graphclass and return the results.
     */
    /*FIXME port to jgrapht
    private static ArrayList<ISGCIGraph> split(String gc,
            ArrayList<ISGCIGraph> graphs) {
        ArrayList<ISGCIGraph> res = new ArrayList<ISGCIGraph>();

        //---- Find super/subs of splitter
        ISGCIGraph.ISGCINode splitter = null, n;
        Enumeration eenum = thegraph.getNodes();

        while (eenum.hasMoreElements()) {
            splitter = (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (splitter.getName().equals(gc))
                break;
        }

        Vector supers = splitter.superNodes();
        Vector subs = splitter.subNodes();

        for (ISGCIGraph g: graphs) {
            ArrayList<ISGCIGraph> gs = split(g, supers, subs);
            for (int i = 0; i < 3; i++)
                if (gs.get(i) != null  &&  gs.get(i).countNodes() > 20)
                    res.add(gs.get(i));
        }

        return res;
    }*/


    /**
     * Print number of super/subclasses of the classes in g.
     */
    /*FIXME port to jgrapht
    private static void showClasses(ISGCIGraph g) {
        Enumeration eenum = thegraph.getNodes();
        while (eenum.hasMoreElements()) {
            ISGCIGraph.ISGCINode n= (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (n.getGraphClass().getClass() != BaseClass.class)
                continue;

            Vector supers = n.superNodes();
            Vector subs = n.subNodes();
            int supn = 0, subn = 0, restn = 0;

            Enumeration gnodes = g.getNodes();
            while (gnodes.hasMoreElements()) {
                ISGCIGraph.ISGCINode gn =
                        (ISGCIGraph.ISGCINode) gnodes.nextElement();
                if (supers.contains(gn.getUltimateParent()))
                    supn++;
                else if (subs.contains(gn.getUltimateParent()))
                    subn++;
                else
                    restn++;
            }

            System.out.print(restn);
            System.out.print("\t");
            System.out.print(supn);
            System.out.print("\t");
            System.out.print(subn);
            System.out.print("\t");
            System.out.print(n);
            System.out.print("\t");
            System.out.print(n.getGraphClass());
            System.out.println("");
        }
    }

    /**
     * Main
     */
    /*FIXME port to jgrapht
    public static void main(String args[]) throws Exception {
        int i;

        Getopt opts = new Getopt("LandMark", args, "h");
        opts.setOpterr(false);
        while ((i = opts.getopt()) != -1) {
            switch (i) {
                case '?':
                case 'h':
                    usage();
                    System.exit(1);
            }
        }

        //---- Load everything
        thegraph = new ISGCIGraph();
        Vector problems = new Vector();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        ArrayList<ISGCIGraph> graphs = new ArrayList<ISGCIGraph>();

        load(args[opts.getOptind()], thegraph, problems);
        show(thegraph);
        thegraph.transitiveClosure();
        graphs.add(thegraph);

        //--- split repeatedly
        while (true) {
            String classname =  in.readLine();
            int num = -1;

            try {
                num = Integer.decode(classname);
            } catch (NumberFormatException e) {}

            if (num == -1)
                graphs = split(classname, graphs);
            else
                showClasses(graphs.get(num));

            for (ISGCIGraph g : graphs) {
                System.out.print(g == null ? 0 : g.countNodes());
                System.out.print(" ");
            }
            System.out.println("");
            System.out.println("");
        }

    }


    private static void usage() {
        System.out.println("Usage: java LandMark input.xml "+
                "");
    }
    */
}
