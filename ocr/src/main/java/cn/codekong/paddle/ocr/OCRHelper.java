package cn.codekong.paddle.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OCRHelper {

    /**
     * 从assets中复制文件到指定路径
     *
     * @param appCtx
     * @param srcPath
     * @param dstPath
     */
    public static void copyFileFromAssets(Context appCtx, String srcPath, String dstPath) {
        if (srcPath.isEmpty() || dstPath.isEmpty()) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(appCtx.getAssets().open(srcPath));
            os = new BufferedOutputStream(new FileOutputStream(dstPath));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从assets中复制目录到指定目录
     *
     * @param appCtx
     * @param srcDir
     * @param dstDir
     */
    public static void copyDirectoryFromAssets(Context appCtx, String srcDir, String dstDir) {
        if (srcDir.isEmpty() || dstDir.isEmpty()) {
            return;
        }
        try {
            if (!new File(dstDir).exists()) {
                new File(dstDir).mkdirs();
            }
            for (String fileName : appCtx.getAssets().list(srcDir)) {
                String srcSubPath = srcDir + File.separator + fileName;
                String dstSubPath = dstDir + File.separator + fileName;
                if (new File(srcSubPath).isDirectory()) {
                    copyDirectoryFromAssets(appCtx, srcSubPath, dstSubPath);
                } else {
                    // TODO 不严谨,简单优化一下,如果目标文件已经存在并且长度与原文件相同则不拷贝
                    File dstFile = new File(dstSubPath);
                    if (dstFile.exists() && dstFile.length() == appCtx.getAssets().open(srcSubPath).available()) {
                        return;
                    }
                    copyFileFromAssets(appCtx, srcSubPath, dstSubPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 缩放图片宽高为step的整数倍
     *
     * @param bitmap
     * @param maxLength
     * @param step
     * @return
     */
    public static Bitmap resizeWithStep(Bitmap bitmap, int maxLength, int step) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWH = Math.max(width, height);
        int newWidth = width;
        int newHeight = height;
        if (maxWH > maxLength) {
            float ratio = maxLength * 1.0f / maxWH;
            newWidth = (int) Math.floor(ratio * width);
            newHeight = (int) Math.floor(ratio * height);
        }

        newWidth = newWidth - newWidth % step;
        if (newWidth == 0) {
            newWidth = step;
        }
        newHeight = newHeight - newHeight % step;
        if (newHeight == 0) {
            newHeight = step;
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
