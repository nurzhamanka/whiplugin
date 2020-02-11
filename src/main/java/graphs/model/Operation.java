package graphs.model;

import java.io.Serializable;

public abstract class Operation implements Serializable {
    
    protected String name;
    protected OpType type;
    
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