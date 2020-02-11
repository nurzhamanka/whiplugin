package graphs.model;

import java.io.Serializable;

public class Operation implements Serializable {
    
    private String name;
    private OpType type;
    
    public Operation() {
    }
    
    public Operation(String name, OpType type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public OpType getType() {
        return type;
    }
    
    public void setType(OpType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return name + "\n" + type.toString();
    }
}