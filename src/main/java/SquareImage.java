import net.imagej.ops.AbstractOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

public class SquareImage<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private RandomAccessibleInterval<T> inputImage;
    
    @Parameter(type = ItemIO.OUTPUT)
    private RandomAccessibleInterval<T> outputImage;
    
    @Override
    public void run() {
    
    }
}
