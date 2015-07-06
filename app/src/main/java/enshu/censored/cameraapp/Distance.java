package enshu.censored.cameraapp;

/**
 * Created by censored on 15/07/06.
 */
class Distance {
    public enum Genre {
        Far, Fit, Close, Noise, BackGround
    };

    public static Distance.Genre valueOf(int i) {
        switch (i) {
            case 1:
                return Genre.Close;
            case 2:
                return Genre.Fit;
            case 3:
                return Genre.Far;
            case 4:
                return Genre.Noise;
            default:
                return Genre.BackGround;
        }
    }

    static boolean getBinary(int pixel) {
        return (pixel & 0x00FFFFFF) != 0;
    }

    public static int toPixelFormat(int k) {
        final int _Close = 0xFFFF0000;// red
        final int _Fit = 0xFF00FF00;// green
        final int _Far = 0xFF0000FF;// blue
        final int _Noise = 0xFFFFFFFF;// white
        final int _BackGround = 0xFF000000;// black
        switch (k) {
            case 1:
                return _Close;
            case 2:
                return _Fit;
            case 3:
                return _Far;
            case 4:
                return _Noise;
            default:
                return _BackGround;
        }
    }
}
