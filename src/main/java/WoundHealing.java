import fiji.util.gui.GenericDialogPlus;
import graphs.editor.GraphEditor;
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
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ALL", "FieldCanBeLocal"})
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
    private OpService ops;
    @Parameter
    private LogService log;
    
    /** PARAMETERS */
    private GraphEditor gEditor;
    private Dataset activeDataset;
    private File inputDir, outputDir;
    private boolean isSaveBinMask;
    private boolean isSaveOutline;
    private boolean isDoTiltCorrect;
    private boolean isSavePlots;
    private int threshold;
    
    @SuppressWarnings("unchecked")
    public void run() {

//        try {
//            SwingUtilities.invokeAndWait(() -> {
//
//                try {
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//
//                mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
//                mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";
//
//                if (gEditor == null) {
//                    gEditor = new BasicGraphEditor();
//                }
//                gEditor.createFrame(new MenuBar(gEditor)).setVisible(true);
//            });
//        } catch (InterruptedException e) {
//            cancel("Algorithm Builder was interrupted");
//        } catch (InvocationTargetException e) {
//            log.error(e);
//            cancel(e.getMessage());
//        }
        
        activeDataset = imageDisplayService.getActiveDataset();
        if (activeDataset != null) {
            final GenericDialogPlus dialogActiveDataset = new GenericDialogPlus(PLUGIN_NAME);
            dialogActiveDataset.addMessage("Do you want to process the active dataset?");
            dialogActiveDataset.addHelp("https://github.com/nurzhamanka/whiplugin");
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
        }
        dialogParams.addCheckbox("Save binary masks", true);
        dialogParams.addCheckbox("Save outline", false);
        dialogParams.addSlider("Threshold", 0, 255, 100, 1);
        dialogParams.addCheckbox("Tilt correction", false);
        dialogParams.addCheckbox("Save plots", false);
        dialogParams.addHelp("https://github.com/nurzhamanka/whi-plugin");
        dialogParams.showDialog();
        if (dialogParams.wasOKed()) {
            // populate parameters
            if (hasDirectory) {
                inputDir = new File(dialogParams.getNextString());
                outputDir = new File(dialogParams.getNextString());
                if (!inputDir.isDirectory() || !outputDir.isDirectory()) {
                    cancel("Input and output paths must be directories");
                    return;
                }
            }
            isSaveBinMask = dialogParams.getNextBoolean();
            isSaveOutline = dialogParams.getNextBoolean();
            threshold = (int) dialogParams.getNextNumber();
            isDoTiltCorrect = dialogParams.getNextBoolean();
            isSavePlots = dialogParams.getNextBoolean();
        } else {
            cancel("Plugin canceled by user");
        }
    }
    
    private void processActiveDataset() {
        // TODO: process the active dataset, produce a hyperstack
        cancel("To be implemented...");
    }
    
    private void processDirectory() {
        
        assert inputDir.isDirectory();
        final File[] sets = inputDir.listFiles(File::isDirectory);
        assert sets != null;
        if (sets.length == 0) {
            cancel("The dataset is empty");
            return;
        }
        // TODO: save dataset in the same directory structure
        for (final File set : sets) {
            assert set.isDirectory();
            final File[] inputImages = set.listFiles(f -> datasetIOService.canOpen(f.getAbsolutePath()));
            assert inputImages != null;
            if (inputImages.length == 0) continue;  // this particular dataset is empty, move on
            final List<Long> times = new ArrayList<>();  // primitive benchmarking
            for (final File image : inputImages) {
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
                
                // save the image
                try {
                    datasetIOService.save(result, outputDir.toPath().resolve(image.getName()).toAbsolutePath().toString());
                } catch (IOException e) {
                    log.error(e);
                    continue;
                }
                final long endTime = System.currentTimeMillis();
                final long fd = endTime - startTime;
                log.info(image.getName() + " saved. It took " + fd / 1000.0 + "s.");
                times.add(fd);
                return;
            }
            double sum = times.stream().mapToDouble(val -> val).sum();
            double average = times.stream().mapToDouble(val -> val).average().orElse(0.0);
            log.info("Dataset processed in " + sum / 1000.0 + "s.");
            log.info("Average time per image is " + average / 1000.0 + "s.");
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
        
        // TODO: move all the stack forming into ProcessActiveDataset
        
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
    public static void main(final String... args) {
        
        final ImageJ ij = new ImageJ();
        ij.launch(args);
        ij.command().run(WoundHealing.class, true);
    }
}
