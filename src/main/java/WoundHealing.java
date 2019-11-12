
import fiji.util.gui.GenericDialogPlus;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplayService;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;
import io.scif.config.SCIFIOConfig;
import io.scif.services.DatasetIOService;
import net.imagej.*;

import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.Command;
import org.scijava.display.DisplayService;
import org.scijava.log.LogLevel;
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
    
    
    @SuppressWarnings("unchecked")
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
        int threshold = 0;
        int dt = 0;
        activeImage = gd.getNextBoolean();
        if (!activeImage){
            gd = new GenericDialogPlus("Wound Healing");
            /** Location on disk of the input images. */
            gd.addDirectoryField("Input directory", inputFolder);
            /** Location on disk to save the processed images. */
            gd.addDirectoryField("Output directory", outputFolder);
            gd.addCheckbox("Segment out?", segmentOut);
            gd.addCheckbox("Binarization mask?", binarizationMask);
            gd.addNumericField("Threshold (0...1 float)", 0, threshold);
            gd.addCheckbox("Tilt correct?", tiltCorrect);
            gd.addCheckbox("Save Plots?", savePlots);
            gd.addNumericField("DT (0..100%)", 0, dt);
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
              
            }
            inputFolder = gd.getNextString();
            outputFolder = gd.getNextString();
            segmentOut = gd.getNextBoolean();
            binarizationMask = gd.getNextBoolean();
            threshold = (int)gd.getNextNumber();
            tiltCorrect = gd.getNextBoolean();
            savePlots = gd.getNextBoolean();
            dt = (int)gd.getNextNumber();


        }


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
    
    @SuppressWarnings("unchecked")
    private Dataset process(final Dataset dataset) {
        
        final Img<RealType<?>> result = process((Img) dataset.getImgPlus().getImg());
        
        // Finally, coerce the result back to an ImageJ Dataset object.
        return new DefaultDataset(dataset.context(), new ImgPlus<>(result));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> Img<T> process(final Img<T> image) {
    
        final ImageStack processStack = new ImageStack((int) image.dimension(0), (int) image.dimension(1));
        
        // convert to a DoubleType Img
        Img<DoubleType> img = ops.convert().float64(image);
        
        // normalize to [0, 1]
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        
        processStack.addSlice("1. Original Image", makeSlice(img));
    
        // square each pixel
        img = (Img<DoubleType>) ops.run(SquareImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        processStack.addSlice("2. Squared Image", makeSlice(img));

        // difference of Gaussians
        img = (Img<DoubleType>) ops.run(GaussianDifference.class, img, 2, 1);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        processStack.addSlice("3. DoG", makeSlice(img));

        // hybrid filter
        img = (Img<DoubleType>) ops.run(HybridFilter.class, img, 9);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        processStack.addSlice("4. Hybrid Filter", makeSlice(img));

        // TopHat morphology filtering
        img = (Img<DoubleType>) ops.run(TophatImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        processStack.addSlice("5. Top Hat", makeSlice(img));
    
//        // local entropy filtering
//        img = (Img<DoubleType>) ops.run(LocalEntropyFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        // median filter for getting rid of white "veins"
//        img = (Img<DoubleType>) ops.run(MedianFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
//
//        processStack.addSlice("7. Median Filter", makeSlice(img));

        // average filter
        img = (Img<DoubleType>) ops.run(AverageFilter.class, img, 9);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
    
        processStack.addSlice("8. Mean Smoothing", makeSlice(img));

        // get the binary mask
        final Img<BitType> binImg = (Img<BitType>) ops.run(Binarize.class, img);
        img = ops.convert().float64(binImg);
    
        processStack.addSlice("9. Binary Mask", makeSlice(img));
    
        final ImagePlus processImage = new ImagePlus("Process", processStack);
        processImage.getProcessor().convertToByte(true);
        final ContrastEnhancer ch = new ContrastEnhancer();
        for (int i = 1; i <= processImage.getStack().getSize(); i++) {
            final ImageProcessor ip = processImage.getStack().getProcessor(i);
            ch.equalize(ip);
        }
        processImage.show();

        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0, 255);
        return (Img<T>) ops.convert().uint8(img);
    }
    
    @SuppressWarnings("unchecked")
    private ImageProcessor makeSlice(Img img) {
        final ImageProcessor ip = ImageJFunctions.wrapBit(img, "").getProcessor();
        ip.convertToByte(true);
        return ip;
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
