package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.Join;
import graphs.model.JoinStrategy;

import javax.swing.*;

public class JoinDialog extends PropertiesDialog {
    
    private Join joinOp;
    
    private JoinStrategy joinStrategy;
    
    private JLabel strategyLabel;
    private JComboBox<JoinStrategy> strategyComboBox;
    
    public JoinDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        joinOp = (Join) operation;
        joinStrategy = joinOp.getStrategy();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        strategyLabel = new JLabel("Operation:");
        strategyComboBox = new JComboBox<>(JoinStrategy.values());
        strategyComboBox.setSelectedItem(joinStrategy);
        strategyComboBox.addActionListener(e -> joinStrategy = (JoinStrategy) ((JComboBox) e.getSource()).getSelectedItem());
        
        labelPanel.add(strategyLabel);
        fieldPanel.add(strategyComboBox);
    }
    
    @Override
    protected void save() {
        joinOp.setStrategy(joinStrategy);
        graph.refresh();
    }
}
