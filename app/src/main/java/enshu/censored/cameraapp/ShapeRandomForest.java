package enshu.censored.cameraapp;

import android.util.Log;

import java.io.InputStream;
import java.io.ObjectInputStream;

import static java.util.Arrays.fill;

/**
 * Created by 龍一 on 2015/07/07.
 */
public class ShapeRandomForest {
    RandomForest forest;
    int[] classifiedImage;
    public int[] genre;
    //0:BackGround 1:Rock 2:Scissors 3:Paper 4:Noise 5:NoGesture
    private int _width,_height;



    public int max(int x[]) {
        int max_x = 0;
        int max_i = 0;
        for (int i = 0; i < x.length; i++) {
            if (max_x < x[i]) {
                max_x = x[i];
                max_i = i;
            }
        }
        return max_i;
    }
    private int getGenre(int n,BitArray s,int x,int y){
        RandomForest.Tree tree = forest.forest[n];
        while (!tree.isSplitNode){
            if (tree.splitfunction.satisfy(s,x,y,_width,_height)){
                tree = tree.left;
            }
            else {
                tree = tree.right;
            }
        }
        return tree.genre;
    }

    public void classify(BitArray bits){
        fill(genre,0);
        for (int x = 0; x < _width; x++)
            for (int y = 0; y < _height; y++){
                boolean bit = bits.get(x+_width*y);
                if (bit){
                    int[] g = {0,0,0,0,0,0};
                    for (int n = 0; n < forest.numberOfTree; n++)
                    {
                        g[getGenre(n,bits,x,y)]++;
                        genre[getGenre(n,bits,x,y)]++;
                    }
                    classifiedImage[x+_width*y]=Distance.toPixelFormat(max(g));
                } else {
                    classifiedImage[x+_width*y]=0xFF000000;
                }
            }
    }

    ShapeRandomForest(InputStream is,int[] image,int width,int height){
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            forest = (RandomForest) ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.e("SRF.construct", Log.getStackTraceString(e));
        }
        genre = new int[6];
        classifiedImage = image;
        _width = width;
        _height = height;
    }
}
