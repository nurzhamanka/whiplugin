package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.HybridOp;

import javax.swing.*;

public class HybridDialog extends PropertiesDialog {
    
    private HybridOp hybridOp;
    
    private int kernelSize;
    
    private JLabel kernelSizeLabel;
    private JSlider kernelSizeSlider;
    
    public HybridDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        hybridOp = (HybridOp) operation;
        kernelSize = hybridOp.getKernelSize();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        
        kernelSizeLabel = new JLabel("Radius:");
        kernelSizeSlider = new JSlider(JSlider.HORIZONTAL, 5, 21, 5);
        kernelSizeSlider.addChangeListener(e -> kernelSize = ((JSlider) e.getSource()).getValue());
        kernelSizeSlider.setMajorTickSpacing(4);
        kernelSizeSlider.setPaintTicks(true);
        kernelSizeSlider.setPaintLabels(true);
        kernelSizeSlider.setSnapToTicks(true);
        
        labelPanel.add(kernelSizeLabel);
        fieldPanel.add(kernelSizeSlider);
    }
    
    @Override
    protected void save() {
        hybridOp.setKernelSize(kernelSize);
        graph.refresh();
    }
}
