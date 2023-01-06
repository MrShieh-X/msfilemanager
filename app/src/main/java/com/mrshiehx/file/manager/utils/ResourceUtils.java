package com.mrshiehx.file.manager.utils;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.mrshiehx.file.manager.BuildConfig;
import com.mrshiehx.file.manager.application.MSFMApplication;

import java.util.Locale;

public class ResourceUtils {
    public static Drawable getDrawable(@DrawableRes int id) {
        return MSFMApplication.getContext().getResources().getDrawable(id);
    }

    public static String getString(@StringRes int id) {
        return MSFMApplication.getContext().getResources().getString(id);
    }

    public static CharSequence getText(@StringRes int id) {
        return MSFMApplication.getContext().getResources().getText(id);
    }

    public static CharSequence getTextByLocale(@StringRes int idRes) {
        String language = MSFMApplication.getSharedPreferences().getString("language", "auto");
        String[] languageAndCountry2 = new String[]{"en", "US"};
        if (language.equals("auto")) {
            languageAndCountry2 = new String[]{Locale.getDefault().getLanguage(), Locale.getDefault().getCountry().toUpperCase()};
        } else {
            try {
                languageAndCountry2 = language.split("_");
            } catch (Throwable ignored) {
            }
        }
        return getTextByLocale(idRes, languageAndCountry2[0], languageAndCountry2[1]);
    }

    public static CharSequence getTextByLocale(@StringRes int stringId, String language, String country) {
        Resources resources = getApplicationResource(MSFMApplication.getContext().getApplicationContext().getPackageManager(),
                BuildConfig.APPLICATION_ID, new Locale(language, country));
        if (resources == null) {
            return "";
        } else {
            try {
                return resources.getText(stringId);
            } catch (Exception e) {
                return "";
            }
        }
    }

    private static Resources getApplicationResource(PackageManager pm, String pkgName, Locale l) {
        Resources resourceForApplication = null;
        try {
            resourceForApplication = pm.getResourcesForApplication(pkgName);
            Configuration config = resourceForApplication.getConfiguration();
            config.locale = l;
            resourceForApplication.updateConfiguration(config, null);
        } catch (PackageManager.NameNotFoundException ignored) {

        }
        return resourceForApplication;
    }
}
