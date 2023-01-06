package com.mrshiehx.file.manager.enums;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.file.openers.APKFileOpener;
import com.mrshiehx.file.manager.file.openers.AudioFileOpener;
import com.mrshiehx.file.manager.file.openers.FileOpener;
import com.mrshiehx.file.manager.file.openers.MoreOpenersAllTypes;
import com.mrshiehx.file.manager.file.openers.MoreOpenersDefaultType;
import com.mrshiehx.file.manager.file.openers.PictureFileOpener;
import com.mrshiehx.file.manager.file.openers.TextFileOpener;
import com.mrshiehx.file.manager.utils.ResourceUtils;

public enum FileViewer {
    TEXT_EDITOR(ResourceUtils.getTextByLocale(R.string.file_viewer_text_editor), new TextFileOpener()),
    PICTURE_VIEWER(ResourceUtils.getTextByLocale(R.string.file_viewer_picture_viewer), new PictureFileOpener()),
    AUDIO_PLAYER(ResourceUtils.getTextByLocale(R.string.file_viewer_audio_player), new AudioFileOpener()),
    APK(ResourceUtils.getTextByLocale(R.string.file_viewer_apk), new APKFileOpener()),
    MORE(ResourceUtils.getTextByLocale(R.string.file_viewer_more), new MoreOpenersDefaultType()),
    MORE_ALL_TYPES(ResourceUtils.getTextByLocale(R.string.file_viewer_more_all_types), new MoreOpenersAllTypes());

    private final CharSequence displayName;
    private final FileOpener opener;

    FileViewer(CharSequence displayName, FileOpener opener) {
        this.displayName = displayName;
        this.opener = opener;
    }

    public CharSequence getDisplayName() {
        return displayName;
    }

    public FileOpener getOpener() {
        return opener;
    }
}
