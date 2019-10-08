import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Op.class, name = "imsquare")
public class SquareImage<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private LogService log;
    
    @Override
    public void run() {
        @SuppressWarnings("unchecked")
        final Img<T> newImg = inImg.factory().create(inImg);
        outImg = newImg;
    
        Cursor<T> cIn = inImg.cursor();
        Cursor<T> cOut = outImg.cursor();
        
        while (cIn.hasNext()) {
            cIn.fwd();
            cOut.fwd();
            T srcVal = cIn.get();
            cOut.get().set(srcVal);
            cOut.get().mul(srcVal);
        }
    }
}
