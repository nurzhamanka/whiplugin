import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.algorithm.morphology.Closing;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
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
    
        log.info("--- computing the threshold");
        
        // final DoubleType threshold = ops.stats().mean(inImg);
        // final DoubleType imgStdev = ops.stats().stdDev(inImg);
        // final DoubleType imgMedian = ops.stats().median(inImg);
        // threshold.div(new DoubleType(Math.PI)); // empirically optimal threshold for ORCA
    
        log.info("--- applying IsoData threshold");
        outImg = (Img<BitType>) ops.threshold().isoData(inImg);
    
        log.info("--- filling holes...");
        outImg = (Img<BitType>) ops.morphology().fillHoles(outImg);
        
        List<Shape> disk1 = StructuringElements.disk(1, 2);
        List<Shape> disk3 = StructuringElements.disk(3, 2);
        List<Shape> rectangle = StructuringElements.rectangle(new int[]{2,2});
    
        log.info("--- closing...");
        outImg = Closing.close(outImg, rectangle, 4);
        log.info("--- dilating...");
        outImg = Dilation.dilate(outImg, disk1, 4);
        log.info("--- closing...");
        outImg = Closing.close(outImg, disk1, 4);
        log.info("--- eroding...");
        outImg = Erosion.erode(outImg, disk3, 4);
    
        long endTime = System.currentTimeMillis();
        long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
}
