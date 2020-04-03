package graphs.model;

import java.util.Map;

public class SmoothOp extends Operation {
    
    private int kernelSize;
    
    public SmoothOp() {
        super("Mean Smoothing", OpType.MEAN);
        kernelSize = 19;
    }
    
    public SmoothOp(int kernelSize) {
        this.kernelSize = kernelSize;
    }
    
    public int getKernelSize() {
        return kernelSize;
    }
    
    public void setKernelSize(int kernelSize) {
        this.kernelSize = kernelSize;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("kernelSize", String.valueOf(kernelSize));
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("%s\nsize = %s", super.toString(), kernelSize);
    }
}
