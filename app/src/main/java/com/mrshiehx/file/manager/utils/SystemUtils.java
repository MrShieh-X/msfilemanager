package com.mrshiehx.file.manager.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.application.MSFMApplication;

import java.io.IOException;
import java.util.Locale;

public class SystemUtils {
    public static void setLanguage(Context context, String language, String country) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.locale = new Locale(language, country);
        resources.updateConfiguration(config, dm);
    }

    public static Process executeCommand(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }

    public static void copyWithToast(CharSequence content) {
        copy(content);
        Toast.makeText(MSFMApplication.getContext(), MSFMApplication.getContext().getText(R.string.message_success_copy), Toast.LENGTH_SHORT).show();
    }

    public static void copy(CharSequence content) {
        ClipboardManager clipboardManager = (ClipboardManager) MSFMApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("LABEL", content));
    }

    public static CharSequence getClipboardContent() {
        ClipboardManager clipboardManager = (ClipboardManager) MSFMApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboardManager.getPrimaryClip().getItemAt(0).getText();
    }
}
