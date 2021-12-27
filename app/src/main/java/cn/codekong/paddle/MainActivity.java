package cn.codekong.paddle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import cn.codekong.paddle.ocr.OCRPredictor;
import cn.codekong.paddle.ocr.OCRPredictorNative;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OCRPredictor predictor = new OCRPredictor();
        OCRPredictorNative.loadLibrary();
        Log.e(TAG, "onCreate: " + predictor);
    }
}