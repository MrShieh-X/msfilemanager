package com.mrshiehx.file.manager.utils;

import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.enums.SortMethod;

public class SharedPreferencesGetter {
    public static String getFileDateFormat() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    public static String getSortMethod() {
        return MSFMApplication.getSharedPreferences().getString("sort", SortMethod.BY_NAME.getName());
    }

    public static boolean getGetRoot() {
        return MSFMApplication.getSharedPreferences().getBoolean("getRoot", false);
    }
}
