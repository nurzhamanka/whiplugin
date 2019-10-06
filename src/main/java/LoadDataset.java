import net.imagej.Dataset;
import net.imagej.ImageJ;

import java.io.File;
import java.io.IOException;

public class LoadDataset {
    
    public static void main(final String... args) {
        
        final ImageJ imageJ = new ImageJ();
        
        final File file = imageJ.ui().chooseFile(null, "open");
    
        try {
            final Dataset dataset = imageJ.scifio().datasetIO().open(file.getPath());
            imageJ.ui().show(dataset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
