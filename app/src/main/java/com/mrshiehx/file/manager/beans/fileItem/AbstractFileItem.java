package com.mrshiehx.file.manager.beans.fileItem;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;

import com.mrshiehx.file.manager.adapters.FilesAdapter;
import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.enums.FileType;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.shared.FileTypesFile;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.ImageUtils;
import com.mrshiehx.file.manager.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractFileItem implements Serializable {
    protected final boolean isBacker;
    protected final transient Drawable smallIcon;
    protected final FileType type;
    public transient Drawable icon;

    public abstract String getAbsolutePath();

    public abstract String getFileName();

    public abstract long getFileSize();

    public abstract long getModifiedDate();

    public abstract boolean isDirectory();

    public abstract boolean isSymbolicLink();

    /**
     * 如果该文件不是一个链接，则返回null，否则返回链接所指向的文件路径。
     **/
    public abstract String getLinkTo();

    /**
     * 支持删除文件夹
     **/
    public abstract boolean delete();

    public abstract boolean renameTo(File dest);

    public abstract byte[] getFileBytes() throws IOException;

    public abstract String getFileContent() throws IOException;

    public abstract void appendBytes(byte[] bytes) throws IOException;

    public abstract void modifyAllBytes(byte[] bytes) throws IOException;

    public abstract void appendBytes(String content) throws IOException;

    public abstract void modifyAllBytes(String content) throws IOException;

    public abstract int modifyDate(String date) throws Exception;

    public abstract boolean exists();

    public abstract boolean createNewFile() throws IOException;

    protected AbstractFileItem(boolean isBacker, Drawable smallIcon, boolean isDirectory, String fileName) {
        this.isBacker = isBacker;
        this.smallIcon = smallIcon;
        if (isDirectory) {
            type = FileType.FOLDER;
        } else {
            int indexOf = fileName.lastIndexOf(".");
            if (indexOf != -1) {
                String e = fileName.substring(indexOf + 1);
                JSONObject fileTypesJsonObject = FileTypesFile.getJSONObject();
                String getted = fileTypesJsonObject.optString(e.toLowerCase());
                if (!Utils.isEmpty(getted)) {
                    switch (getted.toLowerCase()) {
                        case "text":
                            type = FileType.TEXT;
                            break;
                        case "picture":
                            type = FileType.PICTURE;
                            break;
                        case "video":
                            type = FileType.VIDEO;
                            break;
                        case "audio":
                            type = FileType.AUDIO;
                            break;
                        case "apk":
                            type = FileType.APK;
                            break;
                        default:
                            type = FileType.UNKNOWN;
                            break;
                    }
                } else {
                    type = FileType.UNKNOWN;
                }
            } else {
                type = FileType.UNKNOWN;
            }
        }
    }


    public boolean isBacker() {
        return isBacker;
    }

    public FileType getType() {
        return type;
    }

    public Drawable getSmallIcon() {
        return smallIcon;
    }

    public String getFormattedFileSize() {
        return FileUtils.getFormatSize(this.getFileSize());
    }

    public String getFormattedModifiedDate(String format) {
        Date date = new Date(getModifiedDate());
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public FileOpener getOpener() {
        return this.getType().getOpener();
    }

    public Drawable getIcon() {
        /**code Waiting for improvement*/
        Drawable icon = getType().getIcon();
        switch (getType()) {
            case APK:
                PackageInfo packageInfo = Utils.getPackageInfo(MSFMApplication.getContext(), getAbsolutePath());
                if (packageInfo != null) {
                    try {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        appInfo.sourceDir = getAbsolutePath();
                        appInfo.publicSourceDir = getAbsolutePath();
                        Drawable iconFromApk = appInfo.loadIcon(MSFMApplication.getContext().getPackageManager());
                        if (iconFromApk != null) icon = iconFromApk;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case VIDEO:
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(getAbsolutePath());
                    icon = ImageUtils.bitmap2Drawable(mmr.getFrameAtTime(1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case PICTURE:
                try {
                    icon = ImageUtils.bitmap2Drawable(ImageUtils.getImageThumbnail(getFileBytes(), 120, 120));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        this.icon = icon;
        FilesAdapter.THUMBNAILS.put(getAbsolutePath().hashCode(), icon);
        return this.icon;
    }

    public String getParent() {
        String abs = getAbsolutePath();
        if ("/".equals(abs)) return null;
        return abs.substring(0, abs.lastIndexOf('/'));
    }

    public String getName() {
        return this.getFileName();
    }

    public double length() {
        return this.getFileSize();
    }

}
