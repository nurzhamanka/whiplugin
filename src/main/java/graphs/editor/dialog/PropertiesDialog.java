package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.Operation;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a generic properties dialog.
 *
 * @author Nurzhan Sakenov
 * @author github.com/nurzhamanka
 */
public abstract class PropertiesDialog extends JDialog {
    
    private static final long serialVersionUID = 7771193885935137066L;
    
    protected String name;
    protected Operation operation;
    protected mxGraph graph;
    protected mxCell cell;
    
    protected JPanel labelPanel;
    protected JPanel fieldPanel;
    private JPanel buttonPanel;
    
    private JButton okButton;
    private JButton cancelButton;
    
    public PropertiesDialog(mxCell cell, mxGraph graph) {
        super((Frame) null, ((Operation) cell.getValue()).getName(), true);
        this.graph = graph;
        this.cell = cell;
        operation = (Operation) cell.getValue();
        name = operation.getName();
    
        labelPanel = new JPanel(new GridLayout(0, 1));
        fieldPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
    }
    
    protected void assembleDialog() {
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(okButton);
        
        okButton.addActionListener(e -> {
            save();
            PropertiesDialog.this.dispose();
        });
        
        cancelButton.addActionListener(e -> PropertiesDialog.this.dispose());
        
        getContentPane().add(labelPanel, BorderLayout.CENTER);
        getContentPane().add(fieldPanel, BorderLayout.LINE_END);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pack();
        setResizable(false);
        setVisible(true);
    }
    
    protected abstract void populateFields();
    
    protected abstract void save();
}