package graphs.editor;

import com.mxgraph.analysis.StructuralException;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphProperties;
import com.mxgraph.analysis.mxGraphStructure;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxGdCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.*;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.util.png.mxPngTextDecoder;
import com.mxgraph.view.mxGraph;
import graphs.AlgorithmGraph;
import graphs.editor.dialog.PropertiesDialog;
import graphs.editor.dialog.PropertiesDialogFactory;
import graphs.model.OpType;
import graphs.model.Operation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("ALL")
public class Actions {
    
    public static GraphEditor getEditor(ActionEvent e) {
        if (e.getSource() instanceof Component) {
            Component component = (Component) e.getSource();
            
            while (component != null
                    && !(component instanceof GraphEditor)) {
                component = component.getParent();
            }
            
            return (GraphEditor) component;
        }
        
        return null;
    }
    
    public static class ToggleGridItem extends JCheckBoxMenuItem {
        public ToggleGridItem(final GraphEditor editor, String name) {
            super(name);
            setSelected(true);
    
            addActionListener(e -> {
                mxGraphComponent graphComponent = editor.getGraphComponent();
                mxGraph graph = graphComponent.getGraph();
                boolean enabled = !graph.isGridEnabled();
                graph.setGridEnabled(enabled);
                graphComponent.setGridVisible(enabled);
                graphComponent.repaint();
                setSelected(enabled);
            });
        }
    }
    
    public static class ExitAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            GraphEditor editor = getEditor(e);
            
            if (editor != null) {
                editor.exit();
            }
        }
    }
    
    public static class StylesheetAction extends AbstractAction {
        protected String stylesheet;
        
        public StylesheetAction(String stylesheet) {
            this.stylesheet = stylesheet;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                mxGraph graph = graphComponent.getGraph();
                mxCodec codec = new mxCodec();
                Document doc = mxUtils.loadDocument(Actions.class
                        .getResource(stylesheet).toString());
                
                if (doc != null) {
                    codec.decode(doc.getDocumentElement(),
                            graph.getStylesheet());
                    graph.refresh();
                }
            }
        }
    }
    
    public static class ZoomPolicyAction extends AbstractAction {
        protected int zoomPolicy;
        
        public ZoomPolicyAction(int zoomPolicy) {
            this.zoomPolicy = zoomPolicy;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                graphComponent.setPageVisible(true);
                graphComponent.setZoomPolicy(zoomPolicy);
            }
        }
    }
    
    public static class ScaleAction extends AbstractAction {
        protected double scale;
        
        public ScaleAction(double scale) {
            this.scale = scale;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                double scale = this.scale;
                
                if (scale == 0) {
                    String value = (String) JOptionPane.showInputDialog(
                            graphComponent, "Value",
                            "Scale" + " (%)",
                            JOptionPane.PLAIN_MESSAGE, null, null, "");
                    
                    if (value != null) {
                        scale = Double.parseDouble(value.replace("%", "")) / 100;
                    }
                }
                
                if (scale > 0) {
                    graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
                }
            }
        }
    }
    
    public static class SaveAction extends AbstractAction {
        protected boolean showDialog;
        protected String lastDir = null;
        
        public SaveAction(boolean showDialog) {
            this.showDialog = showDialog;
        }
        
        protected void saveXmlPng(GraphEditor editor, String filename,
                                  Color bg) throws IOException {
            mxGraphComponent graphComponent = editor.getGraphComponent();
            mxGraph graph = graphComponent.getGraph();
            
            // Creates the image for the PNG file
            BufferedImage image = mxCellRenderer.createBufferedImage(graph,
                    null, 1, bg, graphComponent.isAntiAlias(), null,
                    graphComponent.getCanvas());
            
            // Creates the URL-encoded XML data
            mxCodec codec = new mxCodec();
            String xml = URLEncoder.encode(
                    mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
            mxPngEncodeParam param = mxPngEncodeParam
                    .getDefaultEncodeParam(image);
            param.setCompressedText(new String[]{"mxGraphModel", xml});
            
            // Saves as a PNG file
            FileOutputStream outputStream = new FileOutputStream(new File(
                    filename));
            try {
                mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream,
                        param);
                
                if (image != null) {
                    encoder.encode(image);
                    
                    editor.setModified(false);
                    editor.setCurrentFile(new File(filename));
                } else {
                    JOptionPane.showMessageDialog(graphComponent,
                            "No Image Data");
                }
            } finally {
                outputStream.close();
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            GraphEditor editor = getEditor(e);
            
            if (editor != null) {
                mxGraphComponent graphComponent = editor.getGraphComponent();
                mxGraph graph = graphComponent.getGraph();
                FileFilter selectedFilter = null;
                DefaultFileFilter xmlPngFilter = new DefaultFileFilter(".png",
                        "PNG+XML " + "File" + " (.png)");
                FileFilter vmlFileFilter = new DefaultFileFilter(".html",
                        "VML " + "File" + " (.html)");
                String filename = null;
                boolean dialogShown = false;
                
                if (showDialog || editor.getCurrentFile() == null) {
                    String wd;
                    
                    if (lastDir != null) {
                        wd = lastDir;
                    } else if (editor.getCurrentFile() != null) {
                        wd = editor.getCurrentFile().getParent();
                    } else {
                        wd = System.getProperty("user.dir");
                    }
                    
                    JFileChooser fc = new JFileChooser(wd);
                    
                    // Adds the default file format
                    fc.addChoosableFileFilter(xmlPngFilter);
                    
                    // Adds special vector graphics formats and HTML
                    fc.addChoosableFileFilter(new DefaultFileFilter(".mxe",
                            "mxGraph Editor " + "File"
                                    + " (.mxe)"));
                    fc.addChoosableFileFilter(new DefaultFileFilter(".txt",
                            "Graph Drawing " + "File"
                                    + " (.txt)"));
                    fc.addChoosableFileFilter(new DefaultFileFilter(".svg",
                            "SVG " + "File" + " (.svg)"));
                    fc.addChoosableFileFilter(vmlFileFilter);
                    fc.addChoosableFileFilter(new DefaultFileFilter(".html",
                            "HTML " + "File" + " (.html)"));
                    
                    // Adds a filter for each supported image format
                    Object[] imageFormats = ImageIO.getReaderFormatNames();
                    
                    // Finds all distinct extensions
                    HashSet<String> formats = new HashSet<>();
                    
                    for (int i = 0; i < imageFormats.length; i++) {
                        String ext = imageFormats[i].toString().toLowerCase();
                        formats.add(ext);
                    }
                    
                    imageFormats = formats.toArray();
                    
                    for (int i = 0; i < imageFormats.length; i++) {
                        String ext = imageFormats[i].toString();
                        fc.addChoosableFileFilter(new DefaultFileFilter("."
                                + ext, ext.toUpperCase() + " "
                                + "File" + " (." + ext + ")"));
                    }
                    
                    // Adds filter that accepts all supported image formats
                    fc.addChoosableFileFilter(new DefaultFileFilter.ImageFileFilter(
                            "All Images"));
                    fc.setFileFilter(xmlPngFilter);
                    int rc = fc.showDialog(null, "Save");
                    dialogShown = true;
                    
                    if (rc != JFileChooser.APPROVE_OPTION) {
                        return;
                    } else {
                        lastDir = fc.getSelectedFile().getParent();
                    }
                    
                    filename = fc.getSelectedFile().getAbsolutePath();
                    selectedFilter = fc.getFileFilter();
                    
                    if (selectedFilter instanceof DefaultFileFilter) {
                        String ext = ((DefaultFileFilter) selectedFilter)
                                .getExtension();
                        
                        if (!filename.toLowerCase().endsWith(ext)) {
                            filename += ext;
                        }
                    }
                    
                    if (new File(filename).exists()
                            && JOptionPane.showConfirmDialog(graphComponent,
                            "Overwrite existing file") != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else {
                    filename = editor.getCurrentFile().getAbsolutePath();
                }
                
                try {
                    String ext = filename
                            .substring(filename.lastIndexOf('.') + 1);
                    
                    if (ext.equalsIgnoreCase("svg")) {
                        mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer
                                .drawCells(graph, null, 1, null,
                                        new CanvasFactory() {
                                            public mxICanvas createCanvas(
                                                    int width, int height) {
                                                mxSvgCanvas canvas = new mxSvgCanvas(
                                                        mxDomUtils.createSvgDocument(
                                                                width, height));
                                                canvas.setEmbedded(true);
                                                
                                                return canvas;
                                            }
                                            
                                        });
                        
                        mxUtils.writeFile(mxXmlUtils.getXml(canvas.getDocument()),
                                filename);
                    } else if (selectedFilter == vmlFileFilter) {
                        mxUtils.writeFile(mxXmlUtils.getXml(mxCellRenderer
                                .createVmlDocument(graph, null, 1, null, null)
                                .getDocumentElement()), filename);
                    } else if (ext.equalsIgnoreCase("html")) {
                        mxUtils.writeFile(mxXmlUtils.getXml(mxCellRenderer
                                .createHtmlDocument(graph, null, 1, null, null)
                                .getDocumentElement()), filename);
                    } else if (ext.equalsIgnoreCase("mxe")
                            || ext.equalsIgnoreCase("xml")) {
                        mxCodec codec = new mxCodec();
                        String xml = mxXmlUtils.getXml(codec.encode(graph
                                .getModel()));
                        
                        mxUtils.writeFile(xml, filename);
                        
                        editor.setModified(false);
                        editor.setCurrentFile(new File(filename));
                    } else if (ext.equalsIgnoreCase("txt")) {
                        String content = mxGdCodec.encode(graph);
                        
                        mxUtils.writeFile(content, filename);
                    } else {
                        Color bg = null;
                        
                        if ((!ext.equalsIgnoreCase("gif") && !ext
                                .equalsIgnoreCase("png"))
                                || JOptionPane.showConfirmDialog(
                                graphComponent, mxResources
                                        .get("transparentBackground")) != JOptionPane.YES_OPTION) {
                            bg = graphComponent.getBackground();
                        }
                        
                        if (selectedFilter == xmlPngFilter
                                || (editor.getCurrentFile() != null
                                && ext.equalsIgnoreCase("png") && !dialogShown)) {
                            saveXmlPng(editor, filename, bg);
                        } else {
                            BufferedImage image = mxCellRenderer
                                    .createBufferedImage(graph, null, 1, bg,
                                            graphComponent.isAntiAlias(), null,
                                            graphComponent.getCanvas());
                            
                            if (image != null) {
                                ImageIO.write(image, ext, new File(filename));
                            } else {
                                JOptionPane.showMessageDialog(graphComponent,
                                        "No Image Data");
                            }
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(graphComponent,
                            ex.toString(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public static class ToggleDirtyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                graphComponent.showDirtyRectangle = !graphComponent.showDirtyRectangle;
            }
        }
        
    }
    
    public static class ToggleConnectModeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                mxConnectionHandler handler = graphComponent
                        .getConnectionHandler();
                handler.setHandleEnabled(!handler.isHandleEnabled());
            }
        }
    }
    
    public static class ToggleCreateTargetItem extends JCheckBoxMenuItem {
        public ToggleCreateTargetItem(final GraphEditor editor, String name) {
            super(name);
            setSelected(true);
    
            addActionListener(e -> {
                mxGraphComponent graphComponent = editor
                        .getGraphComponent();
        
                if (graphComponent != null) {
                    mxConnectionHandler handler = graphComponent
                            .getConnectionHandler();
                    handler.setCreateTarget(!handler.isCreateTarget());
                    setSelected(handler.isCreateTarget());
                }
            });
        }
    }
    
    public static class PromptPropertyAction extends AbstractAction {
        protected Object target;
        protected String fieldname, message;
        
        public PromptPropertyAction(Object target, String message) {
            this(target, message, message);
        }
        
        public PromptPropertyAction(Object target, String message,
                                    String fieldname) {
            this.target = target;
            this.message = message;
            this.fieldname = fieldname;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof Component) {
                try {
                    Method getter = target.getClass().getMethod(
                            "get" + fieldname);
                    Object current = getter.invoke(target);
                    
                    // TODO: Support other atomic types
                    if (current instanceof Integer) {
                        Method setter = target.getClass().getMethod(
                                "set" + fieldname, int.class);
                        
                        String value = (String) JOptionPane.showInputDialog(
                                (Component) e.getSource(), "Value", message,
                                JOptionPane.PLAIN_MESSAGE, null, null, current);
                        
                        if (value != null) {
                            setter.invoke(target, Integer.parseInt(value));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            // Repaints the graph component
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                graphComponent.repaint();
            }
        }
    }
    
    public static class TogglePropertyItem extends JCheckBoxMenuItem {
        public TogglePropertyItem(Object target, String name, String fieldname) {
            this(target, name, fieldname, false);
        }
        
        public TogglePropertyItem(Object target, String name, String fieldname,
                                  boolean refresh) {
            this(target, name, fieldname, refresh, null);
        }
        
        public TogglePropertyItem(final Object target, String name,
                                  final String fieldname, final boolean refresh,
                                  ActionListener listener) {
            super(name);
            
            // Since action listeners are processed last to first we add the given
            // listener here which means it will be processed after the one below
            if (listener != null) {
                addActionListener(listener);
            }
    
            addActionListener(e -> execute(target, fieldname, refresh));
    
            PropertyChangeListener propertyChangeListener = evt -> {
                if (evt.getPropertyName().equalsIgnoreCase(fieldname)) {
                    update(target, fieldname);
                }
            };
            
            if (target instanceof mxGraphComponent) {
                ((mxGraphComponent) target)
                        .addPropertyChangeListener(propertyChangeListener);
            } else if (target instanceof mxGraph) {
                ((mxGraph) target)
                        .addPropertyChangeListener(propertyChangeListener);
            }
            
            update(target, fieldname);
        }
        
        public void update(Object target, String fieldname) {
            if (target != null && fieldname != null) {
                try {
                    Method getter = target.getClass().getMethod(
                            "is" + fieldname);
                    
                    if (getter != null) {
                        Object current = getter.invoke(target);
                        
                        if (current instanceof Boolean) {
                            setSelected(((Boolean) current).booleanValue());
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        public void execute(Object target, String fieldname, boolean refresh) {
            if (target != null && fieldname != null) {
                try {
                    Method getter = target.getClass().getMethod(
                            "is" + fieldname);
                    Method setter = target.getClass().getMethod(
                            "set" + fieldname, boolean.class);
                    
                    Object current = getter.invoke(target);
                    
                    if (current instanceof Boolean) {
                        boolean value = !((Boolean) current).booleanValue();
                        setter.invoke(target, value);
                        setSelected(value);
                    }
                    
                    if (refresh) {
                        mxGraph graph = null;
                        
                        if (target instanceof mxGraph) {
                            graph = (mxGraph) target;
                        } else if (target instanceof mxGraphComponent) {
                            graph = ((mxGraphComponent) target).getGraph();
                        }
                        
                        graph.refresh();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
    
    public static class HistoryAction extends AbstractAction {
        protected boolean undo;
        
        public HistoryAction(boolean undo) {
            this.undo = undo;
        }
        
        public void actionPerformed(ActionEvent e) {
            GraphEditor editor = getEditor(e);
            
            if (editor != null) {
                if (undo) {
                    editor.getUndoManager().undo();
                } else {
                    editor.getUndoManager().redo();
                }
            }
        }
    }
    
    public static class NewAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            GraphEditor editor = getEditor(e);
            
            if (editor != null) {
                if (editor.isNotModified()
                        || JOptionPane.showConfirmDialog(editor,
                        "Lose changes?") == JOptionPane.YES_OPTION) {
                    mxGraph graph = editor.getGraphComponent().getGraph();
                    
                    // Check modified flag and display save dialog
                    mxCell root = new mxCell();
                    root.insert(new mxCell());
                    graph.getModel().setRoot(root);
                    
                    editor.setModified(false);
                    editor.setCurrentFile(null);
                    editor.getGraphComponent().zoomAndCenter();
                }
            }
        }
    }
    
    public static class OpenAction extends AbstractAction {
        protected String lastDir;
    
        @SuppressWarnings("WeakerAccess")
        protected void resetEditor(GraphEditor editor) {
            editor.setModified(false);
            editor.getUndoManager().clear();
            editor.getGraphComponent().zoomAndCenter();
        }
        
        protected void openXmlPng(GraphEditor editor, File file)
                throws IOException {
            Map<String, String> text = mxPngTextDecoder
                    .decodeCompressedText(new FileInputStream(file));
            
            if (text != null) {
                String value = text.get("mxGraphModel");
                
                if (value != null) {
                    Document document = mxXmlUtils.parseXml(URLDecoder.decode(
                            value, "UTF-8"));
                    mxCodec codec = new mxCodec(document);
                    codec.decode(document.getDocumentElement(), editor
                            .getGraphComponent().getGraph().getModel());
                    editor.setCurrentFile(file);
                    resetEditor(editor);
                    
                    return;
                }
            }
            
            JOptionPane.showMessageDialog(editor, "Image contains no diagram data");
        }
        
        protected void openGD(GraphEditor editor, File file,
                              String gdText) {
            mxGraph graph = editor.getGraphComponent().getGraph();
            
            // Replaces file extension with .mxe
            String filename = file.getName();
            filename = filename.substring(0, filename.length() - 4) + ".mxe";
            
            if (new File(filename).exists()
                    && JOptionPane.showConfirmDialog(editor,
                    "Overwrite existing file?") != JOptionPane.YES_OPTION) {
                return;
            }
            
            ((mxGraphModel) graph.getModel()).clear();
            mxGdCodec.decode(gdText, graph);
            editor.getGraphComponent().zoomAndCenter();
            editor.setCurrentFile(new File(lastDir + "/" + filename));
        }
        
        public void actionPerformed(ActionEvent e) {
            GraphEditor editor = getEditor(e);
            
            if (editor != null) {
                if (editor.isNotModified()
                        || JOptionPane.showConfirmDialog(editor,
                        "Lose changes?") == JOptionPane.YES_OPTION) {
                    mxGraph graph = editor.getGraphComponent().getGraph();
                    
                    if (graph != null) {
                        String wd = (lastDir != null) ? lastDir : System
                                .getProperty("user.dir");
                        
                        JFileChooser fc = new JFileChooser(wd);
                        
                        // Adds file filter for supported file format
                        DefaultFileFilter defaultFilter = new DefaultFileFilter(
                                ".mxe", "All supported formats"
                                + " (.mxe, .png, .vdx)") {
                            
                            public boolean accept(File file) {
                                String lcase = file.getName().toLowerCase();
                                
                                return super.accept(file)
                                        || lcase.endsWith(".png")
                                        || lcase.endsWith(".vdx");
                            }
                        };
                        fc.addChoosableFileFilter(defaultFilter);
                        
                        fc.addChoosableFileFilter(new DefaultFileFilter(".mxe",
                                "mxGraph Editor " + "File"
                                        + " (.mxe)"));
                        fc.addChoosableFileFilter(new DefaultFileFilter(".png",
                                "PNG+XML  " + "File"
                                        + " (.png)"));
                        
                        // Adds file filter for VDX import
                        fc.addChoosableFileFilter(new DefaultFileFilter(".vdx",
                                "XML Drawing  " + "File"
                                        + " (.vdx)"));
                        
                        // Adds file filter for GD import
                        fc.addChoosableFileFilter(new DefaultFileFilter(".txt",
                                "Graph Drawing  " + "File"
                                        + " (.txt)"));
                        
                        fc.setFileFilter(defaultFilter);
                        
                        int rc = fc.showDialog(null,
                                "Open file");
                        
                        if (rc == JFileChooser.APPROVE_OPTION) {
                            lastDir = fc.getSelectedFile().getParent();
                            
                            try {
                                if (fc.getSelectedFile().getAbsolutePath()
                                        .toLowerCase().endsWith(".png")) {
                                    openXmlPng(editor, fc.getSelectedFile());
                                } else if (fc.getSelectedFile().getAbsolutePath()
                                        .toLowerCase().endsWith(".txt")) {
                                    openGD(editor, fc.getSelectedFile(),
                                            mxUtils.readFile(fc
                                                    .getSelectedFile()
                                                    .getAbsolutePath()));
                                } else {
                                    Document document = mxXmlUtils
                                            .parseXml(mxUtils.readFile(fc
                                                    .getSelectedFile()
                                                    .getAbsolutePath()));
                                    
                                    mxCodec codec = new mxCodec(document);
                                    codec.decode(
                                            document.getDocumentElement(),
                                            graph.getModel());
                                    editor.setCurrentFile(fc
                                            .getSelectedFile());
                                    
                                    resetEditor(editor);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(
                                        editor.getGraphComponent(),
                                        ex.toString(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static class ToggleAction extends AbstractAction {
        protected String key;
        protected boolean defaultValue;
        
        public ToggleAction(String key) {
            this(key, false);
        }
        
        public ToggleAction(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
        
        public void actionPerformed(ActionEvent e) {
            mxGraph graph = mxGraphActions.getGraph(e);
            
            if (graph != null) {
                graph.toggleCellStyles(key, defaultValue);
            }
        }
    }
    
    public static class KeyValueAction extends AbstractAction {
        protected String key, value;
        
        public KeyValueAction(String key) {
            this(key, null);
        }
        
        public KeyValueAction(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public void actionPerformed(ActionEvent e) {
            mxGraph graph = mxGraphActions.getGraph(e);
            
            if (graph != null && !graph.isSelectionEmpty()) {
                graph.setCellStyles(key, value);
            }
        }
    }
    
    public static class PromptValueAction extends AbstractAction {
        protected String key, message;
        
        public PromptValueAction(String key, String message) {
            this.key = key;
            this.message = message;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof Component) {
                mxGraph graph = mxGraphActions.getGraph(e);
                
                if (graph != null && !graph.isSelectionEmpty()) {
                    String value = (String) JOptionPane.showInputDialog(
                            (Component) e.getSource(),
                            "Value", message,
                            JOptionPane.PLAIN_MESSAGE, null, null, "");
                    
                    if (value != null) {
                        if (value.equals(mxConstants.NONE)) {
                            value = null;
                        }
                        
                        graph.setCellStyles(key, value);
                    }
                }
            }
        }
    }
    
    public static class AlignCellsAction extends AbstractAction {
        protected String align;
        
        public AlignCellsAction(String align) {
            this.align = align;
        }
        
        public void actionPerformed(ActionEvent e) {
            mxGraph graph = mxGraphActions.getGraph(e);
            
            if (graph != null && !graph.isSelectionEmpty()) {
                graph.alignCells(align);
            }
        }
    }
    
    public static class AutosizeAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            mxGraph graph = mxGraphActions.getGraph(e);
            
            if (graph != null && !graph.isSelectionEmpty()) {
                Object[] cells = graph.getSelectionCells();
                mxIGraphModel model = graph.getModel();
                
                model.beginUpdate();
                try {
                    for (int i = 0; i < cells.length; i++) {
                        graph.updateCellSize(cells[i]);
                    }
                } finally {
                    model.endUpdate();
                }
            }
        }
    }
    
    public static class ColorAction extends AbstractAction {
        protected String name, key;
        
        public ColorAction(String name, String key) {
            this.name = name;
            this.key = key;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                mxGraph graph = graphComponent.getGraph();
                
                if (!graph.isSelectionEmpty()) {
                    Color newColor = JColorChooser.showDialog(graphComponent,
                            name, null);
                    
                    if (newColor != null) {
                        graph.setCellStyles(key, mxUtils.hexString(newColor));
                    }
                }
            }
        }
    }
    
    public static class BackgroundAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                Color newColor = JColorChooser.showDialog(graphComponent,
                        "Background", null);
                
                if (newColor != null) {
                    graphComponent.getViewport().setOpaque(true);
                    graphComponent.getViewport().setBackground(newColor);
                }
                
                // Forces a repaint of the outline
                graphComponent.getGraph().repaint();
            }
        }
    }
    
    public static class StyleAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof mxGraphComponent) {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                mxGraph graph = graphComponent.getGraph();
                String initial = graph.getModel().getStyle(
                        graph.getSelectionCell());
                String value = (String) JOptionPane.showInputDialog(
                        graphComponent, "Style",
                        "Style", JOptionPane.PLAIN_MESSAGE,
                        null, null, initial);
                
                if (value != null) {
                    graph.setCellStyle(value);
                }
            }
        }
    }
    
    public static class PropertiesAction extends AbstractAction {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
            final mxGraph graph = graphComponent.getGraph();
            final mxCell cell = (mxCell) graph.getSelectionCell();
            final PropertiesDialog dialog = PropertiesDialogFactory.getDialog(cell, graph);
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension frameSize = dialog.getSize();
            dialog.setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));
        }
    }
    
    // todo: Action for packing the graph into a format that can be converted to ImageJ algorithms
    public static class ValidateGraphAction extends AbstractAction {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
            final mxGraph graph = graphComponent.getGraph();
    
            // valid = connected + DAG + all sources are inputs, all sinks are outputs
            boolean isInvalidated = false;
            
            // init an analysis graph
            final mxAnalysisGraph aGraph = new mxAnalysisGraph();
            aGraph.setGraph(graph);
            mxGraphProperties.setDirected(aGraph.getProperties(), true);
            
            boolean isConnected = mxGraphStructure.isConnected(aGraph);
    
            if (!isConnected) {
                System.out.println("Graph not connected");
            }
            
            boolean isDag = !mxGraphStructure.isCyclicDirected(aGraph);
    
            if (!isDag) {
                System.out.println("Graph not a DAG");
            }
            
            boolean isConnectedDag = isConnected && isDag;
    
            if (!isConnectedDag) isInvalidated = true;
            else {
                try {
                    boolean areSourcesValid = true;
                    mxCell[] sources = Arrays.stream(mxGraphStructure.getSourceVertices(aGraph)).toArray(mxCell[]::new);
                    for (mxCell source : sources) {
                        Operation op = (Operation) source.getValue();
                        if (op.getType() != OpType.INPUT) {
                            areSourcesValid = false;
                            break;
                        }
                    }
                    if (!areSourcesValid) isInvalidated = true;
                    else System.out.println("All sources are inputs");
                } catch (StructuralException e1) {
                    isInvalidated = true;
                    e1.printStackTrace();
                }
    
                try {
                    boolean areSinksValid = true;
                    mxCell[] sinks = (mxCell[]) mxGraphStructure.getSinkVertices(aGraph);
                    for (mxCell sink : sinks) {
                        Operation op = (Operation) sink.getValue();
                        if (op.getType() != OpType.OUTPUT) {
                            areSinksValid = false;
                            break;
                        }
                    }
                    if (!areSinksValid) isInvalidated = true;
                } catch (StructuralException e1) {
                    isInvalidated = true;
                    e1.printStackTrace();
                }
        
                if (!isInvalidated) {
                    System.out.println("Graph is valid!");
                    new AlgorithmGraph(graph);
                } else {
                    System.out.println("Graph is invalid!");
                }
            }
        }
    }
}
