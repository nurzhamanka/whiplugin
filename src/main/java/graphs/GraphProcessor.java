package graphs;

import graphs.model.*;
import net.imagej.ops.AbstractOp;
import net.imglib2.img.Img;
import ops.*;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* this was supposed to be the graph processing module */
public class GraphProcessor {
    
    private ImageJCore core;
    private AlgorithmGraph algoGraph;
    
    private File inputDir, outputDir;
    private String datasetName;
    private Img inputImg, outputImg;
    
    public GraphProcessor(ImageJCore core, AlgorithmGraph algoGraph) {
        this.core = core;
        this.algoGraph = algoGraph;
    }
    
    public List<Payload> parsePath(GraphPath<Operation, DefaultEdge> path) {
        List<Operation> operationSequence = path.getVertexList();
        List<Payload> pipeline = new ArrayList<>();
        for (Operation operation : operationSequence) {
            Payload p;
            switch (operation.getType()) {
                case INPUT:
                    datasetName = ((InputOp) operation).getDatasetName();
                    inputDir = ((InputOp) operation).getInputDirectory();
                    core.getLog().info("Added input dataset <" + datasetName + "> at " + inputDir);
                    break;
                case OUTPUT:
                    outputDir = ((OutputOp) operation).getOutputDirectory();
                    core.getLog().info("Will be saved to " + outputDir);
                    break;
                case NORMALIZE:
                    p = new Payload(Normalize.class, Arrays.asList(0.0, 1.0));
                    pipeline.add(p);
                    core.getLog().info(" - Normalize image");
                    break;
                case MATH:
                    MathOp mOp = (MathOp) operation;
                    p = new Payload(MathFilter.class, Arrays.asList(mOp.getMathType(), mOp.getArg()));
                    pipeline.add(p);
                    core.getLog().info(" - MathFilter: " + mOp.getMathType().toString() + ", arg = " + mOp.getArg());
                    break;
                case DOG:
                    DoGaussOp dogOp = (DoGaussOp) operation;
                    p = new Payload(GaussianDifference.class, Arrays.asList(dogOp.getSigma1(), dogOp.getSigma2()));
                    pipeline.add(p);
                    core.getLog().info(" - Difference of Gaussians: sigma1 = " + dogOp.getSigma1() + ", sigma2 = " + dogOp.getSigma2());
                    break;
                case HYBRID:
                    HybridOp hOp = (HybridOp) operation;
                    p = new Payload(HybridFilter.class, Collections.singletonList(hOp.getKernelSize()));
                    pipeline.add(p);
                    core.getLog().info(" - Hybrid Filter: kernelSize = " + hOp.getKernelSize());
                    break;
                case TOPHAT:
                    TopHatOp thOp = (TopHatOp) operation;
                    p = new Payload(TophatImage.class, Arrays.asList(thOp.getThType(), thOp.getShape(), thOp.getRadius()));
                    pipeline.add(p);
                    core.getLog().info(String.format(" - %s TopHat with %s structuring element of radius %s", thOp.getThType().toString(), thOp.getShape(), thOp.getRadius()));
                    break;
                case MEAN:
                    p = new Payload(AverageFilter.class, Collections.singletonList(((SmoothOp) operation).getKernelSize()));
                    pipeline.add(p);
                    core.getLog().info(" - Mean Smoothing: kernelSize = " + ((SmoothOp) operation).getKernelSize());
                    break;
                case BINARIZE:
                    BinarizeOp bOp = (BinarizeOp) operation;
                    p = new Payload(Binarize.class, Arrays.asList(bOp.getThreshType(), bOp.getDenominator()));
                    pipeline.add(p);
                    core.getLog().info(String.format(" - Binarization with threshold %s/%.4f", bOp.getThreshType(), bOp.getDenominator()));
                    break;
            }
        }
        return pipeline;
    }
    
    public void executePipeline(List<Payload> pipeline) {
        core.getLog().info("Processing <" + datasetName + ">...");
        // execute each Payload
    }
    
    private class Payload {
        
        private Class<? extends AbstractOp> opClass;
        private List<Object> args;
        
        Payload(Class<? extends AbstractOp> opClass, List<Object> args) {
            this.opClass = opClass;
            this.args = args;
        }
        
        public Class<? extends AbstractOp> getOpClass() {
            return opClass;
        }
        
        public void setOpClass(Class<? extends AbstractOp> opClass) {
            this.opClass = opClass;
        }
        
        public List<Object> getArgs() {
            return args;
        }
        
        public void setArgs(List<Object> args) {
            this.args = args;
        }
    }
}