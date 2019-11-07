import io.scif.config.SCIFIOConfig;
import io.scif.services.DatasetIOService;
import net.imagej.*;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.Command;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Plugin(type = Command.class, menuPath = "Wound Healing")
public class WoundHealing implements Command {
    
    // -- Needed services --
    
    @Parameter
    private DatasetIOService datasetIOService;
    
    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;
    
    @Parameter
    private ImageDisplayService imgDisplayService;
    
    @Parameter
    private DatasetService datasetService;
    
    // -- Inputs to the command --
    
    /** Location on disk of the input images. */
    @Parameter(label = "Input Folder", style = FileWidget.DIRECTORY_STYLE)
    private File inputDir;
    
    /** Location on disk to save the processed images. */
    @Parameter(label = "Output Folder", style = FileWidget.DIRECTORY_STYLE)
    private File outputDir;
    
    
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            log.setLevel(LogLevel.INFO);
            if (inputDir.isDirectory()) {
                final File[] inputImages = inputDir.listFiles();
                if (inputImages == null) {
                    log.error("Input folder is empty");
                } else {
                    final List<Long> ts = new ArrayList<>();
                    final SCIFIOConfig config = new SCIFIOConfig();
                    config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.ARRAY);
                    for (File image : inputImages) {
                        final long startTime = System.currentTimeMillis();
                        if (image.getName().contains("DS_Store")) continue;
                        log.info("Processing " + image.getName());
                        final Dataset currentImage = datasetIOService.open(image.getAbsolutePath(), config);
                        final Dataset result = process(currentImage);
                        datasetIOService.save(result, outputDir.toPath().resolve(image.getName()).toAbsolutePath().toString());
                        final long endTime = System.currentTimeMillis();
                        final long fd = endTime - startTime;
                        log.info(image.getName() + " saved. It took " + fd / 1000.0 + "s.");
                        ts.add(fd);
//                        break;
                    }
                    final double average = ts.stream().mapToDouble(val -> val).average().orElse(0.0);

                    log.info( "Average time = " + average/1000.0 + "s.");
                    System.out.println("FINISHED.");
                }
            }
        }
        catch (final IOException exc) {
            log.error(exc);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Dataset process(final Dataset dataset) {
        
        final Img<RealType<?>> result = process((Img) dataset.getImgPlus().getImg());
        
        // Finally, coerce the result back to an ImageJ Dataset object.
        return new DefaultDataset(dataset.context(), new ImgPlus<>(result));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> Img<T> process(final Img<T> image) {
        
        // convert to a DoubleType Img
        Img<DoubleType> img = ops.convert().float64(image);
        
        // normalize to [0, 1]
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        // square each pixel
        img = (Img<DoubleType>) ops.run(SquareImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // difference of Gaussians
        img = (Img<DoubleType>) ops.run(GaussianDifference.class, img, 2, 1);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // hybrid filter
        img = (Img<DoubleType>) ops.run(HybridFilter.class, img, 9);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // TopHat morphology filtering
        img = (Img<DoubleType>) ops.run(TophatImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
//        // local entropy filtering
//        img = (Img<DoubleType>) ops.run(LocalEntropyFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // median filter for getting rid of white "veins"
        img = (Img<DoubleType>) ops.run(MedianFilter.class, img, 3);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // average filter
        img = (Img<DoubleType>) ops.run(AverageFilter.class, img, 3);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

        // get the binary mask
        final Img<BitType> binImg = (Img<BitType>) ops.run(Binarize.class, img);
        img = ops.convert().float64(binImg);

        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0, 255);
        return (Img<T>) ops.convert().uint8(img);
    }
    
    /*
     * This main method is for convenience - so you can run this command
     * directly from Eclipse (or other IDE).
     *
     * It will launch ImageJ and then run this command using the CommandService.
     */
    public static void main(final String... args) throws Exception {
        
        final ImageJ ij = new ImageJ();
        ij.launch(args);
        ij.command().run(WoundHealing.class, true);
    }
}
