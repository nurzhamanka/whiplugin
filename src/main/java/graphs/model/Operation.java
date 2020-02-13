package graphs.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

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
    
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("type", type.toString());
        return map;
    }
    
    @Override
    public String toString() {
        return type.toString();
    }
}