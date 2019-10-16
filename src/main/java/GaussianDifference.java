import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import static net.imglib2.algorithm.gauss3.Gauss3.gauss;

@Plugin(type = Op.class, name = "gaussdiff")
public class GaussianDifference<T extends RealType<T>> extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;

    @Parameter(type = ItemIO.INPUT)
    private double sigma;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;

    // For scaling the image.
    @Parameter
    private OpService ops;

    @Override
    public void run() {
        @SuppressWarnings("unchecked")
        final Img<T> newImg = inImg.factory().create(inImg);
        outImg = newImg;
//
//        gauss(sigma, inImg, outImg);
        // We can also blur the channels.
//        double[] channelSigmas = sigma, sigma, 0
        final RandomAccessibleInterval<T> res = ops.filter().gauss(inImg, sigma);
        outImg = ImgView.wrap(res, inImg.factory());
    }
}
