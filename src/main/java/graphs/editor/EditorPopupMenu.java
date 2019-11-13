package graphs.editor;

import com.mxgraph.swing.util.mxGraphActions;
import graphs.editor.EditorActions.HistoryAction;

import javax.swing.*;

public class EditorPopupMenu extends JPopupMenu {
    
    /**
     *
     */
    private static final long serialVersionUID = -3132749140550242191L;
    
    public EditorPopupMenu(GraphEditor editor) {
        boolean selected = !editor.getGraphComponent().getGraph()
                .isSelectionEmpty();
        
        add(editor.bind("Undo", new HistoryAction(true)));
        
        addSeparator();
        
        add(editor.bind("Cut", TransferHandler.getCutAction())).setEnabled(selected);
        
        add(editor.bind("Copy", TransferHandler.getCopyAction())).setEnabled(selected);
        
        add(editor.bind("Paste", TransferHandler.getPasteAction()));
        
        addSeparator();
        
        add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
        
        addSeparator();
        
        // Creates the format menu
        JMenu menu = (JMenu) add(new JMenu("Format"));
        
        EditorMenuBar.populateFormatMenu(menu, editor);
        
        // Creates the shape menu
        menu = (JMenu) add(new JMenu("Shape"));
        
        EditorMenuBar.populateShapeMenu(menu, editor);
        
        addSeparator();
        
        add(editor.bind("Edit", mxGraphActions.getEditAction())).setEnabled(selected);
        
        addSeparator();
        
        add(editor.bind("Select vertices", mxGraphActions.getSelectVerticesAction()));
        
        add(editor.bind("Select edges", mxGraphActions.getSelectEdgesAction()));
        
        addSeparator();
        
        add(editor.bind("Select all", mxGraphActions.getSelectAllAction()));
    }
    
}
