package com.mrshiehx.file.manager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        Drawable d = (Drawable) bd;
        return d;
    }
}