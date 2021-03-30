package com.mrshiehx.file.manager.shared.variables;

import android.os.Environment;

import java.io.File;

public class FilePaths {
    public static String getRootPath(){
        return "/";
    }

    public static File getSdcard(){
        return Environment.getExternalStorageDirectory();
    }

    public static File getSystemPart(){
        return Environment.getRootDirectory();
    }
}
