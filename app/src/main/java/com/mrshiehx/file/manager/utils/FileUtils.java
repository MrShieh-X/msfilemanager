package com.mrshiehx.file.manager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File value : fileList) {
                if (value.isDirectory()) {
                    size = size + getFolderSize(value);
                } else {
                    size = size + value.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;

        if (size==0) {
            //return size + "B";
            return "0.00MB";
        }
        if (kiloByte < 1) {
            return (int)size + "B";
            //return "0.00MB";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    public static String getString(File target) throws IOException {
        return getString(new FileInputStream(target));
    }

    public static String getString(InputStream target) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(target, "UTF-8");
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void deleteDirectory(File directory) {
        if (directory != null) {
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (directory.listFiles() != null) {
                        if (directory.listFiles().length != 0) {
                            File[] files = directory.listFiles();
                            for (File file : files) {
                                if (file.isFile()) {
                                    file.delete();
                                } else {
                                    deleteDirectory(file);
                                }
                            }
                        }
                    }
                }
                directory.delete();
            }
        }
    }

    public static String addSeparatorToPath(String path) {
        if (!path.endsWith("/")) {
            return path + "/";
        }
        return path;
    }

    public static void copy(File source, File to) throws IOException {
        InputStream input = new FileInputStream(source);
        OutputStream output = new FileOutputStream(to);
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buf)) != -1) {
            output.write(buf, 0, bytesRead);
        }
        input.close();
        output.close();
    }

    public static String readFileContent(File file) throws IOException {
        BufferedReader reader;
        StringBuffer sbf = new StringBuffer();
        reader = new BufferedReader(new FileReader(file));
        String tempStr;
        while ((tempStr = reader.readLine()) != null) {
            sbf.append(tempStr);
        }
        reader.close();
        return sbf.toString();
    }

    public static String readFileByRandomAccess(File file) throws IOException {
        RandomAccessFile randomFile;
        // 打开一个随机访问文件流，按只读方式
        randomFile = new RandomAccessFile(file, "r");
        // 文件长度，字节数
        long fileLength = randomFile.length();
        // 读文件的起始位置
        int beginIndex = (fileLength > 4) ? 0 : 0;
        // 将读文件的开始位置移到beginIndex位置。
        randomFile.seek(beginIndex);
        byte[] bytes = new byte[10];
        int byteread = 0;
        StringBuilder builder = new StringBuilder();
        // 一次读10个字节，如果文件内容不足10个字节，则读剩下的字节。
        // 将一次读取的字节数赋给byteread
        while ((byteread = randomFile.read(bytes)) != -1) {
            builder.append(new String(bytes));
        }
        randomFile.close();
        return builder.toString();
    }

    public static String readFileByBytes(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        byte[] tempbytes = new byte[100];
        int byteread = 0;
        // 读入多个字节到字节数组中，byteread为一次读入的字节数
        StringBuilder builder = new StringBuilder();
        while ((byteread = in.read(tempbytes)) != -1) {
            builder.append(new String(tempbytes));
        }
        in.close();
        return builder.toString();
    }

    public static byte[] toByteArray(File file) throws IOException {
        FileChannel fc=new RandomAccessFile(file, "r").getChannel();
        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                fc.size()).load();
        //System.out.println(byteBuffer.isLoaded());
        byte[] result = new byte[(int) fc.size()];
        if (byteBuffer.remaining() > 0) {
            // System.out.println("remain");
            byteBuffer.get(result, 0, byteBuffer.remaining());
        }
        fc.close();
        return result;
    }

}
