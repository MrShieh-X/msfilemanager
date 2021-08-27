package com.mrshiehx.file.manager.enums;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.file.openers.APKFileOpener;
import com.mrshiehx.file.manager.file.openers.AudioFileOpener;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.file.openers.PictureFileOpener;
import com.mrshiehx.file.manager.file.openers.TextFileOpener;
import com.mrshiehx.file.manager.file.openers.VideoFileOpener;
import com.mrshiehx.file.manager.utils.ImageUtils;

import static com.mrshiehx.file.manager.utils.ResourceUtils.getDrawable;
import static com.mrshiehx.file.manager.utils.ResourceUtils.getTextByLocale;

public enum FileType {
    FOLDER(getDrawable(R.drawable.gray_file_type_folder),getTextByLocale(R.string.file_type_folder)),
    UNKNOWN(getDrawable(R.drawable.file_type_unknown),getTextByLocale(R.string.file_type_unknown)),
    TEXT(getDrawable(R.drawable.file_type_text),getTextByLocale(R.string.file_type_text),new TextFileOpener()),
    PICTURE(getDrawable(R.drawable.file_type_picture),getTextByLocale(R.string.file_type_picture),new PictureFileOpener()),
    VIDEO(getDrawable(R.drawable.file_type_video),getTextByLocale(R.string.file_type_video),new VideoFileOpener()),
    AUDIO(getDrawable(R.drawable.file_type_audio),getTextByLocale(R.string.file_type_audio),new AudioFileOpener()),
    APK(getDrawable(R.drawable.file_type_apk),getTextByLocale(R.string.file_type_apk),new APKFileOpener());

    private final Drawable icon;
    private final CharSequence displayName;
    private final FileOpener opener;
    FileType(Drawable icon, CharSequence displayName){
        this(icon,displayName,null);
    }
    FileType(Drawable icon, CharSequence displayName, FileOpener opener){
        this.icon=icon;
        this.displayName=displayName;
        this.opener=opener;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Bitmap getIconBitmap() {
        return ImageUtils.drawable2Bitmap(icon);
    }

    public CharSequence getDisplayName() {
        return displayName;
    }

    public FileOpener getOpener() {
        return opener;
    }
}
