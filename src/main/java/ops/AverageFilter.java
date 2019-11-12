package ops;

import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.AbstractRealType;
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
    private long kernelSize;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<DoubleType> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;

    @Override
    public void run() {
    
        log.info("Mean Smoothing...");
        final long startTime = System.currentTimeMillis();
        
        final long[] kernelDims = {kernelSize, kernelSize};
        
        // averaging kernel
        final Img<DoubleType> kernel = Helper.getKernel(kernelDims);
        
        outImg =  (Img<DoubleType>) ops.filter().convolve(inImg, kernel);
    
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
    private static class Helper {
        private static final ImgFactory<DoubleType> imgFactory = new ArrayImgFactory<>(new DoubleType());
        private static Img<DoubleType> kernel;
        static Img<DoubleType> getKernel(long[] kernelDims) {
            if (kernel != null) return kernel;
            kernel = imgFactory.create(kernelDims);
            kernel.forEach(AbstractRealType::inc);
            return kernel;
        }
    }
}