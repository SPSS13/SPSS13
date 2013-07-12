package teo.isgci.gui;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxPoint;
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
        this.setCellsEditable(false);
        this.setCellsDisconnectable(false);
        this.setCellsDeletable(false);
        this.setCellsCloneable(false);
        this.setAutoSizeCells(true);
        this.setBorder(10);
        this.setEdgeLabelsMovable(false);
        this.setVertexLabelsMovable(false);
        this.setSplitEnabled(false);
        this.setResetEdgesOnMove(true);
        this.setHtmlLabels(true);
        this.setAllowDanglingEdges(false);
        this.setConnectableEdges(false);
        this.setDisconnectOnMove(false);
        this.setCellsBendable(false);
        this.setMultigraph(false);
        this.setAllowLoops(false);
    }
}
