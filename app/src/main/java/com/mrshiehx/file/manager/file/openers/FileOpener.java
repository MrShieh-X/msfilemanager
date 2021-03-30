package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import java.io.File;

public interface FileOpener {
    void open(Context context, File file);
}
