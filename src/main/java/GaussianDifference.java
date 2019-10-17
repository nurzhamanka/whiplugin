import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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

    @Override
    public void run() {
        //noinspection unchecked
        outImg = (Img<T>) ops.run("dog", inImg, sigma1, sigma2);
    }
}
