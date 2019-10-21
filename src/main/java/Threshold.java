import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "thresh")
public class Threshold extends AbstractOp {

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
        // compute the threshold
        final DoubleType threshold = ops.stats().mean(inImg);
//        final DoubleType imgStdev = ops.stats().stdDev(inImg);
//        final DoubleType imgMedian = ops.stats().median(inImg);
        threshold.div(new DoubleType(Math.PI));
        
        final Img<BitType> preOutImg = (Img<BitType>) ops.threshold().apply(inImg, threshold);
        outImg = preOutImg.factory().create(preOutImg);
        ops.image().invert(outImg, preOutImg);
    }
}
