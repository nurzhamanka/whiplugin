package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.OpType;
import graphs.model.Operation;

public class PropertiesDialogFactory {
    
    public static PropertiesDialog getDialog(mxCell cell, mxGraph graph) {
        Operation operation = (Operation) cell.getValue();
        OpType type = operation.getType();
        
        switch (type) {
            case DOG:
                return new DoGaussDialog(cell, graph);
            case TOPHAT:
                return new TopHatDialog(cell, graph);
            case HYBRID:
                return new HybridDialog(cell, graph);
            default:
                return null;
        }
    }
}
