/*
 * An Iterable wrapper for an Iterator.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Itera.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.util.Iterator;
import java.util.Vector;

public class Itera<T> implements Iterable<T>, Iterator<T> {
    private Iterator<T> iter;


    /**
     * Especially for subclassing. Overload next/hasNext/remove or an endless
     * loop will result!
     */
    public Itera() {
        this.iter = this;
    }

    public Itera(Iterator<T> iter) {
        this.iter = iter;
    }

    public Iterator<T> iterator() {
        return iter;
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    public T next() {
        return iter.next();
    }

    public void remove() {
        iter.remove();
    }

    /*public static void main(String[] args) {
        Vector<String> v = new Vector<String>();
        v.add("one");
        v.add("two");
        v.add("three");
        for (String s : new Itera<String>(v.iterator()))
            System.out.println(s);
    }*/
}

/* EOF */
