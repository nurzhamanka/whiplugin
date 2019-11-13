package graphs.editor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraphView;
import graphs.editor.EditorActions.HistoryAction;
import graphs.editor.EditorActions.NewAction;
import graphs.editor.EditorActions.OpenAction;
import graphs.editor.EditorActions.SaveAction;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class EditorToolBar extends JToolBar {
    
    
    private static final long serialVersionUID = -8015443128436394471L;
    
    private boolean ignoreZoomChange = false;
    
    public EditorToolBar(final GraphEditor editor, int orientation) {
        super(orientation);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(3, 3, 3, 3), getBorder()));
        setFloatable(false);
        
        add(editor.bind("New", new NewAction()));
        add(editor.bind("Open", new OpenAction()));
        add(editor.bind("Save", new SaveAction(false)));
        
        addSeparator();
        
        add(editor.bind("Cut", TransferHandler.getCutAction()));
        add(editor.bind("Copy", TransferHandler.getCopyAction()));
        add(editor.bind("Paste", TransferHandler.getPasteAction()));
        
        addSeparator();
        
        add(editor.bind("Delete", mxGraphActions.getDeleteAction()));
        
        addSeparator();
        
        add(editor.bind("Undo", new HistoryAction(true)));
        add(editor.bind("Redo", new HistoryAction(false)));
        
        addSeparator();
        
        final mxGraphView view = editor.getGraphComponent().getGraph()
                .getView();
        final JComboBox zoomCombo = new JComboBox<>(new String[]{"400%",
                "200%", "150%", "100%", "75%", "50%", "Page",
                "Width", "Actual size"});
        zoomCombo.setEditable(true);
        zoomCombo.setMinimumSize(new Dimension(75, 0));
        zoomCombo.setPreferredSize(new Dimension(75, 0));
        zoomCombo.setMaximumSize(new Dimension(75, 100));
        zoomCombo.setMaximumRowCount(9);
        add(zoomCombo);
        
        // Sets the zoom in the zoom combo the current value
        mxIEventListener scaleTracker = (sender, evt) -> {
            ignoreZoomChange = true;
            try {
                zoomCombo.setSelectedItem((int) Math.round(100 * view.getScale()) + "%");
            } finally {
                ignoreZoomChange = false;
            }
        };
        
        // Installs the scale tracker to update the value in the combo box
        // if the zoom is changed from outside the combo box
        view.getGraph().getView().addListener(mxEvent.SCALE, scaleTracker);
        view.getGraph().getView().addListener(mxEvent.SCALE_AND_TRANSLATE,
                scaleTracker);
        
        // Invokes once to sync with the actual zoom value
        scaleTracker.invoke(null, null);
        
        zoomCombo.addActionListener(e -> {
            mxGraphComponent graphComponent = editor.getGraphComponent();
            
            // Zoomcombo is changed when the scale is changed in the diagram
            // but the change is ignored here
            if (!ignoreZoomChange) {
                String zoom = Objects.requireNonNull(zoomCombo.getSelectedItem()).toString();
                
                switch (zoom) {
                    case "Page":
                        graphComponent.setPageVisible(true);
                        graphComponent
                                .setZoomPolicy(mxGraphComponent.ZOOM_POLICY_PAGE);
                        break;
                    case "Width":
                        graphComponent.setPageVisible(true);
                        graphComponent
                                .setZoomPolicy(mxGraphComponent.ZOOM_POLICY_WIDTH);
                        break;
                    case "Actual size":
                        graphComponent.zoomActual();
                        break;
                    default:
                        try {
                            zoom = zoom.replace("%", "");
                            double scale = Math.min(16, Math.max(0.01,
                                    Double.parseDouble(zoom) / 100));
                            graphComponent.zoomTo(scale, graphComponent
                                    .isCenterZoom());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(editor, ex
                                    .getMessage());
                        }
                        break;
                }
            }
        });
    }
}
