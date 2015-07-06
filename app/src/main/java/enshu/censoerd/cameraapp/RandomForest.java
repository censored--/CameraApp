package enshu.censoerd.cameraapp;

import java.io.Serializable;

public class RandomForest implements Serializable {
    private static final long serialVersionUID = -4042668303301496279L;
    public int numberOfTree;
    public Tree[] forest;

    static public class Tree implements Serializable {
        private static final long serialVersionUID = -3463601467746667431L;
        public Tree left;
        public Tree right;
        public boolean isSplitNode;
        public SplitFunction splitfunction;
        public int genre;
    }

    static public class SplitFunction implements Serializable {
        private static final long serialVersionUID = -1080757224636557780L;
        public int wx, wy;
        public int vx, vy;
        public boolean Gammax;
        public boolean Gammay;

        public boolean satisfy(BitArray s, int ux, int uy, int width, int height) {
            int upwx = ux + wx;
            int upwy = uy + wy;
            int upvx = ux + vx;
            int upvy = uy + vy;
            upwx = upwx < 0 ? 0 : (upwx < width ? upwx : width - 1);
            upvx = upvx < 0 ? 0 : (upvx < width ? upvx : width - 1);
            upwy = upwy < 0 ? 0 : (upwy < height ? upwy : height - 1);
            upvy = upvy < 0 ? 0 : (upvy < height ? upvy : height - 1);
            return s.get(upwx + width * upwy) == Gammax
                    && s.get(upvx + width * upvy) == Gammay;
        }

        SplitFunction(int _wx, int _wy, int _vx, int _vy, boolean _Gammax,
                      boolean _Gammay) {
            wx = _wx;
            wy = _wy;
            vx = _vx;
            vy = _vy;
            Gammax = _Gammax;
            Gammay = _Gammay;
        }
    }

    RandomForest(int _numberOfTree) {
        forest = new Tree[_numberOfTree];
        numberOfTree = _numberOfTree;
    }
}
