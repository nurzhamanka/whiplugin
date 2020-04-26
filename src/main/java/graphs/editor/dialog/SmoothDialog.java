package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.SmoothOp;

import javax.swing.*;

public class SmoothDialog extends PropertiesDialog {
    
    private SmoothOp smoothOp;
    
    private Integer kernelSize;
    
    private JLabel kernelSizeLabel;
    private JSlider kernelSizeSlider;
    
    public SmoothDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        smoothOp = (SmoothOp) operation;
        kernelSize = smoothOp.getKernelSize();
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
        smoothOp.setKernelSize(kernelSize);
        graph.refresh();
    }
}
