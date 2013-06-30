/*
/*
 * Searching classes for keywords through the server
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/WebSearch.java,v 2.1 2011/09/29 18:34:29 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JFrame;
import javax.swing.JTextField;

import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.LessLatex;

/**
 * Sucht in der DB nach dem Suchbegriff, der im Konstruktor angegeben wird
 * 
 */
public class WebSearch extends JTextField implements Iterator<Object> {
    /**
     * 
     */
    private static final long serialVersionUID = 7307237767491425666L;
    /** die URL, auf der das Script liegt */
    protected static final String wwwurl = "http://www.graphclasses.org/search.cgi";
    protected static Comparator<Object> cmpor = new LessLatex();
    protected List<String> ergebnis;
    /** Next element to be given out by nextElement() */
    protected int count;

    /**
     * initialisiert Suche nach "graph"
     * 
     */
    public static void main(String[] args) {
        // baut testfenster mit eingabefeld auf
        // und schreibt ergebnisse auf die Konsole
        JFrame frame = new JFrame("testwindow");
        WebSearch w = new WebSearch();
        w.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Source ermitteln
                WebSearch source = (WebSearch)e.getSource();
                // Suche ausloesen
                try {
                    source.search(e.getActionCommand(), false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // DEBUG: nach Suche Elemente auf Konsole ausgeben
                for (Object o : source.ergebnis)
                    System.out.println(o);
                // Text loeschen
                source.setText("");
            }
        });
        frame.getContentPane().add(w);
        frame.setSize(200, 50);
        frame.setVisible(true);
    }

    public WebSearch() {
        super();
        ergebnis = new ArrayList<String>();
        count = 0;
    }

    public void search(String search, boolean ignoreCase) throws IOException {
        count = 0;
        ergebnis.clear();

        String line;
        String ic = ignoreCase ? "yes" : "no";
        String param = "?ignorecase=" + ic + "&search="
                + URLEncoder.encode(search, "UTF-8");
        URLConnection h = new URL(wwwurl + param).openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                h.getInputStream()));
        while ((line = in.readLine()) != null) {
            ergebnis.add(line);
            // System.err.print(ergebnis.size());
            // System.err.println(ergebnis.lastElement());
        }
        if (ergebnis.size() > 1)
            Collections.sort(ergebnis, cmpor);
    }

    /**
     * Set the listdata of the given list using the search results. If the
     * search fails an errorbox is displayed and false is returned. Otherwise
     * true is returned.
     */
    public boolean setListData(ISGCIMainFrame parent, NodeList<GraphClass> list) {
        String text = getText();
        if (text.length() == 0) {
            list.setListData(DataSet.getClasses());
        } else {
            boolean error = false;
            try {
                search(text, true);
            } catch (Exception e) {
                MessageDialog.error(parent,
                        "Search failed: Can't connect to server.");
                error = true;
            }
            if (error)
                return false;

            list.setListData(this);
        }
        return true;
    }

    public boolean hasNext() {
        return count < ergebnis.size();
    }

    public Object next() {
        if (count < ergebnis.size()) {
            return DataSet.getClass(ergebnis.get(count++));
        } else
            throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

/* EOF */
