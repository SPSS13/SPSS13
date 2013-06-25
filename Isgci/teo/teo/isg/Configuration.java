/*
 * Represents a configuration of graphs
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/Configuration.java,v 1.11 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class Configuration extends SmallGraph{
    private int matrix[][];
    private int cnt;  // number of nodes in Configuration
    
    final static int EDGE = 1;
    final static int NONEDGE = -1;
    final static int OPTEDGE = 0;
    final static int UNKNOWN = 2;   /* XXX was soll das? */

    /** Graphs contained in Configuration */
    private Vector<SmallGraph> contains;
    
    public Configuration(){
        this(0);
    }
    
    public Configuration(int n){
        super();
        contains = null;
        addNodesCount(n);
    }
    

    public void copyFromComplement() {
        int i, j;

        super.copyFromComplement();
        //---- First copy the definition
        Configuration c = (Configuration) complement;
        cnt = c.cnt;
        matrix = new int[cnt][cnt];
        for (i=0; i<cnt; i++)
            for (j=0; j<cnt; j++)
                matrix[i][j] = c.matrix[i][j];
        contains = (c.contains != null) ? (Vector) c.contains.clone() : null;

        //--- Then complement it
        for(i=0; i<cnt; i++)
            for(j=0; j<cnt; j++)
                if(i!=j) matrix[i][j] = -matrix[i][j];
        if (contains != null) {
            Vector<SmallGraph> newContains = new Vector<SmallGraph>();
            for (SmallGraph g : contains)
                newContains.add(g.getComplement());
            contains = newContains;
        }
    }
    
    /*public SmallGraph makeComplement() {
        Configuration c = new Configuration(this);
        c.complement();
        setComplement(c);
        c.setComplement(this);
        return c;
    }*/
    
    /* Erzeugt eine von /c/ induzierte Konfiguration. Welche Knoten enthalten
     * sind gibt /mask/ an. */
    public Configuration(Configuration c, boolean mask[]){
        super();

        int i;

        cnt = 0;
        for (i = 0; i < c.cnt; i++)
            if (mask[i])
                cnt++;

        matrix = new int[cnt][cnt];

        int j, k, l;

        k = -1;
        for (i = 0; i < c.cnt; i++) {
            if (mask[i])
                k++;
            else
                continue;

            l = -1;
            for (j = 0; j < c.cnt; j++) {
                if (mask[j])
                    l++;
                else
                    continue;

                matrix[k][l] = c.matrix[i][j];
            }
        }

        contains = null;
        induced = null;
        link = null;
    }

    public void addNodesCount(int n){
        cnt = n;
        matrix = new int[cnt][cnt];
        for (int i=0; i<cnt; i++)
            for (int j=0; j<cnt; j++)
                matrix[i][j] = i==j ? NONEDGE : UNKNOWN;
        contains = null;
    }
    
    /** Counts the nodes in this Configuration. */
    public int countNodes(){
        return cnt;
    }
    
    /** 
     * Adds an edge to the Configuration.
     * <tt>type</tt> shows which edge should be added.
     */
    private void addEdge(int a, int b, int type){
        if (type != EDGE && type != NONEDGE && type != OPTEDGE) {
            System.out.println("Wrong type of edge!");
            return;
        }
        if (a == b) return;
        if (a<0 || b<0 || a>=cnt || b>=cnt) return;
        if (matrix[a][b]==type || matrix[b][a]==type) {
            String s = new String();
            if (type == EDGE)
                s = "Edge";
            else if (type == NONEDGE)
                s = "Nonedge";
            else if (type == OPTEDGE)
                s = "Optedge";
            System.err.println(getName() +":"+ s +" \""+ a +"-"+ b +
                    "\" already exists!");
        }
        matrix[a][b] = type;
        matrix[b][a] = type;
    }
    
    /** Adds an edge to Configuration */
    public void addEdge(int a, int b){
        addEdge(a, b, EDGE);
    }
    
    /** Adds an nonedge to Configuration */
    public void addNonedge(int a, int b){
        addEdge(a, b, NONEDGE);
    }
    
    /** Adds an optedge to Configuration */
    public void addOptedge(int a, int b){
        addEdge(a, b, OPTEDGE);
    }
    
    public int getEdge(int a, int b){
        return matrix[a][b];
    }
    
    /** Counts the edges of type <tt>type</tt> in Configuration */
    public int countEdges(int type){
        int i, j, n=0;
        for (i=0; i<cnt; i++)
            for (j=0; j<i; j++)
                if (matrix[i][j] == type)
                    n++;
        return n;
    }
    
    /** Counts the edges in Configuration */
    public int countEdges(){
        return countEdges(EDGE);
    }

    /** Counts the nonedges in Configuration */
    public int countNonedges(){
        return countEdges(NONEDGE);
    }

    /** Counts the optedges in Configuration */
    public int countOptedges(){
        return countEdges(OPTEDGE);
    }

    /** 
     * Returns the degree of type <tt>type</tt> of the node at index
     * <tt>v</tt>.
     */
    public int degree(int v, int type){
        if (v<0 || v>=cnt) return -1; // illegal argument
        int i, n=0;
        for(i=0; i<cnt; i++){
            if (matrix[v][i] == type) n++;
            // matrix[v][v] is always a NONEDGE
        }
        return n;
    }

    /** 
     * Returns the degree of type <tt>type</tt> of the node at index
     * <tt>v</tt> in the subgraph induced by <tt>mask<tt>.
     */
    public int degree(int v, int type, boolean mask[]){
        if (v<0 || v>=cnt || !mask[v]) return -1; // illegal argument
        int i, n=0;
        for(i=0; i<cnt; i++){
            if (matrix[v][i] == type && mask[i]) n++;
            // matrix[v][v] is always a NONEDGE
        }
        return n;
    }

    /** Returns the degree of the node at index <tt>v</tt> */
    public int degree(int v){
        return degree(v, EDGE);
    }

    /** Returns the degree of the node at index <tt>v</tt> in the subgraph
     * induced by <tt>mask<tt>. */
    public int degree(int v, boolean mask[]){
        return degree(v, EDGE, mask);
    }

    /** Returns the optDegree of the node at index <tt>v</tt> */
    public int optDegree(int v){
        return degree(v, OPTEDGE);
    }

    /** Returns the optDegree of the node at index <tt>v</tt> in the subgraph
     * induced by <tt>mask<tt> */
    public int optDegree(int v, boolean mask[]){
        return degree(v, OPTEDGE, mask);
    }
    
    /** Adds contains <tt>elt</tt> to Configuration */
    public void addContains(Graph elt){
        if (contains == null)
            contains = new Vector<SmallGraph>(2,2);
        if (!contains.contains(elt))
            contains.addElement(elt);
    }
    
    /** Returns Vector contains */
    public Vector<SmallGraph> getContains(){
        return contains;
    }


    public String toString(){
        if(cnt == 0)
            return "";

        int i,j;
        String s="{"+String.valueOf(cnt)+"} ";
        s += namesToString() + " ";
        for(i=0;i<cnt;i++)
            for(j=0;j<i;j++)
                if(matrix[i][j] == EDGE)
                    s+=(j+" - "+i+"; ");
                else if(matrix[i][j] == OPTEDGE)
                    s+=(j+" = "+i+"; ");
        return s;
    }

    public void addInduced(Graph g){
        Vector<SmallGraph> innerVec = new Vector<SmallGraph>();
        innerVec.addElement(g);
        this.addInduced(innerVec);
    }


    /* Helpfunction for getGraphs() */
    private int getSmallerNumber(int num, Vector T, int len){
        for (int i = 0; i < T.size(); i++) {
            int trafo[] = (int []) T.elementAt(i);

            int zahl = 0;
            for (int j = 0; j < len; j++)
                if ((num & (1 << j)) != 0)
                    zahl |= (1 << trafo[j]);

            if (zahl < num)
                return zahl;
        }

        return num;
    }

    public Vector<Graph> getGraphs(){
        return getGraphs(100);
    }

    /** Calculates Graphs from contains of Configuration */
    public Vector<Graph> getGraphs(int maxGraphs){
        final boolean DEBUG = false; /* auf true setzen für Debug-Ausgaben */
        final int maxOptEdges = 30;

        int optEdges[][] = new int[maxOptEdges][2];// x=[][0], y=[][1]
        int i, j, cntOpt = 0, power2n;
        Vector<Graph> confGraphs = new Vector<Graph>();

        long ta = 0, te;

        if (DEBUG) {
            System.out.print("Name: " + getName() + "\n");
            System.out.print("  " + this.toString() + "\n");
            System.out.print("  Knoten: " + cnt + "\n");
            ta = System.currentTimeMillis();
        }

        for (i=0; i<cnt-1; i++)
            for (j=i+1; j<cnt; j++)
                if (matrix[i][j] == OPTEDGE) {
                    if (cntOpt == maxOptEdges){
                        if (DEBUG)
                            System.out.print("  mehr als " + maxOptEdges
                                    + " optionale Kanten\n\n");
                        return null;
                    }
                    optEdges[cntOpt][0] = i;
                    optEdges[cntOpt++][1] = j;
                }

        if (DEBUG) {
            System.out.print("  optionale Kanten: " + cntOpt + "\n");
            for (i=0; i < cntOpt; i++)
                System.out.print("    " + i + ": " + optEdges[i][0]
                                + " = " + optEdges[i][1] + "\n");
        }

        Vector Transformationen = new Vector();

        /* es gibt tatsächlich eine Konfiguration ohne optionale Kanten... */
        if (cntOpt > 1) {
            Vector automorph = getAutomorphisms();

            if (DEBUG)
                System.out.print("  Automorphismen: " + automorph.size() +"\n");

            for (i = 0; i < automorph.size(); i++) {
                int p[] = (int []) automorph.elementAt(i);

                int trafo[] = new int[cntOpt];

                if (DEBUG) {
                    System.out.print("    (");
                    for (j = 0; j < cnt; j++)
                        System.out.print(" " + p[j] + " ");
                    System.out.print(")  ---> ");
                }

loop:           for (j=0; j < cntOpt; j++) {
                    int x_a = optEdges[j][0];
                    int y_a = optEdges[j][1];
                    int x_b = p[x_a];
                    int y_b = p[y_a];

                    /* Suche Kante x_b - y_b in optEdges */
                    for (int k = 0; k < cntOpt; k++) {
                        if (optEdges[k][0] == x_b && optEdges[k][1] == y_b
                                || optEdges[k][1] == x_b
                                && optEdges[k][0] == y_b) {
                            trafo[j]=k;
                            continue loop;
                        }
                    }

                    System.err.print("Denkfehler in Configuration."
                                    + "getGraphs()!!!\n");
                }

                /* Die Permutation /trafo/ noch invertieren */
                int transformation[] = new int [cntOpt];
                for (j = 0; j < cntOpt; j++)
                    transformation[trafo[j]] = j;

                if (DEBUG){
                    System.out.print("Trafo:  (");
                    for (j=0; j < cntOpt; j++)
                        System.out.print(" " + transformation[j] + " ");
                    System.out.print(")");
                }

                /* alle außer identische Trafo zu /Transforamtionen/
                 * hinzufügen */
                for (j = 0; j < cntOpt; j++)
                    if (transformation[j] != j) {
                        Transformationen.addElement(transformation);

                        if (DEBUG)
                            System.out.print(" X");

                        break;
                    }

                if (DEBUG)
                        System.out.print("\n");
            }
        }        /* if (cntOpt > 1) */

        if (DEBUG)
            System.out.print("  Kantenabbildungen: "
                            + Transformationen.size() + "\n");


        if (DEBUG) {
            te = System.currentTimeMillis();
            System.out.print("  Zeit bisher: " + (te - ta)/10 + "ms\n");
        }

        power2n = 1 << cntOpt;

        // Creating a graph with edges equal to edges of Configuration
        Graph vorlage = new Graph(cnt);
        for (i=0; i<cnt; i++)
            for (j=i+1; j<cnt; j++)
                if (matrix[i][j] == EDGE)
                    vorlage.addEdge(i, j);

        int zaehler = 0;

        /* jetzt Repräsentanten erzeugen */
loop:   for (i=0; i < power2n; i++) {
            /* Isomorph zu bereits erzeugten Repräsentantnen? */
            if (getSmallerNumber(i, Transformationen, cntOpt) < i)
                continue loop;

            zaehler++;

            /* neuen Repräsentanten konstruieren */
            Graph rep = new Graph(vorlage);
            for (j = cntOpt - 1; j >= 0; j--)
                if ((i & (1 << j)) != 0)
                    rep.addEdge(optEdges[j][0], optEdges[j][1]);

            /* ueberpruefen, ob neuer Repraesentant isomorph zu bereits in
             * Liste enthaltenen Repraesentanten ist. Der obige Test deckt
             * nicht alle Fälle ab.*/
            for (j = 0; j < confGraphs.size(); j++)
               if (confGraphs.elementAt(j).isIsomorphic(rep))
                   continue loop;        /* ein goto */

            /* was neues */
            confGraphs.addElement(rep);

            if (confGraphs.size() > maxGraphs) {
               /* zuviele, bottom-Graph erzeugen und raus */
               /*rep.setBottom();
               System.err.print("Erzeuge Bottom-Graph\n");
               break;*/

                /* zuviele, leere Liste zurückliefern */

                if (DEBUG) {
                    te = System.currentTimeMillis();
                     System.out.print("  Repräsentanten: >" + maxGraphs
                                     + " von " + power2n + "\n");
                     System.out.print("  Zeit: " + (te - ta)/10 + "ms\n\n");
                }
                return null;
            }
        }


        if (DEBUG) {
            te = System.currentTimeMillis();
            System.out.print("  Repräsentanten: " + confGraphs.size()
                            + " (" + zaehler + ") von " + power2n + "\n");
            System.out.print("  Zeit: " + (te - ta)/10 + "ms\n\n");
        }

        return confGraphs;
    }

    public Vector getAutomorphisms(){
        final boolean DEBUG = false;

        int i, j;
        boolean darf[][] = new boolean[cnt][cnt];
        boolean mask[][] = new boolean[2][cnt];
        Vector ret = new Vector();

        for (i = 0; i < cnt; i++) {
            mask[0][i] = true;
            mask[1][i] = true;
            for (j = 0; j < cnt; j++)
                    darf[i][j] = true;
        }

        isIsomorphicIntern(this, darf, mask); /* rückgabe egal, da immer */
        /* zu sich selbst isomorph */

        Permutation perm = new Permutation(cnt,darf);
        do {
            int p[] = perm.get();
            if (check(this, p))
            ret.addElement(p);
        } while (perm.next());

        if (DEBUG) {
            System.err.print(" getAutomorphisms(" + getName() + ")\n");
            System.err.print("  bijektive Funktionen: " + perm.count() + "\n");
            System.err.print("  Automorphismen: " + ret.size() + "\n");
        }

        return ret;
    }

    public int countAutomorphisms(){
        return getAutomorphisms().size();
    }

    public boolean isIsomorphic(Configuration c){
        /* check if numbers of nodex, Edges and non-Edges are equal */
        if (cnt!=c.cnt || countEdges()!=c.countEdges() ||
                          countNonedges()!=c.countNonedges())
            return false;

        int i, j;
        boolean darf[][] = new boolean[cnt][cnt];
        boolean mask[][] = new boolean[2][cnt];

        for (i = 0; i < cnt; i++) {
            mask[0][i] = true;
            mask[1][i] = true;
            for (j = 0; j < cnt; j++)
                    darf[i][j] = true;
        }

        if (!isIsomorphicIntern(c, darf, mask))
                return false;

        Permutation perm = new Permutation(cnt,darf);
        do {
            if (check(c, perm.get())) return true;
        } while (perm.next());

        return false;
    }

    /** Hilfsfunktion zur Bestimmung von <tt>darf<tt>.
     * In diesen Fall nicht-rekursiv, da der Aufwand dafür zu groß ist. Die
     * Matrix <tt>darf</tt> enthält an der Stell i,j eine 1, wenn der Knoten i
     * in <tt>this<tt> den gleichen Grad hat wie der Knoten j in <tt>g<tt>. In
     * mask ist hierbei gespeichert welcher Teil des Graphen bearbeitet werden
     * soll. In mask[0] beschreibt hierbei <tt>this<tt> und mask[1] <tt>g<tt>.
     */
    private boolean isIsomorphicIntern(
            Configuration c,
            boolean darf[][],
            boolean mask[][]){
        int i, j;
        int Grad[][] = new int[2][cnt];
        int OptGrad[][] = new int[2][cnt];
        boolean changed = false;

        /* Speichere in Grad[0][i] den Grad des Knoten i in der Configuration
         * this Analog für die Configuration c wenn der Index 1 gewählt ist.
         * Das ganze aber nur dann, wenn in der jeweiligen Maske das Flag für
         * den entsprechenden Knoten gesetzt ist. Nebenbei wird ermittelt, ob
         * die Gradzahlen in beiden Graphen übereinsstimmen.*/
        for (i=0; i < cnt; i++) {
            int h;

            if (mask[0][i]) {
                Grad[0][i] = degree(i,mask[0]);
                OptGrad[0][i] = optDegree(i,mask[0]);
            }

            if (mask[1][i]) {
                Grad[1][i] = c.degree(i,mask[1]);
                OptGrad[1][i] = c.optDegree(i,mask[1]);
            }
        }

        /* jetzt darf[][] bearbeiten */
        for (i=0; i < cnt; i++) {
            if (!mask[0][i])
                continue;

            for (j=0; j < cnt; j++) {
                if (!mask[1][j])
                    continue;

                if ((Grad[0][i] != Grad[1][j]
                    || OptGrad[0][i] != OptGrad[1][j])
                    && darf[i][j]) {
                    changed = true;
                    darf[i][j] = false;
                }
            }
        }

        if (!changed)
            return true;       /* nix passiert */

        /* untersuchen, ob es Knoten gibt, für die es keine
         * Positionsmöglichkeiten gibt */
        for (i = 0; i < cnt; i++) {
            if (mask[0][i]) { /* nur wenn in diesen Durchlauf eventuell */
                    /*angefaßt */
                boolean ziel = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[i][j])
                        ziel = true;;
                }
                if (! ziel) 
                    return false;
            }
        }

        /* untersuchen, ob es Knoten gibt, für die es keine Quelle gibt */
        for (i = 0; i < cnt; i++) {
            if (mask[1][i]) { /* nur wenn in diesen Durchlauf eventuell */
                    /* angefaßt */
                boolean quelle = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[j][i])
                        quelle = true;;
                }
                if (! quelle)
                    return false;
            }
        }

        return true;
    }

    /* Wenn /true/ ist /g/ in allen Repräsentanten der Konfiguration enthalten.
     * Wenn /false/ ist dies entweder nicht der Fall, oder es konnte mit den
     * hier verwendeten einfachen Tests nicht ermittelt werden */
    public boolean isInducedSubgraph(Graph g){
        /* check if /g/ is nonempty and not the bottom-graph */
        if (g == null || g.countNodes() == 0 || g.getBottom())
            return false;

        /* check if /g/ is "smaller" than /this/ */
        if (g.countNodes() > cnt || g.countEdges() > countEdges())
            return false;

        /* /g/ hat einen Knoten */
        if (g.countNodes() == 1)
            return cnt >= 1;    /* der K1 ist in jeder nichtleeren */
            /* Konfiguration enthalten */
        int i, j;

        /* /g/ hat zwei Knoten */
        if (g.countNodes() == 2) {
            if (g.degree(0) == 1) {
                /* /g/ ist ein K2 */
                for (i = 0; i < cnt - 1; i++)
                    for (j = i + 1; j < cnt; j++)
                        if (matrix[i][j] == EDGE)
                            return true;
            } else {
                /* /g/ ist ein 2K1 */
                for (i = 0; i < cnt - 1; i++)
                    for (j = i + 1; j < cnt; j++)
                        if (matrix[i][j] == NONEDGE || matrix[i][j] == UNKNOWN)
                            return true;
            }

            return false;   /* keine aufwändigeren Tests für diesen Fall */
        }


        /* Der Rumpf einer Konfiguration ist der Graph, der entsteht wenn man
         * alle optionalen Kanten wegläßt */
        Graph rumpf = new Graph(cnt);
        for (i = 0; i < cnt - 1; i++)
            for (j = i + 1; j < cnt; j++)
                if (matrix[i][j] == EDGE)
                    rumpf.addEdge(i, j);

        switch ( countOptedges() ) {
            case 0:
                /* keine optionale Kanten */
                return rumpf.isSubIsomorphic(g);

            case 1:
                /* eine optionale Kante -> beide Repräsentanten
                 * erzeugen und testen */
                if (! rumpf.isSubIsomorphic(g))
                        return false;

                for (i = 0; i < cnt - 1; i++)
                    for (j = i + 1; j < cnt; j++)
                        if (matrix[i][j] == OPTEDGE) {
                            rumpf.addEdge(i, j);
                            return rumpf.isSubIsomorphic(g);
                        }
        }

        if (rumpf.getComponents() > 1) {
            /* /rumpf/ nicht zusammenhängend. Teilkonfigurationen erzeugen und
             * Rekursion */

            for (i = 0; i < rumpf.getComponents(); i++) {
                Configuration c = new Configuration(this, rumpf.getComponents(i));

                if (c.isInducedSubgraph(g))
                    return true;
            }

            if (g.getComponents() == 1) /* g zusammenhängend */
                return false;
        }

        /* jetzt Knoten entfernen, so daß keine optionalen Kanten
         * mehr vorkommen */
        {
            Graph h = new Graph(cnt);

            for (i = 0; i < cnt - 1; i++)
                for (j = i + 1; j < cnt; j++)
                    if (matrix[i][j] == OPTEDGE)
                        h.addEdge(i,j);

            boolean maske[] = h.getVertexCover();

            for (i = 0; i < cnt; i++)
                maske[i] = ! maske[i];

            Configuration c = new Configuration(this, maske);

            if (c.isInducedSubgraph(g))
                return true;
        }

        return false;
    }

    /**
     * Checks whether the matrices of <tt>this</tt> and <tt>c</tt>
     * are equal if the nodes of <tt>c</tt> are reordered
     * according to the permutation given by <tt>perm</tt>.
     * If the result is true, <tt>perm</tt> contains a mapping
     * that is needed for isomorphism (by definition).
     */
    private boolean check(Configuration c, int perm[]){
        // this.cnt==g.cnt is checked before
        int i, j;
        for (i=0; i<cnt; i++)
            for (j=i+1; j<cnt; j++)
                if (matrix[i][j] != c.matrix[perm[i]][perm[j]])
                    return false;
        return true;
    }
}
