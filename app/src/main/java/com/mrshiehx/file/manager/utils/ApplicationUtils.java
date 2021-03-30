package com.mrshiehx.file.manager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.mrshiehx.file.manager.application.MSFMApplication;

public class ApplicationUtils {
    public static int getVersionCode() {
        Context context=MSFMApplication.getContext();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName() {
        Context context=MSFMApplication.getContext();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AlertDialog showDialog(Context context, CharSequence title, CharSequence message, CharSequence positiveButtonText, CharSequence negativeButtonText, CharSequence neutralButtonText, DialogInterface.OnClickListener positiveButtonOnClickListener, DialogInterface.OnClickListener negativeButtonOnClickListener, DialogInterface.OnClickListener neutralButtonOnClickListener, boolean cancelable){
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        if(null!=title)
            dialog.setTitle(title);
        if(null!=message)
            dialog.setMessage(message);
        if(null!=positiveButtonText)
            dialog.setPositiveButton(positiveButtonText,positiveButtonOnClickListener);
        if(null!=negativeButtonText)
            dialog.setNegativeButton(negativeButtonText,negativeButtonOnClickListener);
        if(null!=neutralButtonText)
            dialog.setNeutralButton(neutralButtonText,neutralButtonOnClickListener);
        dialog.setCancelable(cancelable);
        return dialog.show();
    }
}
