package ops;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
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
        
        log.info("--- applying threshold");
        
        outImg = (Img<BitType>) ops.threshold().apply(inImg, new DoubleType(ops.stats().mean(inImg).getRealDouble() / Math.PI));
    
        log.info("--- inverting image...");
        outImg.forEach(p -> p.set(!p.get()));
    
        // the wound must be white here
        log.info("--- filling holes...");
        outImg = (Img<BitType>) ops.morphology().fillHoles(outImg);
        // after this, the wound region is mostly white

        final List<Shape> strel1 = StructuringElements.disk(1, 2);
        final List<Shape> strel2 = StructuringElements.disk(2, 2);
        final List<Shape> strel3 = StructuringElements.disk(3, 2);
        
        log.info("--- closing...");
        outImg = Closing.close(outImg, strel1, 4);
        outImg = Dilation.dilate(outImg, strel1, 4);
        outImg = Closing.close(outImg, strel1, 4);
        outImg = Erosion.erode(outImg, strel2, 4);
    
        log.info("--- inverting image...");
        outImg.forEach(p -> p.set(!p.get()));
    
        /* use a legacy ROI manager to clean black artifacts */
        final RoiManager roiManager = new RoiManager(true);
        final ResultsTable rt = new ResultsTable();
        final ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                ParticleAnalyzer.ADD_TO_MANAGER,
                Measurements.AREA, rt, 0, Double.POSITIVE_INFINITY);
        //noinspection AccessStaticViaInstance
        particleAnalyzer.setRoiManager(roiManager);
        particleAnalyzer.setHideOutputImage(true);

        final ImagePlus invImp = ImageJFunctions.wrapBit(outImg, "Binarized (Dirty)");
        final ByteProcessor ip = invImp.getProcessor().convertToByteProcessor();
        assert (ip.isBinary());
        ip.snapshot();

        if (particleAnalyzer.analyze(invImp, ip)) {
            log.info("--- wound particle detection successful");
            // separate the two largest regions (the monolayer) from the wound
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
                ip.setValue(0);
                ip.fill();
                ip.reset(ip.getMask());
            }
            log.info("--- wound artifacts fixed");
        } else {
            log.error("--- wound particle detection failed");
        }
        
        // now the wound should be completely white
        ip.resetRoi();
        ip.invert();
        roiManager.reset();
        rt.reset();
    
        final ParticleAnalyzer particleAnalyzer2 = new ParticleAnalyzer(
                ParticleAnalyzer.ADD_TO_MANAGER,
                Measurements.AREA, rt, 1000, Double.POSITIVE_INFINITY);
        //noinspection AccessStaticViaInstance
        particleAnalyzer2.setRoiManager(roiManager);
        particleAnalyzer2.setHideOutputImage(true);
        final ImagePlus imp = new ImagePlus("Fixed Wound, white monolayer", ip);
        ImageProcessor ip2 = imp.getProcessor().convertToByteProcessor();
        assert (ip2.isBinary());
    
        if (particleAnalyzer2.analyze(imp, ip2)) {
            log.info("--- wound detection successful");
            // find the largest ROI (the wound) and invert the selection
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
            final Roi antiWound = roiManager.getRoi(maxIndex).getInverse(imp);
            assert (antiWound != null);
            ip2.setRoi(antiWound);
            ip2.setValue(0);
            ip2.fill();
            ip2.reset(ip2.getMask());
            ip2.resetRoi();
//            ip2 = antiWound.getMask();
//            ip2.invert();
            log.info("--- wound extracted");
        } else {
            log.error("--- wound detection failed");
        }
    
        roiManager.reset();
        rt.reset();
        
        final Img<UnsignedByteType> invImg = ImageJFunctions.wrap(new ImagePlus("Binarized (Clean)", ip2));
    
        outImg = ops.convert().bit(invImg);
        outImg = Closing.close(outImg, strel3, 4);
        
        long endTime = System.currentTimeMillis();
        long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
}
