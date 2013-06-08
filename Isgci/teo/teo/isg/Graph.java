/*
 * Represents a (induced sub)graph.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/Graph.java,v 1.16 2011/10/27 15:53:27 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

import java.util.Vector;

public class Graph extends SmallGraph{
    private boolean matrix[][];
    private int cnt;    // number of used nodes
    private int Ecnt;   // number of edges
    private int size;   // size of matrix and KomponentenVektor
    private int Komponenten;    // Anzahl von Zusammenhangskomponenten
    private int KomponentenVektor[];
    private boolean Komponenten_isKanonisch;

    private boolean is_bottom;

    /** Creates a new graph without nodes. */
    public Graph(){
        this(0);
    }

    /** Creates a new graph with <tt>n</tt> nodes. */
    public Graph(int n){
        super();
        addNodesCount(n);
    }

    public Graph(Graph g){
        this(0);
        copyFrom(g);
    }

    /**
     * Set the nodecount of this to n. Any previous nodes/edges are lost!
     */
    public void addNodesCount(int n) {
        size=n;
        cnt=n;
        Ecnt = 0;
        Komponenten = n;    /* noch gibt es keine Kanten */
        Komponenten_isKanonisch = false;
        is_bottom = false;
        matrix=new boolean[size][size];
        KomponentenVektor = new int[size];
        int i,j;
        for(i=0;i<size;i++) {
            KomponentenVektor[i] = i;
            for(j=0;j<size;j++)
                matrix[i][j]=false;
        }
    }
    
    /** Copy the contents of gs into this. */
    private void copyFrom(SmallGraph gs){
        Graph g = (Graph)gs;

        if (g.is_bottom) {
            is_bottom = true;
            return;
        }

        size=g.size;
        cnt=g.cnt;
        Ecnt=g.Ecnt;
        Komponenten = g.Komponenten;
        Komponenten_isKanonisch = g.Komponenten_isKanonisch;
        matrix=new boolean[size][size];
        KomponentenVektor = new int[size];
        int i,j;
        for(i=0;i<size;i++) {
            KomponentenVektor[i] = g.KomponentenVektor[i];
            for(j=0;j<size;j++)
                matrix[i][j]=g.matrix[i][j];
        }
    }

    public Graph(Graph g, boolean mask[]){
        super();

        int i;

        cnt = 0;
        for (i = 0; i < g.cnt; i++)
            if (mask[i])
                cnt++;

        size = cnt;

        matrix = new boolean[size][size];
        KomponentenVektor = new int[size];

        int j, k, l;

        k = -1;
        for (i = 0; i < g.cnt; i++) {
            if (mask[i])
                k++;
            else
                continue;

            l = -1;
            for (j = 0; j < g.cnt; j++) {
                if (mask[j])
                    l++;
                else
                    continue;

                matrix[k][l] = g.matrix[i][j];
            }
        }

        Ecnt = 0;
        for (i = 0; i < cnt - 1; i++)
            for (j = i + 1; j < cnt; j++)
                if (matrix[i][j])
                    Ecnt++;

        updateKomponentenVektor();
    }

    public void copyFromComplement() {
        super.copyFromComplement();
        copyFrom(complement);

        //---- Then complement it.
        for(int i=0;i<cnt;i++)
            for(int j=0;j<cnt;j++)
                if(i!=j) matrix[i][j] = !matrix[i][j];
        Ecnt=(((cnt-1)*cnt)/2) - Ecnt;
        updateKomponentenVektor();
    }

    public boolean getBottom(){
        return is_bottom;
    }

    public void setBottom(){
        is_bottom = true;
        cnt = 0;
        Ecnt = 0;
    }
    
    
    /** Counts the nodes in this graph. */
    public int countNodes(){
        return cnt;
    }
    
    /** Counts the edges in this graph. */
    public int countEdges(){
        return Ecnt;
    }
    
    /** Returns the degree of the node at index <tt>v</tt> */
    public int degree(int v){
        if(v<0 || v>=cnt) return -1; // illegal argument
        int i,n=0;
        for(i=0;i<cnt;i++){
            if(matrix[v][i]) n++;
            // matrix[v][v] is always false
        }
        return n;
    }

    /** Returns the degree of the node at index <tt>v</tt> in the subgraph
     * induced by <tt>mask<tt>*/
    public int degree(int v, boolean mask[]){
        if(v<0 || v>=cnt || ! mask[v]) return -1; // illegal argument
        int i,n=0;
        for(i=0;i<cnt;i++){
            if(matrix[v][i] && mask[i]) n++;
            // matrix[v][v] is always false
        }
        return n;
    }
    
    /** Returns an array with all adjacent nodes of <tt>v</tt>. */
    public int[] adjList(int v){
        int i,n=degree(v);
        if(n<0) return null;
        int list[]=new int[n];
        n=0;
        for(i=0;i<cnt;i++){
            if(matrix[v][i]) list[n++]=i;
        }
        return list;
    }
    
    /** Adds a node to the graph. */
    public void addNode(){
        int i,j;
        if(cnt==size) increment();
        cnt++;
        j = -1; /* größte Zahl im KomponentenVektor in j speichern */
        for(i=0;i<cnt;i++){
            matrix[i][cnt-1]=false;
            matrix[cnt-1][i]=false;
            if (KomponentenVektor[i] > j)
                j = KomponentenVektor[i];
        }
        KomponentenVektor[cnt-1] = j + 1;
        Komponenten++;  /* der neue Knoten bildet eine neue Komponente */
        /* Komponenten_isKanonisch ändert sich nicht */
    }
    
    /** Increases the size of the matrix by 10. */
    private void increment(){
        size+=10;
        boolean newMatrix[][]=new boolean[size][size];
        int newKomponentenVektor[]=new int[size];
        int i,j;
        for(i=0;i<size;i++){
            if (i<cnt)
                newKomponentenVektor[i] = KomponentenVektor[i];
            else
                newKomponentenVektor[i] = -1;

            for(j=0;j<size;j++){
                if(i<cnt && j<cnt) newMatrix[i][j]=matrix[i][j];
                else newMatrix[i][j]=false;
            }
        }
        matrix=newMatrix;
        KomponentenVektor=newKomponentenVektor;
    }
    
    /** Adds an edge to the graph. */
    public void addEdge(int a,int b){
        if(a==b) return;
        if(a<0 || b<0 || a>=cnt || b>=cnt) return;
        if (matrix[a][b] || matrix[b][a])
            System.err.println("Edge \""+a+" - "+b+
            "\" already exists in graph "+this.getName()+"!");
        matrix[a][b]=true;
        matrix[b][a]=true;
        Ecnt++;

        /* Test, ob die neue Kante zwei Zusammenhangskomponenten verbindet */
        if (KomponentenVektor[a] != KomponentenVektor[b]) {
            int i, x, y;

            x = java.lang.Math.min(KomponentenVektor[a], KomponentenVektor[b]);
            y = java.lang.Math.max(KomponentenVektor[a], KomponentenVektor[b]);

            for (i = 0; i < cnt; i++)
                if (KomponentenVektor[i] == y)
                    KomponentenVektor[i] = x;

            Komponenten--;
            Komponenten_isKanonisch = false;
        }
    }
    
    /** Removes a node and all its adjacent edges from the graph. */
    public void delNode(int v){
        int i,j;

        /* Die Anzahl der Kanten im Graphen aktualisieren */
        for (i=0;i<cnt;i++)
            if (matrix[i][v])
                Ecnt--;

        /* Die Adjazenzmatrix aktualisieren */
        for(i=v+1;i<cnt;i++)
            for(j=0;j<cnt;j++)
                matrix[i-1][j]=matrix[i][j];
        for(i=0;i<cnt;i++)
            for(j=v+1;j<cnt;j++)
                matrix[i][j-1]=matrix[i][j];

        /* KomponentenVektor verschieben */
        for (i = v + 1; i < cnt; i++)
            KomponentenVektor[i-1] = KomponentenVektor[i];

        /* Anzahl der Knoten aktualisiseren */
        cnt--;

        /* KomponentenVektor aktualisieren */
        updateKomponentenVektor();
    }
    
    /** Removes the edge between the two given nodes. */
    public void delEdge(int a,int b){
        matrix[a][b]=false;
        matrix[b][a]=false;
        Ecnt--;
        updateKomponentenVektor();
    }

    private void updateKomponentenVektor(){
        int i, j, k;

        Komponenten = cnt;

        for (i = 0; i < cnt; i++)
           KomponentenVektor[i] = i;

        for (i = 0; i < cnt; i++) {
            for (j = 0; j < cnt; j++) {
                if (matrix[i][j] && KomponentenVektor[i] !=
                        KomponentenVektor[j]) {
                    int x, y;

                    x = java.lang.Math.min(KomponentenVektor[i],
                            KomponentenVektor[j]);
                    y = java.lang.Math.max(KomponentenVektor[i],
                            KomponentenVektor[j]);

                    for (k = 0; k < cnt; k++)
                        if (KomponentenVektor[k] == y)
                            KomponentenVektor[k] = x;

                    Komponenten--;
                }
            }
        }
        Komponenten_isKanonisch = false;
    }

    /* kanonisiert den KomponentenVektor */
    private void kanonKomponentenVektor(){
        int i, j;
        int frei;
        int zugross;

        while (true) {
            /* suche Komponente mit zu großer Nummer */
            zugross = -1;
            for (i = 0; i < cnt; i++) {
                if (KomponentenVektor[i] >= Komponenten) {
                    zugross = KomponentenVektor[i];
                    break;
                }
            }

            if (zugross == -1) {
                Komponenten_isKanonisch = true;
                return; /* fertig */
            }

            /* suche Lücke in Numerierung */
            frei = 0;
loop:       while (true) {
                for (i = 0; i < cnt; i++) {
                    if (KomponentenVektor[i] == frei) {
                        frei++;
                        continue loop;
                    }
                }
                break loop;
            }

            if (frei >= Komponenten) {
                System.err.println("es gibt keine kanonische Numerierung!!!");
                System.exit(1);
            }

            /* jetzt alle Vorkommen von /zugross/ durch /frei/ ersetzen. */
            for (i = 0; i < cnt; i++)
                if (KomponentenVektor[i] == zugross)
                    KomponentenVektor[i] = frei;
        }
    }

    
    /** Returns <tt>true</tt> if there is an edge between the given nodes. */
    public boolean getEdge(int a,int b){
        return matrix[a][b];
    }
    
    /**
     * Returns a string that represents the graph.
     * It contains the number of nodes, the name(s) of the graph
     * and the list of edges.
     */
    public String toString(){
        if(cnt==0) return "";
        int i,j;
        String s="{"+String.valueOf(cnt)+"}\n";
        s += namesToString() + "\n";
        for(i=0;i<cnt;i++)
            for(j=0;j<i;j++)
                if(matrix[i][j])
                    s+=(j+" - "+i+"\n");
        return s;
    }
    
    // --------------------------------------------------------------
    public boolean isIsomorphic(Graph g){
        final boolean DEBUG = false;

        if (DEBUG) {
            System.err.print("isIsomorphic(" + this.getName() + ", " +
                    g.getName() + ")\n");
        }

        /* check if one of both is the bottom-graph*/
        if (g.is_bottom || is_bottom) {
            if (DEBUG) {
                System.err.print("    sind nicht isomorph\n");
            }
            return false;
        }

        /* check if number of nodes and edges are equal */
        if(cnt!=g.cnt || countEdges()!=g.countEdges()) {
            if (DEBUG) {
                System.err.print("    sind nicht isomorph\n");
            }
            return false;
        }

        if (Komponenten != g.Komponenten) {
            if (DEBUG) {
                Permutation perm=new Permutation(cnt);
                System.err.print("    0 potentielle Graphenisomorphismen\n");
                System.err.print("    " + perm.fac(cnt)
                        + " Permutationen der Knotenmenge\n");
                System.err.print("    0 getestete Graphenisomorphismen\n");
                System.err.print("    " + perm.fac(cnt)
                        + " getestet (alter Algorithmus)\n");
                System.err.print("    sind nicht isomorph\n");
            }
            return false;
        }

        int i;
        int j;
        boolean darf[][] = new boolean[cnt][cnt];
        boolean mask[][] = new boolean[2][cnt];

        for (i=0; i<cnt; i++) {
            mask[0][i] = true;
            mask[1][i] = true;
            for (j=0; j<cnt; j++)
                darf[i][j] = true;
        }

        if (!isIsomorphicIntern(g,darf,mask)) {
            if (DEBUG) {
                Permutation perm=new Permutation(cnt);
                System.err.print("    0 potentielle Graphenisomorphismen\n");
                System.err.print("    " + perm.fac(cnt)
                        + " Permutationen der Knotenmenge\n");
                System.err.print("    0 getestete Graphenisomorphismen\n");
                System.err.print("    " + perm.fac(cnt)
                        + " getestet (alter Algorithmus)\n");
                System.err.print("    sind nicht isomorph\n");
            }
            return false;
        }

        /* so wird das bestimmt besser optimiert */
        if (DEBUG) {
            Permutation perm=new Permutation(cnt,darf);
            int anzahl = 0;

            System.err.print("    " + perm.count()
                    + " potentielle Graphenisomorphismen\n");
            System.err.print("    " + perm.fac(cnt)
                    + " Permutationen der Knotenmenge\n");

            perm.first();
            do {
                anzahl++;
                if (check(g,perm.get())) {
                    System.err.print("    " + anzahl
                            + " getestete Graphenisomorphismen\n");

            Permutation p = new Permutation(cnt);
            int x[] = perm.get();


            anzahl = 1;
            while (!permeq(x, p.get())) {
                p.next();
                anzahl++;
            }


                    System.err.print("    " + anzahl +
                            " getest (alter Algorithmus)\n");

                    System.err.print("    are isomorphic\n");
                    return true;
                }
            } while (perm.next());

            System.err.print("    " + anzahl +
                    " getestete Graphenisomorphismen\n");
            System.err.print("    are not isomorphic\n");

        } else {
            Permutation perm=new Permutation(cnt,darf);
            do {
                if (check(g,perm.get()))
                    return true;
            } while (perm.next());
        }

        return false;
    }

    private boolean permeq(int a[], int b[]){
        for (int i = 0; i < cnt; i++)
            if (a[i] != b[i])
                return false;

        return true;
    }

    /** Rekursive Hilfsfunktion zur Bestimmung von <tt>darf<tt>.
     * Die Matrix <tt>darf</tt> enthält an der Stell i,j eine 1, wenn der
     * Knoten i in <tt>this<tt> den gleichen Grad hat wie der Knoten j in
     * <tt>g<tt>. In mask ist hierbei gespeichert welcher Teil des Graphen
     * bearbeitet werden soll. In mask[0] beschreibt hierbei <tt>this<tt> und
     * mask[1] <tt>g<tt>.*/
    private boolean isIsomorphicIntern(Graph g,
                        boolean darf[][], boolean mask[][]){
        int i, j;
        boolean KnotenMitGrad[][][] = new boolean[cnt][2][cnt];
        int Grad[][] = new int[2][cnt];
        boolean changed = false;

        /* initialisiere KnotenMitGrad */
        for (i=0; i < cnt; i++) {
            for (j=0; j < cnt ; j++) {
                KnotenMitGrad[j][0][i] = false;
                KnotenMitGrad[j][1][i] = false;
            }
        }

        /* Speichere in Grad[0][i] den Grad des Knoten i im Graph this und
         * setze KnotenMitGrad[x][0][y] auf true, wenn im Graphen this der
         * Knoten y den Grad x hat. Analog für den Graphen g wenn der Index 1
         * gewählt ist. Das ganze aber nur dann, wenn in der jeweiligen Maske
         * das Flag für den entsprechenden Knoten gesetzt ist. */
        for (i=0; i < cnt; i++) {
            int h;

            if (mask[0][i]) {
                h = degree(i,mask[0]);
                Grad[0][i] = h;
                KnotenMitGrad[h][0][i] = true;
            }

            if (mask[1][i]) {
                h = g.degree(i,mask[1]);
                Grad[1][i] = h;
                KnotenMitGrad[h][1][i] = true;
            }
        }

        /* jetzt darf[][] bearbeiten */
        for (i = 0; i < cnt; i++)
            if (mask[0][i]) 
                for (j = 0; j < cnt; j++) 
                    /* Gradzahlen vergleichen */
                    if (mask[1][j] && darf[i][j] && Grad[0][i] != Grad[1][j]) {
                        changed = true;
                        darf[i][j] = false;
                    }

        if (!changed)
            return true;    /* nix passiert */

        for (i=0;i<cnt;i++) { /* cnt - 1 ist maximal möglicher Grad */
            int differenz = 0;
            int anzahl = 0;

            for (j=0; j<cnt; j++) {
                if (KnotenMitGrad[i][0][j]) {
                    differenz++;
                    anzahl++;
                }
                if (KnotenMitGrad[i][1][j])
                    differenz--;
            }

            if (differenz != 0)
                return false;

            if (anzahl > 1
                    && !isIsomorphicIntern2(g, darf, mask, KnotenMitGrad[i]))
                return false;
        }

        for (i = 0; i < cnt; i++) {
            /* untersuchen, ob es Knoten gibt, für die es keine
             * Positionsmöglichkeiten gibt */
            if (mask[0][i]) { /* nur wenn in diesen Durchlauf eventuell */
                /*angefaßt */
                boolean ziel = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[i][j]) {
                        ziel = true;
                        break;
                    }
                }
                if (! ziel) 
                    return false;
            }

            /* untersuchen, ob es Knoten gibt, für die es keine Quelle gibt */
            if (mask[1][i]) { /* nur wenn in diesen Durchlauf eventuell */
                /*angefaßt */
                boolean quelle = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[j][i]) {
                        quelle = true;
                        break;
                    }
                }
                if (! quelle)
                    return false;
            }
        }

        return true;
    }

    private boolean isIsomorphicIntern2(Graph g,
                    boolean darf[][],
                    boolean maskAlt[][],
                    boolean maskNeu[][]){
        int i,j;
        int SummeGradNachbarn[][] = new int[2][cnt];
        int grad = 0;        /* sonst nörgelt der Compiler */

        for (i = 0; i < cnt; i++)
            if (maskNeu[0][i]) {
                grad = degree(i);
                break;
            }

        for (i = 0; i < cnt; i++) {
            if (maskNeu[0][i]) {
                int s = 0;
                for (j = 0; j < cnt; j++) {
                    if (maskAlt[0][j] && matrix[i][j])
                        s += degree(j);
                }
                SummeGradNachbarn[0][i] = s;
            }

            if (maskNeu[1][i]) {
                int s = 0;
                for (j = 0; j < cnt; j++) {
                    if (maskAlt[1][j] && g.matrix[i][j])
                        s += g.degree(j);
                }
                SummeGradNachbarn[1][i] = s;
            }
        }

        for (i = 0; i < cnt; i++)
            if (maskNeu[0][i])
                for (j = 0; j < cnt; j++)
                    if (maskNeu[1][j]
                            && SummeGradNachbarn[0][i] !=
                                     SummeGradNachbarn[1][j]) {
                        darf[i][j] = false;
                    }

        for (i = 0; i < cnt; i++) {
            /* untersuchen, ob es Knoten gibt, für die es keine
             * Positionsmöglichkeiten gibt */
            if (maskNeu[0][i]) { /* nur wenn in diesen Durchlauf */
                    /*eventuell angefaßt */
                boolean ziel = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[i][j]) {
                        ziel = true;
                        break;
                    }
                }
                if (! ziel) 
                    return false;
            }

            /* untersuchen, ob es Knoten gibt, für die es keine Quelle gibt */
            if (maskNeu[1][i]) { /* nur wenn in diesen Durchlauf */
                            /*eventuell angefaßt */
                boolean quelle = false;;
                for (j = 0; j < cnt; j++) {
                    if (darf[j][i]) {
                        quelle = true;
                        break;
                    }
                }
                if (! quelle)
                    return false;
            }
        }

        return (grad > 1) ? isIsomorphicIntern(g,darf,maskNeu) : true;

    }

    /* liefert true, wenn g ein von this induzierter Teilgraph ist */
    public boolean isSubIsomorphic(Graph g){
        /* check number of nodes and edges */
        if(cnt < g.cnt || countEdges() < g.countEdges())
            return false;

        int i, j, k;

        /* XXX was ist mit g.cnt == 0? */

        /* shortcuts für kleine Graphen */
        switch (g.cnt) {
            case 1:
                /* Der K1 ist immer drin */
                return true;

            case 2:
                for (i = 0; i < cnt; i++)
                    for (j = i + 1; j < cnt; j++)
                        if (matrix[i][j] == g.matrix[0][1])
                            return true;
                return false;

            case 3:
                /* K3 */
                if (g.countEdges() == 3) {
                    for (i = 0; i < cnt; i++)
                        for (j = 0; j < cnt; j++) {
                            if (!matrix[i][j])
                                continue;

                            for (k = j + 1; k < cnt; k++)
                                if (matrix[i][k] && matrix[j][k])
                                    /* K3: i - j  - k - i */
                                    return true;
                        }
                    return false;
                }

                /* P2 */
                else if (g.countEdges() == 2) {
                    for (i = 0; i < cnt; i++)
                        for (j = 0; j < cnt; j++) {
                            if (!matrix[i][j])
                                continue;

                            for (k = j + 1; k < cnt; k++)
                                if (matrix[i][k] && !matrix[j][k])
                                    /* P2: k - i - j */
                                    return true;
                        }
                    return false;
                }

                /* co-P2 */
                else if (g.countEdges() == 1) {
                    for (i = 0; i < cnt; i++)
                        for (j = 0; j < cnt; j++) {
                            if (matrix[i][j])
                                continue;

                            for (k = j + 1; k < cnt; k++)
                                if (matrix[i][k] && !matrix[j][k])
                                    /* co-P2: i - k, j */
                                    return true;
                        }
                    return false;
                }

                /* co-K3 */
                else /*if (g.countEdges() == 0) */{
                    for (i = 0; i < cnt; i++)
                        for (j = 0; j < cnt; j++) {
                            if (matrix[i][j])
                                continue;

                            for (k = j + 1; k < cnt; k++)
                                if (!matrix[i][k] && !matrix[j][k])
                                    /* co-K3: i, j, k */
                                    return true;
                        }
                    return false;
                }
        }

        if (cnt == g.cnt)
            return isIsomorphic(g);

        /* jetzt haben wir g als Graphen mit einer Knotenzahl g.m
         * von 4 <= g.m < this.m
         * 
         * nun systematisch alle induzierten Teilgraphen dieser Größe erzeugen
         * und überprüfen ob g isomorph zu einem dieser Graphen ist. */

        //throw new Error("PermutationMask needed");
        PermutationMask p = new PermutationMask(cnt, g.cnt);

        p.first();
        do {
            Graph g1 = new Graph(this, p.get());

            if (g1.isIsomorphic(g))
                return true;
        } while (p.next());

        return false;
    }

    /**
     * Checks whether the matrices of <tt>this</tt> and <tt>g</tt>
     * are equal if the nodes of <tt>g</tt> are reordered
     * according to the permutation given by <tt>perm</tt>.
     * If the result is true, <tt>perm</tt> contains a mapping
     * that is needed for isomorphism (by definition).
     */
    private boolean check(Graph g, int perm[]){
        // this.cnt==g.cnt is checked before
        int i,j;
        for(i=0;i<cnt;i++)
            for(j=i+1;j<cnt;j++)
                if(matrix[i][j] != g.matrix[perm[i]][perm[j]])
                    return false;
        return true;
    }

    public int getComponents(){
        return Komponenten;
    }

    /* liefert eine Maske zurück, die angibt welche Knoten in der i-ten
     * Komponente enthalten sind */
    public boolean[] getComponents(int num){
        boolean ret[] = new boolean[cnt];

        if (num < 0 && num >= Komponenten)
            return null;

        if (! Komponenten_isKanonisch)
            kanonKomponentenVektor();

        for (int i = 0; i < cnt; i++)
            ret[i] = KomponentenVektor[i] == num;

        return ret;
    }

    /* liefert Maske, die VertexCover bildet */
    public boolean[] getVertexCover(){
        /* eine einfache Greedy-Strategie */

        int grad[] = new int[cnt];
        boolean ret[] = new boolean[cnt];

        for (int i = 0; i < cnt; i++) {
            grad[i] = degree(i);
            ret[i] = false;
        }

        int max, pos;

        while (true) {

            max = 0;
            pos = -1;

            for (int i = 0; i < cnt; i++)
                if (grad[i] > max) {
                    max = grad[i];
                    pos = i;
                }

            if (pos == -1)
                break;

            ret[pos] = true;
            grad[pos] = 0;

            for (int i = 0; i < cnt; i++)
                if (matrix[i][pos])
                    grad[i]--;

        }

        return ret;
    }


    public static void main(String args[]){
/*
        Graph g1 = new Graph(5);
        Graph g2 = new Graph(5);

        g1.addEdge(0,1);
        g1.addEdge(1,2);
        g1.addEdge(2,3);
        g1.addEdge(3,4);
        g1.addEdge(0,3);

        g2.addEdge(0,1);
        g2.addEdge(1,2);
        g2.addEdge(2,3);
        g2.addEdge(0,4);
        g2.addEdge(2,4);
*/
        Graph g1 = new Graph(9);
        Graph g2 = new Graph(9);

        g1.addEdge(0,2);
        g1.addEdge(0,3);
        g1.addEdge(1,3);
        g1.addEdge(0,4);
        g1.addEdge(1,4);
        g1.addEdge(2,4);
        g1.addEdge(1,5);
        g1.addEdge(3,5);
        g1.addEdge(0,6);
        g1.addEdge(3,6);
        g1.addEdge(0,7);
        g1.addEdge(5,7);
        g1.addEdge(4,8);
        g1.addEdge(5,8);
        g1.addEdge(6,8);
        g1.addEdge(7,8);

        g2.addEdge(0,2);
        g2.addEdge(0,3);
        g2.addEdge(1,3);
        g2.addEdge(0,4);
        g2.addEdge(1,4);
        g2.addEdge(2,4);
        g2.addEdge(1,5);
        g2.addEdge(3,5);
        g2.addEdge(0,6);
        g2.addEdge(5,6);
        g2.addEdge(4,7);
        g2.addEdge(5,7);
        g2.addEdge(6,7);
        g2.addEdge(0,8);
        g2.addEdge(1,8);
        g2.addEdge(6,8);

        System.out.print("G1: " + g1 + "\n");
        System.out.print("G2: " + g2 + "\n");
        if (g1.isIsomorphic(g2))
            System.out.print("g1 und g2 sind isomorph\n");
        else
            System.out.print("g1 und g2 sind NICHT isomorph\n");
    }
}

/* EOF */
