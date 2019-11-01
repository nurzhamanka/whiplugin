import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "average")
public class AverageFilter extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<DoubleType> inImg;

    @Parameter(type = ItemIO.INPUT)
    private long size;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<DoubleType> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;

    @Override
    public void run() {
    
        long startTime = System.currentTimeMillis();
        
        final long[] kernelDims = {size, size};
        
        // averaging kernel
        Img<DoubleType> kernel = Helper.getKernel(kernelDims, ops);
        
        outImg =  ImgView.wrap(ops.filter().convolve(inImg, kernel), inImg.factory());
    
        long endTime = System.currentTimeMillis();
        long fd = endTime - startTime;
        log.info("Mean Smoothing: " + fd / 1000.0 + "s.");
    }
    
    private static class Helper {
        static Img<DoubleType> kernel;
        static Img<DoubleType> getKernel(long[] kernelDims, OpService ops) {
            if (kernel != null) return kernel;
            kernel = ops.create().img(kernelDims);
            kernel = (Img<DoubleType>) ops.math().add(kernel, new DoubleType(1.0));
            return kernel;
        }
    }
}