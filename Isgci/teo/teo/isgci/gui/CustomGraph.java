package teo.isgci.gui;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class CustomGraph extends mxGraph {
    
    /**
     * set edges not selectable
     */
    @Override
    public boolean isCellSelectable(Object cell) {
       if (cell != null) {
          if (cell instanceof mxCell) {
             mxCell myCell = (mxCell) cell;
             if (myCell.isEdge())
                return false;
          }
       }
       return super.isCellSelectable(cell);
    }
    
    public CustomGraph() {
        super();
        setCellsEditable(false);
        setCellsDisconnectable(false);
        setCellsDeletable(false);
        setCellsCloneable(false);
        setAutoSizeCells(true);
        setBorder(10);
        setEdgeLabelsMovable(false);
        setVertexLabelsMovable(false);
        setSplitEnabled(false);
        setResetEdgesOnMove(true);
        setHtmlLabels(true);
        setAllowDanglingEdges(false);
        setConnectableEdges(false);
        setDisconnectOnMove(false);
        setCellsBendable(false);
        setMultigraph(false);
        setAllowLoops(false);
    }
}
