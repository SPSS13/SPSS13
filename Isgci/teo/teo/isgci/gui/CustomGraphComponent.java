package teo.isgci.gui;

import java.awt.event.MouseEvent;

import teo.XsltUtil;
import teo.isgci.gc.GraphClass;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class CustomGraphComponent extends mxGraphComponent {
    /**
     * 
     */
    private static final long serialVersionUID = -7478582081513091476L;

    public CustomGraphComponent(mxGraph graph) {
        super(graph);
        graphControl = new CustomGraphControl();
        // register everything that would go to super to the CustomGraphControl
        installFocusHandler();
        installKeyHandler();
        installResizeHandler();
        setGraph(graph);

        // Adds the viewport view and initializes handlers
        setViewportView(graphControl);
        createHandlers();
        installDoubleClickHandler();
    }

    public class CustomGraphControl extends mxGraphControl {
        /**
         * 
         */
        private static final long serialVersionUID = 3745586558243796428L;

        public CustomGraphControl() {
            super();
        }

        /**
         * custom method to show the tooltip of a node
         * 
         * shows all possible names of the node, the actual name first, then the
         * others, unsorted
         */
        public String getToolTipText(MouseEvent e) {
            String tip = null;

            Object cell = getCellAt(e.getX(), e.getY());
            // get the GraphclassSet
            if (cell != null && ((mxCell)cell).isVertex()) {
                GraphClassSet gcs = ((GraphClassSet)((mxCell)cell).getValue());
                // get the first entry
                tip = gcs.toLongString();
                // get the following
                for (GraphClass gc : gcs.getSet()) {
                    if (gc != gcs.getLabel()) {
                        tip += ("<br>" + XsltUtil.latex(gc.toString()));
                    }
                }
                // add html-tag
                tip = "<html><body style=\"max-height:4;\">" + tip
                        + "</body></html>";
            }
            // return a proper tooltip
            if (tip != null && tip.length() > 0) {
                return tip;
            }
            // or something else
            return super.getToolTipText(e);
        }
    }

}
