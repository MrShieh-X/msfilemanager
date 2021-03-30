package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.file.operations.FileOperations;

import java.io.File;

public class VideoFileOpener implements FileOpener {
    @Override
    public void open(Context context, File file) {
        FileOperations.openFileByOtherApplication(context,file);
    }
}
