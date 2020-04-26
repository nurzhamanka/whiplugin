package graphs.model;

import java.util.Map;

public class MathOp extends Operation {
    
    private MathType mathType;
    private Double arg;
    
    public MathOp() {
        super("Math", OpType.MATH);
        this.mathType = MathType.SQUARE;
        this.arg = Double.NaN;
    }
    
    public MathOp(MathType mathType) {
        super("Math", OpType.MATH);
        this.mathType = mathType;
    }
    
    public void clearArg() {
        arg = Double.NaN;
    }
    
    public MathType getMathType() {
        return mathType;
    }
    
    public void setMathType(MathType mathType) {
        this.mathType = mathType;
    }
    
    public Double getArg() {
        return arg;
    }
    
    public void setArg(Double arg) {
        this.arg = arg;
    }
    
    @Override
    public boolean isValid() {
        if (mathType == MathType.SQUARE || mathType == MathType.SQRT) return true;
        if (mathType == MathType.POW) return arg != Double.NaN;
        return true;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("mathType", String.valueOf(mathType));
        map.put("arg", String.valueOf(arg));
        return map;
    }
    
    @Override
    public String toString() {
        switch (mathType) {
            case SQUARE:
                return String.format("%s\nsqr(x)", super.toString());
            case SQRT:
                return String.format("%s\nsqrt(x)", super.toString());
            case POW:
                return String.format("%s\npow(x, %s)", super.toString(), arg);
            default:
                return "ERROR";
        }
    }
}
