package graphs.editor;

import com.mxgraph.swing.util.mxGraphActions;

import javax.swing.*;

public class PopupMenu extends JPopupMenu {
    private static final long serialVersionUID = -3132749140550242191L;
    
    public PopupMenu(GraphEditor editor) {
        boolean selected = !editor.getGraphComponent().getGraph()
                .isSelectionEmpty();
        
        add(editor.bind("Cut", TransferHandler.getCutAction())).setEnabled(selected);
        
        add(editor.bind("Copy", TransferHandler.getCopyAction())).setEnabled(selected);
        
        add(editor.bind("Paste", TransferHandler.getPasteAction()));
        
        add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
    }
    
}
