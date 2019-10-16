import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Op.class, name = "imnorm")
public class NormalizeOp<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;
    
    @Parameter(type = ItemIO.INPUT)
    private T min;

    @Parameter(type = ItemIO.INPUT)
    private T max;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private LogService log;
    
    @Parameter
    private OpService ops;
    
    @Override
    public void run() {
//
//        // compute the global maximum and minimum of the image
//
//        final T gmin = inImg.firstElement().createVariable();
//        final T gmax = inImg.firstElement().createVariable();
//
//        final Iterator<T> iter = inImg.iterator();
//        T currentValue = iter.next();
//        gmin.set(currentValue);
//        gmax.set(currentValue);
//
//        final IterableInterval<T> ii = ops.image().normalize(inImg, gmin, gmax, min, max);
//        outImg = (Img<T>) ii;
//        log.info("Normalize success.");
    }
}
