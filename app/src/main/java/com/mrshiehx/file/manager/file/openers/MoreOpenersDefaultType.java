package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.file.operations.FileOperations;
import com.mrshiehx.file.manager.utils.MIMETypeUtils;

public class MoreOpenersDefaultType implements FileOpener {
    @Override
    public void open(Context context, AbstractFileItem file) {
        FileOperations.openFileByOtherApplication(context, file, MIMETypeUtils.getMIMEType(file.getAbsolutePath()));
    }
}
