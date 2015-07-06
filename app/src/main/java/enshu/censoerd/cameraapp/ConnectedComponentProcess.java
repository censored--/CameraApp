package enshu.censoerd.cameraapp;

import android.util.Log;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.Arrays.fill;


/**
 * Created by 龍�? on 2015/06/25.
 */
public class ConnectedComponentProcess {
    private int[] r_table;
    private BitArray pic;
    private int w,h,size;
    private TreeMap<Integer,TreeSet<Integer>> S;
    public ConnectedComponentProcess(BitArray p,int width,int height){
        w = width;
        h = height;
        size = width * height;
        pic = p;
        S = new TreeMap<>();
        r_table = new int[size];
    }

    private int up(int x){
        return x >= w ? x - w : x;
    }
    private int left(int x){
        return x % w > 0 ? x - 1 : x;
    }
    public void deleteNoise(){
        //1st pass,2nd pass
        for (int i = 0; i < size; i++){
            if (pic.get(i)){
                int up = up(i),left = left(i);
                boolean picUp = pic.get(up),picLeft = pic.get(left);
                if (picUp && up != i) {
                    r_table[i] = r_table[up];
                    S.get(r_table[up]).add(i);

                    if (picLeft && left != i){
                        int r_up = r_table[up];
                        int r_left = r_table[left];
                        if (r_up < r_left){
                            for (int j : S.get(r_left))
                                r_table[j] = r_up;
                            S.get(r_up).addAll(S.get(r_left));
                            S.remove(r_left);
                        }
                        else if (r_up > r_left){
                            for (int j : S.get(r_up))
                                r_table[j] = r_left;
                            S.get(r_left).addAll(S.get(r_up));
                            S.remove(r_up);
                        }
                    }
                } else if (picLeft&&left != i) {
                        r_table[i] = r_table[left];
                        S.get(r_table[left]).add(i);
                } else{
                    r_table[i] = i;
                    TreeSet<Integer> t = new TreeSet<>();
                    t.add(i);
                    S.put(i, t);
                }
            }
        }
        int max = 0;
        int i_max = -1;
        for (Map.Entry<Integer, TreeSet<Integer>> s : S.entrySet()){
            int sSize = s.getValue().size();
            if (max < sSize){
                max = sSize;
                i_max = s.getKey();
            }
        }
        S.remove(i_max);
        for (TreeSet<Integer> Svalue : S.values()){
            for (int k : Svalue){
                pic.set(k,false);
            }
        }
        S.clear();
    }
}
