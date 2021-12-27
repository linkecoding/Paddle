package cn.codekong.paddle.ocr;

import java.io.File;

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
}
