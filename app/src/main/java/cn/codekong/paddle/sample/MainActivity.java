package cn.codekong.paddle.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.codekong.paddle.R;
import cn.codekong.paddle.ocr.OCRConfig;
import cn.codekong.paddle.ocr.OCRPredictor;
import cn.codekong.paddle.ocr.OCRResultModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int LOAD_MODEL = 1;
    public static final int REC_IMG = 2;
    private volatile OCRPredictor mOCRPredictor;
    private Handler mActionHandler;
    private HandlerThread mWorker;
    private ImageView mRecResultImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEvents();
        initViews();
    }

    private void initEvents() {

        mWorker = new HandlerThread("OCR-HandlerThread");
        mWorker.start();
        mActionHandler = new Handler(mWorker.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case LOAD_MODEL:
                        boolean initRes = loadModel();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, initRes ? "初始化模型成功" : "模型初始化失败", Toast.LENGTH_SHORT).show());
                        break;
                    case REC_IMG:
                        List<OCRResultModel> recRes = recImg();
                        if (recRes != null && !recRes.isEmpty()) {
                            Bitmap image = ((BitmapDrawable) mRecResultImg.getDrawable()).getBitmap();
                            image = image.copy(Bitmap.Config.ARGB_8888, true);
                            String recWords = ImgHelper.drawResults(recRes, image);
                            Log.d(TAG, "ocr words = " + recWords);
                            Bitmap finalImage = image;
                            runOnUiThread(() -> mRecResultImg.setImageBitmap(finalImage));
                        }
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, recRes != null ? "OCR识别成功" : "OCR识别失败", Toast.LENGTH_SHORT).show());
                        break;
                }
            }
        };

    }

    private void initViews() {
        Button loadModelBtn = findViewById(R.id.load_model);
        Button recImgBtn = findViewById(R.id.rec_img);
        loadModelBtn.setOnClickListener(v -> {
            mActionHandler.sendEmptyMessage(LOAD_MODEL);
        });
        recImgBtn.setOnClickListener(v -> {
            mActionHandler.sendEmptyMessage(REC_IMG);
        });
        mRecResultImg = findViewById(R.id.rec_result_img);
    }

    private boolean loadModel() {
        if (mOCRPredictor == null) {
            mOCRPredictor = new OCRPredictor();
        }
        return mOCRPredictor.init(App.getAppContext(), OCRConfig.ASSETS_MODEL_DIR_PATH, OCRConfig.ASSETS_LABEL_FILE_PATH);
    }

    private List<OCRResultModel> recImg() {
        Bitmap image = ((BitmapDrawable) mRecResultImg.getDrawable()).getBitmap();
        mOCRPredictor.setInputImage(image);
        if (mOCRPredictor.isLoaded()) {
            return mOCRPredictor.runModel();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String assetImagePath = "images/sample.jpeg";
        InputStream imageStream = null;
        try {
            imageStream = getAssets().open(assetImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap image = BitmapFactory.decodeStream(imageStream);
        mRecResultImg.setImageBitmap(image);
    }
}