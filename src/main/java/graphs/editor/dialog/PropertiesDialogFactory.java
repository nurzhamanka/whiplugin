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
            case INPUT:
                return new InputDialog(cell, graph);
            case OUTPUT:
                return new OutputDialog(cell, graph);
            case DOG:
                return new DoGaussDialog(cell, graph);
            case TOPHAT:
                return new TopHatDialog(cell, graph);
            case HYBRID:
                return new HybridDialog(cell, graph);
            case MATH:
                return new MathDialog(cell, graph);
            case MEAN:
                return new SmoothDialog(cell, graph);
            case BINARIZE:
                return new BinarizeDialog(cell, graph);
            case JOIN:
                return new JoinDialog(cell, graph);
            default:
                return null;
        }
    }
}
