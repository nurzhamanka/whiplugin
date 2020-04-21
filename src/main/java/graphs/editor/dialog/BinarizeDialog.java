package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.BinarizeOp;
import graphs.model.ThresholdType;

import javax.swing.*;
import java.text.NumberFormat;

public class BinarizeDialog extends PropertiesDialog {
    
    private BinarizeOp binOp;
    
    private ThresholdType threshType;
    private Double denominator;
    
    private JLabel typeLabel;
    private JComboBox<ThresholdType> typeBox;
    private ThresholdType[] types = {ThresholdType.MEAN, ThresholdType.MEDIAN,
            ThresholdType.OTSU, ThresholdType.MAX_ENTROPY, ThresholdType.ISO_DATA,
            ThresholdType.RENYI, ThresholdType.HUANG};
    
    private JLabel denominatorLabel;
    private JFormattedTextField denominatorField;
    
    private JLabel convertLabel;
    private JCheckBox convertCheckBox;
    
    public BinarizeDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        binOp = (BinarizeOp) operation;
        threshType = binOp.getThreshType();
        denominator = binOp.getDenominator();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        typeLabel = new JLabel("Threshold type:");
        typeBox = new JComboBox<>(types);
        typeBox.setSelectedItem(threshType);
        typeBox.addActionListener(e -> threshType = (ThresholdType) ((JComboBox) e.getSource()).getSelectedItem());
        
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(0);
        doubleFormat.setMaximumFractionDigits(10);
        
        denominatorLabel = new JLabel("Denominator:");
        denominatorField = new JFormattedTextField(doubleFormat);
        denominatorField.setValue(denominator);
        
        convertLabel = new JLabel();
        convertCheckBox = new JCheckBox("3.14...->PI & 2.71...->E");
        
        labelPanel.add(typeLabel);
        fieldPanel.add(typeBox);
        
        labelPanel.add(denominatorLabel);
        fieldPanel.add(denominatorField);
        
        labelPanel.add(convertLabel);
        fieldPanel.add(convertCheckBox);
    }
    
    @Override
    protected void save() {
        binOp.setThreshType(threshType);
        Double denom = ((Number) denominatorField.getValue()).doubleValue();
        if (Math.abs(denom - Math.PI) < 0.01 && convertCheckBox.isSelected())
            denom = Math.PI;
        else if (Math.abs(denom - Math.E) < 0.01 && convertCheckBox.isSelected())
            denom = Math.E;
        binOp.setDenominator(denom);
        graph.refresh();
    }
}
