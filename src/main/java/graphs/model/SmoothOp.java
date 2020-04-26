package graphs.model;

import java.util.Map;

public class SmoothOp extends Operation {
    
    private Integer kernelSize;
    
    public SmoothOp() {
        super("Mean Smoothing", OpType.MEAN);
        kernelSize = 19;
    }
    
    public SmoothOp(Integer kernelSize) {
        this.kernelSize = kernelSize;
    }
    
    public Integer getKernelSize() {
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
        return String.format("%s\nsize = %sx%s", super.toString(), kernelSize, kernelSize);
    }
}
