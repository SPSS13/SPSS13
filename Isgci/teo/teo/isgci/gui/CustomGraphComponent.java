package teo.isgci.gui;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class CustomGraphComponent extends mxGraphComponent {
    public CustomGraphComponent(mxGraph graph) {
        super(graph);
        graphControl = new CustomGraphControal();
        installFocusHandler();
        installKeyHandler();
        installResizeHandler();
        setGraph(graph);

        // Adds the viewport view and initializes handlers
        setViewportView(graphControl);
        createHandlers();
        installDoubleClickHandler();
    }

    public class CustomGraphControal extends mxGraphControl {
        public CustomGraphControal() {
            super();
        }
    }
}
