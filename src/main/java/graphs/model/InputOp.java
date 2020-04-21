package graphs.model;

import java.io.File;

public class InputOp extends Operation {
    
    private String datasetName;
    private File inputDirectory;
    
    public InputOp() {
        super("Input", OpType.INPUT);
    }
    
    public String getDatasetName() {
        return datasetName;
    }
    
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    
    public File getInputDirectory() {
        return inputDirectory;
    }
    
    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }
}
