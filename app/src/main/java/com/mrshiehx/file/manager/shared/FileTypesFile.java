package com.mrshiehx.file.manager.shared;

import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.utils.FileUtils;

import org.json.JSONObject;

public class FileTypesFile {
    public static JSONObject fileTypesJsonObject;

    public static JSONObject getJSONObject() {
        if (fileTypesJsonObject == null) {
            try {
                fileTypesJsonObject = new JSONObject(FileUtils.getString(MSFMApplication.getContext().getAssets().open("fileTypes.json")));
            } catch (Exception e) {
                e.printStackTrace();
                fileTypesJsonObject = new JSONObject();
            }
        }
        return fileTypesJsonObject;
    }
}
