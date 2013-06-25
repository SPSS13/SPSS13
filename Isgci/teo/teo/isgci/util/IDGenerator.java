/*
 * Generates and caches node id's for graph classes.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/IDGenerator.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.BitSet;

public class IDGenerator {
    /** The id strings start with this */
    private String prefix;
    /** Maps graph class names to ids (from the cache file) */
    private HashMap<String,String> cache;
    /** Every true bit is a number that is handed out either in the cache file
     * or while running.
     */
    private BitSet used;

    /**
     * Create a new IDGenerator using the given prefix, reading the ids from
     * the given cachefile.
     */
    public IDGenerator(String prefix, String cachefile) {
        this.prefix = prefix;
        cache = new HashMap<String,String>();
        used = new BitSet();

        if (cachefile == null)
            return;

        try {
            readCache(cachefile);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Warning: Cannot read class name cache file "+
                    cachefile);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Return the node id to use for classname. If classname exists in the
     * cache file, the same id is used, otherwise a new, unused id is returned.
     */
    public String getID(String classname) {
        String res = cache.get(classname);
        if (res != null)
            return res;

        int id = used.nextClearBit(1);          // AUTO_0 not used
        used.set(id);
        return prefix + String.valueOf(id);
    }


    /**
     * Fill the cache from the given file. File format is "id\tclassname" per
     * line.
     */
    public void readCache(String filename) throws
            FileNotFoundException, IOException{
        String line;
        int sep;
        BufferedReader in = new BufferedReader(new FileReader(filename));

        while ((line = in.readLine()) != null) {
            String[] parts = line.split("\t", 2);
            if (cache.put(parts[1], parts[0]) != null)
                throw new Error("Duplicate key "+ parts[1] +
                        "in name cache file.");
            if (!parts[0].startsWith(prefix))
                throw new Error("Cached name doesn't start with "+ prefix);
            used.set(Integer.parseInt(parts[0].substring(prefix.length())));
        }
        in.close();
    }
}

/* EOF */
