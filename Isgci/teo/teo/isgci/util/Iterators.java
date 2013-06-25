/*
 * Utilities for iterators.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Iterators.java,v 2.1 2011/10/22 20:09:01 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Adapted from java2s.com/Collections data structures.
 */
public class Iterators {

    /** Prevent instances */
    private Iterators() {}
  
    /**
     * Return the union of two iterators.
     */
    public static <E> Iterator<E> union(Iterator<E> a, Iterator<E> b) {
        List<Iterator<E> > iters = new ArrayList<Iterator<E> >();
        iters.add(a);
        iters.add(b);
        return union(iters);
    }


    /**
     * Return the union of a list of iterators.
     */
    public static <E> Iterator<E> union(final List<Iterator<E> > iterators) {
        return new Iterator<E>() {
            private int iteratorIndex = 0;
            private Iterator<E> current = iterators.size() > 0 ?
                    iterators.get(0) : null; 

            public boolean hasNext() {  
                for(;;) {
                    if(current == null) {
                        return false;
                    }
                    if(current.hasNext()) {
                        return true;
                    }
                    iteratorIndex++;
                    current = iteratorIndex >= iterators.size() ? null :
                        iterators.get(iteratorIndex);
                }       
            }

            public E next() {
                for(;;) {
                    if(this.current == null) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return this.current.next();
                    } catch(NoSuchElementException nse) {
                        this.iteratorIndex++;
                        this.current = this.iteratorIndex >= iterators.size()
                            ? null : iterators.get(this.iteratorIndex);
                    }
                }
            }

            public void remove() {
                if(this.current == null) {
                    throw new NoSuchElementException();
                }
                this.current.remove();
            }
        };
    }
  

    /**
     * Return an iterator with just the single given element.
     */
    public static <E> Iterator<E> singleton(final E item) {
        return new Iterator<E>() {
            private boolean gotItem = false;

            public boolean hasNext() {
                return !this.gotItem;
            }

            public E next() {
                if(this.gotItem) {
                    throw new NoSuchElementException();
                }
                this.gotItem = true;
                return item;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

/* EOF */
