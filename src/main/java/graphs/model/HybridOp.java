package graphs.model;

import java.util.Map;

public class HybridOp extends Operation {
    
    private int kernelSize;
    
    public HybridOp() {
        super("Hybrid Filter", OpType.HYBRID);
        kernelSize = 5;
    }
    
    public HybridOp(int kernelSize) {
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
