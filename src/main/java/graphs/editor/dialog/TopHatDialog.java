package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.TopHatOp;

import javax.swing.*;

public class TopHatDialog extends PropertiesDialog {
    
    private TopHatOp topHatOp;
    
    private TopHatOp.ThType thType;
    private TopHatOp.Shape shape;
    private int radius;
    
    private JLabel typeLabel;
    private JRadioButton typeWhiteButton;
    private JRadioButton typeBlackButton;
    private ButtonGroup typeButtonGroup;
    
    private JLabel shapeLabel;
    private JComboBox<TopHatOp.Shape> shapeBox;
    
    private JLabel radiusLabel;
    private JSlider radiusSlider;
    
    private TopHatOp.Shape[] shapeStrings = {TopHatOp.Shape.DISK, TopHatOp.Shape.DIAMOND, TopHatOp.Shape.SQUARE};
    
    public TopHatDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        topHatOp = (TopHatOp) operation;
        thType = topHatOp.getThType();
        shape = topHatOp.getShape();
        radius = topHatOp.getRadius();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        typeLabel = new JLabel("Type:");
        JPanel buttonPanel = new JPanel();
        BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
        buttonPanel.setLayout(buttonPanelLayout);
        typeWhiteButton = new JRadioButton("White");
        typeBlackButton = new JRadioButton("Black");
        buttonPanel.add(typeWhiteButton);
        buttonPanel.add(typeBlackButton);
        typeButtonGroup = new ButtonGroup();
        typeButtonGroup.add(typeWhiteButton);
        typeButtonGroup.add(typeBlackButton);
        typeWhiteButton.setSelected(true);
        typeWhiteButton.addActionListener(e -> thType = TopHatOp.ThType.WHITE);
        typeBlackButton.addActionListener(e -> thType = TopHatOp.ThType.BLACK);
        
        shapeLabel = new JLabel("Shape:");
        shapeBox = new JComboBox<>(shapeStrings);
        shapeBox.setSelectedItem(TopHatOp.Shape.DISK);
        shapeBox.addActionListener(e -> shape = (TopHatOp.Shape) ((JComboBox) e.getSource()).getSelectedItem());
        
        radiusLabel = new JLabel("Radius:");
        radiusSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 6);
        radiusSlider.addChangeListener(e -> radius = ((JSlider) e.getSource()).getValue());
        radiusSlider.setMajorTickSpacing(4);
        radiusSlider.setMinorTickSpacing(1);
        radiusSlider.setPaintTicks(true);
        radiusSlider.setPaintLabels(true);
        radiusSlider.setSnapToTicks(true);
        
        labelPanel.add(typeLabel);
        fieldPanel.add(buttonPanel);
        
        labelPanel.add(shapeLabel);
        fieldPanel.add(shapeBox);
        
        labelPanel.add(radiusLabel);
        fieldPanel.add(radiusSlider);
    }
    
    @Override
    protected void save() {
        topHatOp.setThType(thType);
        topHatOp.setShape(shape);
        topHatOp.setRadius(radius);
        graph.refresh();
    }
}
