package graphs;

import java.io.File;

/* this was supposed to be the graph processing module */
public class GraphProcessor {
    
    private ImageJCore core;
    private AlgorithmGraph algoGraph;
    
    private File inputDir, outputDir;
    
    public GraphProcessor(ImageJCore core, AlgorithmGraph algoGraph) {
        this.core = core;
        this.algoGraph = algoGraph;
    }
}
