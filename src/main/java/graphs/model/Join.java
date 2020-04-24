package graphs.model;

import java.util.Map;

public class Join extends Operation {
    
    private JoinStrategy strategy;
    
    public Join() {
        super("Join", OpType.JOIN);
        strategy = JoinStrategy.AVERAGE;
    }
    
    public JoinStrategy getStrategy() {
        return strategy;
    }
    
    public void setStrategy(JoinStrategy strategy) {
        this.strategy = strategy;
    }
    
    @Override
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> map = super.getKeyValuePairs();
        map.put("strategy", strategy.toString());
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("%s\nstrategy = %s", super.toString(), strategy.toString());
    }
}
