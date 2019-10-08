import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Iterator;

@Plugin(type = Op.class, name = "imnorm")
public class Normalize<T extends RealType<T>> extends AbstractOp {
    
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
    
    @Override
    public void run() {
        @SuppressWarnings("unchecked")
        final Img<T> newImg = inImg.factory().create(inImg);
        outImg = newImg;
        
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
        
        log.info("Global minimum is " + gmin + "\nGlobal maximum is " + gmax);
    
        // normalize the image
        // I_N(i,j) = (I(i,j) - gmin) * (max-min)/(gmax-gmin) + min
        
        final T diff = max.copy();
        diff.sub(min);
        
        final T gdiff = gmax.copy();
        gdiff.sub(gmin);
    
        log.info("diff is " + diff + "\nGdiff is " + gdiff);
    
        Cursor<T> cIn = inImg.cursor();
        Cursor<T> cOut = outImg.cursor();
        
        int flag = 0;
        
        while (cIn.hasNext()) {
            cIn.fwd();
            cOut.fwd();
            cOut.get().set(cIn.get());
            if (flag == 100) {
                log.info("pixel was " + cOut.get().toString());
            }
            cOut.get().sub(gmin);
            cOut.get().mul(diff);
            cOut.get().div(gdiff);
            cOut.get().add(min);
            
            if (flag == 100) {
                log.info("pixel is now " + cOut.get().toString());
            }
            flag++;
        }
    }
}
