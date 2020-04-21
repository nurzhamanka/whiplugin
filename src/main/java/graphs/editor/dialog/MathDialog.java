package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.MathOp;
import graphs.model.MathType;

import javax.swing.*;
import java.text.NumberFormat;

public class MathDialog extends PropertiesDialog {
    
    private MathOp mathOp;
    
    private MathType mathType;
    private Float arg;
    
    private JLabel typeLabel;
    private JRadioButton typeSqrButton;
    private JRadioButton typeSqrtButton;
    private JRadioButton typePowButton;
    private ButtonGroup typeButtonGroup;
    
    private JLabel argLabel;
    private JFormattedTextField argField;
    
    public MathDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        mathOp = (MathOp) operation;
        mathType = mathOp.getMathType();
        arg = mathOp.getArg();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        typeLabel = new JLabel("Operation:");
        JPanel buttonPanel = new JPanel();
        
        BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
        buttonPanel.setLayout(buttonPanelLayout);
        
        typeSqrButton = new JRadioButton("sqr(x)");
        typeSqrtButton = new JRadioButton("sqrt(x)");
        typePowButton = new JRadioButton("pow(x,n)");
        
        buttonPanel.add(typeSqrButton);
        buttonPanel.add(typeSqrtButton);
        buttonPanel.add(typePowButton);
        
        typeButtonGroup = new ButtonGroup();
        typeButtonGroup.add(typeSqrButton);
        typeButtonGroup.add(typeSqrtButton);
        typeButtonGroup.add(typePowButton);
        
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(2);
        
        argLabel = new JLabel("Power:");
        argField = new JFormattedTextField(doubleFormat);
        argField.setValue(arg);
        
        typeSqrButton.addActionListener(e -> {
            mathType = MathType.SQUARE;
            argLabel.setVisible(false);
            argField.setVisible(false);
        });
        typeSqrtButton.addActionListener(e -> {
            mathType = MathType.SQRT;
            argLabel.setVisible(false);
            argField.setVisible(false);
        });
        typePowButton.addActionListener(e -> {
            mathType = MathType.POW;
            argLabel.setVisible(true);
            argField.setVisible(true);
        });
        
        switch (mathType) {
            case SQUARE:
                typeSqrButton.setSelected(true);
                argLabel.setVisible(false);
                argField.setVisible(false);
                break;
            case SQRT:
                typeSqrtButton.setSelected(true);
                argLabel.setVisible(false);
                argField.setVisible(false);
                break;
            case POW:
                typePowButton.setSelected(true);
                argLabel.setVisible(true);
                argField.setVisible(true);
                break;
        }
        
        labelPanel.add(typeLabel);
        fieldPanel.add(buttonPanel);
        
        labelPanel.add(argLabel);
        fieldPanel.add(argField);
    }
    
    @Override
    protected void save() {
        mathOp.setMathType(mathType);
        if (argField.isVisible())
            mathOp.setArg(((Number) argField.getValue()).floatValue());
        graph.refresh();
    }
}
