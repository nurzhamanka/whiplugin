package graphs.editor;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.Operation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Represents a generic properties dialog.
 *
 * @author Nurzhan Sakenov
 * @author github.com/nurzhamanka
 */
public class PropertiesDialog extends JDialog {
    
    private static final long serialVersionUID = 7771193885935137066L;
    
    protected String name;
    protected JTextField nameField;
    protected Operation operation;
    protected mxGraph graph;
    protected mxCell cell;
    
    public PropertiesDialog(mxCell cell, mxGraph graph) {
        super((Frame) null, ((Operation) cell.getValue()).getName());
        this.graph = graph;
        this.cell = cell;
        operation = (Operation) cell.getValue();
        name = operation.getName();
        
        JPanel panel = new JPanel(new GridLayout(1, 2, 4, 4));
        panel.add(new JLabel("Name"));
        nameField = new JTextField(name);
        panel.add(nameField);
        
        JPanel panelBorder = new JPanel();
        panelBorder.setBorder(new EmptyBorder(4, 4, 4, 4));
        panelBorder.add(panel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder()));
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        getRootPane().setDefaultButton(saveButton);
        
        saveButton.addActionListener(e -> {
            save();
            PropertiesDialog.this.dispose();
        });
        
        cancelButton.addActionListener(e -> PropertiesDialog.this.dispose());
        
        getContentPane().add(panelBorder, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }
    
    private void save() {
        operation.setName(nameField.getText());
        graph.refresh();
    }
}