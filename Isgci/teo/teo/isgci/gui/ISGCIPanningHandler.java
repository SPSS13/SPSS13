package teo.isgci.gui;

import java.awt.event.MouseEvent;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxPanningHandler;

public class ISGCIPanningHandler extends mxPanningHandler {

	public ISGCIPanningHandler(mxGraphComponent component) {
		super(component);
	}
	public void mousePressed(MouseEvent e)
	{
		if (isEnabled() && !e.isConsumed() && !e.isPopupTrigger())
		{
			start = e.getPoint();
		}
	}

}
