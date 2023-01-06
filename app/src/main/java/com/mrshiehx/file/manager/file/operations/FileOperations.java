package com.mrshiehx.file.manager.file.operations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.beans.fileItem.FileItem;
import com.mrshiehx.file.manager.beans.fileItem.RootFileItem;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.LsParser;
import com.mrshiehx.file.manager.utils.MIMETypeUtils;
import com.mrshiehx.file.manager.utils.PathUtils;
import com.mrshiehx.file.manager.utils.ShellUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOperations {
    private FileOperations() {
    }

    public static void openFile(Context context, AbstractFileItem item) {
        FileOpener opener = item.getOpener();
        if (opener != null) {
            opener.open(context, item);
        } else {
            FileOperationsDialogs.showOpenMethodDialog(context, item);
            //Toast.makeText(context, getText(R.string.message_unopenable_file), Toast.LENGTH_SHORT).show();
        }
    }

    public static void installApk(Context context, AbstractFileItem file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //安装完成后，启动app（源码中少了这句话）
        try {
            //兼容7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, SharedVariables.getFileProviderPackageName(), new File(file.getAbsolutePath()));
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
                intent.setDataAndType(Uri.fromFile(new File(file.getAbsolutePath())), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate the size, folders number and files number of the file
     *
     * @param file Target File or Folder
     * @return A long array: [0]: file or folder size; [1]: folders number; [2]: files number. If the thread is interrupted suddenly, it will return null
     **/
    public static long[] calculate(AbstractFileItem file) {
        if (file instanceof FileItem) {
            return calculate(((FileItem) file).getFile());
        } else if (file instanceof RootFileItem) {
            return calculate(((RootFileItem) file));
        }
        return null;
    }

    /**
     * @see #calculate(AbstractFileItem)
     */
    public static long[] calculate(RootFileItem item) {
        long totalSize = 0;
        long folders = 0;
        long files = 0;
        try {
            List<String> out = ShellUtils.executeSuCommand("ls -a -R -l '" + PathUtils.toDirectoryPath(item.getAbsolutePath()) + "'").getOut();
            String pathNow = null;
            // <父,     Map<子,     大小>>
            Map<String, Map<String, Long>> map = new HashMap<>();
            for (int i = 0; i < out.size(); i++) {
                String s = out.get(i);
                if (s.isEmpty()) {
                    //pathNow = null;
                    continue;
                }
                if (s.startsWith("total "))
                    continue;

                if ((i == 0 || out.get(i - 1).isEmpty()) && (s.length() > 1 && s.endsWith(":"))) {
                    pathNow = PathUtils.toDirectoryPath(s.substring(0, s.length() - 1).replaceAll("/+", "/"));
                    map.put(pathNow, new HashMap<>());
                    continue;
                }
                if (Thread.currentThread().isInterrupted()) return null;

                if (!Utils.isEmpty(pathNow)) {
                    try {
                        LsParser lsParser = new LsParser(s, null);
                        String name = lsParser.getOnlyFileName();
                        if (name.equals("..") || name.equals("."))
                            continue;
                        String absPath = pathNow + name;
                        long size = lsParser.getFileSize();
                        map.get(pathNow).put(absPath, size);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (Thread.currentThread().isInterrupted()) return null;

            for (Map.Entry<String, Map<String, Long>> e1 : map.entrySet()) {
                for (Map.Entry<String, Long> e2 : e1.getValue().entrySet()) {
                    if (Thread.currentThread().isInterrupted()) return null;
                    String path = e2.getKey();
                    if (map.containsKey(PathUtils.toDirectoryPath(path))) {
                        folders++;
                    } else {
                        files++;
                        totalSize += e2.getValue();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return new long[]{totalSize, folders, files};
    }

    /**
     * @see #calculate(AbstractFileItem)
     */
    public static long[] calculate(File file) {
        long size = 0;
        long folders = 0;
        long files = 0;
        File[] fileList = file.listFiles();
        if (fileList != null) {
            if (file.isDirectory()) {
                for (File value : fileList) {
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    if (value.isDirectory()) {
                        long[] var = calculate(value);
                        if (var == null) continue;
                        size = size + var[0];
                        folders++;
                        folders += var[1];
                        files += var[2];
                    } else {
                        size = size + value.length();
                        files++;
                    }
                }
            } else {
                files++;
                size += file.length();
            }
        }
        return new long[]{size, folders, files};
    }

    public static void openFileByOtherApplication(Context context, AbstractFileItem file) {
        openFileByOtherApplication(context, file, MIMETypeUtils.getMIMEType(file.getAbsolutePath()));
    }

    public static void openFileByOtherApplication(Context context, AbstractFileItem file, String type) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uriForFile;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uriForFile = FileProvider.getUriForFile(context,
                    SharedVariables.getFileProviderPackageName(),
                    new File(file.getAbsolutePath()));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uriForFile = Uri.fromFile(new File(file.getAbsolutePath()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uriForFile, type);
        context.startActivity(intent);
    }
}
