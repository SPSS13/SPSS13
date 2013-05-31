/*
 * Utils for XSLT processing.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/XsltUtil.java,v 1.3 2013/03/01 17:06:41 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo;

import java.io.*;
import teo.isgci.util.Latex2Html;

public class XsltUtil {

    /**
     * Run a command with the given data as input.
     */
    public static void systemIn(String command, String data) throws Exception {
        String cmd[] = new String[3];
        cmd[0] = System.getProperty("SHELL", "/bin/sh");
        cmd[1] = "-c";
        cmd[2] = command;
        Process p = Runtime.getRuntime().exec(cmd);
        PrintWriter w = new PrintWriter(p.getOutputStream());
        w.print(data);
        w.flush();
        w.close();
        p.waitFor();
    }

    
    /**
     * Run a command and return its output.
     */
    public static String systemOut(String command) throws Exception {
        int c;
        String cmd[] = new String[3];
        cmd[0] = System.getProperty("SHELL", "/bin/sh");
        cmd[1] = "-c";
        cmd[2] = command;
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader r =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder s = new StringBuilder();
        while ((c = r.read()) != -1)
            s.append((char) c);
        p.waitFor();

        return s.toString();
    }

    

    //----------------------------- Latex stuff -----------------------------
    static Latex2Html converter = new Latex2Html("images/");

    /**
     * Interpret the given string as latex and "draw" it.
     */
    public static String latex(String str) {
        return converter.html(str);
    }
}


/* EOF */
