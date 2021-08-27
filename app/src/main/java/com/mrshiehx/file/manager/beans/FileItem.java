package com.mrshiehx.file.manager.beans;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;

import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.enums.FileType;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.shared.FileTypesFile;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.ImageUtils;
import com.mrshiehx.file.manager.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileItem {
    private File file;
    private final FileType type;
    private Drawable smallIcon;
    private boolean isBacker;

    public Drawable picture;

    public FileItem(File file){
        this(file,null);
    }

    public FileItem(File file, Drawable smallIcon){
        this.file=file;
        this.smallIcon=smallIcon;
        if(file.isDirectory()){
            type = FileType.FOLDER;
        }else{
            int indexOf=getFileName().lastIndexOf(".");
            if(indexOf!=-1){
                String e=getFileName().substring(indexOf+1);
                JSONObject fileTypesJsonObject = FileTypesFile.getJSONObject();
                String getted=fileTypesJsonObject.optString(e.toLowerCase());
                if(!Utils.isEmpty(getted)) {
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
                }else{
                    type = FileType.UNKNOWN;
                }
            }else{
                type = FileType.UNKNOWN;
            }
        }
    }

    public File getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }

    public long getFileSize() {
        if(file.isDirectory()){
            return FileUtils.getFolderSize(file);
        }
        return file.length();
    }

    public String getFormattedFileSize() {
        return FileUtils.getFormatSize(this.getFileSize());
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getFileName() {
        return file.getName();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Drawable getSmallIcon(){
        return smallIcon;
    }

    public void setSmallIcon(Drawable smallIcon) {
        this.smallIcon = smallIcon;
    }

    public long getModifiedDate(){
        return file.lastModified();
    }

    public String getFormattedModifiedDate(String format){
        Date date=new Date(getModifiedDate());
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public void setIsBacker(boolean isBacker){
        this.isBacker=isBacker;
    }

    public boolean isBacker(){
        return isBacker;
    }

    public FileOpener getOpener(){
        return this.getType().getOpener();
    }

    public Drawable getIcon(){
        Drawable icon=type.getIcon();
        switch (type){
            case APK:
                PackageInfo packageInfo=Utils.getPackageInfo(MSFMApplication.getContext(),file.getAbsolutePath());
                if(packageInfo!=null){
                    try {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        appInfo.sourceDir = file.getAbsolutePath();
                        appInfo.publicSourceDir = file.getAbsolutePath();
                        Drawable iconFromApk=appInfo.loadIcon(MSFMApplication.getContext().getPackageManager());
                        if(iconFromApk!=null)icon = iconFromApk;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case VIDEO:
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(getAbsolutePath());
                    icon=ImageUtils.bitmap2Drawable(mmr.getFrameAtTime(1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case PICTURE:
                try{
                    icon=ImageUtils.inputStream2Drawable(new FileInputStream(getFile()));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                icon = type.getIcon();
                break;
        }
        return icon;
    }
}

/**Useless code*/
/**
 switch (e) {
 case "txt":
 type = FileType.TEXT;
 break;
 case "png":
 case "jpg":
 case "jpeg":
 type = FileType.PICTURE;
 break;
 case "mp4":
 type = FileType.VIDEO;
 break;
 case "mp3":
 type = FileType.AUDIO;
 break;
 default:
 type=FileType.UNKNOWN;
 break;
 }*/
