package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.OutputOp;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class OutputDialog extends PropertiesDialog {
    
    private OutputOp outputOp;
    
    private File outputDirectory;
    
    private JLabel outputDirectoryLabel;
    private JButton outputDirectoryButton;
    private JFileChooser outputDirectoryChooser;
    
    public OutputDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        outputOp = (OutputOp) operation;
        outputDirectory = outputOp.getOutputDirectory();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        
        outputDirectoryLabel = new JLabel("Save directory:");
        outputDirectoryButton = new JButton("Browse...");
        outputDirectoryChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        outputDirectoryChooser.setSelectedFile(outputDirectory);
        outputDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputDirectoryButton.addActionListener(e -> {
            int returnValue = outputDirectoryChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                outputDirectory = outputDirectoryChooser.getSelectedFile();
            }
        });
        
        labelPanel.add(outputDirectoryLabel);
        fieldPanel.add(outputDirectoryButton);
    }
    
    @Override
    protected void save() {
        outputOp.setoutputDirectory(outputDirectory);
        graph.refresh();
    }
}
