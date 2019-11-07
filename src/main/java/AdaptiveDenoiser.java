import ij.ImagePlus;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "denoiser")
public class AdaptiveDenoiser<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;
    
    @Parameter(type = ItemIO.INPUT)
    private double radius;

    @Parameter(type = ItemIO.INPUT)
    private int threshold;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private LogService log;
    
    @Parameter
    private OpService ops;
    
    @Override
    public void run() {
        
        log.info("Adaptive Denoiser, radius " + radius + ", threshold " + threshold + "...");
        final long startTime = System.currentTimeMillis();
    
        final ImagePlus imp = ImageJFunctions.wrapFloat(inImg, "Input Image");
        final ImageProcessor ip = imp.getProcessor();
        
        final RankFilters rf = new RankFilters();
        rf.setup("outliers", imp);
        rf.rank(ip, radius, RankFilters.OUTLIERS, RankFilters.BRIGHT_OUTLIERS, threshold);
    
        log.info("--- outlier blobs weeded out");
        
        final Img<UnsignedByteType> img = ImageJFunctions.wrapReal(new ImagePlus("Denoised (Clean)", ip));
        outImg = (Img<T>) ops.convert().float64(img);
    
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
}
