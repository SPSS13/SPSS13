package teo.isgci.gui;

//author: Nicolas Siebeck, Matthias Kraus
//date: 07.06.13

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import teo.isgci.db.DataSet;
import teo.isgci.problem.Problem;

import com.javaswingcomponents.accordion.JSCAccordion;
import com.javaswingcomponents.accordion.JSCAccordion.TabInformation;
import com.javaswingcomponents.accordion.TabOrientation;
import com.javaswingcomponents.accordion.listener.AccordionEvent;
import com.javaswingcomponents.accordion.listener.AccordionListener;
import com.javaswingcomponents.accordion.plaf.steel.SteelAccordionUI;

@SuppressWarnings("serial")
public class Accordion extends JPanel implements Runnable {

    private JEditorPane GraphclassPane = new JEditorPane("text/html", " "); // Graphclasses
    private JEditorPane InclusionPane = new JEditorPane("text/html", " "); // Inclusions
    private JEditorPane ProblemsPane = new JEditorPane("text/html", " "); // Problems

    private HyperlinkListener listener;

    private String actualContent;
    private ISGCIMainFrame mainFrame;

    public Accordion(final ISGCIMainFrame mainFrame) {
        this.mainFrame = mainFrame;

		/*
		 * Set default-content of Sidebar html possible here!!
		 */
		String defaultTextSidebar = "<div style='margin:10px;'>Please select Graph-Class...<div>";
        GraphclassPane.setText(defaultTextSidebar);
        InclusionPane.setText(defaultTextSidebar);
        ProblemsPane.setText(defaultTextSidebar);

        final JSCAccordion acc = new JSCAccordion();
        acc.setTabOrientation(TabOrientation.VERTICAL);
        addtabs(acc);
        changeListener(acc);
        customizeLook(acc);
        setLayout(new BorderLayout());

        final BasicArrowButton button = new BasicArrowButton(
                SwingConstants.WEST); // Hide Button as Arrow
        button.setBorder(new LineBorder(Color.BLACK, 2));
        // button.setBackground(new Color(255, 255, 255));

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                mainFrame.visibleHaken.setState(false);
                mainFrame.getContentPane().add("West", mainFrame.button2);
                mainFrame.button2.setVisible(true);
            }
        });

        add(acc, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
        validate();
    }

    protected boolean visibilityChanged = false;
    protected boolean contentChanged = false;

    public void actualize() {
        if (visibilityChanged) {
            visibilityChanged = false;
            toggleVisibility();
            if (mainFrame.graphCanvas.getSelectedCell() != null) {
                setContent(((GraphClassSet)mainFrame.graphCanvas
                        .getSelectedCell().getValue()).getLabel().getID());
            }

        }
        if (contentChanged) {
            if (mainFrame.sidebar.isVisible()) {
                contentChanged = false;
                setContent(actualContent);
            }
        }

    }

    @Override
    public void run() {
        while (true) {
            actualize();
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Toggle Sidebar (switch in, out)
     */
    public void toggleVisibility() {
        if (this.isVisible()) {
            this.setVisible(false);
            mainFrame.getContentPane().add("West", mainFrame.button2);
            mainFrame.button2.setVisible(true);
        } else {
            mainFrame.button2.setVisible(false);
            mainFrame.getContentPane().add("West", mainFrame.sidebar);
            mainFrame.sidebar.setVisible(true);
        }
    }

    public String getContent() {
        return actualContent;
    }

    /*
     * Set content of Sidebar - given Graph-Class-Nr. is used to generate the
     * web-link to the specific Graph-Class on Graphclasses.org.
     */
    private String loadingLogo = "<div style='text-align: center; margin-top: 50px;'><img src='file:data/images/loading.gif'></div>";

    public void setContent(String u) {

        /*
         * Display Loading-Logo while refreshing content
         */
        GraphclassPane.setText(loadingLogo);
        InclusionPane.setText(loadingLogo);
        ProblemsPane.setText(loadingLogo);

        String url = "http://www.graphclasses.org/classes/" + u; // generating
                                                                 // url
        actualContent = u; // Saves current Nr. of shown content (Used for
                           // abortion of show-request for the same node)
        try {

            /*
             * A Parser is used to formate the Homepage-Content. The Array
             * "text" contains now several html-blocks to be shown in the
             * panels.
             */
            String[] text = fetchurl(url);

            /*
             * Following two rows setting html-envoirement for embedded
             * html-snippets
             */
            String panelTextBegin = "<html><head><style type='text/css'> a {text-decoration: none;} body {margin: 7px;} ul {list-style-type: none;} div {display: none;} .sub {border:1px solid red;} .overlinedTd {border-top:1px solid blue; margin-right:-5; margin-left:-5; padding-top:-3; height:10px;} .normalTd {padding-top:-3;}</style></head> <body>"; // Overline:
                                                                                                                                                                                                                                                                                                                                                                  // A&#x305;
            String panelTextEnd = "</body></html>";

            GraphclassPane.setOpaque(true);
            GraphclassPane.setEditable(false);

            InclusionPane.setOpaque(true);
            InclusionPane.setEditable(false);

            ProblemsPane.setOpaque(true);
            ProblemsPane.setEditable(false);
            /*
             * listeners must be removed before creating new one - otherwise
             * multiple listeners would react on hyperlink-clicks.
             */
            GraphclassPane.removeHyperlinkListener(listener);
            InclusionPane.removeHyperlinkListener(listener);
            ProblemsPane.removeHyperlinkListener(listener);
            //

            listener = new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        /*
                         * Link is a function-call: Coloring of problems
                         * implemented as link-call. Triggers coloring of the
                         * selected problem.
                         */
                        if (e.getURL().toString().substring(6, 9).equals("do_")) {
                            try {
                                /*
                                 * Set Color in Graph
                                 */
                                String problem = e.getURL().toString()
                                        .substring(11);
                                mainFrame.graphCanvas.setProblem(DataSet
                                        .getProblem(problem));
                                /*
                                 * Set Selection in Menu: Problems - Color for
                                 * Problems
                                 */
                                String problemnumber = e.getURL().toString()
                                        .substring(9, 11);
                                Enumeration<AbstractButton> x = ((ProblemsMenu)mainFrame.miColourProblem).group
                                        .getElements();
                                for (int i = 0; i < Integer
                                        .parseInt(problemnumber) + 2; i++) {
                                    x.nextElement().setSelected(true);
                                }
                            } catch (Exception E) {
                            }
                        }
                        /*
                         * If a browser is available on the user's system, it
                         * starts and opens up the requested url.
                         */
                        else if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(e.getURL().toURI());
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            } catch (URISyntaxException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    }

                }
            };

            //
            GraphclassPane.addHyperlinkListener(listener);
            InclusionPane.addHyperlinkListener(listener);
            ProblemsPane.addHyperlinkListener(listener);

            /*
             * Now the content of the three panels is set. 1. Graphclasses:
             * Definition + Equivalent Classes + Complement Classes + Related
             * Classes 2. Inclusions: Introduction + Minimal Superclasses +
             * Maximal Subclasses 3. Problems: Table of Problems
             */
            GraphclassPane.setText(panelTextBegin + text[0] + text[1] + text[2]
                    + text[3] + text[4] + panelTextEnd);
            InclusionPane.setText(panelTextBegin + text[0] + text[6] + text[7]
                    + text[8] + panelTextEnd);
            ProblemsPane.setText(panelTextBegin + text[0] + text[10]
                    + panelTextEnd);

            GraphclassPane.setCaretPosition(0);
            InclusionPane.setCaretPosition(0);
            ProblemsPane.setCaretPosition(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Fetches html from given URL of Graph-Class, bunches the information in
	 * seperate html-blocks and returns them as a String-Array JSoup is used to
	 * parse the html.
	 */
	public String[] fetchurl(String url) throws IOException {

        String[] resultArray = new String[11];
        org.jsoup.select.Elements elements;
        org.jsoup.select.Elements links;
        int counter = 0;

        // fetch html-document from url
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();

        // Graphclass-Sections
        elements = doc.select("h1");
        resultArray[0] = elements.toString().replace("<h1>", "")
                .replace("</h1>", "");

		resultArray[0] = resultArray[0]
				.replace(
						"<span class=\"complement\">",
						"</b></td><td style=\"font:bold; border-top:1px solid; margin-right:-5; margin-left:-5; padding-top:-3; height:10px;\">");
		resultArray[0] = resultArray[0].replace("</span>",
				"</b></td><td style=\"padding-top:-3; font:bold;\"><b>");
		resultArray[0] = "<div style='border-bottom: 1px solid;margin-left: -2 cm; width:100cm;'><table><tr><td style=\"padding-top:-3;\"><b>"
				+ resultArray[0] + "</b></td></tr></table></div><br>";

		System.out.println(resultArray[0]);
        // Definition Box
        elements = doc.select("#definition>p"); // Selects the p-html-box from
                                                // all blocks with the id:
                                                // definition
        resultArray[1] = "";
        if (!elements.isEmpty()) {
            resultArray[1] = elements.toString();

            resultArray[1] = "<b>Definition:</b><br> <i>" + resultArray[1]
                    + "</i>"; // adds the <i></i> tags for italic style
            elements = null;
        }

        // Equivalent Classes
        elements = doc.select(".equivs>.classeslist>li>.graphclass");
        links = doc.select(".equivs>.classeslist>li>.graphclass>a[href]");

        resultArray[2] = "";
        if (!elements.isEmpty()) {
            for (Element e : elements) {
                String link = links.get(counter).attr("href").toString();

                String liElement;

                // Kill link-tag
                liElement = e.toString()
                        .replace("<a href=\"" + link + "\">", "")
                        .replace("</a>", "");
                liElement = liElement.replace("<span class=\"complement\">",
                        "</a></td><td class=\"overlinedTd\"><a href=\"" + link
                                + "\">");
                liElement = liElement.replace("</span>",
                        "</a></td><td class=\"normalTd\"><a href=\"" + link
                                + "\">");
                counter++;
                resultArray[2] += "<li><div style=\"width:80cm;\"><table><tr><td class=\"normalTd\"><a href=\""
                        + link
                        + "\">"
                        + liElement
                        + "</a></td></tr></table></div></li>";
            }
            resultArray[2] = "<br><b>Equivalent classes:</b><br> <ul>"
                    + resultArray[2] + "</ul>";
            elements = null;
            links = null;
            counter = 0;
        }

        // Complement Classes
        elements = doc.select(".complements>.classeslist>li>.graphclass");
        links = doc.select(".complements>.classeslist>li>.graphclass>a[href]");

        resultArray[3] = "";
        if (!elements.isEmpty()) {
            for (Element e : elements) {
                String link = links.get(counter).attr("href").toString();

                String liElement;

                // Kill link-tag
                liElement = e.toString()
                        .replace("<a href=\"" + link + "\">", "")
                        .replace("</a>", "");
                liElement = liElement.replace("<span class=\"complement\">",
                        "</a></td><td class=\"overlinedTd\"><a href=\"" + link
                                + "\">");
                liElement = liElement.replace("</span>",
                        "</a></td><td class=\"normalTd\"><a href=\"" + link
                                + "\">");
                counter++;
                resultArray[3] += "<li><div style='width:80cm;'><table><tr><td class=\"normalTd\"><a href=\""
                        + link
                        + "\">"
                        + liElement
                        + "</a></td></tr></table></div></li>";
            }
            resultArray[3] = "<br><b>Complement classes:</b><br> <ul>"
                    + resultArray[3] + "</ul>";
            elements = null;
            links = null;
            counter = 0;
        }

        // Related Classes
        elements = doc.select(".related>.classeslist>li>.graphclass>a");
        links = doc.select(".related>.classeslist>li>.graphclass>a[href]");

        resultArray[4] = "";
        if (!elements.isEmpty()) {
            for (Element e : elements) {
                String link = links.get(counter).attr("href").toString();// .replace("/classes/",
                                                                         // "http://www.graphclasses.org/classes/");

                String liElement;

                // Kill link-tag
                liElement = e.toString()
                        .replace("<a href=\"" + link + "\">", "")
                        .replace("</a>", "");
                liElement = liElement.replace("<span class=\"complement\">",
                        "</a></td><td class=\"overlinedTd\"><a href=\"" + link
                                + "\">");
                liElement = liElement.replace("</span>",
                        "</a></td><td class=\"normalTd\"><a href=\"" + link
                                + "\">");
                counter++;
                resultArray[4] += "<li><div style='width:80cm;'><table><tr><td class=\"normalTd\"><a href=\""
                        + link
                        + "\">"
                        + liElement
                        + "</a></td></tr></table></div></li>";
            }
            resultArray[4] = "<br><b>Related classes:</b><br> <ul>"
                    + resultArray[4] + "</ul>";
            elements = null;
            links = null;
            counter = 0;
        }

        // Inclusions-Sections
        resultArray[5] = ""; // doc.select("#problemssummary").toString();

        // Inclusions - Text
        /*
         * No specified tags available - searches possible introduction in
         * html-doc
         */
        elements = doc.select("p > i");
        resultArray[6] = "";
        if (!elements.isEmpty()) {
            try {
                resultArray[6] = doc.select("p > i").get(1).toString();
            } catch (Exception e) {
                resultArray[6] = doc.select("p > i").get(0).toString();
            }
            elements = null;
        }

        // Minimal Superclasses
        elements = doc.select(".minsuper>.classeslist>li>.graphclass");
        links = doc.select(".minsuper>.classeslist>li>.graphclass>a[href]");

        resultArray[7] = "";
        if (!elements.isEmpty()) {
            for (Element e : elements) {
                String link = links.get(counter).attr("href").toString();// .replace("/classes/",
                                                                         // "http://www.graphclasses.org/classes/");

                String liElement;

                // Kill link-tag
                liElement = e.toString()
                        .replace("<a href=\"" + link + "\">", "")
                        .replace("</a>", "");
                liElement = liElement.replace("<span class=\"complement\">",
                        "</a></td><td class=\"overlinedTd\"><a href=\"" + link
                                + "\">");
                liElement = liElement.replace("</span>",
                        "</a></td><td class=\"normalTd\"><a href=\"" + link
                                + "\">");
                counter++;
                resultArray[7] += "<li><div style='width:80cm'><table><tr><td class=\"normalTd\"><a href=\""
                        + link
                        + "\">"
                        + liElement
                        + "</a></td></tr></table></div></li>";
            }
            resultArray[7] = "<br><br><b>Minimal superclasses:</b><br> <ul>"
                    + resultArray[7] + "</ul>";
            elements = null;
            links = null;
            counter = 0;
        }

        // Maximal Subclasses
        elements = doc.select(".maxsub>.classeslist>li>.graphclass");
        links = doc.select(".maxsub>.classeslist>li>.graphclass>a[href]");

        resultArray[8] = "";
        if (!elements.isEmpty()) {
            for (Element e : elements) {
                String link = links.get(counter).attr("href").toString();// .replace("/classes/",
                                                                         // "http://www.graphclasses.org/classes/");

                String liElement;

                // Kill link-tag
                liElement = e.toString()
                        .replace("<a href=\"" + link + "\">", "")
                        .replace("</a>", "");
                liElement = liElement.replace("<span class=\"complement\">",
                        "</a></td><td class=\"overlinedTd\"><a href=\"" + link
                                + "\">");
                liElement = liElement.replace("</span>",
                        "</a></td><td class=\"normalTd\"><a href=\"" + link
                                + "\">");
                counter++;
                resultArray[8] += "<li><div style=\"width:80cm;\"><table><tr><td class=\"normalTd\"><a href=\""
                        + link
                        + "\">"
                        + liElement
                        + "</a></td></tr></table></div></li>";
            }
            resultArray[8] = "<br><b>Maximal subclasses:</b><br> <ul>"
                    + resultArray[8] + "</ul>";
            elements = null;
            links = null;
            counter = 0;
        }

        // Problems-Sections
        resultArray[9] = ""; // doc.select("#problemssummary").toString();

        // Problems-Table
        /*
         * Select the table. Reduce it by applying several regular expressions
         * to it. Convert the links to the applicable format
         */
        elements = doc.select("body>table>tbody>tr");
        String resultString = "";
        for (Element e : elements) {
            if (e.toString().matches("(?si).*\"tooltip\".*")) {
                String tablerow = e.toString().replaceAll(
                        "(?si)<div class=\"tooltip\">.*</div> </td>", "</td>");
                resultString += tablerow.replaceAll(
                        "(?si)<td><a.*</a></td>",
                        e.select("a").get(0).toString()
                                .replace("problem_", "/classes/problem_"));
            }
        }
        resultArray[10] = resultString;

        /*
         * Insert links for coloring.
         */
        String problemname = "";
        String problemnumber = "";
        for (int i = 0; i < DataSet.problems.size(); i++) {
            if (i < 10) {
                problemnumber = "0";
            }
            problemnumber += i;
            problemname = ((Problem)DataSet.problems.elementAt(i)).getName();
            resultArray[10] = resultArray[10].replace("<td>" + problemname
                    + " \n  </td>", "<td><a href=\"http:\\do_" + problemnumber
                    + problemname + "\">" + problemname + "</a></td>");
            problemnumber = "";
        }

        for (int i = 0; i < 11; i++) {
            // convert all relative links to absolute
            resultArray[i] = resultArray[i].replace("/classes",
                    "http://www.graphclasses.org/classes");
            // adjust image paths
            resultArray[i] = resultArray[i].replace("<img src=\"images/",
                    "<img style=\"color:#ffffff;\" src=\"file:data/images/");
        }

        return resultArray;
    }

    /**
     * Hier wird die Information �ber die Graphklasse an den Sidebar
     * �bergeben
     */
    private void addtabs(JSCAccordion accordion) {
        // Der Inhalt der Tabs

        UIManager.put("ScrollBar.background", new Color(240, 240, 240));
        JScrollPane bla1 = new JScrollPane(GraphclassPane);
        JScrollPane bla2 = new JScrollPane(InclusionPane);
        JScrollPane bla3 = new JScrollPane(ProblemsPane);

        bla1.setOpaque(false);
        bla1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        bla1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bla2.setOpaque(false);
        bla2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        bla2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        bla3.setOpaque(false);
        bla3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        accordion.addTab("    Graphclass", bla1);
        accordion.addTab("    Inclusion", bla2);
        accordion.addTab("    Problems", bla3);

        ArrayList<TabInformation> tabs = (ArrayList<TabInformation>)accordion
                .getTabs();
        for (TabInformation tab : tabs) {
            // GetTabComponentParameter
            tab.getContents().setBackground(Color.white);
        }

        GraphclassPane.setCaretPosition(0);
        InclusionPane.setCaretPosition(0);
        ProblemsPane.setCaretPosition(0);
        validate();
    }

    private void changeListener(JSCAccordion accordion) {
        accordion.addAccordionListener(new AccordionListener() {

            @Override
            public void accordionChanged(AccordionEvent accordionEvent) {
                // available fields on accordionEvent.
                switch (accordionEvent.getEventType()) {
                case TAB_ADDED: {
                    // add your logic here to react to a tab being added.
                    break;
                }
                case TAB_REMOVED: {
                    // add your logic here to react to a tab being removed.
                    break;
                }
                case TAB_SELECTED: {
                    // add your logic here to react to a tab being selected.
                    break;
                }
                }
            }
        });
    }

    private void customizeLook(JSCAccordion accordion) {
        accordion.setDrawShadow(true);

        SteelAccordionUI steelAccordionUI = (SteelAccordionUI)accordion.getUI();

        // Padding vom Standard Accordion wegmachen
        steelAccordionUI.setHorizontalBackgroundPadding(0);
        steelAccordionUI.setVerticalBackgroundPadding(0);
        steelAccordionUI.setHorizontalTabPadding(0);
        steelAccordionUI.setVerticalTabPadding(0);
        steelAccordionUI.setTabPadding(0);

        // Selbiges for die Tabs geschieht in der Klasse
        accordion.setVerticalAccordionTabRenderer(new MyAccordionTabRenderer());

    }

    public void visibilityChanged() {
        visibilityChanged = true;
    }

    public void changeContent(String NodeID) {
        actualContent = NodeID;
        contentChanged = true;
    }

}
