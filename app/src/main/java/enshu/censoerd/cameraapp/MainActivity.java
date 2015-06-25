package enshu.censoerd.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebIconDatabase;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {

    private SurfaceView mySurfaceView, mySurfaceView2;
    private Camera myCamera; //hardware
    private Bitmap myBitmap;
    private int[] myGray;
    public int tau;
    private int WIDTH, HEIGHT;
    private SurfaceHolder holder, holder2;
    NumberPicker npicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SurfaceView
        mySurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mySurfaceView2 = (SurfaceView) findViewById(R.id.surface_view2);
        //listner�̒ǉ�
        mySurfaceView.setOnClickListener(onSurfaceClickListener);
        //SurfaceHolder(SV�̐���Ɏg��Interface�j
        holder = mySurfaceView.getHolder();
        holder2 = mySurfaceView2.getHolder();
        //�R�[���o�b�N��ݒ�
        holder.addCallback(callback);
        holder2.addCallback(callback2);
        //Number Picker�̐ݒ�
        npicker = (NumberPicker) findViewById(R.id.numberPicker);
        npicker.setMaxValue(255);
        npicker.setMinValue(0);
        tau = 27;
        npicker.setValue(tau);
        npicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                tau = npicker.getValue();
            }
        });
    }

    //�R�[���o�b�N
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

            //CameraOpen
            myCamera = Camera.open();
            myCamera.setDisplayOrientation(90);

            //�o�͂�SurfaceView�ɐݒ�
            try {
                //����̑����
                myCamera.setPreviewDisplay(surfaceHolder);
                //���������g��
                myCamera.setPreviewCallback(normalpreviewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
            //�œK�ȃT�C�Y���擾
            Camera.Parameters params = myCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
            WIDTH = optimalSize.width;
            HEIGHT = optimalSize.height;
            HEIGHT = 240;
            WIDTH = 320;
            params.setPreviewSize(WIDTH, HEIGHT);
            myCamera.setParameters(params);
            int size = WIDTH * HEIGHT *
                    ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
            myGray = new int[WIDTH * HEIGHT];
            myBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            // ���C�A�E�g����
            ViewGroup.LayoutParams layoutParams = mySurfaceView.getLayoutParams();
            layoutParams.width = HEIGHT;
            layoutParams.height = WIDTH;
            mySurfaceView.setLayoutParams(layoutParams);
            // �e�L�X�g�r���[�̃e�L�X�g��ݒ肵�܂�
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText("width:" + String.valueOf(layoutParams.width) + "\n" + "height:" + String.valueOf(layoutParams.height));
            //�v���r���[�X�^�[�g�iChanged�͍ŏ��ɂ�1�x�͌Ă΂��j
            myCamera.startPreview();

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            //�Еt��
            myCamera.release();
            myCamera = null;
        }
    };

    private SurfaceHolder.Callback callback2 = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
            ViewGroup.LayoutParams layoutParams = mySurfaceView2.getLayoutParams();
            layoutParams.width = HEIGHT;
            layoutParams.height = WIDTH;
            mySurfaceView2.setLayoutParams(layoutParams);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    //�ȉ��ASTEP2�Œǉ�

    //surface���N���b�N�����Ƃ�
    private View.OnClickListener onSurfaceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (myCamera != null) {
                //auto focus�����s
                myCamera.autoFocus(autoFocusCallback);
            }
        }
    };

    //autofocus�����Ƃ�
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            //preview���떇�؂���
            camera.setOneShotPreviewCallback(previewCallback);
        }
    };

    private final Camera.PreviewCallback normalpreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            //�v���r���[�̃t�H�[�}�b�g��YUV�Ȃ̂ŁAYUV��Bmp�ɕϊ�����K�v������i��₱�����j
            int[] frame = myGray;
          /*for (int i = 0; i < frame.length; i ++) {
              int gray = bytes[i] & 0xff;
              // ���x��ARGB�ɕϊ����܂�
              // �Ⴆ�Ζ��x��30�Ȃ�AA=255, R=30, G=30, B=30�Ƃ��Ă��܂�
              frame[i] = 0xff000000 | gray << 16 | gray << 8 | gray;
          }*/
            YUV_NV21_TO_RGB(frame, bytes, WIDTH, HEIGHT, tau);
            // int[]��Bitmap�ɕϊ�����
            myBitmap.setPixels(myGray, 0, WIDTH, 0, 0,
                    WIDTH, HEIGHT);
            Matrix mat = new Matrix();
            mat.postRotate(90);
            Bitmap myBitmap2 = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), mat, true);
            Canvas c = holder2.lockCanvas();
            c.drawBitmap(myBitmap2, 0, 0, null);
            holder2.unlockCanvasAndPost(c);
            Log.d("test", "[PreviewCallBack]W:" + String.valueOf(myBitmap2));
        }
    };

    //�؂��������i�����ŎB�e�A�e��摜�������s���j
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        //OnShotPreview����byte[]���n���Ă���
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            String res = MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap, "pohe.jpg", null);
            Toast.makeText(getApplicationContext(), "Get Focus." + res, Toast.LENGTH_LONG).show();
        }
    };

    //ApiDemo�ł悭�g��getOptimalPreviewSize << STEP4
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    ;


    //encoder
    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height, int tau) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                int skin = r - g < r - b ? r - g : r - b;
                //argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
                if (skin > tau)
                    argb[a++] = 0xffffffff;
                else
                    argb[a++] = 0xff000000;//0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }
}
