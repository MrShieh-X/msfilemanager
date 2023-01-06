package com.mrshiehx.file.manager.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.LinkedList;
import java.util.List;

public class MSFMApplication extends Application {
    private final List<Activity> activityList = new LinkedList<Activity>();
    private static MSFMApplication instance;
    static Context context;
    static SharedPreferences sharedPreferences;

    /*private Process process;

    public Process getRootProcess() throws Exception {
        if (process == null) {
            process = Runtime.getRuntime().exec("su");
            if (process.waitFor() != 0) {
                process = null;
                throw new Exception(getText(R.string.message_failed_to_get_root).toString());
            }
        }
        return process;
    }*/
    public MSFMApplication() {
    }

    public static MSFMApplication getInstance() {
        if (null == instance) {
            instance = new MSFMApplication();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Context getContext() {
        return context;
    }
}
