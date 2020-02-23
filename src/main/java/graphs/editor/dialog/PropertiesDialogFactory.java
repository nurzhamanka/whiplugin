package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.OpType;
import graphs.model.Operation;

public class PropertiesDialogFactory {
    
    // TODO: maybe leave this factory and get rid of inheritance, if it makes sense?
    
    public static PropertiesDialog getDialog(mxCell cell, mxGraph graph) {
        Operation operation = (Operation) cell.getValue();
        OpType type = operation.getType();
        
        switch (type) {
            case DOG:
                return new DoGaussDialog(cell, graph);
            case TOPHAT:
                return new TopHatDialog(cell, graph);
            default:
                return null;
        }
    }
}
