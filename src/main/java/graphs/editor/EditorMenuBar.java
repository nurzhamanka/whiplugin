package graphs.editor;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import graphs.editor.EditorActions.*;

import javax.swing.*;

public class EditorMenuBar extends JMenuBar {
    
    /**
     *
     */
    private static final long serialVersionUID = 4060203894740766714L;
    
    public EditorMenuBar(final GraphEditor editor) {
        final mxGraphComponent graphComponent = editor.getGraphComponent();
        final mxGraph graph = graphComponent.getGraph();
        mxAnalysisGraph aGraph = new mxAnalysisGraph();
        
        JMenu menu = null;
        JMenu submenu = null;
        
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
        menu.add(new ToggleRulersItem(editor, "Rulers"));
        
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
        
        // Creates the format menu
        menu = add(new JMenu("Format"));
        
        populateFormatMenu(menu, editor);
        
        // Creates the shape menu
        menu = add(new JMenu("Shape"));
        
        populateShapeMenu(menu, editor);
        
        // Creates the diagram menu
        menu = add(new JMenu("Diagram"));
        
        menu.add(new ToggleOutlineItem(editor, "Outline"));
        
        menu.addSeparator();
        
        submenu = (JMenu) menu.add(new JMenu("Background"));
        
        submenu.add(editor.bind("BG color", new BackgroundAction()));
        submenu.add(editor.bind("BG image", new BackgroundImageAction()));
        
        submenu = (JMenu) menu.add(new JMenu("Layout"));
        
        submenu.add(editor.graphLayout("verticalHierarchical", true));
        submenu.add(editor.graphLayout("horizontalHierarchical", true));
        
        submenu.addSeparator();
        
        submenu.add(editor.graphLayout("verticalPartition", false));
        submenu.add(editor.graphLayout("horizontalPartition", false));
        
        submenu.addSeparator();
        
        submenu.add(editor.graphLayout("verticalStack", false));
        submenu.add(editor.graphLayout("horizontalStack", false));
        
        submenu.addSeparator();
        
        submenu.add(editor.graphLayout("verticalTree", true));
        submenu.add(editor.graphLayout("horizontalTree", true));
        
        submenu.addSeparator();
        
        submenu.add(editor.graphLayout("placeEdgeLabels", false));
        submenu.add(editor.graphLayout("parallelEdges", false));
        
        submenu.addSeparator();
        
        submenu.add(editor.graphLayout("organicLayout", true));
        submenu.add(editor.graphLayout("circleLayout", true));
        
        submenu = (JMenu) menu.add(new JMenu("Style"));
        
        submenu.add(editor.bind("Basic", new StylesheetAction("/graphs/style/basic-style.xml")));
        submenu.add(editor.bind("Default", new StylesheetAction("/graphs/style/default-style.xml")));
        
        // Creates the options menu
        menu = add(new JMenu("Options"));
        
        submenu = (JMenu) menu.add(new JMenu("Display"));
        submenu.add(new TogglePropertyItem(graphComponent, "Buffering", "TripleBuffered", true));
        
        submenu.add(new TogglePropertyItem(graphComponent, "Prefer page size", "PreferPageSize", true, e -> graphComponent.zoomAndCenter()));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Tolerance", new PromptPropertyAction(graphComponent, "Tolerance")));
        
        submenu.add(editor.bind("Dirty", new ToggleDirtyAction()));
        
        submenu = (JMenu) menu.add(new JMenu("Zoom"));
        
        submenu.add(new TogglePropertyItem(graphComponent, "Center Zoom", "CenterZoom", true));
        submenu.add(new TogglePropertyItem(graphComponent, "Zoom to Selection", "KeepSelectionVisibleOnZoom", true));
        
        submenu.addSeparator();
        
        submenu.add(new TogglePropertyItem(graphComponent, "Center page", "CenterPage", true, e -> {
            if (graphComponent.isPageVisible() && graphComponent.isCenterPage()) {
                graphComponent.zoomAndCenter();
            }
        }));
        
        menu.addSeparator();
        
        submenu = (JMenu) menu.add(new JMenu("Drag & Drop"));
        
        submenu.add(new TogglePropertyItem(graphComponent, "Drag enabled", "DragEnabled"));
        submenu.add(new TogglePropertyItem(graph, "Drop enabled", "DropEnabled"));
        
        submenu.addSeparator();
        
        submenu.add(new TogglePropertyItem(graphComponent.getGraphHandler(), "Image preview", "ImagePreview"));
        
        submenu = (JMenu) menu.add(new JMenu("Labels"));
        
        submenu.add(new TogglePropertyItem(graph, "HTML labels", "HtmlLabels", true));
        submenu.add(new TogglePropertyItem(graph, "Show labels", "LabelsVisible", true));
        
        submenu.addSeparator();
        
        submenu.add(new TogglePropertyItem(graph, "Move edge labels", "EdgeLabelsMovable"));
        submenu.add(new TogglePropertyItem(graph, "Move vertex labels", "VertexLabelsMovable"));
        
        submenu.addSeparator();
        
        submenu.add(new TogglePropertyItem(graphComponent, "Handle Return?", "EnterStopsCellEditing"));
        
        menu.addSeparator();
        
        submenu = (JMenu) menu.add(new JMenu("Connections"));
        
        submenu.add(new TogglePropertyItem(graphComponent, "Connectable", "Connectable"));
        submenu.add(new TogglePropertyItem(graph, "Connectable edges", "ConnectableEdges"));
        
        submenu.addSeparator();
        
        submenu.add(new ToggleCreateTargetItem(editor, "Create target"));
        submenu.add(new TogglePropertyItem(graph, "Disconnect on move", "DisconnectOnMove"));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Connect mode", new ToggleConnectModeAction()));
    }
    
    /**
     * Adds menu items to the given shape menu. This is factored out because
     * the shape menu appears in the menubar and also in the popupmenu.
     */
    public static void populateShapeMenu(JMenu menu, GraphEditor editor) {
        
        menu.add(editor.bind("Home", mxGraphActions.getHomeAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Exit Group", mxGraphActions.getExitGroupAction()));
        menu.add(editor.bind("Enter Group", mxGraphActions.getEnterGroupAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Group", mxGraphActions.getGroupAction()));
        menu.add(editor.bind("Ungroup", mxGraphActions.getUngroupAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Collapse", mxGraphActions.getCollapseAction()));
        menu.add(editor.bind("Expand", mxGraphActions.getExpandAction()));
        
        menu.addSeparator();
        
        menu.add(editor.bind("To Back", mxGraphActions.getToBackAction()));
        menu.add(editor.bind("To Front", mxGraphActions.getToFrontAction()));
        
        menu.addSeparator();
        
        JMenu submenu = (JMenu) menu.add(new JMenu("Align"));
        
        submenu.add(editor.bind("Left", new AlignCellsAction(mxConstants.ALIGN_LEFT)));
        submenu.add(editor.bind("Center", new AlignCellsAction(mxConstants.ALIGN_CENTER)));
        submenu.add(editor.bind("Right", new AlignCellsAction(mxConstants.ALIGN_RIGHT)));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Top", new AlignCellsAction(mxConstants.ALIGN_TOP)));
        submenu.add(editor.bind("Middle", new AlignCellsAction(mxConstants.ALIGN_MIDDLE)));
        submenu.add(editor.bind("Bottom", new AlignCellsAction(mxConstants.ALIGN_BOTTOM)));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Autosize", new AutosizeAction()));
        
    }
    
    /**
     * Adds menu items to the given format menu. This is factored out because
     * the format menu appears in the menubar and also in the popupmenu.
     */
    public static void populateFormatMenu(JMenu menu, GraphEditor editor) {
        JMenu submenu = (JMenu) menu.add(new JMenu("Background"));
        
        submenu.add(editor.bind("Color", new ColorAction("Fillcolor", mxConstants.STYLE_FILLCOLOR)));
        
        menu.addSeparator();
        
        submenu = (JMenu) menu.add(new JMenu("Spacing"));
        
        submenu.add(editor.bind("Top", new PromptValueAction(mxConstants.STYLE_SPACING_TOP, "Top Spacing")));
        submenu.add(editor.bind("Right", new PromptValueAction(mxConstants.STYLE_SPACING_RIGHT, "Right Spacing")));
        submenu.add(editor.bind("Bottom", new PromptValueAction(mxConstants.STYLE_SPACING_BOTTOM, "Bottom Spacing")));
        submenu.add(editor.bind("Left", new PromptValueAction(mxConstants.STYLE_SPACING_LEFT, "Left Spacing")));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Global", new PromptValueAction(mxConstants.STYLE_SPACING, "Spacing")));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Source Spacing", new PromptValueAction(mxConstants.STYLE_SOURCE_PERIMETER_SPACING,
                "Source Spacing")));
        submenu.add(editor.bind("Target Spacing", new PromptValueAction(mxConstants.STYLE_TARGET_PERIMETER_SPACING,
                "Target Spacing")));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Perimeter", new PromptValueAction(mxConstants.STYLE_PERIMETER_SPACING,
                "Perimeter Spacing")));
        
        submenu = (JMenu) menu.add(new JMenu("Direction"));
        
        submenu.add(editor.bind("North", new KeyValueAction(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH)));
        submenu.add(editor.bind("East", new KeyValueAction(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST)));
        submenu.add(editor.bind("South", new KeyValueAction(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH)));
        submenu.add(editor.bind("West", new KeyValueAction(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST)));
        
        submenu.addSeparator();
        
        submenu.add(editor.bind("Rotation", new PromptValueAction(mxConstants.STYLE_ROTATION, "Rotation (0-360)")));
        
        menu.addSeparator();
        
        menu.add(editor.bind("Rounded", new ToggleAction(mxConstants.STYLE_ROUNDED)));
        
        menu.add(editor.bind("Style", new StyleAction()));
    }
}