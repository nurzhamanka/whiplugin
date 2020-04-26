package graphs.model;

import java.io.File;
import java.util.Map;

public class OutputOp extends Operation {
    
    private File outputDirectory;
    
    public OutputOp() {
        super("Output", OpType.OUTPUT);
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("inputDirectory", outputDirectory != null ? outputDirectory.toString() : "None");
        return map;
    }
    
    @Override
    public String toString() {
        String str = super.toString();
        if (outputDirectory != null)
            str += String.format("\noutputDir = %s", outputDirectory.toString());
        return str;
    }
    
    @Override
    public boolean isValid() {
        return outputDirectory != null && outputDirectory.isDirectory();
    }
    
    public File getOutputDirectory() {
        return outputDirectory;
    }
    
    public void setoutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
