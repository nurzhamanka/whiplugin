package graphs.model;

import java.util.Map;

public class BinarizeOp extends Operation {
    
    private ThresholdType threshType;
    private Double denominator;
    private Boolean doParticleAnalysis;
    
    public BinarizeOp() {
        super("Binarize", OpType.BINARIZE);
        threshType = ThresholdType.MEAN;
        denominator = Math.PI;
        doParticleAnalysis = false;
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
    
    public Boolean getDoParticleAnalysis() {
        return doParticleAnalysis;
    }
    
    public void setDoParticleAnalysis(Boolean doParticleAnalysis) {
        this.doParticleAnalysis = doParticleAnalysis;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("threshType", String.valueOf(threshType.toString()));
        map.put("denominator", String.valueOf(denominator));
        map.put("doParticleAnalysis", String.valueOf(doParticleAnalysis));
        return map;
    }
    
    @Override
    public String toString() {
        if (threshType == ThresholdType.MEAN || threshType == ThresholdType.MEDIAN)
            return String.format("%s\nt = %s/%.4f\n%s", super.toString(), threshType.toString(), denominator, doParticleAnalysis);
        return String.format("%s\nt = %s\n%s", super.toString(), threshType.toString(), doParticleAnalysis);
    }
}
