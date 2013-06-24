/*
 * A collection of some common and useful functions.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Utility.java,v 2.2 2011/09/29 18:33:49 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */
 
package teo.isgci.util;

import java.util.Vector;

public final class Utility{
    
    /** Don't allow any objects of this class */
    private Utility(){}
    

    /**
     * Returns a shortened name of a graph class with "..." at the end.
     * If a graph class had a name shorter than 40 symbols, returns the name
     * itself. Used when the name of a graph class is too long for putting on a
     * drawing or in the Dialog for changing name.
     * @param name a full name of the graph class
     */
    public static String getShortName(String name){
        if (name.length() <= 40)
            return name;
        
        int breakSymbol, lastComma, lastSpace, lastOpenBracket,
            lastCloseBracket;
        
        lastComma = name.lastIndexOf(',', 37);
        lastSpace = name.lastIndexOf(' ', 37);
        breakSymbol = lastComma >= lastSpace ? lastComma : lastSpace;
        lastOpenBracket = name.lastIndexOf('{', breakSymbol);
        lastCloseBracket = name.lastIndexOf('}', breakSymbol);
        if (lastOpenBracket != -1)
            if (lastCloseBracket == -1 || lastOpenBracket > lastCloseBracket)
                breakSymbol = name.lastIndexOf(',', lastOpenBracket);
        name = name.substring(0, breakSymbol) + "...";
        
        return name;
    }
    
    public static void main(String args[]){
        Utility ut = new Utility();
        System.out.println(getShortName(args[0]));
    }
}
