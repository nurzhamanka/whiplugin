import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "gaussdiff")
public class GaussianDifference<T extends RealType<T>> extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;

    @Parameter(type = ItemIO.INPUT)
    private double sigma1;

    @Parameter(type = ItemIO.INPUT)
    private double sigma2;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;

    @Override
    public void run() {
    
        log.info("DoG, sigma1 = " + sigma1 + ", sigma2 = " + sigma2 + "...");
        final long startTime = System.currentTimeMillis();
        
        final double[] sigmas1 = new double[]{sigma1, sigma1};
        final double[] sigmas2 = new double[]{sigma2, sigma2};
    
        final Img<T> in1 = inImg.copy();
        final RandomAccessible<T> inf1 = Views.extendValue(in1, (T) new DoubleType());
        Gauss3.gauss(sigmas1, inf1, in1);
        
        final Img<T> in2 = inImg.copy();
        final RandomAccessible<T> inf2 = Views.extendValue(in2, (T) new DoubleType());
        Gauss3.gauss(sigmas2, inf2, in2);
    
        outImg = (Img<T>) ops.math().subtract(in1, (RandomAccessibleInterval) in2);
    
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
}
