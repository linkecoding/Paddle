package cn.codekong.paddle.ocr;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NDK代理类
 */
public class OCRPredictorNative {

    private static final AtomicBoolean isSOLoaded = new AtomicBoolean();
    private long nativePointer;

    public static void loadLibrary() throws RuntimeException {
        if (!isSOLoaded.get() && isSOLoaded.compareAndSet(false, true)) {
            try {
                System.loadLibrary("paddle_ocr_native");
            } catch (Throwable e) {
                throw new RuntimeException(
                        "Load libpaddle_ocr_native.so failed, please check it exists in apk file.", e);
            }
        }
    }

    public OCRPredictorNative(Config config) {
        loadLibrary();
        nativePointer = init(config.detModelFileName, config.recModelFileName, config.clsModelFileName,
                config.cpuThreadNum, config.cpuPower);
    }

    public List<OCRResultModel> runImage(float[] inputData, int width, int height, int channels, Bitmap originalImage) {
        float[] dims = new float[]{1, channels, height, width};
        float[] rawResults = forward(nativePointer, inputData, dims, originalImage);
        return postProcess(rawResults);
    }

    public static class Config {
        // cpu线程数
        public int cpuThreadNum;
        // cpu运行模式
        public String cpuPower;
        // 文本检测模型文件名
        public String detModelFileName;
        // 文本识别模型文件名
        public String recModelFileName;
        // 文本方向分类模型文件名
        public String clsModelFileName;

    }

    public void destroy() {
        if (nativePointer > 0) {
            release(nativePointer);
            nativePointer = 0;
        }
    }

    protected native long init(String detModelPath, String recModelPath, String clsModelPath, int threadNum, String cpuMode);

    protected native float[] forward(long pointer, float[] buf, float[] ddims, Bitmap originalImage);

    protected native void release(long pointer);

    /**
     * 原始rawData数据处理
     * @param raw
     * @return
     */
    private List<OCRResultModel> postProcess(float[] raw) {
        List<OCRResultModel> results = new ArrayList<>();
        int begin = 0;
        while (begin < raw.length) {
            int pointNum = Math.round(raw[begin]);
            int wordNum = Math.round(raw[begin + 1]);
            OCRResultModel model = parse(raw, begin + 2, pointNum, wordNum);
            begin += 2 + 1 + pointNum * 2 + wordNum;
            results.add(model);
        }
        return results;
    }

    /**
     * 从二进制数据中解析处出信息
     * [置信度][(point1, point2)...][wordIndex1, wordIndex2...]
     * @param raw
     * @param begin
     * @param pointNum
     * @param wordNum
     * @return
     */
    private OCRResultModel parse(float[] raw, int begin, int pointNum, int wordNum) {
        int current = begin;
        OCRResultModel model = new OCRResultModel();
        model.setConfidence(raw[current]);
        current++;
        for (int i = 0; i < pointNum; i++) {
            model.addPoints(Math.round(raw[current + i * 2]), Math.round(raw[current + i * 2 + 1]));
        }
        current += (pointNum * 2);
        for (int i = 0; i < wordNum; i++) {
            int index = Math.round(raw[current + i]);
            model.addWordIndex(index);
        }
        return model;
    }
}
