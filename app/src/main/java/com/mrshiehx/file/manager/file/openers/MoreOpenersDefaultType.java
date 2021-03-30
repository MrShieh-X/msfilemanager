package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.file.operations.FileOperations;
import com.mrshiehx.file.manager.utils.MIMETypeUtils;

import java.io.File;

public class MoreOpenersDefaultType implements FileOpener{
    @Override
    public void open(Context context, File file) {
        FileOperations.openFileByOtherApplication(context,file, MIMETypeUtils.getMIMEType(file.getAbsolutePath()));
    }
}
