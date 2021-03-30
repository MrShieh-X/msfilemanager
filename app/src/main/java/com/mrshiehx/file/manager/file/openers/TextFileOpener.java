package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.editors.activities.TextEditorActivity;

import java.io.File;

public class TextFileOpener implements FileOpener{
    @Override
    public void open(Context context, File file) {
        Utils.startActivityWithFilePath(context, TextEditorActivity.class,file.getAbsolutePath());
    }
}
