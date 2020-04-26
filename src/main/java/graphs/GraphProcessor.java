package graphs;

import graphs.model.*;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imagej.ops.AbstractOp;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import ops.*;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.IOException;
import java.util.*;

/* this was supposed to be the graph processing module */
public class GraphProcessor {
    
    private ImageJCore core;
    private AlgorithmGraph algoGraph;
    
    private File inputDir, outputDir;
    private String datasetName;
    private List<GraphPath<Operation, DefaultEdge>> paths;
    private List<Payload> pipeline;
    
    private HashMap<File, Integer> outCount;
    
    public GraphProcessor(ImageJCore core, AlgorithmGraph algoGraph) {
        this.core = core;
        this.algoGraph = algoGraph;
        outCount = new HashMap<>();
        paths = algoGraph.getAllPipelines();
        processAllPaths();
    }
    
    private void processAllPaths() {
        for (GraphPath<Operation, DefaultEdge> path : paths) {
            core.getLog().info(">>>>>>>>>>>>>> PROCESSING NEW PATH <<<<<<<<<<<<<<<");
            parsePath(path);
            processCurrentDataset();
            core.getLog().info(">>>>>>>>>>>>>>>> PATH PROCESSED <<<<<<<<<<<<<<<<<<");
            core.getLog().info("--------------------------------------------------");
        }
        core.getLog().info("=============== PROCESSING FINISHED. =================");
    }
    
    private void parsePath(GraphPath<Operation, DefaultEdge> path) {
        List<Operation> operationSequence = path.getVertexList();
        pipeline = new ArrayList<>();
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
                    if (!outCount.containsKey(outputDir)) {
                        outCount.put(outputDir, 0);
                    }
                    outCount.put(outputDir, outCount.get(outputDir) + 1);
                    core.getLog().info("Will be saved to " + outputDir + " as <" + datasetName + "-" + outCount.get(outputDir) + ">");
                    break;
                case NORMALIZE:
                    p = new Payload(Normalize.class, Arrays.asList(0.0, 1.0));
                    pipeline.add(p);
                    core.getLog().info("- Normalize image");
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
                    core.getLog().info("- Difference of Gaussians: sigma1 = " + dogOp.getSigma1() + ", sigma2 = " + dogOp.getSigma2());
                    break;
                case HYBRID:
                    HybridOp hOp = (HybridOp) operation;
                    p = new Payload(HybridFilter.class, Collections.singletonList(hOp.getKernelSize()));
                    pipeline.add(p);
                    core.getLog().info("- Hybrid Filter: kernelSize = " + hOp.getKernelSize());
                    break;
                case TOPHAT:
                    TopHatOp thOp = (TopHatOp) operation;
                    p = new Payload(TophatImage.class, Arrays.asList(thOp.getThType(), thOp.getShape(), thOp.getRadius()));
                    pipeline.add(p);
                    core.getLog().info(String.format("- %s TopHat with %s structuring element of radius %s", thOp.getThType().toString(), thOp.getShape(), thOp.getRadius()));
                    break;
                case MEAN:
                    p = new Payload(AverageFilter.class, Collections.singletonList(((SmoothOp) operation).getKernelSize()));
                    pipeline.add(p);
                    core.getLog().info("- Mean Smoothing: kernelSize = " + ((SmoothOp) operation).getKernelSize());
                    break;
                case BINARIZE:
                    BinarizeOp bOp = (BinarizeOp) operation;
                    p = new Payload(Binarize.class, Arrays.asList(bOp.getThreshType(), bOp.getDenominator(), bOp.getDoParticleAnalysis()));
                    pipeline.add(p);
                    core.getLog().info(String.format("- Binarization with threshold %s/%.4f", bOp.getThreshType(), bOp.getDenominator()));
                    break;
            }
        }
    }
    
    private void processCurrentDataset() {
        core.getLog().info("Processing <" + datasetName + ">...");
        // execute each Payload
        // todo: there can be several path leading to an output: separate each path by folder
        
        File saveDir = new File(outputDir.getAbsolutePath() + File.separator + datasetName + "-" + outCount.get(outputDir));
        if (!saveDir.mkdir()) {
            core.getLog().info("Could not create " + saveDir.getAbsolutePath());
            if (saveDir.exists()) core.getLog().info(" --- this directory exists");
            else {
                core.getLog().error("!- something went wrong, moving on to the next dataset...");
                return;
            }
        }
        
        final File[] inputImages = inputDir.listFiles(pathname -> !pathname.getAbsolutePath().contains(".DS_Store") && core.getDatasetIOService().canOpen(pathname.getAbsolutePath()));
        assert inputImages != null;
        if (inputImages.length == 0) {
            core.getLog().error("!- this dataset is empty...");
            assert saveDir.delete();
            return;
        }
        int currentImageNumber = 0;
        for (final File image : inputImages) {
            
            core.getStatusService().showProgress(currentImageNumber, inputImages.length);
            final long startTime = System.currentTimeMillis();
            core.getLog().info("-- processing " + image.getName());
            final String imgPath = image.getAbsolutePath();
            final Dataset currentImage;
            try {
                currentImage = core.getDatasetIOService().open(imgPath);
            } catch (IOException e) {
                core.getLog().error("!- invalid image: " + image.getName());
                continue;
            }
            
            // process the image
            final Dataset result = process(currentImage);
            
            try {
                core.getDatasetIOService().save(result, saveDir.getAbsolutePath() + File.separator + image.getName());
            } catch (IOException e) {
                core.getLog().error(e);
                continue;
            }
            final long endTime = System.currentTimeMillis();
            final long fd = endTime - startTime;
            core.getLog().info("-- " + image.getName() + " saved. It took " + fd / 1000.0 + "s.");
            currentImageNumber++;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Dataset process(Dataset image) {
        final Img<RealType<?>> result = process((Img) image.getImgPlus().getImg());
        
        // Finally, coerce the result back to an ImageJ Dataset object.
        return new DefaultDataset(image.context(), new ImgPlus<>(result));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> Img<T> process(final Img<T> image) {
        int numOperations = pipeline.size();
        int currentOperation = 0;
        
        // convert image to 64-bit
        Img<DoubleType> img = core.getOps().convert().float64(image);
        
        for (Payload p : pipeline) {
            Class<? extends AbstractOp> opClass = p.getOpClass();
            List<Object> args = p.getArgs();
            core.getLog().info("ClassName=" + opClass.getSimpleName());
            switch (opClass.getSimpleName()) {
                case "Normalize": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "normalizing image...");
                    double from = (Double) args.get(0);
                    double to = (Double) args.get(1);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, from, to);
                    break;
                }
                case "MathFilter": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying MathFilter...");
                    MathType mathType = (MathType) args.get(0);
                    Double n = (Double) args.get(1);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, mathType, n);
                    break;
                }
                case "GaussianDifference": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Difference of Gaussians...");
                    Double sigma1 = (Double) args.get(0);
                    Double sigma2 = (Double) args.get(1);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, sigma1, sigma2);
                    break;
                }
                case "HybridFilter": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Hybrid Filter...");
                    Integer kernelSize = (Integer) args.get(0);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, kernelSize);
                    break;
                }
                case "TophatImage": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Top-Hat...");
                    TopHatOp.ThType thType = (TopHatOp.ThType) args.get(0);
                    TopHatOp.Shape shape = (TopHatOp.Shape) args.get(1);
                    Integer radius = (Integer) args.get(2);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, thType, shape, radius);
                    break;
                }
                case "AverageFilter": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Mean Smoothing...");
                    Integer kernelSize = (Integer) args.get(0);
                    img = (Img<DoubleType>) core.getOps().run(opClass, img, kernelSize);
                    break;
                }
                case "Binarize": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Binarization...");
                    ThresholdType threshType = (ThresholdType) args.get(0);
                    Double denominator = (Double) args.get(1);
                    Boolean doParticleAnalysis = (Boolean) args.get(2);
                    Img<BitType> binImg = (Img<BitType>) core.getOps().run(opClass, img, threshType, denominator, doParticleAnalysis);
                    img = core.getOps().convert().float64(binImg);
                    break;
                }
                case "LocalEntropyFilter": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Shannon Entropy...");
                    // todo: local entropy node
                    break;
                }
                case "MedianFilter": {
                    core.getStatusService().showStatus(currentOperation, numOperations, "applying Median Filter...");
                    // todo: median node
                    break;
                }
            }
        }
        img = (Img<DoubleType>) core.getOps().run(Normalize.class, img, 0, 255);
        return (Img<T>) core.getOps().convert().uint8(img);
    }
    
    private class Payload {
        
        private Class<? extends AbstractOp> opClass;
        private List<Object> args;
        
        Payload(Class<? extends AbstractOp> opClass, List<Object> args) {
            this.opClass = opClass;
            this.args = args;
        }
    
        Class<? extends AbstractOp> getOpClass() {
            return opClass;
        }
        
        public void setOpClass(Class<? extends AbstractOp> opClass) {
            this.opClass = opClass;
        }
    
        List<Object> getArgs() {
            return args;
        }
        
        public void setArgs(List<Object> args) {
            this.args = args;
        }
    }
}