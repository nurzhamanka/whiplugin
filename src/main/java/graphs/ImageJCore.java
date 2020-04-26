package graphs;

import io.scif.services.DatasetIOService;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

public class ImageJCore {
    
    /**
     * SERVICES
     */
    private DatasetIOService datasetIOService;
    private ImageDisplayService imageDisplayService;
    private DisplayService displayService;
    private OpService ops;
    private LogService log;
    private StatusService statusService;
    private ThreadService thread;
    private UIService ui;
    
    public ImageJCore() {
    
    }
    
    public DatasetIOService getDatasetIOService() {
        return datasetIOService;
    }
    
    public void setDatasetIOService(DatasetIOService datasetIOService) {
        this.datasetIOService = datasetIOService;
    }
    
    public ImageDisplayService getImageDisplayService() {
        return imageDisplayService;
    }
    
    public void setImageDisplayService(ImageDisplayService imageDisplayService) {
        this.imageDisplayService = imageDisplayService;
    }
    
    public DisplayService getDisplayService() {
        return displayService;
    }
    
    public void setDisplayService(DisplayService displayService) {
        this.displayService = displayService;
    }
    
    public OpService getOps() {
        return ops;
    }
    
    public void setOps(OpService ops) {
        this.ops = ops;
    }
    
    public LogService getLog() {
        return log;
    }
    
    public void setLog(LogService log) {
        this.log = log;
    }
    
    public StatusService getStatusService() {
        return statusService;
    }
    
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }
    
    public ThreadService getThread() {
        return thread;
    }
    
    public void setThread(ThreadService thread) {
        this.thread = thread;
    }
    
    public UIService getUi() {
        return ui;
    }
    
    public void setUi(UIService ui) {
        this.ui = ui;
    }
}
