import fiji.util.gui.GenericDialogPlus;
import graphs.AlgorithmFlowEditor;
import graphs.ImageJCore;
import graphs.editor.MenuBar;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Plugin(type = Command.class, menuPath = "Plugins>Wound Healing Tool")
public class WoundHealing extends DynamicCommand {
    
    private final String PLUGIN_NAME = "Wound Healing Tool 2";
    private final String USER_DIR = System.getProperty("user.home");
    private final String GITHUB_URL = "https://github.com/nurzhamanka/whiplugin";
    
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
    private boolean isOverwrite;
    private boolean isShowProcess;
    
    /**
     * GRAPH STUFF
     */
    private AlgorithmFlowEditor graphEditor;
    
    @SuppressWarnings("unchecked")
    public void run() {
        
        activeDataset = imageDisplayService.getActiveDataset();
    
        // prevent concurrency issues
        AtomicReference<Boolean> isGuiSelected = new AtomicReference<>(false);
        
        if (activeDataset != null) {
            final GenericDialogPlus dialogActiveDataset = new GenericDialogPlus(PLUGIN_NAME);
            dialogActiveDataset.addMessage("Choose a process");
            dialogActiveDataset.addButton("Launch GUI", e -> {
                isGuiSelected.set(true);
                dialogActiveDataset.dispose();
            });
            dialogActiveDataset.enableYesNoCancel("Process active dataset", "Process a directory");
            dialogActiveDataset.addHelp(GITHUB_URL);
            dialogActiveDataset.showDialog();
            if (dialogActiveDataset.wasOKed()) {
                populateParams(false);
                if (isCanceled()) return;
                processActiveDataset();
            } else if (dialogActiveDataset.wasCanceled()) {
                cancel();
            } else if (!isGuiSelected.get()) {
                populateParams(true);
                if (isCanceled()) return;
                processDirectory();
            } else {
                launchGUI();
            }
        } else {
            final GenericDialogPlus dialogActiveDataset = new GenericDialogPlus(PLUGIN_NAME);
            dialogActiveDataset.addMessage("Choose a process");
            dialogActiveDataset.enableYesNoCancel("Launch GUI", "Process a directory");
            dialogActiveDataset.addHelp(GITHUB_URL);
            dialogActiveDataset.showDialog();
            if (dialogActiveDataset.wasOKed()) {
                launchGUI();
            } else if (dialogActiveDataset.wasCanceled()) {
                cancel();
            } else {
                populateParams(true);
                if (isCanceled()) return;
                processDirectory();
            }
        }
    }
    
    
    private void launchGUI() {
        log.info("GUI launched!");
        SwingUtilities.invokeLater(() -> {
            if (graphEditor == null) {
                ImageJCore core = new ImageJCore();
                core.setDatasetIOService(datasetIOService);
                core.setDisplayService(displayService);
                core.setImageDisplayService(imageDisplayService);
                core.setLog(log);
                core.setOps(ops);
                core.setStatusService(statusService);
                graphEditor.createFrame(new MenuBar(graphEditor)).setVisible(true);
                graphEditor = new AlgorithmFlowEditor(core);
            }
        });
    }
    
    
    private void populateParams(final boolean hasDirectory) {
        final GenericDialogPlus dialogParams = new GenericDialogPlus(PLUGIN_NAME);
        dialogParams.addMessage("Welcome to Wound Healing Tool 2. Please specify the parameters.");
        if (hasDirectory) {
            dialogParams.addDirectoryField("Input root path", USER_DIR);
            dialogParams.addDirectoryField("Output root path", USER_DIR);
            dialogParams.addCheckbox("Save binary masks", true);
            dialogParams.addMessage("If checked, already processed images will be overwritten. \nUncheck if you want to resume an aborted process.");
            dialogParams.addCheckbox("Overwrite processed data", true);
        }
        dialogParams.addMessage("If checked, a separate stack showing the pipeline's steps will be produced.");
        dialogParams.addCheckbox("Produce a process stack?", true);
        dialogParams.addHelp(GITHUB_URL);
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
            isShowProcess = dialogParams.getNextBoolean();
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
                if ((currentImageNumber + 1) % 3 == 0) {
                    currentImageNumber++;
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
    
        ImageStack processStack = null;
    
        if (isShowProcess) processStack = new ImageStack((int) image.dimension(0), (int) image.dimension(1));
        
        // convert to a DoubleType Img
        statusService.showStatus(0, 8, "Normalizing image...");
        Img<DoubleType> img = ops.convert().float64(image);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("1. Original Image", makeSlice(img));
        
        // square each pixel
        statusService.showStatus(1, 8, "Stretching histogram...");
        img = (Img<DoubleType>) ops.run(SquareImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("2. Squared Image", makeSlice(img));
        
        // difference of Gaussians
        statusService.showStatus(2, 8, "Bandpass Filtering...");
        img = (Img<DoubleType>) ops.run(GaussianDifference.class, img, 2, 1);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("3. DoG", makeSlice(img));
        
        // hybrid filter
        statusService.showStatus(3, 8, "Gradient Denoising...");
        img = (Img<DoubleType>) ops.run(HybridFilter.class, img, 9);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("4. Hybrid Filter", makeSlice(img));
        
        // TopHat morphology filtering
        statusService.showStatus(4, 8, "Morphological Filtering...");
        img = (Img<DoubleType>) ops.run(TophatImage.class, img);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("5. Top Hat", makeSlice(img));

//        // local entropy filtering
//        img = (Img<DoubleType>) ops.run(ops.LocalEntropyFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(ops.Normalize.class, img, 0.0, 1.0);

//        // median filter for getting rid of white "veins"
//        img = (Img<DoubleType>) ops.run(ops.MedianFilter.class, img, 3);
//        img = (Img<DoubleType>) ops.run(ops.Normalize.class, img, 0.0, 1.0);
//
//        processStack.addSlice("7. Median Filter", makeSlice(img));
        
        // average filter
        statusService.showStatus(5, 8, "Smoothing image...");
        img = (Img<DoubleType>) ops.run(AverageFilter.class, img, 19);
        img = (Img<DoubleType>) ops.run(Normalize.class, img, 0.0, 1.0);
        if (processStack != null) processStack.addSlice("6. Mean Smoothing", makeSlice(img));
    
        // get the binary mask
        statusService.showStatus(6, 8, "Binarizing image...");
        final Img<BitType> binImg = (Img<BitType>) ops.run(Binarize.class, img);
        img = ops.convert().float64(binImg);
        if (processStack != null) processStack.addSlice("7. Binary Mask", makeSlice(img));
    
        if (processStack != null) {
            statusService.showStatus(7, 8, "Building the process stack...");
            final ImagePlus processImage = new ImagePlus("Process", processStack);
            final ContrastEnhancer ch = new ContrastEnhancer();
            for (int i = 1; i <= processImage.getStack().getSize(); i++) {
                final ImageProcessor ip = processImage.getStack().getProcessor(i).convertToByteProcessor();
                ch.equalize(ip);
            }
            processImage.show();
        }
    
        statusService.showStatus(8, 8, "Finishing up...");
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
