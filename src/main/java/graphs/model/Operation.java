package graphs.model;

import java.io.Serializable;

public class Operation implements Serializable {
    
    private String name;
    
    public Operation() {
    }
    
    public Operation(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}