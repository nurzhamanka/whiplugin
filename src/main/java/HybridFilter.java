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
        
        outImg = inImg.factory().create(inImg);
    
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
        avgker[1] = ImgView.wrap(ops.transform().rotateView(avgker[0], 1, 0), avgker[0].factory());
        
        // "South-East" kernel (c)
        avgker[2] = ops.create().img(kernelDims);
        RandomAccess<DoubleType> seRa = avgker[2].randomAccess();
        for (long i = 0; i < winSize; i++) {
            final long[] pos = {i, i};
            seRa.setPosition(pos);
            seRa.get().set(1);
        }
    
        // "South-East" kernel (d)
        avgker[3] = ImgView.wrap(ops.transform().rotateView(avgker[0], 1, 0), avgker[2].factory());
    
        // convolution of the input image with the 4 kernels
        final Img<DoubleType>[] avgs = new Img[4];
        final RandomAccess<DoubleType>[] ras = new RandomAccess[4];
        for (int i = 0; i < 4; i++) {
            avgs[i] = ImgView.wrap(ops.filter().convolve(inImg, avgker[i]), inImg.factory());
            ras[i] = avgs[i].randomAccess();
        }
        
        // construction of the final image via minimization of each pixel over the 4 convolutions
        final RandomAccess<DoubleType> outRa = outImg.randomAccess();
        for (long i = 0; i < imageDims[0]; i++) {
            for (long j = 0; j < imageDims[1]; j++) {
                final long[] pos = {i, j};
                for (RandomAccess ra : ras) {
                    ra.setPosition(pos);
                }
                outRa.setPosition(pos);
                outRa.get().set(Math.min(Math.min(ras[0].get().get(), ras[1].get().get()),
                        Math.min(ras[2].get().get(), ras[3].get().get())));
            }
        }
    }
}
