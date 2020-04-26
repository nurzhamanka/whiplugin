package ops;

import graphs.model.TopHatOp;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imglib2.algorithm.morphology.BlackTopHat;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.morphology.TopHat;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@Plugin(type = Op.class, name = "tophatimage")
public class TophatImage<T extends RealType<T>> extends AbstractOp {
    
    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;
    
    @Parameter(type = ItemIO.INPUT)
    private TopHatOp.ThType thType;
    
    @Parameter(type = ItemIO.INPUT)
    private TopHatOp.Shape shape;
    
    @Parameter(type = ItemIO.INPUT)
    private int radius;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;
    
    @Parameter
    private LogService log;
    
    @Override
    public void run() {
        
        log.info("Top Hat Transform...");
        final long startTime = System.currentTimeMillis();
    
        final List<Shape> strel = Helper.getStrel(shape, radius);
    
        switch (thType) {
            case WHITE:
                outImg = TopHat.topHat(inImg, strel, 4);
                break;
            case BLACK:
                outImg = BlackTopHat.blackTopHat(inImg, strel, 4);
                break;
        }
        
        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
    private static class Helper {
        private static List<Shape> strel;
    
        static List<Shape> getStrel(TopHatOp.Shape shape, int radius) {
            if (strel != null) return strel;
            switch (shape) {
                case SQUARE:
                    strel = StructuringElements.square(radius, 2);
                    break;
                case DISK:
                    strel = StructuringElements.disk(radius, 2);
                    break;
                case DIAMOND:
                    strel = StructuringElements.diamond(radius, 2);
                    break;
                default:
                    strel = StructuringElements.disk(radius, 2);
                    break;
            }
            return strel;
        }
    }
}
