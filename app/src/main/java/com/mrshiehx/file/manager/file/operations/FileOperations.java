package com.mrshiehx.file.manager.file.operations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.mrshiehx.file.manager.beans.FileItem;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.MIMETypeUtils;

import java.io.File;

public class FileOperations {
    private FileOperations(){}
    public static void openFile(Context context,FileItem item, File file){
        FileOpener opener=item.getOpener();
        if(opener!=null){
            opener.open(context,file);
        }else{
            FileOperationsDialogs.showOpenMethodDialog(context,file);
            //Toast.makeText(context, getText(R.string.message_unopenable_file), Toast.LENGTH_SHORT).show();
        }
    }

    public static void installApk(Context context,File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //安装完成后，启动app（源码中少了这句话）
        try {
            //兼容7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, SharedVariables.getFileProviderPackageName(), file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        //注意这个是8.0新API
                        Intent intent2 = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent2);
                    }
                }
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                context.startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * (Useless) Calculate the size, folders number and files number of the file
     * @param activity The first param is {@link android.app.Activity} not {@link android.content.Context} because the method runOnUiThread is not in {@link android.content.Context}
     * @param file Target File or Folder
     * @param bytes The TextView for showing files bytes, could be null
     * @param formatted The TextView for showing the formatted size of files, could be null
     * @param foldersTv The TextView for showing folders number, could be null
     * @param filesTV The TextView for showing files number, could be null
     * @param add don't explain formatted don't do it
     * @return [0]:file or folder size [1]:folders number [2]:files number
     **/
    public static long[] calculate(Activity activity, File file, TextView bytes, TextView formatted, TextView foldersTv, TextView filesTV, boolean add){
        long size=0;
        long folders=0;
        long files=0;
        File[] fileList = file.listFiles();
        if(fileList!=null) {
            if(file.isDirectory()) {
                for (File value : fileList) {
                    if (value.isDirectory()) {
                        long[]var=calculate(activity,value,bytes,formatted,foldersTv,filesTV,add);
                        size = size + (long) var[0];
                        folders++;
                        folders+=(int)var[1];
                        files+=(int)var[2];
                    } else {
                        size = size + value.length();
                        files++;
                        if(filesTV!=null){
                            long finalFiles = files;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(add){
                                        filesTV.setText(String.valueOf(Integer.parseInt((String)filesTV.getText())+1));

                                    }else {
                                        filesTV.setText(String.valueOf(finalFiles));
                                    }
                                }
                            });
                        }
                        if(bytes!=null){
                            long finalBytes = size;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(add){
                                        bytes.setText(String.valueOf(Long.parseLong((String)bytes.getText())+value.length()));
                                    }else {
                                        bytes.setText(String.valueOf(finalBytes));
                                    }
                                }
                            });
                        }
                        if(formatted!=null){
                            long finalBytes = size;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    formatted.setText(FileUtils.getFormatSize(finalBytes));
                                }
                            });
                        }
                    }
                }
            }else{
                files++;
                size+=file.length();
                if(filesTV!=null){
                    long finalFiles = files;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(add){
                                filesTV.setText(String.valueOf(Integer.parseInt((String)filesTV.getText())+1));

                            }else {
                                filesTV.setText(String.valueOf(finalFiles));
                            }
                        }
                    });
                }
                if(bytes!=null){
                    long finalBytes = size;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(add){
                                bytes.setText(String.valueOf(Long.parseLong((String)bytes.getText())+file.length()));
                            }else {
                                bytes.setText(String.valueOf(finalBytes));
                            }
                        }
                    });
                }
                if(formatted!=null){
                    long finalBytes = size;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            formatted.setText(FileUtils.getFormatSize(finalBytes));
                        }
                    });
                }
            }
        }
        return new long[]{size,folders,files};
    }



    /**
     * Calculate the size, folders number and files number of the file
     * @param file Target File or Folder
     * @return [0]:file or folder size [1]:folders number [2]:files number
     **/
    public static long[] calculate(File file){
        long size=0;
        long folders=0;
        long files=0;
        File[] fileList = file.listFiles();
        if(fileList!=null) {
            if(file.isDirectory()) {
                for (File value : fileList) {
                    if (value.isDirectory()) {
                        long[]var=calculate(value);
                        size = size + (long) var[0];
                        folders++;
                        folders+=(int)var[1];
                        files+=(int)var[2];
                    } else {
                        size = size + value.length();
                        files++;
                    }
                }
            }else{
                files++;
                size+=file.length();
            }
        }
        return new long[]{size,folders,files};
    }

    public static void openFileByOtherApplication(Context context, File file) {
        openFileByOtherApplication(context, file, MIMETypeUtils.getMIMEType(file.getAbsolutePath()));
    }

    public static void openFileByOtherApplication(Context context, File file, String type){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uriForFile;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uriForFile = FileProvider.getUriForFile(context,
                    SharedVariables.getFileProviderPackageName(),
                    file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uriForFile = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uriForFile, type);
        context.startActivity(intent);
    }
}
