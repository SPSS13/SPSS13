/*
 * "Human" comparison of latex strings.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/LessLatex.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.util.Comparator;

/**
 * Compares two latex strings.
 */
public class LessLatex implements BinaryPredicate, Comparator {

    public boolean equals(Object first, Object second) {
        return compare(first, second) == 0;
    }

    public int compare(Object first, Object second) {
        String s = first.toString();
        String t = second.toString();
        int is = 0, it = 0;
        char cs = 0, ct = 0;

        for (;;) {
            do {
                ct = it == t.length() ? '\0' : t.charAt(it++);
            } while (ct == '$'  ||  ct == '{'  ||  ct == '}'  ||
                     ct == '('  ||  ct == ')'  ||  ct == '^'  ||  ct == '_'  ||
                     ct == '-'  &&  t.charAt(it) == '-');
            do {
                cs = is == s.length() ? '\0' : s.charAt(is++);
            } while (cs == '$'  ||  cs == '{'  ||  cs == '}'  ||
                     cs == '('  ||  cs == ')'  ||  cs == '^'  ||  cs == '_'  ||
                     cs == '-'  &&  s.charAt(is) == '-');
            if (cs < ct)
                return -1;
            if (cs > ct)
                return 1;
            if (cs == '\0')
                return s.compareTo(t);
        }
        
    }

    /**
     * Return true if first < second.
     */
    public boolean execute(Object first, Object second) {
        return compare(first, second) < 0;
    }
}

/* EOF */
