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
}
