package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.activities.PictureViewerActivity;

import java.io.File;

public class PictureFileOpener implements FileOpener{
    @Override
    public void open(Context context, File file) {
        Utils.startActivityWithFilePath(context, PictureViewerActivity.class,file.getAbsolutePath());
    }
}
