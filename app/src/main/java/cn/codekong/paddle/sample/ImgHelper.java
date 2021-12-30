package cn.codekong.paddle.sample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import java.util.List;

import cn.codekong.paddle.ocr.OCRResultModel;

/**
 * Created by linke on 2021/12/31
 */
public class ImgHelper {
    /**
     * 绘制识别结果到Bitmap，同时返回识别结果
     *
     * @param results
     */
    public static String drawResults(List<OCRResultModel> results, Bitmap srcImg) {
        if (results.isEmpty() || srcImg == null) {
            return "";
        }
        StringBuffer outputResultSb = new StringBuffer("");
        for (int i = 0; i < results.size(); i++) {
            OCRResultModel result = results.get(i);
            StringBuilder sb = new StringBuilder("");
            sb.append(result.getLabel());
            sb.append(" ").append(result.getConfidence());
            sb.append("; Points: ");
            for (Point p : result.getPoints()) {
                sb.append("(").append(p.x).append(",").append(p.y).append(") ");
            }
            outputResultSb.append(i + 1).append(": ").append(result.getLabel()).append("\n");
        }
        String outputResult = outputResultSb.toString();
        Canvas canvas = new Canvas(srcImg);
        Paint paintFillAlpha = new Paint();
        paintFillAlpha.setStyle(Paint.Style.FILL);
        paintFillAlpha.setColor(Color.parseColor("#3B85F5"));
        paintFillAlpha.setAlpha(50);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#3B85F5"));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        for (OCRResultModel result : results) {
            Path path = new Path();
            List<Point> points = result.getPoints();
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = points.size() - 1; i >= 0; i--) {
                Point p = points.get(i);
                path.lineTo(p.x, p.y);
            }
            canvas.drawPath(path, paint);
            canvas.drawPath(path, paintFillAlpha);
        }
        return outputResult;
    }
}
