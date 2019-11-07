import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.algorithm.morphology.Closing;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;


@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "binarize")
public class Binarize extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<DoubleType> inImg;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<BitType> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;
    
    @Override
    public void run() {
        
        log.info("Binarization...");
        final long startTime = System.currentTimeMillis();
        
        log.info("--- applying IsoData threshold");
        
        outImg = (Img<BitType>) ops.threshold().apply(inImg, new DoubleType(ops.stats().mean(inImg).getRealDouble() / Math.PI));
        
        outImg.forEach(BitType::not);
        
//        log.info("--- filling holes...");
//        outImg = (Img<BitType>) ops.morphology().fillHoles(outImg);
        
        final List<Shape> strel1 = StructuringElements.disk(1, 2);
        final List<Shape> strel2 = StructuringElements.disk(2, 2);
        final List<Shape> strel3 = StructuringElements.disk(3, 2);
        

        log.info("--- closing...");
        outImg = Closing.close(outImg, strel1, 4);
        outImg = Dilation.dilate(outImg, strel1, 4);
        outImg = Closing.close(outImg, strel1, 4);
        outImg = Erosion.erode(outImg, strel2, 4);

//        log.info("--- filling holes again...");
//        outImg = (Img<BitType>) ops.morphology().fillHoles(outImg);
        
        /* use a legacy ROI manager to retain the biggest area */
        final RoiManager roiManager = new RoiManager(true);
        final ResultsTable rt = new ResultsTable();
        final ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.ADD_TO_MANAGER,
                Measurements.AREA, rt, 0, Double.POSITIVE_INFINITY);
        //noinspection AccessStaticViaInstance
        particleAnalyzer.setRoiManager(roiManager);
        particleAnalyzer.setHideOutputImage(true);

//        log.info("--- inverting image...");
//        outImg.forEach(BitType::not);
        
        final ImagePlus invImp = ImageJFunctions.wrapBit(outImg, "Binarized (Dirty)");
        final ImageProcessor ip = invImp.getProcessor();
        assert (ip.isBinary());
        
        if (particleAnalyzer.analyze(invImp, ip)) {
            log.info("--- negative particle detection successful");
            // separate the two largest regions (the monolayer) from the rest
            final Roi[] rois = roiManager.getRoisAsArray();
            assert (roiManager.getRoi(0).equals(rois[0]));
            final int[] maxIndices = {-1, -1};
            long currentMax1 = 0;
            long currentMax2 = 0;
            for (int i = 0; i < rois.length; i++) {
                final long probe = rois[i].getStatistics().longPixelCount;
                if (probe > currentMax1) {
                    currentMax2 = currentMax1;
                    maxIndices[1] = maxIndices[0];
                    currentMax1 = probe;
                    maxIndices[0] = i;
                } else if (probe > currentMax2) {
                    currentMax2 = probe;
                    maxIndices[1] = i;
                }
            }
            final int[] roiIndices = new int[rois.length - 2];
            for (int i = 0, j = 0; i < rois.length; i++) {
                if (i != maxIndices[0] && i != maxIndices[1]) {
                    roiIndices[j] = i;
                    j++;
                }
            }
            // whiten the artifacts
            for (int i : roiIndices) {
                ip.setRoi(rois[i]);
                ip.setValue(255);
                ip.fill();
            }
            log.info("--- negative artifacts fixed");
        } else {
            log.error("--- negative particle detection failed");
        }
        
        rt.reset();
        roiManager.reset();
        ip.resetRoi();
        ip.invert();
        if (particleAnalyzer.analyze(invImp, ip)) {
            log.info("--- positive particle detection successful");
            // separate the largest region (the wound) from the rest
            final Roi[] rois = roiManager.getRoisAsArray();
            assert (roiManager.getRoi(0).equals(rois[0]));
            int maxIndex = -1;
            long currentMax = 0;
            for (int i = 0; i < rois.length; i++) {
                final long probe = rois[i].getStatistics().longPixelCount;
                if (probe > currentMax) {
                    currentMax = probe;
                    maxIndex = i;
                }
            }
            // darken the artifacts
            for (int i = 0; i < rois.length; i++) {
                if (i != maxIndex) {
                    ip.setRoi(rois[i]);
                    ip.setValue(255);
                    ip.fill();
                }
            }
            log.info("--- positive artifacts fixed");
        } else {
            log.error("--- positive particle detection failed");
        }
        rt.reset();
        roiManager.reset();
        ip.resetRoi();
        ip.invert();
        final Img<UnsignedByteType> invImg = ImageJFunctions.wrap(new ImagePlus("Binarized (Clean)", ip));
        outImg = (Img<BitType>) ops.threshold().mean(invImg);

//        log.info("--- inverting image...");
//        outImg.forEach(BitType::not);
        
        long endTime = System.currentTimeMillis();
        long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
}
