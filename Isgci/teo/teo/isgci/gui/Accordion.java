package teo.isgci.gui;
//author: Nicolas Siebeck
//date: 07.06.13

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.javaswingcomponents.accordion.JSCAccordion;
import com.javaswingcomponents.accordion.TabOrientation;
import com.javaswingcomponents.accordion.listener.AccordionEvent;
import com.javaswingcomponents.accordion.listener.AccordionListener;
import com.javaswingcomponents.accordion.plaf.steel.SteelAccordionUI;
import com.javaswingcomponents.framework.painters.configurationbound.GradientColorPainter;
import com.javaswingcomponents.framework.painters.configurationbound.GradientDirection;

@SuppressWarnings("serial")
public class Accordion extends JPanel{

//	private JSCAccordion acc;
//	private BasicArrowButton button;
	JEditorPane thePane1 = new JEditorPane("text/html", " ");
	JEditorPane thePane2 = new JEditorPane("text/html", " "); 
    JEditorPane thePane3 = new JEditorPane("text/html", " ");                



	

	public static void main(String[] args) {
	}

	
	
	public Accordion() {		
		
		final JSCAccordion acc = new JSCAccordion();
		acc.setTabOrientation(TabOrientation.VERTICAL);//Unser Accordion ist vertikal
		addtabs(acc);
		changeListener(acc);
		customizeLook(acc);//Farben und Aussehen des Accordions werden hier ver�ndert
		setLayout(new BorderLayout());//F�r die zwei JPanels Button und JSCAccordion
		
		final BasicArrowButton button = new BasicArrowButton(SwingConstants.WEST); //Hide Button als Pfeil
		button.setBorder(new LineBorder(Color.BLACK, 2));
		button.setBackground(new Color(255,255,255));
		button.setText("Hide Details");
		
		button.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent e){
				setVisible(false);

	        }
	    });
		
		add(button, BorderLayout.PAGE_START);
		add(acc, BorderLayout.CENTER);

	}
	

	public void setContent(String url){
		try {
			String[] text = fetchurl(url);
			
			String panelTextBegin = "<html><head><style type='text/css'>body {background: white; } ul {list-style-type: none;} div {display: none;} </style></head> <body>";
			String panelTextEnd ="</body></html>"; 

			
	        thePane1.setOpaque(false);
	        thePane1.setEditable(false);
//	        thePane1.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
			
	        thePane2.setOpaque(false);
	        thePane2.setEditable(false);
//	        thePane2.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
			
	        thePane3.setOpaque(false);
	        thePane3.setEditable(false);
//	        thePane3.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
	        
	        
	        thePane1.addHyperlinkListener(new HyperlinkListener() {
	            public void hyperlinkUpdate(HyperlinkEvent e) {
	                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        System.out.println(e.getURL());
	                    if (Desktop.isDesktopSupported()) {
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
	        });
	        thePane2.addHyperlinkListener(new HyperlinkListener() {
	            public void hyperlinkUpdate(HyperlinkEvent e) {
	                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        System.out.println(e.getURL());
	                    if (Desktop.isDesktopSupported()) {
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
	        });
	        thePane3.addHyperlinkListener(new HyperlinkListener() {
	            public void hyperlinkUpdate(HyperlinkEvent e) {
	                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        System.out.println(e.getURL());
	                    if (Desktop.isDesktopSupported()) {
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
	        });
			
			
			//Achtung Name des Graphen text[0] fehlt noch!!!
			thePane1.setText(panelTextBegin + text[1] + text[2] + text[3] + text[4] + panelTextEnd);
			thePane2.setText(panelTextBegin + text[6] + text[7] + text[8] + panelTextEnd);
			thePane3.setText(panelTextBegin + text[10] + panelTextEnd);
			
			thePane1.setCaretPosition(0);
			thePane2.setCaretPosition(0);
			thePane3.setCaretPosition(0);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
		public String[] fetchurl(String url) throws IOException{
    	
    	String[] resultArray = new String[11];
    	org.jsoup.select.Elements elements;
    	
    	
   //fetch url
    	Document doc = Jsoup.connect(url).get();
    	
    	
   //Graphclass
    	resultArray[0] = "";//doc.select("h1").toString();
    	
    	// Definition Box
    	elements = doc.select("#definition>p");
    	resultArray[1] = "";
    	if(!elements.isEmpty()){
            resultArray[1] = elements.toString();

		    resultArray[1] = "<b>Definition:</b><br> <i>" + resultArray[1] + "</i>";
		    elements = null;
    	}
    	
    	// Equivalent Classes
    	elements = doc.select(".equivs>.classeslist>li>.graphclass");
    	resultArray[2] = "";
    	if(!elements.isEmpty()){
	    	for(Element e : elements){
		    		resultArray[2] += "<li>" + e.toString() + "</li>";
		    }
		    	
		    resultArray[2] = "<br><br><b>Equivalent classes:</b><br> <ul>" + resultArray[2] + "</ul>";
		    elements = null;
    	}
    	
    	// Complement Classes
    	elements = doc.select(".complements>.classeslist>li>.graphclass");
    	resultArray[3] = "";
    	if(!elements.isEmpty()){
	    	for(Element e : elements){
		    		resultArray[3] += "<li>" + e.toString() + "</li>";
		    }
		    	
		    resultArray[3] = "<br><b>Complement classes:</b><br> <ul>" + resultArray[3] + "</ul>";
		    elements = null;
    	}
    	
    	// Related Classes
    	elements = doc.select(".related>.classeslist>li>.graphclass");
    	resultArray[4] = "";
    	if(!elements.isEmpty()){
	    	for(Element e : elements){
		    		resultArray[4] += "<li>" + e.toString() + "</li>";
		    }
		    	
		    resultArray[4] = "<br><b>Related classes:</b><br> <ul>" + resultArray[4] + "</ul>";
		    elements = null;
    	}
    	
    	//Inclusions
    	resultArray[5] = ""; //doc.select("#problemssummary").toString();
    	
    	//Inclusions - Text
    	elements = doc.select("p > i");
    	resultArray[6] = "";
    	if(!elements.isEmpty()){
    		try{
    			resultArray[6] = doc.select("p > i").get(1).toString();
    		}catch(Exception e){
    			resultArray[6] = doc.select("p > i").get(0).toString();
    		}
		    elements = null;
    	}
    	
    	//Minimal Superclasses
    	elements = doc.select(".minsuper>.classeslist>li>.graphclass");
    	resultArray[7] = "";
    	if(!elements.isEmpty()){
	    	for(Element e : elements){
		    		resultArray[7] += "<li>" + e.toString() + "</li>";
		    }
		    	
		    resultArray[7] = "<br><br><b>Minimal superclasses:</b><br> <ul>" + resultArray[7] + "</ul>";
    	}
    	
    	//Maximal Subclasses
    	elements = doc.select(".maxsub>.classeslist>li>.graphclass");
    	resultArray[8] = "";
    	if(!elements.isEmpty()){
	    	for(Element e : elements){
		    		resultArray[8] += "<li>" + e.toString() + "</li>";
		    }
		    	
		    resultArray[8] = "<br><br><b>Maximal subclasses:</b><br> <ul>" + resultArray[8] + "</ul>";
    	}
    	
    	
    //Problems
    	resultArray[9] = ""; // doc.select("#problemssummary").toString();
    	
    	//Problems-Table
    	elements = doc.select("body>table>tbody>tr");
    	String resultString = "";
    	for(Element e : elements){
    		if(e.toString().matches("(?si).*\"tooltip\".*")){
    			String tablerow = e.toString().replaceAll("(?si)<div class=\"tooltip\">.*</div> </td>", "</td>");
    			resultString += tablerow.replaceAll("(?si)<td><a.*</a></td>",e.select("a").get(0).toString().replace("problem_", "/classes/problem_"));
    		}
    	}
    	resultArray[10] = resultString;

    	
    	
    	
    	//convert all relative links to absolute
    	for(int i = 0; i<11; i++){
    		resultArray[i] = resultArray[i].replace("/classes", "http://www.graphclasses.org/classes");
    	}

    	return resultArray;
    }
	
	

	/**
	 * Hier wird die Information �ber die Graphklasse an den Sidebar �bergeben
	 */
	private void addtabs(JSCAccordion accordion) {
		//Der Inhalt der Tabs
        
       	
		
		UIManager.put("ScrollBar.background", Color.WHITE);		
		JScrollPane bla1 = new JScrollPane (thePane1);
		JScrollPane bla2 = new JScrollPane (thePane2);		
		JScrollPane bla3 = new JScrollPane (thePane3);
		
		bla1.setOpaque(false);
		bla1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		bla2.setOpaque(false);
		bla2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		bla3.setOpaque(false);
		bla3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		

		accordion.addTab("Graphclass", bla1);
		accordion.addTab("Inclusion", bla2);
		accordion.addTab("Problems", bla3);
		
		thePane1.setCaretPosition(0);
		thePane2.setCaretPosition(0);
		thePane3.setCaretPosition(0);

		
		
	}
	
	
	private void changeListener(JSCAccordion accordion) {
		accordion.addAccordionListener(new AccordionListener() {
			
			@Override
			public void accordionChanged(AccordionEvent accordionEvent) {
				//available fields on accordionEvent.
				switch (accordionEvent.getEventType()) {
				case TAB_ADDED: {
					//add your logic here to react to a tab being added.
					break;
				}
				case TAB_REMOVED: {
					//add your logic here to react to a tab being removed.
					break;					
				}
				case TAB_SELECTED: {
					//add your logic here to react to a tab being selected.
					break;					
				}
				}
			}
		});
	}
	
	
	private void customizeLook(JSCAccordion accordion){
		accordion.setDrawShadow(false);
		
		SteelAccordionUI steelAccordionUI = (SteelAccordionUI) accordion.getUI();
				
		//Padding vom Standard Accordion wegmachen
		steelAccordionUI.setHorizontalBackgroundPadding(0);
		steelAccordionUI.setVerticalBackgroundPadding(0);
		steelAccordionUI.setHorizontalTabPadding(0);
		steelAccordionUI.setVerticalTabPadding(0);
		steelAccordionUI.setTabPadding(0);
		
		//Background Painter wei�, geht vermutlich auch einfacher...
		GradientColorPainter backgroundPainter = new GradientColorPainter();
		backgroundPainter.setGradientDirection(GradientDirection.HORIZONTAL);
		backgroundPainter.setStartColor(new Color(255,255,255));
		backgroundPainter.setEndColor(new Color(255,255,255));
		accordion.setBackgroundPainter(backgroundPainter);
		
		//Selbiges f�r die Tabs geschieht in der Klasse MyAccordionTabRenderer
		accordion.setVerticalAccordionTabRenderer(new MyAccordionTabRenderer());
		
	}
	
}