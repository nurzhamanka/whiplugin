import fiji.util.gui.GenericDialogPlus;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import ops.*;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Plugin(type = Command.class, menuPath = "Plugins>Wound Healing Tool")
public class WoundHealing extends DynamicCommand {
    
    private final String PLUGIN_NAME = "Wound Healing Tool 2";
    private final String USER_DIR = System.getProperty("user.home");
    
    /** SERVICES */
    @Parameter
    private DatasetIOService datasetIOService;
    @Parameter
    private ImageDisplayService imageDisplayService;
    @Parameter
    private DisplayService displayService;
    @Parameter
    private OpService ops;
    @Parameter
    private LogService log;
    @Parameter
    private StatusService statusService;
    
    /** PARAMETERS */
    private Dataset activeDataset;
    private File inputDir, outputDir;
    private boolean isSaveBinMask;
    private boolean isSaveOutline;
    private boolean isOverwrite;
    private boolean isDoTiltCorrect;
    private boolean isSavePlots;
    private int threshold;
    
    /**  */
    
    @SuppressWarnings("unchecked")
    public void run() {
    
        log.setLevel(0);
        
        activeDataset = imageDisplayService.getActiveDataset();
        if (activeDataset != null) {
            final GenericDialogPlus dialogActiveDataset = new GenericDialogPlus(PLUGIN_NAME);
            dialogActiveDataset.addMessage("Do you want to process the active dataset?");
            dialogActiveDataset.addHelp("https://github.com/nurzhamanka/whi-plugin");
            dialogActiveDataset.showDialog();
            if (dialogActiveDataset.wasOKed()) {
                populateParams(false);
                if (isCanceled()) return;
                processActiveDataset();
            } else {
                final GenericDialogPlus dialogDirectory = new GenericDialogPlus(PLUGIN_NAME);
                dialogDirectory.addMessage("Do you want to process a directory instead?");
                dialogDirectory.addHelp("https://github.com/nurzhamanka/whi-plugin");
                dialogDirectory.showDialog();
                if (dialogDirectory.wasOKed()) {
                    populateParams(true);
                    if (isCanceled()) return;
                    processDirectory();
                } else {
                    cancel("Plugin canceled by user");
                }
            }
        } else {
            populateParams(true);
            if (isCanceled()) return;
            processDirectory();
        }
    }
    
    
    private void populateParams(final boolean hasDirectory) {
        final GenericDialogPlus dialogParams = new GenericDialogPlus(PLUGIN_NAME);
        dialogParams.addMessage("Welcome to Wound Healing Tool 2. Please specify the parameters.");
        if (hasDirectory) {
            dialogParams.addDirectoryField("Input root path", USER_DIR);
            dialogParams.addDirectoryField("Output root path", USER_DIR);
            dialogParams.addCheckbox("Save binary masks", true);
            dialogParams.addMessage("If checked, overwrites already processed images. \nUncheck if you want to resume an aborted process.");
            dialogParams.addCheckbox("Overwrite processed data", true);
        }
        dialogParams.addHelp("https://github.com/nurzhamanka/whiplugin");
        dialogParams.showDialog();
        if (dialogParams.wasOKed()) {
            // populate parameters
            if (hasDirectory) {
                inputDir = new File(dialogParams.getNextString());
                outputDir = new File(dialogParams.getNextString());
                if (!inputDir.isDirectory() || !outputDir.isDirectory()) {
                    cancel("Input and output paths must be directories");
                    return;
                } else if (inputDir.equals(outputDir)) {
                    cancel("Input and output paths must be different directories");
                    return;
                }
                isSaveBinMask = dialogParams.getNextBoolean();
                isOverwrite = dialogParams.getNextBoolean();
            }
        } else {
            cancel("Plugin canceled by user");
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> void processActiveDataset() {
        
        final ImagePlus activeImagePlus = ImageJFunctions.wrap((Img<T>) activeDataset.getImgPlus().getImg(), "Active dataset");
        final ImagePlus processImage;
        if (activeImagePlus.isStack() || activeImagePlus.isHyperStack()) {
            log.info("Active dataset is a stack...");
            final ImageStack processStack = new ImageStack(activeImagePlus.getWidth(), activeImagePlus.getHeight());
            final ImageStack stack = activeImagePlus.getStack();
            final int stackSize = stack.getSize();
            for (int i = 1; i <= stackSize; i++) {
                statusService.showStatus("Processing the active dataset... (" + i + "/" + stackSize + ")");
                statusService.showProgress(i - 1, stackSize);
                final Img<T> sliceImg = ImageJFunctions.wrapReal(new ImagePlus("Slice " + i, stack.getProcessor(i)));
                final Img<T> sliceImgProcessed = process(sliceImg);
                final ImageProcessor ip = ImageJFunctions.wrap(sliceImgProcessed, "Slice " + i + " processed").getProcessor();
                processStack.addSlice("Slice " + i, ip);
            }
            statusService.showProgress(stackSize, stackSize);
            processImage = new ImagePlus(activeDataset.getName() + " (processed)", processStack);
        } else {
            log.info("Active dataset is a single image...");
            statusService.showStatus("Processing the active dataset...");
            final Dataset processedDataset = process(activeDataset);
            processImage = ImageJFunctions.wrap((Img<T>) processedDataset.getImgPlus().getImg(),
                    activeDataset.getName() + " (processed)");
        }
        statusService.showStatus("Displaying the result...");
        processImage.show();
    }
    
    
    private void processDirectory() {
        
        assert inputDir.isDirectory();
        final File[] sets = inputDir.listFiles(File::isDirectory);
        assert sets != null;
        if (sets.length == 0) {
            cancel("The dataset is empty");
            return;
        }
        int currentSetNumber = 1;
        for (final File set : sets) {
            assert set.isDirectory();
            final File saveDir = new File(outputDir.getAbsolutePath() + File.separator + set.getName());
            log.info("CURRENT DIR: " + saveDir.getAbsolutePath());
            if (!saveDir.mkdir()) {
                log.info("Could not create " + saveDir.getAbsolutePath());
                if (saveDir.exists()) log.info("--- this directory exists");
                else {
                    log.error("--- something went wrong, moving on to the next dataset...");
                    continue;
                }
            }
            statusService.showStatus("Processing:" + set.getName() + " (" + currentSetNumber + "/" + sets.length + ")");
            final File[] inputImages = set.listFiles(pathname -> datasetIOService.canOpen(pathname.getAbsolutePath()));
            assert inputImages != null;
            if (inputImages.length == 0) continue;  // this particular dataset is empty, move on
            final List<Long> times = new ArrayList<>();  // primitive benchmarking
            int currentImageNumber = 0;
            for (final File image : inputImages) {
    
                // check if that image has been processed already
                final File imgFile = new File(saveDir.getAbsolutePath() + File.separator + image.getName());
                if (!isOverwrite && imgFile.exists()) {
                    log.info("Image " + imgFile.getName() + " has been processed - skipping...");
                    continue;
                }
                statusService.showProgress(currentImageNumber, inputImages.length);
                final long startTime = System.currentTimeMillis();
                log.info("Processing " + image.getName());
                final String imgPath = image.getAbsolutePath();
                final Dataset currentImage;
                try {
                    currentImage = datasetIOService.open(imgPath);
                } catch (IOException e) {
                    log.error(e);
                    continue;
                }
    
                // process the image
                final Dataset result = process(currentImage);
    
                if (isSaveBinMask) {
                    // save the binary mask
                    try {
                        datasetIOService.save(result, saveDir.getAbsolutePath() + File.separator + image.getName());
                    } catch (IOException e) {
                        log.error(e);
                        continue;
                    }
                } else {
                    displayService.createDisplay(currentImage.getName() + " (original)", currentImage);
                    displayService.createDisplay(currentImage.getName() + " (processed)", result);
                }
                final long endTime = System.currentTimeMillis();
                final long fd = endTime - startTime;
                log.info(image.getName() + " saved. It took " + fd / 1000.0 + "s.");
                times.add(fd);
                currentImageNumber++;
            }
            double sum = times.stream().mapToDouble(val -> val).sum();
            double average = times.stream().mapToDouble(val -> val).average().orElse(0.0);
            log.info("Dataset processed in " + sum / 1000.0 + "s.");
            log.info("Average time per image is " + average / 1000.0 + "s.");
            currentSetNumber++;
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

//        final ImageStack processStack = new ImageStack((int) image.dimension(0), (int) image.dimension(1));
        
        // convert to a DoubleType Img
        Img<DoubleType> img = ops.convert().float64(image);
        
        // normalize to [0, 1]
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("1. Original Image", makeSlice(img));
        
        // square each pixel
        img = (Img<DoubleType>) ops.run(SquareImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("2. Squared Image", makeSlice(img));
        
        // difference of Gaussians
        img = (Img<DoubleType>) ops.run(GaussianDifference.class, img, 2, 1);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("3. DoG", makeSlice(img));
        
        // hybrid filter
        img = (Img<DoubleType>) ops.run(HybridFilter.class, img, 9);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("4. Hybrid Filter", makeSlice(img));
        
        // TopHat morphology filtering
        img = (Img<DoubleType>) ops.run(TophatImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("5. Top Hat", makeSlice(img));

//        // local entropy filtering
//        img = (Img<DoubleType>) ops.run(ops.LocalEntropyFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(ops.Normalize.class, img, 0.0, 1.0);

//        // median filter for getting rid of white "veins"
//        img = (Img<DoubleType>) ops.run(ops.MedianFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(ops.Normalize.class, img, 0.0, 1.0);
//
//        processStack.addSlice("7. Median Filter", makeSlice(img));
        
        // average filter
        img = (Img<DoubleType>) ops.run(AverageFilter.class, img, 19);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);

//        processStack.addSlice("8. Mean Smoothing", makeSlice(img));
    
        // get the binary mask
        final Img<BitType> binImg = (Img<BitType>) ops.run(Binarize.class, img);
        img = ops.convert().float64(binImg);

//        processStack.addSlice("9. Binary Mask", makeSlice(img));

//        final ImagePlus processImage = new ImagePlus("Process", processStack);
//        processImage.getProcessor().convertToByte(true);
//        final ContrastEnhancer ch = new ContrastEnhancer();
//        for (int i = 1; i <= processImage.getStack().getSize(); i++) {
//            final ImageProcessor ip = processImage.getStack().getProcessor(i);
//            ch.equalize(ip);
//        }
//        processImage.show();
        
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0, 255);
        return (Img<T>) ops.convert().uint8(img);
    }
    
    @SuppressWarnings("unchecked")
    private ImageProcessor makeSlice(Img img) {
        final ImageProcessor ip = ImageJFunctions.wrap(img, "").getProcessor();
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
