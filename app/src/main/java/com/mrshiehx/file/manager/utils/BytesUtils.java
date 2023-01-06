package com.mrshiehx.file.manager.utils;

import static com.mrshiehx.file.manager.utils.Utils.bytesToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class BytesUtils {
    public static String getMD5(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("MD5").digest(bytes));
    }

    public static String getSHA1(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("SHA1").digest(bytes));
    }

    public static String getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    public static String getSHA224(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("SHA-224").digest(bytes));
    }

    public static String getSHA384(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("SHA-384").digest(bytes));
    }

    public static String getSHA512(byte[] bytes) throws NoSuchAlgorithmException {
        return bytesToString(MessageDigest.getInstance("SHA-512").digest(bytes));
    }

    public static String getCRC32(byte[] bytes) throws IOException {
        CRC32 crc32 = new CRC32();
        CheckedInputStream checkedinputstream = new CheckedInputStream(new ByteArrayInputStream(bytes), crc32);
        while (checkedinputstream.read() != -1) ;
        checkedinputstream.close();
        return String.format("%08x", crc32.getValue());
    }
}
