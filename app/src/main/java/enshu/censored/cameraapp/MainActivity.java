package enshu.censored.cameraapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import enshu.censoerd.cameraapp.R;

import static java.util.Arrays.fill;
import static java.util.Arrays.toString;


public class MainActivity extends Activity {

    private SurfaceView mySurfaceView, mySurfaceView2,mySurfaceView3;
    private Camera myCamera; //hardware
    private Bitmap myBitmap,myBitmap2,myBitmap3,myBitmap4;
    private BitArray myGray;
    private BitArray Mono;
    public int tau;
    private int WIDTH, HEIGHT;
    private int[] output;
    private SurfaceHolder holder, holder2,holder3;
    NumberPicker npicker;
    private boolean enableNoiseRejection;
    private DistanceRandomForest DRF;
    private ShapeRandomForest SRF;
    TextView textView2,textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SurfaceView
        mySurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mySurfaceView2 = (SurfaceView) findViewById(R.id.surface_view2);
        mySurfaceView3 = (SurfaceView) findViewById(R.id.surface_view3);
        mySurfaceView.setOnClickListener(onSurfaceClickListener);
        mySurfaceView2.setOnClickListener(onSurface2ClickListener);
        holder = mySurfaceView.getHolder();
        holder2 = mySurfaceView2.getHolder();
        holder3 = mySurfaceView3.getHolder();
        holder.addCallback(callback);
        holder2.addCallback(callback2);
        holder3.addCallback(callback3);
        npicker = (NumberPicker) findViewById(R.id.numberPicker);
        npicker.setMaxValue(255);
        npicker.setMinValue(0);
        tau = 10;
        npicker.setValue(tau);
        npicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                tau = npicker.getValue();
            }
        });
        initializeCheckBox();
    }

    private void initializeCheckBox(){
        CheckBox checkBox = (CheckBox) findViewById(R.id.RefineCheckBox);
        // チェックボックスのチェ�?ク状態を設定しま�?
        checkBox.setChecked(false);
        // チェ�?クボックスがクリ�?クされた時に呼び出されるコールバックリスナ�?�を登録しま�?
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            // チェ�?クボックスがクリ�?クされた時に呼び出されま�?
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                // チェ�?クボックスのチェ�?ク状態を取得しま�?
                enableNoiseRejection = checkBox.isChecked();
                Toast.makeText(MainActivity.this,
                        "onClick():" + String.valueOf(enableNoiseRejection),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

            try {
                //CameraOpen
                myCamera = Camera.open();
                myCamera.setDisplayOrientation(90);
                myCamera.setPreviewDisplay(surfaceHolder);
                myCamera.setPreviewCallback(normalpreviewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
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
            myGray = new BitArray(WIDTH * HEIGHT);
            Mono = new BitArray(WIDTH * HEIGHT / 16);
            myBitmap = Bitmap.createBitmap(HEIGHT/4, WIDTH/4, Bitmap.Config.ARGB_8888);
            myBitmap3 = Bitmap.createBitmap(HEIGHT/4, WIDTH/4, Bitmap.Config.ARGB_8888);
            ViewGroup.LayoutParams layoutParams = mySurfaceView.getLayoutParams();
            layoutParams.width = HEIGHT;
            layoutParams.height = WIDTH;
            mySurfaceView.setLayoutParams(layoutParams);
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText("width:" + String.valueOf(layoutParams.width) + "\n" + "height:" + String.valueOf(layoutParams.height));
            textView2 = (TextView) findViewById(R.id.textView2);
            textView3 = (TextView) findViewById(R.id.textView3);
            myCamera.startPreview();
            output = new int[WIDTH/4*HEIGHT/4];
            AssetManager assetManager = getResources().getAssets();
            InputStream iDRF = null;
            try {
                iDRF = assetManager.open("DRF.dat");
                Log.d("DRF","opened \"DRF.dat\"");
            }catch (Exception e){
                Log.e("open(\"DRF.dat\")",Log.getStackTraceString(e));
            }
            DRF = new DistanceRandomForest(iDRF,output,HEIGHT/4,WIDTH/4);
            InputStream iSRF = null;
            try {
                iSRF = assetManager.open("SRF.dat");
                Log.d("SRF","opened \"SRF.dat\"");
            }catch (Exception e){
                Log.e("open(\"SRF.dat\")",Log.getStackTraceString(e));
            }
            SRF = new ShapeRandomForest(iSRF,output,HEIGHT/4,WIDTH/4);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
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

    private SurfaceHolder.Callback callback3 = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
            ViewGroup.LayoutParams layoutParams = mySurfaceView3.getLayoutParams();
            layoutParams.width = HEIGHT;
            layoutParams.height = WIDTH;
            mySurfaceView3.setLayoutParams(layoutParams);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        }
    };

    private View.OnClickListener onSurfaceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (myCamera != null) {
                myCamera.autoFocus(autoFocusCallback);
            }
        }
    };

    private View.OnClickListener onSurface2ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                // sdcardフォルダを指定
                final String SAVE_DIR = "/MyPhoto/";
                File root = new File(Environment.getExternalStorageDirectory().getPath() + SAVE_DIR);
                try{
                    if(!root.exists()){
                        root.mkdir();
                    }
                }catch(SecurityException e){
                    e.printStackTrace();
                    throw e;
                }
                // 日付でファイル名を作成　
                Date mDate = new Date();
                SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String fn = fileName.format(mDate)+".png";
                File AttachName = new File(root,fn);
                FileOutputStream out = new FileOutputStream(AttachName);
                myBitmap2.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                Toast.makeText(getApplicationContext(), "Save " + AttachName.toString(), Toast.LENGTH_LONG).show();

                ContentValues values = new ContentValues();
                ContentResolver contentResolver = getContentResolver();
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.TITLE, fn);
                values.put("_data", AttachName.toString());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            camera.setOneShotPreviewCallback(previewCallback);
        }
    };

    private final Camera.PreviewCallback normalpreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            YUV_NV21_TO_RGB(myGray, bytes, WIDTH, HEIGHT, tau);
            int width = HEIGHT / 4;
            int height = WIDTH / 4;
            quarter_and_rotate(myGray, Mono, WIDTH, HEIGHT);
            int[] frame;
            if (enableNoiseRejection) {
                ConnectedComponentProcess CCP = new ConnectedComponentProcess(Mono, width, height);
                CCP.deleteNoise();
            }
            frame = BIT_TO_INT(Mono);
            DRF.classify(Mono);
            int sum = 1;
            int max_k = 2;
            int max_genre = 0;
            for (int k = 1; k < 4; k++){
                sum += DRF.genre[k];
                if (max_genre < DRF.genre[k]){
                    max_k = k;
                    max_genre = DRF.genre[k];
                }
            }
            if (max_k == 2) {//if reasonable
                sum = 1;
                max_k = 0;
                max_genre = 0;
                SRF.classify(Mono);
                for (int k = 1; k < 5; k++){
                    sum += SRF.genre[k];
                    if (max_genre < SRF.genre[k]){
                        max_k = k;
                        max_genre = SRF.genre[k];
                    }
                }
                textView2.setText("Rock:\t"+100* SRF.genre[1]/sum+"%"
                        +"\nScissors:\t"+100* SRF.genre[2]/sum+"%"
                        +"\nPaper:\t"+100* SRF.genre[3]/sum+"%"
                        +"\nNo Gesture:\t"+100* SRF.genre[4]/sum+"%");
                textView3.setText("Shape:" + Shape.valueOf(max_k));
            }
            else {
                textView2.setText("Too Close:\t"+100* DRF.genre[1]/sum+"%"
                        +"\nReasonable:\t"+100* DRF.genre[2]/sum+"%"
                        +"\nToo Far:\t"+100* DRF.genre[3]/sum+"%");
                textView3.setText("Distance:" + Distance.valueOf(max_k));
            }
            myBitmap.setPixels(frame, 0, width, 0, 0, width, height);
            myBitmap3.setPixels(output, 0, width, 0, 0, width, height);
            myBitmap2 = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(),null, true);
            myBitmap4 = Bitmap.createBitmap(myBitmap3, 0, 0, myBitmap3.getWidth(), myBitmap3.getHeight(), null,true);
            Rect src = new Rect(0, 0, myBitmap4.getWidth(), myBitmap4.getHeight());
            Rect dst = new Rect(0, 0, myBitmap4.getWidth()*4, myBitmap4.getHeight()*4);
            Canvas c3 = holder2.lockCanvas();
            c3.drawBitmap(myBitmap2, src, dst, null);
            holder2.unlockCanvasAndPost(c3);
            c3 = holder3.lockCanvas();
            c3.drawBitmap(myBitmap4, src, dst, null);
            holder3.unlockCanvasAndPost(c3);
        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            //String res = MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap2, "pohe.jpg", null);
            Toast.makeText(getApplicationContext(), "Get Focus.", Toast.LENGTH_LONG).show();
        }
    };

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

    public int[] BIT_TO_INT(BitArray b){
        int[] i = new int[b.length()];
        for (int k = 0; k < b.length(); k++)
            i[k] = b.get(k) ? 0xFFFFFFFF : 0xFF000000;
        return i;
    }
    private void quarter_and_rotate(BitArray before,BitArray after,int width,int height){
        for (int y = 0 ; y < height/4; y++)
            for (int x = 0; x < width/4; x++){
                int sum = 0;
                for (int dy = 0; dy < 4; dy++)
                    for (int dx = 0; dx < 4; dx++) {
                        if (before.get(4 * x + dx + width * (4 * y + dy))) sum++;
                    }
                after.set((height/4 - 1 - y)+height/4*x,sum >= 8);
            }
    }

    //encoder
    public static void YUV_NV21_TO_RGB(BitArray bw, byte[] yuv, int width, int height, int tau) {
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
                    bw.set(a++,true);
                else
                    bw.set(a++,false);//0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }
}
