package com.mrshiehx.file.manager.file.openers;

import android.content.Context;

import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;

public interface FileOpener {
    void open(Context context, AbstractFileItem file);
}
