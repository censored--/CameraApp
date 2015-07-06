package enshu.censoerd.cameraapp;

/*
64bit: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
set(3,true): XXX1XXXXXXXXXXXXXXXXXXXXXXXXXX

*/
public class BitArray {
    private long[] bitarray;
    public int length(){
        return bitarray.length*Long.SIZE;
    }
    public boolean get(int k){
        int p = k / Long.SIZE;
        int q = k % Long.SIZE;
        long mask = Long.rotateLeft(1, Long.SIZE - q - 1);
        return (mask & bitarray[p])!=0;
    }

    public void set(int k,boolean b){
        int p = k / Long.SIZE;
        int q = k % Long.SIZE;
        long mask = Long.rotateLeft(1, Long.SIZE - q - 1);
        if (b)
            bitarray[p] |= mask;
        else
            bitarray[p] &= ~mask;
    }

    public BitArray(int length) {
        int l =length / Long.SIZE + ((length%Long.SIZE)!= 0 ? 1 : 0);
        bitarray = new long[l];
    }

    public BitArray(boolean[] b){
        int length = b.length;
        int l =length / Long.SIZE + ((length%Long.SIZE)!= 0 ? 1 : 0);
        bitarray = new long[l];
        for (int i = 0; i < length; i++)
            set(i,b[i]);
    }
}