/*
 * Export dialog.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ExportDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Element;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

//TODO make import work
public class ExportDialog extends JDialog implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7006637049631986533L;
    /** The card titles (and ids) */
    protected static final String CARD_FORMAT = "Please choose the file format";
    protected static final String CARD_PS = "Postscript options";
    protected static final String CARD_GML = "GraphML options";
    protected static final String CARD_FILE = "Destination file";
    protected String current;

    /* Global items */
    protected ISGCIMainFrame parent;
    protected JLabel title;
    protected JPanel cardPanel;
    protected CardLayout cardLayout;
    protected JButton backButton, nextButton, cancelButton;

    /* Format items */
    protected ButtonGroup formats;
    protected JRadioButton radioPS, radioGML, radioSVG;

    /* Postscript items */
    protected JCheckBox fittopage, keepsideratio, rotate, color;

    /* GraphML items */
    protected JRadioButton gmlPlain, gmlYed, gmlHtml, gmlLatex;

    /* Save location items */
    protected JFileChooser file;

    public ExportDialog(ISGCIMainFrame parent) {
        super(parent, "Export drawing", true);
        this.parent = parent;

        Container content = getContentPane();
        JPanel buttonPanel = new JPanel();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        backButton = new JButton("< Back");
        nextButton = new JButton("Next >");
        cancelButton = new JButton("Cancel");

        backButton.addActionListener(this);
        nextButton.addActionListener(this);
        cancelButton.addActionListener(this);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

        title = new JLabel("");
        Font f = title.getFont();
        title.setFont(f.deriveFont((float)(f.getSize() * 1.2)));
        title.setOpaque(true);
        title.setBackground(Color.darkGray);
        title.setForeground(Color.white);
        title.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 40, 5, 40)));
        content.add(title, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.SOUTH);
        content.add(cardPanel, BorderLayout.CENTER);

//        cardPanel.add(cardFormat(), CARD_FORMAT);
//        cardPanel.add(cardPS(), CARD_PS);
//        cardPanel.add(cardGML(), CARD_GML);
        cardPanel.add(cardFile(), CARD_FILE);

        showCard(CARD_FILE);
    }

    /**
     * Show the given card and adjust button settings etc. for it.
     */
    protected void showCard(String card) {
        title.setText(card);
        current = card;
        cardLayout.show(cardPanel, card);
        backButton.setEnabled(card != CARD_FORMAT);
        // nextButton.setText(card == CARD_FILE ? "Export" : "Next >");

        // Use the JFileChooser buttons instead, to prevent errors when the
        // user clicks Next, but didn't press Return after typing a file name.
        nextButton.setVisible(card != CARD_FILE);
        cancelButton.setVisible(card != CARD_FILE);
    }

    // /**
    // * Return the card where the user can select the file format.
    // */
    // private Component cardFormat() {
    // Box box = new Box(BoxLayout.Y_AXIS);
    //
    // formats = new ButtonGroup();
    //
    // radioPS = new JRadioButton("Postscript ( .ps)");
    // radioPS.setAlignmentX(Component.LEFT_ALIGNMENT);
    // formats.add(radioPS);
    // box.add(radioPS);
    // box.add(explText("A Postscript file can be included immediately in e.g. LaTeX\n"
    // + "documents, but it cannot easily be edited."));
    //
    // radioSVG = new JRadioButton("Structured Vector Graphics ( .svg)");
    // radioSVG.setAlignmentX(Component.LEFT_ALIGNMENT);
    // formats.add(radioSVG);
    // box.add(radioSVG);
    // box.add(explText("An SVG file is suitable for editing the diagram, e.g. with\n"
    // + "inkscape (http://www.inkscape.org), but cannot be included\n"
    // + "directly in LaTeX."));
    //
    // radioGML = new JRadioButton("GraphML ( .graphml)");
    // radioGML.setAlignmentX(Component.LEFT_ALIGNMENT);
    // formats.add(radioGML);
    // box.add(radioGML);
    // box.add(explText("A graphml file contains the structure of the graph and is\n"
    // + "suitable for processing by many graph tools, but does not\n"
    // + "contain layout information and cannot be included directly\n"
    // + "in LaTeX. Editing and laying out can be done with e.g. yEd.\n"
    // + "(http://www.yworks.com)"));
    //
    // radioPS.setSelected(true);
    //
    // JPanel p = new JPanel();
    // p.add(box, BorderLayout.CENTER);
    // return p;
    // }

    // /**
    // * Return the card where the user can set Postscript options
    // */
    // private Component cardPS() {
    // Box box = new Box(BoxLayout.Y_AXIS);
    //
    // fittopage = new JCheckBox("Fit to page", true);
    // box.add(fittopage);
    //
    // keepsideratio = new JCheckBox("Keep side ratio", true);
    // box.add(keepsideratio);
    //
    // rotate = new JCheckBox("Rotate 90 degrees", false);
    // box.add(rotate);
    //
    // color = new JCheckBox("Colour", false);
    // box.add(color);
    //
    // Box box2 = new Box(BoxLayout.X_AXIS);
    //
    // JLabel label = new JLabel("Paper size:", JLabel.LEFT);
    // label.setBorder(new EmptyBorder(new Insets(0, 0, 0, 10)));
    // box2.add(label);
    //
    // papersize = new JComboBox();
    // papersize.addItem("A4");
    // papersize.addItem("A3");
    // papersize.addItem("Letter");
    // papersize.addItem("Legal");
    // papersize.addItem("Tabloid");
    // papersize.setMaximumSize(papersize.getPreferredSize());
    // box2.add(papersize);
    // box.add(box2);
    //
    // JPanel p = new JPanel();
    // p.add(box, BorderLayout.CENTER);
    // return p;
    // }

    /**
     * Return the card where the user can set GraphML options
     */
//    private Component cardGML() {
//        ButtonGroup b;
//        Box box = new Box(BoxLayout.Y_AXIS);
//
//        b = new ButtonGroup();
//
//        gmlPlain = new JRadioButton("Plain graphml");
//        gmlPlain.addActionListener(this);
//        b.add(gmlPlain);
//        box.add(gmlPlain);
//        box.add(explText("This contains the class names in Latex format as comments\n"
//                + "and the relations between the classes."));
//
//        gmlYed = new JRadioButton("Graphml for yEd");
//        gmlYed.setSelected(true);
//        gmlYed.addActionListener(this);
//        b.add(gmlYed);
//        box.add(gmlYed);
//        box.add(explText("This contains class names as labels of the nodes, styled\n"
//                + "arrows for (un)proper inclusions and colourings for\n"
//                + "algorithmic complexity. Note that for yEd the file must have\n"
//                + "extension .graphml."));
//
//        b = new ButtonGroup();
//        box.add(Box.createRigidArea(new Dimension(0, 20)));
//
//        gmlHtml = new JRadioButton("Html labels");
//        gmlHtml.setSelected(true);
//        b.add(gmlHtml);
//        box.add(gmlHtml);
//        box.add(explText("Class names are formatted using html"));
//
//        gmlLatex = new JRadioButton("Latex labels");
//        b.add(gmlLatex);
//        box.add(gmlLatex);
//        box.add(explText("Class names are unformatted LaTeX code"));
//
//        JPanel p = new JPanel();
//        p.add(box, BorderLayout.CENTER);
//        return p;
//    }

    /**
     * Return the card where the user can select the destination file.
     */
    private Component cardFile() {
        file = new JFileChooser();
        file.setApproveButtonText("Export");
        file.addActionListener(this);
        // file.setControlButtonsAreShown(false); doesn't work - see showCard()
        return file;
    }

//    /**
//     * Returns a component with explanation of e.g. a radiobutton.
//     */
//    private Component explText(String text) {
//        JTextArea t = new JTextArea(text);
//        // t.setLineWrap(true);
//        // t.setWrapStyleWord(true);
//        t.setEditable(false);
//        t.setAlignmentX(Component.LEFT_ALIGNMENT);
//        t.setOpaque(false);
//        t.setBorder(new EmptyBorder(new Insets(0, 20, 0, 0)));
//        return t;
//    }

    public void closeDialog() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton)
            closeDialog();
        if (e.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
            if (export())
                closeDialog();
        } else if (e.getActionCommand() == JFileChooser.CANCEL_SELECTION)
            closeDialog();
    }

    /**
     * Export using the entered settings. Return true iff no error occured.
     */
    protected boolean export() {
        boolean res = true;
        FileOutputStream f;
        try {
            f = new FileOutputStream(file.getSelectedFile());
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.error(parent, "Cannot open file for writing:\n"
                    + file.getSelectedFile().getPath());
            return false;
        }

        try {
            exportSVG(f);
        } catch (Exception e) {
            res = false;
            e.printStackTrace();
            MessageDialog.error(parent,
                    "Error while exporting:\n" + e.toString());
        }
        return res;
    }

    /**
     * Export to Postscript.
     */

    // protected void exportPS(FileOutputStream f) throws Exception {
    // Exception res = null;
    // byte[] outstr;
    // DataOutputStream out = null;
    //
    // try {
    // out = new DataOutputStream(f);
    // final PSGraphics g = new PSGraphics(
    // (String) papersize.getSelectedItem(),
    // fittopage.isSelected(), keepsideratio.isSelected(),
    // rotate.isSelected(), color.isSelected());
    // mxGraphics2DCanvas canvas = (mxGraphics2DCanvas) mxCellRenderer
    // .drawCells(parent.graphCanvas.getGraph(), null, 1, null,
    // new CanvasFactory() {
    // // public mxICanvas createCanvas(int width,
    // // int height) {
    // // mxGraphics2DCanvas canvas = new mxGraphics2DCanvas(
    // // g.getG());
    // // canvas.setDrawLabels(true);
    // // return canvas;
    // }
    //
    // });
    //
    // for (Object cell : parent.graphCanvas.getGraph().getChildCells(
    // parent.graphCanvas.getGraph().getDefaultParent())) {
    // //
    // canvas.drawCell(parent.graphCanvas.getGraph().getModel().getStyle((((mxCell)cell))));
    // }
    //
    // // outstr = g.getG().getBytes();
    // g.dispose();
    // // out.write(outstr);
    // // } catch (IOException ex) {
    // // res = ex;
    // } finally {
    // out.close();
    // }
    //
    // if (res != null)
    // throw res;
    // }

    // /**
    // * Export to GraphML.
    // */
    // protected void exportGML(FileOutputStream f) throws Exception {
    // Exception res = null;
    // String outstr;
    // Writer out = null;
    //
    // try {
    // out = new OutputStreamWriter(f, "UTF-8");
    // GraphMLWriter w = new GraphMLWriter(out,
    // gmlYed.isSelected() ? GraphMLWriter.MODE_YED
    // : GraphMLWriter.MODE_PLAIN,
    // parent.graphCanvas.drawUnproper, gmlHtml.isSelected());
    //
    // w.startDocument();
    // // parent.graphCanvas.write(w);
    // w.endDocument();
    // } catch (IOException ex) {
    // res = ex;
    // } finally {
    // out.close();
    // }
    //
    // if (res != null)
    // throw res;
    // }

    /**
     * Export to SVG.
     */
    protected void exportSVG(FileOutputStream f) throws Exception {

        final int mywidth = (int)(parent.graphCanvas.getGraphControl()
                .getWidth() * (parent.graphCanvas.getZoomFactor()));
        System.out.println(parent.graphCanvas.getZoomFactor());
        System.out.println(mywidth);
        final int myheight = (int)(parent.graphCanvas.getGraphControl()
                .getHeight() * (parent.graphCanvas.getZoomFactor()));
        System.out.println(myheight);
        Exception res = null;
        // DataOutputStream out = new DataOutputStream(f);
        Writer out = new OutputStreamWriter(f, "UTF-8");

        HashSet<Object> set = new HashSet<Object>();

        Map<String, Object> map = ((mxGraphModel) parent.graphCanvas
                .getGraph().getModel()).getCells();
        for (Object o : map.values()) {
            set.add(o);
        }

        Object[] toDraw = set.toArray();

        try {
            final ISGCISvgCanvas can = new ISGCISvgCanvas(
                    mxDomUtils.createSvgDocument(mywidth, myheight));
            mxCellRenderer.drawCells(new mxGraph(), toDraw, 1, null,
                    new CanvasFactory() {

                        @Override
                        public mxICanvas createCanvas(int mywidth, int myheight) {

                            return can;
                        }
                    });
            // resizing dependent on the drawn nodes
            String myW = String.valueOf(can.farX + 600);
            String myH = String.valueOf(can.farY + 200);

            ((Element) can.getDocument().getElementsByTagName("svg").item(0))
                    .setAttribute("width", myW);
            ((Element) can.getDocument().getElementsByTagName("svg").item(0))
                    .setAttribute("height", myH);
            ((Element) can.getDocument().getElementsByTagName("svg").item(0))
                    .setAttribute("viewBox", "-10 -10 " + myW + " " + myH);

            out.write(mxXmlUtils.getXml(can.getDocument()));
            // out.writeBytes(mxXmlUtils.getXml(can.getDocument()));
        } catch (IOException ex) {
            res = ex;
        } finally {
            out.close();
        }

        if (res != null)
            throw res;
    }
}

/* EOF */
