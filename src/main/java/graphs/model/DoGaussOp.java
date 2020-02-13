package graphs.model;

import java.util.Map;

public class DoGaussOp extends Operation {
    
    private double sigma1;
    private double sigma2;
    
    public DoGaussOp(double sigma1, double sigma2) {
        super("Difference of Gaussians", OpType.DOG);
        this.sigma1 = sigma1;
        this.sigma2 = sigma2;
    }
    
    public DoGaussOp() {
        super("Difference of Gaussians", OpType.DOG);
        sigma1 = 2;
        sigma2 = 1;
    }
    
    public double getSigma1() {
        return sigma1;
    }
    
    public void setSigma1(double sigma1) {
        this.sigma1 = sigma1;
    }
    
    public double getSigma2() {
        return sigma2;
    }
    
    public void setSigma2(double sigma2) {
        this.sigma2 = sigma2;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("sigma1", String.valueOf(sigma1));
        map.put("sigma2", String.valueOf(sigma2));
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("%s\ns1 = %s\ns2 = %s", super.toString(), sigma1, sigma2);
    }
}