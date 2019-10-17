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
        List<Shape> disks = StructuringElements.disk(13, 2, 4);
        outImg = TopHat.topHat(inImg, disks, 4);
    }
}
