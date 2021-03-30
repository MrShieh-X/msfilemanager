package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.editors.activities.HEXEditorActivity;

import java.io.File;

public class HEXEditor implements FileOpener{
    @Override
    public void open(Context context, File file) {
        Utils.startActivityWithFilePath(context, HEXEditorActivity.class,file.getAbsolutePath());
    }
}
