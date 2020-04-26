package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.DoGaussOp;

import javax.swing.*;
import java.text.NumberFormat;

public class DoGaussDialog extends PropertiesDialog {
    
    private DoGaussOp gaussOp;
    
    private Double[] sigmas = new Double[2];
    
    private JLabel[] sigmaLabels = new JLabel[2];
    private JFormattedTextField[] sigmaFields = new JFormattedTextField[2];
    
    public DoGaussDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        gaussOp = (DoGaussOp) operation;
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        
        sigmas[0] = gaussOp.getSigma1();
        sigmas[1] = gaussOp.getSigma2();
        
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(2);
        
        for (byte i = 0; i < 2; i++) {
            sigmaLabels[i] = new JLabel("sigma" + (i + 1) + ":");
            sigmaFields[i] = new JFormattedTextField(doubleFormat);
            sigmaFields[i].setValue(sigmas[i]);
            sigmaFields[i].setColumns(10);
            sigmaLabels[i].setLabelFor(sigmaFields[i]);
            labelPanel.add(sigmaLabels[i]);
            fieldPanel.add(sigmaFields[i]);
        }
    }
    
    @Override
    protected void save() {
        gaussOp.setSigma1(((Number) sigmaFields[0].getValue()).doubleValue());
        gaussOp.setSigma2(((Number) sigmaFields[1].getValue()).doubleValue());
        graph.refresh();
    }
}
