package com.mrshiehx.file.manager.file.openers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.file.operations.FileOperationsDialogs;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;

public class APKFileOpener implements FileOpener{
    @Override
    public void open(Context context, File file) {
        try {
            String path = file.getAbsolutePath();
            Drawable icon = context.getResources().getDrawable(R.drawable.file_type_apk);
            String name = "";
            String version = "0.0.0";
            int versionCode = 0;
            String packageName = "";
            String size = FileUtils.getFormatSize(file.length());
            PackageInfo packageInfo = Utils.getPackageInfo(context, path);
            if (packageInfo != null) {
                try {
                    PackageManager pm=context.getPackageManager();
                    version = packageInfo.versionName;
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    applicationInfo.sourceDir = path;
                    applicationInfo.publicSourceDir = path;
                    Drawable iconFromApk = pm.getApplicationIcon(applicationInfo);
                    if (iconFromApk != null) {
                        icon = iconFromApk;
                    }
                    versionCode=packageInfo.versionCode;
                    packageName=packageInfo.packageName;
                    name=pm.getApplicationLabel(applicationInfo).toString();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, context.getText(R.string.message_failed_to_get_apk_information), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, context.getText(R.string.message_failed_to_get_apk_information), Toast.LENGTH_SHORT).show();
            }
            FileOperationsDialogs.showApkInformationDialog(context,file,icon,name,version,packageName,versionCode,size);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, context.getText(R.string.message_failed_to_open_file), Toast.LENGTH_SHORT).show();
        }
    }
}
