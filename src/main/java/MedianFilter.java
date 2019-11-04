import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.util.Arrays;


@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "median")
public class MedianFilter extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<DoubleType> inImg;

    @Parameter(type = ItemIO.INPUT)
    private int kernelSize;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<DoubleType> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;
    
    @Parameter
    private UIService ui;

    @Override
    public void run() {
    
        log.info("Median Filter, kernel size [" + kernelSize + "x" + kernelSize + "]...");
        final long startTime = System.currentTimeMillis();
        
        // enforce that the window size is congruent to 1 modulo 4
        if (kernelSize % 2 != 1)
            log.error(new Exception("Kernel size must be of the form 2k+1."));
        
        outImg = inImg.factory().create(inImg);
        
        // kernel is a square, but can be changed to whatever
        final RectangleShape shape = new RectangleShape((kernelSize - 1)/2, false);
        
        // pad the image
        final RandomAccessibleInterval paddedImg = Views.interval(new ExtendedRandomAccessibleInterval<>(inImg, new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE)), inImg);
        
        final RandomAccess<DoubleType> inRa = paddedImg.randomAccess();
        final RandomAccess<DoubleType> outRa = outImg.randomAccess();
        final Iterable<Neighborhood<DoubleType>> neighborhoods = shape.neighborhoods(paddedImg);
        
        for (final Neighborhood<DoubleType> neighborhood : neighborhoods) {
            
            inRa.setPosition(neighborhood);
            outRa.setPosition(inRa);
            double[] vals = new double[kernelSize * kernelSize];
            int i = 0;
            for (final DoubleType value : neighborhood) {
                vals[i] = value.get();
                i++;
            }
            Arrays.sort(vals);
            outRa.get().set(vals[(kernelSize * kernelSize - 1) / 2]);
        }
    
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
}
