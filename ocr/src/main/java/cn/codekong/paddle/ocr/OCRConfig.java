package cn.codekong.paddle.ocr;

/**
 * OCR配置信息
 * Created by linke on 2021/12/27
 */
public class OCRConfig {
    // 模型在assets中的目录
    public static final String ASSETS_MODEL_DIR_PATH = "models/ocr_v2_for_cpu";
    // 文本标签在assets中的路径
    public static final String ASSETS_LABEL_FILE_PATH = "labels/ppocr_keys_v1.txt";

    // 默认CPU线程数
    public static final int DEFAULT_CPU_THREAD = 4;
    // 默认CPU运行模式
    public static final String DEFAULT_CPU_RUN_MODE = "LITE_POWER_HIGH";

    // 检测模型文件名
    public static final String DET_MODEL_NAME = "ch_ppocr_mobile_v2.0_det_opt.nb";
    // 识别模型文件名
    public static final String REC_MODEL_NAME = "ch_ppocr_mobile_v2.0_rec_opt.nb";
    // 方向分类模型文件名
    public static final String CLS_MODEL_NAME = "ch_ppocr_mobile_v2.0_cls_opt.nb";

    // 输入图片的通道(固定为3)
    public static final int IMG_DATA_CHANNELS = 3;
    // 输入图片颜色格式
    public static final String IMG_INPUT_COLOR_FORMAT = "BGR";
    // 与上面颜色模式对应(RGB颜色分量的在颜色值(rgb)中的索引)
    public static final int[] CHANNEL_IDX = new int[]{2, 1, 0};

    // 图片缩放限制最大大小
    public static final int IMG_DATA_MAC_SIZE = 960;
    // 输入数据shape
    public static final long[] INPUT_SHAPE = new long[]{1, 3, 960};
    // 超参数[均值]
    public static final float[] INPUT_MEAN = new float[]{0.485f, 0.456f, 0.406f};
    // 超参数[标准差]
    public static final float[] INPUT_STD = new float[]{0.229f, 0.224f, 0.225f};


}
