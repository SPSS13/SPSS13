/*
 * Find the relation between induced subgraphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/FindISG.java,v 1.29 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.*;
import java.net.URL;
import java.io.*;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.*;
import gnu.getopt.Getopt;
import teo.isgci.grapht.GAlg;
import teo.isgci.xml.*;
import teo.isgci.util.Itera;

public class FindISG{
    
    private static Vector graphs, families, configurations, grammars;
    private static Hashtable results;
    private static SimpleDirectedGraph<Graph,DefaultEdge> resultGraph;
    
    private static int usg; // Running number for unknown subgraphs
    //private static Annotation<Graph> graphAnn; // Graph in resultGraph
    private static int minCnt=4; // Minimum number of nodes in small graphs
    private static int maxCnt=0; // The largest size of graphs given in the
                                 // beginning
    private static final String USG="USG", ISG="ISG";
    
    static final String XMLDECL =
        "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
        "<!DOCTYPE SMALLGRAPHS SYSTEM \"smallgraphs.dtd\">\n";
    
    private static boolean noComplements = false;  // Don't handle complements

    private static int verbose = 0;

    public static void main(String args[]) throws IOException,
            InterruptedException {
        boolean transitivelyClosed = false;

        long t1,t2,ts=0;
        int c;
        
        graphs = new Vector();
        families = new Vector();
        configurations = new Vector();
        grammars = new Vector();
        results = new Hashtable();
        resultGraph = new SimpleDirectedGraph<Graph,DefaultEdge>(
                DefaultEdge.class);
        usg=0;

        Getopt opts = new Getopt("FindISG", args, "ctv");
        while ((c = opts.getopt()) != -1) {
            switch (c) {
                case 'c':
                    noComplements = true;
                    break;
                case 't':
                    transitivelyClosed = true;
                    break;

                case 'v':
                    verbose++;
                    break;

                default:
                    usage();    /* doesn't return */
                    break;
            }
        }

        /* stimmt Anzahl der verbliebenen Parameter? */
        if (args.length != opts.getOptind() + 2) {
            usage();    /* doesn't return */
        }

        String inxml = args[opts.getOptind()];
        String outxml = args[opts.getOptind()+1];

        if (verbose != 0)
            System.err.print("Lese " + inxml + " ein");

        t1=System.currentTimeMillis();
        try{
            readXMLFile(inxml);
        }catch(Exception ex){
            ex.printStackTrace();//System.err.println(ex);
            System.exit(1);
        }
        t2=System.currentTimeMillis();

        if (verbose != 0) {
            long zeit = t2 - t1;
            ts += zeit;
            System.err.print(". (" + time2String(zeit) + ")\n");

            System.err.print("Graphen         : " + graphs.size() + "\n");
            System.err.print("Familien        : " + families.size() + "\n");
            System.err.print("Konfigurationen : " + configurations.size()
                            + "\n");

            System.err.print("Bestimme Teilgraphen");
        }

        t1=System.currentTimeMillis();
        
        for (int i=0; i<graphs.size(); i++)
            getSubs((Graph)graphs.elementAt(i));
        
        t2=System.currentTimeMillis();

        if (verbose != 0) {
            long zeit = t2 - t1;

            ts += zeit;
            System.err.print(". ("+ time2String(zeit) + ")\n");

            System.err.print("Graphen         : " + graphs.size() + "\n");
            System.err.print("Familien        : " + families.size() + "\n");
            System.err.print("Konfigurationen : " + configurations.size()
                            + "\n");

            System.err.print("Bestimme Teilgraphen der Konfigurationen");
        }

        t1=System.currentTimeMillis();
        for (int i = 0; i < configurations.size(); i++)
            for (int j = 0; j < graphs.size(); j++) {
                Configuration C = (Configuration) configurations.elementAt(i);
                Graph g = (Graph) graphs.elementAt(j);

                if (!g.getName().startsWith("USG") && C.isInducedSubgraph(g)) {
/*                    System.err.print("  XXXX " + g.getName()
                                    + " ist in allen Repräsentanten von "
                                    + C.getName() + " enthalten \n");*/
                    C.addInduced(g);
                }
            }

        t2=System.currentTimeMillis();

        if (verbose != 0) {
            long zeit = t2 - t1;

            ts += zeit;
            System.err.print(". ("+ time2String(zeit) + ")\n");

            System.err.print("Graphen         : " + graphs.size() + "\n");
            System.err.print("Familien        : " + families.size() + "\n");
            System.err.print("Konfigurationen : " + configurations.size()
                            + "\n");

            System.err.print("Sortiere Graphen");
        }

        t1=System.currentTimeMillis();
        sortNum(graphs,0,graphs.size()-1);
        t2=System.currentTimeMillis();

        if (verbose != 0) {
            long zeit = t2 - t1;

            ts += zeit;
            System.err.print(". (" + time2String(zeit) + ")\n");
            System.err.print("Schreibe " + outxml);
        }

        
	makeDigraph();
        System.out.println("Digraph is made. Starting to add big smallmembers");
	addBigSmallmembers();

	if (transitivelyClosed)
            GAlg.transitiveClosure(resultGraph);

        t1=System.currentTimeMillis();
        try{
            makeNewXMLFile(outxml);
        }catch(Exception ioe){
            ioe.printStackTrace();
        }
        t2=System.currentTimeMillis();

        if (verbose != 0) {
            long zeit = t2 - t1;

            ts += zeit;
            System.err.print(". (" + time2String(zeit) + ")\n");
            System.err.print("Total time: " + time2String(ts)+ "\n");
        }
    }

    private static void usage(){
        System.err.println(
            "Usage: FindISG [opts] input.xml out.xml\n"+
            "   -c: don't handle complements\n"+
            "   -t: create transitively closed out.dig\n"+
            "   -v: be verbose\n");
        System.exit(1);
    }

    /* konvertiert eine Zeit in ms in eine Zeichenkette */
    private static String time2String(long zeit)
    {
        /* kleiner eine Sekunde */
        if (zeit < 1500) {
            return zeit + "ms";

        } else if (zeit < 120000) {
            return (zeit + 500)/1000 + "s (" + zeit + "ms)";

        /* im Minutenbereich */
        } else {
            return (zeit + 500)/1000/60 + "m (" + zeit + "ms)";
        }
    }

    
    /** Creates a new XML file */
    public static void makeNewXMLFile(String outFile)
                throws IOException, SAXException{
        SmallGraphWriter sgw=new SmallGraphWriter(new FileWriter(outFile));
        sgw.writeSmallGraphs(XMLDECL, graphs, grammars, families,
                configurations, resultGraph);
    }
   

    /**
     * Create a list of direct subgraphs for each graph and check
     * to which graph these subgraphs are isomorph.
     * If no isomorph graphs are found, add the subgraph to the list
     * of graphs.
     */
    public static void getSubs(Graph graph)
    {
        /* /null/ ist kein Graph und Bottom-Graph auch ignorieren */
        if (graph == null || graph.getBottom())
            return;

/*        System.out.println("bestimme Teilgraphen von " + graph.getName()
                + " (" + graph.countNodes() + " Knoten)"); */
        
        int i,j,cnt=graph.countNodes();
        // dont check graphs with 3 or less nodes
        if(cnt<minCnt) return;
        
        Graph g1,g2;
        Vector candidate=new Vector(),result=new Vector();
        boolean found;
        
        // since vector names is sorted by number of nodes ...
        /* XXX was bitteschön ist sortiert????? */
        for(i=graphs.size()-1;i>=0;i--){
            g1=(Graph)graphs.elementAt(i);
            if (!g1.getBottom() && g1.countNodes() == cnt - 1
                    && g1.countEdges() <= graph.countEdges())
                candidate.addElement(g1);
        }
        
        g2=null; // must be initialized due to javac
        for(i=0;i<cnt;i++){
            g1=new Graph(graph);
            g1.delNode(i);
            found=false;
            for(j=candidate.size()-1;j>=0;j--){
                g2=(Graph)candidate.elementAt(j);
                if(g1.isIsomorphic(g2)){
                    found=true;
                    break;
                }
            }
            if(!found){
                addUSG(g1, graphs, USG);
                candidate.addElement(g1);
                g2=g1; // pretend g2 was found in previous loop
            }
            if(result.indexOf(g2)<0)
                result.addElement(g2);
        }

        results.put(graph,result);
    }

    
    /**
     * Reads the graphs from XML format (their names, nodes, edges and aliases
     * and links, if there are any).
     */
    private static void readXMLFile(String file) throws Exception{
        SmallGraphReader handler = new SmallGraphReader();
        int i, j, ci;

        URL url = new File(file).toURI().toURL();
        InputSource input = new InputSource(url.openStream());
        input.setSystemId(url.toString());
        XMLParser xr = new XMLParser(input, handler, null,
                new NoteFilter(SmallGraphTags.EXPL));
        xr.parse();
        
        for (HMTGrammar gram : handler.getGrammars())
            grammars.addElement(gram);

        for (SmallGraph g : handler.getGraphs()) {
            if (noComplements  &&  !g.isPrimary())
                continue;
            if (g instanceof Graph)
                graphs.addElement(g);
            else if (g instanceof Family)
                families.addElement(g);
            else if (g instanceof Configuration)
                configurations.addElement(g);
            else
                System.err.println("Don't know how to handle "+ g.getName());
        }
        
        for (i=0; i<graphs.size(); i++)
            for (j=i+1; j<graphs.size(); j++)
                if (((Graph)graphs.elementAt(i)).
                        isIsomorphic((Graph)graphs.elementAt(j)))
                    System.err.println("Mistake!!! "+
                        ((Graph)graphs.elementAt(i)).getName()+
                        " isomorphic to "+
                        ((Graph)graphs.elementAt(j)).getName());
        
        for (ci = 0; ci < configurations.size(); ci++) {
            Configuration c = (Configuration) configurations.elementAt(ci);

            Vector contained = c.getGraphs();
        /* das ist genau dann der Fall, wenn es zuviele Repräsentanten gibt */
            if (contained == null) {
               System.err.print("Warning: " + c.getName()
                               + " hat zuviele Repraesentanten!!\n");
                continue;
            }
contConf:   for (i=0; i<contained.size(); i++) {
                for (j=0; j<graphs.size(); j++)
                    if (((Graph)contained.elementAt(i)).
                            isIsomorphic((Graph)graphs.elementAt(j))) {
                        c.addContains((Graph)graphs.elementAt(j));
                        continue contConf;
                    }
                ((Graph)contained.elementAt(i)).addLink(c.getLink());
                addUSG((Graph)contained.elementAt(i), graphs, ISG);
                c.addContains((Graph)contained.elementAt(i));
            }
//            configurations.addElement(c);
        }
        
        for (i=0; i<configurations.size(); i++)
            for (j=i+1; j<configurations.size(); j++)
                if (((Configuration)configurations.elementAt(i)).
                        isIsomorphic((Configuration)configurations.
                        elementAt(j)))
                    System.err.println("Mistake!!! "+
                        ((Configuration)configurations.elementAt(i)).getName()+
                        " isomorphic to "+
                        ((Configuration)configurations.elementAt(j)).getName());

        int curCnt = 0;
        for (i=0; i<graphs.size(); i++){
            curCnt = ((Graph)graphs.elementAt(i)).countNodes();
            if (curCnt>maxCnt)
                maxCnt = curCnt;
	    if (curCnt < minCnt)
		minCnt = curCnt;
        }
        
        for (i=0; i<families.size(); i++)
            if ((Family)families.elementAt(i) instanceof HMTFamily)
                if (((HMTFamily)families.elementAt(i)).getGrammar() != null) {
                    HMTFamily fhmt = (HMTFamily)families.elementAt(i);
                    fhmt.initFromGrammar(maxCnt);
                    
                    Vector smMem = fhmt.getSmallmembers();
contFHMT:           for (j=0; j<smMem.size(); j++)
                        if (((Graph)smMem.elementAt(j)).countNodes()<=maxCnt) {
                            for (int k=0; k<graphs.size(); k++)
                                if (((Graph)smMem.elementAt(j)).isIsomorphic(
                                        (Graph)graphs.elementAt(k))) {
                                    smMem.setElementAt(
                                            (Graph)graphs.elementAt(k), j);
                                    continue contFHMT;
                                }
                            ((Graph)smMem.elementAt(j)).addLink(fhmt.getLink());
                            addUSG((Graph)smMem.elementAt(j), graphs, ISG);
                        }
                }
        
        // Completing information about ComplementFamilies which are complement
        // to HMTFamilies with HMT-grammars
        for (i=0; i<families.size(); i++)
            if ((families.elementAt(i) instanceof HMTFamily) &&
                    !((Family)families.elementAt(i)).isPrimary()) {
                HMTFamily fcomp = (HMTFamily)families.elementAt(i);
                HMTFamily fhmt = (HMTFamily)fcomp.getComplement();
                
                //fcomp.smallmembers = new Graph[fhmt.smallmembers.length];
                Vector smMem = fhmt.getSmallmembers();
                Vector compSmMem = new Vector();
                for (j=0; j<smMem.size(); j++)
                    if (((Graph)smMem.elementAt(j)).countNodes()<=maxCnt)
                        compSmMem.addElement(((Graph)smMem.elementAt(j)).
                                getComplement());
                fcomp.setSmallmembers(compSmMem);
/*                Graph[] compSmMem = new Graph[smMem.size()];
                for (j=0; j<smMem.length; j++)
                    if (smMem[j].countNodes()<=maxCnt)
                        compSmMem[j] = (Graph)smMem[j].getComplement();
                fcomp.setSmallmembers(compSmMem);*/
            }
    }
    
    private static void addUSG(Graph g, Vector graphs, String type){
        usg++;
        g.addName(type+usg);
        Graph co = (Graph)g.makeComplement();
        usg++;
        co.addName(type+usg);
        graphs.addElement(g);
        graphs.addElement(co);
    }

    /** Name of Graph TO Number of nodes */
    private static int n2n(Object graph){
        return graph==null ? -1 : ((Graph)graph).countNodes();
    }
    
    /** Sorts graphs by their number of nodes */
    private static void sortNum(Vector vec, int left, int right) {
        Object y;
        int x;
        int i = left;
        int j = right;
        // x is the element for comparison. (pivot)
        x = n2n(vec.elementAt((i+j)>>1));
        while (i <= j) {
            while(n2n(vec.elementAt(i))<x) i++;
            while(n2n(vec.elementAt(j))>x) j--;
            if (i <= j) {
                // Swap vec.elementAt(i) and vec.elementAt(j)
                y = vec.elementAt(i);
                vec.setElementAt(vec.elementAt(j),i);
                vec.setElementAt(y,j);
                i++;
                j--;
            }
        }
        if(left < j) sortNum(vec, left, j);
        if(i < right) sortNum(vec, i, right);
    }
    
    public static void makeDigraph() {
	// Creating a Node for every graph
	for (int i=0; i<graphs.size(); i++)
            resultGraph.addVertex((Graph) graphs.elementAt(i));
	
	// Creating Edges from graphs to their induced subgraphs
        for (Graph v : resultGraph.vertexSet()) {
	    Vector<Graph>subs = (Vector<Graph>) results.get(v);
	    if (subs == null)
                continue;
            for (Graph vSub: subs)
                resultGraph.addEdge(v, vSub);
        }
	
	GAlg.transitiveClosure(resultGraph);
	
	// Removing Nodes of graphs with "USG" in the name
        ArrayList<Graph> remove = new ArrayList<Graph>();
        for (Graph v : resultGraph.vertexSet()) {
            if (v.getName().startsWith(USG))
                remove.add(v);
        }
        resultGraph.removeAllVertices(remove);
	
	GAlg.transitiveReduction(resultGraph);
    }
    
    public static void addBigSmallmembers() throws
            IOException, InterruptedException {
	
	Vector bigSmallmemb = new Vector();// Contains graphs of size larger
                                           // than maxCnt
	for (int i=0; i<families.size(); i++)
            if (families.elementAt(i) instanceof HMTFamily)
                if (((HMTFamily)families.elementAt(i)).getGrammar() != null) {
                    HMTFamily fhmt = (HMTFamily)families.elementAt(i);
                    HMTFamily fcomp = (HMTFamily)fhmt.getComplement();
                    Vector smMem = fhmt.getSmallmembers();
                    Vector compSmMem = new Vector();
contBig:            for (int j=0; j<smMem.size(); j++)
                        if (((Graph)smMem.elementAt(j)).countNodes() > maxCnt) {
                            for (int k=0; k<bigSmallmemb.size(); k++)
                                if (((Graph)smMem.elementAt(j)).countNodes() ==
                                        ((Graph)bigSmallmemb.elementAt(k)).
                                        countNodes() &&
                                        isSubgraphVF((Graph)smMem.elementAt(j),
                                        (Graph)bigSmallmemb.elementAt(k))) {
                                    smMem.setElementAt((Graph)bigSmallmemb.
                                            elementAt(k), j);
                                    compSmMem.addElement((Graph)
                                            ((Graph)bigSmallmemb.elementAt(k)).
                                            getComplement());
                                    continue contBig;
                                }
                            ((Graph)smMem.elementAt(j)).addLink(fhmt.getLink());
                            addUSG((Graph)smMem.elementAt(j), bigSmallmemb,ISG);
                            compSmMem.addElement((Graph)((Graph)smMem.
                                    elementAt(j)).getComplement());
/*                            System.out.println(fhmt.getName()+".smMem["+j+"]="+
                                    ((Graph)smMem.elementAt(j)).getName());
                            System.out.println(fcomp.getName()+".smMem["+j+"]="+
                                   ((Graph)compSmMem.elementAt(j)).getName());*/
                        }
                        else
                            compSmMem.addElement((Graph)
                                    fcomp.getSmallmembers().elementAt(j));
                    ((HMTFamily)fcomp).setSmallmembers(compSmMem);
                }

        ArrayList<Graph> topo = new ArrayList<Graph>();
        for (Graph v : GAlg.topologicalOrder(resultGraph))
            topo.add(v);

        // For testing
        // Not converted to jgrapht!
/*        for (int j=0; j<topo.size(); j++)
            for (int k=0; k<topo.size(); k++) {
		if (j == k)
		    continue;
                if (resultGraph.getPath((Node)topo.elementAt(j),
                        (Node)topo.elementAt(k)).size() == 0) {
                    if (isSubgraphVF(
                            (Graph) ((Node)topo.elementAt(j)).getData(grIndex),
                            (Graph) ((Node)topo.elementAt(k)).getData(grIndex)))
                    {    
                        System.out.println("VF found: "+
                                ((Graph)((Node)topo.elementAt(k)).
                                getData(grIndex)).getName()+" ind.s.g. of "+
                                ((Graph)((Node)topo.elementAt(j)).
                                getData(grIndex)).getName());
                        if (((Graph)((Node)topo.elementAt(k)).
                                getData(grIndex)).getName().startsWith(ISG))
                            System.out.println(((Graph)((Node)topo.elementAt(k)).
                                getData(grIndex)).toString());
                        if (((Graph)((Node)topo.elementAt(j)).
                                getData(grIndex)).getName().startsWith(ISG))
                            System.out.println(((Graph)((Node)topo.elementAt(j)).
                                getData(grIndex)).toString());
                    }
                } else if (!isSubgraphVF(
                        (Graph) ((Node)topo.elementAt(j)).getData(grIndex),
                        (Graph) ((Node)topo.elementAt(k)).getData(grIndex)))
                {
                    System.out.println("getPath found: "+
                            ((Graph)((Node)topo.elementAt(k)).
                            getData(grIndex)).getName()+" ind.s.g. of "+
                            ((Graph)((Node)topo.elementAt(j)).
                            getData(grIndex)).getName());
                    if (((Graph)((Node)topo.elementAt(k)).
                            getData(grIndex)).getName().startsWith(ISG))
                        System.out.println(((Graph)((Node)topo.elementAt(k)).
                            getData(grIndex)).toString());
                    if (((Graph)((Node)topo.elementAt(j)).
                            getData(grIndex)).getName().startsWith(ISG))
                        System.out.println(((Graph)((Node)topo.elementAt(j)).
                            getData(grIndex)).toString());
                }
            }*/

        System.out.println("All big smallmembers are added.");
        
        for (int i=0; i<bigSmallmemb.size(); i++) {
            Graph bigGr = (Graph)bigSmallmemb.elementAt(i);
            resultGraph.addVertex(bigGr);
            
            for (Graph v : topo) {
                if (GAlg.getPath(resultGraph, bigGr, v) == null)
                    if (isSubgraphVF(bigGr, v))
                        resultGraph.addEdge(bigGr, v);
            }
        }
        
        for (int i=0; i<bigSmallmemb.size(); i++)
            graphs.addElement((Graph)bigSmallmemb.elementAt(i));
    }
    
    /**
     * Use VFLib2 to decide whether small is an induced subgraph of large.
     */
    public static boolean isSubgraphVF(Graph large, Graph small) throws
            IOException, InterruptedException {
        int i, j, n;

        Process p = Runtime.getRuntime().exec("vf");
        BufferedReader in =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        PrintWriter out = new PrintWriter(p.getOutputStream());

        n = small.countNodes();
        out.println(n);
        for (i = 0; i < n-1; i++)
            for (j = i+1; j < n; j++)
                if (small.getEdge(i,j))
                    out.println(i +" "+ j);
        out.println(-1);

        n = large.countNodes();
        out.println(n);
        for (i = 0; i < n-1; i++)
            for (j = i+1; j < n; j++)
                if (large.getEdge(i,j))
                    out.println(i +" "+ j);
        out.println(-1);

        out.close();
        n = Integer.parseInt(in.readLine());
        p.waitFor();
        in.close();
        p.getErrorStream().close();
        if (p.exitValue() != 0  ||  n != 0  &&  n != 1) {
            System.err.println("VFLib Error testing "+ small.getName()
                +" and "+ large.getName());
            return false;       // So that we can check more relations
        }
        // This is just for debugging
//        System.out.print(small.getName());
//        System.out.print(n == 1 ? " << " : " </< ");
//        System.out.print(large.getName());

        return n == 1;
    }
}

/* EOF */
