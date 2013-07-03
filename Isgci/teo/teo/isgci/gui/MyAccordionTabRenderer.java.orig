package teo.isgci.gui;

//author: Nicolas Siebeck
//date: 07.06.13

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.javaswingcomponents.accordion.JSCAccordion;
import com.javaswingcomponents.accordion.tabrenderer.AccordionTabRenderer;
import com.javaswingcomponents.accordion.tabrenderer.GetTabComponentParameter;
import com.javaswingcomponents.framework.painters.configurationbound.GradientColorPainter;
import com.javaswingcomponents.framework.painters.configurationbound.GradientDirection;

@SuppressWarnings("serial")
class MyAccordionTabRenderer extends JLabel implements AccordionTabRenderer {

	public JComponent getTabComponent(GetTabComponentParameter parameters) {
		// read the tabText from the parameter
		setText(parameters.tabText);
		// set the text color to black
		setBackground(Color.LIGHT_GRAY);
		// returns itself, which extends JLabel
		return this;
	}

	public GradientColorPainter painter = new GradientColorPainter();

	protected void paintComponent(Graphics g) {
		// paints the background

		painter.setGradientDirection(GradientDirection.VERTICAL);
		painter.setStartColor(Color.white);
		painter.setEndColor(Color.lightGray);
		// original color on g is stored and then later reset
		// this is to prevent clobbering of the Graphics object.
		Color originalColor = g.getColor();
		painter.paint((Graphics2D) g, new Rectangle(0, 0, getWidth(), getHeight()));
		// paints a simple line on the bottom of the tab
		g.setColor(new Color(87, 86, 111));
		g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

		g.setColor(originalColor);
		super.paintComponent(g);
	}

	public void setAccordion(JSCAccordion accordion) {
	}
}