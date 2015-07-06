package enshu.censored.cameraapp;

import android.util.Log;
import android.widget.TextView;

import java.io.InputStream;
import java.io.ObjectInputStream;

import enshu.censoerd.cameraapp.R;

import static java.util.Arrays.fill;

/**
 * Created by 龍一 on 2015/07/06.
 */


public class DistanceRandomForest {
    RandomForest forest;
    int[] classifiedImage;
    public int[] genre;
    //0:Too Close 1:Fit 2:Too Far 3:Noise 4:BackGround
    private int _width,_height;



    private int max(int x[]) {
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
                    int g = 0;
                    for (int n = 0; n < forest.numberOfTree; n++)
                    {
                        g = getGenre(n,bits,x,y);
                        genre[g]++;
                    }
                    classifiedImage[x+_width*y]=Distance.toPixelFormat(g);
                } else {
                    classifiedImage[x+_width*y]=0xFF000000;
                }
            }
    }

    DistanceRandomForest(InputStream is,int[] image,int width,int height){
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            forest = (RandomForest) ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.e("DRF.construct",Log.getStackTraceString(e));
        }
        genre = new int[5];
        classifiedImage = image;
        _width = width;
        _height = height;
    }
}
