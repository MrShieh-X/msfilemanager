package com.mrshiehx.file.manager.utils;

public class NativeUtils {
    public static final NativeUtils NATIVE_UTILS = new NativeUtils();

    static {
        System.loadLibrary("msfilemanager");
    }

    public native boolean isDirectory(String path);
}
