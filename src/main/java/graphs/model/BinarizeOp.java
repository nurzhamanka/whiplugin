package graphs.model;

import java.util.Map;

public class BinarizeOp extends Operation {
    
    private ThresholdType threshType;
    private Double denominator;
    
    public BinarizeOp() {
        super("Binarize", OpType.BINARIZE);
        threshType = ThresholdType.MEAN;
        denominator = Math.PI;
    }
    
    public ThresholdType getThreshType() {
        return threshType;
    }
    
    public void setThreshType(ThresholdType threshType) {
        this.threshType = threshType;
    }
    
    public Double getDenominator() {
        return denominator;
    }
    
    public void setDenominator(Double denominator) {
        this.denominator = denominator;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("threshType", String.valueOf(threshType.toString()));
        map.put("denominator", String.valueOf(denominator));
        return map;
    }
    
    @Override
    public String toString() {
        if (threshType == ThresholdType.MEAN || threshType == ThresholdType.MEDIAN)
            return String.format("%s\nt = %s/%.4f", super.toString(), threshType.toString(), denominator);
        return String.format("%s\nt = %s", super.toString(), threshType.toString());
    }
}
