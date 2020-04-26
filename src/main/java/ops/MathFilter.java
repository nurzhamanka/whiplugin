package ops;

import graphs.model.MathType;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("FieldCanBeLocal")
@Plugin(type = Op.class, name = "imsquare")
public class MathFilter extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<DoubleType> inImg;
    
    @Parameter(type = ItemIO.INPUT)
    private MathType mathType;
    
    @Parameter(type = ItemIO.INPUT)
    private Double n;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<DoubleType> outImg;
    
    @Parameter
    private LogService log;
    
    @Override
    public void run() {
    
        log.info("SquareOp Image...");
        final long startTime = System.currentTimeMillis();
    
        if (mathType == MathType.POW && n == null) {
            throw new IllegalArgumentException("MathFilter requires a power if POW is used");
        }
        
        outImg = inImg.factory().create(inImg);
        
        /* creating outImg from inImg's factory
         * asserts that they have identical
         * cursor traversal ordering */
    
        final Cursor<DoubleType> cIn = inImg.cursor();
        final Cursor<DoubleType> cOut = outImg.cursor();
        
        while (cIn.hasNext()) {
            cIn.fwd();
            cOut.fwd();
            switch (mathType) {
                case SQUARE:
                    cOut.get().set(Math.pow(cIn.get().get(), 2.0));
                    break;
                case SQRT:
                    cOut.get().set(Math.sqrt(cIn.get().get()));
                    break;
                case POW:
                    cOut.get().set(Math.pow(cIn.get().get(), n));
                    break;
            }
        }
        
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
}
