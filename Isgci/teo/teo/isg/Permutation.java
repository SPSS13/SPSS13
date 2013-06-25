/*
 * Permutations.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/Permutation.java,v 1.5 2011/04/07 07:28:31 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

public class Permutation {

    int element[];
    boolean darf[][];
    int anzahl;
    boolean leer;

    /**
     * Creates a new Permutation-Object with <tt>n</tt> elements and
     * initializes them in ascending order.
     */
    public Permutation(int n){
        element=new int[n];
        anzahl = n;
        this.darf =  new boolean[n][n];
        leer = false;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                darf[i][j] = true;

        this.first();
    }

    public Permutation(int n, boolean darf[][]){
        element=new int[n];
        anzahl = n;
        this.darf = darf;
        leer = false;

        this.first();
    }

    /* Counts how many permutations has the object */
    public long count(){
        if (leer)
            return 0;

        long i = 1;

        first();
        while (next())
            i++;

        return i;
    }

    public long fac(int n){
        long f = 1;
        if (n > 1)
            return n * fac(n-1);
        return f;
    }

    public void first(){
        boolean used[] = new boolean[anzahl];

        for (int i = 0; i < anzahl; i++)
            used[i] = false;

        for (int i = 0; i < anzahl; i++) {
            boolean gefunden = false;
            for (int j = 0; j < anzahl; j++)
                if (darf[i][j] && !used[j]) {
                    used[j] = true;
                    element[i] = j;
                    gefunden = true;
                    break;
                }
            if (!gefunden)
                leer = true;
        }
    }

    public void last(){
        boolean used[] = new boolean[anzahl];

        if (leer)
            return;

        for (int i = 0; i < anzahl; i++)
            used[i] = false;

        for (int i = 0; i < anzahl; i++) {
            for (int j = anzahl - 1; j > 0; j--)
                if (darf[i][j] && !used[j]) {
                    used[j] = true;
                    element[i] = j;
                    break;
                }
        }
    }

    /* Ermittelt die nächste Permutation entsprechend darf[][] mithilfe von
     * Backtracking */
    public boolean next(){
        boolean used[] = new boolean[anzahl];
        int pos;
        boolean gefunden;

        if (leer)
            return false;

        for (int i = 0; i < anzahl; i++)
            used[i] = true;

        gefunden = false;
        /* von hinten abbauen bis einen anderen Weg gibt (Backtracking) */
        for (pos = anzahl - 1 ; pos >= 0; pos--) {

            used[element[pos]] = false;   /* freigeben */

            /* andere Möglichkeit suchen */
            for (int j = element[pos] + 1; j < anzahl; j++)
                if (darf[pos][j] && !used[j]) {
                    gefunden=true;
                    break;
                }

            if (gefunden) break;
        }

        if (!gefunden)
            return false;

        /* an der Stelle pos ein Element positionieren, das größer als
         * das bisherige ist, den Rest nach dem normalen Verfahren
         * auffüllen */
        for (int j = element[pos] + 1; j < anzahl; j++)
            if (darf[pos][j] && !used[j]) {
                used[j] = true;
                element[pos] = j;
                break;
            }

        for (int i = pos + 1; i < anzahl; i++) {
            for (int j = 0; j < anzahl; j++)
                if (darf[i][j] && !used[j]) {
                    used[j] = true;
                    element[i] = j;
                    break;
                }
        }

        return true;
    }

    public void show(){
        for (int i=0;i<element.length;i++)
            System.out.print(element[i]+"  ");
        System.out.println();
    }

    public int[] get(){
        int neu[]=new int[element.length];
        System.arraycopy(element,0,neu,0,element.length);
        return neu;
    }

    /** Returns same value as get()[i] would. */
    public int get(int i){
        // throws ArrayIndexOutOfBoundsException
        return element[i];
    }

    public static void main(String args[]){
        long t1, t2;

        Permutation p=new Permutation(10);

        t1=System.currentTimeMillis();
        while(p.next());
        t2=System.currentTimeMillis();
        System.out.println("next: "+(t2-t1)+" ms");
    }
}

/* EOF */
