import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Iterator;

@SuppressWarnings("unchecked")
@Plugin(type = Op.class, name = "imnorm")
public class Normalize<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;
    
    @Parameter(type = ItemIO.INPUT)
    private double min;

    @Parameter(type = ItemIO.INPUT)
    private double max;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private LogService log;
    
    @Override
    public void run() {
        
        log.info("Normalization to [" + min + ", " + max + "]...");
        final long startTime = System.currentTimeMillis();
        
        outImg = inImg.factory().create(inImg);
        
        // compute the global maximum and minimum of the image
        
        final T gmin = inImg.firstElement().createVariable();
        final T gmax = inImg.firstElement().createVariable();
        
        final Iterator<T> iter = inImg.iterator();
        T currentValue = iter.next();
        gmin.set(currentValue);
        gmax.set(currentValue);
        
        while (iter.hasNext()) {
            currentValue = iter.next();
            if (currentValue.compareTo(gmin) < 0)
                gmin.set(currentValue);
            else if (currentValue.compareTo(gmax) > 0)
                gmax.set(currentValue);
        }

        // normalize the image
        // I_N(i,j) = (I(i,j) - gmin) * (max-min)/(gmax-gmin) + min
        
        final T gdiff = gmax.copy();
        gdiff.sub(gmin);
    
        final Cursor<T> cIn = inImg.cursor();
        final Cursor<T> cOut = outImg.cursor();
        
        while (cIn.hasNext()) {
            cIn.fwd();
            cOut.fwd();
            cOut.get().set(cIn.get());
            cOut.get().sub(gmin);
            cOut.get().mul((T) new DoubleType(max - min));
            cOut.get().div(gdiff);
            cOut.get().add((T) new DoubleType(min));
        }
    
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
}
