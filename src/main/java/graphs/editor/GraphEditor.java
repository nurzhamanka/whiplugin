package graphs.editor;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.*;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

@SuppressWarnings("ALL")
public class GraphEditor extends JPanel {
    private static final long serialVersionUID = -6561623072112577140L;
    
    protected mxGraphComponent graphComponent;
    protected JTabbedPane libraryPane;
    protected mxUndoManager undoManager;
    protected String appTitle;
    protected JLabel statusBar;
    protected File currentFile;
    protected boolean modified = false;
    protected mxRubberband rubberband;
    protected mxKeyboardHandler keyboardHandler;
    protected mxIEventListener undoHandler = (source, evt) -> undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
    protected mxIEventListener changeTracker = (source, evt) -> setModified(true);
    
    public GraphEditor(String appTitle, mxGraphComponent component) {
        // Stores and updates the frame title
        this.appTitle = appTitle;
        
        // Stores a reference to the graph and creates the command history
        graphComponent = component;
        final mxGraph graph = graphComponent.getGraph();
        
        undoManager = createUndoManager();
        
        // Do not change the scale and translation after files have been loaded
        graph.setResetViewOnRootChange(false);
        
        // Updates the modified flag if the graph model changes
        graph.getModel().addListener(mxEvent.CHANGE, changeTracker);
        
        // Adds the command history to the model and view
        graph.getModel().addListener(mxEvent.UNDO, undoHandler);
        graph.getView().addListener(mxEvent.UNDO, undoHandler);
        
        // Keeps the selection in sync with the command history
        mxIEventListener undoHandler = (source, evt) -> {
            List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
            graph.setSelectionCells(graph.getSelectionCellsForChanges(changes));
        };
        
        undoManager.addListener(mxEvent.UNDO, undoHandler);
        undoManager.addListener(mxEvent.REDO, undoHandler);
        
        // Creates the library pane that contains the tabs with the palettes
        libraryPane = new JTabbedPane();
        
        // Creates the outer split pane that contains the inner split pane and
        // the graph component on the right side of the window
        JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, libraryPane,
                graphComponent);
        outer.setOneTouchExpandable(true);
        outer.setDividerLocation(200);
        outer.setDividerSize(6);
        outer.setBorder(null);
        
        // Creates the status bar
        statusBar = createStatusBar();
        
        // Display some useful information about repaint events
        installRepaintListener();
        
        // Puts everything together
        setLayout(new BorderLayout());
        add(outer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        installToolBar();
        
        // Installs rubberband selection and handling for some special
        // keystrokes such as F2, Control-C, -V, X, A etc.
        installHandlers();
        installListeners();
        updateTitle();
    }
    
    protected mxUndoManager createUndoManager() {
        return new mxUndoManager();
    }
    
    protected void installHandlers() {
        rubberband = new mxRubberband(graphComponent);
        keyboardHandler = new KeyboardHandler(graphComponent);
    }
    
    protected void installToolBar() {
        add(new ToolBar(this, JToolBar.HORIZONTAL), BorderLayout.NORTH);
    }
    
    protected JLabel createStatusBar() {
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        
        return statusBar;
    }
    
    protected void installRepaintListener() {
        graphComponent.getGraph().addListener(mxEvent.REPAINT,
                (source, evt) -> {
                    String buffer = (graphComponent.getTripleBuffer() != null) ? ""
                            : " (unbuffered)";
                    mxRectangle dirty = (mxRectangle) evt
                            .getProperty("region");
                    
                    if (dirty == null) {
                        status("Repaint all" + buffer);
                    } else {
                        status("Repaint: x=" + (int) (dirty.getX()) + " y="
                                + (int) (dirty.getY()) + " w="
                                + (int) (dirty.getWidth()) + " h="
                                + (int) (dirty.getHeight()) + buffer);
                    }
                });
    }
    
    public Palette insertPalette(String title) {
        final Palette palette = new Palette();
        final JScrollPane scrollPane = new JScrollPane(palette);
        scrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        libraryPane.add(title, scrollPane);
        
        // Updates the widths of the palettes if the container size changes
        libraryPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = scrollPane.getWidth()
                        - scrollPane.getVerticalScrollBar().getWidth();
                palette.setPreferredWidth(w);
            }
            
        });
        
        return palette;
    }
    
    protected void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            graphComponent.zoomIn();
        } else {
            graphComponent.zoomOut();
        }
        
        status("Scale: "
                + (int) (100 * graphComponent.getGraph().getView().getScale())
                + "%");
    }
    
    protected void showGraphPopupMenu(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
                graphComponent);
        PopupMenu menu = new PopupMenu(GraphEditor.this);
        menu.show(graphComponent, pt.x, pt.y);
        
        e.consume();
    }
    
    protected void mouseLocationChanged(MouseEvent e) {
        status(e.getX() + ", " + e.getY());
    }
    
    protected void installListeners() {
        // Installs mouse wheel listener for zooming
        MouseWheelListener wheelTracker = e -> {
            if (e.getSource() instanceof mxGraphOutline || e.isControlDown()) {
                GraphEditor.this.mouseWheelMoved(e);
            }
        };
        
        graphComponent.addMouseWheelListener(wheelTracker);
        
        // Installs the popup menu in the graph component
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // Handles context menu on the Mac where the trigger is on mousepressed
                mouseReleased(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showGraphPopupMenu(e);
                }
            }
            
        });
        
        // Installs a mouse motion listener to display the mouse location
        graphComponent.getGraphControl().addMouseMotionListener(
                new MouseMotionListener() {
                    public void mouseDragged(MouseEvent e) {
                        mouseLocationChanged(e);
                    }
                    
                    public void mouseMoved(MouseEvent e) {
                        mouseDragged(e);
                    }
                    
                });
    }
    
    public File getCurrentFile() {
        return currentFile;
    }
    
    public void setCurrentFile(File file) {
        File oldValue = currentFile;
        currentFile = file;
        
        firePropertyChange("currentFile", oldValue, file);
        
        if (oldValue != file) {
            updateTitle();
        }
    }
    
    public boolean isNotModified() {
        return !modified;
    }
    
    public void setModified(boolean modified) {
        boolean oldValue = this.modified;
        this.modified = modified;
        
        firePropertyChange("modified", oldValue, modified);
        
        if (oldValue != modified) {
            updateTitle();
        }
    }
    
    public mxGraphComponent getGraphComponent() {
        return graphComponent;
    }
    
    public JTabbedPane getLibraryPane() {
        return libraryPane;
    }
    
    public mxUndoManager getUndoManager() {
        return undoManager;
    }
    
    @SuppressWarnings("serial")
    public Action bind(String name, final Action action) {
        AbstractAction newAction = new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(new ActionEvent(getGraphComponent(), e.getID(), e.getActionCommand()));
            }
        };
        newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));
        return newAction;
    }
    
    public void status(String msg) {
        statusBar.setText(msg);
    }
    
    public void updateTitle() {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
        
        if (frame != null) {
            String title = (currentFile != null) ? currentFile
                    .getAbsolutePath() : "New Diagram";
            
            if (modified) {
                title += "*";
            }
            
            frame.setTitle(title + " - " + appTitle);
        }
    }
    
    public void exit() {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
        
        if (frame != null) {
            frame.dispose();
        }
    }
    
    public void setLookAndFeel(String clazz) {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
        
        if (frame != null) {
            try {
                UIManager.setLookAndFeel(clazz);
                SwingUtilities.updateComponentTreeUI(frame);
                
                // Needs to assign the key bindings again
                keyboardHandler = new KeyboardHandler(graphComponent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
    
    public JFrame createFrame(JMenuBar menuBar) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        frame.setSize(870, 640);
        
        // Updates the frame title
        updateTitle();
        
        return frame;
    }
    
    @SuppressWarnings("serial")
    public Action graphLayout(final String key, boolean animate) {
        final mxIGraphLayout layout = createLayout(key, animate);
        
        if (layout != null) {
            return new AbstractAction(key) {
                public void actionPerformed(ActionEvent e) {
                    final mxGraph graph = graphComponent.getGraph();
                    Object cell = graph.getSelectionCell();
                    
                    if (cell == null || graph.getModel().getChildCount(cell) == 0) {
                        cell = graph.getDefaultParent();
                    }
                    
                    graph.getModel().beginUpdate();
                    try {
                        long t0 = System.currentTimeMillis();
                        layout.execute(cell);
                        status("Layout: " + (System.currentTimeMillis() - t0) + " ms");
                    } finally {
                        mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);
                        morph.addListener(mxEvent.DONE, (sender, evt) -> graph.getModel().endUpdate());
                        morph.startAnimation();
                    }
                }
            };
        } else {
            return new AbstractAction(key) {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(graphComponent, "No Layout");
                }
            };
        }
    }
    
    protected mxIGraphLayout createLayout(String ident, boolean animate) {
        mxIGraphLayout layout = null;
        
        if (ident != null) {
            mxGraph graph = graphComponent.getGraph();
            
            switch (ident) {
                case "verticalHierarchical":
                    layout = new mxHierarchicalLayout(graph);
                    break;
                case "horizontalHierarchical":
                    layout = new mxHierarchicalLayout(graph, JLabel.WEST);
                    break;
                case "verticalTree":
                    layout = new mxCompactTreeLayout(graph, false);
                    break;
                case "horizontalTree":
                    layout = new mxCompactTreeLayout(graph, true);
                    break;
                case "parallelEdges":
                    layout = new mxParallelEdgeLayout(graph);
                    break;
                case "placeEdgeLabels":
                    layout = new mxEdgeLabelLayout(graph);
                    break;
                case "organicLayout":
                    layout = new mxOrganicLayout(graph);
                    break;
            }
            switch (ident) {
                case "verticalPartition":
                    layout = new mxPartitionLayout(graph, false) {
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "horizontalPartition":
                    layout = new mxPartitionLayout(graph, true) {
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "verticalStack":
                    layout = new mxStackLayout(graph, false) {
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "horizontalStack":
                    layout = new mxStackLayout(graph, true) {
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "circleLayout":
                    layout = new mxCircleLayout(graph);
                    break;
            }
        }
        
        return layout;
    }
    
}
