package com.mrshiehx.file.manager.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.mrshiehx.file.manager.R;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static void goToWebsite(Context context, String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        context.startActivity(intent);
    }

    public static void sendMail(Context context, String receiver, String subject, String text) {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:"+receiver+"?subject="+subject+"&body="+text));
        //data.putExtra(Intent.EXTRA_EMAIL,receiver);
        //data.putExtra("subject", subject);
        //data.putExtra("text", text);
        //data.putExtra("body", text);
        try {
            context.startActivity(data);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, context.getText(R.string.message_no_mail_app), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /*public static void getApkInformation(Context context, String path) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager
                .getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null && packageInfo.versionName
                .equals(getUpdateInfo().newVersionName)) {
        }
        packageManager.getApplicationIcon()
    }*/


    public static PackageInfo getPackageInfo(Context context,String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
        /*if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            *//* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon *//*
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            String packageName = appInfo.packageName; // 得到包名
            String version = pkgInfo.versionName; // 得到版本信息
            *//* icon1和icon2其实是一样的 *//*
            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);
            Log.i(TAG, String.format("PkgInfo: %s", pkgInfoStr));
        }*/
        return pkgInfo;
    }

    public static String readHex(InputStream is) throws IOException {
        int bytesCounter = 0;
        int value = 0;
        StringBuilder sbHex = new StringBuilder();
        StringBuilder sbText = new StringBuilder();
        StringBuilder sbResult = new StringBuilder();

        while ((value = is.read()) != -1) {
            sbHex.append(String.format("%02X", value));

            if (!Character.isISOControl(value)) {
                sbText.append((char) value);
            } else {
                sbText.append("");
            }
            if (bytesCounter == 15) {
                sbResult.append(sbHex);
                sbHex.setLength(0);
                sbText.setLength(0);
                bytesCounter = 0;
            } else {
                bytesCounter++;
            }
        }
        //if still got content
        if (bytesCounter != 0) {
            for (; bytesCounter < 16; bytesCounter++) {
                sbHex.append("");
            }
            sbResult.append(sbHex);
        }
        is.close();
        return sbResult.toString();
    }

    public static void startActivityWithFilePath(Context context, Class<?> clazz, String filePath){
        Intent intent=new Intent();
        intent.setClass(context, clazz);
        intent.putExtra("filePath",filePath);
        intent.putExtra("backButton",true);
        context.startActivity(intent);
    }
}
