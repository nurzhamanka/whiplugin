import fiji.util.gui.GenericDialogPlus;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

import javax.swing.*;
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
    private ImageDisplayService imageDisplayService;

    @Parameter
    private UIService uiService;

    @Parameter
    private OpService ops;
    
    @Parameter
    private LogService log;

//    @Parameter(type = ItemIO.INPUT)
//    private ImageDisplay displayIn;

    // -- Inputs to the command --


//    @Parameter(label = "Input Folder", style = FileWidget.DIRECTORY_STYLE)
//    private File inputDir;


//    @Parameter(label = "Output Folder", style = FileWidget.DIRECTORY_STYLE)
//    private File outputDir;
    
    public void run() {
        String inputFolder = "", outputFolder = "";
        boolean activeImage = false;
        GenericDialogPlus gd = new GenericDialogPlus("Wound Healing");
        gd.addCheckbox("Use current active image?", activeImage);

        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        boolean segmentOut = false;
        boolean binarizationMask = false;
        boolean tiltCorrect = false;
        boolean savePlots = false;
        String threshold = "";
        activeImage = gd.getNextBoolean();
        if (!activeImage){
            gd = new GenericDialogPlus("Wound Healing");
            /** Location on disk of the input images. */
            gd.addDirectoryField("Input directory", inputFolder);
            /** Location on disk to save the processed images. */
            gd.addDirectoryField("Output directory", outputFolder);
            gd.addCheckbox("Segment out?", segmentOut);
            gd.addCheckbox("Binarization mask?", binarizationMask);
            gd.addStringField("Threshold (0...1 float)", threshold);
            gd.addCheckbox("Tilt correct?", tiltCorrect);
            gd.addCheckbox("Save Plots?", savePlots);
            gd.addStringField("DT (0..100%)", threshold);
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
        }

        inputFolder = gd.getNextString();
        outputFolder = gd.getNextString();





        if (activeImage)
        {
            Dataset image2 = imageDisplayService.getActiveDataset();
            if (image2 != null){
                log.info("Processing " + image2.getName());
                final Dataset result = process(image2);
                uiService.show(result);
            } else {
                log.info("No image provided");
                // cancel
            }

        } else {
            try {
                File inputDir = new File(inputFolder);
                File outputDir = new File(outputFolder);
                if (inputDir.isDirectory()) {
                    final File[] inputImages = inputDir.listFiles();
                    if (inputImages == null) {
                        log.error("Input folder is empty");
                    } else {
                        int total = inputImages.length;
                        final List<Long> ts = new ArrayList<>();
                        for (File image : inputImages) {
                            long startTime = System.currentTimeMillis();
                            if (image.getName().contains("DS_Store")) continue;
                            log.info("Processing " + image.getName());
                            // load the image
                            final Dataset currentImage = datasetIOService.open(image.getAbsolutePath());

                            // scale the image
                            final Dataset result = process(currentImage);

                            // save the image
                            datasetIOService.save(result, outputDir.toPath().resolve(image.getName()).toAbsolutePath().toString());
                            long endTime = System.currentTimeMillis();
                            long fd = endTime - startTime;
                            log.info(image.getName() + " saved. It took " + fd / 1000.0 + "s.");
                            ts.add(fd);
                        }
                        double average = ts.stream().mapToDouble(val -> val).average().orElse(0.0);

                        log.info("Average time = " + average / 1000.0 + "s.");
                    }
                }
            } catch (final IOException exc) {
                log.error(exc);
            }
        }
    }
    
    private Dataset process(final Dataset dataset) {
        // NB: We must do a raw cast through Img, because Dataset does not
        // retain the recursive type parameter; it has an ImgPlus<RealType<?>>.
        // This is invalid for routines that need Img<T extends RealType<T>>.
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Img<RealType<?>> result = process((Img) dataset.getImgPlus());
        
        // Finally, coerce the result back to an ImageJ Dataset object.
        return new DefaultDataset(dataset.context(), new ImgPlus<>(result));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> Img<T> process(final Img<T> image) {
        
        // convert to a DoubleType Img
        Img<DoubleType> img = ops.convert().float64(image);
        
        // normalize to [0, 1]
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
    
        // square each pixel
        img = (Img<DoubleType>) ops.run(SquareImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
    
        // difference of Gaussians TODO
        img = (Img<DoubleType>) ops.run(GaussianDifference.class, img, 2.0, 1.0);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
        
        // entropy filter
        img = (Img<DoubleType>) ops.run(HybridFilter.class, img, 5);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
    
        // TopHat morphology filtering
        img = (Img<DoubleType>) ops.run(TophatImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
        
        // average filter
        img = (Img<DoubleType>) ops.run(AverageFilter.class, img, 20);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(1.0));
        
        final Img<BitType> binImg = (Img<BitType>) ops.run(Threshold.class, img);
        img = ops.convert().float64(binImg);
    
        // final normalization for saving (soon to be replaced)
        img = (Img<DoubleType>) ops.run(Normalize.class, img, new DoubleType(0.0), new DoubleType(65535.0));
        final Img<T> fImg = (Img<T>) ops.convert().uint16(img);
        
        return fImg;
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
