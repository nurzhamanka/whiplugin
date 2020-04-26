package graphs;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.OpType;
import graphs.model.Operation;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlgorithmGraph extends DefaultDirectedGraph<Operation, DefaultEdge> {
    
    private mxGraph oldGraph;
    private Set<Operation> sources;
    private Set<Operation> targets;
    
    public AlgorithmGraph(mxGraph oldGraph) {
        super(DefaultEdge.class);
        this.oldGraph = oldGraph;
        sources = new HashSet<>();
        targets = new HashSet<>();
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
            
            Operation srcOp = null;
            Operation trgOp = null;
            
            if (src != null) {
                srcOp = (Operation) src.getValue();
                if (srcOp.getType() == OpType.INPUT)
                    sources.add(srcOp);
                addVertex(srcOp);
            }
            if (trg != null) {
                trgOp = (Operation) trg.getValue();
                if (trgOp.getType() == OpType.OUTPUT)
                    targets.add(trgOp);
                addVertex(trgOp);
            }
            if (src != null && trg != null)
                addEdge(srcOp, trgOp);
        }
    }
    
    public List<GraphPath<Operation, DefaultEdge>> getAllPipelines() {
        AllDirectedPaths<Operation, DefaultEdge> adp = new AllDirectedPaths<>(this);
        return adp.getAllPaths(sources, targets, true, null);
    }
}
