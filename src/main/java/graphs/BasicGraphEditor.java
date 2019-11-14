package graphs;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import graphs.editor.GraphEditor;
import graphs.editor.MenuBar;
import graphs.editor.Palette;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;

@SuppressWarnings("ALL")
public class BasicGraphEditor extends GraphEditor {
    
    public static final NumberFormat numberFormat = NumberFormat.getInstance();
    
    private static final long serialVersionUID = -4601740824088314699L;
    
    public BasicGraphEditor() {
        this("Algorithm Builder", new CustomGraphComponent(new CustomGraph()));
    }
    
    public BasicGraphEditor(String appTitle, mxGraphComponent component) {
        super(appTitle, component);
        final mxGraph graph = graphComponent.getGraph();
        
        // Creates the shapes palette
        Palette nodesPalette = insertPalette("Nodes");
        
        // Sets the edge template to be used for creating new edges if an edge
        // is clicked in the shape palette
        nodesPalette.addListener(mxEvent.SELECT, (sender, evt) -> {
            Object tmp = evt.getProperty("transferable");
            
            if (tmp instanceof mxGraphTransferable) {
                mxGraphTransferable t = (mxGraphTransferable) tmp;
                Object cell = t.getCells()[0];
                
                if (graph.getModel().isEdge(cell)) {
                    ((CustomGraph) graph).setEdgeTemplate(cell);
                }
            }
        });
        
        // Adds some template cells for dropping into the graph
        nodesPalette.addTemplate(
                "Operation",
                new ImageIcon(GraphEditor.class.getResource("/graphs/img/rectangle.png")),
                null, 100, 100, "Operation");
        nodesPalette.addTemplate(
                "Join",
                new ImageIcon(GraphEditor.class.getResource("/graphs/img/rhombus.png")),
                "rhombus", 50, 50, "Join");
        nodesPalette.addTemplate(
                "Fork",
                new ImageIcon(GraphEditor.class.getResource("/graphs/img/rounded.png")),
                "ellipse", 50, 50, "Fork");
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
        mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
        mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";
        
        GraphEditor editor = new BasicGraphEditor();
        editor.createFrame(new MenuBar(editor)).setVisible(true);
    }
    
    public static class CustomGraphComponent extends mxGraphComponent {
        private static final long serialVersionUID = -6833603133512882012L;
        
        public CustomGraphComponent(mxGraph graph) {
            super(graph);
            
            // Sets switches typically used in an editor
            setPageVisible(true);
            setGridVisible(true);
            setToolTips(true);
            getConnectionHandler().setCreateTarget(true);
            
            // Loads the defalt stylesheet from an external file
            mxCodec codec = new mxCodec();
            Document doc = mxUtils.loadDocument(GraphEditor.class.getResource(
                    "/graphs/style/default-style.xml")
                    .toString());
            codec.decode(doc.getDocumentElement(), graph.getStylesheet());
            
            // Sets the background to white
            getViewport().setOpaque(true);
            getViewport().setBackground(Color.WHITE);
        }
        
        public Object[] importCells(Object[] cells, double dx, double dy,
                                    Object target, Point location) {
            if (target == null && cells.length == 1 && location != null) {
                target = getCellAt(location.x, location.y);
                
                if (target instanceof mxICell && cells[0] instanceof mxICell) {
                    mxICell targetCell = (mxICell) target;
                    mxICell dropCell = (mxICell) cells[0];
                    
                    if (targetCell.isVertex() == dropCell.isVertex()
                            || targetCell.isEdge() == dropCell.isEdge()) {
                        mxIGraphModel model = graph.getModel();
                        model.setStyle(target, model.getStyle(cells[0]));
                        graph.setSelectionCell(target);
                        
                        return null;
                    }
                }
            }
            
            return super.importCells(cells, dx, dy, target, location);
        }
        
    }
    
    public static class CustomGraph extends mxGraph {
        protected Object edgeTemplate;
        
        public CustomGraph() {
            setAlternateEdgeStyle(mxConstants.EDGESTYLE_TOPTOBOTTOM);
        }
        
        public void setEdgeTemplate(Object template) {
            edgeTemplate = template;
        }
        
        public String getToolTipForCell(Object cell) {
            StringBuilder tip = new StringBuilder("<html>");
            mxGeometry geo = getModel().getGeometry(cell);
            mxCellState state = getView().getState(cell);
            
            if (getModel().isEdge(cell)) {
                tip.append("points={");
                
                if (geo != null) {
                    List<mxPoint> points = geo.getPoints();
                    
                    if (points != null) {
                        
                        for (mxPoint point : points) {
                            tip.append("[x=").append(numberFormat.format(point.getX())).append(",y=").append(numberFormat.format(point.getY())).append("],");
                        }
                        
                        tip = new StringBuilder(tip.substring(0, tip.length() - 1));
                    }
                }
                
                tip.append("}<br>");
                tip.append("absPoints={");
                
                if (state != null) {
                    
                    for (int i = 0; i < state.getAbsolutePointCount(); i++) {
                        mxPoint point = state.getAbsolutePoint(i);
                        tip.append("[x=").append(numberFormat.format(point.getX())).append(",y=").append(numberFormat.format(point.getY())).append("],");
                    }
                    
                    tip = new StringBuilder(tip.substring(0, tip.length() - 1));
                }
                
                tip.append("}");
            } else {
                tip.append("geo=[");
                
                if (geo != null) {
                    tip.append("x=").append(numberFormat.format(geo.getX())).append(",y=").append(numberFormat.format(geo.getY())).append(",width=").append(numberFormat.format(geo.getWidth())).append(",height=").append(numberFormat.format(geo.getHeight()));
                }
                
                tip.append("]<br>");
                tip.append("state=[");
                
                if (state != null) {
                    tip.append("x=").append(numberFormat.format(state.getX())).append(",y=").append(numberFormat.format(state.getY())).append(",width=").append(numberFormat.format(state.getWidth())).append(",height=").append(numberFormat.format(state.getHeight()));
                }
                
                tip.append("]");
            }
            
            mxPoint trans = getView().getTranslate();
            
            tip.append("<br>scale=").append(numberFormat.format(getView().getScale())).append(", translate=[x=").append(numberFormat.format(trans.getX())).append(",y=").append(numberFormat.format(trans.getY())).append("]");
            tip.append("</html>");
            
            return tip.toString();
        }
        
        
        public Object createEdge(Object parent, String id, Object value,
                                 Object source, Object target, String style) {
            if (edgeTemplate != null) {
                mxCell edge = (mxCell) cloneCells(new Object[]{edgeTemplate})[0];
                edge.setId(id);
                
                return edge;
            }
            
            return super.createEdge(parent, id, value, source, target, style);
        }
        
    }
}
