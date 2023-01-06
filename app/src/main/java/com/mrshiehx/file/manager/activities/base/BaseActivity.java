package com.mrshiehx.file.manager.activities.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.FileManagerActivity;
import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.utils.SystemUtils;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(this);
        init();
    }

    protected abstract void init();

    public static SharedPreferences getSharedPreferences() {
        return MSFMApplication.getSharedPreferences();
    }

    public static void init(Activity activity) {
        MSFMApplication.getInstance().addActivity(activity);
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (activity instanceof FileManagerActivity) {
            initTheme(activity, R.style.Theme_MSFileManager_Dark_NoActionBar, R.style.Theme_MSFileManager_NoActionBar);
        } else {
            initTheme(activity, R.style.Theme_MSFileManager_Dark, R.style.Theme_MSFileManager);
        }
        String language = sharedPreferences.getString("language", "auto");
        String[] languageAndCountry2 = new String[]{"en", "US"};
        if (language.equals("auto")) {
            languageAndCountry2 = new String[]{Locale.getDefault().getLanguage(), Locale.getDefault().getCountry().toUpperCase()};
        } else {
            try {
                languageAndCountry2 = language.split("_");
            } catch (Throwable ignored) {
            }
        }
        SystemUtils.setLanguage(activity, languageAndCountry2[0], languageAndCountry2[1]);
    }

    public static void initTheme(Context context, @StyleRes int dark, @StyleRes int notDark) {
        boolean darkTheme = getSharedPreferences().getBoolean("darkTheme", false);
        if (darkTheme) {
            context.setTheme(dark);
        } else {
            context.setTheme(notDark);
        }
    }
}
