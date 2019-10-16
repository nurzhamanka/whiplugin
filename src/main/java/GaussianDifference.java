import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.img.Img;
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

    @Override
    public void run() {
        @SuppressWarnings("unchecked")
        final Img<T> newImg = inImg.factory().create(inImg);
        outImg = newImg;

        gauss(sigma, inImg, outImg);
    }
}
