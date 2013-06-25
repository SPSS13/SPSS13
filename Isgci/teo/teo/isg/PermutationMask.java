/*
 * Permutations of Masks.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isg/Attic/PermutationMask.java,v 1.2 2011/06/04 08:11:15 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isg;

public class PermutationMask {

    int element[];
    int anzahl;
    int einsen;

    /**
     * Creates a new Permutation-Object with <tt>n</tt> elements, <tt>m</tt> of
     * them are one and initializes them in ascending order.
     */
    public PermutationMask(int n, int m){
        element = new int[n];
        anzahl = n;
        einsen = m;

        this.first();
    }

    /* Zählt, wieviele Permutationen das Objekt hat */
    public long count()
    {
        long ret = 1;
        boolean[] save = new boolean[anzahl];

        System.arraycopy(element,0,save,0,anzahl);
        first();
        while (next())
            ret++;
        System.arraycopy(save,0,element,0,anzahl);

        return ret;
    }

    private long fac(int n)
    {
        long f = 1;
        if (n > 1)
            return n * fac(n-1);
        return f;
    }

    public void first()
    {
        for (int i = 0; i < anzahl; i++)
            element[i] = i;
    }

    /* Ermittelt die nächste Permutation, so es eine nächste gibt wird /true/
     * zurückgegeben */
    public boolean next()
    {
        /* Algorithmus
         * suche von hinten erste Stelle p, wo element[p]<element[p+1]
         * suche von hinten erste Stelle p2, wo element[p]<element[p2]
         * tausche element[p]<->element[p2]
         * setze p eine Position nach hinten
         * wenn p<element.length-1 (=p1), sortiere elemnt[p]..element[p1]
         * weil das ganze schon (rueckwaerts) sortiert ist reicht es,
         * den ersten mit dem letzten,
         * den zweiten mit dem vorletzten usw zu vertauschen
         *
         * zusätzlich wird bei der Suche der zu tauschenden Positionen darauf
         * geachtet, daß keine Elemente getauscht werden, die beiden in den
         * Bereich der Einsen bzw. Nullen fallen
         */
        int j,p1,p2,p;
        p2 = p1 = p = anzahl - 1;

        // erste Stelle (von hinten) suchen, wo [p]<[p+1]
        while (--p >= 0)
            if (element[p] < element[p+1]
                    && element[p] < einsen
                    && element[p+1] >= einsen)
                break;

        if (p < 0)
            return false;

        // suche (v.h.) erstes (dh kleinstes [p2])mit [p2]>[p],
        while (element[p2] <= element[p]
                || (element[p2] < einsen && element[p] < einsen)
                || (element[p2] >= einsen && element[p] >= einsen))
            p2--;

        j = element[p];           // tauschen
        element[p] = element[p2];
        element[p2] = j;
        p++;                    // p ein dahinter

        for (; p1 > p; p1--, p++) {        // wenn p nicht letzte stelle
            j = element[p1];            // tauschen
            element[p1] = element[p];
            element[p] = j;
        }

        return true;
    }

    private void show()
    {
        for (int i = 0; i<anzahl; i++)
            System.out.print(element[i]+"  ");
    }

    public boolean[] get(){
        boolean neu[]=new boolean [anzahl];

        for (int i = 0; i < anzahl; i++)
            neu[i] = (element[i] < einsen);

        return neu;
    }

    public static void main(String args[]){
        long t1, t2;

        PermutationMask p=new PermutationMask(4,2);

        t1=System.currentTimeMillis();
        do {
            boolean maske[] = p.get();

            p.show();
            System.out.print(" -> ( ");
            for (int i = 0; i < 4; i++ ){
                System.out.print( (maske[i] ? "1": "0" )+ " ");
            }
            System.out.print(")\n");
        }while(p.next());
        t2=System.currentTimeMillis();
        System.out.println("next: "+(t2-t1)+" ms");
    }
}

/* EOF */
