package cn.codekong.paddle.ocr;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class OCRResultModel {
    // 文本边框点信息
    private List<Point> points;
    // 识别出的字在label中的索引
    private List<Integer> wordIndex;
    // 识别结果
    private String label;
    // 识别置信度
    private float confidence;

    public OCRResultModel() {
        super();
        points = new ArrayList<>();
        wordIndex = new ArrayList<>();
    }

    public void addPoints(int x, int y) {
        Point point = new Point(x, y);
        points.add(point);
    }

    public void addWordIndex(int index) {
        wordIndex.add(index);
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<Integer> getWordIndex() {
        return wordIndex;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
