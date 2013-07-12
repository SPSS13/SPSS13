package teo.isgci.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.jsoup.examples.HtmlToPlainText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import teo.XsltUtil;
import teo.isgci.util.Latex2Html;
import teo.isgci.util.LatexGlyph;

import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxCloudShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxCellState;

public class ISGCISvgCanvas extends mxSvgCanvas {

    private String delHtml = "";
    private int elementlength = 0;
    protected int farX = 0;
    protected int farY = 0;

    public ISGCISvgCanvas(Document document) {
        super(document);
    }

    /**
     * Method that calls dependent on the cellState it is given a method for
     * drawing a node or an edge
     * 
     * @author Fabian Vollmer
     */
    public Object drawCell(mxCellState state) {
        Map<String, Object> style = state.getStyle();
        Element elem = null;

        if (state.getAbsolutePointCount() > 1) {
            // Edge
            List<mxPoint> pts = state.getAbsolutePoints();
            for (mxPoint p : pts) {
                if (p.getX() > farX) {
                    farX = (int) p.getX();
                }
                if (p.getY() > farY) {
                    farY = (int) p.getY();
                }
            }

            // Transpose all points by cloning into a new array
            pts = mxUtils.translatePoints(pts, translate.x, translate.y);

            // Draws the line
            elem = drawLine(pts, style);

            // Applies opacity
            float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY,
                    100);

            if (opacity != 100) {
                String value = String.valueOf(opacity / 100);
                elem.setAttribute("fill-opacity", value);
                elem.setAttribute("stroke-opacity", value);
            }
        } else {
            // Node

            int x = (int) state.getX() + translate.x;
            int y = (int) state.getY() + translate.y;
            int w = (int) state.getWidth();
            int h = (int) state.getHeight();

            Element rect = drawShape((int) state.getX(), y, w, h, style);
            Element text = drawText(
                    ((GraphClassSet) ((mxCell) state.getCell()).getValue())
                            .toString(),
                    (int) state.getX() + 5, y + (h / 2) + 4, w, h, style);

            elem = document.createElement("g");
            elem.appendChild(rect);
            elem.appendChild(text);

        }
        appendSvgElement(elem);
        return elem;
    }

    public Element drawText(String text, int x, int y, int w, int h,
            Map<String, Object> style) {
        Element elem = document.createElement("text");
        elem.setAttribute("x", String.valueOf(x));
        elem.setAttribute("y", String.valueOf(y));
        // elem.setAttribute("textLength", String.valueOf(w));

        String fontColor = mxUtils.getString(style,
                mxConstants.STYLE_FONTCOLOR, "black");
        String fontFamily = mxUtils
                .getString(style, mxConstants.STYLE_FONTFAMILY,
                        mxConstants.DEFAULT_FONTFAMILIES);
        int fontSize = (int) (mxUtils.getInt(style,
                mxConstants.STYLE_FONTSIZE, mxConstants.DEFAULT_FONTSIZE) * scale);

        elem.setAttribute("font-size", String.valueOf(fontSize));
        elem.setAttribute("font-family", fontFamily);
        elem.setAttribute("fill", fontColor);
        elem.setAttribute("text-anchor", "start");
        Element[] segments = createSegments(text, fontSize, y, x);
        for (int i = 0; i < segments.length; i++) {
            elem.appendChild(segments[i]);
        }

        return elem;
    }

    private Element[] createSegments(String text, int fontSize, int y, int x) {
        List<Element> list = new ArrayList<Element>();
        Element[] segments = null;

        String toAdd = "";
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '<') {
                Element t = document.createElement("tspan");
                t.setAttribute("y", String.valueOf(y));
                t.setTextContent(toAdd);
                list.add(t);
                toAdd = "";

                Element seg = executeTag(text.substring(i), fontSize, y, x);
                if (seg != null) {
                    list.add(seg);
                }
                i += elementlength;

            } else {
                toAdd += text.charAt(i);
            }
        }

        segments = new Element[list.size()];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = list.get(i);
        }
        if (list.size() == 0) {
            segments = new Element[1];
            Element tsp = document.createElement("tspan");
            tsp.setTextContent(text);
            segments[0] = tsp;
        }
        return segments;
    }

    private Element executeTag(String s, int fontSize, int y, int x) {
        if (s.startsWith("<sub>")) {
            Element tsp = document.createElement("tspan");
            tsp.setAttribute("font-size", String.valueOf(fontSize / 1.3));
            tsp.setAttribute("baseline-shift", "sub");
            tsp.setTextContent(s.substring(5, s.indexOf("</sub>")));
            elementlength = s.indexOf("</sub>") + 5;
            return tsp;
        } else if (s.startsWith("<sup>")) {
            Element tsp = document.createElement("tspan");
            tsp.setAttribute("font-size", String.valueOf(fontSize / 1.3));
            tsp.setAttribute("baseline-shift", "super");
            tsp.setTextContent(s.substring(5, s.indexOf("</sup>")));
            elementlength = s.indexOf("</sup>") + 5;
            return tsp;

        } else if (s.startsWith("<td style=\"border")) {
            Element g = document.createElement("tspan");
            g.setAttribute("text-decoration", "overline");
            // TODO: Draw a line above this element

            Element[] array = createSegments(
                    s.substring(s.indexOf('>') + 1, s.indexOf("</td>")),
                    fontSize, y, x);
            for (int i = 0; i < array.length; i++) {
                g.appendChild(array[i]);
            }
            elementlength = s.indexOf("</td>") + 4;
            return g;
        } else {
            elementlength = s.indexOf('>');
            return null;
        }
    }

    /**
     * Creates an Element which includes the svg code for showing a rounded
     * rectangle and returns it
     * 
     * @author Fabian Vollmer
     */
    public Element drawShape(int x, int y, int w, int h,
            Map<String, Object> style) {
        if (x > farX) {
            farX = x;
        }
        if (y > farY) {
            farY = y;
        }
        Element elem = document.createElement("rect");
        elem.setAttribute("x", String.valueOf(x));
        elem.setAttribute("y", String.valueOf(y));
        elem.setAttribute("width", String.valueOf(w));
        elem.setAttribute("height", String.valueOf(h));

        // Rectangle is rounded!
        String r = String.valueOf(Math.min(w
                * mxConstants.RECTANGLE_ROUNDING_FACTOR, h
                * mxConstants.RECTANGLE_ROUNDING_FACTOR));

        elem.setAttribute("rx", r);
        elem.setAttribute("ry", r);
        Color back = mxHtmlColor.parseColor(mxUtils.getString(style,
                mxConstants.STYLE_FILLCOLOR));
        int red = back.getRed();
        int green = back.getGreen();
        int blue = back.getBlue();
        String fillcolor = "fill:rgb(" + red + "," + green + "," + blue + ")";
        elem.setAttribute("style", fillcolor);
        elem.setAttribute("stroke", "black");
        elem.setAttribute("stroke-width", String.valueOf(2));

        return elem;
    }

    /**
     * Calculates offset and calls method for creating the svg code for a
     * textblock
     * 
     * @author Fabian Vollmer
     */
    public Element drawLabel(String label, mxCellState state, boolean html) {
        mxRectangle bounds = state.getLabelBounds();

        if (drawLabels && bounds != null) {
            int x = (int) bounds.getX() + translate.x;
            int y = (int) bounds.getY() + translate.y;
            int w = (int) bounds.getWidth();
            int h = (int) bounds.getHeight();
            Map<String, Object> style = state.getStyle();

            // System.out.println("drawLabel call");
            // return drawText(label, x, y, w, h, style);
        }

        return null;

    }

    public Element drawLine(List<mxPoint> pts, Map<String, Object> style) {
        Element group = document.createElement("g");
        Element path = document.createElement("path");

        boolean rounded = true;
        String strokeColor = mxUtils.getString(style,
                mxConstants.STYLE_STROKECOLOR);
        float tmpStroke = (mxUtils.getFloat(style,
                mxConstants.STYLE_STROKEWIDTH, 1));
        float strokeWidth = (float) (tmpStroke * scale);

        if (strokeColor != null && strokeWidth > 0) {
            // Draws the start marker
            Object marker = style.get(mxConstants.STYLE_STARTARROW);

            mxPoint pt = pts.get(1);
            mxPoint p0 = pts.get(0);
            mxPoint offset = null;

            if (marker != null) {
                float size = (mxUtils.getFloat(style,
                        mxConstants.STYLE_STARTSIZE,
                        mxConstants.DEFAULT_MARKERSIZE));
                offset = drawMarker(group, marker, new mxPoint(pt.getX()
                        - translate.x, pt.getY() - translate.y), new mxPoint(
                        p0.getX() - translate.x, p0.getY() - translate.y),
                        size, tmpStroke, strokeColor);
            } else {
                double dx = pt.getX() - p0.getX();
                double dy = pt.getY() - p0.getY();

                double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
                double nx = dx * strokeWidth / dist;
                double ny = dy * strokeWidth / dist;

                offset = new mxPoint(nx / 2, ny / 2);
            }

            // Draws the end marker
            marker = style.get(mxConstants.STYLE_ENDARROW);

            pt = pts.get(pts.size() - 2);
            mxPoint pe = pts.get(pts.size() - 1);

            if (marker != null) {
                float size = (mxUtils.getFloat(style,
                        mxConstants.STYLE_ENDSIZE,
                        mxConstants.DEFAULT_MARKERSIZE));
                offset = drawMarker(group, marker, new mxPoint(pt.getX()
                        - translate.x, pt.getY() - translate.y), new mxPoint(
                        pe.getX() - translate.x, pe.getY() - translate.y),
                        size, tmpStroke, strokeColor);
            } else {
                double dx = pt.getX() - p0.getX();
                double dy = pt.getY() - p0.getY();

                double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
                double nx = dx * strokeWidth / dist;
                double ny = dy * strokeWidth / dist;

                offset = new mxPoint(nx / 2, ny / 2);
            }

            // Draws the line segments
            double arcSize = mxConstants.LINE_ARCSIZE * scale;
            pt = p0;
            String d = "M " + (pt.getX() - translate.x) + " "
                    + (pt.getY() - translate.y);

            for (int i = 1; i < pts.size() - 1; i++) {
                mxPoint tmp = pts.get(i);
                double dx = pt.getX() - tmp.getX();
                double dy = pt.getY() - tmp.getY();

                if ((rounded && i < pts.size() - 1) && (dx != 0 || dy != 0)) {
                    // Draws a line from the last point to the current
                    // point with a spacing of size off the current point
                    // into direction of the last point
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    double nx1 = dx * Math.min(arcSize, dist / 2) / dist;
                    double ny1 = dy * Math.min(arcSize, dist / 2) / dist;

                    double x1 = tmp.getX() + nx1;
                    double y1 = tmp.getY() + ny1;
                    d += " L " + (x1 - translate.x) + " " + (y1 - translate.y);

                    // Draws a curve from the last point to the current
                    // point with a spacing of size off the current point
                    // into direction of the next point
                    mxPoint next = pts.get(i + 1);
                    dx = next.getX() - tmp.getX();
                    dy = next.getY() - tmp.getY();

                    dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
                    double nx2 = dx * Math.min(arcSize, dist / 2) / dist;
                    double ny2 = dy * Math.min(arcSize, dist / 2) / dist;

                    double x2 = tmp.getX() + nx2;
                    double y2 = tmp.getY() + ny2;

                    d += " Q " + (tmp.getX() - translate.x) + " "
                            + (tmp.getY() - translate.y) + " "
                            + (x2 - translate.x) + " " + (y2 - translate.y);
                    tmp = new mxPoint(x2, y2);
                } else {
                    d += " L " + (tmp.getX() - translate.x) + " "
                            + (tmp.getY() - translate.y);
                }

                pt = tmp;
            }

            d += " L " + (pe.getX() - translate.x) + " "
                    + (pe.getY() - translate.y);

            path.setAttribute("d", d);
            path.setAttribute("stroke", strokeColor);
            path.setAttribute("fill", "none");
            path.setAttribute("stroke-width", String.valueOf(strokeWidth));

            if (mxUtils.isTrue(style, mxConstants.STYLE_DASHED)) {
                String pattern = mxUtils.getString(style,
                        mxConstants.STYLE_DASH_PATTERN, "3, 3");
                path.setAttribute("stroke-dasharray", pattern);
            }

            group.appendChild(path);
            appendSvgElement(group);
        }
        return group;
    }

}
