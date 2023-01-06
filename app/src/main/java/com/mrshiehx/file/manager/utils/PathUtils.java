package com.mrshiehx.file.manager.utils;

public class PathUtils {
    public static String toDirectoryPath(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    public static String removeLastSeparator(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String toAbsolutePath(String basePath, String relativePath) {
        String result;
        if (relativePath.startsWith("../")) {//此必须为首判断
            if (basePath.equals("/")) {
                result = "/" + relativePath.substring(3);
            } else {
                basePath = removeLastSeparator(basePath);
                basePath = basePath.substring(0, basePath.lastIndexOf('/') + 1);//with / at the end
                result = basePath + relativePath.substring(3);
            }
        } else if (relativePath.startsWith("./")) {
            result = toDirectoryPath(basePath) + relativePath.substring(2);
        } else if (!relativePath.startsWith("/")) {
            result = toDirectoryPath(basePath) + relativePath;
        } else result = relativePath;
        return result.replaceAll("/+", "/");
    }
}
