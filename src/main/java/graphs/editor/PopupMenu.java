package graphs.editor;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;

import javax.swing.*;

public class PopupMenu extends JPopupMenu {
    private static final long serialVersionUID = -3132749140550242191L;
    
    public PopupMenu(GraphEditor editor) {
        boolean selected = !editor.getGraphComponent().getGraph()
                .isSelectionEmpty();
    
        boolean nodeSelected = editor.getGraphComponent().getGraph().getSelectionCount() == 1 && ((mxCell) editor.getGraphComponent().getGraph().getSelectionCell()).isVertex();
        
        add(editor.bind("Cut", TransferHandler.getCutAction())).setEnabled(selected);
        
        add(editor.bind("Copy", TransferHandler.getCopyAction())).setEnabled(selected);
        
        add(editor.bind("Paste", TransferHandler.getPasteAction()));
        
        add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
    
        add(editor.bind("Properties", new Actions.PropertiesAction())).setEnabled(nodeSelected);
    }
    
}
