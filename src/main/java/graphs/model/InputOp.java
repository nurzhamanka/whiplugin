package graphs.model;

import java.io.File;
import java.util.Map;

public class InputOp extends Operation {
    
    private String datasetName;
    private File inputDirectory;
    
    public InputOp() {
        super("Input", OpType.INPUT);
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("datasetName", datasetName != null ? datasetName : "None");
        map.put("inputDirectory", inputDirectory != null ? inputDirectory.toString() : "None");
        return map;
    }
    
    @Override
    public String toString() {
        String str = super.toString();
        if (datasetName != null)
            str += String.format("\ndatasetName = %s", datasetName);
        if (inputDirectory != null)
            str += String.format("\ninputDir = %s", inputDirectory.toString());
        return str;
    }
    
    @Override
    public boolean isValid() {
        return datasetName != null && !datasetName.isEmpty() && inputDirectory != null && inputDirectory.isDirectory();
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
