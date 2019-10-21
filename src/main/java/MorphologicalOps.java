import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
//import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.morphology.*;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@Plugin(type = Op.class, name = "morph")
public class MorphologicalOps<T extends RealType<T>> extends AbstractOp {

    @Parameter(type = ItemIO.INPUT)
    private Img<T> inImg;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<T> outImg;

    @Parameter
    private LogService log;

    @Override
    public void run() {
        List<Shape> disks1 = StructuringElements.disk(1, 2, 4);
        List<Shape> disks3 = StructuringElements.disk(3, 2, 4);
        List<Shape> rectangles = StructuringElements.rectangle(new int[]{2,2});

        inImg = Closing.close(inImg, rectangles,4);
        inImg = Dilation.dilate(inImg, disks1, 4);
        inImg = Closing.close(inImg, disks1, 4);
        outImg = Erosion.erode(inImg, disks3, 4);
    }
}
