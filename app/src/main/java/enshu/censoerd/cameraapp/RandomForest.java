package enshu.censoerd.cameraapp;

import android.hardware.camera2.params.MeteringRectangle;
import android.util.Pair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * Created by —´ˆê on 2015/06/25.
 */

/*
public class Forest{
    private class SplitFunction{
        public double w;
        public double v;
        public Pair<Boolean,Boolean> Gamma;
    }
    private class Tree{
        public int key;
        public Pair<Integer,Integer>[] bags;
        public Tree left;
        public Tree right;
        public boolean isSplitNode;
        public SplitFunction splitfunction;
    }
    public HashMap<Integer,Tree>[][][] forest;
}*/

public class RandomForest {
    Forest forest;
    private int numberOfTree;

    RandomForest(int width, int height, String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            forest = (Forest) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        numberOfTree = forest.forest[0][0].length;
    }

    RandomForest(int width, int height, int _numberOfTree) {
        numberOfTree = _numberOfTree;
        forest = new Forest(width, height, _numberOfTree);
    }

    int getNumberOfTree() {
        return numberOfTree;
    }

    void writeObject(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
