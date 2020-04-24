package graphs;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.Operation;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;

public class AlgorithmGraph extends DefaultDirectedGraph<Operation, DefaultEdge> {
    
    private mxGraph oldGraph;
    
    public AlgorithmGraph(mxGraph oldGraph) {
        super(DefaultEdge.class);
        this.oldGraph = oldGraph;
        convertGraph();
    }
    
    private void convertGraph() {
        oldGraph.selectCells(false, true);
        mxCell[] edges = Arrays.stream(oldGraph.getSelectionCells()).toArray(mxCell[]::new);
        oldGraph.selectCells(false, false);
        assert Arrays.stream(edges).allMatch(mxCell::isEdge);
        for (mxCell edge : edges) {
            mxCell src = (mxCell) edge.getSource();
            mxCell trg = (mxCell) edge.getTarget();
            
            System.out.println("src: " + (src != null ? src : "null") + "; trg: " + (trg != null ? trg : "null"));
            
            Operation srcOp = null;
            Operation trgOp = null;
            
            if (src != null) {
                srcOp = (Operation) src.getValue();
                addVertex(srcOp);
            }
            if (trg != null) {
                trgOp = (Operation) trg.getValue();
                addVertex(trgOp);
            }
            if (src != null && trg != null)
                addEdge(srcOp, trgOp);
        }
    }
}
