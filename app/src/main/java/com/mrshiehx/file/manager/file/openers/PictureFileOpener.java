package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.activities.PictureViewerActivity;

public class PictureFileOpener implements FileOpener {
    @Override
    public void open(Context context, AbstractFileItem file) {
        Utils.startActivityWithAFI(context, PictureViewerActivity.class, file);
    }
}
