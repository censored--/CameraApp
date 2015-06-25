package enshu.censoerd.cameraapp;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by —´ˆê on 2015/06/25.
 */
public class LearningRandomForest {
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".bmp");
        }
    };

    File[] loadFiles(String dirname) {
        String path = new File(".").getAbsoluteFile().getParent();
        File d = new File(path);
        File[] files = d.listFiles();
        for (File file : files) {
            if (file.isDirectory() && file.toString().equals(dirname)) {
                File[] images = file.listFiles(filter);
                return files;
            }
        }
        return null;
    }

    public final void main() {

    }
}
