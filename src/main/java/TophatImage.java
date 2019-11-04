import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
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

@Plugin(type = Op.class, name = "tophatimage")
public class TophatImage<T extends RealType<T>> extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;

    @Parameter
    private LogService log;

    @Override
    public void run() {
        
        log.info("Top Hat Transform...");
        final long startTime = System.currentTimeMillis();
        
        final List<Shape> disk = Helper.getDisk(3);
        outImg = TopHat.topHat(inImg, StructuringElements.square(13, 2), 4);

        final long endTime = System.currentTimeMillis();
        final long fd = endTime - startTime;
        log.info("--- time: " + fd / 1000.0 + "s.");
    }
    
    private static class Helper {
        private static List<Shape> disk;
        static List<Shape> getDisk(long radius) {
            if (disk != null) return disk;
            disk = StructuringElements.disk(radius, 2);
            return disk;
        }
    }
}
