import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Plugin(type = Command.class, menuPath = "Open+Modify+Save Image")
public class OpenModifySaveImage implements Command {
    
    // -- Needed services --
    
    // For opening and saving images.
    @Parameter
    private DatasetIOService datasetIOService;
    
    // For scaling the image.
    @Parameter
    private OpService ops;
    
    // For logging errors.
    @Parameter
    private LogService log;
    
    // -- Inputs to the command --
    
    /** Location on disk of the input image. */
    @Parameter(label = "Input Folder", style = FileWidget.DIRECTORY_STYLE)
    private File inputDir;
    
    /** Factor by which to scale the image. */
    @Parameter(label = "Scale factor")
    private double factor = 2;
    
    /** Location on disk to save the processed image. */
    @Parameter(label = "Output Folder", style = FileWidget.DIRECTORY_STYLE)
    private File outputDir;
    
    public void run() {
        try {

            if (inputDir.isDirectory()) {
                final File[] inputImages = inputDir.listFiles();
                if (inputImages == null) {
                    log.error("Input folder is empty");
                } else {
                    for (File image : inputImages) {
                        if (image.getName().contains("DS_Store")) continue;
                        log.info("Processing " + image.getName());
                        // load the image
                        final Dataset currentImage = datasetIOService.open(image.getAbsolutePath());
    
                        // scale the image
                        final Dataset result = scaleImage(currentImage);
    
                        // save the image
                        datasetIOService.save(result, outputDir.toPath().resolve(image.getName()).toAbsolutePath().toString());
                        log.info(image.getName() + " saved.");
                    }
                }
            }
        }
        catch (final IOException exc) {
            log.error(exc);
        }
    }
    
    private Dataset scaleImage(final Dataset dataset) {
        // NB: We must do a raw cast through Img, because Dataset does not
        // retain the recursive type parameter; it has an ImgPlus<RealType<?>>.
        // This is invalid for routines that need Img<T extends RealType<T>>.
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Img<RealType<?>> result = scaleImage((Img) dataset.getImgPlus());
        
        // Finally, coerce the result back to an ImageJ Dataset object.
        return new DefaultDataset(dataset.context(), new ImgPlus<>(result));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> Img<T> scaleImage(final Img<T> image) {
        final double[] scaleFactors = new double[image.numDimensions()];
        Arrays.fill(scaleFactors, factor);
        
        final InterpolatorFactory<T, RandomAccessible<T>> interpolator = new NearestNeighborInterpolatorFactory<>();
        
        // Perform the transformation using Ops.
        RandomAccessibleInterval<T> rai = //
                ops.transform().scaleView(image, scaleFactors, interpolator);
        
        final Img<DoubleType> dImg = ops.convert().float64(image);
        final Img<DoubleType> normImg = (Img<DoubleType>) ops.run(Normalize.class, dImg, new DoubleType(0.0), new DoubleType(1.0));
        final Img<DoubleType> sqrImg = (Img<DoubleType>) ops.run(SquareImage.class, normImg);
        final Img<DoubleType> reNormImg = (Img<DoubleType>) ops.run(Normalize.class, sqrImg, new DoubleType(0.0), new DoubleType(65535.0));
        final Img<T> fImg = (Img<T>) ops.convert().uint16(reNormImg);
        return fImg;
        //return ImgView.wrap(rai, image.factory());
        
    }
    
    /*
     * This main method is for convenience - so you can run this command
     * directly from Eclipse (or other IDE).
     *
     * It will launch ImageJ and then run this command using the CommandService.
     * This is equivalent to clicking "Tutorials>Open+Scale+Save Image" in the UI.
     */
    public static void main(final String... args) throws Exception {
        // Launch ImageJ as usual.
        final ImageJ ij = new ImageJ();
        ij.launch(args);
        
        // Launch the "OpenScaleSaveImage" command.
        ij.command().run(OpenModifySaveImage.class, true);
    }
}
