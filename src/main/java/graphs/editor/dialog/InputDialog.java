package graphs.editor.dialog;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import graphs.model.InputOp;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class InputDialog extends PropertiesDialog {
    
    private InputOp inputOp;
    
    private String datasetName;
    private File inputDirectory;
    
    private JLabel datasetNameLabel;
    private JTextField datasetNameField;
    
    private JLabel inputDirectoryLabel;
    private JButton inputDirectoryButton;
    private JFileChooser inputDirectoryChooser;
    
    public InputDialog(mxCell cell, mxGraph graph) {
        super(cell, graph);
        inputOp = (InputOp) operation;
        datasetName = inputOp.getDatasetName();
        inputDirectory = inputOp.getInputDirectory();
        populateFields();
        assembleDialog();
    }
    
    @Override
    protected void populateFields() {
        datasetNameLabel = new JLabel("Dataset name:");
        datasetNameField = new JTextField(datasetName);
        
        inputDirectoryLabel = new JLabel("Dataset directory:");
        inputDirectoryButton = new JButton("Browse...");
        inputDirectoryChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        inputDirectoryChooser.setSelectedFile(inputDirectory);
        inputDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        inputDirectoryButton.addActionListener(e -> {
            int returnValue = inputDirectoryChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                inputDirectory = inputDirectoryChooser.getSelectedFile();
            }
        });
        
        labelPanel.add(datasetNameLabel);
        fieldPanel.add(datasetNameField);
        
        labelPanel.add(inputDirectoryLabel);
        fieldPanel.add(inputDirectoryButton);
    }
    
    @Override
    protected void save() {
        inputOp.setDatasetName(datasetNameField.getText());
        inputOp.setInputDirectory(inputDirectory);
        graph.refresh();
    }
}
