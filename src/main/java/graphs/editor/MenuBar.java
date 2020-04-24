package graphs.editor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import graphs.editor.Actions.*;

import javax.swing.*;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = 4060203894740766714L;
    
    public MenuBar(final GraphEditor editor) {
        final mxGraphComponent graphComponent = editor.getGraphComponent();
        final mxGraph graph = graphComponent.getGraph();
        
        JMenu menu;
        JMenu submenu;
        
        // Creates the file menu
        menu = add(new JMenu("File"));
        
        menu.add(editor.bind("New", new NewAction()));
        menu.add(editor.bind("Open", new OpenAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Save", new SaveAction(false)));
        menu.add(editor.bind("Save as...", new SaveAction(true)));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Terminate", new ExitAction()));
        
        // Creates the edit menu
        menu = add(new JMenu("Edit"));
        
        menu.add(editor.bind("Undo", new HistoryAction(true)));
        menu.add(editor.bind("Redo", new HistoryAction(false)));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Cut", TransferHandler.getCutAction()));
        menu.add(editor.bind("Copy", TransferHandler.getCopyAction()));
        menu.add(editor.bind("Paste", TransferHandler.getPasteAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Delete", mxGraphActions.getDeleteAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Select all", mxGraphActions.getSelectAllAction()));
        menu.add(editor.bind("Deselect", mxGraphActions.getSelectNoneAction()));
        
        // Creates the view menu
        menu = add(new JMenu("View"));
        
        JMenuItem item = menu.add(new TogglePropertyItem(graphComponent, "Page Visible", "PageVisible", true, e -> {
            if (graphComponent.isPageVisible() && graphComponent.isCenterPage()) {
                graphComponent.zoomAndCenter();
            } else {
                graphComponent.getGraphControl().updatePreferredSize();
            }
        }));
        
        
        item.addActionListener(e -> {
            if (e.getSource() instanceof TogglePropertyItem) {
                final mxGraphComponent graphComponent1 = editor.getGraphComponent();
                TogglePropertyItem toggleItem = (TogglePropertyItem) e.getSource();
                
                if (toggleItem.isSelected()) {
                    // Scrolls the view to the center
                    SwingUtilities.invokeLater(() -> {
                        graphComponent1.scrollToCenter(true);
                        graphComponent1.scrollToCenter(false);
                    });
                } else {
                    // Resets the translation of the view
                    mxPoint tr = graphComponent1.getGraph().getView().getTranslate();
                    
                    if (tr.getX() != 0 || tr.getY() != 0) {
                        graphComponent1.getGraph().getView().setTranslate(new mxPoint());
                    }
                }
            }
        });
        
        menu.addSeparator();
        
        menu.add(new ToggleGridItem(editor, "Grid"));
        
        menu.addSeparator();
        
        submenu = (JMenu) menu.add(new JMenu("Zoom"));
        
        submenu.add(editor.bind("400%", new ScaleAction(4)));
        submenu.add(editor.bind("200%", new ScaleAction(2)));
        submenu.add(editor.bind("150%", new ScaleAction(1.5)));
        submenu.add(editor.bind("100%", new ScaleAction(1)));
        submenu.add(editor.bind("75%", new ScaleAction(0.75)));
        submenu.add(editor.bind("50%", new ScaleAction(0.5)));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Custom Scale...", new ScaleAction(0)));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Zoom in", mxGraphActions.getZoomInAction()));
        menu.add(editor.bind("Zoom out", mxGraphActions.getZoomOutAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Fit page", new ZoomPolicyAction(mxGraphComponent.ZOOM_POLICY_PAGE)));
        menu.add(editor.bind("Fit width", new ZoomPolicyAction(mxGraphComponent.ZOOM_POLICY_WIDTH)));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Actual size", mxGraphActions.getZoomActualAction()));
        
        // Creates the diagram menu
        menu = add(new JMenu("Layout"));
    
        menu.add(editor.graphLayout("Beautify (Vertical)", true));
        menu.add(editor.graphLayout("Beautify (Horizontal)", true));
    }
}