package enshu.censoerd.cameraapp;

import android.util.Pair;

import java.util.HashMap;

/**
 * Created by —´ˆê on 2015/06/25.
 */
public class Forest {
    private class SplitFunction {
        public double w;
        public double v;
        public Pair<Boolean, Boolean> Gamma;
    }

    private class Tree {
        public int key;
        public Pair<Integer, Integer>[] bags;
        public Tree left;
        public Tree right;
        public boolean isSplitNode;
        public SplitFunction splitfunction;
    }

    public HashMap<Integer, Tree>[][][] forest;

    Forest(int width, int height, int numberOfTree) {
        forest = new HashMap[width][height][numberOfTree];
    }
}