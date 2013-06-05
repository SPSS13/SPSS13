/*
 * An ISGCI Query
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/iq/IQ.java,v 1.2 2013/01/19 21:38:32 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.iq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;

public class IQ {
    /** The string that contains the query */
    protected String source;
    /** The parser */
    protected IQParser parser;
    /** Errormessage iff parse was not succesful */
    protected String error;
    /** The resulting set */
    protected Set<GraphClass> set;


    /**
     * Create a query containing the given string and execute it immediately.
     */
    public IQ(String s) {
        source = s;
        error = null;
        set = null;
        parser = new IQParser();
        parse();
    }


    /**
     * Parse the query and set the instance variables.
     */
    private void parse() {
        if (!parser.parse(new mouse.runtime.SourceString(source)))
            error = "parse failed";  // Shouldn't happen
        else
            error = ((IQSemantics) parser.semantics()).getError();
        if (error == null)
            set = ((IQSemantics) parser.semantics()).getIQ().eval();
    }


    /**
     * Return the result of the query as a set of graph classes or null if an
     * error occurred.
     */
    public Set<GraphClass> getSet() {
        if (error != null)
            return null;
        return set;
    }

    /**
     * Return the error message or null if no error occurred.
     */
    public String getError() {
        return error;
    }

    /**
     * For testing.
     */
    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: IQ url [query]");
            System.exit(1);
        }

        teo.Loader loader = new teo.Loader(args[0], true);
        DataSet.init(loader, "data/isgci.xml");

        if (args.length > 1) {
            IQ iq = new IQ(args[1]);
            Set<GraphClass> res = iq.getSet();
            if (res == null)
                System.out.println(iq.getError());
            else
                System.out.println(res);
        } else {
            System.out.println("Enter a query or an empty line to stop");
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line.length() == 0)
                    System.exit(0);
                IQ iq = new IQ(line);
                Set<GraphClass> res = iq.getSet();
                if (res == null)
                    System.out.println(iq.getError());
                else {
                    for (GraphClass g : res)
                        System.out.println(g);
                }
            }
        }
    }
}

/* EOF */
