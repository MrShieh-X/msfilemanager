package com.mrshiehx.file.manager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import com.mrshiehx.file.manager.application.MSFMApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/***
 * 图像格式转换
 * @author kristain
 * @from https://www.cnblogs.com/kristain/articles/3340558.html
 */
public class ImageUtils {
    // 将byte[]转换成InputStream
    public static InputStream byte2InputStream(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return bais;
    }

    // 将InputStream转换成byte[]
    public static byte[] inputStream2Bytes(InputStream is) throws IOException {
        StringBuilder str = new StringBuilder();
        byte[] readByte = new byte[1024];
        int readCount = -1;
        while ((readCount = is.read(readByte, 0, 1024)) != -1) {
            str.append(new String(readByte).trim());
        }
        return str.toString().getBytes();
    }

    public static InputStream bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    public static InputStream bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    public static Bitmap inputStream2Bitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }

    public static InputStream drawable2InputStream(Drawable d) {
        Bitmap bitmap = drawable2Bitmap(d);
        return bitmap2InputStream(bitmap);
    }

    public static Drawable inputStream2Drawable(InputStream is) {
        Bitmap bitmap = inputStream2Bitmap(is);
        return bitmap2Drawable(bitmap);
    }

    public static byte[] drawable2Bytes(Drawable d) {
        Bitmap bitmap = drawable2Bitmap(d);
        return bitmap2Bytes(bitmap);
    }

    public static Drawable bytes2Drawable(byte[] b) {
        Bitmap bitmap = bytes2Bitmap(b);
        return bitmap2Drawable(bitmap);
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(MSFMApplication.getContext().getResources(), bitmap);
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param bytes  图像字节
     * @param width  指定输出图像的宽度
     * @param height 指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(byte[] bytes, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = Math.min(beWidth, beHeight);
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}