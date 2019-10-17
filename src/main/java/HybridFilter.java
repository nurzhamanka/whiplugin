import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "hybrid")
public class HybridFilter extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<DoubleType> inImg;

    @Parameter(type = ItemIO.INPUT)
    private long winSize;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<DoubleType> outImg;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;

    @Override
    public void run() {
        // enforce that the window size is congruent to 1 modulo 4
        if (winSize % 4 != 1)
            log.error(new Exception("Window size must be of the form 4k+1."));
    
        final long[] imageDims = {inImg.dimension(0), inImg.dimension(1)};
        final long[] kernelDims = {winSize, winSize};
        
        final Img<DoubleType>[] avgker = new Img[4];
        
        // "North-West" kernel (a)
        avgker[0] = ops.create().img(kernelDims);
        RandomAccess<DoubleType> nwRa = avgker[0].randomAccess();
        for (long i = 0; i < winSize; i++) {
            final long[] pos = {i, winSize/2};
            nwRa.setPosition(pos);
            nwRa.get().set(1);
        }
        
        // "North-East" kernel (b)
        avgker[1] = (Img<DoubleType>) ops.transform().rotateView(avgker[0], 1, 0);
        
        // "South-East" kernel (c)
        avgker[2] = ops.create().img(kernelDims);
        RandomAccess<DoubleType> seRa = avgker[2].randomAccess();
        for (long i = 0; i < winSize; i++) {
            final long[] pos = {i, i};
            seRa.setPosition(pos);
            seRa.get().set(1);
        }
    
        // "South-East" kernel (d)
        avgker[3] = (Img<DoubleType>) ops.transform().rotateView(avgker[2], 1, 0);
    
        
        final Img<DoubleType>[] avgs = new Img[4];
        for (int i = 0; i < 4; i++) {
            avgs[i] = ImgView.wrap(ops.filter().convolve(inImg, avgker[i]), inImg.factory());
        }
        
    }
}
