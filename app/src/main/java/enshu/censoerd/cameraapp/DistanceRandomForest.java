package enshu.censoerd.cameraapp;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Created by 龍一 on 2015/07/06.
 */
public class DistanceRandomForest {
    RandomForest forest;
    DistanceRandomForest(String filename){
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            forest = (RandomForest) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
