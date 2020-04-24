package graphs.model;

import java.util.Map;

public class TopHatOp extends Operation {
    
    private ThType thType;
    private Shape shape;
    private int radius;
    
    public TopHatOp(ThType thType, Shape shape, int radius) {
        super("Top-Hat", OpType.TOPHAT);
        this.thType = thType;
        this.shape = shape;
        this.radius = radius;
    }
    
    public TopHatOp() {
        super("Top-Hat", OpType.TOPHAT);
        this.thType = ThType.WHITE;
        this.shape = Shape.DISK;
        this.radius = 6;
    }
    
    public ThType getThType() {
        return thType;
    }
    
    public void setThType(ThType thType) {
        this.thType = thType;
    }
    
    public Shape getShape() {
        return shape;
    }
    
    public void setShape(Shape shape) {
        this.shape = shape;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("thType", thType.toString());
        map.put("shape", shape.toString());
        map.put("radius", String.valueOf(radius));
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("%s\ntype = %s\nshape = %s\nradius = %s", super.toString(), thType.toString(), shape.toString(), radius);
    }
    
    public enum ThType {
        BLACK,
        WHITE
    }
    
    public enum Shape {
        DISK,
        DIAMOND,
        SQUARE
    }
}