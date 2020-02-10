package graphs.editor;

import graphs.model.Operation;

import javax.swing.*;
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
    
    public PropertiesDialog(Operation operation) {
        super((Frame) null, String.format("Properties (%s)", operation.getName()), true);
        final JLabel label = new JLabel("Hello there! Click OK to change the node!");
        add(label);
        pack();
        
        final JButton okButton = new JButton("OK");
        add(okButton);
        okButton.addActionListener(e -> {
            operation.setName("LMAO");
            System.out.println("before =" + getModalityType());
            setModal(true);
            System.out.println("after =" + getModalityType());
            getOwner().setEnabled(true);
            PropertiesDialog.this.dispose();
        });
        pack();
    }
}