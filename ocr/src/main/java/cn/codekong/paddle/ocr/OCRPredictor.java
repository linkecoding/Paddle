package cn.codekong.paddle.ocr;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * OCR预测器
 */
public class OCRPredictor {
    public boolean isLoaded = false;
    protected OCRPredictorNative mPaddlePredictorNative;
    protected long inferenceTime = 0;
    protected Vector<String> wordLabels = new Vector<>();
    protected long[] inputShape = OCRConfig.INPUT_SHAPE;
    protected float[] inputMean = OCRConfig.INPUT_MEAN;
    protected float[] inputStd = OCRConfig.INPUT_STD;
    protected Bitmap inputImage;
    protected float preprocessTime = 0;
    protected float postProcessTime = 0;

    public boolean init(Context appCtx) {
        return init(appCtx, OCRConfig.ASSETS_MODEL_DIR_PATH, OCRConfig.ASSETS_LABEL_FILE_PATH, OCRConfig.DEFAULT_CPU_THREAD, OCRConfig.DEFAULT_CPU_RUN_MODE);
    }

    /**
     * 初始化
     *
     * @param appCtx
     * @param modelPath
     * @param labelPath
     * @return
     */
    public boolean init(Context appCtx, String modelPath, String labelPath) {
        return init(appCtx, modelPath, labelPath, OCRConfig.DEFAULT_CPU_THREAD, OCRConfig.DEFAULT_CPU_RUN_MODE);
    }

    /**
     * 初始化
     *
     * @param appCtx
     * @param modelPath
     * @param labelPath
     * @param cpuThreadNum
     * @param cpuPowerMode
     * @return
     */
    public boolean init(Context appCtx, String modelPath, String labelPath, int cpuThreadNum, String cpuPowerMode) {
        isLoaded = loadModel(appCtx, modelPath, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }

    /**
     * 加载模型
     *
     * @param appCtx
     * @param modelPath
     * @param cpuThreadNum
     * @param cpuPowerMode
     * @return
     */
    protected boolean loadModel(Context appCtx, String modelPath, int cpuThreadNum, String cpuPowerMode) {
        // 释放之前的模型
        release();
        if (modelPath.isEmpty()) {
            return false;
        }
        String realPath = modelPath;
        if (modelPath.charAt(0) != '/') {
            // 如果路径以'/'开头则直接读取,都则认为是assets路径,从assets拷贝到用户缓存目录
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            OCRHelper.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }

        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.cpuThreadNum = cpuThreadNum;
        config.detModelFileName = realPath + File.separator + OCRConfig.DET_MODEL_NAME;
        config.recModelFileName = realPath + File.separator + OCRConfig.REC_MODEL_NAME;
        config.clsModelFileName = realPath + File.separator + OCRConfig.CLS_MODEL_NAME;
        config.cpuPower = cpuPowerMode;
        mPaddlePredictorNative = new OCRPredictorNative(config);
        return true;
    }

    /**
     * 释放模型
     */
    public void release() {
        if (mPaddlePredictorNative != null) {
            mPaddlePredictorNative.destroy();
            mPaddlePredictorNative = null;
        }
        isLoaded = false;
    }

    /**
     * 加载文本标签
     *
     * @param appCtx
     * @param labelPath
     * @return
     */
    protected boolean loadLabel(Context appCtx, String labelPath) {
        wordLabels.clear();
        wordLabels.add("black");
        // 从文件中加载标签
        try {
            InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
            int available = assetsInputStream.available();
            byte[] lines = new byte[available];
            assetsInputStream.read(lines);
            assetsInputStream.close();
            String words = new String(lines);
            String[] contents = words.split("\n");
            wordLabels.addAll(Arrays.asList(contents));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<OCRResultModel> runOcr() {
        if (inputImage == null || !isLoaded()) {
            return Collections.emptyList();
        }

        // 预处理图片数据,为下一步喂到模型预测做准备
        Bitmap scaleImage = OCRHelper.resizeWithStep(inputImage, Long.valueOf(inputShape[2]).intValue(), 32);

        Date start = new Date();
        int channels = (int) inputShape[1];
        int width = scaleImage.getWidth();
        int height = scaleImage.getHeight();
        float[] inputData = new float[channels * width * height];
        if (channels == 3) {
            int[] channelIdx = OCRConfig.CHANNEL_IDX;
            int[] channelStride = new int[]{width * height, width * height * 2};
            int[] pixels = new int[width * height];
            scaleImage.getPixels(pixels, 0, scaleImage.getWidth(), 0, 0, scaleImage.getWidth(), scaleImage.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];
                float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                        (float) blue(color) / 255.0f};
                inputData[i] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                inputData[i + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                inputData[i + channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];
            }
        }
        Date end = new Date();
        preprocessTime = (float) (end.getTime() - start.getTime());

        // 进行预测
        start = new Date();
        List<OCRResultModel> results = mPaddlePredictorNative.runImage(inputData, width, height, channels, inputImage);
        end = new Date();
        inferenceTime = end.getTime() - start.getTime();
        // 从字索引转换为字
        postProcess(results);
        return results;
    }


    public boolean isLoaded() {
        return mPaddlePredictorNative != null && isLoaded;
    }

    public long inferenceTime() {
        return inferenceTime;
    }

    public Bitmap inputImage() {
        return inputImage;
    }

    public float preprocessTime() {
        return preprocessTime;
    }

    public float postProcessTime() {
        return postProcessTime;
    }


    public void setInputImage(Bitmap image) {
        if (image == null) {
            return;
        }
        this.inputImage = image.copy(Bitmap.Config.ARGB_8888, true);
    }

    private List<OCRResultModel> postProcess(List<OCRResultModel> results) {
        Date start = new Date();
        for (OCRResultModel r : results) {
            StringBuilder word = new StringBuilder();
            for (int index : r.getWordIndex()) {
                if (index >= 0 && index < wordLabels.size()) {
                    word.append(wordLabels.get(index));
                } else {
                    // TODO 识别不出的字符怎么定义
                    word.append("×");
                }
            }
            r.setLabel(word.toString());
        }
        Date end = new Date();
        this.postProcessTime = end.getTime() - start.getTime();
        return results;
    }
    
    public List<OcrResult> transformData(List<OcrResultModel> OcrResultModelList) {
        if (OcrResultModelList == null) {
            return Collections.emptyList();
        }
        List<OcrResult> words_result = new ArrayList<>();
        for (OcrResultModel model : OcrResultModelList) {
            List<Point> pointList = model.getPoints();
            if (pointList.isEmpty()) {
                continue;
            }
            Point firstPoint = pointList.get(0);
            int left = firstPoint.x;
            int top = firstPoint.y;
            int right = firstPoint.x;
            int bottom = firstPoint.y;
            for (Point p : pointList) {
                if (p.x < left) {
                    left = p.x;
                }
                if (p.x > right) {
                    right = p.x;
                }
                if (p.y < top) {
                    top = p.y;
                }
                if (p.y > bottom) {
                    bottom = p.y;
                }
            }
            OcrResult ocrResult = new OcrResult();
            ocrResult.preprocessTime = preprocessTime;
            ocrResult.inferenceTime = inferenceTime;
            ocrResult.confidence = model.getConfidence();
            ocrResult.words = model.getLabel().trim().replace("\r", "");
            ocrResult.location = new OcrResult.RectLocation(left, top, Math.abs(right - left), Math.abs(bottom - top));
            ocrResult.bounds = new Rect(left, top, right, bottom);
            words_result.add(ocrResult);
        }
        Collections.sort(words_result);
        return words_result;
    }

    public List<OcrResult> ocr() {
        if (inputImage == null) {
            return Collections.emptyList();
        }
        // Pre-process image, and feed input tensor with pre-processed data
        Bitmap scaleImage = Utils.resizeWithStep(this.inputImage, Long.valueOf(inputShape[2]).intValue(), 32);
        Date start = new Date();
        int channels = (int) inputShape[1];
        int width = scaleImage.getWidth();
        int height = scaleImage.getHeight();
        float[] inputData = new float[channels * width * height];
        if (channels == 3) {
            int[] channelIdx = null;
            if (inputColorFormat.equalsIgnoreCase("RGB")) {
                channelIdx = new int[]{0, 1, 2};
            } else if (inputColorFormat.equalsIgnoreCase("BGR")) {
                channelIdx = new int[]{2, 1, 0};
            } else {
                Log.i(TAG, "Unknown color format " + inputColorFormat + ", only RGB and BGR color format is " +
                        "supported!");
                return Collections.emptyList();
            }
            int[] channelStride = new int[]{width * height, width * height * 2};
            int[] pixels = new int[width * height];
            scaleImage.getPixels(pixels, 0, scaleImage.getWidth(), 0, 0, scaleImage.getWidth(), scaleImage.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];
                float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                        (float) blue(color) / 255.0f};
                inputData[i] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                inputData[i + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                inputData[i + channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];
            }
        } else if (channels == 1) {
            int[] pixels = new int[width * height];
            scaleImage.getPixels(pixels, 0, scaleImage.getWidth(), 0, 0, scaleImage.getWidth(), scaleImage.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];
                float gray = (float) (red(color) + green(color) + blue(color)) / 3.0f / 255.0f;
                inputData[i] = (gray - inputMean[0]) / inputStd[0];
            }
        } else {
            Log.i(TAG, "Unsupported channel size " + Integer.toString(channels) + ",  only channel 1 and 3 is " +
                    "supported!");
            return Collections.emptyList();
        }
        float[] pixels = inputData;
        Log.i(TAG, "pixels " + pixels[0] + " " + pixels[1] + " " + pixels[2] + " " + pixels[3]
                + " " + pixels[pixels.length / 2] + " " + pixels[pixels.length / 2 + 1] + " " + pixels[pixels.length - 2] + " " + pixels[pixels.length - 1]);
        Date end = new Date();
        preprocessTime = (float) (end.getTime() - start.getTime());

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        }
        warmupIterNum = 0; // do not need warm
        // Run inference
        start = new Date();
        ArrayList<OcrResultModel> results = paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        end = new Date();
        inferenceTime = (float) (end.getTime() - start.getTime());
        results = postprocess(results);
        return transformData(results);
    }
}
